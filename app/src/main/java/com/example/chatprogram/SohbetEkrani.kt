package com.example.chatprogram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatprogram.Model.FCMModel
import com.example.chatprogram.Model.SohbetMesaj
import com.example.chatprogram.Model.kullanici
import com.example.chatprogram.adapters.SohbetMesajRecyclerViewAdapter
import com.example.chatprogram.adapters.SohbetOdasiGetirAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_sohbet_ekrani.*
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class SohbetEkrani : AppCompatActivity() {

    companion object{
        var activityAcikMi = false
    }

    var mAutStateListener:FirebaseAuth.AuthStateListener?=null
    var mMesajReference: DatabaseReference?=null

    var SERVER_KEY:String?=null

    var secilenOdaID:String = ""
    var mesajID:HashSet<String>?=null
    var tumMesajlar:ArrayList<SohbetMesaj>?=null
    var myAdapter: SohbetMesajRecyclerViewAdapter?= null

    val BASE_URL = "https://fcm.googleapis.com/fcm/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_sohbet_ekrani)

        kullaniciDinlemeyeBasla()

        //sohbet activityden gelen seçilen sohbet odasının id bilgisini alır ve valueEventListener kaydı yapar.
        sohbetODasiOgren()

        serverKeyOku()

        init()

    }

    private fun serverKeyOku() {
        var ref = FirebaseDatabase.getInstance().reference.child("server").orderByValue()

        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
               var singleSnapshot = p0.children.iterator().next()
                SERVER_KEY = singleSnapshot.getValue().toString()

            }

        })
    }

    private fun init() {

        etMesajYaz.setOnClickListener {
            rvMesajYeri.smoothScrollToPosition(myAdapter!!.itemCount-1)
        }

        imgMesajGonder.setOnClickListener {
            if (!etMesajYaz.text.toString().equals("")){
                var mesaj = etMesajYaz.text.toString()

                var eklenecekMesajBilgileri = SohbetMesaj()

                eklenecekMesajBilgileri.mesaj = mesaj
                eklenecekMesajBilgileri.timestamp = getTarihGetir()
                eklenecekMesajBilgileri.kullanici_id = FirebaseAuth.getInstance().currentUser?.uid

                var reference = FirebaseDatabase.getInstance().reference
                    .child("sohbet_odasi")
                    .child(secilenOdaID)
                    .child("sohbet_odasi_mesajlari")

                var yeniGonderilenMesajIDsi = reference.push().key

                reference.child(yeniGonderilenMesajIDsi!!)
                    .setValue(eklenecekMesajBilgileri)



                var retrofit= Retrofit.Builder().baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                var myInterface = retrofit.create(FCMInterface::class.java)

                var headers = HashMap<String,String>()
                headers.put("Content-Type", "application/json")
                headers.put("Authorization", "key=$SERVER_KEY")

                var ref = FirebaseDatabase.getInstance().reference
                    .child("sohbet_odasi")
                    .child(secilenOdaID)
                    .child("sohbet_odasindaki_kullanicilar")
                    .orderByKey()
                    .addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {

                            for (kullaniciID in p0.children){
                                var id = kullaniciID.key

                                if(!id!!.equals(FirebaseAuth.getInstance().currentUser?.uid.toString())){

                                    var ref = FirebaseDatabase.getInstance().reference
                                        .child("kullanici")
                                        .orderByKey()
                                        .equalTo(id)
                                        .addListenerForSingleValueEvent(object : ValueEventListener{
                                            override fun onCancelled(p0: DatabaseError) {

                                            }

                                            override fun onDataChange(p0: DataSnapshot) {
                                                var tekkullanici = p0.children?.iterator()?.next()
                                                var mesaj_token = tekkullanici.getValue(kullanici::class.java)?.mesaj_token

                                                var data = FCMModel.Data("Yeni Mesaj Var", etMesajYaz.text.toString(),"sohbet",secilenOdaID)

                                                var to = mesaj_token

                                                var bildirim = FCMModel(to, data)

                                                var istek = myInterface.bildirimGonder(headers,bildirim)

                                                istek.enqueue(object : Callback<Response<FCMModel>>{

                                                    override fun onFailure(call: Call<Response<FCMModel>>, t: Throwable) {
                                                        Log.e("RETROFİT:","Hata:" + t?.message)
                                                    }

                                                    override fun onResponse(call: Call<Response<FCMModel>>,response: Response<Response<FCMModel>>)
                                                    {
                                                        Log.e("RETROFİT:","Başarılı: " + response.toString())
                                                    }
                                                })

                                                etMesajYaz.setText("")
                                            }

                                        })
                                }

                            }

                        }
                    })



            }
        }
    }

    private fun  getTarihGetir():String{
        var tarihFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("tr"))


        return tarihFormat.format(Date())
    }

    private fun sohbetODasiOgren() {

        if (intent.hasExtra("sohbet_odasi_id")){
            secilenOdaID = intent.getStringExtra("sohbet_odasi_id")
            baslatMesajListener()
        }else{
            secilenOdaID = intent.getStringExtra("sohbetodasi_id")
            baslatMesajListener()
        }

    }

    private fun sohbetOdasinaYonlendir(){

    }


    var mValueEventListener:ValueEventListener = object : ValueEventListener{
        override fun onCancelled(p0: DatabaseError) {

        }

        override fun onDataChange(p0: DataSnapshot) {
            sohbetOdasindakiMesajlariGetir()
            gorunenMesajSayisiniGuncelle(p0.childrenCount.toString())
        }

    }

    private fun gorunenMesajSayisiniGuncelle(sonGorunenMesajSayisi : String) {
        var ref = FirebaseDatabase.getInstance().reference
            .child("sohbet_odasi")
            .child(secilenOdaID)
            .child("sohbet_odasindaki_kullanicilar")
            .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
            .child("son_gonderilen_mesaj_sayisi")
            .setValue(sonGorunenMesajSayisi)
    }

    private fun sohbetOdasindakiMesajlariGetir() {

        if (tumMesajlar==null){
            tumMesajlar = ArrayList<SohbetMesaj>()
            mesajID = HashSet<String>()
        }

        mMesajReference = FirebaseDatabase.getInstance().getReference()

        var sorgu = mMesajReference?.child("sohbet_odasi")
            ?.child(secilenOdaID)?.child("sohbet_odasi_mesajlari")
            ?.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {


                    for (tekMesaj in p0.children){

                        var geciciMesaj = SohbetMesaj()


                        var kullaniciID = tekMesaj.getValue(SohbetMesaj::class.java)?.kullanici_id

                        if (!mesajID!!.contains(tekMesaj.key)){

                            mesajID!!.add(tekMesaj.key!!)

                            if (kullaniciID!= null){
                                geciciMesaj.mesaj = tekMesaj.getValue(SohbetMesaj::class.java)?.mesaj
                                geciciMesaj.timestamp = tekMesaj.getValue(SohbetMesaj::class.java)?.timestamp
                                geciciMesaj.kullanici_id = tekMesaj.getValue(SohbetMesaj::class.java)?.kullanici_id

                                var kullaniciDetaylari = mMesajReference?.child("kullanici")
                                    ?.orderByKey()?.equalTo(kullaniciID)
                                kullaniciDetaylari?.addListenerForSingleValueEvent(object : ValueEventListener{
                                    override fun onCancelled(p0: DatabaseError) {

                                    }

                                    override fun onDataChange(p0: DataSnapshot) {

                                        var bulananKullanici = p0.children?.iterator()?.next()

                                        geciciMesaj.adi = bulananKullanici.getValue(kullanici::class.java)?.isim
                                        geciciMesaj.profil_resmi =  bulananKullanici.getValue(kullanici::class.java)?.profil_resmi
                                    }
                                })



                                tumMesajlar?.add(geciciMesaj)

                                myAdapter?.notifyDataSetChanged()

                                rvMesajYeri.scrollToPosition(myAdapter!!.itemCount-1)

                            }else{
                                geciciMesaj.mesaj = tekMesaj.getValue(SohbetMesaj::class.java)?.mesaj
                                geciciMesaj.timestamp = tekMesaj.getValue(SohbetMesaj::class.java)?.timestamp
                                geciciMesaj.adi = ""
                                geciciMesaj.profil_resmi = ""

                                tumMesajlar?.add(geciciMesaj)
                            }
                        }
                    }
                }

        })

        if (myAdapter == null){
            initMesajlarListesi()
        }


    }

    private fun initMesajlarListesi() {
        myAdapter =
            SohbetMesajRecyclerViewAdapter(
                this,
                tumMesajlar!!
            )
        rvMesajYeri.adapter = myAdapter
        rvMesajYeri.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        rvMesajYeri.scrollToPosition(myAdapter!!.itemCount-1)
    }

    private fun baslatMesajListener() {
        mMesajReference = FirebaseDatabase.getInstance().getReference().child("sohbet_odasi")
            .child(secilenOdaID).child("sohbet_odasi_mesajlari")
        mMesajReference?.addValueEventListener(mValueEventListener)

    }

    private fun kullaniciDinlemeyeBasla() {
        mAutStateListener = FirebaseAuth.AuthStateListener {

            var kullanici = it.currentUser

            if (kullanici == null){
                var intent = Intent(this@SohbetEkrani,GirisEkrani::class.java)
                startActivity(intent)
                finish()
            }

        }
    }

    override fun onStart() {
        super.onStart()
        activityAcikMi = true
        FirebaseAuth.getInstance().addAuthStateListener(mAutStateListener!!)
    }

    private fun mesajlarıOku() {

        var toplamMesajSayisi = 0

        var sorgu = FirebaseDatabase.getInstance().reference
            .child("sohbet_odasi")
            .orderByKey()
            .equalTo(secilenOdaID)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    var tekSohbetOdasi = p0.children.iterator().next()

                     toplamMesajSayisi = tekSohbetOdasi.child("sohbet_odasi_mesajlari").childrenCount.toInt()
                    var ref = FirebaseDatabase.getInstance().reference
                        .child("sohbet_odasi")
                        .child(secilenOdaID.toString())
                        .child("sohbet_odasindaki_kullanicilar")
                        .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                        .child("okunan_mesaj_sayisi")
                        .setValue(toplamMesajSayisi.toString())
                }
            })
    }

    override fun onStop() {
        super.onStop()
        activityAcikMi = false
        mesajlarıOku()
        if (mAutStateListener != null){
            FirebaseAuth.getInstance().removeAuthStateListener(mAutStateListener!!)
        }

    }

    override fun onResume() {
        super.onResume()
        kullaniciyiKontrolEt()
    }

    private fun kullaniciyiKontrolEt() {
        var kullanici = FirebaseAuth.getInstance().currentUser

        if (kullanici == null){
            var intent = Intent(this@SohbetEkrani,GirisEkrani::class.java)
            startActivity(intent)
            finish()
        }

    }

}


/* private fun sohbetOdasiYazismalariGetir() {
       var secilenSohbetOdasiid = intent.getStringExtra("sohbetodasi_id")
       tumMesajlar = ArrayList<SohbetMesaj>()

       var ref = FirebaseDatabase.getInstance().reference
       ref.child("sohbet_odasi").child(secilenSohbetOdasiid).child("sohbet_odasi_mesajlari")
           .addListenerForSingleValueEvent(object : ValueEventListener{
               override fun onCancelled(p0: DatabaseError) {

               }

               override fun onDataChange(p0: DataSnapshot) {

                   for (mesajlar in p0.children){

                       var eklenecekMesaj = SohbetMesaj()

                       var kullaniciID = mesajlar.getValue(SohbetMesaj::class.java)?.kullanici_id

                       if (kullaniciID != null){

                           eklenecekMesaj.kullanici_id = kullaniciID
                           eklenecekMesaj.mesaj = mesajlar.getValue(SohbetMesaj::class.java)?.mesaj
                           eklenecekMesaj.timestamp = mesajlar.getValue(SohbetMesaj::class.java)?.timestamp
                           tumMesajlar.add(eklenecekMesaj)

                       }else{
                           eklenecekMesaj.mesaj = mesajlar.getValue(SohbetMesaj::class.java)?.mesaj
                           eklenecekMesaj.timestamp = mesajlar.getValue(SohbetMesaj::class.java)?.timestamp
                           tumMesajlar.add(eklenecekMesaj)
                       }
                   }

               }
           })
   }*/
