package com.hassanassowe.raspberrycam.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.hassanassowe.raspberrycam.BuildConfig;
import com.hassanassowe.raspberrycam.R;

import java.util.Date;

public class Information extends AppCompatActivity {

    private MaterialCardView contact, link;
    private Button about;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);


        about = findViewById(R.id.about);
        contact = findViewById(R.id.three);
        link = findViewById(R.id.seven);


        //Initialize Material Toolbar & Set its Navigation Icon
        MaterialToolbar topAppBar = findViewById(R.id.menuAppBar);
        topAppBar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);


        //Apply Actionbar to our view.
        setSupportActionBar(topAppBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Set what to do if navigation icon is pressed
        topAppBar.setNavigationOnClickListener(
                v -> finish()
        );

        //When Contact is clicked, open mail application.
        contact();

        //When About is clicked, it opens a MaterialDialog showing information on the application.
        about();

        //Handler to determine version number & Build #
        build_version_handler();

        //Handler to open GStreamer documentation
        GStreamerDocumentation();

        //Handler to open LinkedIn Link (Hopefully for a job)
        linkedInContact();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    //Handler for Contact Us MaterialCardView
    private void contact() {
        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + "dev@hassanassowe.com"));
                intent.putExtra(Intent.EXTRA_SUBJECT, "Support Inquiry" + " [" + new Date().toString() + "]");
                intent.putExtra(Intent.EXTRA_TEXT, "Manufacturer: " + Build.MANUFACTURER + " \n Model: " + Build.MODEL + " \n Version: " + Build.VERSION.SDK_INT + " \n versionRelease " + Build.VERSION.RELEASE);
                startActivity(intent);
            }
        });
    }

    private void about() {
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaterialAlertDialogBuilder aboutDialog = new MaterialAlertDialogBuilder(Information.this, R.style.AlertDialogTheme_Center);
                aboutDialog.setTitle(getResources().getString(R.string.app_name))
                        .setMessage("RaspberryCam is a third party Android application for viewing streams of Raspberry Pi Camera Modules. \n \n" +
                                "Developed by Hassan Assowe")
                        .setIcon(R.drawable.ic_info_24dp)
                        .setPositiveButton(getResources().getString(R.string.close), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int d) {
                                dialogInterface.dismiss();
                            }
                        }).show();
            }
        });
    }

    //References Version Name & Compile #, populates TextView with information.
    private void build_version_handler() {
        TextView build_version = findViewById(R.id.version_build);
        build_version.setText("v" + BuildConfig.VERSION_NAME + " (Build #" + BuildConfig.BUILDNUM + ")");
    }

    //Opens Linkedin Contact URL (I NEED A JOB DAMNIT!)
    private void linkedInContact() {
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(getString(R.string.LinkedIn)); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }

    private void GStreamerDocumentation() {
        MaterialCardView link = findViewById(R.id.five);
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(getString(R.string.Gstreamer)); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }
}