package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_account")
data class UserAccount(
    @PrimaryKey val username: String, // unique username (e.g. "admin", "quanghuy")
    val passwordHash: String = "", // stored plaintext password
    val tier: String = "UNPAID", // UNPAID, VIP1, VIP2, STAFF, MANAGER, PARTNER, ADMIN
    val balance: Double = 0.0,
    val expiryTimestamp: Long = 0L,
    val isAutoRenew: Boolean = true,
    val isAutoFreeze: Boolean = false,
    val freezeThreshold: Int = 80, // RAM utilization threshold %
    val vip1Price: Double = 50000.0,
    val vip2Price: Double = 120000.0,
    val customRole: String = "Thành viên thường",
    val lastGeneratedCode: String = "",
    val link1Passed: Boolean = false,
    val link2Passed: Boolean = false,
    val isScheduledOptEnabled: Boolean = false,
    val optIntervalMinutes: Int = 15,
    val email: String = "",
    val requestedTier: String = "",
    val requestedRoleName: String = "",
    val hasPendingRequest: Boolean = false,
    val isUserDataAuthorized: Boolean = false,
    val authPin: String = ""
)

@Entity(tableName = "app_items")
data class AppItem(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
    val isFrozen: Boolean,
    val ramUsage: Double, // in MB
    val isTrash: Boolean,
    val lastOptimized: Long = System.currentTimeMillis()
)

@Entity(tableName = "game_accounts")
data class GameAccount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val gameTitle: String,
    val accountName: String,
    val price: Double,
    val details: String,
    val isBought: Boolean = false,
    val buyerEmail: String = ""
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // USER or AI
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "transaction_history")
data class TransactionItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val amount: Double,
    val type: String, // "DEPOSIT", "UPGRADE_VIP1", "UPGRADE_VIP2", "MANUAL_ADJUST"
    val status: String, // "SUCCESS", "PENDING_AI", "FAILED"
    val referenceNote: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "imported_features")
data class ImportedFeature(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fileName: String,
    val rawCode: String,
    val detectedName: String,
    val detectedDescription: String,
    val systemPrompt: String,
    val isActive: Boolean = false
)

@Entity(tableName = "cache_clean_records")
data class CacheCleanRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val clearedSizeMb: Double,
    val reclaimedRamMb: Double,
    val cleanType: String // "MANUAL", "AUTO"
)
