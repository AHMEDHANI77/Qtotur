package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.entity.Academy
import com.example.data.entity.Session
import com.example.data.repository.QuranTrackerRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class QuranTrackerViewModel(private val repository: QuranTrackerRepository) : ViewModel() {

    // List of academies
    val academies: StateFlow<List<Academy>> = repository.allAcademies
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // List of all sessions
    val sessions: StateFlow<List<Session>> = repository.allSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current filter month (0-11, where 0 is January)
    private val _filterMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    val filterMonth: StateFlow<Int> = _filterMonth.asStateFlow()

    // Current filter year
    private val _filterYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val filterYear: StateFlow<Int> = _filterYear.asStateFlow()

    fun updateFilter(month: Int, year: Int) {
        _filterMonth.value = month
        _filterYear.value = year
    }

    // Helper to check if a session falls within the filtered month + year
    private fun isSessionInFilter(sessionDate: Long, month: Int, year: Int): Boolean {
        val cal = Calendar.getInstance()
        cal.timeInMillis = sessionDate
        return cal.get(Calendar.MONTH) == month && cal.get(Calendar.YEAR) == year
    }

    // Reports and Calculations state
    // Combines sessions, academies, filterMonth, and filterYear to output filtered calculations
    val reportState: StateFlow<ReportSummary> = combine(
        sessions,
        academies,
        filterMonth,
        filterYear
    ) { sessionsList, academiesList, m, y ->
        val filteredSessions = sessionsList.filter { isSessionInFilter(it.sessionDate, m, y) }
        
        val academyReportDetails = academiesList.map { academy ->
            val academySessions = filteredSessions.filter { it.academyId == academy.id }
            val totalHours = academySessions.sumOf { it.durationHours }
            val totalEarnings = academySessions.sumOf { it.durationHours * it.hourlyRate }
            AcademyReport(
                academy = academy,
                sessionCount = academySessions.size,
                totalHours = totalHours,
                totalEarnings = totalEarnings
            )
        }

        val totalEarningsAll = filteredSessions.sumOf { it.durationHours * it.hourlyRate }
        val totalHoursAll = filteredSessions.sumOf { it.durationHours }
        
        ReportSummary(
            totalEarnings = totalEarningsAll,
            totalHours = totalHoursAll,
            totalSessions = filteredSessions.size,
            academyReports = academyReportDetails
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportSummary()
    )

    // Current Month quick stats (independent of custom reports filtering, always displays the actual current month)
    val currentMonthSummary: StateFlow<QuickStats> = sessions.map { sessionsList ->
        val now = Calendar.getInstance()
        val currentM = now.get(Calendar.MONTH)
        val currentY = now.get(Calendar.YEAR)
        
        val currentMonthSessions = sessionsList.filter { 
            val cal = Calendar.getInstance()
            cal.timeInMillis = it.sessionDate
            cal.get(Calendar.MONTH) == currentM && cal.get(Calendar.YEAR) == currentY
        }
        
        val totalEarnings = currentMonthSessions.sumOf { it.durationHours * it.hourlyRate }
        val totalHours = currentMonthSessions.sumOf { it.durationHours }
        
        QuickStats(
            earnings = totalEarnings,
            hours = totalHours,
            sessionCount = currentMonthSessions.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = QuickStats()
    )

    // Operations
    fun addAcademy(name: String, defaultRate: Double) {
        viewModelScope.launch {
            repository.insertAcademy(Academy(name = name, defaultHourlyRate = defaultRate))
        }
    }

    fun updateAcademy(academy: Academy) {
        viewModelScope.launch {
            repository.updateAcademy(academy)
        }
    }

    fun deleteAcademy(
        academy: Academy,
        force: Boolean = false,
        onHasSessions: (sessionCount: Int) -> Unit,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val sessionsCount = repository.getSessionCountForAcademy(academy.id)
            if (sessionsCount > 0 && !force) {
                // Return callback indicating connected sessions exist
                onHasSessions(sessionsCount)
            } else {
                repository.deleteAcademy(academy)
                onSuccess()
            }
        }
    }

    fun addSession(
        academyId: Long,
        studentName: String,
        durationHours: Double,
        dateMillis: Long,
        notes: String,
        rate: Double
    ) {
        viewModelScope.launch {
            repository.insertSession(
                Session(
                    academyId = academyId,
                    studentName = studentName,
                    sessionDate = dateMillis,
                    durationHours = durationHours,
                    hourlyRate = rate,
                    notes = notes
                )
            )
        }
    }

    fun deleteSession(session: Session) {
        viewModelScope.launch {
            repository.deleteSession(session)
        }
    }
}

// Data models for states
data class QuickStats(
    val earnings: Double = 0.0,
    val hours: Double = 0.0,
    val sessionCount: Int = 0
)

data class AcademyReport(
    val academy: Academy,
    val sessionCount: Int,
    val totalHours: Double,
    val totalEarnings: Double
)

data class ReportSummary(
    val totalEarnings: Double = 0.0,
    val totalHours: Double = 0.0,
    val totalSessions: Int = 0,
    val academyReports: List<AcademyReport> = emptyList()
)

// Factory pattern for simple, robust constructor injection
class QuranTrackerViewModelFactory(private val repository: QuranTrackerRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuranTrackerViewModel::class.java)) {
            return QuranTrackerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
