package com.example.practice3kormiletsv.data.repository

import com.example.practice3kormiletsv.data.mapper.toMeal
import com.example.practice3kormiletsv.data.mapper.toSummary
import com.example.practice3kormiletsv.data.remote.MealDbApi
import com.example.practice3kormiletsv.domain.model.FailureReason
import com.example.practice3kormiletsv.domain.model.FilterOption
import com.example.practice3kormiletsv.domain.model.FilterType
import com.example.practice3kormiletsv.domain.model.Meal
import com.example.practice3kormiletsv.domain.model.MealFailure
import com.example.practice3kormiletsv.domain.model.MealFilter
import com.example.practice3kormiletsv.domain.model.MealSummary
import com.example.practice3kormiletsv.domain.repository.MealRepository
import com.google.gson.JsonParseException
import com.google.gson.stream.MalformedJsonException
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class NetworkMealRepository(
    private val api: MealDbApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : MealRepository {
    private data class Request(val query: String, val filter: MealFilter?)

    private val lists = ConcurrentHashMap<Request, List<MealSummary>>()
    private val details = ConcurrentHashMap<String, Meal>()
    private val options = ConcurrentHashMap<FilterType, List<FilterOption>>()

    override fun observeMeals(query: String, filter: MealFilter?, forceRefresh: Boolean): Flow<List<MealSummary>> = flow {
        val request = Request(if (filter == null) query.lowercase(Locale.ROOT) else "", filter)
        val meals = if (!forceRefresh && lists.containsKey(request)) {
            lists.getValue(request)
        } else {
            networkCall {
                val response = when (filter?.type) {
                    FilterType.CATEGORY -> api.filter(category = filter.value)
                    FilterType.AREA -> api.filter(area = filter.value)
                    FilterType.INGREDIENT -> api.filter(ingredient = filter.value)
                    null -> api.search(query)
                }
                response.meals.orEmpty().map {
                    it.toSummary(
                        category = filter?.takeIf { it.type == FilterType.CATEGORY }?.value.orEmpty(),
                        area = filter?.takeIf { it.type == FilterType.AREA }?.value.orEmpty()
                    )
                }.distinctBy { it.id }.also { lists[request] = it }
            }
        }
        emit(if (filter == null || query.isBlank()) meals else meals.filter { it.name.contains(query, ignoreCase = true) })
    }.flowOn(ioDispatcher)

    override suspend fun getMeal(id: String, forceRefresh: Boolean): Meal = withContext(ioDispatcher) {
        if (!forceRefresh) details[id]?.let { return@withContext it }
        networkCall {
            val dto = api.lookup(id).meals?.firstOrNull { it.idMeal == id }
                ?: throw MealFailure(FailureReason.NOT_FOUND)
            dto.toMeal().also { details[id] = it }
        }
    }

    override suspend fun getFilterOptions(type: FilterType, forceRefresh: Boolean): List<FilterOption> = withContext(ioDispatcher) {
        if (!forceRefresh) options[type]?.let { return@withContext it }
        networkCall {
            val response = when (type) {
                FilterType.CATEGORY -> api.categories()
                FilterType.AREA -> api.areas()
                FilterType.INGREDIENT -> api.ingredients()
            }
            val rows = response.meals ?: throw MealFailure(FailureReason.INVALID_DATA)
            rows.mapNotNull {
                val value = when (type) {
                    FilterType.CATEGORY -> it.strCategory
                    FilterType.AREA -> it.strArea
                    FilterType.INGREDIENT -> it.strIngredient
                }
                value?.trim()?.takeIf(String::isNotEmpty)?.let(::FilterOption)
            }.distinctBy { it.value.lowercase(Locale.ROOT) }.sortedBy { it.value }.also { options[type] = it }
        }
    }

    private suspend fun <T> networkCall(block: suspend () -> T): T = try {
        block()
    } catch (error: CancellationException) {
        throw error
    } catch (error: MealFailure) {
        throw error
    } catch (error: SocketTimeoutException) {
        throw MealFailure(FailureReason.TIMEOUT, error)
    } catch (error: HttpException) {
        throw MealFailure(FailureReason.SERVER, error)
    } catch (error: JsonParseException) {
        throw MealFailure(FailureReason.INVALID_DATA, error)
    } catch (error: MalformedJsonException) {
        throw MealFailure(FailureReason.INVALID_DATA, error)
    } catch (error: IOException) {
        throw MealFailure(FailureReason.NETWORK, error)
    }
}
