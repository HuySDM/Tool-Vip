package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val appDao: AppDao) {

    // User Account
    fun getUserAccount(username: String): Flow<UserAccount?> = appDao.getUserAccountFlow(username)
    
    suspend fun getUserAccountDirect(username: String): UserAccount? = appDao.getUserAccountDirect(username)

    val allUserAccounts: Flow<List<UserAccount>> = appDao.getAllUserAccountsFlow()

    suspend fun saveUserAccount(userAccount: UserAccount) {
        appDao.insertUserAccount(userAccount)
    }

    suspend fun deleteUserAccount(username: String) {
        appDao.deleteUserAccount(username)
    }

    // App Items
    val allApps: Flow<List<AppItem>> = appDao.getAllAppsFlow()

    suspend fun saveApps(apps: List<AppItem>) {
        appDao.insertApps(apps)
    }

    suspend fun updateApp(app: AppItem) {
        appDao.updateApp(app)
    }

    suspend fun updateApps(apps: List<AppItem>) {
        appDao.updateApps(apps)
    }

    suspend fun clearApps() {
        appDao.clearApps()
    }

    // Game Accounts
    val allGameAccounts: Flow<List<GameAccount>> = appDao.getAllGameAccountsFlow()

    suspend fun saveGameAccounts(accounts: List<GameAccount>) {
        appDao.insertGameAccounts(accounts)
    }

    suspend fun updateGameAccount(account: GameAccount) {
        appDao.updateGameAccount(account)
    }

    // Chat Messages
    val chatHistory: Flow<List<ChatMessage>> = appDao.getChatHistoryFlow()

    suspend fun addMessage(message: ChatMessage) {
        appDao.insertMessage(message)
    }

    suspend fun clearChatHistory() {
        appDao.clearChatHistory()
    }

    // Transaction History
    val allTransactions: Flow<List<TransactionItem>> = appDao.getAllTransactionsFlow()

    fun getTransactionsForUser(username: String): Flow<List<TransactionItem>> = 
        appDao.getTransactionsForUserFlow(username)

    suspend fun addTransaction(transaction: TransactionItem) {
        appDao.insertTransaction(transaction)
    }

    suspend fun clearAllTransactions() {
        appDao.clearTransactionHistory()
    }

    // Imported Features (Custom Bots)
    val allImportedFeatures: Flow<List<ImportedFeature>> = appDao.getAllImportedFeaturesFlow()

    suspend fun addImportedFeature(feature: ImportedFeature) {
        appDao.insertImportedFeature(feature)
    }

    suspend fun deactivateAllImportedFeatures() {
        appDao.deactivateAllImportedFeatures()
    }

    suspend fun setImportedFeatureActive(id: Int, isActive: Boolean) {
        appDao.setImportedFeatureActive(id, isActive)
    }

    suspend fun deleteImportedFeature(id: Int) {
        appDao.deleteImportedFeature(id)
    }

    val allCacheCleanRecords: Flow<List<CacheCleanRecord>> = appDao.getAllCacheCleanRecordsFlow()

    suspend fun insertCacheCleanRecord(record: CacheCleanRecord) {
        appDao.insertCacheCleanRecord(record)
    }

    suspend fun clearCacheCleanRecords() {
        appDao.clearCacheCleanRecords()
    }
}
