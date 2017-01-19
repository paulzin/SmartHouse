package com.paulzin.smarthouseandroid.model

data class Device(var currentUid : String,
             var deviceId: String,
             val name: String,
             val imageUrl: String,
             val turnedOn: Boolean) {
    constructor() : this("", "", "Smart Lamp", "", true)
}