package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import com.example.util.PdfInvoiceGenerator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import com.example.data.model.Sale
import com.example.data.model.ShopSettings
import com.example.ui.theme.AppleBlue
import com.example.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InvoiceMemoDialog(
    sale: Sale,
    shopSettings: ShopSettings = ShopSettings(),
    onDismiss: () -> Unit,
    onCollectDue: (Sale) -> Unit,
    onEditSale: (Sale) -> Unit = {},
    onDeleteSale: (Sale, Boolean) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val strings = com.example.ui.LocalAppStrings.current
    val isEnglish = strings.tabDashboard == "Dashboard"
    val lang = if (isEnglish) "EN" else "BN"
    val dateFormat = SimpleDateFormat("dd MMMM, yyyy - hh:mm a", if (isEnglish) Locale.US else Locale("bn", "BD"))
    val formattedDate = dateFormat.format(Date(sale.soldAt))

    fun shareInvoiceText() {
        val invoiceText = """
══════════════════════════════
        ${shopSettings.shopName}
    ${shopSettings.shopTagline}
══════════════════════════════
${strings.invoiceNoLabel}: #${sale.invoiceNumber}
${strings.dateLabel}: $formattedDate

[${strings.customerInfoSection}]
${if (isEnglish) "Name:" else "নাম:"} ${sale.customerName}
${if (isEnglish) "Phone:" else "মোবাইল:"} ${sale.customerPhone}
${if (sale.customerAddress.isNotBlank()) "${if (isEnglish) "Address:" else "ঠিকানা:"} ${sale.customerAddress}" else ""}

[${strings.productDetailsSection}]
${if (isEnglish) "Device:" else "প্রোডাক্ট:"} ${sale.productDetails}
${strings.imeiLabel} ${sale.imei}
${if (sale.batteryHealth.isNotBlank()) "${strings.batteryLabel} ${sale.batteryHealth}" else ""}
${if (sale.boxCondition.isNotBlank()) "${strings.boxLabel} ${sale.boxCondition}" else ""}

[${strings.paymentSummarySection}]
${strings.totalSalePriceLabel} ${strings.currencySymbol} ${InventoryViewModel.formatAmount(sale.salePrice, lang)}
${strings.paidLabel} ${strings.currencySymbol} ${InventoryViewModel.formatAmount(sale.paidAmount, lang)}
${strings.dueLabel} ${strings.currencySymbol} ${InventoryViewModel.formatAmount(sale.dueAmount, lang)}
${strings.paymentMethodLabel}: ${sale.paymentMethod}

[${strings.warrantySection}]
${sale.warrantyInfo}

${if (isEnglish) "Thank you for choosing us!" else "ধন্যবাদ! আবার আসবেন।"}
══════════════════════════════
${shopSettings.shopName} • ${shopSettings.shopPhone}
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "${strings.invoiceTitle} - ${sale.invoiceNumber} - ${shopSettings.shopName}")
            putExtra(Intent.EXTRA_TEXT, invoiceText)
        }
        context.startActivity(Intent.createChooser(intent, strings.btnShareMemo))
    }

    fun callCustomer() {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${sale.customerPhone}")
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(AppleBlue.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = "Receipt",
                                tint = AppleBlue
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ডিজিটাল ক্যাশ মেমো",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onEditSale(sale) },
                            modifier = Modifier.testTag("edit_memo_button")
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Memo", tint = AppleBlue)
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_invoice_dialog_button")
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                // Printable Cash Memo Paper
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (MaterialTheme.colorScheme.background == Color(0xFF0D1117)) Color(0xFF1E2530) else Color(0xFFFBFDFF)
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Shop Header
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.shop_logo),
                                contentDescription = "Apple Area BD Logo",
                                modifier = Modifier
                                    .height(52.dp)
                                    .width(88.dp)
                                    .padding(bottom = 6.dp)
                            )
                            Text(
                                text = shopSettings.shopName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AppleBlue,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = shopSettings.shopTagline,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${strings.invoiceNoLabel}: #${sale.invoiceNumber}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Text(
                                text = formattedDate,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        // Customer Details
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = strings.customerInfoSection,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppleBlue
                            )
                            Text(
                                text = sale.customerName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${if (isEnglish) "Phone:" else "মোবাইল:"} ${sale.customerPhone}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (sale.customerAddress.isNotBlank()) {
                                Text(
                                    text = "${if (isEnglish) "Address:" else "ঠিকানা:"} ${sale.customerAddress}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        // Product Details
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = strings.productDetailsSection,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppleBlue
                            )
                            Text(
                                text = sale.productDetails,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${strings.imeiLabel} ${sale.imei}",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (sale.batteryHealth.isNotBlank()) {
                                    Text(
                                        text = "${strings.batteryLabel} ${sale.batteryHealth}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF10B981)
                                    )
                                }
                                if (sale.boxCondition.isNotBlank()) {
                                    Text(
                                        text = "${strings.boxLabel} ${sale.boxCondition}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        // Price and Payment
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = strings.totalSalePriceLabel, fontSize = 13.sp)
                                Text(
                                    text = "${strings.currencySymbol} ${InventoryViewModel.formatAmount(sale.salePrice, lang)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = strings.paidLabel, fontSize = 13.sp)
                                Text(
                                    text = "${strings.currencySymbol} ${InventoryViewModel.formatAmount(sale.paidAmount, lang)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = strings.dueLabel,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (sale.dueAmount > 0) Color(0xFFD97706) else Color(0xFF10B981)
                                )
                                Text(
                                    text = "${strings.currencySymbol} ${InventoryViewModel.formatAmount(sale.dueAmount, lang)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (sale.dueAmount > 0) Color(0xFFD97706) else Color(0xFF10B981)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "${strings.paymentMethodLabel}:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = sale.paymentMethod, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        // Warranty Badge with Shop Logo
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppleBlue.copy(alpha = 0.08f))
                                .border(1.dp, AppleBlue.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Image(
                                            painter = painterResource(id = R.drawable.shop_logo),
                                            contentDescription = "Shop Logo",
                                            modifier = Modifier
                                                .height(28.dp)
                                                .width(46.dp)
                                                .padding(end = 6.dp)
                                        )
                                        Text(
                                            text = strings.warrantySection,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AppleBlue
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "Verified Warranty",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF047857),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = sale.warrantyInfo.ifBlank { shopSettings.fullWarrantyText },
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        // Store Stamp
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = strings.memoDisclaimer,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .border(1.5.dp, AppleBlue, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified Seal",
                                        tint = AppleBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Text(
                                    text = "${shopSettings.shopName}\nAuthorized",
                                    fontSize = 8.sp,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    color = AppleBlue
                                )
                            }
                        }
                    }
                }

                // Interactive Action Buttons
                // Row 1: PDF Download & Direct Print
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            PdfInvoiceGenerator.savePdfToDownloads(context, sale, shopSettings)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("download_pdf_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = "Download PDF")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.btnDownloadPdf, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            PdfInvoiceGenerator.printInvoice(context, sale, shopSettings)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("print_invoice_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = "Print Invoice")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.btnPrint, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Row 2: Share Memo & Call Customer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { shareInvoiceText() },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("share_invoice_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.btnShareMemo, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = { callCustomer() },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("call_customer_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.btnCallCustomer, fontSize = 12.sp)
                    }
                }

                // Edit Memo Button
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onEditSale(sale)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("dialog_edit_memo_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Memo", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.btnEditMemo, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                if (sale.dueAmount > 0) {
                    Button(
                        onClick = { onCollectDue(sale) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("collect_due_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Payments, contentDescription = "Collect Due")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${strings.btnCollectDue} (${strings.currencySymbol} ${InventoryViewModel.formatAmount(sale.dueAmount, lang)})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
