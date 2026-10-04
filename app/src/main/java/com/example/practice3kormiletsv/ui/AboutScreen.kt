package com.example.practice3kormiletsv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.practice3kormiletsv.R

@Composable
fun AboutScreen() {
    val uriHandler = LocalUriHandler.current
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.testTag("about_screen")) {
        item {
            BrandHeader()
            Spacer(Modifier.height(30.dp))
            Text("Вкусные открытия", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text("Весь мир\nна вашей кухне", style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(14.dp))
            Text("Небольшая коллекция рецептов, которая помогает выбрать блюдо и приготовить его шаг за шагом.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(painterResource(R.drawable.ic_bowl), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    Text("12 рецептов. Без интернета.", style = MaterialTheme.typography.titleLarge)
                    Text("Это учебная версия для практической работы № 3. Рецепты и фотографии сохранены в приложении. Поиск и фильтры работают с этой подборкой.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Источник вдохновения", style = MaterialTheme.typography.headlineMedium)
                Text("TheMealDB — открытая база рецептов со всего мира. Названия, ингредиенты и инструкции в подборке переведены на русский язык.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(onClick = { uriHandler.openUri("https://www.themealdb.com") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Открыть TheMealDB  ")
                    Icon(painterResource(R.drawable.ic_external), null, Modifier.size(18.dp))
                }
                Text("Избранное сохраняется при переключении экранов и повороте устройства. После нового запуска коллекцию можно собрать заново.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Практическая работа № 3 · TheMealDB", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
