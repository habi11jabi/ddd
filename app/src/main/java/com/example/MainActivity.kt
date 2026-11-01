package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.BanglaStrings
import com.example.ui.EnglishStrings
import com.example.ui.LocalAppStrings
import com.example.ui.components.ScannerDialog
import com.example.ui.screens.AddEditProductScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DuePaymentDialog
import com.example.ui.screens.EditSaleDialog
import com.example.ui.screens.InvoiceMemoDialog
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SaleCheckoutDialog
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StockScreen
import com.example.ui.screens.SalesScreen
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppTab
import com.example.viewmodel.InventoryViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: InventoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: InventoryViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val isAddEditOpen by viewModel.isAddEditOpen.collectAsStateWithLifecycle()
    val editingProduct by viewModel.editingProduct.collectAsStateWithLifecycle()
    val productToSell by viewModel.productToSell.collectAsStateWithLifecycle()
    val activeInvoiceSale by viewModel.activeInvoiceSale.collectAsStateWithLifecycle()
    val editingSale by viewModel.editingSale.collectAsStateWithLifecycle()
    val isScannerOpen by viewModel.isScannerOpen.collectAsStateWithLifecycle()
    val saleForDuePayment by viewModel.saleForDuePayment.collectAsStateWithLifecycle()
    val shopSettings by viewModel.shopSettings.collectAsStateWithLifecycle()

    val filteredProducts by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val allSales by viewModel.allSales.collectAsStateWithLifecycle()
    val dashboardMetrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()
    val reportData by viewModel.reportData.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedStatusFilter by viewModel.selectedStatusFilter.collectAsStateWithLifecycle()
    val selectedBrandFilter by viewModel.selectedBrandFilter.collectAsStateWithLifecycle()
    val reportPeriod by viewModel.reportPeriod.collectAsStateWithLifecycle()

    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    val currentStrings = if (shopSettings.appLanguage.equals("EN", ignoreCase = true)) EnglishStrings else BanglaStrings

    CompositionLocalProvider(LocalAppStrings provides currentStrings) {
        // Modal Dialogs
        // 1. Live CameraX Barcode / IMEI Scanner Dialog
        if (isScannerOpen) {
            ScannerDialog(
                onDismiss = { viewModel.closeScanner() },
                onBarcodeScanned = { scannedImei ->
                    viewModel.closeScanner()
                    viewModel.setSearchQuery(scannedImei)
                    viewModel.setTab(AppTab.STOCK)
                }
            )
        }

        // 2. Sale Checkout Dialog
        productToSell?.let { product ->
            SaleCheckoutDialog(
                product = product,
                shopSettings = shopSettings,
                onDismiss = { viewModel.closeSaleCheckout() },
                onConfirmSale = { name, phone, address, salePrice, paidAmount, paymentMethod, warranty, remarks ->
                    viewModel.completeSale(
                        customerName = name,
                        customerPhone = phone,
                        customerAddress = address,
                        salePrice = salePrice,
                        paidAmount = paidAmount,
                        paymentMethod = paymentMethod,
                        warrantyInfo = warranty,
                        remarks = remarks
                    )
                }
            )
        }

        // 3. Digital Cash Memo / Invoice Dialog
        activeInvoiceSale?.let { sale ->
            InvoiceMemoDialog(
                sale = sale,
                shopSettings = shopSettings,
                onDismiss = { viewModel.closeInvoice() },
                onCollectDue = { s ->
                    viewModel.closeInvoice()
                    viewModel.openDuePayment(s)
                },
                onEditSale = { s ->
                    viewModel.closeInvoice()
                    viewModel.openEditSale(s)
                },
                onDeleteSale = { s, restoreStock ->
                    viewModel.deleteSale(s, restoreStock)
                }
            )
        }

        // 4. Edit Sale / Invoice Memo Dialog
        editingSale?.let { sale ->
            EditSaleDialog(
                sale = sale,
                onDismiss = { viewModel.closeEditSale() },
                onSaveSale = { updatedSale -> viewModel.saveEditedSale(updatedSale) },
                onDeleteSale = { s, restoreStock -> viewModel.deleteSale(s, restoreStock) }
            )
        }

        // 5. Due Payment Dialog
        saleForDuePayment?.let { sale ->
            DuePaymentDialog(
                sale = sale,
                onDismiss = { viewModel.closeDuePayment() },
                onPaymentConfirmed = { additionalPaid ->
                    viewModel.recordDuePayment(sale, additionalPaid)
                }
            )
        }

        // If Add/Edit Product is open, show the dedicated full-screen form with BackHandler
        if (isAddEditOpen) {
            BackHandler {
                viewModel.closeAddEdit()
            }
            AddEditProductScreen(
                editingProduct = editingProduct,
                shopSettings = shopSettings,
                onSave = { company, model, variant, color, battery, box, supplier, purchasePrice, targetPrice, imei, notes ->
                    viewModel.saveProduct(
                        company = company,
                        model = model,
                        variant = variant,
                        color = color,
                        batteryHealth = battery,
                        boxCondition = box,
                        supplier = supplier,
                        purchasePrice = purchasePrice,
                        targetSalePrice = targetPrice,
                        imei = imei,
                        notes = notes
                    )
                },
                onSaveBatch = { company, model, variant, color, battery, box, supplier, purchasePrice, targetPrice, imeis, notes ->
                    viewModel.saveBatchProducts(
                        company = company,
                        model = model,
                        variant = variant,
                        color = color,
                        batteryHealth = battery,
                        boxCondition = box,
                        supplier = supplier,
                        purchasePrice = purchasePrice,
                        targetSalePrice = targetPrice,
                        imeis = imeis,
                        notes = notes
                    )
                },
                onBack = { viewModel.closeAddEdit() }
            )
            return@CompositionLocalProvider
        }

        // Normal Tab Navigation BackHandler
        if (currentTab != AppTab.DASHBOARD) {
            BackHandler {
                viewModel.setTab(AppTab.DASHBOARD)
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == AppTab.DASHBOARD,
                        onClick = { viewModel.setTab(AppTab.DASHBOARD) },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text(currentStrings.tabDashboard, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AppleBlue,
                            indicatorColor = AppleBlue.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("tab_dashboard")
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.STOCK,
                        onClick = { viewModel.setTab(AppTab.STOCK) },
                        icon = { Icon(Icons.Default.Inventory2, contentDescription = "Stock") },
                        label = { Text(currentStrings.tabStock, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AppleBlue,
                            indicatorColor = AppleBlue.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("tab_stock")
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.SALES,
                        onClick = { viewModel.setTab(AppTab.SALES) },
                        icon = { Icon(Icons.Default.PointOfSale, contentDescription = "Sales") },
                        label = { Text(currentStrings.tabSales, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AppleBlue,
                            indicatorColor = AppleBlue.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("tab_sales")
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.REPORTS,
                        onClick = { viewModel.setTab(AppTab.REPORTS) },
                        icon = { Icon(Icons.Default.Assessment, contentDescription = "Reports") },
                        label = { Text(currentStrings.tabReports, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AppleBlue,
                            indicatorColor = AppleBlue.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("tab_reports")
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.SETTINGS,
                        onClick = { viewModel.setTab(AppTab.SETTINGS) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text(currentStrings.tabSettings, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AppleBlue,
                            indicatorColor = AppleBlue.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("tab_settings")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    AppTab.DASHBOARD -> DashboardScreen(
                        metrics = dashboardMetrics,
                        products = filteredProducts,
                        searchQuery = searchQuery,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onAddProductClick = { viewModel.openAddProduct() },
                        onOpenScanner = { viewModel.openScanner() },
                        onNavigateTab = { viewModel.setTab(it) },
                        onSellProduct = { viewModel.openSaleCheckout(it) },
                        onEditProduct = { viewModel.openEditProduct(it) },
                        onDeleteProduct = { viewModel.deleteProduct(it) }
                    )
                    AppTab.STOCK -> StockScreen(
                        products = filteredProducts,
                        searchQuery = searchQuery,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        selectedBrandFilter = selectedBrandFilter,
                        onBrandFilterChange = { viewModel.setBrandFilter(it) },
                        onAddProductClick = { viewModel.openAddProduct() },
                        onOpenScanner = { viewModel.openScanner() },
                        onSellProduct = { viewModel.openSaleCheckout(it) },
                        onEditProduct = { viewModel.openEditProduct(it) },
                        onDeleteProduct = { viewModel.deleteProduct(it) }
                    )
                    AppTab.SALES -> SalesScreen(
                        sales = allSales,
                        onViewInvoice = { viewModel.openInvoice(it) },
                        onCollectDue = { viewModel.openDuePayment(it) },
                        onEditSale = { viewModel.openEditSale(it) }
                    )
                    AppTab.REPORTS -> ReportsScreen(
                        report = reportData,
                        metrics = dashboardMetrics,
                        selectedPeriod = reportPeriod,
                        onPeriodSelect = { viewModel.setReportPeriod(it) }
                    )
                    AppTab.SETTINGS -> SettingsScreen(
                        currentSettings = shopSettings,
                        onSaveSettings = { viewModel.updateShopSettings(it) },
                        onResetDefaults = { viewModel.resetShopSettings() },
                        onClearAllData = { viewModel.clearAllDatabaseData() },
                        onLoadDemoData = { viewModel.loadDemoData() }
                    )
                }
            }
        }
    }
}
