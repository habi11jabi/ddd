package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val designation: String, // ম্যানেজার, সেলস এক্সিকিউটিভ, টেকনিশিয়ান, ইত্যাদি
    val phone: String,
    val monthlySalary: Double,
    val joiningDateMillis: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val address: String = "",
    val nidNumber: String = "",
    val notes: String = ""
)

@Entity(tableName = "salary_payments")
data class SalaryPayment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeId: Long,
    val employeeName: String,
    val monthYear: String, // e.g. "নভেম্বর ২০২৬" or "November 2026"
    val amountPaid: Double,
    val paymentDateMillis: Long = System.currentTimeMillis(),
    val paymentMethod: String = "Cash", // Cash, bKash, Nagad, Bank
    val paymentType: String = "FULL", // FULL, PARTIAL, ADVANCE
    val note: String = ""
)
