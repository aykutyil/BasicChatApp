package com.example.chatprogram.Model

class kullanici {

    var isim:String? = null
    var telefon:String? = null
    var profil_resmi:String? = null
    var kullanici_id:String? = null
    var seviye:String? = null
    var mesaj_token:String?=null

    constructor(isim:String?,telefon:String?,profil_resmi:String?,kullanici_id:String?,seviye:String? = null){
        this.isim = isim
        this.kullanici_id = kullanici_id
        this.profil_resmi = profil_resmi
        this.seviye = seviye
        this.telefon = telefon
    }

    constructor(){}


}