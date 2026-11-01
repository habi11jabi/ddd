package com.example.data.repository

import android.content.Context
import com.example.data.db.EmployeeDao
import com.example.data.db.ExpenseDao
import com.example.data.db.ProductDao
import com.example.data.db.SaleDao
import com.example.data.model.Employee
import com.example.data.model.Expense
import com.example.data.model.Product
import com.example.data.model.ProductStatus
import com.example.data.model.SalaryPayment
import com.example.data.model.Sale
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

class InventoryRepository(
    private val context: Context,
    private val productDao: ProductDao,
    private val saleDao: SaleDao,
    private val expenseDao: ExpenseDao,
    private val employeeDao: EmployeeDao
) {
    private val prefs = context.getSharedPreferences("apple_area_bd_prefs", Context.MODE_PRIVATE)

    // Flow streams
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val inStockProducts: Flow<List<Product>> = productDao.getInStockProducts()
    val allSales: Flow<List<Sale>> = saleDao.getAllSales()
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()
    val allEmployees: Flow<List<Employee>> = employeeDao.getAllEmployees()
    val activeEmployees: Flow<List<Employee>> = employeeDao.getActiveEmployees()
    val allSalaryPayments: Flow<List<SalaryPayment>> = employeeDao.getAllSalaryPayments()

    // Product methods
    suspend fun addProduct(product: Product): Long = productDao.insertProduct(product)
    suspend fun addProducts(products: List<Product>) = productDao.insertAll(products)
    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)
    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)
    suspend fun getProductById(id: Long): Product? = productDao.getProductById(id)
    suspend fun getProductByImei(imei: String): Product? = productDao.getProductByImei(imei)

    /**
     * Completes a sale transaction: marks product as SOLD and inserts the Sale record.
     */
    suspend fun recordSale(
        product: Product,
        customerName: String,
        customerPhone: String,
        customerAddress: String,
        salePrice: Double,
        paidAmount: Double,
        paymentMethod: String,
        warrantyInfo: String,
        remarks: String = ""
    ): Long {
        val invoiceNumber = "AABD-" + (System.currentTimeMillis() % 1000000).toString()
        val dueAmount = (salePrice - paidAmount).coerceAtLeast(0.0)
        
        // Update product status to SOLD
        productDao.updateProductStatus(product.id, ProductStatus.SOLD.name)

        val sale = Sale(
            productId = product.id,
            invoiceNumber = invoiceNumber,
            productDetails = "${product.company} ${product.model} (${product.variant}, ${product.color})",
            imei = product.imei,
            batteryHealth = product.batteryHealth,
            boxCondition = product.boxCondition,
            customerName = customerName,
            customerPhone = customerPhone,
            customerAddress = customerAddress,
            costPrice = product.purchasePrice,
            salePrice = salePrice,
            paidAmount = paidAmount,
            dueAmount = dueAmount,
            paymentMethod = paymentMethod,
            warrantyInfo = warrantyInfo,
            soldAt = System.currentTimeMillis(),
            remarks = remarks
        )
        return saleDao.insertSale(sale)
    }

    suspend fun updateDuePayment(saleId: Long, newPaidAmount: Double, newDueAmount: Double) {
        saleDao.updateDuePayment(saleId, newPaidAmount, newDueAmount)
    }

    suspend fun updateSale(sale: Sale) {
        saleDao.updateSale(sale)
    }

    suspend fun deleteSale(sale: Sale, restoreStock: Boolean = true) {
        saleDao.deleteSale(sale)
        if (restoreStock) {
            productDao.updateProductStatus(sale.productId, ProductStatus.IN_STOCK.name)
        }
    }

    // Expense & Income methods
    suspend fun addExpense(expense: Expense): Long = expenseDao.insertExpense(expense)
    suspend fun updateExpense(expense: Expense) = expenseDao.updateExpense(expense)
    suspend fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)
    suspend fun deleteExpenseById(id: Long) = expenseDao.deleteExpenseById(id)

    // Employee & Salary methods
    suspend fun addEmployee(employee: Employee): Long = employeeDao.insertEmployee(employee)
    suspend fun updateEmployee(employee: Employee) = employeeDao.updateEmployee(employee)
    suspend fun deleteEmployee(employee: Employee) = employeeDao.deleteEmployee(employee)
    suspend fun deleteEmployeeById(id: Long) = employeeDao.deleteEmployeeById(id)

    suspend fun recordSalaryPayment(
        payment: SalaryPayment,
        recordAsShopExpense: Boolean = true
    ): Long {
        val paymentId = employeeDao.insertSalaryPayment(payment)
        if (recordAsShopExpense) {
            val expense = Expense(
                type = TransactionType.EXPENSE.name,
                category = "কর্মচারীর বেতন",
                amount = payment.amountPaid,
                note = "বেতন: ${payment.employeeName} (${payment.monthYear}) - ${payment.paymentType}",
                dateMillis = payment.paymentDateMillis,
                paymentMethod = payment.paymentMethod,
                referenceId = "SALARY_$paymentId"
            )
            expenseDao.insertExpense(expense)
        }
        return paymentId
    }

    suspend fun deleteSalaryPayment(payment: SalaryPayment) {
        employeeDao.deleteSalaryPayment(payment)
        // Also remove matching expense if auto-recorded
        expenseDao.deleteExpenseByReferenceId("SALARY_${payment.id}")
    }

    /**
     * Seeds initial realistic mock inventory, expenses, and staff for APPLE AREA BD
     */
    suspend fun seedInitialDataIfEmpty(force: Boolean = false) {
        val hasSeeded = prefs.getBoolean("has_seeded_initial_data_v2", false)
        if (!hasSeeded || force) {
            val currentCount = productDao.getProductCount()
            if (currentCount == 0 || force) {
                val sampleProducts = listOf(
                    Product(
                        company = "Apple",
                        model = "iPhone 15 Pro Max",
                        variant = "256GB",
                        color = "Natural Titanium",
                        batteryHealth = "100%",
                        boxCondition = "সিল প্যাক (Sealed Box)",
                        supplier = "Dubai Stock",
                        purchasePrice = 138000.0,
                        targetSalePrice = 148000.0,
                        imei = "354891098234125",
                        notes = "USA LL/A, 1 Year International Warranty",
                        status = ProductStatus.IN_STOCK.name
                    ),
                    Product(
                        company = "Apple",
                        model = "iPhone 15",
                        variant = "128GB",
                        color = "Blue",
                        batteryHealth = "98%",
                        boxCondition = "আইএমইআই ম্যাচিং বক্স (Matching Box)",
                        supplier = "Singapore ZP/A",
                        purchasePrice = 78000.0,
                        targetSalePrice = 85000.0,
                        imei = "359871023456789",
                        notes = "Very fresh, 10 days replacement",
                        status = ProductStatus.IN_STOCK.name
                    ),
                    Product(
                        company = "Apple",
                        model = "iPhone 14 Pro",
                        variant = "128GB",
                        color = "Deep Purple",
                        batteryHealth = "89%",
                        boxCondition = "বক্স সহ (With Original Box)",
                        supplier = "Local Exchange",
                        purchasePrice = 88000.0,
                        targetSalePrice = 96000.0,
                        imei = "351234567890123",
                        notes = "No scratches, full body lamination",
                        status = ProductStatus.IN_STOCK.name
                    ),
                    Product(
                        company = "Apple",
                        model = "iPhone 13",
                        variant = "128GB",
                        color = "Midnight Black",
                        batteryHealth = "86%",
                        boxCondition = "বক্স ছাড়া (Device Only)",
                        supplier = "Dhaka Wholesaler",
                        purchasePrice = 54000.0,
                        targetSalePrice = 61000.0,
                        imei = "356789012345678",
                        notes = "Only original charger included",
                        status = ProductStatus.IN_STOCK.name
                    ),
                    Product(
                        company = "Samsung",
                        model = "Galaxy S24 Ultra",
                        variant = "512GB",
                        color = "Titanium Black",
                        batteryHealth = "100%",
                        boxCondition = "সিল প্যাক (Sealed Box)",
                        supplier = "Dubai Stock",
                        purchasePrice = 125000.0,
                        targetSalePrice = 135000.0,
                        imei = "359012345678901",
                        notes = "Snapdragon Edition, Official BTRC Approved",
                        status = ProductStatus.IN_STOCK.name
                    ),
                    Product(
                        company = "Google",
                        model = "Pixel 8 Pro",
                        variant = "128GB",
                        color = "Obsidian",
                        batteryHealth = "95%",
                        boxCondition = "বক্স সহ (With Original Box)",
                        supplier = "USA LL/A",
                        purchasePrice = 72000.0,
                        targetSalePrice = 79000.0,
                        imei = "353456789012345",
                        notes = "Mint condition, eSIM active",
                        status = ProductStatus.IN_STOCK.name
                    )
                )
                productDao.insertAll(sampleProducts)

                // Seed sample sale
                val soldItem = Product(
                    company = "Apple",
                    model = "iPhone 14 Pro Max",
                    variant = "256GB",
                    color = "Space Black",
                    batteryHealth = "91%",
                    boxCondition = "বক্স সহ (With Box)",
                    supplier = "Dubai Stock",
                    purchasePrice = 105000.0,
                    targetSalePrice = 117000.0,
                    imei = "357890123456780",
                    notes = "Sold with silicone case",
                    status = ProductStatus.SOLD.name
                )
                val soldItemId = productDao.insertProduct(soldItem)
                val sampleSale = Sale(
                    productId = soldItemId,
                    invoiceNumber = "AABD-100234",
                    productDetails = "Apple iPhone 14 Pro Max (256GB, Space Black)",
                    imei = "357890123456780",
                    batteryHealth = "91%",
                    boxCondition = "বক্স সহ (With Box)",
                    customerName = "তানভীর আহমেদ (Tanvir Ahmed)",
                    customerPhone = "01711223344",
                    customerAddress = "ধানমন্ডি, ঢাকা",
                    costPrice = 105000.0,
                    salePrice = 116000.0,
                    paidAmount = 110000.0,
                    dueAmount = 6000.0,
                    paymentMethod = "ক্যাশ + বিকাশ",
                    warrantyInfo = "১০ দিনের রিপ্লেসমেন্ট এবং ১ বছর ফ্রি সার্ভিস ওয়ারেন্টি",
                    soldAt = System.currentTimeMillis() - 86400000L * 2,
                    remarks = "বাকি ৬,০০০ টাকা আগামী মাসের ১ তারিখে দেবে।"
                )
                saleDao.insertSale(sampleSale)

                // Seed sample employees
                val emp1 = Employee(
                    name = "মোঃ আরিফুল ইসলাম (Arif)",
                    designation = "ম্যানেজার (Store Manager)",
                    phone = "01758596640",
                    monthlySalary = 28000.0,
                    joiningDateMillis = System.currentTimeMillis() - 86400000L * 180,
                    isActive = true,
                    address = "পান্থপথ, ঢাকা",
                    notes = "ক্যাশ ও ইনভেন্টরি তদারকি"
                )
                val emp2 = Employee(
                    name = "তানজিল আহমেদ (Tanjil)",
                    designation = "সিনিয়র সেলস এক্সিকিউটিভ",
                    phone = "01930833375",
                    monthlySalary = 20000.0,
                    joiningDateMillis = System.currentTimeMillis() - 86400000L * 90,
                    isActive = true,
                    address = "ফার্মগেট, ঢাকা",
                    notes = "কাস্টমার ডিলিং এবং ডিভাইস চেক"
                )
                val emp3 = Employee(
                    name = "সাকিবুল হাসান (Sakib)",
                    designation = "হার্ডওয়্যার ও টেকনিশিয়ান",
                    phone = "01812345678",
                    monthlySalary = 22000.0,
                    joiningDateMillis = System.currentTimeMillis() - 86400000L * 60,
                    isActive = true,
                    address = "মিরপুর, ঢাকা",
                    notes = "স্ক্রিন, ব্যাটারি ও সার্ভিসিং"
                )
                val emp1Id = employeeDao.insertEmployee(emp1)
                val emp2Id = employeeDao.insertEmployee(emp2)
                val emp3Id = employeeDao.insertEmployee(emp3)

                // Seed sample salary payment
                val salary1 = SalaryPayment(
                    employeeId = emp1Id,
                    employeeName = emp1.name,
                    monthYear = "অক্টোবর ২০২৬",
                    amountPaid = 28000.0,
                    paymentDateMillis = System.currentTimeMillis() - 86400000L * 3,
                    paymentMethod = "ব্যাংক ট্রান্সফার",
                    paymentType = "FULL",
                    note = "অক্টোবর মাসের পূর্ণ বেতন পরিশোধিত"
                )
                val salary2 = SalaryPayment(
                    employeeId = emp2Id,
                    employeeName = emp2.name,
                    monthYear = "অক্টোবর ২০২৬",
                    amountPaid = 15000.0,
                    paymentDateMillis = System.currentTimeMillis() - 86400000L * 2,
                    paymentMethod = "ক্যাশ",
                    paymentType = "PARTIAL",
                    note = "অক্টোবর মাসের আংশিক বেতন (বাকি ৫,০০০)"
                )
                val sal1Id = employeeDao.insertSalaryPayment(salary1)
                val sal2Id = employeeDao.insertSalaryPayment(salary2)

                // Seed sample expenses
                val sampleExpenses = listOf(
                    Expense(
                        type = TransactionType.EXPENSE.name,
                        category = "দোকান ভাড়া",
                        amount = 35000.0,
                        note = "বসুন্ধরা সিটি শপ নং ১০০ এর চলতি মাসের ভাড়া",
                        dateMillis = System.currentTimeMillis() - 86400000L * 5,
                        paymentMethod = "ব্যাংক চেক"
                    ),
                    Expense(
                        type = TransactionType.EXPENSE.name,
                        category = "বিদ্যুৎ ও ইউটিলিটি",
                        amount = 4500.0,
                        note = "দোকানের বিদ্যুৎ বিল পরিশোধ",
                        dateMillis = System.currentTimeMillis() - 86400000L * 4,
                        paymentMethod = "বিকাশ"
                    ),
                    Expense(
                        type = TransactionType.EXPENSE.name,
                        category = "নাস্তা ও আপ্যায়ন",
                        amount = 650.0,
                        note = "কাস্টমার এবং স্টাফদের দুপুরের নাস্তা ও চা",
                        dateMillis = System.currentTimeMillis() - 86400000L * 1,
                        paymentMethod = "Cash"
                    ),
                    Expense(
                        type = TransactionType.EXPENSE.name,
                        category = "ওয়াইফাই ও ইন্টারনেট",
                        amount = 1200.0,
                        note = "দোকানের হাইস্পিড ব্রডব্যান্ড বিল",
                        dateMillis = System.currentTimeMillis() - 86400000L * 6,
                        paymentMethod = "বিকাশ"
                    ),
                    Expense(
                        type = TransactionType.EXPENSE.name,
                        category = "কর্মচারীর বেতন",
                        amount = 28000.0,
                        note = "বেতন: ${emp1.name} (অক্টোবর ২০২৬) - FULL",
                        dateMillis = System.currentTimeMillis() - 86400000L * 3,
                        paymentMethod = "ব্যাংক ট্রান্সফার",
                        referenceId = "SALARY_$sal1Id"
                    ),
                    Expense(
                        type = TransactionType.EXPENSE.name,
                        category = "কর্মচারীর বেতন",
                        amount = 15000.0,
                        note = "বেতন: ${emp2.name} (অক্টোবর ২০২৬) - PARTIAL",
                        dateMillis = System.currentTimeMillis() - 86400000L * 2,
                        paymentMethod = "ক্যাশ",
                        referenceId = "SALARY_$sal2Id"
                    ),
                    Expense(
                        type = TransactionType.INCOME.name,
                        category = "সার্ভিসিং বিল",
                        amount = 3500.0,
                        note = "iPhone 13 ব্যাটারি পরিবর্তন সার্ভিস চার্জ",
                        dateMillis = System.currentTimeMillis() - 86400000L * 1,
                        paymentMethod = "Cash"
                    )
                )
                expenseDao.insertAll(sampleExpenses)
            }
            prefs.edit().putBoolean("has_seeded_initial_data_v2", true).apply()
        }
    }

    suspend fun clearAllData() {
        productDao.deleteAllProducts()
        saleDao.deleteAllSales()
        expenseDao.deleteAllExpenses()
        employeeDao.deleteAllEmployees()
        employeeDao.deleteAllSalaryPayments()
        prefs.edit().putBoolean("has_seeded_initial_data_v2", true).apply()
    }

    suspend fun seedDemoData() {
        seedInitialDataIfEmpty(force = true)
    }
}
