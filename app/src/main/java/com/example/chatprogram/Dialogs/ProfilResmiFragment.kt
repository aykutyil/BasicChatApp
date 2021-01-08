package com.example.chatprogram.Dialogs

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.chatprogram.R

/**
 * A simple [Fragment] subclass.
 */
class ProfilResmiFragment : DialogFragment() {

    interface onProfilResimListener{
        fun getResimYolu(resimPath: Uri?)
        fun getResimBitmap(bitmap: Bitmap?)
    }

    lateinit var mProfilResimListener: onProfilResimListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_profil_resmi, container, false)

        var tvGaleridenSeç: TextView = view.findViewById(R.id.tvGaleridenSec)
        var tvFotoCek: TextView = view.findViewById(R.id.tvYeniForoCek)

        tvFotoCek.setOnClickListener {

            var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent,200)

        }

        tvGaleridenSeç.setOnClickListener {

            var intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type="image/*"
            startActivityForResult(intent,100)

        }


        return view


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100&& resultCode == Activity.RESULT_OK && data != null){

            var galeridenSecilenResimYolu = data.data
            mProfilResimListener.getResimYolu(galeridenSecilenResimYolu)
            dialog?.dismiss()

        }else if(requestCode == 200 && resultCode == Activity.RESULT_OK && data != null){

            var kameradanCekilenResim = data.extras.get("data") as Bitmap
            mProfilResimListener.getResimBitmap(kameradanCekilenResim)
            dialog?.dismiss()

        }

    }

    override fun onAttach(context: Context) {

        mProfilResimListener = activity as onProfilResimListener

        super.onAttach(context)
    }

}
