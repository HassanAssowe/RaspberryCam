package com.hassanassowe.raspberrycam.Classes;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

/*
This class is responsible for the loading and saving of settings.
-Theme
-Temperature
 */
public class Settings_Save_Load { //Enumerators are initalized for each setting option, based on the value of a giving enumerator. We perform certain tasks.

    public static void saveData(Context context, String theme, String temperature) { //Saves settings information set by user
        SharedPreferences sharedPreferences = context.getSharedPreferences("settingsKey", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        try {
            editor.putString("theme_setting", theme);
        } catch (Exception e) {
            Log.i("Settings","Theme was not adjusted");
        }
        try {
            editor.putString("temperature_setting", temperature);
        }catch (Exception e) {
            Log.i("Settings","Temperature was not passed");
        }
        Log.i("Settings_Save_Load", "Saved Theme: " + theme);
        Log.i("Settings_Save_Load", "Saved Temp: " + temperature);
        editor.apply();
    }

    public void loadData(Context context) { //Loads settings information & Applies settings.
        SharedPreferences sharedPreferences = context.getSharedPreferences("settingsKey", Context.MODE_PRIVATE); //Load our settingsKey
        String theme = sharedPreferences.getString("theme_setting", "com.hassanassowe.raspberrycam:id/system_theme"); //Get the value of "theme" from our Settings Key
        Log.i("Settings_Save_Load", "Loading Theme: " +theme);

        switch (theme) { //Based upon "theme" set the correct theme
            case "com.hassanassowe.raspberrycam:id/light_theme":
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "com.hassanassowe.raspberrycam:id/dark_theme":
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "com.hassanassowe.raspberrycam:id/system_theme":
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }

        //TODO:
        //int temperature = sharedPreferences.getInt("temperature_setting", 2); //Get the value of "theme" from our Settings Key

    }
}
