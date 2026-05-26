package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AcademyDao
import com.example.data.dao.SessionDao
import com.example.data.entity.Academy
import com.example.data.entity.Session

@Database(entities = [Academy::class, Session::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun academyDao(): AcademyDao
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quran_teacher_tracker_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
