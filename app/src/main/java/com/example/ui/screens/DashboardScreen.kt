package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.example.R
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.data.model.ProductStatus
import com.example.ui.LocalAppStrings
import com.example.ui.theme.AppleBlue
import com.example.viewmodel.AppTab
import com.example.viewmodel.DashboardMetrics
import com.example.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    metrics: DashboardMetrics,
    products: List<Product>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    shopSettings: com.example.data.model.ShopSettings = com.example.data.model.ShopSettings(),
    onAddProductClick: () -> Unit,
    onOpenScanner: () -> Unit,
    onNavigateTab: (AppTab) -> Unit,
    onSellProduct: (Product) -> Unit,
    onEditProduct: (Product) -> Unit,
    onDeleteProduct: (Product) -> Unit
) {
    val strings = LocalAppStrings.current
    val isEnglish = strings.tabDashboard == "Dashboard"
    val dateLocale = if (isEnglish) Locale.US else Locale("bn", "BD")
    val currentDateStr = SimpleDateFormat("EEEE, dd MMMM yyyy", dateLocale).format(Date())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Shop Branding & Status Banner
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.shop_logo),
                            contentDescription = "Shop App Logo",
                            modifier = Modifier
                                .height(48.dp)
                                .width(80.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = shopSettings.shopName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "${shopSettings.shopTagline} • $currentDateStr",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 100% Offline Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(Color(0xFF10B981), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isEnglish) "Offline Ready" else "অফলাইন রেডি",
                                color = Color(0xFF059669),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Quick Stats KPI Cards (2x2 Grid)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = strings.metricInStock,
                        value = "${metrics.inStockCount} ${if (isEnglish) "Units" else "টি"}",
                        subtitle = "${strings.purchaseCostLabel} ${strings.currencySymbol} ${InventoryViewModel.formatAmount(metrics.totalStockValue, if (isEnglish) "EN" else "BN")}",
                        icon = Icons.Default.Inventory2,
                        accentColor = AppleBlue,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = strings.metricRevenue,
                        value = "${metrics.totalSalesCount} ${if (isEnglish) "Sold" else "টি"}",
                        subtitle = "${strings.currencySymbol} ${InventoryViewModel.formatAmount(metrics.totalRevenue, if (isEnglish) "EN" else "BN")}",
                        icon = Icons.Default.PointOfSale,
                        accentColor = Color(0xFF8B5CF6),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = strings.metricProfit,
                        value = "${strings.currencySymbol} ${InventoryViewModel.formatAmount(metrics.totalProfit, if (isEnglish) "EN" else "BN")}",
                        subtitle = if (isEnglish) "Revenue - Cost" else "বিক্রি - কেনা খরচ",
                        icon = Icons.Default.TrendingUp,
                        accentColor = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = strings.metricDue,
                        value = "${strings.currencySymbol} ${InventoryViewModel.formatAmount(metrics.totalDue, if (isEnglish) "EN" else "BN")}",
                        subtitle = if (metrics.totalDue > 0) (if (isEnglish) "Pending payment" else "আদায় বাকি আছে") else (if (isEnglish) "No due balance" else "কোনো বকেয়া নেই"),
                        icon = Icons.Default.LocalAtm,
                        accentColor = if (metrics.totalDue > 0) Color(0xFFF59E0B) else Color(0xFF64748B),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Fast Action Buttons Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAddProductClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("dashboard_add_product_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.actionAddStock, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onOpenScanner,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("dashboard_scan_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = "Scan", tint = AppleBlue)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.actionScanBarcode, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Search Bar in Dashboard
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text(strings.searchPlaceholder, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = AppleBlue)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        // In-Stock Header & count
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${strings.recentStockHeader} (${products.count { it.status == ProductStatus.IN_STOCK.name }})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${strings.viewAll} >",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppleBlue,
                    modifier = Modifier
                        .clickable { onNavigateTab(AppTab.STOCK) }
                        .padding(4.dp)
                        .testTag("see_all_stock_link")
                )
            }
        }

        // Product Cards
        val displayedProducts = products.filter { it.status == ProductStatus.IN_STOCK.name }.take(6)
        if (displayedProducts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = "Empty",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = strings.noProducts,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(displayedProducts, key = { it.id }) { product ->
                ProductItemCard(
                    product = product,
                    onSellClick = { onSellProduct(product) },
                    onEditClick = { onEditProduct(product) },
                    onDeleteClick = { onDeleteProduct(product) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(accentColor.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ProductItemCard(
    product: Product,
    onSellClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val strings = com.example.ui.LocalAppStrings.current
    val isEnglish = strings.tabDashboard == "Dashboard"
    val lang = if (isEnglish) "EN" else "BN"

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Brand, Model, Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = product.company,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppleBlue
                        )
                        Text(
                            text = " • ${product.variant}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = product.model,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (product.status == ProductStatus.IN_STOCK.name) Color(0xFFD1FAE5) else Color(0xFFE2E8F0)
                ) {
                    Text(
                        text = if (product.status == ProductStatus.IN_STOCK.name) strings.statusInStock else strings.statusSold,
                        color = if (product.status == ProductStatus.IN_STOCK.name) Color(0xFF065F46) else Color(0xFF475569),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Tags row (Color, Battery Health, Box Condition, Supplier)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BadgeChip(label = product.color, bgColor = MaterialTheme.colorScheme.surfaceVariant)
                BadgeChip(label = "🔋 ${product.batteryHealth}", bgColor = Color(0xFF10B981).copy(alpha = 0.1f), textColor = Color(0xFF047857))
                BadgeChip(label = "📦 ${product.boxCondition}", bgColor = MaterialTheme.colorScheme.surfaceVariant)
            }

            // IMEI & Supplier
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${strings.imeiLabel} ${product.imei}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${strings.supplierLabel} ${product.supplier}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Prices & Profit
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = strings.purchaseCostLabel, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${strings.currencySymbol} ${InventoryViewModel.formatAmount(product.purchasePrice, lang)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = strings.targetPriceLabel, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${strings.currencySymbol} ${InventoryViewModel.formatAmount(product.targetSalePrice, lang)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppleBlue
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    val profit = product.targetSalePrice - product.purchasePrice
                    Text(text = strings.potentialProfitLabel, fontSize = 10.sp, color = Color(0xFF059669))
                    Text(
                        text = "+${strings.currencySymbol} ${InventoryViewModel.formatAmount(profit, lang)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF059669)
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (product.status == ProductStatus.IN_STOCK.name) {
                    Button(
                        onClick = onSellClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("sell_product_button_${product.id}"),
                        colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PointOfSale, contentDescription = "Sell", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.btnSell, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .height(40.dp)
                        .testTag("edit_product_button_${product.id}"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.btnEdit, fontSize = 12.sp)
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.testTag("delete_product_button_${product.id}")
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                }
            }
        }
    }
}

@Composable
fun BadgeChip(
    label: String,
    bgColor: Color,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            maxLines = 1
        )
    }
}
