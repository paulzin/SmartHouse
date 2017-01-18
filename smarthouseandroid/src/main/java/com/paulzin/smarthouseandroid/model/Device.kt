package com.paulzin.smarthouseandroid.model

class Device(val id: String, val name: String, val status : String) {
    constructor() : this("", "", "")
}