package com.louis.app.cavity.ui.tasting.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.FutureTarget
import com.louis.app.cavity.R
import com.louis.app.cavity.model.Tasting
import com.louis.app.cavity.model.TastingAction
import com.louis.app.cavity.model.Wine

object TastingNotifier {
    private const val CHANNEL_ID = "com.louis.app.cavity.TASTING_CHANNEL"
    private const val GROUP_ID = "com.louis.app.cavity.TASTING_GROUP"

    fun buildNotification(
        context: Context,
        tasting: Tasting,
        wine: Wine,
        tastingAction: TastingAction
    ): TastingActionNotification {

        val pendingIntent = NavDeepLinkBuilder(context).run {
            setGraph(R.navigation.nav_graph)
            setDestination(R.id.fragmentTastingOverview)
            setArguments(
                bundleOf("tastingId" to tasting.id, "opportunity" to tasting.opportunity)
            )
            createPendingIntent()
        }

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

        val content = when (tastingAction.type) {
            TastingAction.Action.SET_TO_FRIDGE -> R.string.set_to_fridge
            TastingAction.Action.SET_TO_JUG -> R.string.set_to_jug
            TastingAction.Action.UNCORK -> R.string.uncork
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_glass)
            .setContentTitle(wine.naming)
            .setContentText(context.getString(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setSubText(tasting.opportunity)
            .setLargeIcon(bitmap)
            .setGroup(GROUP_ID)
            .setAutoCancel(false)

        //Glide.with(context).clear(futureBitmap)

        return TastingActionNotification(tastingAction.id, notification.build())
    }

    fun notify(context: Context, notification: TastingActionNotification) {
        NotificationManagerCompat.from(context)
            .notify(notification.tastingActionId.toInt(), notification.notification)
    }

    fun cancelNotification(context: Context, tastingActionId: Int) {
        NotificationManagerCompat.from(context).cancel(tastingActionId)
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.tasting)
            val descriptionText = context.getString(R.string.notification_channel)
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
