package com.example.data.repository

import com.example.data.db.BlackBookDatabase
import com.example.data.model.ContactLedger
import com.example.data.model.ContactWithBalance
import com.example.data.model.LedgerSummary
import com.example.data.model.LedgerTransaction
import com.example.data.model.PersonalExpense
import com.example.data.model.CashFlowSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class BlackBookRepository(
    private val database: BlackBookDatabase
) {
    private val expenseDao = database.expenseDao()
    private val contactDao = database.contactLedgerDao()
    private val transactionDao = database.ledgerTransactionDao()
    private val templateDao = database.messageTemplateDao()

    // --- EXPENSES & CASH FLOW ---
    val allExpenses: Flow<List<PersonalExpense>> = expenseDao.getAllExpenses()

    val cashFlowSummary: Flow<CashFlowSummary> = allExpenses.map { expenses ->
        var totalIn = 0.0
        var totalOut = 0.0
        var todayOut = 0.0
        var thisMonthOut = 0.0
        val categoryTotals = mutableMapOf<String, Double>()

        val now = System.currentTimeMillis()
        val oneDayAgo = now - 86_400_000L
        val thirtyDaysAgo = now - (30L * 86_400_000L)

        for (e in expenses) {
            if (e.type == "INCOME") {
                totalIn += e.amount
            } else {
                totalOut += e.amount
                categoryTotals[e.category] = (categoryTotals[e.category] ?: 0.0) + e.amount
                if (e.timestamp >= oneDayAgo) {
                    todayOut += e.amount
                }
                if (e.timestamp >= thirtyDaysAgo) {
                    thisMonthOut += e.amount
                }
            }
        }

        val topCategory = categoryTotals.maxByOrNull { it.value }?.key ?: "None"

        CashFlowSummary(
            totalCashIn = totalIn,
            totalCashOut = totalOut,
            netAvailableCash = totalIn - totalOut,
            topExpenseCategory = topCategory,
            todayCashOut = todayOut,
            thisMonthCashOut = thisMonthOut
        )
    }

    suspend fun insertExpense(expense: PersonalExpense) = expenseDao.insert(expense)
    suspend fun deleteExpense(expense: PersonalExpense) = expenseDao.delete(expense)

    // --- CONTACTS & DEBTS LEDGER ---
    val allContacts: Flow<List<ContactLedger>> = contactDao.getAllContacts()
    val allTransactions: Flow<List<LedgerTransaction>> = transactionDao.getAllTransactions()

    val contactsWithBalances: Flow<List<ContactWithBalance>> = combine(
        allContacts,
        allTransactions
    ) { contacts, transactions ->
        contacts.map { contact ->
            val contactTxns = transactions.filter { it.contactId == contact.id }

            var gave = 0.0
            var received = 0.0
            var took = 0.0
            var lastTxnTime = contact.createdAt

            for (txn in contactTxns) {
                if (txn.timestamp > lastTxnTime) {
                    lastTxnTime = txn.timestamp
                }
                when (txn.type) {
                    "GAVE" -> gave += txn.amount
                    "RECEIVED", "SETTLED" -> received += txn.amount
                    "TOOK" -> took += txn.amount
                }
            }

            // Net balance:
            // Gave $100, Got $40 back -> Net = +$60 (You will get $60)
            // Took $50 -> Net = -$50 (You will give $50)
            val net = gave - received - took
            val isSettled = kotlin.math.abs(net) < 0.01

            ContactWithBalance(
                contact = contact,
                totalGave = gave,
                totalReceived = received + took,
                netBalance = net,
                isSettled = isSettled,
                lastTransactionTime = lastTxnTime
            )
        }.sortedByDescending { it.lastTransactionTime }
    }

    val ledgerSummary: Flow<LedgerSummary> = combine(
        allTransactions,
        contactsWithBalances
    ) { transactions, contactList ->
        var totalGave = 0.0
        var totalReceived = 0.0

        for (txn in transactions) {
            when (txn.type) {
                "GAVE" -> totalGave += txn.amount
                "RECEIVED", "SETTLED" -> totalReceived += txn.amount
                "TOOK" -> totalReceived += txn.amount
            }
        }

        var willGet = 0.0
        var willGive = 0.0
        var active = 0
        var settled = 0

        for (c in contactList) {
            if (c.netBalance > 0.009) {
                willGet += c.netBalance
                active++
            } else if (c.netBalance < -0.009) {
                willGive += (-c.netBalance)
                active++
            } else {
                settled++
            }
        }

        LedgerSummary(
            totalIGave = totalGave,
            totalIReceived = totalReceived,
            totalYouWillGet = willGet,
            totalYouWillGive = willGive,
            netLedgerBalance = willGet - willGive,
            activeCount = active,
            settledCount = settled
        )
    }

    fun getTransactionsForContact(contactId: Long): Flow<List<LedgerTransaction>> {
        return transactionDao.getTransactionsForContact(contactId)
    }

    suspend fun insertContact(contact: ContactLedger): Long = contactDao.insertContact(contact)
    suspend fun updateContact(contact: ContactLedger) = contactDao.updateContact(contact)
    suspend fun deleteContact(contact: ContactLedger) = contactDao.deleteContact(contact)

    suspend fun insertTransaction(transaction: LedgerTransaction): Long =
        transactionDao.insert(transaction)

    suspend fun updateTransaction(transaction: LedgerTransaction) =
        transactionDao.update(transaction)

    suspend fun deleteTransaction(transaction: LedgerTransaction) =
        transactionDao.delete(transaction)

    /**
     * Settle full balance with contact:
     * Inserts a settlement transaction to bring the net balance to $0.00.
     * The contact remains fully active so you can immediately give or take money anytime after!
     */
    suspend fun settleContact(contactId: Long, currentNet: Double) {
        if (kotlin.math.abs(currentNet) < 0.01) return
        val now = System.currentTimeMillis()
        if (currentNet > 0) {
            // Person owed you $X, they paid you back to settle
            transactionDao.insert(
                LedgerTransaction(
                    contactId = contactId,
                    amount = currentNet,
                    type = "RECEIVED",
                    description = "Account Settled (Got Money Back)",
                    timestamp = now,
                    isSettled = true
                )
            )
        } else {
            // You owed person $X, you paid them back to settle
            transactionDao.insert(
                LedgerTransaction(
                    contactId = contactId,
                    amount = -currentNet,
                    type = "GAVE",
                    description = "Account Settled (Paid Back)",
                    timestamp = now,
                    isSettled = true
                )
            )
        }
    }

    // --- ZERO TRACE / CLEAR DATA ---
    suspend fun burnAndWipeAll() {
        expenseDao.deleteAll()
        transactionDao.deleteAll()
        contactDao.deleteAll()
        templateDao.deleteAll()
    }
}
