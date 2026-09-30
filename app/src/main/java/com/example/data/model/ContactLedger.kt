package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contact_ledgers")
data class ContactLedger(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val alias: String = "",
    val notes: String = "",
    val isAutoSmsEnabled: Boolean = false,
    val autoSmsDaysInterval: Int = 3,
    val lastReminderSentAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
