package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LoanEntity
import com.example.data.model.MemberEntity
import com.example.ui.components.TopBarHeader
import com.example.ui.screens.*
import com.example.ui.theme.CoopSocietyTheme
import com.example.ui.theme.Navy900
import com.example.ui.viewmodel.CoopViewModel
import com.example.ui.viewmodel.CoopViewModelFactory

enum class ScreenTab(val title: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard, "tab_dashboard"),
    MEMBERS("Members", Icons.Default.People, "tab_members"),
    LOANS("Loans", Icons.Default.CreditCard, "tab_loans"),
    COLLECTION("Collection", Icons.Default.ReceiptLong, "tab_collection"),
    RECOVERY("Recovery", Icons.Default.Warning, "tab_recovery")
}

class MainActivity : ComponentActivity() {

    private val viewModel: CoopViewModel by viewModels {
        val app = application as CoopApplication
        CoopViewModelFactory(app.repository, app.someeApiClient)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CoopSocietyTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: CoopViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(ScreenTab.DASHBOARD) }
    var showReportsScreen by remember { mutableStateOf(false) }
    var showServerSettings by remember { mutableStateOf(false) }
    var showWebPortal by remember { mutableStateOf(false) }
    var webPortalUrl by remember { mutableStateOf(viewModel.serverConfig.value.serverUrl) }

    var preselectedMemberForLoan by remember { mutableStateOf<MemberEntity?>(null) }
    var preselectedLoanForCollection by remember { mutableStateOf<LoanEntity?>(null) }

    // If not logged in, show Login Screen first!
    if (!isLoggedIn) {
        if (showWebPortal) {
            WebPortalScreen(
                initialUrl = "http://cooprativesociety.somee.com/Login.aspx",
                onBack = { showWebPortal = false }
            )
        } else {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    currentTab = ScreenTab.DASHBOARD
                },
                onOpenWebLogin = {
                    webPortalUrl = "http://cooprativesociety.somee.com/Login.aspx"
                    showWebPortal = true
                }
            )
        }
        return
    }

    BackHandler(enabled = showWebPortal || showServerSettings || showReportsScreen || currentTab != ScreenTab.DASHBOARD) {
        if (showWebPortal) {
            showWebPortal = false
        } else if (showServerSettings) {
            showServerSettings = false
        } else if (showReportsScreen) {
            showReportsScreen = false
        } else {
            currentTab = ScreenTab.DASHBOARD
        }
    }

    if (showWebPortal) {
        WebPortalScreen(
            initialUrl = webPortalUrl,
            onBack = { showWebPortal = false }
        )
    } else if (showServerSettings) {
        ServerSettingsScreen(
            viewModel = viewModel,
            onBack = { showServerSettings = false },
            onOpenWebPortal = {
                showServerSettings = false
                webPortalUrl = viewModel.serverConfig.value.serverUrl
                showWebPortal = true
            }
        )
    } else if (showReportsScreen) {
        ReportsScreen(
            viewModel = viewModel,
            onBack = { showReportsScreen = false }
        )
    } else {
        Scaffold(
            topBar = {
                TopBarHeader(
                    currentRole = currentUser.role,
                    isServerConnected = connectionStatus?.isSuccess == true,
                    onRoleChange = { role -> viewModel.switchUserRole(role) },
                    onOpenReports = { showReportsScreen = true },
                    onOpenServerSettings = { showServerSettings = true },
                    onOpenWebPortal = {
                        webPortalUrl = viewModel.serverConfig.value.serverUrl
                        showWebPortal = true
                    },
                    onLogout = { viewModel.logout() }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = Navy900,
                    tonalElevation = 8.dp
                ) {
                    ScreenTab.values().forEach { tab ->
                        NavigationBarItem(
                            selected = currentTab == tab,
                            onClick = {
                                currentTab = tab
                                preselectedMemberForLoan = null
                                preselectedLoanForCollection = null
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title
                                )
                            },
                            label = { Text(tab.title) },
                            modifier = Modifier.testTag(tab.tag)
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Crossfade(targetState = currentTab, label = "ScreenTransition") { tab ->
                    when (tab) {
                        ScreenTab.DASHBOARD -> DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToMembers = { currentTab = ScreenTab.MEMBERS },
                            onNavigateToLoans = { currentTab = ScreenTab.LOANS },
                            onNavigateToCollection = { currentTab = ScreenTab.COLLECTION },
                            onNavigateToRecovery = { currentTab = ScreenTab.RECOVERY }
                        )
                        ScreenTab.MEMBERS -> MembersScreen(
                            viewModel = viewModel,
                            onNavigateToApplyLoan = { member ->
                                preselectedMemberForLoan = member
                                currentTab = ScreenTab.LOANS
                            }
                        )
                        ScreenTab.LOANS -> LoansScreen(
                            viewModel = viewModel,
                            preselectedMember = preselectedMemberForLoan,
                            onNavigateToCollectionWithLoan = { loan ->
                                preselectedLoanForCollection = loan
                                currentTab = ScreenTab.COLLECTION
                            }
                        )
                        ScreenTab.COLLECTION -> CollectionScreen(
                            viewModel = viewModel,
                            preselectedLoan = preselectedLoanForCollection
                        )
                        ScreenTab.RECOVERY -> RecoveryScreen(
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}
