package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Sale
import com.example.ui.theme.AppleBlue
import com.example.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.material.icons.filled.Edit

@Composable
fun SalesScreen(
    sales: List<Sale>,
    onViewInvoice: (Sale) -> Unit,
    onCollectDue: (Sale) -> Unit,
    onEditSale: (Sale) -> Unit = {}
) {
    val context = LocalContext.current
    val strings = com.example.ui.LocalAppStrings.current
    val isEnglish = strings.tabDashboard == "Dashboard"
    val lang = if (isEnglish) "EN" else "BN"

    var searchQuery by remember { mutableStateOf("") }
    var showOnlyDue by remember { mutableStateOf(false) }

    val filteredSales = sales.filter { sale ->
        val matchesQuery = searchQuery.isBlank() ||
                sale.customerName.contains(searchQuery, ignoreCase = true) ||
                sale.customerPhone.contains(searchQuery, ignoreCase = true) ||
                sale.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                sale.productDetails.contains(searchQuery, ignoreCase = true) ||
                sale.imei.contains(searchQuery, ignoreCase = true)

        val matchesDue = !showOnlyDue || sale.dueAmount > 0

        matchesQuery && matchesDue
    }

    val totalSalesCount = sales.size
    val totalRevenue = sales.sumOf { it.salePrice }
    val totalDue = sales.sumOf { it.dueAmount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(strings.invoiceSearchPlaceholder, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = AppleBlue)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sales_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        // Filter and Financial summary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !showOnlyDue,
                        onClick = { showOnlyDue = false },
                        label = { Text("${if (isEnglish) "All Sales" else "সকল বিক্রি"} ($totalSalesCount)", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppleBlue,
                            selectedLabelColor = Color.White
                        )
                    )

                    FilterChip(
                        selected = showOnlyDue,
                        onClick = { showOnlyDue = true },
                        label = {
                            Text(
                                "${if (isEnglish) "Due Sales" else "বকেয়া বিক্রি"} (${sales.count { it.dueAmount > 0 }})",
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFD97706),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Sales Summary Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = strings.reportTotalRevenue, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${strings.currencySymbol} ${InventoryViewModel.formatAmount(totalRevenue, lang)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = strings.reportTotalDue, fontSize = 11.sp, color = Color(0xFFD97706))
                        Text(
                            text = "${strings.currencySymbol} ${InventoryViewModel.formatAmount(totalDue, lang)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706)
                        )
                    }
                }
            }
        }

        if (filteredSales.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.PointOfSale,
                            contentDescription = "No Sales",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = strings.noSalesRecorded,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        } else {
            items(filteredSales, key = { it.id }) { sale ->
                SaleItemCard(
                    sale = sale,
                    onViewInvoice = { onViewInvoice(sale) },
                    onCollectDue = { onCollectDue(sale) },
                    onEditSale = { onEditSale(sale) },
                    onCallCustomer = {
                        try {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${sale.customerPhone}"))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun SaleItemCard(
    sale: Sale,
    onViewInvoice: () -> Unit,
    onCollectDue: () -> Unit,
    onCallCustomer: () -> Unit,
    onEditSale: () -> Unit = {}
) {
    val strings = com.example.ui.LocalAppStrings.current
    val isEnglish = strings.tabDashboard == "Dashboard"
    val lang = if (isEnglish) "EN" else "BN"
    val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", if (isEnglish) Locale.US else Locale("bn", "BD")).format(Date(sale.soldAt))

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewInvoice() }
            .testTag("sale_card_${sale.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Invoice No & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(AppleBlue.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = AppleBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "#${sale.invoiceNumber}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleBlue
                    )
                }

                Text(
                    text = dateStr,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Customer Info & Phone
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = sale.customerName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${if (isEnglish) "Phone:" else "মোবাইল:"} ${sale.customerPhone}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onCallCustomer,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF10B981).copy(alpha = 0.12f), CircleShape)
                        .testTag("sale_call_button_${sale.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Device Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = sale.productDetails,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "IMEI: ${sale.imei}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Financials: Sale Price, Paid, Due
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = strings.totalSalePriceLabel, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${strings.currencySymbol} ${InventoryViewModel.formatAmount(sale.salePrice, lang)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = strings.paidLabel, fontSize = 10.sp, color = Color(0xFF10B981))
                    Text(
                        text = "${strings.currencySymbol} ${InventoryViewModel.formatAmount(sale.paidAmount, lang)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (sale.dueAmount > 0) strings.dueLabel else (if (isEnglish) "Paid" else "পরিশোধিত"),
                        fontSize = 10.sp,
                        color = if (sale.dueAmount > 0) Color(0xFFD97706) else Color(0xFF10B981)
                    )
                    Text(
                        text = "${strings.currencySymbol} ${InventoryViewModel.formatAmount(sale.dueAmount, lang)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (sale.dueAmount > 0) Color(0xFFD97706) else Color(0xFF10B981)
                    )
                }
            }

            // Actions: Digital Memo & Collect Due
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onViewInvoice,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("view_invoice_button_${sale.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = "Invoice", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isEnglish) "Cash Memo" else "ক্যাশ মেমো", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onEditSale,
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("edit_sale_button_${sale.id}"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.btnEdit, fontSize = 12.sp)
                }

                if (sale.dueAmount > 0) {
                    OutlinedButton(
                        onClick = onCollectDue,
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("collect_due_card_button_${sale.id}"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Payments, contentDescription = "Due", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isEnglish) "Collect Due" else "বকেয়া আদায়", fontSize = 12.sp, color = Color(0xFFD97706))
                    }
                }
            }
        }
    }
}
