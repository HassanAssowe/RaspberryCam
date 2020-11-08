package com.hassanassowe.raspberrycam.Classes;

import android.content.res.Configuration;
import android.graphics.drawable.Drawable;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;

import com.hassanassowe.raspberrycam.R;

public class MyApplication extends android.app.Application {

    public void onCreate() {
        super.onCreate();
        int nightModeFlags =
                getApplicationContext().getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                Drawable button = AppCompatResources.getDrawable(this, R.drawable.ic_baseline_nights_stay_24);
                Drawable wrappedDrawable = DrawableCompat.wrap(button);
                DrawableCompat.setTint(wrappedDrawable, getResources().getColor(R.color.colorPrimary));
                break;

            case Configuration.UI_MODE_NIGHT_NO:

                break;

            case Configuration.UI_MODE_NIGHT_UNDEFINED:

                break;
        }
    }
}
