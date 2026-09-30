package com.example.data.receiver

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.repository.SecurityPreferences
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PaymentSlipGenerator {

    fun formatPhoneNumberForWhatsApp(phone: String): String {
        var digits = phone.filter { it.isDigit() }
        // Handle Pakistani mobile numbers: 03xx xxx xxxx -> 923xx xxx xxxx
        if (digits.startsWith("03") && digits.length == 11) {
            digits = "92" + digits.substring(1)
        } else if (digits.startsWith("00")) {
            digits = digits.substring(2)
        }
        return digits
    }

    fun generateSlipBitmap(
        context: Context,
        contactName: String,
        phoneNumber: String,
        amount: Double,
        currencySymbol: String,
        businessName: String = "BLACKBOOK",
        headerTitle: String = "PAYMENT RECEIPT SLIP",
        customNote: String = ""
    ): Bitmap {
        val hasNote = customNote.isNotBlank()
        val width = 720
        val height = if (hasNote) 850 else 760
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Deep Noir Background
        val bgPaint = Paint().apply {
            color = Color.parseColor("#090A0D")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Card Box with rounded corners and metallic gold border
        val cardPaint = Paint().apply {
            color = Color.parseColor("#14161E")
            style = Paint.Style.FILL
        }
        val borderPaint = Paint().apply {
            color = Color.parseColor("#D4AF37")
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        val cardRect = RectF(36f, 36f, (width - 36).toFloat(), (height - 36).toFloat())
        canvas.drawRoundRect(cardRect, 28f, 28f, cardPaint)
        canvas.drawRoundRect(cardRect, 28f, 28f, borderPaint)

        // Header Box
        val headerBoxPaint = Paint().apply {
            color = Color.parseColor("#1C1F2B")
            style = Paint.Style.FILL
        }
        val headerRect = RectF(56f, 56f, (width - 56).toFloat(), 210f)
        canvas.drawRoundRect(headerRect, 18f, 18f, headerBoxPaint)

        // Business Name / App Title
        val titlePaint = Paint().apply {
            color = Color.parseColor("#D4AF37")
            textSize = if (businessName.length > 16) 28f else 34f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(businessName.uppercase(Locale.getDefault()), (width / 2).toFloat(), 115f, titlePaint)

        val subtitlePaint = Paint().apply {
            color = Color.parseColor("#00E676")
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(headerTitle.uppercase(Locale.getDefault()), (width / 2).toFloat(), 155f, subtitlePaint)

        val datePaint = Paint().apply {
            color = Color.parseColor("#9CA3AF")
            textSize = 17f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val dateStr = SimpleDateFormat("dd MMM yyyy · hh:mm a", Locale.getDefault()).format(Date())
        canvas.drawText(dateStr, (width / 2).toFloat(), 188f, datePaint)

        // Contact Information
        val textLabelPaint = Paint().apply {
            color = Color.parseColor("#9CA3AF")
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        val textValuePaint = Paint().apply {
            color = Color.parseColor("#FFFFFF")
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        canvas.drawText("Recipient / Contact:", 72f, 275f, textLabelPaint)
        canvas.drawText(contactName, 72f, 315f, textValuePaint)

        if (phoneNumber.isNotBlank()) {
            canvas.drawText("Phone Number:", 72f, 375f, textLabelPaint)
            val phoneValPaint = Paint().apply {
                color = Color.parseColor("#D4AF37")
                textSize = 22f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText(phoneNumber, 72f, 410f, phoneValPaint)
        }

        // Amount Box (Hero Box)
        val amountBoxPaint = Paint().apply {
            color = Color.parseColor("#0D1117")
            style = Paint.Style.FILL
        }
        val amountBorderPaint = Paint().apply {
            color = Color.parseColor("#00E676")
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        val amountRect = RectF(72f, 460f, (width - 72).toFloat(), 640f)
        canvas.drawRoundRect(amountRect, 20f, 20f, amountBoxPaint)
        canvas.drawRoundRect(amountRect, 20f, 20f, amountBorderPaint)

        val amountTitlePaint = Paint().apply {
            color = Color.parseColor("#9CA3AF")
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("PENDING PAYMENT AMOUNT", (width / 2).toFloat(), 510f, amountTitlePaint)

        val amountNumberPaint = Paint().apply {
            color = Color.parseColor("#00E676")
            textSize = 52f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val formattedAmount = "$currencySymbol${String.format(Locale.US, "%,.2f", amount)}"
        canvas.drawText(formattedAmount, (width / 2).toFloat(), 585f, amountNumberPaint)

        // Status Badge Box
        val statusBgPaint = Paint().apply {
            color = Color.parseColor("#2A1215")
            style = Paint.Style.FILL
        }
        val statusRect = RectF(140f, 675f, (width - 140).toFloat(), 735f)
        canvas.drawRoundRect(statusRect, 14f, 14f, statusBgPaint)

        val statusTextPaint = Paint().apply {
            color = Color.parseColor("#FF5252")
            textSize = 20f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("STATUS: PENDING PAYMENT", (width / 2).toFloat(), 712f, statusTextPaint)

        // Optional Custom Note if configured by user
        if (hasNote) {
            val notePaint = Paint().apply {
                color = Color.parseColor("#D4AF37")
                textSize = 17f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText(customNote, (width / 2).toFloat(), 795f, notePaint)
        }

        return bitmap
    }

    fun sharePaymentSlip(
        context: Context,
        contactName: String,
        phoneNumber: String,
        amount: Double,
        currencySymbol: String,
        businessName: String? = null,
        headerTitle: String? = null,
        customNote: String? = null,
        captionTemplate: String? = null,
        targetWhatsAppOnly: Boolean = true
    ) {
        try {
            val securityPrefs = SecurityPreferences(context)
            val finalBusiness = businessName ?: securityPrefs.slipBusinessName
            val finalHeader = headerTitle ?: securityPrefs.slipHeaderTitle
            val finalNote = customNote ?: securityPrefs.slipNote
            val finalCaptionTemplate = captionTemplate ?: securityPrefs.slipCaption

            val bitmap = generateSlipBitmap(
                context = context,
                contactName = contactName,
                phoneNumber = phoneNumber,
                amount = amount,
                currencySymbol = currencySymbol,
                businessName = finalBusiness,
                headerTitle = finalHeader,
                customNote = finalNote
            )

            val cachePath = File(context.cacheDir, "receipts")
            cachePath.mkdirs()
            val file = File(cachePath, "payment_slip_${System.currentTimeMillis()}.png")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val formattedAmount = "$currencySymbol${String.format(Locale.US, "%.2f", amount)}"
            val caption = securityPrefs.formatTemplate(finalCaptionTemplate, contactName, formattedAmount)

            val cleanPhone = formatPhoneNumberForWhatsApp(phoneNumber)
            val jid = if (cleanPhone.isNotBlank()) "$cleanPhone@s.whatsapp.net" else null

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, caption)
                if (jid != null) {
                    putExtra("jid", jid)
                    putExtra(Intent.EXTRA_PHONE_NUMBER, cleanPhone)
                }
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            // Check if WhatsApp or WhatsApp Business is installed
            val pm = context.packageManager
            val isRegularInstalled = try {
                pm.getPackageInfo("com.whatsapp", 0)
                true
            } catch (e: Exception) {
                false
            }

            val isBusinessInstalled = try {
                pm.getPackageInfo("com.whatsapp.w4b", 0)
                true
            } catch (e: Exception) {
                false
            }

            if (targetWhatsAppOnly) {
                if (isRegularInstalled) {
                    intent.setPackage("com.whatsapp")
                } else if (isBusinessInstalled) {
                    intent.setPackage("com.whatsapp.w4b")
                }
            }

            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // If package directly fails, fallback to standard share chooser
                val chooser = Intent.createChooser(intent, "Share Payment Slip via")
                chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.startActivity(chooser)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Could not create payment slip image: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
