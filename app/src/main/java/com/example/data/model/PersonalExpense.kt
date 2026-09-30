package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "personal_expenses")
data class PersonalExpense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: String, // "EXPENSE" or "INCOME"
    val category: String, // "Food", "Bills", "Fuel", "Transport", "Shopping", "Operations", "Salary", "Discretionary"
    val title: String,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
