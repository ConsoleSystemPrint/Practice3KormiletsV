package com.example.practice3kormiletsv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.practice3kormiletsv.core.ui.theme.Practice3KormiletsVTheme
import com.example.practice3kormiletsv.ui.MealsApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Practice3KormiletsVTheme {
                MealsApp()
            }
        }
    }
}
