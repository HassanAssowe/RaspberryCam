package com.hassanassowe.raspberrycam.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.hassanassowe.raspberrycam.Classes.Settings_Save_Load;
import com.hassanassowe.raspberrycam.R;

public class Settings extends AppCompatActivity {
    private MaterialToolbar topAppBar;
    private RadioGroup themeRadioGroup, tempRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        themeRadioGroup = findViewById(R.id.radioGroup_theme);
        tempRadioGroup = findViewById(R.id.radioGroup_temperature);

        MaterialToolbar topAppBar = findViewById(R.id.menuAppBar);
        initalizeActionBar(topAppBar); //SetUp ActionBar
        currentSelection(); //If Options were previously selected, display those options.
        tempSelection(); //Handle Temperature Selection Changes
        themeSelection(); //Handle Theme Selection Changes



    }

    private void initalizeActionBar(MaterialToolbar topAppBar) { //Method is responsible for setting up our ActionBar
        topAppBar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);
        setSupportActionBar(topAppBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        topAppBar.setNavigationOnClickListener(v -> finish());
    }
    private void currentSelection(){ //Shows current selected settings.
        SharedPreferences sharedPreferences = getSharedPreferences("settingsKey", Context.MODE_PRIVATE); //Load our settingsKey
        String theme = sharedPreferences.getString("theme_setting", "com.hassanassowe.raspberrycam:id/system_theme"); //Get the value of "theme" from our Settings Key
        String temperature = sharedPreferences.getString("temperature_setting", "com.hassanassowe.raspberrycam:id/celsius"); //Get the value of "theme" from our Settings Key
        themeRadioGroup.check(getResources().getIdentifier(theme, "id", getPackageName()));
        tempRadioGroup.check(getResources().getIdentifier(temperature, "id", getPackageName()));
    }
    private void tempSelection() { //Method is responsible for handling temperature selection and saving.
        tempRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                Log.i("Settings:", "Passed Temperature: " + getResources().getResourceName(tempRadioGroup.getCheckedRadioButtonId()));
                new Settings_Save_Load().saveData(Settings.this.getApplicationContext(), null, getResources().getResourceName(tempRadioGroup.getCheckedRadioButtonId())); //Save Changed Temp
            }
        });

    }

    private void themeSelection() {
        themeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                Log.i("Settings:","Passed Theme: " + getResources().getResourceName(themeRadioGroup.getCheckedRadioButtonId()));
                new Settings_Save_Load().saveData(Settings.this.getApplicationContext(), getResources().getResourceName(themeRadioGroup.getCheckedRadioButtonId()), null); //Save Changed Theme
                new Settings_Save_Load().loadData(Settings.this.getApplicationContext()); //Load Changed Theme
                new Settings_Save_Load().saveData(Settings.this.getApplicationContext(), getResources().getResourceName(themeRadioGroup.getCheckedRadioButtonId()), null); //SOME KIND OF GLITCH TEMP SOLVE? CALLING LOAD BREAKS
            }
        });

    }
}