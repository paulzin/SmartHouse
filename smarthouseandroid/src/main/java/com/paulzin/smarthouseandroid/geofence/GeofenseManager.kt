package com.paulzin.smarthouseandroid.geofence

import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest

class GeofenseManager {
    val defaultLatitude = 49.839322
    val defaultLongitude = 24.031233
    val defaultRadius = 20f

    private val defaultReqId = "defaultGeofence"

    public fun getDefaultGeofence(): Geofence {
        return Geofence.Builder()
                .setRequestId(defaultReqId)
                .setCircularRegion(
                        defaultLatitude,
                        defaultLongitude,
                        defaultRadius
                )
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()
    }

    public fun getGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(getDefaultGeofence())
                .build()
    }
}

