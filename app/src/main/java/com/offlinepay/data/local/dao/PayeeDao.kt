package com.offlinepay.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.offlinepay.data.local.entity.PayeeEntity
import com.offlinepay.data.local.entity.PayeeType

@Dao
interface PayeeDao {

    @Query("SELECT * FROM payees WHERE payeeId = :payeeId LIMIT 1")
    suspend fun getPayee(payeeId: String): PayeeEntity?

    @Query("SELECT COUNT(*) = 0 FROM payees WHERE payeeId = :payeeId")
    suspend fun isNewPayee(payeeId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(payee: PayeeEntity)

    @Query("UPDATE payees SET isTrusted = :isTrusted WHERE payeeId = :payeeId")
    suspend fun setTrusted(payeeId: String, isTrusted: Boolean)

    @Query("SELECT COALESCE(txnCount, 0) FROM payees WHERE payeeId = :payeeId LIMIT 1")
    suspend fun getPayeeTxnCount(payeeId: String): Int?

    @Transaction
    suspend fun recordPayeeInteraction(
        payeeId: String,
        payeeType: PayeeType,
        name: String?,
        timestamp: Long,
        isTrusted: Boolean
    ) {
        val existing = getPayee(payeeId)
        if (existing == null) {
            insertOrUpdate(
                PayeeEntity(
                    payeeId = payeeId,
                    payeeType = payeeType,
                    name = name,
                    firstSeen = timestamp,
                    txnCount = 1,
                    isTrusted = isTrusted
                )
            )
        } else {
            insertOrUpdate(
                existing.copy(
                    name = name ?: existing.name,
                    txnCount = existing.txnCount + 1,
                    isTrusted = if (isTrusted) true else existing.isTrusted
                )
            )
        }
    }

    @Query("DELETE FROM payees")
    suspend fun clearAll()
}
