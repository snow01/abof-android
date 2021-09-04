package com.example.abof

import android.app.Application

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        println("=====> IN MAIN APP <=====")
    }
}