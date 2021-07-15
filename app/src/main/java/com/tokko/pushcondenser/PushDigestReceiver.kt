package com.tokko.pushcondenser

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class PushDigestReceiver : BroadcastReceiver() {

    private fun getNotifications(context: Context) =
        Gson().fromJson<List<Notification>>(context.getSharedPreferences("notifications", Context.MODE_PRIVATE).getString("notifications", "[]"), object: TypeToken<List<Notification>>(){}.type).toMutableList()


    override fun onReceive(context: Context, intent: Intent) {
        val nm = context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        if(intent.action == ACTION) {

            val notifications = getNotifications(context)
            if (notifications.count() == 0) {
                nm?.cancel(DIGEST_ID)
                return
            }
            val (title, body, id) = if (notifications.count() == 1) {
                Triple(
                    notifications.first().title,
                    notifications.first().body,
                    notifications.first().id
                )
            } else {
                notifications.forEach { nm?.cancel(it.id) }
                val title = "You have ${notifications.count()} notifications"
                val body = notifications.joinToString(" | ") { it.body }
                Triple(title, body, DIGEST_ID)
            }
            val notification = NotificationCompat.Builder(context.applicationContext)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .setChannelId("some unique id I guess")
                .setDeleteIntent(PendingIntent.getBroadcast(context.applicationContext,
                1, Intent(DELETE).putExtra("id", id), PendingIntent.FLAG_ONE_SHOT))
                .build()
            nm?.notify(id, notification)
        }
        else if(intent.action == DELETE){
            val id = intent.getIntExtra("id", 1)
            nm?.cancel(id)
            if(id == 1)
                context.getSharedPreferences("notifications", Context.MODE_PRIVATE).edit().putString("notifications", "[]").apply()
        }
    }

    companion object{
        val DIGEST_ID = 1
        val ACTION = "com.tokko.DIGEST"
        val DELETE = "com.tokko.DELETE"
        val FIRESTORE_ACTION = "com.tokko.FIRESTORE_ACTION"
    }
}