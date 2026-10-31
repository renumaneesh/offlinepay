package com.offlinepay.di

import android.content.Context
import com.offlinepay.data.local.AppDatabase
import com.offlinepay.data.repository.PaymentRepositoryImpl
import com.offlinepay.domain.repository.PaymentRepository

interface AppContainer {
    val paymentRepository: PaymentRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val database: AppDatabase by lazy {
        AppDatabase.getInstance(context)
    }

    override val paymentRepository: PaymentRepository by lazy {
        PaymentRepositoryImpl(
            transactionDao = database.transactionDao(),
            payeeDao = database.payeeDao()
        )
    }
}
