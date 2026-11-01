package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Product
import com.example.data.model.ShopSettings
import com.example.ui.theme.AppleBlue
import com.example.viewmodel.InventoryViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SaleCheckoutDialog(
    product: Product,
    shopSettings: ShopSettings = ShopSettings(),
    onDismiss: () -> Unit,
    onConfirmSale: (
        customerName: String,
        customerPhone: String,
        customerAddress: String,
        salePrice: Double,
        paidAmount: Double,
        paymentMethod: String,
        warrantyInfo: String,
        remarks: String
    ) -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var customerAddress by remember { mutableStateOf("") }

    var salePriceStr by remember { mutableStateOf(product.targetSalePrice.toLong().toString()) }
    var paidAmountStr by remember { mutableStateOf(product.targetSalePrice.toLong().toString()) }
    var paymentMethod by remember { mutableStateOf(shopSettings.paymentMethodPresets.firstOrNull() ?: "ক্যাশ (Cash)") }
    var warrantyInfo by remember { mutableStateOf(shopSettings.fullWarrantyText) }
    var remarks by remember { mutableStateOf("") }

    var validationError by remember { mutableStateOf<String?>(null) }

    val paymentMethods = shopSettings.paymentMethodPresets

    val salePrice = salePriceStr.toDoubleOrNull() ?: 0.0
    val paidAmount = paidAmountStr.toDoubleOrNull() ?: 0.0
    val dueAmount = (salePrice - paidAmount).coerceAtLeast(0.0)
    val estimatedProfit = salePrice - product.purchasePrice

    val strings = com.example.ui.LocalAppStrings.current
    val isEnglish = strings.tabDashboard == "Dashboard"
    val lang = if (isEnglish) "EN" else "BN"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Title Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(AppleBlue.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PointOfSale,
                                contentDescription = "Sale",
                                tint = AppleBlue
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = strings.checkoutTitle,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${strings.appTitle} • ${strings.invoiceTitle}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_sale_dialog_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Selected Product Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${product.company} ${product.model}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "ভ্যারিয়েন্ট: ${product.variant} | কালার: ${product.color}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "আইএমইআই: ${product.imei}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppleBlue
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "ব্যাটারি: ${product.batteryHealth}",
                                fontSize = 11.sp,
                                color = Color(0xFF10B981)
                            )
                            Text(
                                text = "বক্স: ${product.boxCondition}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Customer Info Fields
                Text(
                    text = "ক্রেতার তথ্য (Customer Details):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("ক্রেতার নাম *") },
                    placeholder = { Text("যেমন: মোঃ তানভীর হাসান") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text("মোবাইল নম্বর *") },
                    placeholder = { Text("যেমন: 01712345678") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_phone_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = customerAddress,
                    onValueChange = { customerAddress = it },
                    label = { Text("ঠিকানা / এনআইডি নম্বর (ঐচ্ছিক)") },
                    placeholder = { Text("যেমন: ধানমন্ডি, ঢাকা") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_address_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Price and Due
                Text(
                    text = "টাকা ও পেমেন্ট হিসাব:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = salePriceStr,
                        onValueChange = {
                            salePriceStr = it
                            // If paid wasn't modified separately, keep in sync
                            if (paidAmountStr.isBlank() || paidAmountStr == salePriceStr) {
                                paidAmountStr = it
                            }
                        },
                        label = { Text("বিক্রয় মূল্য (৳) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sale_price_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = paidAmountStr,
                        onValueChange = { paidAmountStr = it },
                        label = { Text("পরিশোধিত টাকা (৳) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("paid_amount_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Due & Profit Summary Pill
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (dueAmount > 0) Color(0xFFFEF3C7) else Color(0xFFD1FAE5)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (dueAmount > 0) "বকেয়া / বাকি টাকা:" else "সম্পূর্ণ পরিশোধিত (Paid In Full)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (dueAmount > 0) Color(0xFF92400E) else Color(0xFF065F46)
                            )
                            if (dueAmount > 0) {
                                Text(
                                    text = "৳ ${InventoryViewModel.formatBdt(dueAmount)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "এই বিক্রিতে মোট লাভ:",
                                fontSize = 11.sp,
                                color = Color(0xFF065F46)
                            )
                            Text(
                                text = "৳ ${InventoryViewModel.formatBdt(estimatedProfit)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (estimatedProfit >= 0) Color(0xFF047857) else Color.Red
                            )
                        }
                    }
                }

                // Payment Method
                Text(
                    text = "পেমেন্ট মাধ্যম (Payment Method):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    paymentMethods.forEach { method ->
                        FilterChip(
                            selected = paymentMethod == method,
                            onClick = { paymentMethod = method },
                            label = { Text(method, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppleBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Warranty
                OutlinedTextField(
                    value = warrantyInfo,
                    onValueChange = { warrantyInfo = it },
                    label = { Text("ওয়ারেন্টি বিবরণ") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("warranty_info_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Remarks
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("বিশেষ নোট বা বকেয়া পরিশোধের তারিখ (ঐচ্ছিক)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                validationError?.let { err ->
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Action Buttons
                Button(
                    onClick = {
                        if (customerName.isBlank()) {
                            validationError = if (isEnglish) "Please enter customer name." else "ক্রেতার নাম প্রদান করুন।"
                            return@Button
                        }
                        if (customerPhone.isBlank()) {
                            validationError = if (isEnglish) "Please enter customer phone number." else "ক্রেতার মোবাইল নম্বর প্রদান করুন।"
                            return@Button
                        }
                        if (salePrice <= 0) {
                            validationError = if (isEnglish) "Please enter a valid sale price." else "সঠিক বিক্রয় মূল্য লিখুন।"
                            return@Button
                        }
                        validationError = null
                        onConfirmSale(
                            customerName,
                            customerPhone,
                            customerAddress,
                            salePrice,
                            paidAmount,
                            paymentMethod,
                            warrantyInfo,
                            remarks
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("confirm_sale_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.PointOfSale, contentDescription = "Sell")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.btnCompleteSale, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
