package com.example.data.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.db.BlackBookDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SmsReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val contactId = intent.getLongExtra("contact_id", -1L)
        val contactName = intent.getStringExtra("contact_name") ?: "Contact"
        val phoneNumber = intent.getStringExtra("phone_number") ?: ""
        val message = intent.getStringExtra("message") ?: "BlackBook Reminder: Settle outstanding balance."

        Log.d("BlackBook_SMS", "Automated trigger firing for $contactName ($phoneNumber)")

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val db = BlackBookDatabase.getDatabase(context, scope)
            // Try direct SMS if permission is available
            val sent = SmsHelper.sendDirectSms(context, phoneNumber, message)

            if (sent) {
                if (contactId != -1L) {
                    db.contactLedgerDao().updateLastReminderSent(contactId, System.currentTimeMillis())
                }
                showNotification(
                    context,
                    "Automated SMS Dispatched",
                    "Sent payment reminder to $contactName ($phoneNumber)"
                )
            } else {
                // Post notification so user can tap to dispatch via WhatsApp or SMS in 1 tap
                showActionNotification(
                    context,
                    contactName,
                    phoneNumber,
                    message
                )
            }
        }
    }

    private fun showNotification(context: Context, title: String, content: String) {
        val channelId = "blackbook_automated_triggers"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "BlackBook Automated Triggers",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Automated local debt collection alerts"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), notification)
    }

    private fun showActionNotification(
        context: Context,
        contactName: String,
        phoneNumber: String,
        message: String
    ) {
        val channelId = "blackbook_overdue_alerts"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Overdue Debt Notices",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle("Collection Due: $contactName")
            .setContentText("Tap to dispatch reminder message: $message")
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), notification)
    }
}
