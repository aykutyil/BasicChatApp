package com.example.chatprogram.Dialogs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.example.chatprogram.R
import com.google.firebase.auth.FirebaseAuth

/**
 * A simple [Fragment] subclass.
 */
class SifreResetleFragment : DialogFragment() {

    lateinit var etEmail:EditText
    lateinit var mContext:FragmentActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View?
    {
        mContext = activity!!

        var view = inflater.inflate(R.layout.fragment_sifre_resetle, container, false)

        var btnIptal:Button = view.findViewById(R.id.btnISifreSifirlaptal)
        btnIptal.setOnClickListener {
            dialog?.dismiss()
        }
        etEmail = view.findViewById(R.id.etSifreSifirlamaEmail)

        var btnGonder = view.findViewById<Button>(R.id.btnSifreSifirlaGonder)
        btnGonder.setOnClickListener {

            FirebaseAuth.getInstance().sendPasswordResetEmail(etEmail.text.toString())
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        Toast.makeText(mContext,"Mail Gönderildi",Toast.LENGTH_SHORT).show()
                        dialog?.dismiss()
                    }
                    else{
                        Toast.makeText(mContext,"Mail Gönderilemedi." + it.exception?.message,Toast.LENGTH_SHORT).show()
                    }
                }

        }

        return view
    }

}
