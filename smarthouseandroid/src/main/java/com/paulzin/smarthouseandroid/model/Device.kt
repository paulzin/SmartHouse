package com.paulzin.smarthouseandroid.model

data class Device(var lastUserUid: String = "",
                  var deviceId: String = "",
                  val name: String = "Living Room Lamp 1",
                  val imageUrl: String = "https://goo.gl/eUdApD",
                  val turnedOn: Boolean = false)