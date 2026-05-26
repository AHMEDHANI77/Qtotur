package com.example.data.repository

import com.example.data.dao.AcademyDao
import com.example.data.dao.SessionDao
import com.example.data.entity.Academy
import com.example.data.entity.Session
import kotlinx.coroutines.flow.Flow

class QuranTrackerRepository(
    private val academyDao: AcademyDao,
    private val sessionDao: SessionDao
) {
    val allAcademies: Flow<List<Academy>> = academyDao.getAllAcademies()
    val allSessions: Flow<List<Session>> = sessionDao.getAllSessions()

    suspend fun getAcademyById(id: Long): Academy? {
        return academyDao.getAcademyById(id)
    }

    suspend fun insertAcademy(academy: Academy): Long {
        return academyDao.insertAcademy(academy)
    }

    suspend fun updateAcademy(academy: Academy): Int {
        return academyDao.updateAcademy(academy)
    }

    suspend fun deleteAcademy(academy: Academy): Int {
        return academyDao.deleteAcademy(academy)
    }

    suspend fun getSessionCountForAcademy(academyId: Long): Int {
        return sessionDao.getSessionCountForAcademy(academyId)
    }

    suspend fun insertSession(session: Session): Long {
        return sessionDao.insertSession(session)
    }

    suspend fun updateSession(session: Session): Int {
        return sessionDao.updateSession(session)
    }

    suspend fun deleteSession(session: Session): Int {
        return sessionDao.deleteSession(session)
    }

    fun getSessionsForAcademy(academyId: Long): Flow<List<Session>> {
        return sessionDao.getSessionsForAcademy(academyId)
    }
}
