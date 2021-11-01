package com.tokko.pushcondenser

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Firebase.initialize(this)
        FirebaseMessaging.getInstance().subscribeToTopic("test")

        val am = getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        am?.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 60000, PendingIntent.getBroadcast(applicationContext, 0, Intent(PushDigestReceiver.FIRESTORE_ACTION), PendingIntent.FLAG_IMMUTABLE))
        registerReceiver(PushDigestReceiver(), IntentFilter(PushDigestReceiver.ACTION).apply { addAction(PushDigestReceiver.DELETE); addAction(PushDigestReceiver.FIRESTORE_ACTION) })

        supportFragmentManager.beginTransaction().replace(android.R.id.content, FirestoreListFragment()).commit()
    }
}