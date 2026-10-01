package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.SecurityPreferences
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PaySlipTemplateDialog(
    securityPrefs: SecurityPreferences,
    onDismiss: () -> Unit,
    onSaved: () -> Unit = {}
) {
    val context = LocalContext.current

    var businessName by remember { mutableStateOf(securityPrefs.slipBusinessName) }
    var headerTitle by remember { mutableStateOf(securityPrefs.slipHeaderTitle) }
    var customNote by remember { mutableStateOf(securityPrefs.slipNote) }
    var captionTemplate by remember { mutableStateOf(securityPrefs.slipCaption) }

    // Online Payment optional fields
    var bankName by remember { mutableStateOf(securityPrefs.slipBankName) }
    var accountNo by remember { mutableStateOf(securityPrefs.slipAccountNo) }
    var accountTitle by remember { mutableStateOf(securityPrefs.slipAccountTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Pay Slip Template",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Customize slip design & online pay info",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section 1: Slip Branding & Details
                Text(
                    text = "SLIP DETAILS",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldAccent,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                OutlinedTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = { Text("Business / App Name") },
                    placeholder = { Text("e.g. BLACKBOOK or Shop Name") },
                    leadingIcon = {
                        Icon(Icons.Default.Business, contentDescription = null, tint = TextMuted)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("slip_business_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = GoldAccent,
                        unfocusedLabelColor = TextSecondary,
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = headerTitle,
                    onValueChange = { headerTitle = it },
                    label = { Text("Receipt Title") },
                    placeholder = { Text("e.g. PAYMENT RECEIPT SLIP") },
                    leadingIcon = {
                        Icon(Icons.Default.Description, contentDescription = null, tint = TextMuted)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("slip_header_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = GoldAccent,
                        unfocusedLabelColor = TextSecondary,
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = customNote,
                    onValueChange = { customNote = it },
                    label = { Text("Custom Slip Note (Optional)") },
                    placeholder = { Text("e.g. Please clear the pending balance soon.") },
                    leadingIcon = {
                        Icon(Icons.Default.EditNote, contentDescription = null, tint = TextMuted)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("slip_note_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = GoldAccent,
                        unfocusedLabelColor = TextSecondary,
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated
                    ),
                    maxLines = 2
                )

                // Section 2: Online Payment Optional Fields
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ONLINE PAYMENT DETAILS (OPTIONAL)",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldGreen,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Text(
                            text = "When filled, your bank details will appear directly on the payment slip and message for fast online transfers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        // Bank Name
                        OutlinedTextField(
                            value = bankName,
                            onValueChange = { bankName = it },
                            label = { Text("Bank Name") },
                            placeholder = { Text("e.g. Meezan Bank, Chase, HBL") },
                            leadingIcon = {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = EmeraldGreen)
                            },
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("slip_bank_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldGreen,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedLabelColor = EmeraldGreen,
                                unfocusedLabelColor = TextSecondary,
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface
                            ),
                            singleLine = true
                        )

                        // Account Title / User Name
                        OutlinedTextField(
                            value = accountTitle,
                            onValueChange = { accountTitle = it },
                            label = { Text("Account Title / User Name") },
                            placeholder = { Text("e.g. Muhammad Ammar") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldGreen)
                            },
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("slip_account_title_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldGreen,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedLabelColor = EmeraldGreen,
                                unfocusedLabelColor = TextSecondary,
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface
                            ),
                            singleLine = true
                        )

                        // Account No / IBAN
                        OutlinedTextField(
                            value = accountNo,
                            onValueChange = { accountNo = it },
                            label = { Text("Account No / IBAN") },
                            placeholder = { Text("e.g. 01020304050607 or PK00...") },
                            leadingIcon = {
                                Icon(Icons.Default.CreditCard, contentDescription = null, tint = EmeraldGreen)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("slip_account_no_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldGreen,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedLabelColor = EmeraldGreen,
                                unfocusedLabelColor = TextSecondary,
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface
                            ),
                            singleLine = true
                        )
                    }
                }

                // Section 3: Caption Template
                Text(
                    text = "MESSAGE CAPTION",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldAccent,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                OutlinedTextField(
                    value = captionTemplate,
                    onValueChange = { captionTemplate = it },
                    label = { Text("Share Caption Template") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("slip_caption_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = GoldAccent,
                        unfocusedLabelColor = TextSecondary,
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated
                    ),
                    maxLines = 3
                )

                Text(
                    text = "Placeholders: {name}, {amount}, {bank_name}, {account_no}, {user_name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    securityPrefs.slipBusinessName = businessName.trim().ifBlank { SecurityPreferences.DEFAULT_SLIP_BUSINESS_NAME }
                    securityPrefs.slipHeaderTitle = headerTitle.trim().ifBlank { SecurityPreferences.DEFAULT_SLIP_HEADER_TITLE }
                    securityPrefs.slipNote = customNote.trim()
                    securityPrefs.slipCaption = captionTemplate.trim().ifBlank { SecurityPreferences.DEFAULT_SLIP_CAPTION }
                    securityPrefs.slipBankName = bankName.trim()
                    securityPrefs.slipAccountNo = accountNo.trim()
                    securityPrefs.slipAccountTitle = accountTitle.trim()

                    Toast.makeText(context, "Pay slip template & bank details saved", Toast.LENGTH_SHORT).show()
                    onSaved()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldAccent,
                    contentColor = DarkBackground
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("save_slip_template_btn")
            ) {
                Text("SAVE TEMPLATE", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = {
                        businessName = SecurityPreferences.DEFAULT_SLIP_BUSINESS_NAME
                        headerTitle = SecurityPreferences.DEFAULT_SLIP_HEADER_TITLE
                        customNote = SecurityPreferences.DEFAULT_SLIP_NOTE
                        captionTemplate = SecurityPreferences.DEFAULT_SLIP_CAPTION
                        bankName = ""
                        accountNo = ""
                        accountTitle = ""
                    }
                ) {
                    Text("Reset", color = TextMuted)
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        }
    )
}
