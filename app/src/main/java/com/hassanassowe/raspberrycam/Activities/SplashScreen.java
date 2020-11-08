package com.hassanassowe.raspberrycam.Activities;
/*
The SplashScreen is responsible for displaying a momentary visual before moving to the next activity.
In addition, it handles some background tasks
- Loading Previously Selected Settings.
- Testing Connection to registered Raspberry Pi Devices (TO:DO)
 */
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hassanassowe.raspberrycam.BuildConfig;
import com.hassanassowe.raspberrycam.Classes.RaspberryPi_Instance.RaspberryPi;
import com.hassanassowe.raspberrycam.Classes.Settings_Save_Load;
import com.hassanassowe.raspberrycam.R;

import java.util.ArrayList;

public class SplashScreen extends AppCompatActivity {
    private static final int TIME_OUT = 1000; //PLACEHOLDER SplashScreen duration.
    public ArrayList<RaspberryPi> raspberryPi_instances = new ArrayList<RaspberryPi>(); //Stored instances of connected PI's

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new Settings_Save_Load().loadData(SplashScreen.this.getApplicationContext()); //Load our settings.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        versionInflate(true);

        new Handler().postDelayed(new Runnable() { //Displays SplashScreen for a fixed amount of time.
            @Override
            public void run() {
                Intent i = new Intent(SplashScreen.this, MainMenu.class);
                startActivity(i);
                finish();
            }
        }, TIME_OUT);
    }


    private void versionInflate(Boolean visiblility){  //Inflates the current version number and displays it below SplashScreen Logo.
        if(visiblility == true) {
            TextView version_build = findViewById(R.id.version_build_splash);
            version_build.setText(BuildConfig.VERSION_NAME);
        }
    }

}