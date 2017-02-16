package com.paulzin.smarthouseandroid;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.paulzin.smarthouseandroid.fb.FbManager;

@TargetApi(Build.VERSION_CODES.N)
public class QuickSettingsService extends TileService {
    private static final String SERVICE_STATUS_FLAG = "serviceStatus";
    private static final String PREFERENCES_KEY = "com.google.android_quick_settings";

    @Override
    public void onClick() {
        updateTile();
    }

    private void updateTile() {
        Tile tile = this.getQsTile();
        boolean isActive = getServiceStatus();
        int newState = isActive ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE;
        // TODO: 16.02.17 remove hardcoded id - implement "default device id" for user
        FbManager.INSTANCE.toggleDevice("0000000031e807ba", isActive);
        tile.setState(newState);
        tile.updateTile();
    }

    private boolean getServiceStatus() {
        SharedPreferences prefs = getApplicationContext()
                .getSharedPreferences(PREFERENCES_KEY, MODE_PRIVATE);

        boolean isActive = prefs.getBoolean(SERVICE_STATUS_FLAG, false);
        isActive = !isActive;

        prefs.edit().putBoolean(SERVICE_STATUS_FLAG, isActive).apply();
        return isActive;
    }
}
