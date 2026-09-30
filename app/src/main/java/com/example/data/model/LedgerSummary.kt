package com.example.data.model

data class ContactWithBalance(
    val contact: ContactLedger,
    val totalGave: Double = 0.0,      // Money you gave this person
    val totalReceived: Double = 0.0,  // Money you received from this person (repayments or borrowed)
    val netBalance: Double = 0.0,     // Positive: You will get. Negative: You will give. 0: Settled
    val isSettled: Boolean = false,
    val lastTransactionTime: Long = 0L
)

data class LedgerSummary(
    val totalIGave: Double = 0.0,       // Total money I gave
    val totalIReceived: Double = 0.0,   // Total money I received
    val totalYouWillGet: Double = 0.0,  // Total I will get back
    val totalYouWillGive: Double = 0.0, // Total I will give / pay
    val netLedgerBalance: Double = 0.0, // totalYouWillGet - totalYouWillGive
    val activeCount: Int = 0,
    val settledCount: Int = 0
)

data class CashFlowSummary(
    val totalCashIn: Double = 0.0,
    val totalCashOut: Double = 0.0,
    val netAvailableCash: Double = 0.0,
    val topExpenseCategory: String = "None",
    val todayCashOut: Double = 0.0,
    val thisMonthCashOut: Double = 0.0
)
