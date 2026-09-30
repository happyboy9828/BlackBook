package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ContactLedger
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactLedgerDao {
    @Query("SELECT * FROM contact_ledgers ORDER BY name ASC")
    fun getAllContacts(): Flow<List<ContactLedger>>

    @Query("SELECT * FROM contact_ledgers WHERE id = :id")
    suspend fun getContactById(id: Long): ContactLedger?

    @Query("SELECT * FROM contact_ledgers WHERE isAutoSmsEnabled = 1")
    fun getAutoSmsContacts(): Flow<List<ContactLedger>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactLedger): Long

    @Update
    suspend fun updateContact(contact: ContactLedger)

    @Delete
    suspend fun deleteContact(contact: ContactLedger)

    @Query("UPDATE contact_ledgers SET lastReminderSentAt = :time WHERE id = :contactId")
    suspend fun updateLastReminderSent(contactId: Long, time: Long)

    @Query("DELETE FROM contact_ledgers")
    suspend fun deleteAll()
}
