package com.epiahackers.crudarticles

import android.app.Application
import androidx.room.Room
import com.epiahackers.crudarticles.database.AppDatabase

class App: Application() {
    lateinit var db: AppDatabase

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "articles-db"
        ).build()
    }
}