package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.BlackBookDatabase
import com.example.data.model.ContactLedger
import com.example.data.model.ContactWithBalance
import com.example.data.model.LedgerSummary
import com.example.data.model.LedgerTransaction
import com.example.data.model.MessageTemplate
import com.example.data.model.PersonalExpense
import com.example.data.model.CashFlowSummary
import com.example.data.repository.BlackBookRepository
import com.example.data.repository.SecurityPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab {
    LEDGER,
    CASH_FLOW,
    VAULT
}

enum class UnlockResult {
    SUCCESS,
    DURESS,
    WRONG_PIN
}

class BlackBookViewModel(application: Application) : AndroidViewModel(application) {

    private val database = BlackBookDatabase.getDatabase(application, viewModelScope)
    private val repository = BlackBookRepository(database)
    val securityPrefs = SecurityPreferences(application)

    // Lock Screen State
    private val _isLocked = MutableStateFlow(securityPrefs.isSecurityEnabled)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _isDuressMode = MutableStateFlow(false)
    val isDuressMode: StateFlow<Boolean> = _isDuressMode.asStateFlow()

    // Navigation State
    private val _currentTab = MutableStateFlow(AppTab.LEDGER)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _selectedContactId = MutableStateFlow<Long?>(null)
    val selectedContactId: StateFlow<Long?> = _selectedContactId.asStateFlow()

    // Search query for Ledger
    private val _ledgerSearchQuery = MutableStateFlow("")
    val ledgerSearchQuery: StateFlow<String> = _ledgerSearchQuery.asStateFlow()

    // Currency Symbol
    private val _currencySymbol = MutableStateFlow(securityPrefs.currencySymbol)
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    // Data streams from repository
    val contactsWithBalances: StateFlow<List<ContactWithBalance>> = combine(
        repository.contactsWithBalances,
        _ledgerSearchQuery,
        _isDuressMode
    ) { list, query, duress ->
        if (duress) {
            emptyList()
        } else if (query.isBlank()) {
            list
        } else {
            list.filter {
                it.contact.name.contains(query, ignoreCase = true) ||
                it.contact.phoneNumber.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ledgerSummary: StateFlow<LedgerSummary> = combine(
        repository.ledgerSummary,
        _isDuressMode
    ) { summary, duress ->
        if (duress) LedgerSummary() else summary
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LedgerSummary())

    val allExpenses: StateFlow<List<PersonalExpense>> = combine(
        repository.allExpenses,
        _isDuressMode
    ) { list, duress ->
        if (duress) emptyList() else list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cashFlowSummary: StateFlow<CashFlowSummary> = combine(
        repository.cashFlowSummary,
        _isDuressMode
    ) { summary, duress ->
        if (duress) CashFlowSummary() else summary
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CashFlowSummary())

    // Transactions for selected contact
    private val _currentContactTransactions = MutableStateFlow<List<LedgerTransaction>>(emptyList())
    val currentContactTransactions: StateFlow<List<LedgerTransaction>> = _currentContactTransactions.asStateFlow()

    fun selectContact(contactId: Long?) {
        _selectedContactId.value = contactId
        if (contactId != null) {
            viewModelScope.launch {
                repository.getTransactionsForContact(contactId).collect {
                    _currentContactTransactions.value = it
                }
            }
        } else {
            _currentContactTransactions.value = emptyList()
        }
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _ledgerSearchQuery.value = query
    }

    // --- SECURITY & LOCK ACTIONS ---
    fun unlockWithPin(enteredPin: String): UnlockResult {
        return when {
            enteredPin == securityPrefs.pin -> {
                _isDuressMode.value = false
                _isLocked.value = false
                securityPrefs.lastUnlockedTime = System.currentTimeMillis()
                UnlockResult.SUCCESS
            }
            enteredPin == securityPrefs.duressPin -> {
                _isDuressMode.value = true
                _isLocked.value = false
                UnlockResult.DURESS
            }
            else -> UnlockResult.WRONG_PIN
        }
    }

    fun unlockWithBiometrics() {
        _isDuressMode.value = false
        _isLocked.value = false
        securityPrefs.lastUnlockedTime = System.currentTimeMillis()
    }

    fun lockApp() {
        _isLocked.value = true
    }

    fun updateMasterPin(newPin: String) {
        securityPrefs.pin = newPin
    }

    fun updateDuressPin(newPin: String) {
        securityPrefs.duressPin = newPin
    }

    fun setBiometricEnabled(enabled: Boolean) {
        securityPrefs.isBiometricEnabled = enabled
    }

    fun setSecurityEnabled(enabled: Boolean) {
        securityPrefs.isSecurityEnabled = enabled
        if (!enabled) {
            _isLocked.value = false
        }
    }

    fun setCurrency(symbol: String) {
        securityPrefs.currencySymbol = symbol
        _currencySymbol.value = symbol
    }

    // --- CONTACT & LEDGER ACTIONS ---
    fun addContact(
        name: String,
        phoneNumber: String,
        alias: String = "",
        notes: String = "",
        isAutoSmsEnabled: Boolean = false,
        autoSmsDaysInterval: Int = 3
    ) {
        viewModelScope.launch {
            repository.insertContact(
                ContactLedger(
                    name = name.trim(),
                    phoneNumber = phoneNumber.trim(),
                    alias = alias.trim(),
                    notes = notes.trim(),
                    isAutoSmsEnabled = false,
                    autoSmsDaysInterval = 3
                )
            )
        }
    }

    fun updateContact(contact: ContactLedger) {
        viewModelScope.launch {
            repository.updateContact(contact)
        }
    }

    fun deleteContact(contact: ContactLedger) {
        viewModelScope.launch {
            repository.deleteContact(contact)
            if (_selectedContactId.value == contact.id) {
                selectContact(null)
            }
        }
    }

    /**
     * Add a transaction (Give or Receive).
     * Works any time, including after an account was settled!
     */
    fun addTransaction(
        contactId: Long,
        amount: Double,
        type: String, // "GAVE" or "RECEIVED"
        description: String,
        dueDate: Long? = null,
        timestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            repository.insertTransaction(
                LedgerTransaction(
                    contactId = contactId,
                    amount = amount,
                    type = type,
                    description = description.trim(),
                    dueDate = dueDate,
                    isSettled = false,
                    timestamp = timestamp
                )
            )
        }
    }

    fun deleteTransaction(transaction: LedgerTransaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    /**
     * Settle full balance with contact:
     * Adds a settlement record so net balance becomes $0.00.
     * User can immediately give money again after settled!
     */
    fun settleContact(contactId: Long, currentNet: Double) {
        viewModelScope.launch {
            repository.settleContact(contactId, currentNet)
        }
    }

    // --- EXPENSES ACTIONS ---
    fun addExpense(
        amount: Double,
        type: String, // "EXPENSE" or "INCOME"
        category: String,
        title: String,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.insertExpense(
                PersonalExpense(
                    amount = amount,
                    type = type,
                    category = category,
                    title = title.trim(),
                    notes = notes.trim()
                )
            )
        }
    }

    fun deleteExpense(expense: PersonalExpense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    // --- ZERO TRACE / CLEAR ALL DATA ---
    fun executeBurnNoticeWipe() {
        viewModelScope.launch {
            repository.burnAndWipeAll()
            securityPrefs.resetSecurity()
            _isDuressMode.value = false
            _isLocked.value = true
            _selectedContactId.value = null
        }
    }
}
