package com.example.chatprogram.adapters

import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.chatprogram.Model.SohbetOdasi
import com.example.chatprogram.Model.kullanici
import com.example.chatprogram.R
import com.example.chatprogram.SohbetEkrani
import com.example.chatprogram.SohbetOdasiActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.teksatirsohbetodasi.view.*

class SohbetOdasiGetirAdapter(tumSohbetOdalari: ArrayList<SohbetOdasi>,sohbetOdasiActivity: SohbetOdasiActivity) :RecyclerView.Adapter<SohbetOdasiGetirAdapter.SohbetOdasiGetirAdapterViewHolder>() {

    var gelenTumSohbetOdalari = tumSohbetOdalari
    var mActivity = sohbetOdasiActivity

    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): SohbetOdasiGetirAdapterViewHolder {
        var inflater = LayoutInflater.from(parent.context)
        var tekSatir = inflater.inflate(R.layout.teksatirsohbetodasi,parent,false)

        return SohbetOdasiGetirAdapterViewHolder(tekSatir)
    }

    override fun getItemCount(): Int {
        return gelenTumSohbetOdalari.size
    }

    override fun onBindViewHolder(holder: SohbetOdasiGetirAdapterViewHolder, position: Int) {

        var oAnOlusturulanSohbetOdasi = gelenTumSohbetOdalari.get(position)
        holder.setData(oAnOlusturulanSohbetOdasi,position)

    }

     inner class SohbetOdasiGetirAdapterViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {

        var itemv = itemView as RelativeLayout

        var odaAdi = itemv.tvSohbetODasiAdi
        var odaOlusturanAdi = itemv.tvSohbetOdasiOlusturanAdi
        var odaSeviye = itemv.tvSohbetOdasiSeviye
        var odaProfilFoto = itemv.imgSohbetOdasipp
        var btnOdaSili = itemv.btnOdaSil
        var odaMesajSayisi = itemv.tvMesajSayisi

        fun setData(oAnOlusturulanSohbetOdasi: SohbetOdasi, position: Int) {

            odaAdi.text = oAnOlusturulanSohbetOdasi.sohbetodasi_adi
            odaSeviye.text = oAnOlusturulanSohbetOdasi.seviye

            btnOdaSili.setOnClickListener {

                if (oAnOlusturulanSohbetOdasi.olusturan_id!!.equals(FirebaseAuth.getInstance().currentUser?.uid)){

                    var dialog = AlertDialog.Builder(itemView.context)
                    dialog.setCancelable(true)
                    dialog.setTitle("Sohbet Odası Sil")
                    dialog.setMessage("Emin misiniz?")
                    dialog.setPositiveButton("Sil",object : DialogInterface.OnClickListener{
                        override fun onClick(dialog: DialogInterface?, which: Int) {

                            mActivity.sohbetOdasiSil(oAnOlusturulanSohbetOdasi.sohbetodasi_id.toString())

                        }

                    })

                    dialog.setNegativeButton("İptal",object : DialogInterface.OnClickListener{
                        override fun onClick(dialog: DialogInterface?, which: Int) {

                        }

                    })

                    dialog.show()

                }else{
                    Toast.makeText(itemView.context,"Sohbet Odasini Sadece Kuran Silebilir",Toast.LENGTH_SHORT).show()
                }

            }

            itemv.setOnClickListener {

                //Kullanıcıya bildirim atabilmek için odaya giriş yapan kullanıcıların tokenlarını kaydetttiğimiz metod.
               // kullaniciTokenKaydet(oAnOlusturulanSohbetOdasi)


                var intent = Intent(mActivity,SohbetEkrani::class.java)
                intent.putExtra("sohbetodasi_id",oAnOlusturulanSohbetOdasi.sohbetodasi_id)
                mActivity.startActivity(intent)
            }

            odaMesajSayisi.text = (oAnOlusturulanSohbetOdasi.sohbet_odasi_mesajlari)?.size.toString()

            var referans = FirebaseDatabase.getInstance().reference
            referans.child("kullanici").orderByKey().equalTo(oAnOlusturulanSohbetOdasi.olusturan_id)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        for (i in p0.children)
                        {
                            var ppFoto = i.getValue(kullanici::class.java)?.profil_resmi.toString()
                            Picasso.get().load(ppFoto).into(odaProfilFoto)
                            odaOlusturanAdi.text = i.getValue(kullanici::class.java)?.isim.toString()
                        }
                    }
                })

        }


          /*fun kullaniciTokenKaydet(oAnOlusturulanSohbetOdasi: SohbetOdasi) {

            var ref = FirebaseDatabase.getInstance().reference
                .child("sohbet_odasi")
                .child(oAnOlusturulanSohbetOdasi.sohbetodasi_id.toString())
                .child("sohbet_odasindaki_kullanicilar")
                .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                .child("okunan_mesaj_sayisi")
                .setValue((oAnOlusturulanSohbetOdasi.sohbet_odasi_mesajlari)?.size.toString())*/

        //}


    }
}