package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Employee
import com.example.data.model.SalaryPayment
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees ORDER BY isActive DESC, name ASC")
    fun getAllEmployees(): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveEmployees(): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE id = :id LIMIT 1")
    suspend fun getEmployeeById(id: Long): Employee?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployees(employees: List<Employee>)

    @Update
    suspend fun updateEmployee(employee: Employee)

    @Delete
    suspend fun deleteEmployee(employee: Employee)

    @Query("DELETE FROM employees WHERE id = :id")
    suspend fun deleteEmployeeById(id: Long)

    @Query("SELECT * FROM salary_payments ORDER BY paymentDateMillis DESC")
    fun getAllSalaryPayments(): Flow<List<SalaryPayment>>

    @Query("SELECT * FROM salary_payments WHERE employeeId = :employeeId ORDER BY paymentDateMillis DESC")
    fun getSalaryPaymentsForEmployee(employeeId: Long): Flow<List<SalaryPayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalaryPayment(payment: SalaryPayment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalaryPayments(payments: List<SalaryPayment>)

    @Delete
    suspend fun deleteSalaryPayment(payment: SalaryPayment)

    @Query("DELETE FROM salary_payments WHERE id = :id")
    suspend fun deleteSalaryPaymentById(id: Long)

    @Query("DELETE FROM employees")
    suspend fun deleteAllEmployees()

    @Query("DELETE FROM salary_payments")
    suspend fun deleteAllSalaryPayments()
}
