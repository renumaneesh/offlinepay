package com.offlinepay.data.local

import androidx.room.TypeConverter
import com.offlinepay.data.local.entity.PayeeType
import com.offlinepay.data.local.entity.RiskLevel

class Converters {

    @TypeConverter
    fun fromPayeeType(value: PayeeType): String = value.name

    @TypeConverter
    fun toPayeeType(value: String): PayeeType = try {
        PayeeType.valueOf(value)
    } catch (e: Exception) {
        PayeeType.UPI_ID
    }

    @TypeConverter
    fun fromRiskLevel(value: RiskLevel): String = value.name

    @TypeConverter
    fun toRiskLevel(value: String): RiskLevel = try {
        RiskLevel.valueOf(value)
    } catch (e: Exception) {
        RiskLevel.LOW
    }

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        return list?.joinToString(";;;") ?: ""
    }

    @TypeConverter
    fun toStringList(data: String?): List<String> {
        if (data.isNullOrBlank()) return emptyList()
        return data.split(";;;").filter { it.isNotBlank() }
    }
}
