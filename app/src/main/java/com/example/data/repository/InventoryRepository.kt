package com.example.data.repository

import android.content.Context
import com.example.data.db.ProductDao
import com.example.data.db.SaleDao
import com.example.data.model.Product
import com.example.data.model.ProductStatus
import com.example.data.model.Sale
import kotlinx.coroutines.flow.Flow

class InventoryRepository(
    private val context: Context,
    private val productDao: ProductDao,
    private val saleDao: SaleDao
) {
    private val prefs = context.getSharedPreferences("apple_area_bd_prefs", Context.MODE_PRIVATE)

    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val inStockProducts: Flow<List<Product>> = productDao.getInStockProducts()
    val allSales: Flow<List<Sale>> = saleDao.getAllSales()

    suspend fun addProduct(product: Product): Long {
        return productDao.insertProduct(product)
    }

    suspend fun addProducts(products: List<Product>) {
        productDao.insertAll(products)
    }

    suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product)
    }

    suspend fun getProductById(id: Long): Product? {
        return productDao.getProductById(id)
    }

    suspend fun getProductByImei(imei: String): Product? {
        return productDao.getProductByImei(imei)
    }

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

    /**
     * Seeds initial realistic mock inventory for APPLE AREA BD ONLY ONCE on fresh install.
     */
    suspend fun seedInitialDataIfEmpty(force: Boolean = false) {
        val hasSeeded = prefs.getBoolean("has_seeded_initial_data", false)
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

            // Also seed 1 sample sale to demonstrate report & sales features immediately
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
                soldAt = System.currentTimeMillis() - 86400000L * 2, // 2 days ago
                remarks = "বাকি ৬,০০০ টাকা আগামী মাসের ১ তারিখে দেবে।"
            )
            saleDao.insertSale(sampleSale)
            }
            prefs.edit().putBoolean("has_seeded_initial_data", true).apply()
        }
    }

    suspend fun clearAllData() {
        productDao.deleteAllProducts()
        saleDao.deleteAllSales()
        prefs.edit().putBoolean("has_seeded_initial_data", true).apply()
    }

    suspend fun seedDemoData() {
        seedInitialDataIfEmpty(force = true)
    }
}
