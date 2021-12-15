package com.louis.app.cavity.ui.tasting.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.FutureTarget
import com.louis.app.cavity.R
import com.louis.app.cavity.model.Tasting
import com.louis.app.cavity.model.TastingAction
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.ui.ActivityMain
import com.louis.app.cavity.util.L

object TastingNotifier {
    private const val CHANNEL_ID = "com.louis.app.cavity.TASTING_CHANNEL"
    private const val GROUP_ID = "com.louis.app.cavity.TASTING_GROUP"

    fun buildNotification(
        context: Context,
        tasting: Tasting,
        wine: Wine,
        tastingAction: TastingAction
    ): TastingActionNotification {
        val intent = Intent(context, ActivityMain::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(context, 0, intent, FLAG_IMMUTABLE)

        var futureBitmap: FutureTarget<Bitmap>? = null
        var bitmap: Bitmap? = null

        if (wine.imgPath.isNotEmpty()) {
            futureBitmap = Glide.with(context)
                .asBitmap()
                .circleCrop()
                .load(Uri.parse(wine.imgPath))
                .submit()

            bitmap = futureBitmap.get()
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_glass)
            .setContentTitle(wine.naming)
            .setContentText(tastingAction.type.toString())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setSubText(tasting.opportunity)
            .setLargeIcon(bitmap)
            .setGroup(GROUP_ID)
            .setAutoCancel(false)

        Glide.with(context).clear(futureBitmap)

        return TastingActionNotification(tastingAction.id, notification.build())
    }

    fun notify(context: Context, notification: TastingActionNotification) {
        NotificationManagerCompat.from(context).run {
            if (notification.tastingActionId <= Int.MAX_VALUE) {
                notify(notification.tastingActionId.toInt(), notification.notification)
            } else {
                L.v("Too much actions in database, cannot perform notify")
            }
        }
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Mon channel"
            val descriptionText = "Channele des dégustations de Cavity"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}

data class TastingActionNotification(val tastingActionId: Long, val notification: Notification)
