package com.example.plantguardian

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.firebase.database.*

class PlantService : Service() {

    private var notificatDeja = false
    private val CHANNEL_ID = "PLANT_SERVICE_CHANNEL"

    override fun onCreate() {
        super.onCreate()
        // 1. CREĂM CANALUL ÎNAINTE DE ORICE
        createServiceChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 2. PORNIRE FOREGROUND - Notificarea care ține aplicația vie în bară
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Plant Guardian Monitor")
            .setContentText("Monitorizez solul 24/7...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()

        // ID-ul trebuie să fie diferit de 0
        startForeground(1, notification)

        // 3. ASCULTARE FIREBASE
        val ref = FirebaseDatabase.getInstance("")
            .getReference("statie_meteo/curent/umiditate_sol")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val hs = snapshot.value.toString().toIntOrNull() ?: 100
                if (hs < 30 && !notificatDeja) {
                    sendAlertNotification("URGENT: Pune-i apă! ($hs%)")
                    notificatDeja = true
                } else if (hs >= 40) {
                    notificatDeja = false
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        return START_STICKY
    }

    private fun createServiceChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Serviciu Monitorizare",
                NotificationManager.IMPORTANCE_LOW // Low ca sa nu sune notificarea de status
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun sendAlertNotification(msg: String) {
        // Canalul PLANT_ALERTS trebuie sa existe deja (creea de MainActivity)
        val alert = NotificationCompat.Builder(this, "PLANT_ALERTS")
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("Plant Guardian Alert")
            .setContentText(msg)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(2, alert)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}