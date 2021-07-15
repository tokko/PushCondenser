package com.tokko.pushcondenser

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import com.google.firebase.ktx.initialize
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Firebase.initialize(this)
        FirebaseMessaging.getInstance().subscribeToTopic("test")

        val am = getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        //am?.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000*5, PendingIntent.getBroadcast(applicationContext, 0, Intent(PushDigestReceiver.ACTION), 0))
        am?.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000, PendingIntent.getBroadcast(applicationContext, 0, Intent(PushDigestReceiver.FIRESTORE_ACTION), 0))
        registerReceiver(PushDigestReceiver(), IntentFilter(PushDigestReceiver.ACTION).apply { addAction(PushDigestReceiver.DELETE); addAction(PushDigestReceiver.FIRESTORE_ACTION) })

        supportFragmentManager.beginTransaction().replace(android.R.id.content, FirrestoreListFragment()).commit()
    }
}