package com.example

import android.app.Application
import com.example.data.database.AppDatabase
import com.example.data.network.SomeeApiClient
import com.example.data.repository.CoopRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class CoopApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val someeApiClient by lazy { SomeeApiClient(this) }
    val repository by lazy {
        CoopRepository(
            database.memberDao(),
            database.loanDao(),
            database.repaymentDao(),
            database.recoveryDao()
        )
    }
}
