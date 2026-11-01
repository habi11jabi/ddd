package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppleBlue
import com.example.viewmodel.DashboardMetrics
import com.example.viewmodel.InventoryViewModel
import com.example.viewmodel.MonthlyReport
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(
    report: MonthlyReport,
    metrics: DashboardMetrics,
    selectedPeriod: String,
    onPeriodSelect: (String) -> Unit
) {
    val context = LocalContext.current
    val strings = com.example.ui.LocalAppStrings.current
    val isEnglish = strings.tabDashboard == "Dashboard"
    val lang = if (isEnglish) "EN" else "BN"
    val currentDateStr = SimpleDateFormat("dd MMMM, yyyy", if (isEnglish) Locale.US else Locale("bn", "BD")).format(Date())

    fun shareReportText() {
        val summaryText = """
📊 ${strings.appTitle} • ${strings.reportsTitle}
${if (isEnglish) "Date:" else "তারিখ:"} $currentDateStr
${if (isEnglish) "Period:" else "সময়কাল:"} ${report.periodName}
════════════════════════════════════
📱 ${strings.reportUnitsSold}: ${report.unitsSold}
💰 ${strings.reportTotalRevenue}: ${strings.currencySymbol} ${InventoryViewModel.formatAmount(report.totalRevenue, lang)}
📦 ${strings.reportTotalCost}: ${strings.currencySymbol} ${InventoryViewModel.formatAmount(report.totalCost, lang)}
📈 ${strings.reportTotalProfit}: ${strings.currencySymbol} ${InventoryViewModel.formatAmount(report.totalProfit, lang)}
🎯 ${strings.reportProfitMargin}: ${String.format("%.1f", report.profitMarginPercent)}%

💵 ${strings.reportTotalPaid}: ${strings.currencySymbol} ${InventoryViewModel.formatAmount(report.totalPaid, lang)}
⚠️ ${strings.reportTotalDue}: ${strings.currencySymbol} ${InventoryViewModel.formatAmount(report.totalDue, lang)}

[${if (isEnglish) "Current Shop Stock Status" else "দোকানের বর্তমান মোট ইনভেন্টরি অবস্থা"}]
📦 ${if (isEnglish) "In-stock phones:" else "স্টকে থাকা ফোন:"} ${metrics.inStockCount}
💎 ${if (isEnglish) "Total Stock Investment:" else "বর্তমান স্টক ইনভেস্টমেন্ট মূল্য:"} ${strings.currencySymbol} ${InventoryViewModel.formatAmount(metrics.totalStockValue, lang)}
════════════════════════════════════
${strings.appTitle}
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "${strings.appTitle} - ${strings.reportsTitle}")
            putExtra(Intent.EXTRA_TEXT, summaryText)
        }
        context.startActivity(Intent.createChooser(intent, if (isEnglish) "Share Report" else "রিপোর্ট শেয়ার করুন"))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Screen Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = strings.reportsTitle,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${strings.appTitle} • ${if (isEnglish) "Sales & Profit Analytics" else "প্রফিট ও বিক্রয় অ্যানালিটিক্স"}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(AppleBlue.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = "Reports",
                            tint = AppleBlue
                        )
                    }
                }
            }
        }

        // Period Filters
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedPeriod == "THIS_MONTH",
                    onClick = { onPeriodSelect("THIS_MONTH") },
                    label = { Text(strings.periodThisMonth, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppleBlue,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("report_filter_this_month")
                )

                FilterChip(
                    selected = selectedPeriod == "LAST_MONTH",
                    onClick = { onPeriodSelect("LAST_MONTH") },
                    label = { Text(strings.periodLastMonth, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppleBlue,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("report_filter_last_month")
                )

                FilterChip(
                    selected = selectedPeriod == "ALL_TIME",
                    onClick = { onPeriodSelect("ALL_TIME") },
                    label = { Text(strings.periodAllTime, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppleBlue,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("report_filter_all_time")
                )
            }
        }

        // Net Profit Hero Banner
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (report.totalProfit >= 0) Color(0xFF065F46) else Color(0xFF991B1B)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = report.periodName,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.SemiBold
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "মার্জিন: ${String.format("%.1f", report.profitMarginPercent)}%",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        text = "মোট নিট লাভ (Net Profit)",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    Text(
                        text = "৳ ${InventoryViewModel.formatBdt(report.totalProfit)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Text(
                        text = "মোট বিক্রয়: ৳ ${InventoryViewModel.formatBdt(report.totalRevenue)}  •  কেনার খরচ: ৳ ${InventoryViewModel.formatBdt(report.totalCost)}",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Accounting Details Grid
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isEnglish) "Financial Statement Overview:" else "আর্থিক সারসংক্ষেপ বিবরণী:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    ReportRow(
                        label = "${strings.reportUnitsSold}:",
                        value = "${report.unitsSold} ${if (isEnglish) "units" else "টি"}",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    HorizontalDivider()

                    ReportRow(
                        label = "${strings.reportTotalRevenue}:",
                        value = "${strings.currencySymbol} ${InventoryViewModel.formatAmount(report.totalRevenue, lang)}",
                        color = AppleBlue
                    )
                    HorizontalDivider()

                    ReportRow(
                        label = "${strings.reportTotalCost}:",
                        value = "${strings.currencySymbol} ${InventoryViewModel.formatAmount(report.totalCost, lang)}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider()

                    ReportRow(
                        label = "${strings.reportTotalPaid}:",
                        value = "${strings.currencySymbol} ${InventoryViewModel.formatAmount(report.totalPaid, lang)}",
                        color = Color(0xFF10B981)
                    )
                    HorizontalDivider()

                    ReportRow(
                        label = "${strings.reportTotalDue}:",
                        value = "${strings.currencySymbol} ${InventoryViewModel.formatAmount(report.totalDue, lang)}",
                        color = if (report.totalDue > 0) Color(0xFFD97706) else Color(0xFF10B981),
                        isBold = true
                    )
                }
            }
        }

        // Current Inventory Valuation Snapshot
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEnglish) "Current Inventory Valuation:" else "বর্তমান দোকানের স্টক স্ট্যাটাস:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = AppleBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = if (isEnglish) "In-stock phones:" else "বর্তমানে অবিক্রিত ফোন:", fontSize = 12.sp)
                        Text(text = "${metrics.inStockCount} ${if (isEnglish) "units" else "টি"}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = if (isEnglish) "Current Stock Investment Value:" else "স্টকে বর্তমান আটকে থাকা ইনভেস্টমেন্ট:", fontSize = 12.sp)
                        Text(
                            text = "${strings.currencySymbol} ${InventoryViewModel.formatAmount(metrics.totalStockValue, lang)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppleBlue
                        )
                    }
                }
            }
        }

        // Popular Selling Models
        if (report.topSellingModels.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoGraph,
                                contentDescription = null,
                                tint = AppleBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.topSellingModelsHeader,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        report.topSellingModels.forEachIndexed { index, (modelName, count) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "${index + 1}. ",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppleBlue
                                    )
                                    Text(
                                        text = modelName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AppleBlue.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = "$count ${if (isEnglish) "sold" else "টি বিক্রিত"}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppleBlue,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Export / Share Report Button
        item {
            Button(
                onClick = { shareReportText() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("share_report_button"),
                colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEnglish) "Share / Copy Monthly Report" else "মাসিক রিপোর্ট শেয়ার / কপি করুন",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ReportRow(
    label: String,
    value: String,
    color: Color,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Bold,
            color = color
        )
    }
}
