package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.formatRupee
import com.example.ui.theme.*
import com.example.ui.viewmodel.CoopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: CoopViewModel,
    onBack: () -> Unit
) {
    val summary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
    val loans by viewModel.loans.collectAsStateWithLifecycle()
    val repayments by viewModel.repayments.collectAsStateWithLifecycle()

    val totalPortfolio = summary.totalDisbursed.coerceAtLeast(1.0)
    val recoveryEfficiency = ((summary.totalRecovered / totalPortfolio) * 100.0).coerceIn(0.0, 100.0)
    val npaRatio = if (summary.totalOutstanding > 0) ((summary.totalNpaAmount / summary.totalOutstanding) * 100.0) else 0.0

    // Group loans by loanType
    val loansByType = loans.groupBy { it.loanType }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial & Audit Reports", fontWeight = FontWeight.Bold) },
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
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Navy800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Shri Kamdar Credit Co-Operative Society",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Financial Position Statement • FY 2026-27",
                            style = MaterialTheme.typography.bodySmall,
                            color = Navy200
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Navy700)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Portfolio Outstanding", style = MaterialTheme.typography.bodySmall, color = Navy200)
                                Text(formatRupee(summary.totalOutstanding), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Recovery Efficiency", style = MaterialTheme.typography.bodySmall, color = Navy200)
                                Text("%.1f%%".format(recoveryEfficiency), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Green100)
                            }
                        }
                    }
                }
            }

            // Key Performance Ratios
            item {
                Text("Key Regulatory Indicators", fontWeight = FontWeight.Bold, color = Slate900, style = MaterialTheme.typography.titleMedium)
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RatioCard(
                        title = "Gross NPA Ratio",
                        value = "%.1f%%".format(npaRatio),
                        subtitle = "Target: < 5.0%",
                        color = if (npaRatio < 5.0) Green700 else Red700,
                        modifier = Modifier.weight(1f)
                    )
                    RatioCard(
                        title = "Today's Recovery",
                        value = formatRupee(summary.todayCollection),
                        subtitle = "Active counter",
                        color = Navy800,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RatioCard(
                        title = "Total Membership",
                        value = "${summary.totalMembers}",
                        subtitle = "${summary.activeMembers} Verified KYC",
                        color = Amber700,
                        modifier = Modifier.weight(1f)
                    )
                    RatioCard(
                        title = "Month Turnover",
                        value = formatRupee(summary.monthCollection),
                        subtitle = "October 2026",
                        color = Slate800,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Scheme-wise portfolio breakdown
            item {
                Text("Scheme-wise Loan Portfolio", fontWeight = FontWeight.Bold, color = Slate900, style = MaterialTheme.typography.titleMedium)
            }

            items(loansByType.entries.toList()) { (scheme, schemeLoans) ->
                val schemeDisbursed = schemeLoans.sumOf { it.principalAmount }
                val schemeOutstanding = schemeLoans.sumOf { it.outstandingBalance }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(scheme, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = Navy800)
                            Text("${schemeLoans.size} Loans", style = MaterialTheme.typography.bodySmall, color = Slate500)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Disbursed", style = MaterialTheme.typography.bodySmall, color = Slate500)
                                Text(formatRupee(schemeDisbursed), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Outstanding", style = MaterialTheme.typography.bodySmall, color = Slate500)
                                Text(formatRupee(schemeOutstanding), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Slate900)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RatioCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = Slate600)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Slate500)
        }
    }
}
