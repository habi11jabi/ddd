package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.Product
import com.example.data.model.ProductStatus
import com.example.data.model.Sale
import com.example.data.model.ShopSettings
import com.example.data.repository.InventoryRepository
import com.example.data.repository.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

enum class AppTab {
    DASHBOARD,
    STOCK,
    SALES,
    REPORTS,
    SETTINGS
}

data class DashboardMetrics(
    val inStockCount: Int = 0,
    val totalStockValue: Double = 0.0,
    val totalTargetValue: Double = 0.0,
    val totalSalesCount: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalProfit: Double = 0.0,
    val totalDue: Double = 0.0
)

data class MonthlyReport(
    val periodName: String,
    val unitsSold: Int,
    val totalRevenue: Double,
    val totalCost: Double,
    val totalProfit: Double,
    val totalPaid: Double,
    val totalDue: Double,
    val profitMarginPercent: Double,
    val topSellingModels: List<Pair<String, Int>>
)

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: InventoryRepository
    private val settingsManager: SettingsManager = SettingsManager(application)

    val allProducts: StateFlow<List<Product>>
    val allSales: StateFlow<List<Sale>>
    val shopSettings: StateFlow<ShopSettings> = settingsManager.settings

    // Navigation Tab
    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Search and Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow("ALL") // "ALL", "IN_STOCK", "SOLD"
    val selectedStatusFilter: StateFlow<String> = _selectedStatusFilter.asStateFlow()

    private val _selectedBrandFilter = MutableStateFlow("ALL")
    val selectedBrandFilter: StateFlow<String> = _selectedBrandFilter.asStateFlow()

    // Add / Edit Product Dialog / Screen State
    private val _isAddEditOpen = MutableStateFlow(false)
    val isAddEditOpen: StateFlow<Boolean> = _isAddEditOpen.asStateFlow()

    private val _editingProduct = MutableStateFlow<Product?>(null)
    val editingProduct: StateFlow<Product?> = _editingProduct.asStateFlow()

    // Sale Checkout Dialog State
    private val _productToSell = MutableStateFlow<Product?>(null)
    val productToSell: StateFlow<Product?> = _productToSell.asStateFlow()

    // Cash Memo / Invoice Dialog State
    private val _activeInvoiceSale = MutableStateFlow<Sale?>(null)
    val activeInvoiceSale: StateFlow<Sale?> = _activeInvoiceSale.asStateFlow()

    // Sale Edit Dialog State
    private val _editingSale = MutableStateFlow<Sale?>(null)
    val editingSale: StateFlow<Sale?> = _editingSale.asStateFlow()

    // Scanner Dialog State
    private val _isScannerOpen = MutableStateFlow(false)
    val isScannerOpen: StateFlow<Boolean> = _isScannerOpen.asStateFlow()

    // Due Payment Update Dialog
    private val _saleForDuePayment = MutableStateFlow<Sale?>(null)
    val saleForDuePayment: StateFlow<Sale?> = _saleForDuePayment.asStateFlow()

    // Report Period: "THIS_MONTH", "LAST_MONTH", "ALL_TIME"
    private val _reportPeriod = MutableStateFlow("THIS_MONTH")
    val reportPeriod: StateFlow<String> = _reportPeriod.asStateFlow()

    // User Feedback Message
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = InventoryRepository(application, db.productDao(), db.saleDao())

        allProducts = repository.allProducts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allSales = repository.allSales.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    // Filtered Products (Stock page only shows active in-stock products)
    val filteredProducts: StateFlow<List<Product>> = combine(
        allProducts,
        _searchQuery,
        _selectedBrandFilter
    ) { products, query, brand ->
        products.filter { product ->
            val isInStock = product.status == ProductStatus.IN_STOCK.name
            val matchesQuery = query.isBlank() ||
                    product.model.contains(query, ignoreCase = true) ||
                    product.company.contains(query, ignoreCase = true) ||
                    product.variant.contains(query, ignoreCase = true) ||
                    product.imei.contains(query, ignoreCase = true) ||
                    product.color.contains(query, ignoreCase = true) ||
                    product.supplier.contains(query, ignoreCase = true)

            val matchesBrand = if (brand == "ALL") true else product.company.equals(brand, ignoreCase = true)

            isInStock && matchesQuery && matchesBrand
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Metrics for Dashboard
    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        allProducts,
        allSales
    ) { products, sales ->
        val inStock = products.filter { it.status == ProductStatus.IN_STOCK.name }
        val inStockCount = inStock.size
        val stockVal = inStock.sumOf { it.purchasePrice }
        val targetVal = inStock.sumOf { it.targetSalePrice }

        val totalSalesCount = sales.size
        val totalRevenue = sales.sumOf { it.salePrice }
        val totalProfit = sales.sumOf { it.profit }
        val totalDue = sales.sumOf { it.dueAmount }

        DashboardMetrics(
            inStockCount = inStockCount,
            totalStockValue = stockVal,
            totalTargetValue = targetVal,
            totalSalesCount = totalSalesCount,
            totalRevenue = totalRevenue,
            totalProfit = totalProfit,
            totalDue = totalDue
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    // Monthly Report calculation
    val reportData: StateFlow<MonthlyReport> = combine(
        allSales,
        _reportPeriod
    ) { sales, period ->
        val cal = Calendar.getInstance()
        val now = System.currentTimeMillis()

        val filteredSales = when (period) {
            "THIS_MONTH" -> {
                cal.timeInMillis = now
                val currentMonth = cal.get(Calendar.MONTH)
                val currentYear = cal.get(Calendar.YEAR)
                sales.filter {
                    cal.timeInMillis = it.soldAt
                    cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
                }
            }
            "LAST_MONTH" -> {
                cal.timeInMillis = now
                cal.add(Calendar.MONTH, -1)
                val lastMonth = cal.get(Calendar.MONTH)
                val lastYear = cal.get(Calendar.YEAR)
                sales.filter {
                    cal.timeInMillis = it.soldAt
                    cal.get(Calendar.MONTH) == lastMonth && cal.get(Calendar.YEAR) == lastYear
                }
            }
            else -> sales
        }

        val periodLabel = when (period) {
            "THIS_MONTH" -> "চলতি মাসের হিসাব (This Month)"
            "LAST_MONTH" -> "বিগত মাসের হিসাব (Last Month)"
            else -> "সর্বমোট হিসাব (All Time)"
        }

        val totalRev = filteredSales.sumOf { it.salePrice }
        val totalCost = filteredSales.sumOf { it.costPrice }
        val totalProf = filteredSales.sumOf { it.profit }
        val totalPaid = filteredSales.sumOf { it.paidAmount }
        val totalDue = filteredSales.sumOf { it.dueAmount }
        val margin = if (totalRev > 0) (totalProf / totalRev) * 100 else 0.0

        val topModels = filteredSales
            .groupBy { it.productDetails }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(5)

        MonthlyReport(
            periodName = periodLabel,
            unitsSold = filteredSales.size,
            totalRevenue = totalRev,
            totalCost = totalCost,
            totalProfit = totalProf,
            totalPaid = totalPaid,
            totalDue = totalDue,
            profitMarginPercent = margin,
            topSellingModels = topModels
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MonthlyReport("চলতি মাসের হিসাব", 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, emptyList())
    )

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(status: String) {
        _selectedStatusFilter.value = status
    }

    fun setBrandFilter(brand: String) {
        _selectedBrandFilter.value = brand
    }

    fun setReportPeriod(period: String) {
        _reportPeriod.value = period
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    // Product actions
    fun openAddProduct() {
        _editingProduct.value = null
        _isAddEditOpen.value = true
    }

    fun openEditProduct(product: Product) {
        _editingProduct.value = product
        _isAddEditOpen.value = true
    }

    fun closeAddEdit() {
        _isAddEditOpen.value = false
        _editingProduct.value = null
    }

    fun saveProduct(
        company: String,
        model: String,
        variant: String,
        color: String,
        batteryHealth: String,
        boxCondition: String,
        supplier: String,
        purchasePrice: Double,
        targetSalePrice: Double,
        imei: String,
        notes: String
    ) {
        viewModelScope.launch {
            val existing = _editingProduct.value
            if (existing != null) {
                val updated = existing.copy(
                    company = company.trim(),
                    model = model.trim(),
                    variant = variant.trim(),
                    color = color.trim(),
                    batteryHealth = batteryHealth.trim(),
                    boxCondition = boxCondition.trim(),
                    supplier = supplier.trim(),
                    purchasePrice = purchasePrice,
                    targetSalePrice = targetSalePrice,
                    imei = imei.trim(),
                    notes = notes.trim()
                )
                repository.updateProduct(updated)
                _snackbarMessage.value = "পণ্য সফলভাবে আপডেট করা হয়েছে!"
            } else {
                val newProduct = Product(
                    company = company.trim(),
                    model = model.trim(),
                    variant = variant.trim(),
                    color = color.trim(),
                    batteryHealth = batteryHealth.trim(),
                    boxCondition = boxCondition.trim(),
                    supplier = supplier.trim(),
                    purchasePrice = purchasePrice,
                    targetSalePrice = targetSalePrice,
                    imei = imei.trim(),
                    notes = notes.trim(),
                    status = ProductStatus.IN_STOCK.name
                )
                repository.addProduct(newProduct)
                _snackbarMessage.value = "নতুন পণ্য সফলভাবে স্টকে যুক্ত হয়েছে!"
            }
            closeAddEdit()
        }
    }

    /**
     * Saves multiple products (5 to 10+ devices) at once from a single supplier batch.
     */
    fun saveBatchProducts(
        company: String,
        model: String,
        variant: String,
        color: String,
        batteryHealth: String,
        boxCondition: String,
        supplier: String,
        purchasePrice: Double,
        targetSalePrice: Double,
        imeis: List<String>,
        notes: String
    ) {
        viewModelScope.launch {
            val validImeis = imeis.map { it.trim() }.filter { it.isNotBlank() }
            if (validImeis.isEmpty()) return@launch

            val products = validImeis.map { imei ->
                Product(
                    company = company.trim(),
                    model = model.trim(),
                    variant = variant.trim(),
                    color = color.trim(),
                    batteryHealth = batteryHealth.trim(),
                    boxCondition = boxCondition.trim(),
                    supplier = supplier.trim(),
                    purchasePrice = purchasePrice,
                    targetSalePrice = targetSalePrice,
                    imei = imei,
                    notes = notes.trim(),
                    status = ProductStatus.IN_STOCK.name
                )
            }
            repository.addProducts(products)
            _snackbarMessage.value = "একসাথে ${products.size}টি প্রোডাক্ট সফলভাবে স্টকে যুক্ত হয়েছে!"
            closeAddEdit()
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            _snackbarMessage.value = "${product.model} স্টক থেকে মুছে ফেলা হয়েছে।"
        }
    }

    // Sale Checkout
    fun openSaleCheckout(product: Product) {
        _productToSell.value = product
    }

    fun closeSaleCheckout() {
        _productToSell.value = null
    }

    fun completeSale(
        customerName: String,
        customerPhone: String,
        customerAddress: String,
        salePrice: Double,
        paidAmount: Double,
        paymentMethod: String,
        warrantyInfo: String,
        remarks: String
    ) {
        val product = _productToSell.value ?: return
        viewModelScope.launch {
            val finalWarranty = warrantyInfo.ifBlank { shopSettings.value.fullWarrantyText }
            repository.recordSale(
                product = product,
                customerName = customerName.trim(),
                customerPhone = customerPhone.trim(),
                customerAddress = customerAddress.trim(),
                salePrice = salePrice,
                paidAmount = paidAmount,
                paymentMethod = paymentMethod.trim(),
                warrantyInfo = finalWarranty,
                remarks = remarks.trim()
            )
            _productToSell.value = null
            _snackbarMessage.value = "বিক্রি সম্পন্ন হয়েছে ও ইনভয়েস তৈরি হয়েছে!"
            _currentTab.value = AppTab.SALES
        }
    }

    // Invoice Memo
    fun openInvoice(sale: Sale) {
        _activeInvoiceSale.value = sale
    }

    fun closeInvoice() {
        _activeInvoiceSale.value = null
    }

    // Sale Edit & Delete
    fun openEditSale(sale: Sale) {
        _editingSale.value = sale
    }

    fun closeEditSale() {
        _editingSale.value = null
    }

    fun saveEditedSale(sale: Sale) {
        viewModelScope.launch {
            repository.updateSale(sale)
            _editingSale.value = null
            // Update active invoice if it was open
            if (_activeInvoiceSale.value?.id == sale.id) {
                _activeInvoiceSale.value = sale
            }
            _snackbarMessage.value = "বিক্রয় মেমো সফলভাবে আপডেট করা হয়েছে!"
        }
    }

    fun deleteSale(sale: Sale, restoreStock: Boolean) {
        viewModelScope.launch {
            repository.deleteSale(sale, restoreStock)
            _editingSale.value = null
            _activeInvoiceSale.value = null
            _snackbarMessage.value = if (restoreStock) "মেমো মুছে ডিভাইসটি স্টকে ফেরত আনা হয়েছে।" else "মেমো মুছে ফেলা হয়েছে।"
        }
    }

    // Due Payment Update
    fun openDuePayment(sale: Sale) {
        _saleForDuePayment.value = sale
    }

    fun closeDuePayment() {
        _saleForDuePayment.value = null
    }

    fun recordDuePayment(sale: Sale, additionalPayment: Double) {
        viewModelScope.launch {
            val newPaid = sale.paidAmount + additionalPayment
            val newDue = (sale.dueAmount - additionalPayment).coerceAtLeast(0.0)
            repository.updateDuePayment(sale.id, newPaid, newDue)
            _saleForDuePayment.value = null
            _snackbarMessage.value = "৳ ${formatBdt(additionalPayment)} বকেয়া জমা সফল হয়েছে!"
        }
    }

    // Settings Customization
    fun updateShopSettings(settings: ShopSettings) {
        settingsManager.saveSettings(settings)
        _snackbarMessage.value = "দোকান ও মেমো সেটিংস সংরক্ষিত হয়েছে!"
    }

    fun resetShopSettings() {
        settingsManager.resetToDefaults()
        _snackbarMessage.value = "ডিফল্ট সেটিংস সফলভাবে রিস্টোর হয়েছে!"
    }

    fun clearAllDatabaseData() {
        viewModelScope.launch {
            repository.clearAllData()
            _snackbarMessage.value = "সকল স্টক এবং বিক্রয় তথ্য মুছে ফেলা হয়েছে।"
        }
    }

    fun loadDemoData() {
        viewModelScope.launch {
            repository.seedDemoData()
            _snackbarMessage.value = "ডেমো স্টক এবং বিক্রয় তথ্য সফলভাবে লোড হয়েছে।"
        }
    }

    // Scanner
    fun openScanner() {
        _isScannerOpen.value = true
    }

    fun closeScanner() {
        _isScannerOpen.value = false
    }

    companion object {
        fun formatAmount(amount: Double, language: String = "BN"): String {
            val locale = if (language.equals("EN", ignoreCase = true)) Locale.US else Locale("bn", "BD")
            val format = NumberFormat.getNumberInstance(locale)
            return format.format(amount.toLong())
        }

        fun formatBdt(amount: Double): String {
            return formatAmount(amount, "BN")
        }
    }
}
