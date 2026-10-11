package com.example.practice3kormiletsv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.practice3kormiletsv.R

@Composable
fun MealListScreen(
    state: MealsUiState,
    onSettingsClick: () -> Unit,
    onRefresh: () -> Unit,
    onMealClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.testTag("recipe_list")
    ) {
        item(key = "header") {
            BrandHeader()
            Spacer(Modifier.height(28.dp))
            Text("Вдохновение на каждый день", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text("Что приготовим\nсегодня?", style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(10.dp))
            Text("Любимые блюда со всего мира —\nв одной кулинарной книге.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(22.dp))
            OutlinedButton(onClick = onSettingsClick, modifier = Modifier.fillMaxWidth().testTag("open_settings"), contentPadding = PaddingValues(16.dp)) {
                BadgedBox(badge = { if (state.settingsActive) Badge(Modifier.testTag("settings_badge")) }) {
                    Icon(painterResource(R.drawable.ic_filter), "Настройки поиска")
                }
                Text("  Поиск и фильтры", style = MaterialTheme.typography.labelLarge)
            }
            if (state.settingsActive) {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (state.query.isNotBlank()) LabelPill("Поиск: ${state.query}", Modifier.weight(1f))
                    state.filter?.let { LabelPill(it.label, Modifier.weight(1f)) }
                }
            }
        }
        item(key = "results_header") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(if (state.settingsActive) "Результаты поиска" else "Рецепты для вас", style = MaterialTheme.typography.titleLarge)
                    if (!state.isLoading && state.error == null) Text("Рецептов: ${state.meals.size}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = onRefresh, enabled = !state.isLoading, modifier = Modifier.testTag("refresh_meals")) { Text("Обновить") }
            }
        }
        if (state.isLoading) {
            item(key = "loading") { LoadingState("Загружаем рецепты…", Modifier.testTag("meals_loading")) }
        } else if (state.error != null) {
            item(key = "error") { ErrorState(state.error, onRefresh, Modifier.testTag("meals_error")) }
        } else if (state.meals.isEmpty()) {
            item(key = "empty") {
                EmptyState("Ничего не найдено", "Попробуйте другое название или измените фильтр в настройках.", "Изменить настройки", onSettingsClick, R.drawable.ic_search)
            }
        } else {
            items(state.meals, key = { it.id }, contentType = { "meal" }) { meal ->
                MealCard(meal, meal.id in state.favoriteIds, { onMealClick(meal.id) }, { onFavoriteClick(meal.id) }, meal.id in state.pendingFavoriteIds)
            }
        }
        item(key = "attribution") {
            Text("Рецепты и фотографии · TheMealDB", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun FavoritesScreen(state: MealsUiState, onMealClick: (String) -> Unit, onFavoriteClick: (String) -> Unit, onRetry: () -> Unit, onBrowse: () -> Unit) {
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.testTag("favorites_screen")) {
        item {
            BrandHeader()
            Spacer(Modifier.height(30.dp))
            Text("Ваша кулинарная книга", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text("Избранное", style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(12.dp))
            Text("Рецепты и фотографии хранятся на устройстве и доступны без интернета.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Text("Сохранено: ${state.favorites.size}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
        when {
            state.favoritesLoading -> item { LoadingState("Открываем избранное…") }
            state.favoritesError != null -> item { ErrorState(state.favoritesError, onRetry) }
            state.favorites.isEmpty() -> item { EmptyState("Здесь будут любимые блюда", "Нажмите на сердечко в списке или карточке рецепта и дождитесь сохранения.", "Выбрать рецепт", onBrowse, R.drawable.ic_heart) }
            else -> items(state.favorites, key = { it.id }) { meal ->
                MealCard(meal, true, { onMealClick(meal.id) }, { onFavoriteClick(meal.id) }, meal.id in state.pendingFavoriteIds)
            }
        }
    }
}
