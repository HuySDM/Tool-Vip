package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // User Account
    @Query("SELECT * FROM user_account WHERE username = :username LIMIT 1")
    fun getUserAccountFlow(username: String): Flow<UserAccount?>

    @Query("SELECT * FROM user_account WHERE username = :username LIMIT 1")
    suspend fun getUserAccountDirect(username: String): UserAccount?

    @Query("SELECT * FROM user_account ORDER BY username ASC")
    fun getAllUserAccountsFlow(): Flow<List<UserAccount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAccount(userAccount: UserAccount)

    @Query("DELETE FROM user_account WHERE username = :username")
    suspend fun deleteUserAccount(username: String)

    // App Items
    @Query("SELECT * FROM app_items ORDER BY appName ASC")
    fun getAllAppsFlow(): Flow<List<AppItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<AppItem>)

    @Update
    suspend fun updateApp(app: AppItem)

    @Update
    suspend fun updateApps(apps: List<AppItem>)

    @Query("DELETE FROM app_items")
    suspend fun clearApps()

    // Game Accounts
    @Query("SELECT * FROM game_accounts ORDER BY id DESC")
    fun getAllGameAccountsFlow(): Flow<List<GameAccount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameAccounts(accounts: List<GameAccount>)

    @Update
    suspend fun updateGameAccount(account: GameAccount)

    // Chat History
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getChatHistoryFlow(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatHistory()

    // Transaction History
    @Query("SELECT * FROM transaction_history ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionItem>>

    @Query("SELECT * FROM transaction_history WHERE username = :username ORDER BY timestamp DESC")
    fun getTransactionsForUserFlow(username: String): Flow<List<TransactionItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionItem)

    @Query("DELETE FROM transaction_history")
    suspend fun clearTransactionHistory()

    // Imported Features (Custom Bots)
    @Query("SELECT * FROM imported_features ORDER BY id DESC")
    fun getAllImportedFeaturesFlow(): Flow<List<ImportedFeature>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImportedFeature(feature: ImportedFeature)

    @Query("UPDATE imported_features SET isActive = 0")
    suspend fun deactivateAllImportedFeatures()

    @Query("UPDATE imported_features SET isActive = :isActive WHERE id = :id")
    suspend fun setImportedFeatureActive(id: Int, isActive: Boolean)

    @Query("DELETE FROM imported_features WHERE id = :id")
    suspend fun deleteImportedFeature(id: Int)
}
