package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ContactLedger
import com.example.data.model.LedgerTransaction
import com.example.data.model.MessageTemplate
import com.example.data.model.PersonalExpense
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PersonalExpense::class,
        ContactLedger::class,
        LedgerTransaction::class,
        MessageTemplate::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BlackBookDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun contactLedgerDao(): ContactLedgerDao
    abstract fun ledgerTransactionDao(): LedgerTransactionDao
    abstract fun messageTemplateDao(): MessageTemplateDao

    companion object {
        @Volatile
        private var INSTANCE: BlackBookDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): BlackBookDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BlackBookDatabase::class.java,
                    "blackbook_vault.db"
                )
                    .addCallback(BlackBookDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class BlackBookDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(database: BlackBookDatabase) {
            val templateDao = database.messageTemplateDao()
            val initialTemplates = listOf(
                MessageTemplate(
                    title = "Cold Ledger Notice",
                    body = "BlackBook Notice: [name], this is an official ledger notice. You have an outstanding balance of [amount]. Please remit payment promptly.",
                    category = "REMINDER",
                    tone = "FIRM"
                ),
                MessageTemplate(
                    title = "Discreet / Tactful",
                    body = "Hey [name], quick reminder regarding the [amount] from [due_date]. Let me know when you can settle the balance. Thanks.",
                    category = "REMINDER",
                    tone = "TACTFUL"
                ),
                MessageTemplate(
                    title = "Zero Mercy (Final Demand)",
                    body = "BlackBook Urgent: [name], your balance of [amount] is critically past due. Uncollected funds will not be tolerated. Remit payment today.",
                    category = "URGENT",
                    tone = "ZERO_MERCY"
                ),
                MessageTemplate(
                    title = "Payment Clearance Receipt",
                    body = "BlackBook Receipt: Confirmed payment received from [name]. Your current net balance is [net_balance]. Ledger updated and secured.",
                    category = "RECEIPT",
                    tone = "CLEARANCE"
                ),
                MessageTemplate(
                    title = "Short Code Call-Out",
                    body = "[name]: Pending ledger obligation: [amount]. Confirm remittance once sent.",
                    category = "REMINDER",
                    tone = "FIRM"
                )
            )
            templateDao.insertAll(initialTemplates)

            // Seed with realistic initial data to show off the capabilities immediately
            val contactDao = database.contactLedgerDao()
            val transactionDao = database.ledgerTransactionDao()
            val expenseDao = database.expenseDao()

            val contact1Id = contactDao.insertContact(
                ContactLedger(
                    name = "Marcus Vance",
                    phoneNumber = "+12025550143",
                    alias = "The Broker",
                    notes = "Owes from collateral trade. High reliability.",
                    isAutoSmsEnabled = true,
                    autoSmsDaysInterval = 3
                )
            )

            val contact2Id = contactDao.insertContact(
                ContactLedger(
                    name = "Sara Lin",
                    phoneNumber = "+12025550188",
                    alias = "Wholesale",
                    notes = "Supplied inventory cash. We owe her.",
                    isAutoSmsEnabled = false,
                    autoSmsDaysInterval = 7
                )
            )

            val contact3Id = contactDao.insertContact(
                ContactLedger(
                    name = "Viktor Reznov",
                    phoneNumber = "+12025550199",
                    alias = "Logistics",
                    notes = "Emergency cash loan for fleet fuel.",
                    isAutoSmsEnabled = true,
                    autoSmsDaysInterval = 1
                )
            )

            val now = System.currentTimeMillis()
            val oneDay = 86_400_000L

            // Marcus owes us $450
            transactionDao.insert(
                LedgerTransaction(
                    contactId = contact1Id,
                    amount = 450.0,
                    type = "GAVE",
                    description = "Bridge capital for contract sign-off",
                    dueDate = now + (2 * oneDay),
                    timestamp = now - (3 * oneDay)
                )
            )

            // We borrowed $120 from Sara
            transactionDao.insert(
                LedgerTransaction(
                    contactId = contact2Id,
                    amount = 120.0,
                    type = "TOOK",
                    description = "Short-term float for shipping fee",
                    dueDate = now + (5 * oneDay),
                    timestamp = now - (1 * oneDay)
                )
            )

            // Viktor owes us $850 (overdue!)
            transactionDao.insert(
                LedgerTransaction(
                    contactId = contact3Id,
                    amount = 850.0,
                    type = "GAVE",
                    description = "Urgent diesel & clearance cash",
                    dueDate = now - (2 * oneDay), // past due!
                    timestamp = now - (6 * oneDay)
                )
            )

            // Personal Expenses & Incomes
            expenseDao.insert(
                PersonalExpense(
                    amount = 3200.0,
                    type = "INCOME",
                    category = "Operations",
                    title = "Cash Liquidation Batch #4",
                    notes = "Physical cash reserve added to safe",
                    timestamp = now - (4 * oneDay)
                )
            )

            expenseDao.insert(
                PersonalExpense(
                    amount = 65.0,
                    type = "EXPENSE",
                    category = "Fuel",
                    title = "Shell Diesel Full Tank",
                    notes = "Cash payment",
                    timestamp = now - (2 * oneDay)
                )
            )

            expenseDao.insert(
                PersonalExpense(
                    amount = 180.0,
                    type = "EXPENSE",
                    category = "Bills",
                    title = "Off-grid Starlink / Satellite Comms",
                    notes = "Monthly private line",
                    timestamp = now - (1 * oneDay)
                )
            )

            expenseDao.insert(
                PersonalExpense(
                    amount = 45.0,
                    type = "EXPENSE",
                    category = "Food",
                    title = "Diner & Safehouse Rations",
                    notes = "Cash paid",
                    timestamp = now - (5 * 3600_000L)
                )
            )
        }
    }
}
