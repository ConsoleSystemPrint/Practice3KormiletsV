package com.example.practice3kormiletsv.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.practice3kormiletsv.R
import com.example.practice3kormiletsv.domain.model.FilterOption
import com.example.practice3kormiletsv.domain.model.FilterType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSettingsScreen(
    state: SearchSettingsUiState,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onTypeChange: (FilterType?) -> Unit,
    onValueChange: (String) -> Unit,
    onRetry: () -> Unit,
    onRetryOptions: () -> Unit,
    onReset: () -> Unit,
    onSave: () -> Unit
) {
    var showOptions by rememberSaveable { mutableStateOf(false) }
    var optionQuery by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    BackHandler(enabled = state.isSaving) {}
    Scaffold(
        topBar = {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, enabled = !state.isSaving) { Icon(painterResource(R.drawable.ic_back), "Назад") }
                Text("Настройки поиска", style = MaterialTheme.typography.titleLarge)
            }
        },
        bottomBar = {
            Row(Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onReset, enabled = !state.isLoading && !state.isSaving, modifier = Modifier.weight(1f).testTag("reset_settings")) { Text("Сбросить") }
                Button(onClick = { focusManager.clearFocus(); onSave() }, enabled = state.canSave, modifier = Modifier.weight(1f).testTag("save_settings")) {
                    if (state.isSaving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    else Text("Готово")
                }
            }
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding).testTag("settings_screen"), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            if (state.isLoading) {
                item { if (state.error != null) ErrorState(state.error, onRetry) else LoadingState("Открываем настройки…") }
            } else {
                item {
                    Text("Найдите свой рецепт", style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.height(12.dp))
                    Text("Изменения применяются по кнопке «Готово» и сохраняются после выхода из приложения.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                item {
                    Text("1. Название рецепта", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.query, onValueChange = onQueryChange,
                        enabled = !state.isSaving, singleLine = true,
                        placeholder = { Text("Например, chicken или salmon") },
                        leadingIcon = { Icon(painterResource(R.drawable.ic_search), null) },
                        shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().testTag("settings_query"),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Названия в TheMealDB — на английском. С фильтром поиск выполняется внутри выбранной подборки.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                item {
                    Text("2. Тип фильтра", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item { FilterChip(selected = state.filterType == null, onClick = { onTypeChange(null) }, enabled = !state.isSaving, label = { Text("Без фильтра") }, modifier = Modifier.testTag("filter_type_NONE")) }
                        items(FilterType.entries) { type ->
                            FilterChip(selected = state.filterType == type, onClick = { onTypeChange(type) }, enabled = !state.isSaving, label = { Text(type.label) }, modifier = Modifier.testTag("filter_type_${type.name}"))
                        }
                    }
                }
                state.filterType?.let { type ->
                    item {
                        Text("3. ${type.label}", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(onClick = { focusManager.clearFocus(); optionQuery = ""; showOptions = true }, enabled = !state.isSaving, modifier = Modifier.fillMaxWidth().testTag("select_filter_value"), contentPadding = PaddingValues(16.dp), shape = RoundedCornerShape(16.dp)) {
                            Text(if (state.filterValue.isBlank()) "Выберите значение" else FilterOption(state.filterValue).label(type), Modifier.weight(1f))
                            Icon(painterResource(R.drawable.ic_filter), null, Modifier.size(20.dp))
                        }
                    }
                }
                if (state.error != null) item { Text(state.error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
            }
        }
    }
    if (showOptions && state.filterType != null) {
        val type = state.filterType
        ModalBottomSheet(onDismissRequest = { showOptions = false }) {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Text("Выберите: ${type.label.lowercase()}", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = optionQuery, onValueChange = { optionQuery = it }, placeholder = { Text("Найти значение") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                LazyColumn(Modifier.fillMaxWidth().heightIn(max = 360.dp), contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)) {
                    when {
                        state.optionsLoading -> item { LoadingState("Загружаем варианты…") }
                        state.optionsError != null -> item { ErrorState(state.optionsError, onRetryOptions) }
                        else -> {
                            val options = state.options.filter { it.label(type).contains(optionQuery, true) || it.value.contains(optionQuery, true) }
                            if (options.isEmpty()) item { Text("Ничего не найдено", Modifier.padding(16.dp)) }
                            items(options, key = { it.value }) { option ->
                                TextButton(onClick = { onValueChange(option.value); showOptions = false }, modifier = Modifier.fillMaxWidth().testTag("option_${option.value}")) {
                                    Text(option.label(type), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                                    if (option.value == state.filterValue) Text("✓")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
