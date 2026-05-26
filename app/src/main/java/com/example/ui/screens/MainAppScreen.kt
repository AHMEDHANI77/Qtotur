package com.example.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.entity.Academy
import com.example.data.entity.Session
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.GoldWarm
import com.example.ui.theme.MediumEmerald
import com.example.ui.viewmodel.QuranTrackerViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: QuranTrackerViewModel) {
    val context = LocalContext.current

    // Force RTL support for Arabic interface
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        var currentTab by remember { mutableStateOf(0) }
        
        // Modal & Dialog Visibility states
        var showAddAcademyDialog by remember { mutableStateOf(false) }
        var showAddSessionDialog by remember { mutableStateOf(false) }
        var academyToEdit by remember { mutableStateOf<Academy?>(null) }
        
        // Delete Warning State
        var academyToDelete by remember { mutableStateOf<Academy?>(null) }
        var connectedSessionsCount by remember { mutableStateOf(0) }
        var showDeleteWarningDialog by remember { mutableStateOf(false) }

        val stats by viewModel.currentMonthSummary.collectAsStateWithLifecycle()
        val academies by viewModel.academies.collectAsStateWithLifecycle()
        val sessions by viewModel.sessions.collectAsStateWithLifecycle()
        val reportSummary by viewModel.reportState.collectAsStateWithLifecycle()

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "مُعلّم القرآن",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = DeepEmerald
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "الرئيسية") },
                        label = { Text("الرئيسية") },
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.List, contentDescription = "الأكاديميات") },
                        label = { Text("الأكاديميات") },
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Info, contentDescription = "التقارير") },
                        label = { Text("التقارير") },
                        selected = currentTab == 2,
                        onClick = { currentTab = 2 },
                        modifier = Modifier.testTag("reports_tab")
                    )
                }
            },
            floatingActionButton = {
                // FAB context action based on current tab
                when (currentTab) {
                    0, 2 -> {
                        if (academies.isNotEmpty()) {
                            FloatingActionButton(
                                onClick = { showAddSessionDialog = true },
                                containerColor = GoldWarm,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.testTag("add_session_fab")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "تسجيل حصة")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تسجيل حصة", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    1 -> {
                        FloatingActionButton(
                            onClick = { showAddAcademyDialog = true },
                            containerColor = DeepEmerald,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.testTag("add_academy_fab")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "إضافة أكاديمية")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إضافة أكاديمية", fontWeight = FontWeight.Bold)
                              }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (currentTab) {
                    0 -> HomeDashboardScreen(
                        stats = stats,
                        sessions = sessions,
                        academies = academies,
                        onDeleteSession = { viewModel.deleteSession(it) }
                    )
                    1 -> AcademiesManagementScreen(
                        academies = academies,
                        onEdit = { academyToEdit = it },
                        onDelete = { academy ->
                            viewModel.deleteAcademy(
                                academy = academy,
                                force = false,
                                onHasSessions = { count ->
                                    connectedSessionsCount = count
                                    academyToDelete = academy
                                    showDeleteWarningDialog = true
                                },
                                onSuccess = {
                                    Toast.makeText(context, "تم حذف الأكاديمية بنجاح", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    )
                    2 -> ReportsScreen(
                        viewModel = viewModel,
                        reportSummary = reportSummary
                    )
                }
                
                // Add Session Dialog
                if (showAddSessionDialog) {
                    AddSessionDialog(
                        academies = academies,
                        onDismiss = { showAddSessionDialog = false },
                        onConfirm = { academyId, student, duration, date, notes, rate ->
                            viewModel.addSession(academyId, student, duration, date, notes, rate)
                            showAddSessionDialog = false
                            Toast.makeText(context, "تم تسجيل الحصة بنجاح", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // Add Academy Dialog
                if (showAddAcademyDialog) {
                    AddEditAcademyDialog(
                        onDismiss = { showAddAcademyDialog = false },
                        onConfirm = { name, rate ->
                            viewModel.addAcademy(name, rate)
                            showAddAcademyDialog = false
                            Toast.makeText(context, "تمت إضافة الأكاديمية بنجاح", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // Edit Academy Dialog
                academyToEdit?.let { academy ->
                    AddEditAcademyDialog(
                        academy = academy,
                        onDismiss = { academyToEdit = null },
                        onConfirm = { name, rate ->
                            viewModel.updateAcademy(academy.copy(name = name, defaultHourlyRate = rate))
                            academyToEdit = null
                            Toast.makeText(context, "تم تعديل الأكاديمية بنجاح", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // Delete Confirmation with Connected Sessions warning
                if (showDeleteWarningDialog && academyToDelete != null) {
                    AlertDialog(
                        onDismissRequest = {
                            showDeleteWarningDialog = false
                            academyToDelete = null
                        },
                        icon = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(36.dp)) },
                        title = { Text("تنبيه الحذف!", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) },
                        text = {
                            Text(
                                "الأكاديمية \"${academyToDelete?.name}\" مرتبطة بـ ($connectedSessionsCount) حصة مسجلة.\n\nإن قيامك بحذف الأكاديمية سيؤدي لحذف كافة الحصص المرتبطة بها نهائياً وحذف أرباحها التاريخية. هل أنت متأكد من الحيازة على حذفها بالكامل؟",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    academyToDelete?.let {
                                        viewModel.deleteAcademy(
                                            academy = it,
                                            force = true,
                                            onHasSessions = {},
                                            onSuccess = {
                                                Toast.makeText(context, "تم حذف الأكاديمية وجميع حصصها بنجاح", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                    showDeleteWarningDialog = false
                                    academyToDelete = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("نعم، حذف الكل", color = Color.White)
                            }
                        },
                        dismissButton = {
                            OutlinedButton(
                                onClick = {
                                    showDeleteWarningDialog = false
                                    academyToDelete = null
                                }
                            ) {
                                Text("إلغاء")
                            }
                        }
                    )
                }
            }
        }
    }
}

// ----------------======================----------------
// 1. HOME DASHBOARD SCREEN
// ----------------======================----------------
@Composable
fun HomeDashboardScreen(
    stats: com.example.ui.viewmodel.QuickStats,
    sessions: List<Session>,
    academies: List<Academy>,
    onDeleteSession: (Session) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Stats Card (Aesthetic Dark Emerald Box with Gold Accents)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DeepEmerald),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ملخص أرباح الشهر الحالي",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${String.format(Locale.US, "%,.2f", stats.earnings)} ريال",
                        color = GoldWarm,
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "إجمالي الساعات",
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${stats.hours} ساعة",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "عدد الحصص",
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${stats.sessionCount} حصة",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Section Title
        item {
            Text(
                text = "آخر الحصص المسجلة",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (sessions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد حصص مسجلة حتى الآن",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                        if (academies.isEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "يرجى إضافة أكاديمية أولاً للبدء",
                                color = GoldWarm,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        } else {
            items(sessions.take(15)) { session ->
                val academyName = academies.find { it.id == session.academyId }?.name ?: "أكاديمية غير معروفة"
                SessionCardItem(session = session, academyName = academyName, onDelete = { onDeleteSession(session) })
            }
        }
    }
}

@Composable
fun SessionCardItem(session: Session, academyName: String, onDelete: () -> Unit) {
    val formatter = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }
    val dateStr = formatter.format(Date(session.sessionDate))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = academyName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = DeepEmerald
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row {
                    Text(
                        text = "طالب: ${session.studentName.ifEmpty { "غير محدد" }}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "مدة: ${session.durationHours} ساعة",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                if (session.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ملاحظات: ${session.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                val totalCost = session.durationHours * session.hourlyRate
                Text(
                    text = "${String.format(Locale.US, "%,.2f", totalCost)} ريال",
                    fontWeight = FontWeight.Bold,
                    color = MediumEmerald,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${session.hourlyRate} ر.س/س",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف الحصة", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                }
            }
        }
    }
}

// ----------------======================----------------
// 2. ACADEMIES MANAGEMENT SCREEN
// ----------------======================----------------
@Composable
fun AcademiesManagementScreen(
    academies: List<Academy>,
    onEdit: (Academy) -> Unit,
    onDelete: (Academy) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "قائمة الأكاديميات المسجلة",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (academies.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.List,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "الرجاء إضافة أول أكاديمية للعمل معها",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            items(academies) { academy ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = academy.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "السعر الافتراضي للساعة: ${academy.defaultHourlyRate} ريال",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GoldWarm,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Row {
                            IconButton(onClick = { onEdit(academy) }) {
                                Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = MediumEmerald)
                            }
                            IconButton(onClick = { onDelete(academy) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------======================----------------
// 3. REPORTS SCREEN (CALCULATIONS & REPORTS BREAKDOWN)
// ------------------------------------------------------
@Composable
fun ReportsScreen(
    viewModel: QuranTrackerViewModel,
    reportSummary: com.example.ui.viewmodel.ReportSummary
) {
    val filterMonth by viewModel.filterMonth.collectAsStateWithLifecycle()
    val filterYear by viewModel.filterYear.collectAsStateWithLifecycle()

    var showMonthDropdown by remember { mutableStateOf(false) }
    var showYearDropdown by remember { mutableStateOf(false) }

    val months = listOf(
        "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
        "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
    )

    val years = (2025..2030).toList()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dropdown selection headers for Month & Year
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "فلترة التقارير بالتاريخ",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = DeepEmerald
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Month Selector
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { showMonthDropdown = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(months[filterMonth], fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = showMonthDropdown,
                                onDismissRequest = { showMonthDropdown = false }
                            ) {
                                months.forEachIndexed { index, mName ->
                                    DropdownMenuItem(
                                        text = { Text(mName) },
                                        onClick = {
                                            viewModel.updateFilter(index, filterYear)
                                            showMonthDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Year Selector
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { showYearDropdown = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(filterYear.toString(), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = showYearDropdown,
                                onDismissRequest = { showYearDropdown = false }
                            ) {
                                years.forEach { yr ->
                                    DropdownMenuItem(
                                        text = { Text(yr.toString()) },
                                        onClick = {
                                            viewModel.updateFilter(filterMonth, yr)
                                            showYearDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selected Period Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "تقرير شهر: ${months[filterMonth]} $filterYear",
                        fontWeight = FontWeight.Bold,
                        color = DeepEmerald,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("إجمالي الأرباح المستحقة:", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "${String.format(Locale.US, "%,.2f", reportSummary.totalEarnings)} ريال",
                            fontWeight = FontWeight.Bold,
                            color = MediumEmerald,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("مجموع الساعات المُدرّسة:", style = MaterialTheme.typography.bodyMedium)
                        Text("${reportSummary.totalHours} ساعة", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("إجمالي عدد الحصص:", style = MaterialTheme.typography.bodyMedium)
                        Text("${reportSummary.totalSessions} حصة", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text(
                text = "تفاصيل الأكاديميات لهذا الشهر",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (reportSummary.academyReports.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "لا توجد بيانات مسجلة لهذا الشهر",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            items(reportSummary.academyReports) { report ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = report.academy.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = DeepEmerald
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row {
                                Text("الحصص: ${report.sessionCount}", style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("الساعات: ${report.totalHours}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${String.format(Locale.US, "%,.2f", report.totalEarnings)} ريال",
                                fontWeight = FontWeight.Bold,
                                color = MediumEmerald,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "المعدل: ${report.academy.defaultHourlyRate} ر.س/ساعة",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----------------======================----------------
// DIALOGS & SHAPES FOR INPUT FORMS
// ----------------======================----------------

@Composable
fun AddEditAcademyDialog(
    academy: Academy? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, defaultRate: Double) -> Unit
) {
    var name by remember { mutableStateOf(academy?.name ?: "") }
    var rateStr by remember { mutableStateOf(academy?.defaultHourlyRate?.toString() ?: "") }
    var errorMsg by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (academy == null) "إضافة أكاديمية جديدة" else "تعديل الأكاديمية",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DeepEmerald
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الأكاديمية") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = rateStr,
                    onValueChange = { rateStr = it },
                    label = { Text("سعر ساعة العمل الافتراضي (ريال)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (errorMsg.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMsg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            val rate = rateStr.toDoubleOrNull()
                            if (name.isBlank()) {
                                errorMsg = "يرجى إدخال اسم الأكاديمية"
                            } else if (rate == null || rate <= 0.0) {
                                errorMsg = "يرجى إدخال سعر ساعة صحيح"
                            } else {
                                onConfirm(name, rate)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepEmerald),
                        modifier = Modifier.weight(1f).testTag("submit_academy_button")
                    ) {
                        Text("حفظ", color = Color.White)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSessionDialog(
    academies: List<Academy>,
    onDismiss: () -> Unit,
    onConfirm: (academyId: Long, studentName: String, duration: Double, date: Long, notes: String, rate: Double) -> Unit
) {
    var selectedAcademyIndex by remember { mutableStateOf(0) }
    var showAcademyDropdown by remember { mutableStateOf(false) }

    var studentName by remember { mutableStateOf("") }
    var durationStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val activeAcademy = academies.getOrNull(selectedAcademyIndex)
    var customHourlyRateStr by remember { mutableStateOf(activeAcademy?.defaultHourlyRate?.toString() ?: "") }

    // When the selected academy changes, automatically populate its default rate
    LaunchedEffect(selectedAcademyIndex) {
        academies.getOrNull(selectedAcademyIndex)?.let {
            customHourlyRateStr = it.defaultHourlyRate.toString()
        }
    }

    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    val formatter = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }
    val context = LocalContext.current

    var errorMsg by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        text = "تسجيل حصة جديدة",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DeepEmerald
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Academy Selector
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("الأكاديمية المانحة", style = MaterialTheme.typography.bodySmall, color = DeepEmerald, fontWeight = FontWeight.Bold)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { showAcademyDropdown = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(activeAcademy?.name ?: "اختر الأكاديمية")
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = showAcademyDropdown,
                                onDismissRequest = { showAcademyDropdown = false }
                            ) {
                                academies.forEachIndexed { index, academy ->
                                    DropdownMenuItem(
                                        text = { Text(academy.name) },
                                        onClick = {
                                            selectedAcademyIndex = index
                                            showAcademyDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Student Name
                item {
                    OutlinedTextField(
                        value = studentName,
                        onValueChange = { studentName = it },
                        label = { Text("اسم الطالب (اختياري)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Duration hours (fractions allowed)
                item {
                    OutlinedTextField(
                        value = durationStr,
                        onValueChange = { durationStr = it },
                        label = { Text("مدة الحصة بالساعات (مثال: 0.5 أو 1 أو 1.5)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Session Special rate
                item {
                    OutlinedTextField(
                        value = customHourlyRateStr,
                        onValueChange = { customHourlyRateStr = it },
                        label = { Text("سعر ساعة العمل لهذه الحصة (ريال)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Pick Date Box
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                val calendar = Calendar.getInstance()
                                calendar.timeInMillis = selectedDateMillis
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val selectedCal = Calendar.getInstance()
                                        selectedCal.set(year, month, dayOfMonth)
                                        selectedDateMillis = selectedCal.timeInMillis
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("تاريخ الحصة", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatter.format(Date(selectedDateMillis)),
                            fontWeight = FontWeight.Bold,
                            color = DeepEmerald,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                // Extra notes
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("ملاحظات إضافية (اختياري)") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (errorMsg.isNotEmpty()) {
                    item {
                        Text(text = errorMsg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val duration = durationStr.toDoubleOrNull()
                                val rate = customHourlyRateStr.toDoubleOrNull()
                                if (activeAcademy == null) {
                                    errorMsg = "الرجاء اختيار أكاديمية"
                                } else if (duration == null || duration <= 0.0) {
                                    errorMsg = "الرجاء إدخال مدة صحيحة بالساعات"
                                } else if (rate == null || rate <= 0.0) {
                                    errorMsg = "الرجاء إدخال سعر ساعة صحيح لهذه الحصة"
                                } else {
                                    onConfirm(
                                        activeAcademy.id,
                                        studentName,
                                        duration,
                                        selectedDateMillis,
                                        notes,
                                        rate
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepEmerald),
                            modifier = Modifier.weight(1f).testTag("submit_session_button")
                        ) {
                            Text("حفظ الحصة", color = Color.White)
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء")
                        }
                    }
                }
            }
        }
    }
}
