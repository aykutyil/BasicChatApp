package com.example.chatprogram.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.chatprogram.Model.SohbetMesaj
import com.example.chatprogram.R
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.teksatirmesaj.view.*

class SohbetMesajRecyclerViewAdapter(context: Context,tumMesajlar:ArrayList<SohbetMesaj>):RecyclerView.Adapter<SohbetMesajRecyclerViewAdapter.SohbetMesajViewHolder>() {

    var mContext = context
    var mTumMesajlar = tumMesajlar


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SohbetMesajViewHolder {
        var inflater = LayoutInflater.from(mContext)

        var tekSatirMEsaj:View? =null

        if (viewType == 1){
            tekSatirMEsaj = inflater.inflate(R.layout.teksatirmesaj2,parent,false)
        }else{
            tekSatirMEsaj = inflater.inflate(R.layout.teksatirmesaj,parent,false)
        }

        return SohbetMesajViewHolder(tekSatirMEsaj)
    }

    override fun getItemCount(): Int {
       return mTumMesajlar.size
    }

    override fun getItemViewType(position: Int): Int {
        if(mTumMesajlar.get(position).kullanici_id?.toString().equals(FirebaseAuth.getInstance().currentUser?.uid.toString())){
            return 1
        }else{
            return 2
        }
    }

    override fun onBindViewHolder(holder: SohbetMesajViewHolder, position: Int) {
        var oAnOlusanMesaj = mTumMesajlar.get(position)
        holder.setData(oAnOlusanMesaj,position)
    }

    inner class SohbetMesajViewHolder(item: View):RecyclerView.ViewHolder(item) {

        var tekSatirItem = item as ConstraintLayout

        var ppFoto = tekSatirItem.imgYazanPP
        var mesaj = tekSatirItem.tvMesaj
        var yazanAdi = tekSatirItem.tvYazanAdi
        var tarihBilgisi = tekSatirItem.tvTarihBilgisi

        fun setData(oAnOlusanMesaj: SohbetMesaj, position: Int) {

            mesaj.text = oAnOlusanMesaj.mesaj
            yazanAdi.text = oAnOlusanMesaj.adi
            tarihBilgisi.text = oAnOlusanMesaj.timestamp

            if (!oAnOlusanMesaj.profil_resmi.isNullOrEmpty()){
                Picasso.get().load(oAnOlusanMesaj.profil_resmi).resize(48,48).into(ppFoto)
            }

        }


    }
}