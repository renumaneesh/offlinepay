package com.offlinepay.domain.repository

import com.offlinepay.data.local.entity.PayeeEntity
import com.offlinepay.data.local.entity.RiskLevel
import com.offlinepay.data.local.entity.TransactionEntity
import com.offlinepay.domain.model.PaymentTarget
import kotlinx.coroutines.flow.Flow

data class PayeeMetrics(
    val isNewPayee: Boolean,
    val userAverageAmount: Double?,
    val txnsLast1Hour: Int,
    val txnsLast24Hours: Int,
    val payeeTxnCount: Int,
    val isTrusted: Boolean
)

interface PaymentRepository {
    fun getAllTransactions(): Flow<List<TransactionEntity>>
    suspend fun recordTransaction(
        target: PaymentTarget,
        amount: Double,
        riskScore: Float,
        riskLevel: RiskLevel,
        riskReasons: List<String>,
        handoffMode: String,
        userConfirmed: Boolean,
        isTrusted: Boolean,
        timestamp: Long = System.currentTimeMillis()
    ): Long
    suspend fun getPayeeMetrics(target: PaymentTarget): PayeeMetrics
    suspend fun setPayeeTrusted(target: PaymentTarget, isTrusted: Boolean)
    suspend fun getPayee(payeeId: String): PayeeEntity?
    suspend fun clearHistory()
}
