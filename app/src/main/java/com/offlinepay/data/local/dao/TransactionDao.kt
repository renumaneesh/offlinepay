package com.offlinepay.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.offlinepay.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentTransactions(limit: Int): List<TransactionEntity>

    @Query("SELECT AVG(amount) FROM transactions WHERE userConfirmed = 1")
    suspend fun getAverageAmount(): Double?

    @Query("SELECT COUNT(*) FROM transactions WHERE timestamp >= :sinceTimestamp AND userConfirmed = 1")
    suspend fun getTransactionCountSince(sinceTimestamp: Long): Int

    @Query("SELECT COUNT(*) FROM transactions WHERE payeeId = :payeeId AND userConfirmed = 1")
    suspend fun getTransactionCountForPayee(payeeId: String): Int

    @Query("DELETE FROM transactions")
    suspend fun clearAll()
}
