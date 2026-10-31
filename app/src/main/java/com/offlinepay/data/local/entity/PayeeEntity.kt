package com.offlinepay.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payees")
data class PayeeEntity(
    @PrimaryKey
    val payeeId: String,
    val payeeType: PayeeType,
    val name: String?,
    val firstSeen: Long,
    val txnCount: Int,
    val isTrusted: Boolean
)
