package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.BlackBookAppBar
import com.example.ui.components.BlackBookBottomBar
import com.example.ui.screens.CashFlowScreen
import com.example.ui.screens.ContactDetailScreen
import com.example.ui.screens.LedgerScreen
import com.example.ui.screens.LockScreen
import com.example.ui.screens.VaultSettingsScreen
import com.example.ui.theme.BlackBookTheme
import com.example.ui.theme.DarkBackground
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.BlackBookViewModel

class MainActivity : FragmentActivity() {

    private val viewModel: BlackBookViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BlackBookTheme {
                val isLocked by viewModel.isLocked.collectAsStateWithLifecycle()
                val isDuressMode by viewModel.isDuressMode.collectAsStateWithLifecycle()
                val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
                val selectedContactId by viewModel.selectedContactId.collectAsStateWithLifecycle()

                val contacts by viewModel.contactsWithBalances.collectAsStateWithLifecycle()
                val ledgerSummary by viewModel.ledgerSummary.collectAsStateWithLifecycle()
                val expenses by viewModel.allExpenses.collectAsStateWithLifecycle()
                val cashFlowSummary by viewModel.cashFlowSummary.collectAsStateWithLifecycle()
                val transactions by viewModel.currentContactTransactions.collectAsStateWithLifecycle()
                val searchQuery by viewModel.ledgerSearchQuery.collectAsStateWithLifecycle()
                val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()

                if (isLocked) {
                    LockScreen(
                        onUnlockWithPin = { pin -> viewModel.unlockWithPin(pin) },
                        onUnlockWithBiometrics = { viewModel.unlockWithBiometrics() },
                        isBiometricEnabled = viewModel.securityPrefs.isBiometricEnabled
                    )
                } else if (selectedContactId != null) {
                    val selectedContactWithBalance = contacts.firstOrNull { it.contact.id == selectedContactId }
                    ContactDetailScreen(
                        contactWithBalance = selectedContactWithBalance,
                        transactions = transactions,
                        currencySymbol = currencySymbol,
                        onBack = { viewModel.selectContact(null) },
                        onAddTransaction = { amount, type, desc, dueDate, timestamp ->
                            viewModel.addTransaction(selectedContactId!!, amount, type, desc, dueDate, timestamp)
                        },
                        onDeleteTransaction = { txn -> viewModel.deleteTransaction(txn) },
                        onSettleAll = {
                            val currentNet = selectedContactWithBalance?.netBalance ?: 0.0
                            viewModel.settleContact(selectedContactId!!, currentNet)
                        },
                        onDeleteContact = { contact -> viewModel.deleteContact(contact) }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = DarkBackground,
                        topBar = {
                            BlackBookAppBar(
                                title = when (currentTab) {
                                    AppTab.LEDGER -> "BlackBook"
                                    AppTab.CASH_FLOW -> "Expenses"
                                    AppTab.VAULT -> "Settings"
                                },
                                isDuressMode = isDuressMode,
                                onLockClick = { viewModel.lockApp() }
                            )
                        },
                        bottomBar = {
                            BlackBookBottomBar(
                                currentTab = currentTab,
                                onTabSelected = { viewModel.setTab(it) }
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .background(DarkBackground),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .widthIn(max = 540.dp)
                            ) {
                                when (currentTab) {
                                    AppTab.LEDGER -> {
                                        LedgerScreen(
                                            contacts = contacts,
                                            ledgerSummary = ledgerSummary,
                                            currencySymbol = currencySymbol,
                                            searchQuery = searchQuery,
                                            onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                            onContactClick = { contactId -> viewModel.selectContact(contactId) },
                                            onAddContact = { name, phone, alias, notes, autoSms, days ->
                                                viewModel.addContact(name, phone, alias, notes, autoSms, days)
                                            }
                                        )
                                    }
                                    AppTab.CASH_FLOW -> {
                                        CashFlowScreen(
                                            expenses = expenses,
                                            summary = cashFlowSummary,
                                            currencySymbol = currencySymbol,
                                            onAddExpense = { amount, type, category, title, notes ->
                                                viewModel.addExpense(amount, type, category, title, notes)
                                            },
                                            onDeleteExpense = { exp -> viewModel.deleteExpense(exp) }
                                        )
                                    }
                                    AppTab.VAULT -> {
                                        VaultSettingsScreen(
                                            securityPrefs = viewModel.securityPrefs,
                                            currentCurrency = currencySymbol,
                                            onUpdateMasterPin = { viewModel.updateMasterPin(it) },
                                            onUpdateDuressPin = { viewModel.updateDuressPin(it) },
                                            onToggleBiometrics = { viewModel.setBiometricEnabled(it) },
                                            onToggleSecurity = { viewModel.setSecurityEnabled(it) },
                                            onSelectCurrency = { viewModel.setCurrency(it) },
                                            onBurnNoticeWipe = { viewModel.executeBurnNoticeWipe() }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.securityPrefs.isSecurityEnabled) {
            viewModel.lockApp()
        }
    }

    override fun onStop() {
        super.onStop()
        if (viewModel.securityPrefs.isSecurityEnabled) {
            viewModel.lockApp()
        }
    }
}
