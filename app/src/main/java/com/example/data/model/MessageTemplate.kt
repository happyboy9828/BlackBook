package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "message_templates")
data class MessageTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val body: String,
    val category: String = "REMINDER", // "REMINDER", "RECEIPT", "URGENT", "CUSTOM"
    val tone: String = "FIRM" // "TACTFUL", "FIRM", "ZERO_MERCY", "CLEARANCE"
)
