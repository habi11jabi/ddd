package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    EXPENSE,
    INCOME
}

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String = TransactionType.EXPENSE.name, // EXPENSE or INCOME
    val category: String, // দোকান ভাড়া, বিদ্যুৎ বিল, নাস্তা/আপ্যায়ন, বেতন, ওয়াইফাই, অন্যান্য
    val amount: Double,
    val note: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val paymentMethod: String = "Cash", // Cash, bKash, Nagad, Bank
    val referenceId: String? = null // optional link, e.g. "SALARY_1"
)

object ExpenseCategories {
    val commonExpenseCategories = listOf(
        "দোকান ভাড়া",
        "বিদ্যুৎ ও ইউটিলিটি",
        "নাস্তা ও আপ্যায়ন",
        "কর্মচারীর বেতন",
        "ওয়াইফাই ও ইন্টারনেট",
        "যাতায়াত ও ডেলিভারি",
        "মোবাইল পার্টস ও টুলস",
        "দোকান সাজসজ্জা ও মেরামত",
        "মার্কেটিং ও বিজ্ঞাপন",
        "অন্যান্য খরচ"
    )

    val commonIncomeCategories = listOf(
        "সার্ভিসিং বিল",
        "এক্সেসরিজ বিক্রি লাভ",
        "কমিশন / এক্সচেঞ্জ লাভ",
        "অন্যান্য আয়"
    )
}
