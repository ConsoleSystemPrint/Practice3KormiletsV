package com.example.practice3kormiletsv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.practice3kormiletsv.ui.MealsApp
import com.example.practice3kormiletsv.ui.theme.Practice3KormiletsVTheme

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
