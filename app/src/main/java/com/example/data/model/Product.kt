package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ProductStatus {
    IN_STOCK,
    SOLD,
    RESERVED
}

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val company: String,        // কোম্পানি (Apple, Samsung, Google Pixel etc.)
    val model: String,          // মডেল (iPhone 15 Pro Max etc.)
    val variant: String,        // ভ্যারিয়েন্ট / স্টোরেজ (128GB, 256GB etc.)
    val color: String,          // কালার (Natural Titanium, Midnight etc.)
    val batteryHealth: String,  // ব্যাটারি হেল্থ (100%, 95%, Brand New etc.)
    val boxCondition: String,   // বক্সের অবস্থা (বক্স সহ, সিল প্যাক, বক্স ছাড়া etc.)
    val supplier: String,       // সাপ্লায়ার (Dubai Stock, USA LL/A, Local etc.)
    val purchasePrice: Double,  // কেনা মূল্য (৳)
    val targetSalePrice: Double,// প্রস্তাবিত বিক্রয় মূল্য (৳)
    val imei: String,           // আইএমইআই / বারকোড
    val status: String = ProductStatus.IN_STOCK.name,
    val notes: String = "",     // ওয়ারেন্টি বা বিশেষ নোট
    val createdAt: Long = System.currentTimeMillis()
)
