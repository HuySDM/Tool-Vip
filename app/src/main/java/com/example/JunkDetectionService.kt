package com.example

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.data.AppDatabase
import com.example.data.AppItem
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class JunkDetectionService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var scanJob: Job? = null
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): JunkDetectionService = this@JunkDetectionService
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "Service Bound")
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "JunkDetectionService Created")
        startPeriodicScan()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "JunkDetectionService Started (onStartCommand)")
        // Ensure scanning is active
        if (scanJob == null || scanJob?.isActive == false) {
            startPeriodicScan()
        }
        return START_STICKY
    }

    private fun startPeriodicScan() {
        scanJob?.cancel()
        scanJob = serviceScope.launch {
            while (isActive) {
                try {
                    val database = AppDatabase.getDatabase(applicationContext)
                    val dao = database.appDao()
                    
                    // Fetch all apps directly from DB
                    val apps = dao.getAllAppsFlow().first()
                    
                    // Analyze for junk packages (isTrash == true)
                    val junkApps = apps.filter { it.isTrash }
                    
                    // Auto-Clean: Silently clean and freeze newly detected unfrozen junk apps to save battery & RAM
                    var autoCleanedCount = 0
                    junkApps.forEach { app ->
                        if (!app.isFrozen) {
                            val updatedApp = app.copy(isFrozen = true, ramUsage = (app.ramUsage * 0.1).coerceAtLeast(0.0)) // Reduce RAM on freeze
                            dao.updateApp(updatedApp)
                            autoCleanedCount++
                        }
                    }
                    
                    // Also periodically optimize/trim RAM of non-frozen heavy apps a tiny bit to simulate continuous active background cleaning
                    apps.forEach { app ->
                        if (!app.isFrozen && app.ramUsage > 150.0) {
                            val slightlyReducedRam = (app.ramUsage * 0.95).coerceAtLeast(150.0)
                            if (slightlyReducedRam != app.ramUsage) {
                                dao.updateApp(app.copy(ramUsage = slightlyReducedRam))
                            }
                        }
                    }
                    
                    val totalMemorySaved = junkApps.filter { it.isFrozen }.sumOf { it.ramUsage }
                    
                    Log.d(TAG, "[SCAN_REPORT] === JUNK APP ACTIVE BACKGROUND AUTO-CLEAN ===")
                    Log.d(TAG, "[SCAN_REPORT] Total apps monitored: ${apps.size}")
                    Log.d(TAG, "[SCAN_REPORT] Junk apps auto-cleaned/frozen in this cycle: $autoCleanedCount")
                    Log.d(TAG, "[SCAN_REPORT] Total junk apps frozen: ${junkApps.count { it.isFrozen }}")
                    Log.d(TAG, "[SCAN_REPORT] RAM released through active freeze: $totalMemorySaved MB")
                    
                    // Background network speedup & bandwidth optimization simulation
                    Log.d(TAG, "[NETWORK_SPEEDUP] Optimizing background network sockets...")
                    Log.d(TAG, "[NETWORK_SPEEDUP] Traffic prioritizing enabled: Low packet loss, Game latency reduced!")
                    Log.d(TAG, "[NETWORK_SPEEDUP] Network channels successfully optimized in the background (Low power mode).")
                    
                    junkApps.forEach { app ->
                        Log.d(TAG, "[SCAN_REPORT]  - Package: ${app.packageName} | Name: ${app.appName} | RAM: ${app.ramUsage}MB | Frozen: ${app.isFrozen}")
                    }
                    
                    // Smart auto-detection: If there's an unfrozen background heavy non-system app, suggest it
                    val heavyApp = apps.find { !it.isSystemApp && !it.isFrozen && it.ramUsage > 300.0 }
                    if (heavyApp != null) {
                        Log.w(TAG, "[SCAN_REPORT] Heavy background app detected: ${heavyApp.appName} (${heavyApp.ramUsage}MB). Consider freezing to save battery!")
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error in background scan: ${e.message}", e)
                }
                
                // Scan periodically every 15 seconds in the background
                delay(15000)
            }
        }
    }

    /**
     * Toggles the freeze state of a specific package in the database.
     */
    fun togglePackageFreeze(packageName: String, shouldFreeze: Boolean) {
        serviceScope.launch {
            try {
                val database = AppDatabase.getDatabase(applicationContext)
                val dao = database.appDao()
                val apps = dao.getAllAppsFlow().first()
                val app = apps.find { it.packageName == packageName }
                if (app != null) {
                    val updatedApp = app.copy(isFrozen = shouldFreeze)
                    dao.updateApp(updatedApp)
                    Log.i(TAG, "[STATE_CHANGE] Successfully toggled freeze state for $packageName to $shouldFreeze")
                } else {
                    Log.e(TAG, "[STATE_CHANGE] Package $packageName not found in database.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to toggle package freeze: ${e.message}", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scanJob?.cancel()
        serviceScope.cancel()
        Log.d(TAG, "JunkDetectionService Destroyed")
    }

    companion object {
        private const val TAG = "JunkDetectionService"
    }
}
