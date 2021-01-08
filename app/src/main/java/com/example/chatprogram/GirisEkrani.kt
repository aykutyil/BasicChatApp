package com.example.chatprogram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.example.chatprogram.Dialogs.OnayMailiTekrarGonderFragment
import com.example.chatprogram.Dialogs.SifreResetleFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_giris_ekrani.*

class GirisEkrani : AppCompatActivity() {

    lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_giris_ekrani)
        initMyAuthStateListener()

        tvOnayMail.setOnClickListener {

            var dialogGoster =
                OnayMailiTekrarGonderFragment()
            dialogGoster.show(supportFragmentManager,"dialogGoster")

        }

        tvKayitOl.setOnClickListener {
            var intent = Intent(this@GirisEkrani,RegisterActivity::class.java)
            startActivity(intent)
        }

        btnGiris.setOnClickListener {

            if (etMail.text.toString().isNotEmpty() && etSifre.text.toString().isNotEmpty()){
                    ProgressBarGoster()
                FirebaseAuth.getInstance().signInWithEmailAndPassword(etMail.text.toString(),etSifre.text.toString())
                    .addOnCompleteListener(object : OnCompleteListener<AuthResult>{
                        override fun onComplete(p0: Task<AuthResult>) {
                            if (p0.isSuccessful){
                                ProgressBarKapat()
                                if (!p0.result!!.user!!.isEmailVerified){
                                    FirebaseAuth.getInstance().signOut()
                                }
                                //Toast.makeText(this@GirisEkrani,"Giriş Başarılı: " + FirebaseAuth.getInstance().currentUser?.email,Toast.LENGTH_SHORT).show()

                            }else{
                                ProgressBarKapat()
                                Toast.makeText(this@GirisEkrani,"Giriş Başarısız: " + p0.exception?.message,Toast.LENGTH_SHORT).show()

                            }
                        }

                    })
            }else{
                Toast.makeText(this,"Lütfen boş bırakmayınız.",Toast.LENGTH_SHORT).show()
            }
        }

        tvSifremiUnuttum.setOnClickListener {

            var dialogGoster =
                SifreResetleFragment()
            dialogGoster.show(supportFragmentManager,"sifreResetlemeGoster")

        }
    }

    private fun ProgressBarGoster(){
        progressLogin.visibility = View.VISIBLE
    }

    private fun ProgressBarKapat(){
        progressLogin.visibility = View.INVISIBLE
    }

    private fun initMyAuthStateListener(){

        mAuthStateListener = object : FirebaseAuth.AuthStateListener{
            override fun onAuthStateChanged(p0: FirebaseAuth) {

                var kullanici = p0.currentUser

                if (kullanici != null){

                    if (kullanici.isEmailVerified){
                        var intent = Intent(this@GirisEkrani,MainActivity::class.java)
                        startActivity(intent)
                        finish()

                    }else{
                        Toast.makeText(this@GirisEkrani,"Mail Onaylanmadan giriş yapılamaz.",Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener)
    }



    override fun onStop() {
        super.onStop()
        FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateListener)
    }
}
