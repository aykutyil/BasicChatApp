package com.example.chatprogram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.view.WindowManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)
        initAuthStateListener()
        initFCM()
        getPenddingIntent()

    }

    private fun getPenddingIntent() {
        var gelenIntent = intent
        if(gelenIntent.hasExtra("sohbet_odasi_id")){
            var intent = Intent(this,SohbetEkrani::class.java)
            intent.putExtra("sohbet_odasi_id",gelenIntent.getStringExtra("sohbet_odasi_id"))
            startActivity(intent)
        }

    }

    private fun initFCM() {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {

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

    private fun KullaniciBilgileriniEkle() {
        var kullanici = FirebaseAuth.getInstance().currentUser
        if (kullanici != null){
            tvKullaniciAdi.text = if (kullanici.displayName.isNullOrEmpty()){ "Tanimlanamadi"} else {kullanici.displayName}
            tvKullaniciEmail.text = kullanici.email
            tvKullaniciID.text = kullanici.uid
        }
    }

    private fun initAuthStateListener() {

        mAuthStateListener = object : FirebaseAuth.AuthStateListener {

            override fun onAuthStateChanged(p0: FirebaseAuth) {
                var kullanici = p0.currentUser

                if (kullanici != null)
                {

                }else{
                    var intent = Intent(this@MainActivity,GirisEkrani::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or  Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finish()
                }
            }

        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menusecenekleri,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when(item?.itemId){

        R.id.menuCikisYap ->{
            cikisYap()
            return true
        }

            R.id.menuHesapAyarlari ->{
                var intent = Intent(this@MainActivity,HesapAyarlariActivity::class.java)
                startActivity(intent)
                return true
            }

            R.id.menuSohbetOdasi ->{
                var intent = Intent(this@MainActivity,SohbetOdasiActivity::class.java)
                startActivity(intent)
                return true
            }

        }


        return super.onOptionsItemSelected(item)
    }

    private fun cikisYap() {

        FirebaseAuth.getInstance().signOut()

    }

    override fun onStart() {

        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener)
    }

    override fun onStop() {

        super.onStop()
        if (mAuthStateListener != null){
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateListener)
        }

    }

    override fun onResume() {
        super.onResume()
        kullaniciyiKontrolEt()
        KullaniciBilgileriniEkle()

    }

    private fun kullaniciyiKontrolEt() {
        var kullanici = FirebaseAuth.getInstance().currentUser

        if (kullanici == null){
            var intent = Intent(this@MainActivity,GirisEkrani::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }
    }


}
