package com.example.chatprogram

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatprogram.Dialogs.SohbetOdasiYeniEkleFragment
import com.example.chatprogram.Model.SohbetMesaj
import com.example.chatprogram.Model.SohbetOdasi
import com.example.chatprogram.adapters.SohbetOdasiGetirAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_sohbet_odasi.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class SohbetOdasiActivity : AppCompatActivity() {

    lateinit var tumSohbetOdalari:ArrayList<SohbetOdasi>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_sohbet_odasi)
        init()


    }

    fun init() {

        tumSohbetOdalariniGetir()

        fabYeniSohbetOdasiEkle.setOnClickListener {
            var dialogGoster =
                SohbetOdasiYeniEkleFragment()
            dialogGoster.show(supportFragmentManager, "fragmentYeniSohbetOdasiEkle")
        }


    }

    private fun tumSohbetOdalariniGetir() {

        tumSohbetOdalari = ArrayList<SohbetOdasi>()

        var reference = FirebaseDatabase.getInstance().reference
        reference.child("sohbet_odasi").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {


                for (tekSohbetOdasi in p0.children){

                    var nesneMap = tekSohbetOdasi.getValue() as HashMap<String,Object>
                    var oAnkiSohbetOdasi = SohbetOdasi()

                    oAnkiSohbetOdasi.olusturan_id = nesneMap.get("olusturan_id").toString()
                    oAnkiSohbetOdasi.seviye = nesneMap.get("seviye").toString()
                    oAnkiSohbetOdasi.sohbetodasi_adi = nesneMap.get("sohbetodasi_adi").toString()
                    oAnkiSohbetOdasi.sohbetodasi_id = nesneMap.get("sohbetodasi_id").toString()

                    var tumMesajlar = ArrayList<SohbetMesaj>()

                    for (tekMesaj in tekSohbetOdasi.child("sohbet_odasi_mesajlari").children){

                        var okunanMesaj = SohbetMesaj()

                        okunanMesaj.adi = tekMesaj.getValue(SohbetMesaj::class.java)?.adi
                        okunanMesaj.timestamp = tekMesaj.getValue(SohbetMesaj::class.java)?.timestamp
                        okunanMesaj.kullanici_id = tekMesaj.getValue(SohbetMesaj::class.java)?.kullanici_id
                        okunanMesaj.mesaj = tekMesaj.getValue(SohbetMesaj::class.java)?.mesaj
                        okunanMesaj.profil_resmi = tekMesaj.getValue(SohbetMesaj::class.java)?.profil_resmi

                        tumMesajlar.add(okunanMesaj)

                    }
                    oAnkiSohbetOdasi.sohbet_odasi_mesajlari = tumMesajlar
                    tumSohbetOdalari.add(oAnkiSohbetOdasi)

                    verileriListele()


                }

                Toast.makeText(this@SohbetOdasiActivity,"Sohbet Odasi Sayisi: " + tumSohbetOdalari.size,Toast.LENGTH_SHORT).show()
            }
        })



    }

    private fun verileriListele() {
        var mAdapter = SohbetOdasiGetirAdapter(
            tumSohbetOdalari,
            this@SohbetOdasiActivity
        )
        rvSohbetOdalari.adapter = mAdapter

        var mlayoutManager = LinearLayoutManager(this@SohbetOdasiActivity,LinearLayoutManager.VERTICAL,false)
        rvSohbetOdalari.layoutManager = mlayoutManager



    }

    fun sohbetOdasiSil(silinecekSohbetOdasiID: String) {


        var ref= FirebaseDatabase.getInstance().reference
        ref.child("sohbet_odasi")
            .child(silinecekSohbetOdasiID)
            .removeValue()
        init()
        Toast.makeText(this,"Sohbet Odasi Silindi.",Toast.LENGTH_SHORT).show()

    }
}
