package com.paulzin.smarthouseandroid.model

class Device(val id: String, val name: String, val imageUrl: String, val turnedOn: Boolean) {
    constructor() : this("", "", "", true)
}