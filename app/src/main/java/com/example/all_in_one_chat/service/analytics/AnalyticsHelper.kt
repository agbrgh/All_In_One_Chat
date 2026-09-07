package com.example.all_in_one_chat.service.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val analytics = FirebaseAnalytics.getInstance(context)

    fun logLogin(method: String, success: Boolean) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, method)
            putString("success", success.toString())
        }
        analytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
    }

    fun logMessageSent(serviceType: String) {
        val bundle = Bundle().apply {
            putString("service_type", serviceType)
        }
        analytics.logEvent("message_sent", bundle)
    }

    fun logMediaDownload(mediaType: String) {
        val bundle = Bundle().apply {
            putString("media_type", mediaType)
        }
        analytics.logEvent("media_download", bundle)
    }

    fun logThemeChanged(themeMode: String) {
        val bundle = Bundle().apply {
            putString("theme_mode", themeMode)
        }
        analytics.logEvent("theme_changed", bundle)
    }

    fun logServiceToggled(serviceType: String, active: Boolean) {
        val bundle = Bundle().apply {
            putString("service_type", serviceType)
            putString("active", active.toString())
        }
        analytics.logEvent("service_toggled", bundle)
    }

    fun logSearchPerformed(queryLength: Int) {
        val bundle = Bundle().apply {
            putInt("query_length", queryLength)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SEARCH, bundle)
    }
}
