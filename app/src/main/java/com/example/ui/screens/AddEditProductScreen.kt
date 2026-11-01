package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.data.model.ShopSettings
import com.example.ui.components.ScannerDialog
import com.example.ui.theme.AppleBlue

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditProductScreen(
    editingProduct: Product?,
    shopSettings: ShopSettings = ShopSettings(),
    onSave: (
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
    ) -> Unit,
    onBack: () -> Unit
) {
    // 1. কোম্পানি (Company)
    var company by remember { mutableStateOf(editingProduct?.company ?: (shopSettings.companyPresets.firstOrNull() ?: "Apple")) }
    // 2. মডেল (Model)
    var model by remember { mutableStateOf(editingProduct?.model ?: "") }
    // 3. ভ্যারিয়েন্ট (Variant)
    var variant by remember { mutableStateOf(editingProduct?.variant ?: (shopSettings.variantPresets.getOrNull(2) ?: "256GB")) }
    // 4. কালার (Color)
    var color by remember { mutableStateOf(editingProduct?.color ?: (shopSettings.colorPresets.firstOrNull() ?: "Natural Titanium")) }
    // 5. ব্যাটারি হেল্থ (Battery Health)
    var batteryHealth by remember { mutableStateOf(editingProduct?.batteryHealth ?: "100%") }
    // 6. বক্সের অবস্থা (Box Condition)
    var boxCondition by remember { mutableStateOf(editingProduct?.boxCondition ?: (shopSettings.boxConditionPresets.firstOrNull() ?: "সিল প্যাক (Sealed Box)")) }
    // 7. সাপ্লায়ার (Supplier)
    var supplier by remember { mutableStateOf(editingProduct?.supplier ?: (shopSettings.supplierPresets.firstOrNull() ?: "Dubai Stock")) }
    // 8. কেনা মূল্য (Purchase Price)
    var purchasePriceStr by remember { mutableStateOf(editingProduct?.purchasePrice?.let { if (it > 0) it.toLong().toString() else "" } ?: "") }
    // 9. প্রস্তাবিত বিক্রয় মূল্য (Target Sale Price)
    var targetSalePriceStr by remember { mutableStateOf(editingProduct?.targetSalePrice?.let { if (it > 0) it.toLong().toString() else "" } ?: "") }
    // 10. আইএমইআই / বারকোড (IMEI / Barcode)
    var imei by remember { mutableStateOf(editingProduct?.imei ?: "") }
    // 11. নোট / ওয়ারেন্টি
    var notes by remember { mutableStateOf(editingProduct?.notes ?: "USA LL/A, 1 Year Service Warranty") }

    var isScannerOpen by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    // Preset options from customizable settings
    val companyPresets = shopSettings.companyPresets
    val variantPresets = shopSettings.variantPresets
    val colorPresets = shopSettings.colorPresets
    val batteryPresets = shopSettings.batteryPresets
    val boxConditionPresets = shopSettings.boxConditionPresets
    val supplierPresets = shopSettings.supplierPresets

    if (isScannerOpen) {
        ScannerDialog(
            onDismiss = { isScannerOpen = false },
            onBarcodeScanned = { scannedImei ->
                imei = scannedImei
                isScannerOpen = false
            }
        )
    }

    val strings = com.example.ui.LocalAppStrings.current
    val isEnglish = strings.tabDashboard == "Dashboard"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (editingProduct == null) strings.addProductTitle else strings.editProductTitle,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${strings.appTitle} • ${strings.productFormSubtitle}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("add_product_back_button")
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header instruction
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AppleBlue.copy(alpha = 0.08f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(AppleBlue, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("1", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isEnglish) "Fill Product Information" else "প্রোডাক্ট ইনফরমেশন পূরণ করুন",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = AppleBlue
                            )
                            Text(
                                text = if (isEnglish) "Company > Model > Variant > Color > Battery > Box > Supplier > Price" else "কোম্পানি > মডেল > ভ্যারিয়েন্ট > কালার > ব্যাটারি > বক্স > সাপ্লায়ার > মূল্য",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 1. কোম্পানি (Company)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "১. কোম্পানি / ব্র্যান্ড (Company) *",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        companyPresets.forEach { preset ->
                            FilterChip(
                                selected = company.equals(preset, ignoreCase = true),
                                onClick = { company = preset },
                                label = { Text(preset, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AppleBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    OutlinedTextField(
                        value = company,
                        onValueChange = { company = it },
                        label = { Text("অন্য কোম্পানি নাম লিখুন") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_company_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // 2. মডেল (Model)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "২. মডেল (Model) *",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        placeholder = { Text("যেমন: iPhone 15 Pro Max, iPhone 14 Pro, Galaxy S24...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_model_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // 3. ভ্যারিয়েন্ট (Variant)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "৩. ভ্যারিয়েন্ট / স্টোরেজ (Variant) *",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        variantPresets.forEach { preset ->
                            FilterChip(
                                selected = variant == preset,
                                onClick = { variant = preset },
                                label = { Text(preset, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AppleBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    OutlinedTextField(
                        value = variant,
                        onValueChange = { variant = it },
                        label = { Text("কাস্টম ভ্যারিয়েন্ট লিখুন") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_variant_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // 4. কালার (Color)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "৪. কালার (Color) *",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        colorPresets.forEach { preset ->
                            FilterChip(
                                selected = color == preset,
                                onClick = { color = preset },
                                label = { Text(preset, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AppleBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    OutlinedTextField(
                        value = color,
                        onValueChange = { color = it },
                        label = { Text("অন্য কালার লিখুন") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_color_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // 5. ব্যাটারি হেল্থ (Battery Health)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "৫. ব্যাটারি হেল্থ (Battery Health) *",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        batteryPresets.forEach { preset ->
                            FilterChip(
                                selected = batteryHealth == preset,
                                onClick = { batteryHealth = preset },
                                label = { Text(preset, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AppleBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    OutlinedTextField(
                        value = batteryHealth,
                        onValueChange = { batteryHealth = it },
                        placeholder = { Text("যেমন: 100%, 94%, Brand New...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_battery_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // 6. বক্সের অবস্থা (Box Condition)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "৬. বক্সের অবস্থা (Box Condition) *",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        boxConditionPresets.forEach { preset ->
                            FilterChip(
                                selected = boxCondition == preset,
                                onClick = { boxCondition = preset },
                                label = { Text(preset, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AppleBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    OutlinedTextField(
                        value = boxCondition,
                        onValueChange = { boxCondition = it },
                        label = { Text("অন্য বক্স অবস্থা লিখুন") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_box_condition_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // 7. সাপ্লায়ার (Supplier)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "৭. সাপ্লায়ার / সোর্স (Supplier) *",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        supplierPresets.forEach { preset ->
                            FilterChip(
                                selected = supplier == preset,
                                onClick = { supplier = preset },
                                label = { Text(preset, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AppleBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    OutlinedTextField(
                        value = supplier,
                        onValueChange = { supplier = it },
                        label = { Text("সাপ্লায়ার বা এক্সচেঞ্জ ডিলারের নাম লিখুন") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_supplier_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // 8. কেনা মূল্য & 9. প্রস্তাবিত বিক্রয় মূল্য (Prices)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "৮ ও ৯. মূল্য ও লাভের হিসাব (Pricing)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = purchasePriceStr,
                                onValueChange = { purchasePriceStr = it },
                                label = { Text("৮. কেনা মূল্য (৳) *") },
                                placeholder = { Text("যেমন: 125000") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("product_purchase_price_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = targetSalePriceStr,
                                onValueChange = { targetSalePriceStr = it },
                                label = { Text("৯. বিক্রয় মূল্য (৳) *") },
                                placeholder = { Text("যেমন: 135000") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("product_target_price_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        val cost = purchasePriceStr.toDoubleOrNull() ?: 0.0
                        val target = targetSalePriceStr.toDoubleOrNull() ?: 0.0
                        if (target > 0 && cost > 0) {
                            val estimatedProfit = target - cost
                            Text(
                                text = "প্রত্যাশিত লাভ: ৳ ${com.example.viewmodel.InventoryViewModel.formatBdt(estimatedProfit)} (${String.format("%.1f", (estimatedProfit / target) * 100)}%)",
                                color = if (estimatedProfit >= 0) Color(0xFF10B981) else Color.Red,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // 10. আইএমইআই / বারকোড (IMEI / Barcode) with Scanner Button
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "১০. আইএমইআই / বারকোড স্ক্যানার (IMEI / Barcode) *",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = imei,
                            onValueChange = { imei = it },
                            placeholder = { Text("15 ডিজিট আইএমইআই লিখুন বা স্ক্যান করুন") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("product_imei_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = { isScannerOpen = true },
                            modifier = Modifier
                                .height(54.dp)
                                .testTag("open_scanner_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = "Scan")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("স্ক্যান", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 11. ওয়ারেন্টি ও নোট
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "১১. ওয়ারেন্টি ও বিশেষ নোট (Warranty & Notes)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("যেমন: USA LL/A, 10 Days Replacement, 1 Year Service...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_notes_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Validation error message if any
            validationError?.let { err ->
                item {
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Save / Update Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val cost = purchasePriceStr.toDoubleOrNull()
                        val target = targetSalePriceStr.toDoubleOrNull()
                        if (company.isBlank() || model.isBlank()) {
                            validationError = if (isEnglish) "Please enter company name and model." else "অনুগ্রহ করে কোম্পানির নাম ও মডেল উল্লেখ করুন।"
                            return@Button
                        }
                        if (cost == null || cost <= 0) {
                            validationError = if (isEnglish) "Please enter a valid purchase/cost price." else "অনুগ্রহ করে সঠিক কেনা মূল্য উল্লেখ করুন।"
                            return@Button
                        }
                        if (target == null || target <= 0) {
                            validationError = if (isEnglish) "Please enter a valid target sale price." else "অনুগ্রহ করে সঠিক বিক্রয় মূল্য উল্লেখ করুন।"
                            return@Button
                        }
                        if (imei.isBlank()) {
                            validationError = if (isEnglish) "Please enter or scan an IMEI/serial number." else "আইএমইআই বা সিরিয়াল নম্বর প্রদান করুন।"
                            return@Button
                        }

                        validationError = null
                        onSave(
                            company,
                            model,
                            variant,
                            color,
                            batteryHealth,
                            boxCondition,
                            supplier,
                            cost,
                            target,
                            imei,
                            notes
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_product_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Save")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (editingProduct == null) strings.btnSaveProduct else (if (isEnglish) "Update Product" else "তথ্য আপডেট করুন"),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
