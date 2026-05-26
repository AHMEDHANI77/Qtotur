package com.example.data.dao

import androidx.room.*
import com.example.data.entity.Session
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY sessionDate DESC")
    fun getAllSessions(): Flow<List<Session>>

    @Query("SELECT * FROM sessions WHERE academyId = :academyId ORDER BY sessionDate DESC")
    fun getSessionsForAcademy(academyId: Long): Flow<List<Session>>

    @Query("SELECT COUNT(*) FROM sessions WHERE academyId = :academyId")
    suspend fun getSessionCountForAcademy(academyId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: Session): Long

    @Update
    suspend fun updateSession(session: Session): Int

    @Delete
    suspend fun deleteSession(session: Session): Int
}
