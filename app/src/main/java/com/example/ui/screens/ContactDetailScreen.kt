package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContactLedger
import com.example.data.model.ContactWithBalance
import com.example.data.model.LedgerTransaction
import com.example.data.receiver.PaymentSlipGenerator
import com.example.data.receiver.SmsHelper
import com.example.data.repository.SecurityPreferences
import com.example.ui.components.AddTransactionDialog
import com.example.ui.components.SendReminderDialog
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WhatsAppGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ContactDetailScreen(
    contactWithBalance: ContactWithBalance?,
    transactions: List<LedgerTransaction>,
    currencySymbol: String,
    onBack: () -> Unit,
    onAddTransaction: (amount: Double, type: String, description: String, dueDate: Long?, timestamp: Long) -> Unit,
    onDeleteTransaction: (LedgerTransaction) -> Unit,
    onSettleAll: () -> Unit,
    onDeleteContact: (ContactLedger) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val securityPrefs = remember { SecurityPreferences(context) }

    if (contactWithBalance == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            Text("Person not found", color = TextSecondary)
        }
        return
    }

    val contact = contactWithBalance.contact
    val net = contactWithBalance.netBalance
    val isYouWillGet = net > 0.009
    val isYouWillGive = net < -0.009
    val isSettled = contactWithBalance.isSettled || (!isYouWillGet && !isYouWillGive)

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf("GAVE") }
    var showReminderDialog by remember { mutableStateOf(false) }
    var showSettleConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteContactDialog by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault()) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 540.dp),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("contact_detail_back")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = contact.name,
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            if (contact.phoneNumber.isNotBlank()) {
                                Text(
                                    text = contact.phoneNumber,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (contact.phoneNumber.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    val amountStr = "$currencySymbol${String.format(Locale.US, "%.2f", kotlin.math.abs(net))}"
                                    val msg = if (isYouWillGet) {
                                        securityPrefs.formatTemplate(
                                            securityPrefs.smsTemplate,
                                            contact.name,
                                            amountStr
                                        )
                                    } else if (isYouWillGive) {
                                        "Hi ${contact.name}, ledger update: your balance is $amountStr. Thank you!"
                                    } else {
                                        "Hi ${contact.name}, your account balance is settled ($currencySymbol 0.00). Thank you!"
                                    }
                                    SmsHelper.openDefaultSms(context, contact.phoneNumber, msg)
                                },
                                modifier = Modifier.testTag("contact_detail_sms_header_btn")
                            ) {
                                Icon(
                                    Icons.Default.Sms,
                                    contentDescription = "Send SMS",
                                    tint = GoldAccent
                                )
                            }
                        }

                        IconButton(
                            onClick = { showDeleteContactDialog = true },
                            modifier = Modifier.testTag("delete_contact_button")
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = CrimsonRed
                            )
                        }
                    }
                }
            }

            // Current Balance Status Card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .padding(18.dp)
                ) {
                    Text(
                        text = "BALANCE STATUS",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldAccent,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isSettled) "Account Settled" else if (isYouWillGet) "You will get" else "You will give",
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (isSettled) TextSecondary else if (isYouWillGet) EmeraldGreen else CrimsonRed,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$currencySymbol${String.format(Locale.US, "%.2f", if (isSettled) 0.0 else kotlin.math.abs(net))}",
                        style = MaterialTheme.typography.displayLarge,
                        color = if (isSettled) TextMuted else if (isYouWillGet) EmeraldGreen else CrimsonRed,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Total Given: $currencySymbol${String.format(Locale.US, "%.2f", contactWithBalance.totalGave)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        Text(text = "•", color = TextMuted)
                        Text(
                            text = "Total Received: $currencySymbol${String.format(Locale.US, "%.2f", contactWithBalance.totalReceived)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Primary Money Actions: I Gave Money, I Got Money, Settle Account
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // "I Gave Money" button (always available, even after settled!)
                        Button(
                            onClick = {
                                selectedType = "GAVE"
                                showAddDialog = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("contact_detail_gave_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldGreen,
                                contentColor = DarkBackground
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "I GAVE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        // "I Got Money" button (log payment received)
                        Button(
                            onClick = {
                                selectedType = "RECEIVED"
                                showAddDialog = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("contact_detail_took_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldAccent,
                                contentColor = DarkBackground
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "I GOT",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // Settle Button
                    if (!isSettled) {
                        Button(
                            onClick = { showSettleConfirmDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("contact_detail_settle_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkSurfaceElevated,
                                contentColor = TextPrimary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MARK AS SETTLED (CLEAR BALANCE)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Reminders / SMS Section
            if (contact.phoneNumber.isNotBlank()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurface)
                            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = if (isYouWillGet) "SEND PAYMENT REMINDER" else "SEND MESSAGE / SMS",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldAccent,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isYouWillGet) {
                                "Send a quick payment reminder to ${contact.name}:"
                            } else {
                                "Send an SMS or balance update to ${contact.name}:"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. WhatsApp Button
                            Button(
                                onClick = {
                                    val amountStr = "$currencySymbol${String.format(Locale.US, "%.2f", kotlin.math.abs(net))}"
                                    val msg = if (isYouWillGet) {
                                        securityPrefs.formatTemplate(
                                            securityPrefs.whatsappTemplate,
                                            contact.name,
                                            amountStr
                                        )
                                    } else {
                                        "Hi ${contact.name}, your account balance is $amountStr. Thank you!"
                                    }
                                    SmsHelper.openWhatsApp(context, contact.phoneNumber, msg)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("remind_whatsapp_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WhatsAppGreen,
                                    contentColor = DarkBackground
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "WHATSAPP",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            // 2. SMS Button
                            Button(
                                onClick = {
                                    val amountStr = "$currencySymbol${String.format(Locale.US, "%.2f", kotlin.math.abs(net))}"
                                    val msg = if (isYouWillGet) {
                                        securityPrefs.formatTemplate(
                                            securityPrefs.smsTemplate,
                                            contact.name,
                                            amountStr
                                        )
                                    } else if (isYouWillGive) {
                                        "Hi ${contact.name}, ledger update: your balance is $amountStr. Thank you!"
                                    } else {
                                        "Hi ${contact.name}, your account balance is settled ($currencySymbol 0.00). Thank you!"
                                    }
                                    SmsHelper.openDefaultSms(context, contact.phoneNumber, msg)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("remind_sms_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DarkSurfaceElevated,
                                    contentColor = TextPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Sms, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SMS",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 3. Send Payment Slip Image on WhatsApp
                        Button(
                            onClick = {
                                PaymentSlipGenerator.sharePaymentSlip(
                                    context = context,
                                    contactName = contact.name,
                                    phoneNumber = contact.phoneNumber,
                                    amount = net,
                                    currencySymbol = currencySymbol,
                                    targetWhatsAppOnly = true
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("remind_whatsapp_image_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldGreen,
                                contentColor = DarkBackground
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SEND SLIP ON WHATSAPP",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            // Transaction History Section
            item {
                Text(
                    text = "TRANSACTION HISTORY (${transactions.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldAccent,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            if (transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No transactions logged yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                }
            } else {
                items(transactions, key = { it.id }) { txn ->
                    val isGave = txn.type == "GAVE"
                    val isSettledTxn = txn.isSettled

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurface)
                            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isSettledTxn) "SETTLED" else if (isGave) "I GAVE" else "I RECEIVED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSettledTxn) TextMuted else if (isGave) EmeraldGreen else GoldAccent,
                                    fontWeight = FontWeight.Bold
                                )

                                if (txn.description.isNotBlank()) {
                                    Text(
                                        text = txn.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary
                                    )
                                }

                                Text(
                                    text = dateFormat.format(Date(txn.timestamp)),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$currencySymbol${String.format(Locale.US, "%.2f", txn.amount)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = if (isSettledTxn) TextMuted else if (isGave) EmeraldGreen else GoldAccent,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black
                                )

                                IconButton(
                                    onClick = { onDeleteTransaction(txn) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dialogs
        if (showAddDialog) {
            AddTransactionDialog(
                contactName = contact.name,
                initialType = selectedType,
                currencySymbol = currencySymbol,
                onDismiss = { showAddDialog = false },
                onConfirm = { amount, type, desc, dueDate, timestamp ->
                    onAddTransaction(amount, type, desc, dueDate, timestamp)
                    showAddDialog = false
                }
            )
        }

        if (showSettleConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showSettleConfirmDialog = false },
                containerColor = DarkSurface,
                title = {
                    Text(
                        text = "Mark Account Settled?",
                        style = MaterialTheme.typography.titleMedium,
                        color = EmeraldGreen,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "This will clear the pending balance of $currencySymbol${String.format(Locale.US, "%.2f", kotlin.math.abs(net))}. You can still give or receive money anytime after settling.",
                        color = TextPrimary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onSettleAll()
                            showSettleConfirmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = DarkBackground)
                    ) {
                        Text("Confirm Settled", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSettleConfirmDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }

        if (showDeleteContactDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteContactDialog = false },
                containerColor = DarkSurface,
                title = {
                    Text(
                        text = "Delete ${contact.name}?",
                        style = MaterialTheme.typography.titleMedium,
                        color = CrimsonRed,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "This will remove ${contact.name} and all their transaction history.",
                        color = TextPrimary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteContact(contact)
                            showDeleteContactDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                    ) {
                        Text("Delete", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteContactDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }
    }
}
