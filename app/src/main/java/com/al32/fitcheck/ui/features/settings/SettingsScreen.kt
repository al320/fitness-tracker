package com.al32.fitcheck.ui.features.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.al32.fitcheck.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val clipboardManager = LocalClipboardManager.current
    val preferences by viewModel.userPreferences.collectAsState()
    
    var showExportDialog by remember { mutableStateOf(false) }
    var exportJson by remember { mutableStateOf("") }
    var importJson by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SETTINGS", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SettingsCategory("UNIT PREFERENCE")
                SettingsToggle(
                    label = "Weight Unit", 
                    value = preferences.weightUnit,
                    checked = preferences.weightUnit == "LBS", 
                    onToggle = { viewModel.updateWeightUnit(if (preferences.weightUnit == "KG") "LBS" else "KG") }
                )
            }
            
            item {
                SettingsCategory("TRAINING SYSTEM")
                SettingsItem("Default Rest Timer", "${preferences.defaultRestSeconds} SECONDS")
                SettingsToggle(
                    label = "Haptic Feedback", 
                    value = if (preferences.hapticEnabled) "ENABLED" else "DISABLED",
                    checked = preferences.hapticEnabled,
                    onToggle = { viewModel.updateHapticEnabled(!preferences.hapticEnabled) }
                )
            }

            item {
                SettingsCategory("DATA OWNERSHIP")
                SettingsItem(
                    label = "Export History (JSON)", 
                    value = "SECURE EXPORT", 
                    icon = Icons.Default.Backup,
                    onClick = {
                        viewModel.exportData { json ->
                            exportJson = json
                            showExportDialog = true
                        }
                    }
                )
                SettingsItem(
                    label = "Import Backup", 
                    value = "RESTORE DATA", 
                    icon = Icons.Default.Backup,
                    onClick = { showImportDialog = true }
                )
            }

            item {
                SettingsCategory("SYSTEM")
                SettingsItem("App Version", "1.0.0 (RC1)", Icons.Default.Info)
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            confirmButton = {
                Button(onClick = { 
                    clipboardManager.setText(AnnotatedString(exportJson))
                    showExportDialog = false
                }) {
                    Text("COPY TO CLIPBOARD")
                }
            },
            title = { Text("EXPORT COMPLETE") },
            text = { Text("Your training history has been serialized to JSON. Copy and save it securely.") }
        )
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            confirmButton = {
                Button(onClick = { 
                    viewModel.importData(
                        importJson, 
                        onComplete = { showImportDialog = false },
                        onError = { errorMessage = it }
                    )
                }) {
                    Text("RESTORE")
                }
            },
            title = { Text("IMPORT BACKUP") },
            text = {
                Column {
                    if (errorMessage != null) {
                        Text(errorMessage!!, color = Color.Red, style = MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.height(8.dp))
                    }
                    OutlinedTextField(
                        value = importJson,
                        onValueChange = { importJson = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Paste JSON here") }
                    )
                }
            }
        )
    }
}

@Composable
fun SettingsCategory(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
    )
}

@Composable
fun SettingsItem(
    label: String, 
    value: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(20.dp)) {
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyMedium)
                Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
            }
            if (icon != null) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun SettingsToggle(label: String, value: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyMedium)
                Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
            }
            Switch(checked = checked, onCheckedChange = onToggle)
        }
    }
}
