package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.Sale
import com.example.ui.theme.AppleBlue
import com.example.viewmodel.InventoryViewModel

@Composable
fun EditSaleDialog(
    sale: Sale,
    onDismiss: () -> Unit,
    onSaveSale: (Sale) -> Unit,
    onDeleteSale: (Sale, restoreStock: Boolean) -> Unit
) {
    var customerName by remember { mutableStateOf(sale.customerName) }
    var customerPhone by remember { mutableStateOf(sale.customerPhone) }
    var customerAddress by remember { mutableStateOf(sale.customerAddress) }

    var salePriceStr by remember { mutableStateOf(sale.salePrice.toLong().toString()) }
    var paidAmountStr by remember { mutableStateOf(sale.paidAmount.toLong().toString()) }
    var paymentMethod by remember { mutableStateOf(sale.paymentMethod) }
    var warrantyInfo by remember { mutableStateOf(sale.warrantyInfo) }
    var remarks by remember { mutableStateOf(sale.remarks) }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var restoreStockChecked by remember { mutableStateOf(true) }

    val salePrice = salePriceStr.toDoubleOrNull() ?: sale.salePrice
    val paidAmount = paidAmountStr.toDoubleOrNull() ?: sale.paidAmount
    val dueAmount = (salePrice - paidAmount).coerceAtLeast(0.0)

    val strings = com.example.ui.LocalAppStrings.current
    val isEnglish = strings.tabDashboard == "Dashboard"
    val lang = if (isEnglish) "EN" else "BN"

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(if (isEnglish) "Delete sale memo?" else "বিক্রয় মেমো মুছে ফেলতে চান?", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (isEnglish) "Invoice #${sale.invoiceNumber} and its transaction will be removed." else "ইনভয়েস #${sale.invoiceNumber} এবং এই বিক্রয়ের যাবতীয় হিসাব মুছে যাবে।")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Checkbox(
                            checked = restoreStockChecked,
                            onCheckedChange = { restoreStockChecked = it }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isEnglish) "Restore device back into In-Stock inventory" else "ডিভাইসটি আবার ইন স্টকে (In Stock) ফেরত আনুন", fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteSale(sale, restoreStockChecked)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text(if (isEnglish) "Yes, Delete" else "হ্যাঁ, ডিলিট করুন")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 20.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = AppleBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "মেমো ও বিক্রয় তথ্য এডিট",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ইনভয়েস: #${sale.invoiceNumber}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Device summary card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(text = sale.productDetails, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "IMEI: ${sale.imei}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Customer Fields
                Text(text = "ক্রেতার তথ্য পরিবর্তন:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppleBlue)

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("ক্রেতার নাম") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text("মোবাইল নম্বর") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = customerAddress,
                    onValueChange = { customerAddress = it },
                    label = { Text("ঠিকানা / এনআইডি") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Financial Fields
                Text(text = "বিক্রয় ও পরিশোধের হিসাব:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppleBlue)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = salePriceStr,
                        onValueChange = { salePriceStr = it },
                        label = { Text("বিক্রয় মূল্য (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = paidAmountStr,
                        onValueChange = { paidAmountStr = it },
                        label = { Text("পরিশোধিত টাকা (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Due pill
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (dueAmount > 0) Color(0xFFFEF3C7) else Color(0xFFD1FAE5)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (dueAmount > 0) "বর্তমান বকেয়া:" else "পরিশোধিত (Paid In Full)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (dueAmount > 0) Color(0xFF92400E) else Color(0xFF065F46)
                        )
                        Text(
                            text = "৳ ${InventoryViewModel.formatBdt(dueAmount)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = if (dueAmount > 0) Color(0xFFB45309) else Color(0xFF047857)
                        )
                    }
                }

                OutlinedTextField(
                    value = paymentMethod,
                    onValueChange = { paymentMethod = it },
                    label = { Text("পেমেন্ট মাধ্যম") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Warranty Info editing
                Text(text = "ওয়ারেন্টি পলিসি ও শর্তাবলী পরিবর্তন:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppleBlue)

                OutlinedTextField(
                    value = warrantyInfo,
                    onValueChange = { warrantyInfo = it },
                    label = { Text("ক্যাশ মেমোর ওয়ারেন্টি কার্ড টেক্সট") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("মন্তব্য / বিশেষ নোট") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Action Buttons: Save and Delete
                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        val updated = sale.copy(
                            customerName = customerName.trim(),
                            customerPhone = customerPhone.trim(),
                            customerAddress = customerAddress.trim(),
                            salePrice = salePrice,
                            paidAmount = paidAmount,
                            dueAmount = dueAmount,
                            paymentMethod = paymentMethod.trim(),
                            warrantyInfo = warrantyInfo.trim(),
                            remarks = remarks.trim()
                        )
                        onSaveSale(updated)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_edited_sale_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = "Save")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isEnglish) "Update Memo & Sale" else "মেমো আপডেট করুন", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("delete_sale_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isEnglish) "Delete This Sale Memo" else "এই বিক্রয় মেমো ডিলিট করুন", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
