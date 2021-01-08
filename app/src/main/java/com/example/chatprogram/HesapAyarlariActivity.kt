package com.example.chatprogram

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.chatprogram.Dialogs.ProfilResmiFragment
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_hesap_ayarlari.*
import java.io.ByteArrayOutputStream

class HesapAyarlariActivity : AppCompatActivity(), ProfilResmiFragment.onProfilResimListener {

    var izinlerBool:Boolean = false
    var galeridenGelenFoto:Uri? = null
    var kameradanGelenResim:Bitmap?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_hesap_ayarlari)

        var kullanici = FirebaseAuth.getInstance().currentUser



        kullaniciBilgilerinOku()

        etKullaniciAdi.setText(kullanici?.displayName.toString())

        tvSifremiUnuttum.setOnClickListener {

            FirebaseAuth.getInstance().sendPasswordResetEmail(FirebaseAuth.getInstance().currentUser?.email.toString())
                .addOnCompleteListener {

                    if (it.isSuccessful){
                        Toast.makeText(this@HesapAyarlariActivity,"Mail Gönderildi",Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this@HesapAyarlariActivity,"Mail Gönderilemedi",Toast.LENGTH_SHORT).show()
                    }
                }
        }

        btnDegisiklikleriKaydet.setOnClickListener {

            if (etKullaniciAdi.text.toString().isNotEmpty()){
                if (!etKullaniciAdi.text.toString().equals(kullanici?.displayName.toString())){
                    var bilgileriGuncelle = UserProfileChangeRequest.Builder()
                        .setDisplayName(etKullaniciAdi.text.toString())
                        .build()

                    kullanici?.updateProfile(bilgileriGuncelle)
                        ?.addOnCompleteListener {
                            if (it.isSuccessful){

                                FirebaseDatabase.getInstance().reference
                                    .child("kullanici")
                                    .child(kullanici.uid.toString())
                                    .child("isim")
                                    .setValue(etKullaniciAdi.text.toString())
                                Toast.makeText(this@HesapAyarlariActivity,"Değişiklik Yapıldı.",Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }else{
                Toast.makeText(this@HesapAyarlariActivity,"Kullanıcı Adını Doldurunuz.",Toast.LENGTH_SHORT).show()
            }

            if (etKullaniciTelefon.text.toString().isNotEmpty()){

                FirebaseDatabase.getInstance().reference
                    .child("kullanici")
                    .child(kullanici!!.uid.toString())
                    .child("telefon")
                    .setValue(etKullaniciTelefon.text.toString())

            }

            if(galeridenGelenFoto != null){
                fotografComperessed(galeridenGelenFoto!!)
            }else if (kameradanGelenResim != null){
                fotografComperessed(kameradanGelenResim!!)
            }


        }

        tvMailSifreGuncelle.setOnClickListener {

            if (etKullaniciSuankiSifre.text.toString().isNotEmpty()){
                var credantial = EmailAuthProvider.getCredential(kullanici?.email.toString(),etKullaniciSuankiSifre.text.toString())
                kullanici?.reauthenticate(credantial)
                    ?.addOnCompleteListener {
                      if (it.isSuccessful){

                          guncelleLayout.visibility = View.VISIBLE
                          btnYeniMailGuncelle.setOnClickListener {
                            YeniMailiGuncelle()
                          }
                          btnYeniSifreGuncelle.setOnClickListener {
                              YeniSifreyiGuncelle()
                          }

                      }else{
                          Toast.makeText(this@HesapAyarlariActivity,"Şifre Yanlış Girildi.",Toast.LENGTH_SHORT).show()
                          guncelleLayout.visibility = View.INVISIBLE
                      }
                    }
            }




        }

        imgCircleProfil.setOnClickListener {

            if (izinlerBool){

                var dialog =
                    ProfilResmiFragment()
                dialog.show(supportFragmentManager,"dialogProfilGoster")
            }else{
                izinIste()
            }
        }

    }

    private fun kullaniciBilgilerinOku() {

        var kullanici = FirebaseAuth.getInstance().currentUser
        var referans = FirebaseDatabase.getInstance().reference

        tvMailAdresi.text = kullanici?.email.toString()


        //query 1

        var sorgu = referans.child("kullanici")
            .orderByKey()
            .equalTo(kullanici?.uid)
        sorgu.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                for (i in p0!!.children){
                    var okunanElemanlar = i.getValue(com.example.chatprogram.Model.kullanici::class.java)
                    etKullaniciAdi.setText(okunanElemanlar?.isim.toString())
                    etKullaniciTelefon.setText(okunanElemanlar?.telefon.toString())
                    Picasso.get().load(okunanElemanlar?.profil_resmi).resize(100,100).into(imgCircleProfil)
                    Log.e("Veriler:" , "Kullanıcı Adi : " + okunanElemanlar?.isim + " Kullanici uid: " + okunanElemanlar!!.kullanici_id)
                }

            }
        })

    }

    private fun YeniSifreyiGuncelle() {

        var kullanici = FirebaseAuth.getInstance().currentUser

        if (kullanici != null) {
            kullanici.updatePassword(etYeniSifre.text.toString())
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        Toast.makeText(this@HesapAyarlariActivity,"Şifreniz Değiştirilmiştir. Yeniden Giriş Yapınız.",Toast.LENGTH_SHORT).show()
                        FirebaseAuth.getInstance().signOut()
                        GirisEkraninaYonlendir()
                    }
                }
        }

    }

    private fun YeniMailiGuncelle() {
        var kullanici = FirebaseAuth.getInstance().currentUser

        if (kullanici != null) {

            FirebaseAuth.getInstance().fetchSignInMethodsForEmail(etYeniEmail.text.toString()).addOnCompleteListener{
                if (it.getResult()?.signInMethods!!.isNotEmpty()){

                    Toast.makeText(this@HesapAyarlariActivity,"Mail Kullanımdadır.",Toast.LENGTH_SHORT).show()

            }else{
                    kullanici.updateEmail(etYeniEmail.text.toString())
                        .addOnCompleteListener {
                            if (it.isSuccessful){
                                Toast.makeText(this@HesapAyarlariActivity,"Mailiniz Değiştirilmiştir. Yeniden Giriş Yapınız.",Toast.LENGTH_SHORT).show()
                                FirebaseAuth.getInstance().signOut()
                                GirisEkraninaYonlendir()
                            }
                        }
                }

            }
        }

    }

    fun GirisEkraninaYonlendir(){
        var intent = Intent(this@HesapAyarlariActivity,GirisEkrani::class.java)
        startActivity(intent)
    }

    override fun getResimYolu(resimPath: Uri?) {

        galeridenGelenFoto = resimPath
        Picasso.get().load(galeridenGelenFoto).resize(100,100).into(imgCircleProfil)
    }

    override fun getResimBitmap(bitmap: Bitmap?) {
        kameradanGelenResim = bitmap
        imgCircleProfil.setImageBitmap(kameradanGelenResim)
    }

    private fun izinIste() {

        var izinler = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    android.Manifest.permission.CAMERA)
        if ( ContextCompat.checkSelfPermission(this,izinler[0])==PackageManager.PERMISSION_GRANTED &&
             ContextCompat.checkSelfPermission(this,izinler[1])==PackageManager.PERMISSION_GRANTED &&
             ContextCompat.checkSelfPermission(this,izinler[2])==PackageManager.PERMISSION_GRANTED){

            izinlerBool = true

        }else{
            ActivityCompat.requestPermissions(this,izinler,150)
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int,permissions: Array<out String>,grantResults: IntArray) {
        if (requestCode==150){
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED
                && grantResults[2]==PackageManager.PERMISSION_GRANTED){

                var dialog =
                    ProfilResmiFragment()
                dialog.show(supportFragmentManager,"dialogProfilGoster")

            }
            else{
                Toast.makeText(this@HesapAyarlariActivity,"Tüm izinler Verilmeli.",Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class BackGroundResimProcess:AsyncTask<Uri,Void,ByteArray>{

        var mBitmap:Bitmap?=null

        constructor(){}
        constructor(bitmap: Bitmap?){
            if (bitmap != null){
                mBitmap = bitmap
            }

        }

        override fun onPreExecute() {
            super.onPreExecute()
        }
        override fun doInBackground(vararg params: Uri?): ByteArray {

            if (mBitmap == null){
                mBitmap = MediaStore.Images.Media.getBitmap(this@HesapAyarlariActivity.contentResolver,params[0])
            }

            var resimByte:ByteArray? = null
            for (i in 1..5){
                resimByte = convertBitmaptoByte(mBitmap,100/i)
            }
            return resimByte!!
        }

        private fun convertBitmaptoByte(mBitmap: Bitmap?, i: Int): ByteArray? {
            var stream = ByteArrayOutputStream()
            mBitmap?.compress(Bitmap.CompressFormat.JPEG,i,stream)

            return  stream.toByteArray()
        }

        override fun onPostExecute(result: ByteArray?) {

            super.onPostExecute(result)

            uploadResim(result)
        }

    }

    private fun uploadResim(result: ByteArray?) {

       progressGoster()

        var storageReference = FirebaseStorage.getInstance().reference
        var eklenecekYer = storageReference.child("image/users/" + FirebaseAuth.getInstance().currentUser?.uid +
                                                                    "/profil_resmi")
        var uploadIsi = eklenecekYer.putBytes(result!!)

         uploadIsi.addOnCompleteListener {
            if (it.isSuccessful){


                Toast.makeText(this@HesapAyarlariActivity,"Resim Yükiendi.", Toast.LENGTH_SHORT).show()

                eklenecekYer.downloadUrl.addOnSuccessListener {
                    Log.e("deney:" , it.toString())
                    var referans = FirebaseDatabase.getInstance().reference
                    referans.child("kullanici")
                        .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                        .child("profil_resmi")
                        .setValue(it.toString())
                }.addOnFailureListener{
                    Toast.makeText(this@HesapAyarlariActivity,"Resim Yüklenirken Sorun Oluştu.",Toast.LENGTH_SHORT).show()
                }



                progressKapat()
            }
        }

    }

    private fun fotografComperessed(galeridenGelenFoto: Uri) {

        var compress = BackGroundResimProcess()
        compress.execute(galeridenGelenFoto)

    }

    private fun fotografComperessed(kameradanGelenFoto: Bitmap) {

        var compress = BackGroundResimProcess(kameradanGelenFoto)
        var uri:Uri? = null
        compress.execute(uri)

    }

    fun progressGoster()
    {
        progressBarResim.visibility = View.VISIBLE
    }
    fun progressKapat(){
        progressBarResim.visibility = View.INVISIBLE
    }

}
