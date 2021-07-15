package com.tokko.pushcondenser

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

data class Notification(val id: Int, val title: String, val body: String)
class PushService : FirebaseMessagingService() {
    val TAG = "SOME TAG LOL"

    private fun saveNotification(context: Context, notification: Notification){
       val prefs = context.getSharedPreferences("notifications", Context.MODE_PRIVATE)
        val list = Gson().fromJson<List<Notification>>(prefs.getString("notifications", "[]"), object: TypeToken<List<Notification>>(){}.type).toMutableList()
        list.add(notification)
        prefs.edit().putString("notifications", Gson().toJson(list)).apply()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            setupNotificationChannel()
            saveNotification(this, Notification(Random().nextInt()+1, remoteMessage.data["title"] ?: "", remoteMessage.data["body"] ?: ""))
        }
    }

    private fun setupNotificationChannel() {
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel("some unique id I guess", "main channel", importance)
        mChannel.description = "the main goddamn channel for notifications for this sample app"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }
}