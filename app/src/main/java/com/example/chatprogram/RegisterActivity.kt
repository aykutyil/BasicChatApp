package com.example.chatprogram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.example.chatprogram.Model.kullanici
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_register)

        btnKayitOl.setOnClickListener {

            if (etMail.toString().isNotEmpty()&& etSifre.toString().isNotEmpty() && etSifreTekrar.toString().isNotEmpty()){

                if (etSifre.text.toString().equals(etSifreTekrar.text.toString())){
                    YeniUyeOlustur(etMail.text.toString(),etSifre.text.toString())
                }else{
                    Toast.makeText(this,"Şifreler Aynı Giriniz.",Toast.LENGTH_SHORT).show()
                    FirebaseAuth.getInstance().signOut()
                }

            }else{
                Toast.makeText(this,"Lütfen Bilgileri Giriniz",Toast.LENGTH_SHORT).show()

            }


        }

        ProgressBarKapat()

    }

    private fun YeniUyeOlustur(mail: String, sifre: String) {

        ProgressBarGoster()

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(mail,sifre)
            .addOnCompleteListener {
                if (it.isSuccessful){

                    onayMailiGonder()

                    var veriTabaniKayit =
                        kullanici()
                    veriTabaniKayit.isim = etMail.text.toString().substring(0,etMail.text.toString().indexOf("@"))
                    veriTabaniKayit.kullanici_id = FirebaseAuth.getInstance().uid
                    veriTabaniKayit.profil_resmi = ""
                    veriTabaniKayit.seviye = "1"
                    veriTabaniKayit.telefon = "123"

                    FirebaseDatabase.getInstance().reference
                        .child("kullanici")
                        .child(FirebaseAuth.getInstance().currentUser!!.uid)
                        .setValue(veriTabaniKayit)
                        .addOnCompleteListener {
                            if (it.isSuccessful){
                                Toast.makeText(this,"Kayıt Başarılı",Toast.LENGTH_SHORT).show()
                                FirebaseAuth.getInstance().signOut()
                            }
                        }

                }
                else{
                    Toast.makeText(this,"Kayıt Başarısız" + it.exception?.message,Toast.LENGTH_SHORT).show()

                }
            }

    }

    private fun onayMailiGonder() {

        var kullanici = FirebaseAuth.getInstance().currentUser

        if (kullanici != null){

            kullanici.sendEmailVerification().addOnCompleteListener {
                if (it.isSuccessful){
                    Toast.makeText(this@RegisterActivity,"Mailinizi Onaylayınız.",Toast.LENGTH_SHORT).show()
                    var intent = Intent(this@RegisterActivity,GirisEkrani::class.java)
                    startActivity(intent)

                }else{
                    Toast.makeText(this@RegisterActivity,"Mail Gönderirken Hata Oluştu." + it.exception?.message,Toast.LENGTH_SHORT).show()

                }
            }

        }

    }

    private fun ProgressBarGoster(){
        progressBar.visibility = View.VISIBLE
    }
    private fun ProgressBarKapat(){
        progressBar.visibility = View.INVISIBLE
    }

    fun GirisEkraninaYonlendir(){
        var intent = Intent(this@RegisterActivity,GirisEkrani::class.java)
        startActivity(intent)
    }
}
