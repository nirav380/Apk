package com.example.data.network

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.LoanEntity
import com.example.data.model.MemberEntity
import com.example.data.model.RepaymentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class ServerConnectionConfig(
    val serverUrl: String = "http://cooprativesociety.somee.com",
    val apiKey: String = "",
    val autoSyncEnabled: Boolean = false,
    val lastSyncTime: String = "Never"
)

data class ConnectionTestResult(
    val isSuccess: Boolean,
    val statusCode: Int = 0,
    val message: String,
    val latencyMs: Long = 0,
    val resolvedUrl: String? = null
)

data class SyncResult(
    val isSuccess: Boolean,
    val membersPushed: Int = 0,
    val loansPushed: Int = 0,
    val repaymentsPushed: Int = 0,
    val membersFetched: Int = 0,
    val loansFetched: Int = 0,
    val message: String
)

class SomeeApiClient(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("somee_server_prefs", Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    fun getConfig(): ServerConnectionConfig {
        return ServerConnectionConfig(
            serverUrl = prefs.getString("server_url", "http://cooprativesociety.somee.com") ?: "http://cooprativesociety.somee.com",
            apiKey = prefs.getString("api_key", "") ?: "",
            autoSyncEnabled = prefs.getBoolean("auto_sync", false),
            lastSyncTime = prefs.getString("last_sync", "Never") ?: "Never"
        )
    }

    fun saveConfig(config: ServerConnectionConfig) {
        prefs.edit()
            .putString("server_url", config.serverUrl.trim().removeSuffix("/"))
            .putString("api_key", config.apiKey.trim())
            .putBoolean("auto_sync", config.autoSyncEnabled)
            .putString("last_sync", config.lastSyncTime)
            .apply()
    }

    fun updateLastSyncTime(time: String) {
        prefs.edit().putString("last_sync", time).apply()
    }

    suspend fun testConnection(baseUrl: String, apiKey: String = ""): ConnectionTestResult = withContext(Dispatchers.IO) {
        var cleanUrl = baseUrl.trim().removeSuffix("/")
        val startTime = System.currentTimeMillis()

        // Helper to attempt connection
        fun attempt(targetUrl: String): Pair<Int, Long>? {
            return try {
                val reqBuilder = Request.Builder()
                    .url(targetUrl)
                    .header("User-Agent", "CoopSociety-Android/1.0")
                if (apiKey.isNotBlank()) {
                    reqBuilder.header("Authorization", "Bearer $apiKey")
                }
                val resp = client.newCall(reqBuilder.build()).execute()
                val lat = System.currentTimeMillis() - startTime
                val c = resp.code
                resp.close()
                Pair(c, lat)
            } catch (e: Exception) {
                null
            }
        }

        val primaryResult = attempt(cleanUrl)
        if (primaryResult != null && primaryResult.first in 200..399) {
            return@withContext ConnectionTestResult(
                isSuccess = true,
                statusCode = primaryResult.first,
                message = "Connected to Somee Server successfully! (HTTP ${primaryResult.first})",
                latencyMs = primaryResult.second,
                resolvedUrl = cleanUrl
            )
        }

        // Check if user typed 'cooperativesociety' instead of 'cooprativesociety'
        if (cleanUrl.contains("cooperativesociety.somee.com")) {
            val altUrl = cleanUrl.replace("cooperativesociety.somee.com", "cooprativesociety.somee.com")
            val altResult = attempt(altUrl)
            if (altResult != null && altResult.first in 200..399) {
                saveConfig(getConfig().copy(serverUrl = altUrl))
                return@withContext ConnectionTestResult(
                    isSuccess = true,
                    statusCode = altResult.first,
                    message = "Connected to Somee Server at $altUrl (HTTP ${altResult.first})!",
                    latencyMs = altResult.second,
                    resolvedUrl = altUrl
                )
            }
        }

        val latency = System.currentTimeMillis() - startTime
        ConnectionTestResult(
            isSuccess = false,
            statusCode = primaryResult?.first ?: 0,
            message = if (primaryResult != null) "Server returned HTTP ${primaryResult.first}." else "Could not reach $cleanUrl. Please verify domain name in Somee.com control panel.",
            latencyMs = latency
        )
    }

    suspend fun syncWithServer(
        members: List<MemberEntity>,
        loans: List<LoanEntity>,
        repayments: List<RepaymentEntity>
    ): SyncResult = withContext(Dispatchers.IO) {
        val config = getConfig()
        val url = "${config.serverUrl}/api/sync"

        try {
            // Prepare payload
            val root = JSONObject()
            val membersArray = JSONArray()
            members.forEach { m ->
                val obj = JSONObject()
                obj.put("MemberId", m.memberId)
                obj.put("MemberNo", m.memberNo)
                obj.put("FullName", m.fullName)
                obj.put("Mobile", m.mobile)
                obj.put("Email", m.email)
                obj.put("Address", m.address)
                obj.put("JoinDate", m.joinDate)
                obj.put("IsActive", m.isActive)
                obj.put("AadharNo", m.aadharNo)
                obj.put("PanNo", m.panNo)
                obj.put("BankName", m.bankName)
                obj.put("AccountNumber", m.accountNumber)
                obj.put("IfscCode", m.ifscCode)
                obj.put("ApprovalStatus", m.approvalStatus)
                membersArray.put(obj)
            }
            root.put("members", membersArray)

            val loansArray = JSONArray()
            loans.forEach { l ->
                val obj = JSONObject()
                obj.put("LoanId", l.loanId)
                obj.put("MemberId", l.memberId)
                obj.put("MemberNo", l.memberNo)
                obj.put("LoanNo", l.loanNo)
                obj.put("LoanType", l.loanType)
                obj.put("PrincipalAmount", l.principalAmount)
                obj.put("InterestRate", l.interestRate)
                obj.put("TenureMonths", l.tenureMonths)
                obj.put("EmiAmount", l.emiAmount)
                obj.put("OutstandingBalance", l.outstandingBalance)
                obj.put("Status", l.status)
                loansArray.put(obj)
            }
            root.put("loans", loansArray)

            val repaymentsArray = JSONArray()
            repayments.forEach { r ->
                val obj = JSONObject()
                obj.put("RepaymentId", r.repaymentId)
                obj.put("LoanId", r.loanId)
                obj.put("ReceiptNo", r.receiptNo)
                obj.put("PaymentDate", r.paymentDate)
                obj.put("AmountPaid", r.amountPaid)
                obj.put("PrincipalPaid", r.principalPaid)
                obj.put("InterestPaid", r.interestPaid)
                obj.put("PaymentMode", r.paymentMode)
                obj.put("CollectedBy", r.collectedBy)
                repaymentsArray.put(obj)
            }
            root.put("repayments", repaymentsArray)

            val requestBody = root.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val requestBuilder = Request.Builder()
                .url(url)
                .post(requestBody)

            if (config.apiKey.isNotBlank()) {
                requestBuilder.header("Authorization", "Bearer ${config.apiKey}")
            }

            val response = client.newCall(requestBuilder.build()).execute()
            val code = response.code
            val body = response.body?.string() ?: ""
            response.close()

            if (code in 200..299) {
                SyncResult(
                    isSuccess = true,
                    membersPushed = members.size,
                    loansPushed = loans.size,
                    repaymentsPushed = repayments.size,
                    message = "Sync Completed Successfully with Somee.com! (HTTP $code)"
                )
            } else {
                SyncResult(
                    isSuccess = false,
                    membersPushed = members.size,
                    loansPushed = loans.size,
                    repaymentsPushed = repayments.size,
                    message = "Server returned HTTP $code. Ensure /api/sync endpoint is deployed on your Somee site."
                )
            }
        } catch (e: Exception) {
            SyncResult(
                isSuccess = false,
                message = "Sync error: ${e.localizedMessage ?: e.message}"
            )
        }
    }
}
