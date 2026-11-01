package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val invoiceNumber: String,     // যেমন: AABD-2024-001
    val productDetails: String,    // ব্র্যান্ড, মডেল ও ভ্যারিয়েন্ট
    val imei: String,              // ডিভাইসের আইএমইআই
    val batteryHealth: String = "",// ব্যাটারি হেল্থ
    val boxCondition: String = "", // বক্সের অবস্থা
    val customerName: String,      // ক্রেতার নাম
    val customerPhone: String,     // ক্রেতার মোবাইল নম্বর
    val customerAddress: String,   // ঠিকানা / এনআইডি
    val costPrice: Double,         // কেনার খরচ (৳)
    val salePrice: Double,         // বিক্রয় মূল্য (৳)
    val paidAmount: Double,        // পরিশোধিত টাকা (৳)
    val dueAmount: Double,         // বকেয়া টাকা (৳)
    val paymentMethod: String,     // ক্যাশ, বিকাশ, নগদ, ব্যাংক কার্ড
    val warrantyInfo: String,      // ওয়ারেন্টি বিবরণ
    val soldAt: Long = System.currentTimeMillis(),
    val remarks: String = ""
) {
    val profit: Double
        get() = salePrice - costPrice
}
