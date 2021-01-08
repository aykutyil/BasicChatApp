package com.example.chatprogram.Dialogs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.example.chatprogram.Model.SohbetMesaj
import com.example.chatprogram.Model.SohbetOdasi
import com.example.chatprogram.R
import com.example.chatprogram.SohbetOdasiActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class SohbetOdasiYeniEkleFragment : DialogFragment() {

    lateinit var tvOdaSeviyesiGoster:TextView
    var mSeekBarSeviye = 0
    var kullaniciSeviyesi = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {

        var view = inflater.inflate(R.layout.fragment_sohbet_odasi_yeni_ekle, container, false)

        var etYeniOdaIsmi:EditText = view.findViewById(R.id.etYeniSohbetOdasiAdi)
        var btnYeniSohbetOdasiEkle:Button = view.findViewById(R.id.btnYeniSohbetOdasiKAydet)
        var sbOdaSeviyesi:SeekBar = view.findViewById(R.id.sbOdaSeviyesi)
         tvOdaSeviyesiGoster = view.findViewById(R.id.tvOdaSeviyesiGoster)

        sbOdaSeviyesi.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                mSeekBarSeviye = progress/10
                tvOdaSeviyesiGoster.setText(mSeekBarSeviye.toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        kullaniciBilgileriniGetir()

        btnYeniSohbetOdasiEkle.setOnClickListener {
            if (!etYeniOdaIsmi.text.toString().isNullOrEmpty()){

                if (kullaniciSeviyesi >= tvOdaSeviyesiGoster.text.toString().toInt()){

                    var kullanici = FirebaseAuth.getInstance().currentUser

                    var reference = FirebaseDatabase.getInstance().reference
                    var sohbetOdasiID = reference.child("sohbet_odasi").push().key

                    var olusturanKullanici =SohbetOdasi()

                    olusturanKullanici.sohbetodasi_adi = etYeniOdaIsmi.text.toString()
                    olusturanKullanici.sohbetodasi_id = sohbetOdasiID
                    olusturanKullanici.seviye = mSeekBarSeviye.toString()
                    olusturanKullanici.olusturan_id = kullanici?.uid

                    reference.child("sohbet_odasi").child(sohbetOdasiID!!).setValue(olusturanKullanici)

                    var mesajID = reference.child("sohbet_odasi").push().key

                    var karsilamaMesajOlustur = SohbetMesaj()
                    karsilamaMesajOlustur.mesaj = "Hoşgeldiniz."
                    karsilamaMesajOlustur.timestamp = getTarihGetir()

                    reference.child("sohbet_odasi")
                        .child(sohbetOdasiID)
                        .child("sohbet_odasi_mesajlari")
                        .child(mesajID!!)
                        .setValue(karsilamaMesajOlustur)

                    Toast.makeText(activity,"Sohbet Odası Olusturuldu.",Toast.LENGTH_SHORT).show()

                    dialog?.dismiss()
                    (activity as SohbetOdasiActivity).init()



                }else{
                    Toast.makeText(activity,"Seviyeniz: " + kullaniciSeviyesi.toString()
                                                    + " Sohbet Odasını Seviyenizden Yüksek Kurumazsınız.",Toast.LENGTH_SHORT).show()

                }

            }else{
                Toast.makeText(activity,"Sohbet Odası İsmini Giriniz.",Toast.LENGTH_SHORT).show()
            }
        }


        return view
    }

    private fun kullaniciBilgileriniGetir() {

        var kullanici = FirebaseAuth.getInstance().currentUser
        var reference = FirebaseDatabase.getInstance().reference
        var sorgu = reference.child("kullanici")
            .orderByKey()
            .equalTo(kullanici?.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {

                    for (i in p0.children){
                         kullaniciSeviyesi = i.getValue(com.example.chatprogram.Model.kullanici::class.java)?.seviye!!.toInt()
                    }


                }
            })


    }

    private fun  getTarihGetir():String{
        var tarihFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("tr"))


        return tarihFormat.format(Date())
    }


}
