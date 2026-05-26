package com.example.data.dao

import androidx.room.*
import com.example.data.entity.Academy
import kotlinx.coroutines.flow.Flow

@Dao
interface AcademyDao {
    @Query("SELECT * FROM academies ORDER BY name ASC")
    fun getAllAcademies(): Flow<List<Academy>>

    @Query("SELECT * FROM academies WHERE id = :id LIMIT 1")
    suspend fun getAcademyById(id: Long): Academy?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAcademy(academy: Academy): Long

    @Update
    suspend fun updateAcademy(academy: Academy): Int

    @Delete
    suspend fun deleteAcademy(academy: Academy): Int
}
