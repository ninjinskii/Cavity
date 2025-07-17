package com.louis.app.cavity.ui.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.StringRes
import androidx.annotation.WorkerThread
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.FutureTarget
import com.louis.app.cavity.R
import com.louis.app.cavity.domain.error.SentryErrorReporter
import com.louis.app.cavity.model.Tasting
import com.louis.app.cavity.model.TastingAction
import com.louis.app.cavity.model.Wine
import java.util.concurrent.ExecutionException
import kotlin.random.Random
import androidx.core.net.toUri

object NotificationBuilder {
    private const val TASTING_CHANNEL_ID = "com.louis.app.cavity.TASTING_CHANNEL"
    private const val AUTO_BACKUPS_CHANNEL_ID = "com.louis.app.cavity.AUTO_BACKUPS_CHANNEL"
    private const val GROUP_ID = "com.louis.app.cavity.TASTING_GROUP"

    fun buildAutoBackupNotification(
        context: Context,
        @StringRes title: Int,
        @StringRes content: Int
    ): NotificationWithId {
        val pendingIntent = NavDeepLinkBuilder(context).run {
            setGraph(R.navigation.nav_graph)
            setDestination(R.id.fragmentLogin)
            createTaskStackBuilder()
            createPendingIntent()
        }

        val notification = NotificationCompat.Builder(context, AUTO_BACKUPS_CHANNEL_ID)
            .setContentTitle(context.getString(title))
            .setContentText(context.getString(content))
            .setSmallIcon(R.drawable.ic_glass)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        return NotificationWithId(Random.nextLong(1000), notification)
    }

    @WorkerThread
    fun buildTastingNotification(
        context: Context,
        tasting: Tasting,
        wine: Wine,
        tastingAction: TastingAction
    ): NotificationWithId {
        val errorReporter = SentryErrorReporter.getInstance(context)

        val pendingIntent = NavDeepLinkBuilder(context).run {
            setGraph(R.navigation.nav_graph)
            setDestination(R.id.fragmentTastingOverview)
            createTaskStackBuilder()
            setArguments(
                bundleOf("tastingId" to tasting.id, "opportunity" to tasting.opportunity)
            )
            createPendingIntent()
        }

        val flags =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

        val actionPendingIntent = Intent(context, TastingActionDoneReceiver::class.java).let {
            it.putExtra(TastingActionDoneReceiver.EXTRA_TASTING_ACTION_ID, tastingAction.id)
            PendingIntent.getBroadcast(context, tastingAction.id.hashCode(), it, flags)
        }

        val futureBitmap: FutureTarget<Bitmap>?
        val action = context.getString(R.string.done)
        var bitmap: Bitmap? = null

        if (wine.imgPath.isNotEmpty()) {
            futureBitmap = Glide.with(context)
                .asBitmap()
                .circleCrop()
                .load(wine.imgPath.toUri())
                .submit()

            try {
                bitmap = futureBitmap.get()
            } catch (e: ExecutionException) {
                errorReporter.captureMessage("Image for tasting notification couldn't be loaded")
            }
        }

        val content = when (tastingAction.type) {
            TastingAction.Action.SET_TO_FRIDGE -> R.string.set_to_fridge
            TastingAction.Action.SET_TO_JUG -> R.string.set_to_jug
            TastingAction.Action.UNCORK -> R.string.uncork
        }

        val notification = NotificationCompat.Builder(context, TASTING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_glass)
            .setContentTitle(wine.naming)
            .setContentText(context.getString(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setSubText(tasting.opportunity)
            .setLargeIcon(bitmap)
            .setGroup(GROUP_ID)
            .addAction(R.drawable.ic_check, action, actionPendingIntent)
            .setAutoCancel(false)

        //Glide.with(context).clear(futureBitmap)

        return NotificationWithId(tastingAction.id, notification.build())
    }

    fun notify(context: Context, notification: NotificationWithId) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        NotificationManagerCompat.from(context)
            .notify(notification.id.toInt(), notification.notification)
    }

    fun cancelNotification(context: Context, id: Int) {
        NotificationManagerCompat.from(context).cancel(id)
    }

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val tastingName = context.getString(R.string.tasting)
            val tastingDescriptionText = context.getString(R.string.tasting_channel_text)
            val autoBackupName = context.getString(R.string.auto_backup_channel)
            val autoBackupDescriptionText = context.getString(R.string.auto_backup_channel_text)

            createNotificationChannel(
                context,
                tastingName,
                tastingDescriptionText,
                TASTING_CHANNEL_ID
            )

            createNotificationChannel(
                context,
                autoBackupName,
                autoBackupDescriptionText,
                AUTO_BACKUPS_CHANNEL_ID
            )
        }
    }

    private fun createNotificationChannel(
        context: Context,
        name: String,
        descriptionText: String,
        channelId: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

data class NotificationWithId(val id: Long, val notification: Notification)
