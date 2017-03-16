package com.paulzin.smarthouseandroid.quick_settings

import android.annotation.TargetApi
import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.paulzin.smarthouseandroid.R
import com.paulzin.smarthouseandroid.fb.FbManager

@TargetApi(Build.VERSION_CODES.N)
class QuickSettingsService : TileService() {

    override fun onClick() {
        updateTile()
    }

    private fun updateTile() {
        val tile = this.qsTile
        val isActive = getServiceStatus()
        val newState = if (isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        // TODO: 16.02.17 remove hardcode. Implement "default device" feature
        FbManager.toggleDevice("0000000031e807ba", isActive)

        // This is a workaround to fix a possible Android bug
        // Using different drawable icons for active and not active states
        val icon = Icon.createWithResource(applicationContext,
                if (isActive) R.drawable.ic_wb_incandescent_black_24dp
                else R.drawable.ic_wb_incandescent_black2_24dp)

        tile.icon = icon
        tile.state = newState
        tile.updateTile()
    }

    private fun getServiceStatus(): Boolean {
        val prefs = applicationContext
                .getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)

        var isActive = prefs.getBoolean(SERVICE_STATUS_FLAG, false)
        isActive = !isActive

        prefs.edit().putBoolean(SERVICE_STATUS_FLAG, isActive).apply()

        return isActive
    }

    companion object {
        private val SERVICE_STATUS_FLAG = "serviceStatus"
        private val PREFERENCES_KEY = "com.paulzin.smarthouseandroid.quick_settings"
    }
}
