package com.example

import android.app.Application
import com.example.data.database.AppDatabase
import com.example.data.repository.QuranTrackerRepository

class QuranTrackerApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: QuranTrackerRepository by lazy {
        QuranTrackerRepository(database.academyDao(), database.sessionDao())
    }
}
