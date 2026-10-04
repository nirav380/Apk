package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.network.ServerConnectionConfig
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.CoopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerSettingsScreen(
    viewModel: CoopViewModel,
    onBack: () -> Unit,
    onOpenWebPortal: (() -> Unit)? = null
) {
    val serverConfig by viewModel.serverConfig.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
    val isTesting by viewModel.isTestingConnection.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val lastSyncResult by viewModel.lastSyncResult.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()
    val loans by viewModel.loans.collectAsStateWithLifecycle()

    var serverUrl by remember { mutableStateOf(serverConfig.serverUrl) }
    var apiKey by remember { mutableStateOf(serverConfig.apiKey) }
    var autoSync by remember { mutableStateOf(serverConfig.autoSyncEnabled) }
    var showSqlExportDialog by remember { mutableStateOf(false) }

    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var showSaveToast by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Somee.com Server Sync", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy800,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Status Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Navy800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(if (connectionStatus?.isSuccess == true) Green100 else if (isTesting) Amber100 else Slate100),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (connectionStatus?.isSuccess == true) Icons.Default.CloudDone else if (isTesting) Icons.Default.CloudSync else Icons.Default.CloudQueue,
                                        contentDescription = null,
                                        tint = if (connectionStatus?.isSuccess == true) Green700 else if (isTesting) Amber700 else Navy800,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (connectionStatus?.isSuccess == true) "Somee.com Server Online" else if (isTesting) "Connecting..." else "Server Status",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = serverConfig.serverUrl,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Navy200
                                    )
                                }
                            }
                            if (connectionStatus != null) {
                                StatusBadge(status = if (connectionStatus!!.isSuccess) "Connected" else "Offline")
                            }
                        }

                        connectionStatus?.let { res ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (res.isSuccess) Green700.copy(alpha = 0.25f) else Red700.copy(alpha = 0.25f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = res.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (res.latencyMs > 0) {
                                        Text(
                                            text = "Latency: ${res.latencyMs} ms",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Amber100
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Navy700)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Last Synced:", style = MaterialTheme.typography.bodySmall, color = Navy200)
                            Text(serverConfig.lastSyncTime, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Server Configuration Inputs
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Somee Server Credentials",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )

                        OutlinedTextField(
                            value = serverUrl,
                            onValueChange = { serverUrl = it },
                            label = { Text("Somee Server URL (HTTP/HTTPS) *") },
                            placeholder = { Text("http://your-site.somee.com") },
                            modifier = Modifier.fillMaxWidth().testTag("server_url_input"),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Dns, contentDescription = null, tint = Navy800) },
                            trailingIcon = {
                                if (serverUrl != "http://cooperativesociety.somee.com") {
                                    IconButton(onClick = { serverUrl = "http://cooperativesociety.somee.com" }) {
                                        Icon(Icons.Default.Restore, contentDescription = "Default")
                                    }
                                }
                            }
                        )

                        // Quick domain presets
                        Text("Suggested Domains:", style = MaterialTheme.typography.bodySmall, color = Slate500)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            PresetChip("cooprativesociety.somee.com (Live)") { serverUrl = "http://cooprativesociety.somee.com" }
                            PresetChip("cooperativesociety.somee.com") { serverUrl = "http://cooperativesociety.somee.com" }
                        }

                        if (onOpenWebPortal != null) {
                            OutlinedButton(
                                onClick = onOpenWebPortal,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Navy800)
                            ) {
                                Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Open Live Somee Web Portal (WebView)")
                            }
                        }

                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = { apiKey = it },
                            label = { Text("API Key / Bearer Token (Optional)") },
                            placeholder = { Text("Enter token if secured") },
                            modifier = Modifier.fillMaxWidth().testTag("api_key_input"),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = Amber700) }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Auto-Sync on Changes", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                Text("Background sync with Somee.com", style = MaterialTheme.typography.bodySmall, color = Slate500)
                            }
                            Switch(
                                checked = autoSync,
                                onCheckedChange = { autoSync = it }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.testConnection(serverUrl, apiKey) },
                                modifier = Modifier.weight(1f).testTag("test_server_button"),
                                shape = RoundedCornerShape(10.dp),
                                enabled = !isTesting
                            ) {
                                if (isTesting) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Testing...")
                                } else {
                                    Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Test Ping")
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.updateServerConfig(
                                        ServerConnectionConfig(
                                            serverUrl = serverUrl,
                                            apiKey = apiKey,
                                            autoSyncEnabled = autoSync,
                                            lastSyncTime = serverConfig.lastSyncTime
                                        )
                                    )
                                    showSaveToast = true
                                },
                                modifier = Modifier.weight(1f).testTag("save_server_config_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Navy800)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save Settings")
                            }
                        }

                        if (showSaveToast) {
                            Text("✓ Settings saved successfully!", color = Green700, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Sync Data Action Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Two-Way Cloud Synchronization",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "Syncs all local Room Database records (Members, Loans, Daily Collections, NPA Cases) with your Somee server.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )

                        Button(
                            onClick = { viewModel.syncWithSomeeServer() },
                            colors = ButtonDefaults.buttonColors(containerColor = Green700),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("sync_now_button"),
                            enabled = !isSyncing
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Synchronizing with Somee.com...")
                            } else {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sync All Records Now")
                            }
                        }

                        lastSyncResult?.let { res ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (res.isSuccess) Green100 else Red100,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = if (res.isSuccess) "✓ ${res.message}" else "⚠ ${res.message}",
                                        color = if (res.isSuccess) Green700 else Red700,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (res.isSuccess) {
                                        Text(
                                            text = "Uploaded: ${res.membersPushed} Members, ${res.loansPushed} Loans, ${res.repaymentsPushed} Repayments",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Green700
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // MSSQL Script Generator Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate100),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Somee MS SQL Server Backup", fontWeight = FontWeight.Bold, color = Slate900)
                                Text("Export local data directly to SQL script", style = MaterialTheme.typography.bodySmall, color = Slate500)
                            }
                            Button(
                                onClick = { showSqlExportDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Export SQL")
                            }
                        }
                    }
                }
            }

            // Guide for Somee.com setup
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Somee.com Server Setup Guide",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Navy800
                        )
                        Text(
                            text = "1. Apne Somee.com account me login karke 'Websites' section me jayein aur domain name check karein (e.g. yoursite.somee.com).\n2. Upar Server URL me apna exact Somee domain dalein (http://yoursite.somee.com).\n3. 'Test Ping' button daba kar check karein ki server response de raha hai ya nahi.\n4. 'Sync All Records Now' se local app ka sara data Somee database me sync ho jayega.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate700
                        )
                    }
                }
            }
        }
    }

    if (showSqlExportDialog) {
        val sqlScript = remember(members, loans) {
            val sb = StringBuilder()
            sb.append("-- Shri Kamdar Credit Co-Operative Society SQL Export for Somee MS SQL Server\n")
            sb.append("USE CooprativeDemo;\nGO\n\n")
            members.forEach { m ->
                sb.append("INSERT INTO dbo.Members (MemberNo, FullName, Mobile, Email, [Address], JoinDate, IsActive) VALUES ('${m.memberNo}', N'${m.fullName}', '${m.mobile}', '${m.email}', N'${m.address}', '${m.joinDate}', 1);\n")
            }
            sb.append("\nGO\n")
            loans.forEach { l ->
                sb.append("INSERT INTO dbo.Loans (MemberId, LoanNo, LoanType, PrincipalAmount, InterestRate, TenureMonths, EmiAmount, OutstandingBalance, Status) VALUES (${l.memberId}, '${l.loanNo}', '${l.loanType}', ${l.principalAmount}, ${l.interestRate}, ${l.tenureMonths}, ${l.emiAmount}, ${l.outstandingBalance}, '${l.status}');\n")
            }
            sb.toString()
        }

        Dialog(onDismissRequest = { showSqlExportDialog = false }) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f)
            ) {
                Column(modifier = Modifier.padding(18.dp).fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Somee MS SQL Script", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { showSqlExportDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Slate900, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(sqlScript, color = Green100, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(sqlScript))
                            showSqlExportDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy SQL to Clipboard")
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetChip(label: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Navy100,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Navy800
        )
    }
}
