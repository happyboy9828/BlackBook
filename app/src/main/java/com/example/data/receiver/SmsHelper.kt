package com.example.data.receiver

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.content.ContextCompat

object SmsHelper {

    fun formatDynamicMessage(
        templateBody: String,
        contactName: String,
        amountText: String,
        dueDateText: String,
        netBalanceText: String
    ): String {
        return templateBody
            .replace("[name]", contactName, ignoreCase = true)
            .replace("{name}", contactName, ignoreCase = true)
            .replace("[amount]", amountText, ignoreCase = true)
            .replace("{amount}", amountText, ignoreCase = true)
            .replace("[due_date]", dueDateText, ignoreCase = true)
            .replace("{due_date}", dueDateText, ignoreCase = true)
            .replace("[net_balance]", netBalanceText, ignoreCase = true)
            .replace("{net_balance}", netBalanceText, ignoreCase = true)
    }

    fun cleanPhoneNumber(phone: String): String {
        return phone.replace("[^0-9+]".toRegex(), "")
    }

    /**
     * One-Tap WhatsApp Action:
     * Opens chat with contact pre-filled with the message text.
     */
    fun openWhatsApp(context: Context, phoneNumber: String, message: String) {
        val clean = cleanPhoneNumber(phoneNumber).replace("+", "")
        try {
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$clean&text=${Uri.encode(message)}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                // Secondary fallback
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                    `package` = "com.whatsapp"
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(sendIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "WhatsApp not installed. Launching SMS fallback.", Toast.LENGTH_SHORT).show()
                openDefaultSms(context, phoneNumber, message)
            }
        }
    }

    /**
     * One-Tap Default SMS App Action:
     * Zero-permission, opens default device messaging app with recipient and text pre-filled.
     */
    fun openDefaultSms(context: Context, phoneNumber: String, message: String) {
        try {
            val clean = cleanPhoneNumber(phoneNumber)
            val uri = Uri.parse("smsto:$clean")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", message)
                putExtra(Intent.EXTRA_TEXT, message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open SMS app: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Direct Automated Background SMS sending via SIM card.
     * Uses SmsManager.
     */
    fun sendDirectSms(context: Context, phoneNumber: String, message: String): Boolean {
        return try {
            val clean = cleanPhoneNumber(phoneNumber)
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.SEND_SMS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }

            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            val parts = smsManager.divideMessage(message)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(clean, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(clean, null, message, null, null)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
