package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences

class SecurityPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("blackbook_security_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PIN = "master_pin"
        private const val KEY_DURESS_PIN = "duress_pin"
        private const val KEY_IS_BIOMETRIC_ENABLED = "is_biometric_enabled"
        private const val KEY_IS_SECURITY_ACTIVE = "is_security_active"
        private const val KEY_CURRENCY = "currency_symbol"
        private const val KEY_AUTO_LOCK_MINUTES = "auto_lock_minutes"
        private const val KEY_LAST_UNLOCKED_TIME = "last_unlocked_time"

        const val DEFAULT_PIN = "1234"
        const val DEFAULT_DURESS_PIN = "9999"
        const val DEFAULT_CURRENCY = "Rs "

        private const val KEY_WHATSAPP_TEMPLATE = "whatsapp_template"
        private const val KEY_SMS_TEMPLATE = "sms_template"

        const val DEFAULT_WHATSAPP_TEMPLATE =
            "Hi {name}, friendly reminder that {amount} is pending. Please pay when you can. Thank you!"
        const val DEFAULT_SMS_TEMPLATE =
            "Hi {name}, reminder from BlackBook: pending amount is {amount}. Please clear soon. Thanks."

        private const val KEY_SLIP_BUSINESS_NAME = "slip_business_name"
        private const val KEY_SLIP_HEADER_TITLE = "slip_header_title"
        private const val KEY_SLIP_NOTE = "slip_note"
        private const val KEY_SLIP_CAPTION = "slip_caption"

        const val DEFAULT_SLIP_BUSINESS_NAME = "BLACKBOOK"
        const val DEFAULT_SLIP_HEADER_TITLE = "PAYMENT RECEIPT SLIP"
        const val DEFAULT_SLIP_NOTE = "Please clear the pending balance soon."
        const val DEFAULT_SLIP_CAPTION =
            "Hi {name}, here is your payment slip for {amount}. Please check the image attached."
    }

    var whatsappTemplate: String
        get() = prefs.getString(KEY_WHATSAPP_TEMPLATE, DEFAULT_WHATSAPP_TEMPLATE) ?: DEFAULT_WHATSAPP_TEMPLATE
        set(value) = prefs.edit().putString(KEY_WHATSAPP_TEMPLATE, value).apply()

    var smsTemplate: String
        get() = prefs.getString(KEY_SMS_TEMPLATE, DEFAULT_SMS_TEMPLATE) ?: DEFAULT_SMS_TEMPLATE
        set(value) = prefs.edit().putString(KEY_SMS_TEMPLATE, value).apply()

    var slipBusinessName: String
        get() = prefs.getString(KEY_SLIP_BUSINESS_NAME, DEFAULT_SLIP_BUSINESS_NAME) ?: DEFAULT_SLIP_BUSINESS_NAME
        set(value) = prefs.edit().putString(KEY_SLIP_BUSINESS_NAME, value).apply()

    var slipHeaderTitle: String
        get() = prefs.getString(KEY_SLIP_HEADER_TITLE, DEFAULT_SLIP_HEADER_TITLE) ?: DEFAULT_SLIP_HEADER_TITLE
        set(value) = prefs.edit().putString(KEY_SLIP_HEADER_TITLE, value).apply()

    var slipNote: String
        get() = prefs.getString(KEY_SLIP_NOTE, DEFAULT_SLIP_NOTE) ?: DEFAULT_SLIP_NOTE
        set(value) = prefs.edit().putString(KEY_SLIP_NOTE, value).apply()

    var slipCaption: String
        get() = prefs.getString(KEY_SLIP_CAPTION, DEFAULT_SLIP_CAPTION) ?: DEFAULT_SLIP_CAPTION
        set(value) = prefs.edit().putString(KEY_SLIP_CAPTION, value).apply()

    fun formatTemplate(template: String, name: String, amountFormatted: String): String {
        return template
            .replace("{name}", name, ignoreCase = true)
            .replace("[name]", name, ignoreCase = true)
            .replace("{amount}", amountFormatted, ignoreCase = true)
            .replace("[amount]", amountFormatted, ignoreCase = true)
    }

    var pin: String
        get() = prefs.getString(KEY_PIN, DEFAULT_PIN) ?: DEFAULT_PIN
        set(value) = prefs.edit().putString(KEY_PIN, value).apply()

    var duressPin: String
        get() = prefs.getString(KEY_DURESS_PIN, DEFAULT_DURESS_PIN) ?: DEFAULT_DURESS_PIN
        set(value) = prefs.edit().putString(KEY_DURESS_PIN, value).apply()

    var isBiometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_IS_BIOMETRIC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_IS_BIOMETRIC_ENABLED, value).apply()

    var isSecurityEnabled: Boolean
        get() = prefs.getBoolean(KEY_IS_SECURITY_ACTIVE, true)
        set(value) = prefs.edit().putBoolean(KEY_IS_SECURITY_ACTIVE, value).apply()

    var currencySymbol: String
        get() {
            val saved = prefs.getString(KEY_CURRENCY, DEFAULT_CURRENCY) ?: DEFAULT_CURRENCY
            return if (saved == "$") DEFAULT_CURRENCY else saved
        }
        set(value) = prefs.edit().putString(KEY_CURRENCY, value).apply()

    var autoLockMinutes: Int
        get() = prefs.getInt(KEY_AUTO_LOCK_MINUTES, 1)
        set(value) = prefs.edit().putInt(KEY_AUTO_LOCK_MINUTES, value).apply()

    var lastUnlockedTime: Long
        get() = prefs.getLong(KEY_LAST_UNLOCKED_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_UNLOCKED_TIME, value).apply()

    fun resetSecurity() {
        prefs.edit().clear().apply()
    }
}
