package com.example.ocr

data class SaveDetailInfo(val name:String, val email:String, val company:String, val phone:String, val web:String,val notkey:String){
    constructor() : this("","","","","","")
}
