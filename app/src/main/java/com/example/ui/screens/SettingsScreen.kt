package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import com.example.ui.LocalAppStrings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ShopSettings
import com.example.ui.theme.AppleBlue

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    currentSettings: ShopSettings,
    onSaveSettings: (ShopSettings) -> Unit,
    onResetDefaults: () -> Unit,
    onClearAllData: () -> Unit = {},
    onLoadDemoData: () -> Unit = {}
) {
    val strings = LocalAppStrings.current
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showLoadDemoConfirmDialog by remember { mutableStateOf(false) }

    var selectedLanguage by remember(currentSettings) { mutableStateOf(currentSettings.appLanguage) }
    var shopName by remember(currentSettings) { mutableStateOf(currentSettings.shopName) }
    var shopTagline by remember(currentSettings) { mutableStateOf(currentSettings.shopTagline) }
    var shopPhone by remember(currentSettings) { mutableStateOf(currentSettings.shopPhone) }
    var shopAddress by remember(currentSettings) { mutableStateOf(currentSettings.shopAddress) }

    var warrantyReplacement by remember(currentSettings) { mutableStateOf(currentSettings.warrantyReplacement) }
    var warrantyService by remember(currentSettings) { mutableStateOf(currentSettings.warrantyService) }
    var warrantyExchangeDeduction by remember(currentSettings) { mutableStateOf(currentSettings.warrantyExchangeDeduction) }

    var companyList by remember(currentSettings) { mutableStateOf(currentSettings.companyPresets) }
    var newCompanyInput by remember { mutableStateOf("") }

    var variantList by remember(currentSettings) { mutableStateOf(currentSettings.variantPresets) }
    var newVariantInput by remember { mutableStateOf("") }

    var colorList by remember(currentSettings) { mutableStateOf(currentSettings.colorPresets) }
    var newColorInput by remember { mutableStateOf("") }

    var boxList by remember(currentSettings) { mutableStateOf(currentSettings.boxConditionPresets) }
    var newBoxInput by remember { mutableStateOf("") }

    var supplierList by remember(currentSettings) { mutableStateOf(currentSettings.supplierPresets) }
    var newSupplierInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Screen Header Card
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.shop_logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .height(46.dp)
                                .width(76.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = strings.settingsTitle,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = strings.settingsSubtitle,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(AppleBlue.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = AppleBlue)
                    }
                }
            }
        }

        // Section: Language Selection (English / বাংলা)
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = AppleBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = strings.languageSectionTitle, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = strings.languageSectionSubtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Bangla Button
                        Button(
                            onClick = {
                                selectedLanguage = "BN"
                                onSaveSettings(currentSettings.copy(appLanguage = "BN"))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("select_language_bn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedLanguage == "BN") AppleBlue else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (selectedLanguage == "BN") Color.White else MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("বাংলা (Bangla)", fontSize = 13.sp, fontWeight = if (selectedLanguage == "BN") FontWeight.Bold else FontWeight.Normal)
                        }

                        // English Button
                        Button(
                            onClick = {
                                selectedLanguage = "EN"
                                onSaveSettings(currentSettings.copy(appLanguage = "EN"))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("select_language_en"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedLanguage == "EN") AppleBlue else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (selectedLanguage == "EN") Color.White else MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("English", fontSize = 13.sp, fontWeight = if (selectedLanguage == "EN") FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }
        }

        // Section 1: Shop Profile Info
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Storefront, contentDescription = null, tint = AppleBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = strings.shopProfileSection, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        label = { Text(strings.shopNameLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = shopTagline,
                        onValueChange = { shopTagline = it },
                        label = { Text(strings.shopTaglineLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = shopPhone,
                            onValueChange = { shopPhone = it },
                            label = { Text(strings.shopPhoneLabel) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = shopAddress,
                            onValueChange = { shopAddress = it },
                            label = { Text(strings.shopAddressLabel) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        // Section 2: Warranty Card & Cash Memo Policy Customization
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF10B981))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = strings.warrantyCustomizationSection, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = strings.warrantyDesc,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 1. Replacement policy
                    OutlinedTextField(
                        value = warrantyReplacement,
                        onValueChange = { warrantyReplacement = it },
                        label = { Text(strings.warrantyReplacementLabel) },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // 2. Service warranty
                    OutlinedTextField(
                        value = warrantyService,
                        onValueChange = { warrantyService = it },
                        label = { Text(strings.warrantyServiceLabel) },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // 3. Exchange / Deduction policy
                    OutlinedTextField(
                        value = warrantyExchangeDeduction,
                        onValueChange = { warrantyExchangeDeduction = it },
                        label = { Text(strings.warrantyExchangeLabel) },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Live Preview box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = AppleBlue.copy(alpha = 0.08f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = strings.warrantyLivePreview, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppleBlue)
                            Text(text = "• $warrantyReplacement", fontSize = 11.sp)
                            Text(text = "• $warrantyService", fontSize = 11.sp)
                            Text(text = "• $warrantyExchangeDeduction", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Section 3: Customize Input Windows & Selection Boxes (Presets)
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Widgets, contentDescription = null, tint = AppleBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "৩. ইনপুট উইন্ডো ও বক্স অপশন পরিবর্তন", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    // Company presets
                    PresetChipEditor(
                        title = "কোম্পানি / ব্র্যান্ড অপশন",
                        items = companyList,
                        onRemove = { item -> companyList = companyList.filter { it != item } },
                        newInputValue = newCompanyInput,
                        onNewInputChange = { newCompanyInput = it },
                        onAdd = {
                            if (newCompanyInput.isNotBlank()) {
                                companyList = companyList + newCompanyInput.trim()
                                newCompanyInput = ""
                            }
                        }
                    )
                    HorizontalDivider()

                    // Variant presets
                    PresetChipEditor(
                        title = "ভ্যারিয়েন্ট / স্টোরেজ অপশন",
                        items = variantList,
                        onRemove = { item -> variantList = variantList.filter { it != item } },
                        newInputValue = newVariantInput,
                        onNewInputChange = { newVariantInput = it },
                        onAdd = {
                            if (newVariantInput.isNotBlank()) {
                                variantList = variantList + newVariantInput.trim()
                                newVariantInput = ""
                            }
                        }
                    )
                    HorizontalDivider()

                    // Color presets
                    PresetChipEditor(
                        title = "কালার অপশন",
                        items = colorList,
                        onRemove = { item -> colorList = colorList.filter { it != item } },
                        newInputValue = newColorInput,
                        onNewInputChange = { newColorInput = it },
                        onAdd = {
                            if (newColorInput.isNotBlank()) {
                                colorList = colorList + newColorInput.trim()
                                newColorInput = ""
                            }
                        }
                    )
                    HorizontalDivider()

                    // Box condition presets
                    PresetChipEditor(
                        title = "বক্সের অবস্থা অপশন",
                        items = boxList,
                        onRemove = { item -> boxList = boxList.filter { it != item } },
                        newInputValue = newBoxInput,
                        onNewInputChange = { newBoxInput = it },
                        onAdd = {
                            if (newBoxInput.isNotBlank()) {
                                boxList = boxList + newBoxInput.trim()
                                newBoxInput = ""
                            }
                        }
                    )
                    HorizontalDivider()

                    // Supplier presets
                    PresetChipEditor(
                        title = "সাপ্লায়ার অপশন",
                        items = supplierList,
                        onRemove = { item -> supplierList = supplierList.filter { it != item } },
                        newInputValue = newSupplierInput,
                        onNewInputChange = { newSupplierInput = it },
                        onAdd = {
                            if (newSupplierInput.isNotBlank()) {
                                supplierList = supplierList + newSupplierInput.trim()
                                newSupplierInput = ""
                            }
                        }
                    )
                }
            }
        }

        // Section 4: Data Management & Reset
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, tint = Color(0xFFEF4444))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = strings.dataManagementSection, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = strings.dataManagementSubtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showLoadDemoConfirmDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("load_demo_data_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.btnLoadDemoData, fontSize = 11.sp)
                        }

                        Button(
                            onClick = { showClearConfirmDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("clear_all_data_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.btnClearAllData, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Action Buttons: Save Settings & Reset
        item {
            Button(
                onClick = {
                    val updated = currentSettings.copy(
                        appLanguage = selectedLanguage,
                        shopName = shopName.trim(),
                        shopTagline = shopTagline.trim(),
                        shopPhone = shopPhone.trim(),
                        shopAddress = shopAddress.trim(),
                        warrantyReplacement = warrantyReplacement.trim(),
                        warrantyService = warrantyService.trim(),
                        warrantyExchangeDeduction = warrantyExchangeDeduction.trim(),
                        companyPresets = companyList,
                        variantPresets = variantList,
                        colorPresets = colorList,
                        boxConditionPresets = boxList,
                        supplierPresets = supplierList
                    )
                    onSaveSettings(updated)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_settings_button"),
                colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = "Save")
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.btnSaveSettings, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onResetDefaults,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("reset_defaults_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.RestartAlt, contentDescription = "Reset")
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.btnResetDefaults, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444)) },
            title = { Text(strings.confirmClearTitle) },
            text = { Text(strings.confirmClearText) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirmDialog = false
                        onClearAllData()
                    }
                ) {
                    Text(strings.delete, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    if (showLoadDemoConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLoadDemoConfirmDialog = false },
            icon = { Icon(Icons.Default.CloudDownload, contentDescription = null, tint = AppleBlue) },
            title = { Text(strings.confirmLoadDemoTitle) },
            text = { Text(strings.confirmLoadDemoText) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLoadDemoConfirmDialog = false
                        onLoadDemoData()
                    }
                ) {
                    Text(strings.confirm, color = AppleBlue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLoadDemoConfirmDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PresetChipEditor(
    title: String,
    items: List<String>,
    onRemove: (String) -> Unit,
    newInputValue: String,
    onNewInputChange: (String) -> Unit,
    onAdd: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items.forEach { item ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(start = 8.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = item, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { onRemove(item) },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newInputValue,
                onValueChange = onNewInputChange,
                placeholder = { Text("নতুন $title লিখুন...", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            )
            Button(
                onClick = onAdd,
                colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                Text("যোগ", fontSize = 11.sp)
            }
        }
    }
}
