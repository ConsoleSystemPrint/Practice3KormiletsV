package com.example.practice3kormiletsv

import android.app.Application
import com.example.practice3kormiletsv.di.AppContainer

class MealsApplication : Application() {
    val container by lazy { AppContainer(applicationContext) }
}
