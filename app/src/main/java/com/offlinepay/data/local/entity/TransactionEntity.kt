package com.offlinepay.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["payeeId"]),
        Index(value = ["timestamp"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val payeeId: String,
    val payeeType: PayeeType,
    val payeeName: String?,
    val originalIdentifier: String,
    val amount: Double,
    val note: String? = null,
    val timestamp: Long,
    val riskScore: Float,
    val riskLevel: RiskLevel,
    val riskReasons: List<String>,
    val handoffMode: String,
    val userConfirmed: Boolean
)
