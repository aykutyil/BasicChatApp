package com.example.chatprogram.Dialogs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.example.chatprogram.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class OnayMailiTekrarGonderFragment : DialogFragment() {

    lateinit var mContext:FragmentActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,avedInstanceState: Bundle?): View?
    {

        val view = inflater.inflate(R.layout.fragment_onay, container, false)

        mContext = activity!!

        var mail: EditText = view.findViewById(R.id.etDialogMail)
        var sifre: EditText = view.findViewById(R.id.etDialogSifre)

        var btnKapat = view.findViewById<Button>(R.id.btnDialogKapat)
        btnKapat.setOnClickListener {
            dialog?.dismiss()
        }

        var btnGonder:Button = view.findViewById(R.id.btnDialogGonder)
        btnGonder.setOnClickListener {

            GirisYaparakOnayMailiniTekrarGonder(mail.text.toString(),sifre.text.toString())

        }



        return view
    }

    private fun GirisYaparakOnayMailiniTekrarGonder(mail: String, sifre: String) {

        var credantial = EmailAuthProvider.getCredential(mail,sifre)

        FirebaseAuth.getInstance().signInWithCredential(credantial)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    OnayMailiGonder()
                    dialog?.dismiss()
                }else{
                    Toast.makeText(mContext,"Mail veya Şifre Hatalı",Toast.LENGTH_SHORT).show()
                }
            }

    }

    private fun OnayMailiGonder() {
        var kullanici = FirebaseAuth.getInstance().currentUser
var a = kullanici
        if (kullanici != null){
            kullanici.sendEmailVerification()
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        Toast.makeText(mContext,"Mail Tekrar Gönderildi.",Toast.LENGTH_SHORT).show()
                        FirebaseAuth.getInstance().signOut()
                    }
                    else{
                        Toast.makeText(mContext,"Mail Gönderimi Başarısız.",Toast.LENGTH_SHORT).show()
                        FirebaseAuth.getInstance().signOut()
                    }
                }
        }
        else{
            Log.e("Hata:", "Burada")
        }
    }

}
