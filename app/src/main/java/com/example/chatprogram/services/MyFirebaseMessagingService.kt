package com.example.chatprogram.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.support.v4.app.INotificationSideChannel
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.example.chatprogram.MainActivity
import com.example.chatprogram.Model.SohbetOdasi
import com.example.chatprogram.R
import com.example.chatprogram.SohbetEkrani
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService:FirebaseMessagingService() {

    var okunmayi_bekleyen_mesaj_sayisi = 0

    override fun onMessageReceived(p0: RemoteMessage) {

        if (!activityiKontrolEt()){


            var bildirimBaslik = p0.notification?.title
            var bildirimBody= p0.notification?.body
            var data = p0.data

            var baslik = p0?.data?.get("baslik")
            var icerik = p0?.data?.get("icerik")
            var bildirim_turu = p0?.data?.get("bildirim_turu")
            var sohbet_odasi_id = p0?.data?.get("sohbet_odasi_id")

            Log.e("bildirim: " , "Baslik: " + baslik + "Bildirim Gövde: " + icerik + "Data: " + bildirim_turu   + "secilen sohbet odasi: " + sohbet_odasi_id)

            var ref = FirebaseDatabase.getInstance().reference
                .child("sohbet_odasi")
                .orderByKey()
                .equalTo(sohbet_odasi_id)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        var tekSohbetOdasi = p0.children.iterator().next()

                        var nesneMap = tekSohbetOdasi.getValue() as HashMap<String,Object>
                        var oAnkiSohbetOdasi = SohbetOdasi()

                        oAnkiSohbetOdasi.olusturan_id = nesneMap.get("olusturan_id").toString()
                        oAnkiSohbetOdasi.seviye = nesneMap.get("seviye").toString()
                        oAnkiSohbetOdasi.sohbetodasi_adi = nesneMap.get("sohbetodasi_adi").toString()
                        oAnkiSohbetOdasi.sohbetodasi_id = nesneMap.get("sohbetodasi_id").toString()


                        var gorulenMesajSayisi = tekSohbetOdasi.child("sohbet_odasindaki_kullanicilar")
                            .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                            .child("okunan_mesaj_sayisi")
                            .getValue().toString().toInt()

                        var toplamMesajSayisi = tekSohbetOdasi.child("sohbet_odasi_mesajlari").childrenCount.toInt()

                        okunmayi_bekleyen_mesaj_sayisi = toplamMesajSayisi - gorulenMesajSayisi

                        bildirimGonder(baslik,icerik,oAnkiSohbetOdasi)



                    }
                })
        }
    }


    private fun bildirimGonder(baslik: String?, icerik: String?, oAnkiSohbetOdasi: SohbetOdasi) {

        var bildirim_id = notifitoionOlustur(oAnkiSohbetOdasi.sohbetodasi_id!!)
        Log.e("aaa: " , bildirim_id.toString())

        var pendingIntent = Intent(this,MainActivity::class.java)
        pendingIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        pendingIntent.putExtra("sohbet_odasi_id", oAnkiSohbetOdasi.sohbetodasi_id)

        var bildirimPendingIntent = PendingIntent.getActivities(this,10, arrayOf(pendingIntent),PendingIntent.FLAG_UPDATE_CURRENT)


        var builder = NotificationCompat.Builder(this,oAnkiSohbetOdasi.sohbetodasi_id!!)
            .setSmallIcon(R.drawable.ic_action_user)
            .setLargeIcon(BitmapFactory.decodeResource(resources,R.drawable.ic_action_user))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentTitle("" + oAnkiSohbetOdasi.sohbetodasi_adi.toString() + "'sından "+ baslik)
            .setContentText(icerik)
            .setAutoCancel(true)
            .setSubText("" + okunmayi_bekleyen_mesaj_sayisi + " yeni mesaj")
            .setStyle(NotificationCompat.BigTextStyle().bigText(icerik))
            .setNumber(okunmayi_bekleyen_mesaj_sayisi)
            .setOnlyAlertOnce(true)
            .setContentIntent(bildirimPendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            var odaSayisi = 0

            FirebaseDatabase.getInstance().reference.child("sohbet_odasi")
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        odaSayisi= p0.childrenCount.toInt()
                    }

                })

            for (i in 0..odaSayisi){
                var mChannel:NotificationChannel
                var name = baslik
                val icerik = "İÇERİK"
                val importence = NotificationManager.IMPORTANCE_DEFAULT

                 mChannel = NotificationChannel(oAnkiSohbetOdasi.sohbetodasi_id,name,importence)
                 mChannel.description = icerik

                var notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(mChannel)
                notificationManager.notify(bildirim_id,builder.build())
                }
        }
        else{
            var notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(bildirim_id,builder.build())
        }

    }

    private fun notifitoionOlustur(sohbetOdasiID:String):Int{

        var id = 0

        for (i in 6..10){
            id=id + sohbetOdasiID[i].toInt()
        }
        return id
    }

    private fun activityiKontrolEt():Boolean {

        if (SohbetEkrani.activityAcikMi) {
            return true
        } else
        {
            return false
        }


    }

    override fun onNewToken(p0: String) {

        var i  = FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {

            if (it.isSuccessful){
                var token = it.result?.token
                tokenVeriTabaninaKAydet(token)
            }

        }

    }

    private fun tokenVeriTabaninaKAydet(token: String?) {
        var ref = FirebaseDatabase.getInstance().reference
            .child("kullanici")
            .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
            .child("mesaj_token")
            .setValue(token)
    }


}