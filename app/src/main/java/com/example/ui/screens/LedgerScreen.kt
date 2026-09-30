package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import com.example.data.model.ContactWithBalance
import com.example.data.model.LedgerSummary
import com.example.data.receiver.SmsHelper
import com.example.ui.components.AddContactDialog
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
import java.util.Locale

@Composable
fun LedgerScreen(
    contacts: List<ContactWithBalance>,
    ledgerSummary: LedgerSummary,
    currencySymbol: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onContactClick: (Long) -> Unit,
    onAddContact: (String, String, String, String, Boolean, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddContactDialog by remember { mutableStateOf(false) }
    var activeFilter by remember { mutableStateOf("ALL") } // "ALL", "WILL_GET", "WILL_GIVE", "SETTLED"
    var reminderContact by remember { mutableStateOf<ContactWithBalance?>(null) }

    val filteredContacts = remember(contacts, activeFilter) {
        when (activeFilter) {
            "WILL_GET" -> contacts.filter { it.netBalance > 0.009 }
            "WILL_GIVE" -> contacts.filter { it.netBalance < -0.009 }
            "SETTLED" -> contacts.filter { it.isSettled }
            else -> contacts
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Home Summary Card: "Total I Give & Received"
            item {
                TotalGiveAndReceivedCard(
                    summary = ledgerSummary,
                    currencySymbol = currencySymbol
                )
            }

            // Search Bar & Filter Chips in Simple English
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ledger_search_field"),
                        placeholder = {
                            Text(
                                "Search by name or phone...",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Simple English filter chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Pair("ALL", "All (${contacts.size})"),
                            Pair("WILL_GET", "Will Get"),
                            Pair("WILL_GIVE", "Will Give"),
                            Pair("SETTLED", "Settled")
                        ).forEach { (tag, label) ->
                            val isSelected = activeFilter == tag
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) GoldAccent else DarkSurface)
                                    .border(
                                        1.dp,
                                        if (isSelected) GoldAccent else DarkBorder,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { activeFilter = tag }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) DarkBackground else TextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // People List
            if (filteredContacts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No people found",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap '+ Add Person' to start tracking",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else {
                items(filteredContacts, key = { it.contact.id }) { item ->
                    PersonLedgerCard(
                        item = item,
                        currencySymbol = currencySymbol,
                        onClick = { onContactClick(item.contact.id) },
                        onWhatsAppClick = {
                            val amountStr = "$currencySymbol${String.format(Locale.US, "%.2f", kotlin.math.abs(item.netBalance))}"
                            val msg = "Hi ${item.contact.name}, friendly reminder that $amountStr is pending. Please pay when you can. Thank you!"
                            SmsHelper.openWhatsApp(context, item.contact.phoneNumber, msg)
                        },
                        onSmsClick = {
                            val amountStr = "$currencySymbol${String.format(Locale.US, "%.2f", kotlin.math.abs(item.netBalance))}"
                            val msg = "Hi ${item.contact.name}, friendly reminder that $amountStr is pending. Please pay when you can. Thank you!"
                            SmsHelper.openDefaultSms(context, item.contact.phoneNumber, msg)
                        },
                        onOpenReminder = {
                            reminderContact = item
                        }
                    )
                }
            }
        }

        // Add Person FAB
        FloatingActionButton(
            onClick = { showAddContactDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp, end = 20.dp)
                .testTag("add_contact_fab"),
            containerColor = GoldAccent,
            contentColor = DarkBackground,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Person")
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ADD PERSON",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        if (showAddContactDialog) {
            AddContactDialog(
                onDismiss = { showAddContactDialog = false },
                onConfirm = { name, phone, alias, notes, autoSms, days ->
                    onAddContact(name, phone, alias, notes, autoSms, days)
                    showAddContactDialog = false
                }
            )
        }

        reminderContact?.let { c ->
            SendReminderDialog(
                contactName = c.contact.name,
                phoneNumber = c.contact.phoneNumber,
                amount = kotlin.math.abs(c.netBalance),
                currencySymbol = currencySymbol,
                onDismiss = { reminderContact = null }
            )
        }
    }
}

@Composable
private fun TotalGiveAndReceivedCard(
    summary: LedgerSummary,
    currencySymbol: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        // Prominent Total I Give and Received Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Total I Gave
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "TOTAL I GAVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$currencySymbol${String.format(Locale.US, "%.2f", summary.totalIGave)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = EmeraldGreen,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Total I Received
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "TOTAL RECEIVED",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$currencySymbol${String.format(Locale.US, "%.2f", summary.totalIReceived)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = GoldAccent,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Net Pending Status Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(DarkSurfaceElevated)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Pending to Get",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Text(
                    text = "$currencySymbol${String.format(Locale.US, "%.2f", summary.totalYouWillGet)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = EmeraldGreen,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(text = "•", color = TextMuted)

            Column {
                Text(
                    text = "Pending to Give",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Text(
                    text = "$currencySymbol${String.format(Locale.US, "%.2f", summary.totalYouWillGive)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = CrimsonRed,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(text = "•", color = TextMuted)

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Settled",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Text(
                    text = "${summary.settledCount} people",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PersonLedgerCard(
    item: ContactWithBalance,
    currencySymbol: String,
    onClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onSmsClick: () -> Unit,
    onOpenReminder: () -> Unit
) {
    val net = item.netBalance
    val isYouWillGet = net > 0.009
    val isYouWillGive = net < -0.009
    val isSettled = item.isSettled || (!isYouWillGet && !isYouWillGive)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(14.dp)
            .testTag("contact_item_${item.contact.id}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceElevated)
                            .border(
                                1.dp,
                                if (isSettled) TextMuted else if (isYouWillGet) EmeraldGreen else CrimsonRed,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.contact.name.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = item.contact.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        if (item.contact.phoneNumber.isNotBlank()) {
                            Text(
                                text = item.contact.phoneNumber,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Balance status badge in clear, simple English
                Column(horizontalAlignment = Alignment.End) {
                    if (isSettled) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Settled",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "$currencySymbol 0.00",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (isYouWillGet) {
                        Text(
                            text = "You will get",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldGreen,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$currencySymbol${String.format(Locale.US, "%.2f", net)}",
                            style = MaterialTheme.typography.titleLarge,
                            color = EmeraldGreen,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        )
                    } else {
                        Text(
                            text = "You will give",
                            style = MaterialTheme.typography.labelSmall,
                            color = CrimsonRed,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$currencySymbol${String.format(Locale.US, "%.2f", kotlin.math.abs(net))}",
                            style = MaterialTheme.typography.titleLarge,
                            color = CrimsonRed,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action row: Only 2 options (WhatsApp and SMS)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gave: $currencySymbol${String.format(Locale.US, "%.2f", item.totalGave)}  •  Got: $currencySymbol${String.format(Locale.US, "%.2f", item.totalReceived)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    fontSize = 11.sp
                )

                if (isYouWillGet && item.contact.phoneNumber.isNotBlank()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // WhatsApp option
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, WhatsAppGreen.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .clickable { onWhatsAppClick() }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("whatsapp_btn_${item.contact.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Chat,
                                    contentDescription = "WhatsApp",
                                    tint = WhatsAppGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "WhatsApp",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WhatsAppGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // SMS option
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .clickable { onSmsClick() }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("sms_btn_${item.contact.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Sms,
                                    contentDescription = "SMS",
                                    tint = GoldAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "SMS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
