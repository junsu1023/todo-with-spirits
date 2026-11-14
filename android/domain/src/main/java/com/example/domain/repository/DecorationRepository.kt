package com.example.domain.repository

import com.example.domain.model.Decoration
import com.example.domain.model.DecorationPurchase

interface DecorationRepository {
    suspend fun getDecorations(): Result<List<Decoration>>

    suspend fun purchaseDecoration(decorationId: Long): Result<DecorationPurchase>

    suspend fun installDecoration(decorationId: Long): Result<Unit>

    suspend fun uninstallDecoration(decorationId: Long): Result<Unit>
}
