package com.example.practice3kormiletsv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.practice3kormiletsv.R
import com.example.practice3kormiletsv.data.FilterOption
import com.example.practice3kormiletsv.data.FilterType
import com.example.practice3kormiletsv.data.MealFilter

@Composable
fun MealListScreen(
    state: MealsUiState,
    onQueryChange: (String) -> Unit,
    onFilterChange: (MealFilter?) -> Unit,
    filterOptions: (FilterType) -> List<FilterOption>,
    onReset: () -> Unit,
    onMealClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit
) {
    var showFilters by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
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
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth().testTag("recipe_search"),
                placeholder = { Text("Найти рецепт по названию") },
                leadingIcon = { Icon(painterResource(R.drawable.ic_search), null) },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) { Icon(painterResource(R.drawable.ic_close), "Очистить поиск") }
                    } else {
                        IconButton(onClick = { focusManager.clearFocus(); showFilters = true }, modifier = Modifier.testTag("open_filters")) {
                            Icon(painterResource(R.drawable.ic_filter), "Открыть фильтры")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedBorderColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
            )
        }
        item(key = "categories") {
            val categories = remember { filterOptions(FilterType.CATEGORY) }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(selected = state.filter == null, onClick = { onFilterChange(null) }, label = { Text("Все") }, modifier = Modifier.testTag("filter_all"))
                }
                items(categories, key = { it.value }) { option ->
                    FilterChip(
                        selected = state.filter?.type == FilterType.CATEGORY && state.filter.value == option.value,
                        onClick = { onFilterChange(MealFilter(FilterType.CATEGORY, option.value, option.label)) },
                        label = { Text(option.label) },
                        modifier = Modifier.testTag("category_${option.value}")
                    )
                }
            }
        }
        item(key = "results_header") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(if (state.query.isBlank() && state.filter == null) "Рецепты для вас" else "Результаты поиска", style = MaterialTheme.typography.titleLarge)
                    state.filter?.let { Text(it.label, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall) }
                }
                TextButton(onClick = { focusManager.clearFocus(); showFilters = true }, modifier = Modifier.testTag("filter_button")) {
                    Icon(painterResource(R.drawable.ic_filter), null, Modifier.size(18.dp))
                    Text("  ${state.meals.size}")
                }
            }
        }
        if (state.meals.isEmpty()) {
            item(key = "empty") {
                EmptyState("Ничего не найдено", "Попробуйте другое название или сбросьте выбранный фильтр.", "Сбросить поиск", onReset, R.drawable.ic_search)
            }
        } else {
            items(state.meals, key = { it.id }, contentType = { "meal" }) { meal ->
                MealCard(meal, meal.id in state.favoriteIds, { focusManager.clearFocus(); onMealClick(meal.id) }, { onFavoriteClick(meal.id) })
            }
        }
        item(key = "attribution") {
            Text("Рецепты и фотографии · TheMealDB", Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    if (showFilters) {
        FilterSheet(state.filter, filterOptions, onSelect = { onFilterChange(it); showFilters = false }, onDismiss = { showFilters = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheet(
    selected: MealFilter?,
    options: (FilterType) -> List<FilterOption>,
    onSelect: (MealFilter?) -> Unit,
    onDismiss: () -> Unit
) {
    var type by rememberSaveable { mutableStateOf(selected?.type ?: FilterType.CATEGORY) }
    var optionQuery by rememberSaveable { mutableStateOf("") }
    val available = remember(type, optionQuery) { options(type).filter { it.label.contains(optionQuery, true) } }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            Text("Подберите свой рецепт", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text("Выберите один фильтр", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            SecondaryTabRow(selectedTabIndex = type.ordinal, containerColor = Color.Transparent) {
                FilterType.entries.forEach { item ->
                    Tab(selected = type == item, onClick = { type = item; optionQuery = "" }, text = { Text(item.label) }, modifier = Modifier.testTag("filter_type_${item.name}"))
                }
            }
            if (type == FilterType.INGREDIENT) {
                OutlinedTextField(value = optionQuery, onValueChange = { optionQuery = it }, placeholder = { Text("Найти ингредиент") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 12.dp).testTag("ingredient_search"), shape = RoundedCornerShape(14.dp))
            }
            LazyColumn(Modifier.fillMaxWidth().heightIn(max = 360.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
                item { TextButton(onClick = { onSelect(null) }, modifier = Modifier.fillMaxWidth()) { Text("Показать все рецепты") } }
                items(available, key = { it.value }) { option ->
                    TextButton(onClick = { onSelect(MealFilter(type, option.value, option.label)) }, modifier = Modifier.fillMaxWidth().testTag("option_${option.value}")) {
                        Text(option.label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        if (selected?.type == type && selected.value.equals(option.value, true)) {
                            Icon(painterResource(R.drawable.ic_check), "Выбрано", Modifier.size(22.dp))
                        }
                    }
                }
                if (available.isEmpty()) { item { Text("Такого ингредиента нет в подборке", Modifier.padding(16.dp)) } }
            }
        }
    }
}

@Composable
fun FavoritesScreen(state: MealsUiState, onMealClick: (String) -> Unit, onFavoriteClick: (String) -> Unit, onBrowse: () -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.testTag("favorites_list")
    ) {
        item {
            BrandHeader()
            Spacer(Modifier.height(30.dp))
            Text("Ваша коллекция", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text("Избранные рецепты", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(10.dp))
            Text("Сохраните то, что хочется приготовить ещё раз.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
        }
        if (state.favorites.isEmpty()) {
            item { EmptyState("Пока здесь пусто", "Нажмите на сердечко рядом с рецептом, чтобы добавить его в свою коллекцию.", "Выбрать рецепт", onBrowse, R.drawable.ic_heart) }
        }
        items(state.favorites, key = { it.id }, contentType = { "meal" }) { meal ->
            MealCard(meal, true, { onMealClick(meal.id) }, { onFavoriteClick(meal.id) })
        }
    }
}
