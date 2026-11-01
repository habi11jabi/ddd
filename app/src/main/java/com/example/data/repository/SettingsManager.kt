package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.ShopSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("apple_area_bd_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<ShopSettings> = _settings.asStateFlow()

    private fun loadSettings(): ShopSettings {
        val appLanguage = prefs.getString("app_language", "BN") ?: "BN"
        val shopName = prefs.getString("shop_name", "APPLE AREA BD") ?: "APPLE AREA BD"
        val shopTagline = prefs.getString("shop_tagline", "Buy Sell & Exchange") ?: "Buy Sell & Exchange"
        val shopPhone = prefs.getString("shop_phone", "01930833375, 01758596640") ?: "01930833375, 01758596640"
        val shopAddress = prefs.getString("shop_address", "Shop No # 100, Basement - 1, Bashundhara City Shopping Mall, Panthapath, Dhaka- 1205")
            ?: "Shop No # 100, Basement - 1, Bashundhara City Shopping Mall, Panthapath, Dhaka- 1205"

        val warrantyReplacement = prefs.getString("w_replacement", "7 Days Replacement Guarantee Without physical Damage & Touch Display.")
            ?: "7 Days Replacement Guarantee Without physical Damage & Touch Display."
        val warrantyService = prefs.getString("w_service", "2 Years Service Warranty Without Parts & update issue.")
            ?: "2 Years Service Warranty Without Parts & update issue."
        val warrantyExchangeDeduction = prefs.getString("w_deduction", "30% - 35% Deducted From Sell Amount and Exchange.")
            ?: "30% - 35% Deducted From Sell Amount and Exchange."

        val companies = loadList("presets_companies", listOf("Apple", "Samsung", "Google", "Xiaomi", "OnePlus", "Vivo", "Oppo", "Realme"))
        val variants = loadList("presets_variants", listOf("64GB", "128GB", "256GB", "512GB", "1TB"))
        val colors = loadList("presets_colors", listOf("Natural Titanium", "Desert Titanium", "Black Titanium", "White Titanium", "Deep Purple", "Midnight", "Starlight", "Blue", "Space Gray"))
        val batteries = loadList("presets_batteries", listOf("100% (Brand New)", "98%", "95%", "92%", "89%", "85%"))
        val boxes = loadList("presets_boxes", listOf("সিল প্যাক (Sealed Box)", "বক্স সহ (Original Box)", "আইএমইআই ম্যাচিং বক্স", "বক্স ছাড়া (Device Only)", "চার্জার সহ"))
        val suppliers = loadList("presets_suppliers", listOf("Dubai Stock", "USA LL/A", "Singapore ZP/A", "Japan J/A", "Local Dealer", "Customer Exchange"))
        val payments = loadList("presets_payments", listOf("ক্যাশ (Cash)", "বিকাশ (bKash)", "নগদ (Nagad)", "ব্যাংক কার্ড", "ক্যাশ + বিকাশ"))

        return ShopSettings(
            appLanguage = appLanguage,
            shopName = shopName,
            shopTagline = shopTagline,
            shopPhone = shopPhone,
            shopAddress = shopAddress,
            warrantyReplacement = warrantyReplacement,
            warrantyService = warrantyService,
            warrantyExchangeDeduction = warrantyExchangeDeduction,
            companyPresets = companies,
            variantPresets = variants,
            colorPresets = colors,
            batteryPresets = batteries,
            boxConditionPresets = boxes,
            supplierPresets = suppliers,
            paymentMethodPresets = payments
        )
    }

    private fun loadList(key: String, defaultList: List<String>): List<String> {
        val raw = prefs.getString(key, null) ?: return defaultList
        return if (raw.isBlank()) defaultList else raw.split("|||")
    }

    private fun saveList(key: String, list: List<String>) {
        prefs.edit().putString(key, list.joinToString("|||")).apply()
    }

    fun saveSettings(newSettings: ShopSettings) {
        prefs.edit()
            .putString("app_language", newSettings.appLanguage)
            .putString("shop_name", newSettings.shopName)
            .putString("shop_tagline", newSettings.shopTagline)
            .putString("shop_phone", newSettings.shopPhone)
            .putString("shop_address", newSettings.shopAddress)
            .putString("w_replacement", newSettings.warrantyReplacement)
            .putString("w_service", newSettings.warrantyService)
            .putString("w_deduction", newSettings.warrantyExchangeDeduction)
            .apply()

        saveList("presets_companies", newSettings.companyPresets)
        saveList("presets_variants", newSettings.variantPresets)
        saveList("presets_colors", newSettings.colorPresets)
        saveList("presets_batteries", newSettings.batteryPresets)
        saveList("presets_boxes", newSettings.boxConditionPresets)
        saveList("presets_suppliers", newSettings.supplierPresets)
        saveList("presets_payments", newSettings.paymentMethodPresets)

        _settings.value = newSettings
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        _settings.value = ShopSettings()
    }
}
