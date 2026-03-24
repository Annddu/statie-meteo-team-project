package com.example.plantguardian

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private var notificatDeja = false
    private val listaOre = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val serviceIntent = Intent(this, PlantService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 1. Permisiuni Notificări
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        createNotificationChannel()

        val textTemp = findViewById<TextView>(R.id.textTemp)
        val textUmidAer = findViewById<TextView>(R.id.textUmidAer)
        val textUmidSol = findViewById<TextView>(R.id.textUmidSol)
        val chart = findViewById<LineChart>(R.id.chartTemperatura)

        // --- LOGICA BUTON (RESTAURATĂ) ---
        val btnCanta = findViewById<Button>(R.id.btnCanta)

        val database = FirebaseDatabase.getInstance("")
        val refComenzi = database.getReference("statie_meteo/comenzi/melodie")

        btnCanta.setOnClickListener {
            refComenzi.setValue("SPECIAL")
            Toast.makeText(this, "Melodie trimisă!", Toast.LENGTH_SHORT).show()
        }

        // 2. LIVE DATA & NOTIFICĂRI
        database.getReference("statie_meteo/curent").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    try {
                        // Conversie mai sigura a datelor
                        val temp = snapshot.child("temperatura").value.toString()
                        val uAer = snapshot.child("umiditate_aer").value.toString()
                        val uSolObj = snapshot.child("umiditate_sol").value

                        // Transformam in numar indiferent daca e String, Long sau Double in Firebase
                        val hs = when (uSolObj) {
                            is Long -> uSolObj.toInt()
                            is Double -> uSolObj.toInt()
                            else -> uSolObj.toString().toIntOrNull() ?: 100
                        }

                        // Update UI
                        textTemp.text = "🌡️ Temp: $temp °C"
                        textUmidAer.text = "💨 Aer: $uAer %"
                        textUmidSol.text = "🌱 Sol: $hs %"

                        // DEBUG in Logcat: Sa vedem exact ce numere primim
                        Log.d("DEBUG_PLANTA", "Senzor: $hs | Notificat: $notificatDeja")

                        // ALERTA
                        if (hs < 30) {
                            if (!notificatDeja) {
                                sendNotification("URGENT: Planta moare de sete! ($hs%)")
                                notificatDeja = true
                                Log.d("DEBUG_PLANTA", "Notificare TRIMISA!")
                            }
                        } else if (hs >= 40) {
                            // Resetam doar daca umiditatea creste peste 40 (ca sa evitam spam-ul la 29-30)
                            notificatDeja = false
                            Log.d("DEBUG_PLANTA", "Alerta resetata. Gata pentru urmatoarea uscare.")
                        }
                    } catch (e: Exception) {
                        Log.e("DEBUG_PLANTA", "Eroare la procesare date: ${e.message}")
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // 3. ISTORIC & GRAFIC
        val refIstoric = database.getReference("statie_meteo/istoric")
        refIstoric.limitToLast(50).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val entriesT = ArrayList<Entry>()
                val entriesHA = ArrayList<Entry>()
                val entriesHS = ArrayList<Entry>()
                listaOre.clear()

                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                var i = 0f

                for (post in snapshot.children) {
                    try {
                        val temp = post.child("temperatura").value.toString().toFloat()
                        val aer = post.child("umiditate_aer").value.toString().toFloat()
                        val sol = post.child("umiditate_sol").value.toString().toFloat()
                        val ts = post.child("timestamp").value.toString().toLong() * 1000

                        entriesT.add(Entry(i, temp))
                        entriesHA.add(Entry(i, aer))
                        entriesHS.add(Entry(i, sol))
                        listaOre.add(sdf.format(Date(ts)))
                        i++
                    } catch (e: Exception) {}
                }

                val lineData = LineData(
                    prepSet(entriesT, "Temp", Color.RED),
                    prepSet(entriesHA, "Aer", Color.BLUE),
                    prepSet(entriesHS, "Sol", Color.parseColor("#388E3C"))
                )

                chart.data = lineData
                chart.xAxis.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value >= 0 && value < listaOre.size) listaOre[value.toInt()] else ""
                    }
                }

                chart.xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                chart.setTouchEnabled(true)
                chart.isDragEnabled = true
                chart.setScaleEnabled(true)
                chart.setPinchZoom(true)
                chart.description.isEnabled = false
                chart.setVisibleXRangeMaximum(10f)
                chart.moveViewToX(i)
                chart.invalidate()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun prepSet(e: ArrayList<Entry>, l: String, c: Int): LineDataSet {
        val s = LineDataSet(e, l)
        s.color = c
        s.setCircleColor(c)
        s.lineWidth = 2f
        s.setDrawValues(false)
        s.mode = LineDataSet.Mode.CUBIC_BEZIER
        return s
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("PLANT_ALERTS", "Alerte Planta", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notificări critice despre starea plantei"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(msg: String) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val builder = NotificationCompat.Builder(this, "PLANT_ALERTS")
            .setSmallIcon(android.R.drawable.stat_sys_warning) // Iconiță standard de avertizare
            .setContentTitle("Plant Guardian Alert")
            .setContentText(msg)
            .setPriority(NotificationCompat.PRIORITY_MAX) // Forțează apariția pe ecran
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)

        NotificationManagerCompat.from(this).notify(1, builder.build())
    }
}