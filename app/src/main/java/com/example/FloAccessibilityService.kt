package com.example

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class FloAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Không cần xử lý gì, chỉ để app có thể nhận biết trợ năng đã bật
    }

    override fun onInterrupt() {}

    companion object {
        var isRunning = false

        fun isServiceEnabled(context: android.content.Context): Boolean {
            if (isRunning) return true
            val expectedComponentName = android.content.ComponentName(context, FloAccessibilityService::class.java)
            val enabledServicesSetting = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val colonSplitter = android.text.TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServicesSetting)

            while (colonSplitter.hasNext()) {
                val componentNameString = colonSplitter.next()
                val enabledComponentName = android.content.ComponentName.unflattenFromString(componentNameString)
                if (enabledComponentName != null && enabledComponentName == expectedComponentName) {
                    return true
                }
            }
            return false
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
        Log.d("FloAccessibility", "Service connected")
    }

    override fun onDestroy() {
        isRunning = false
        super.onDestroy()
    }
}
