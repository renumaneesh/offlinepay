package com.offlinepay.data.repository

import com.offlinepay.data.local.dao.PayeeDao
import com.offlinepay.data.local.dao.TransactionDao
import com.offlinepay.data.local.entity.PayeeEntity
import com.offlinepay.data.local.entity.RiskLevel
import com.offlinepay.data.local.entity.TransactionEntity
import com.offlinepay.domain.model.PaymentTarget
import com.offlinepay.domain.repository.PayeeMetrics
import com.offlinepay.domain.repository.PaymentRepository
import com.offlinepay.util.PayeeNormalizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PaymentRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val payeeDao: PayeeDao
) : PaymentRepository {

    override fun getAllTransactions(): Flow<List<TransactionEntity>> {
        return transactionDao.getAllTransactions()
    }

    override suspend fun recordTransaction(
        target: PaymentTarget,
        amount: Double,
        riskScore: Float,
        riskLevel: RiskLevel,
        riskReasons: List<String>,
        handoffMode: String,
        userConfirmed: Boolean,
        isTrusted: Boolean,
        timestamp: Long
    ): Long = withContext(Dispatchers.IO) {
        val norm = PayeeNormalizer.normalize(target)

        val tx = TransactionEntity(
            payeeId = norm.payeeId,
            payeeType = norm.payeeType,
            payeeName = norm.displayName,
            originalIdentifier = norm.originalIdentifier,
            amount = amount,
            timestamp = timestamp,
            riskScore = riskScore,
            riskLevel = riskLevel,
            riskReasons = riskReasons,
            handoffMode = handoffMode,
            userConfirmed = userConfirmed
        )

        val insertedId = transactionDao.insertTransaction(tx)

        if (userConfirmed) {
            payeeDao.recordPayeeInteraction(
                payeeId = norm.payeeId,
                payeeType = norm.payeeType,
                name = norm.displayName,
                timestamp = timestamp,
                isTrusted = isTrusted
            )
        }

        insertedId
    }

    override suspend fun getPayeeMetrics(target: PaymentTarget): PayeeMetrics = withContext(Dispatchers.IO) {
        val norm = PayeeNormalizer.normalize(target)
        val now = System.currentTimeMillis()
        val oneHourAgo = now - (60 * 60 * 1000)
        val twentyFourHoursAgo = now - (24 * 60 * 60 * 1000)

        val payee = payeeDao.getPayee(norm.payeeId)
        val isNew = (payee == null || payee.txnCount == 0)
        val isTrusted = payee?.isTrusted ?: false
        val payeeTxnCount = payee?.txnCount ?: 0

        val avgAmount = transactionDao.getAverageAmount()
        val txns1h = transactionDao.getTransactionCountSince(oneHourAgo)
        val txns24h = transactionDao.getTransactionCountSince(twentyFourHoursAgo)

        PayeeMetrics(
            isNewPayee = isNew,
            userAverageAmount = avgAmount,
            txnsLast1Hour = txns1h,
            txnsLast24Hours = txns24h,
            payeeTxnCount = payeeTxnCount,
            isTrusted = isTrusted
        )
    }

    override suspend fun setPayeeTrusted(target: PaymentTarget, isTrusted: Boolean) = withContext(Dispatchers.IO) {
        val norm = PayeeNormalizer.normalize(target)
        payeeDao.setTrusted(norm.payeeId, isTrusted)
    }

    override suspend fun getPayee(payeeId: String): PayeeEntity? = withContext(Dispatchers.IO) {
        payeeDao.getPayee(payeeId)
    }

    override suspend fun clearHistory() = withContext(Dispatchers.IO) {
        transactionDao.clearAll()
        payeeDao.clearAll()
    }
}
