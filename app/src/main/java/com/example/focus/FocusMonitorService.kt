package com.example.focus

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.PlanifyApplication
import com.example.R
import com.example.core.i18n.JalaliCalendar
import com.example.core.i18n.stringsFor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FocusMonitorService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var monitorJob: Job? = null
    private var lastShieldLaunchTime = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ensureNotificationChannel()
        val notification = buildOngoingNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        startMonitoring()
        return START_STICKY
    }

    private fun startMonitoring() {
        if (monitorJob?.isActive == true) return
        val app = applicationContext as? PlanifyApplication ?: return
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        monitorJob = serviceScope.launch {
            while (isActive) {
                val state = app.focusTimerManager.state.value
                if (!state.isActive) {
                    stopSelf()
                    break
                }

                // Update foreground notification
                notificationManager.notify(NOTIFICATION_ID, buildOngoingNotification())

                // Check foreground app if Focus Lock app blocking is enabled and running
                if (state.isRunning && state.appBlockEnabled && UsageAccessHelper.hasUsageStatsPermission(this@FocusMonitorService)) {
                    val blockedPackages = app.database.blockedAppDao()
                        .getBlockedPackagesOnce()
                        .map { it.packageName }
                        .toSet()

                    if (blockedPackages.isNotEmpty()) {
                        val fgPkg = UsageAccessHelper.getForegroundPackageName(this@FocusMonitorService)
                        val now = System.currentTimeMillis()
                        if (fgPkg != null && fgPkg != packageName && blockedPackages.contains(fgPkg) && (now - lastShieldLaunchTime) > 3500L) {
                            lastShieldLaunchTime = now
                            launchFocusShield(fgPkg)
                        }
                    }
                }
                delay(1200L)
            }
        }
    }

    private fun launchFocusShield(blockedPkg: String) {
        val shieldIntent = Intent(this, FocusShieldActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(FocusShieldActivity.EXTRA_BLOCKED_PACKAGE, blockedPkg)
        }
        runCatching { startActivity(shieldIntent) }

        // Also post high-priority notification so Android 10+ background restrictions still alert the user
        val app = applicationContext as? PlanifyApplication
        val lang = app?.currentLanguageCache ?: com.example.core.i18n.AppLanguage.EN
        val strings = stringsFor(lang)
        val pending = PendingIntent.getActivity(
            this,
            202,
            shieldIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alert = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(strings.appName)
            .setContentText(strings.focusShieldMessage)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setFullScreenIntent(pending, true)
            .setContentIntent(pending)
            .build()
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(SHIELD_ALERT_ID, alert)
    }

    private fun buildOngoingNotification(): Notification {
        val app = applicationContext as? PlanifyApplication
        val state = app?.focusTimerManager?.state?.value ?: FocusTimerUiState()
        val lang = app?.currentLanguageCache ?: com.example.core.i18n.AppLanguage.EN
        val strings = stringsFor(lang)
        val timeText = if (state.mode.isCountUp) {
            JalaliCalendar.formatCountdown(state.elapsedSeconds, lang)
        } else {
            JalaliCalendar.formatCountdown(state.remainingSeconds, lang)
        }
        val title = if (state.taskTitle.isNotBlank()) state.taskTitle else state.mode.label(strings)

        val openAppPending = PendingIntent.getActivity(
            this,
            101,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("PLANIFY • $title")
            .setContentText(timeText)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openAppPending)
            .build()
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.focus_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.focus_channel_desc)
            }
            nm.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        monitorJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "planify_focus_channel"
        const val NOTIFICATION_ID = 4001
        const val SHIELD_ALERT_ID = 4002
    }
}
