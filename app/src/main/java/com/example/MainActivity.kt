package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.ToolVipApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Start background JunkDetectionService
        try {
            val serviceIntent = Intent(this, JunkDetectionService::class.java)
            startService(serviceIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            MyApplicationTheme {
                ToolVipApp()
            }
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        // Proactively request GC and trim system cache on high memory pressure
        if (level >= TRIM_MEMORY_RUNNING_LOW || level == TRIM_MEMORY_UI_HIDDEN) {
            System.gc()
        }
    }
}
