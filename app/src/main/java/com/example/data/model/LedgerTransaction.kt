package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ledger_transactions",
    foreignKeys = [
        ForeignKey(
            entity = ContactLedger::class,
            parentColumns = ["id"],
            childColumns = ["contactId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["contactId"])]
)
data class LedgerTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contactId: Long,
    val amount: Double,
    val type: String, // "GAVE" (You lent / they owe you), "TOOK" (You borrowed / you owe them), "SETTLED" (Payment back)
    val description: String = "",
    val dueDate: Long? = null,
    val isSettled: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
