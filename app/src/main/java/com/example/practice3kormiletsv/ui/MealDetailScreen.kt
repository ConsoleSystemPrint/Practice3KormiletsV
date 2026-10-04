package com.example.practice3kormiletsv.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.example.practice3kormiletsv.R
import com.example.practice3kormiletsv.data.Meal

@Composable
fun MealDetailScreen(meal: Meal?, isFavorite: Boolean, onBack: () -> Unit, onFavoriteClick: () -> Unit) {
    if (meal == null) {
        Column {
            IconButton(onClick = onBack) { Icon(painterResource(R.drawable.ic_back), "Назад") }
            EmptyState("Рецепт не найден", "Вернитесь к списку и выберите другое блюдо.", "К рецептам", onBack, R.drawable.ic_recipes)
        }
        return
    }
    var section by rememberSaveable(meal.summary.id) { mutableStateOf(0) }
    val uriHandler = LocalUriHandler.current

    LazyColumn(
        modifier = Modifier.testTag("meal_detail"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item(key = "photo") {
            Box(Modifier.fillMaxWidth().height(320.dp)) {
                Image(painterResource(meal.summary.imageRes), meal.summary.name, Modifier.matchParentSize(), contentScale = ContentScale.Crop)
                Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.34f), Color.Transparent, Color.Black.copy(alpha = 0.2f)))))
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerLow) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back")) { Icon(painterResource(R.drawable.ic_back), "Назад") }
                    }
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerLow) {
                        FavoriteButton(isFavorite, onFavoriteClick, Modifier.testTag("detail_favorite"))
                    }
                }
                Surface(Modifier.align(Alignment.BottomStart).padding(20.dp), shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.95f)) {
                    Text("THE MEAL DB  /  ${meal.summary.id}", Modifier.padding(horizontal = 12.dp, vertical = 7.dp), style = MaterialTheme.typography.labelSmall, color = Color(0xFF285B42))
                }
            }
        }
        item(key = "title") {
            ConstraintLayout(Modifier.fillMaxWidth().padding(24.dp)) {
                val (category, title, original, area, count) = createRefs()
                Text(
                    meal.summary.categoryLabel,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.constrainAs(category) { top.linkTo(parent.top); start.linkTo(parent.start); end.linkTo(parent.end); width = Dimension.fillToConstraints }
                )
                Text(
                    meal.summary.name,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.constrainAs(title) { top.linkTo(category.bottom, 10.dp); start.linkTo(parent.start); end.linkTo(parent.end); width = Dimension.fillToConstraints }.testTag("detail_title")
                )
                Text(
                    meal.summary.originalName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.constrainAs(original) { top.linkTo(title.bottom, 10.dp); start.linkTo(parent.start); end.linkTo(parent.end); width = Dimension.fillToConstraints }
                )
                LabelPill(
                    meal.summary.areaLabel + " кухня",
                    Modifier.constrainAs(area) { top.linkTo(original.bottom, 18.dp); start.linkTo(parent.start); end.linkTo(count.start, 8.dp); width = Dimension.fillToConstraints }
                )
                LabelPill(
                    "${meal.ingredients.size} продуктов",
                    Modifier.constrainAs(count) { top.linkTo(area.top); end.linkTo(parent.end) }
                )
            }
        }
        item(key = "tabs") {
            Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp).background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(16.dp)).padding(5.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Ингредиенты", "Приготовление").forEachIndexed { index, label ->
                    Button(
                        onClick = { section = index },
                        modifier = Modifier.weight(1f).testTag("detail_section_$index"),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (section == index) MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (section == index) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        elevation = null
                    ) { Text(label, style = MaterialTheme.typography.labelLarge) }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
        if (section == 0) {
            item(key = "ingredients_header") {
                Column(Modifier.padding(horizontal = 24.dp)) {
                    Text("Всё, что понадобится", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(6.dp))
                    Text("Количество продуктов из оригинального рецепта", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                }
            }
            itemsIndexed(meal.ingredients, key = { index, _ -> "ingredient_$index" }) { _, ingredient ->
                Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                    Spacer(Modifier.width(14.dp))
                    Text(ingredient.name, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.width(16.dp))
                    Text(ingredient.measure.ifBlank { "по вкусу" }, Modifier.weight(0.75f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.End)
                }
                HorizontalDivider(Modifier.padding(horizontal = 24.dp), color = MaterialTheme.colorScheme.outlineVariant)
            }
        } else {
            item(key = "instructions_header") {
                Column(Modifier.padding(horizontal = 24.dp)) {
                    Text("Готовим шаг за шагом", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(6.dp))
                    Text("Шагов: ${meal.instructions.size} · перевод рецепта TheMealDB", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(22.dp))
                }
            }
            itemsIndexed(meal.instructions, key = { index, _ -> "step_$index" }) { index, instruction ->
                Row(Modifier.padding(start = 24.dp, end = 24.dp, bottom = 24.dp)) {
                    Box(Modifier.size(34.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                        Text("${index + 1}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.width(14.dp))
                    Text(instruction, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        item(key = "links") {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                meal.youtubeUrl?.let { url ->
                    OutlinedButton(onClick = { uriHandler.openUri(url) }, modifier = Modifier.fillMaxWidth()) {
                        Icon(painterResource(R.drawable.ic_external), null, Modifier.size(18.dp))
                        Text("  Видеорецепт")
                    }
                }
                Text("Рецепт и фотография предоставлены TheMealDB. Название и инструкция переведены для учебного приложения.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
