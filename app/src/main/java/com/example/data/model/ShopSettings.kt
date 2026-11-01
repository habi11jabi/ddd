package com.example.data.model

data class ShopSettings(
    val appLanguage: String = "BN", // "BN" (বাংলা) or "EN" (English)
    val shopName: String = "Shop App",
    val shopTagline: String = "এক্সক্লুসিভ স্মার্টফোন ও ইলেকট্রনিক্স শপ",
    val shopPhone: String = "01700000000",
    val shopAddress: String = "ঢাকা, বাংলাদেশ",
    val warrantyReplacement: String = "7 Days Replacement Guarantee Without physical Damage & Touch Display.",
    val warrantyService: String = "2 Years Service Warranty Without Parts & update issue.",
    val warrantyExchangeDeduction: String = "30% - 35% Deducted From Sell Amount and Exchange.",
    val companyPresets: List<String> = listOf("Apple", "Samsung", "Google", "Xiaomi", "OnePlus", "Vivo", "Oppo", "Realme"),
    val variantPresets: List<String> = listOf("64GB", "128GB", "256GB", "512GB", "1TB"),
    val colorPresets: List<String> = listOf("Natural Titanium", "Desert Titanium", "Black Titanium", "White Titanium", "Deep Purple", "Midnight", "Starlight", "Blue", "Space Gray"),
    val batteryPresets: List<String> = listOf("100% (Brand New)", "98%", "95%", "92%", "89%", "85%"),
    val boxConditionPresets: List<String> = listOf("সিল প্যাক (Sealed Box)", "বক্স সহ (Original Box)", "আইএমইআই ম্যাচিং বক্স", "বক্স ছাড়া (Device Only)", "চার্জার সহ"),
    val supplierPresets: List<String> = listOf("Dubai Stock", "USA LL/A", "Singapore ZP/A", "Japan J/A", "Local Dealer", "Customer Exchange"),
    val paymentMethodPresets: List<String> = listOf("ক্যাশ (Cash)", "বিকাশ (bKash)", "নগদ (Nagad)", "ব্যাংক কার্ড", "ক্যাশ + বিকাশ")
) {
    val fullWarrantyText: String
        get() = "• $warrantyReplacement\n• $warrantyService\n• $warrantyExchangeDeduction"
}
