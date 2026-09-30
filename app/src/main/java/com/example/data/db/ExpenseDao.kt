package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PersonalExpense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM personal_expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<PersonalExpense>>

    @Query("SELECT * FROM personal_expenses WHERE type = :type ORDER BY timestamp DESC")
    fun getExpensesByType(type: String): Flow<List<PersonalExpense>>

    @Query("SELECT * FROM personal_expenses WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getExpensesSince(startTime: Long): Flow<List<PersonalExpense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: PersonalExpense): Long

    @Update
    suspend fun update(expense: PersonalExpense)

    @Delete
    suspend fun delete(expense: PersonalExpense)

    @Query("DELETE FROM personal_expenses")
    suspend fun deleteAll()
}
