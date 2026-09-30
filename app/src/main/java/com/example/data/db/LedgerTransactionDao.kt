package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.LedgerTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerTransactionDao {
    @Query("SELECT * FROM ledger_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<LedgerTransaction>>

    @Query("SELECT * FROM ledger_transactions WHERE contactId = :contactId ORDER BY timestamp DESC")
    fun getTransactionsForContact(contactId: Long): Flow<List<LedgerTransaction>>

    @Query("SELECT * FROM ledger_transactions WHERE isSettled = 0 ORDER BY timestamp DESC")
    fun getActiveTransactions(): Flow<List<LedgerTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: LedgerTransaction): Long

    @Update
    suspend fun update(transaction: LedgerTransaction)

    @Delete
    suspend fun delete(transaction: LedgerTransaction)

    @Query("UPDATE ledger_transactions SET isSettled = 1 WHERE contactId = :contactId")
    suspend fun settleAllForContact(contactId: Long)

    @Query("DELETE FROM ledger_transactions")
    suspend fun deleteAll()
}
