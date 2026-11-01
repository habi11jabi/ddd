package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Employee
import com.example.data.model.SalaryPayment
import com.example.ui.LocalAppStrings
import com.example.ui.theme.AppleBlue
import com.example.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeScreen(
    employees: List<Employee>,
    salaryPayments: List<SalaryPayment>,
    onAddEmployee: (name: String, designation: String, phone: String, salary: Double, address: String, notes: String) -> Unit,
    onUpdateEmployee: (Employee) -> Unit,
    onDeleteEmployee: (Employee) -> Unit,
    onPaySalary: (employee: Employee, monthYear: String, amountPaid: Double, paymentType: String, paymentMethod: String, note: String, recordAsExpense: Boolean) -> Unit,
    onDeleteSalaryPayment: (SalaryPayment) -> Unit
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Employees, 1: Salary History

    var showAddEmployeeDialog by remember { mutableStateOf(false) }
    var employeeToEdit by remember { mutableStateOf<Employee?>(null) }
    var employeeToPay by remember { mutableStateOf<Employee?>(null) }
    var employeeToDelete by remember { mutableStateOf<Employee?>(null) }
    var salaryToDelete by remember { mutableStateOf<SalaryPayment?>(null) }

    // Dialog: Add / Edit Employee
    if (showAddEmployeeDialog || employeeToEdit != null) {
        AddEditEmployeeDialog(
            existing = employeeToEdit,
            onDismiss = {
                showAddEmployeeDialog = false
                employeeToEdit = null
            },
            onSave = { name, desig, phone, salary, address, notes ->
                if (employeeToEdit != null) {
                    onUpdateEmployee(
                        employeeToEdit!!.copy(
                            name = name,
                            designation = desig,
                            phone = phone,
                            monthlySalary = salary,
                            address = address,
                            notes = notes
                        )
                    )
                } else {
                    onAddEmployee(name, desig, phone, salary, address, notes)
                }
                showAddEmployeeDialog = false
                employeeToEdit = null
            }
        )
    }

    // Dialog: Pay Salary
    employeeToPay?.let { emp ->
        PaySalaryDialog(
            employee = emp,
            onDismiss = { employeeToPay = null },
            onConfirm = { monthYear, amount, paymentType, paymentMethod, note, recordExpense ->
                onPaySalary(emp, monthYear, amount, paymentType, paymentMethod, note, recordExpense)
                employeeToPay = null
            }
        )
    }

    // Delete Employee Dialog
    employeeToDelete?.let { emp ->
        AlertDialog(
            onDismissRequest = { employeeToDelete = null },
            title = { Text("কর্মচারী মুছে ফেলবেন?") },
            text = { Text("${emp.name} কে কর্মচারীর তালিকা থেকে মুছে ফেলা হবে। আপনি কি নিশ্চিত?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteEmployee(emp)
                        employeeToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(strings.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { employeeToDelete = null }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    // Delete Salary Payment Dialog
    salaryToDelete?.let { sal ->
        AlertDialog(
            onDismissRequest = { salaryToDelete = null },
            title = { Text("বেতন পরিশোধ রেকর্ড মুছবেন?") },
            text = { Text("${sal.employeeName} এর ${sal.monthYear} মাসের ৳ ${InventoryViewModel.formatBdt(sal.amountPaid)} এর পেমেন্ট রেকর্ডটি মুছে ফেলতে চান?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSalaryPayment(sal)
                        salaryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(strings.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { salaryToDelete = null }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("employee_screen")
    ) {
        // 1. Screen Header
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = AppleBlue.copy(alpha = 0.12f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = AppleBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = strings.employeesTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = strings.employeesSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. Sub Tabs (Staff List vs Salary History)
        SecondaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(
                            "${strings.tabEmployeeList} (${employees.size})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                },
                modifier = Modifier.testTag("tab_staff_list")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(
                            "${strings.tabSalaryHistory} (${salaryPayments.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                },
                modifier = Modifier.testTag("tab_salary_history")
            )
        }

        // Tab Content
        if (selectedTab == 0) {
            // TAB 0: Employee Directory
            val activeEmployees = employees.filter { it.isActive }
            val totalMonthlyPayroll = activeEmployees.sumOf { it.monthlySalary }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Summary Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
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
                                    text = "কর্মরত স্টাফ সংখ্যা",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${activeEmployees.size} জন",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AppleBlue
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "মাসিক মোট বেতন বাজেট",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "৳ ${InventoryViewModel.formatBdt(totalMonthlyPayroll)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFE11D48)
                                )
                            }
                        }
                    }
                }

                // Add Employee Action Button
                item {
                    Button(
                        onClick = { showAddEmployeeDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("add_employee_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.btnAddEmployee, fontWeight = FontWeight.Bold)
                    }
                }

                // Employee Cards List
                if (employees.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = strings.noEmployeesFound,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(employees, key = { it.id }) { emp ->
                        EmployeeItemCard(
                            employee = emp,
                            onPaySalary = { employeeToPay = emp },
                            onEdit = { employeeToEdit = emp },
                            onDelete = { employeeToDelete = emp },
                            onCall = {
                                if (emp.phone.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${emp.phone}"))
                                    context.startActivity(intent)
                                }
                            }
                        )
                    }
                }
            }
        } else {
            // TAB 1: Salary Payment History
            val totalDisbursed = salaryPayments.sumOf { it.amountPaid }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Stat
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F766E).copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Paid,
                                    contentDescription = null,
                                    tint = Color(0xFF0F766E),
                                    modifier = Modifier.size(28.dp)
                                )
                                Column {
                                    Text(
                                        text = "মোট পরিশোধিত বেতন",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "৳ ${InventoryViewModel.formatBdt(totalDisbursed)}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0F766E)
                                    )
                                }
                            }
                            Text(
                                text = "${salaryPayments.size}টি ট্রানজ্যাকশন",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                // Salary Payment Records
                if (salaryPayments.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = strings.noSalaryRecordsFound,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(salaryPayments, key = { it.id }) { record ->
                        SalaryRecordCard(
                            record = record,
                            onDelete = { salaryToDelete = record }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmployeeItemCard(
    employee: Employee,
    onPaySalary: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCall: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("employee_card_${employee.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = AppleBlue.copy(alpha = 0.15f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = employee.name.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = AppleBlue,
                                fontSize = 20.sp
                            )
                        }
                    }
                    Column {
                        Text(
                            text = employee.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            ) {
                                Text(
                                    text = employee.designation,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Details Row: Monthly Salary and Phone
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "মাসিক নির্ধারিত বেতন:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "৳ ${InventoryViewModel.formatBdt(employee.monthlySalary)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F766E)
                    )
                }

                if (employee.phone.isNotBlank()) {
                    OutlinedButton(
                        onClick = onCall,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(employee.phone, fontSize = 12.sp)
                    }
                }
            }

            if (employee.address.isNotBlank() || employee.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = listOfNotNull(
                        if (employee.address.isNotBlank()) "ঠিকানা: ${employee.address}" else null,
                        if (employee.notes.isNotBlank()) "নোট: ${employee.notes}" else null
                    ).joinToString(" | "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action: Disburse Salary Button
            Button(
                onClick = onPaySalary,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("pay_salary_btn_${employee.id}")
            ) {
                Icon(Icons.Default.Paid, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("বেতন প্রদান করুন (Pay Salary)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun SalaryRecordCard(
    record: SalaryPayment,
    onDelete: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale("bn", "BD")) }
    val formattedDate = remember(record.paymentDateMillis) { sdf.format(Date(record.paymentDateMillis)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("salary_record_${record.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = record.employeeName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = record.monthYear,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF047857),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = record.paymentMethod,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "৳ ${InventoryViewModel.formatBdt(record.amountPaid)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F766E)
                    )
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (record.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = record.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "পরিশোধের তারিখ: $formattedDate",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                fontSize = 10.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEmployeeDialog(
    existing: Employee?,
    onDismiss: () -> Unit,
    onSave: (name: String, designation: String, phone: String, salary: Double, address: String, notes: String) -> Unit
) {
    val strings = LocalAppStrings.current
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var designation by remember { mutableStateOf(existing?.designation ?: "সেলস এক্সিকিউটিভ") }
    var phone by remember { mutableStateOf(existing?.phone ?: "") }
    var salaryText by remember { mutableStateOf(existing?.monthlySalary?.toLong()?.toString() ?: "") }
    var address by remember { mutableStateOf(existing?.address ?: "") }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }

    val presetRoles = listOf("ম্যানেজার", "সেলস এক্সিকিউটিভ", "টেকনিশিয়ান", "সহকারী")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existing != null) "কর্মচারী তথ্য পরিবর্তন" else "নতুন কর্মচারী যোগ করুন",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("কর্মচারীর পুরো নাম *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("employee_name_input")
                )

                OutlinedTextField(
                    value = designation,
                    onValueChange = { designation = it },
                    label = { Text("পদবী (Designation) *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("employee_designation_input")
                )

                // Quick role chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(presetRoles) { role ->
                        FilterChip(
                            selected = designation == role,
                            onClick = { designation = role },
                            label = { Text(role, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("মোবাইল নম্বর *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("employee_phone_input")
                )

                OutlinedTextField(
                    value = salaryText,
                    onValueChange = { salaryText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("নির্ধারিত মাসিক বেতন (৳) *") },
                    placeholder = { Text("যেমন: ২০০০০") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("employee_salary_input")
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("ঠিকানা (ঐচ্ছিক)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("মন্তব্য / নোট (ঐচ্ছিক)") },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val salary = salaryText.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && salary > 0) {
                        onSave(name, designation, phone, salary, address, notes)
                    }
                },
                enabled = name.isNotBlank() && (salaryText.toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                modifier = Modifier.testTag("save_employee_button")
            ) {
                Text(strings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaySalaryDialog(
    employee: Employee,
    onDismiss: () -> Unit,
    onConfirm: (monthYear: String, amount: Double, paymentType: String, paymentMethod: String, note: String, recordAsExpense: Boolean) -> Unit
) {
    val strings = LocalAppStrings.current
    val currentMonthBangla = remember {
        val cal = Calendar.getInstance()
        val months = listOf(
            "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
            "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
        )
        val m = months[cal.get(Calendar.MONTH)]
        val y = cal.get(Calendar.YEAR)
        "$m $y"
    }

    var monthYear by remember { mutableStateOf(currentMonthBangla) }
    var amountText by remember { mutableStateOf(employee.monthlySalary.toLong().toString()) }
    var paymentType by remember { mutableStateOf("পূর্ণ বেতন (Full)") }
    var paymentMethod by remember { mutableStateOf("Cash") }
    var note by remember { mutableStateOf("") }
    var recordAsExpense by remember { mutableStateOf(true) }

    val paymentTypes = listOf("পূর্ণ বেতন (Full)", "আংশিক বেতন (Partial)", "অগ্রিম (Advance)")
    val paymentMethods = listOf("Cash", "বিকাশ (bKash)", "ব্যাংক ট্রান্সফার (Bank)")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "${employee.name} এর বেতন পরিশোধ",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = monthYear,
                    onValueChange = { monthYear = it },
                    label = { Text("কোন মাসের বেতন *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("salary_month_input")
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("পরিশোধকৃত টাকার পরিমাণ (৳) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("salary_amount_input")
                )

                // Payment Type Chips
                Column {
                    Text(
                        text = "বেতনের ধরন:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(paymentTypes) { type ->
                            FilterChip(
                                selected = paymentType == type,
                                onClick = { paymentType = type },
                                label = { Text(type, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Payment Method Chips
                Column {
                    Text(
                        text = "পরিশোধের মাধ্যম:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(paymentMethods) { method ->
                            FilterChip(
                                selected = paymentMethod == method,
                                onClick = { paymentMethod = method },
                                label = { Text(method, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("নোট বা মন্তব্য (ঐচ্ছিক)") },
                    placeholder = { Text("যেমন: বোনাস সহ / আংশিক পরিশোধ") },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                // Checkbox to record in shop cashbook
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = recordAsExpense,
                        onCheckedChange = { recordAsExpense = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "দোকানের দৈনিক খরচের খাতায়ও যোগ করুন",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0 && monthYear.isNotBlank()) {
                        onConfirm(monthYear, amount, paymentType, paymentMethod, note, recordAsExpense)
                    }
                },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0 && monthYear.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                modifier = Modifier.testTag("confirm_pay_salary_button")
            ) {
                Text("পরিশোধ নিশ্চিত করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}
