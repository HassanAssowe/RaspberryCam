package org.freedesktop.gstreamer.surfaceview;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hassanassowe.raspberrycam.Activities.MainMenu;
import com.hassanassowe.raspberrycam.Classes.RaspberryPi_Instance.RaspberryPi;
import com.hassanassowe.raspberrycam.R;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.freedesktop.gstreamer.GStreamer;

import static android.view.View.INVISIBLE;
import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

public class CameraView extends Activity implements SurfaceHolder.Callback {

    private native void nativeInit();     // Initialize native code, build pipeline, etc

    private native void nativeFinalize(); // Destroy pipeline and shutdown native code

    private native void nativePlay();     // Set pipeline to PLAYING

    private native void nativePause();    // Set pipeline to PAUSED

    private static native boolean nativeClassInit(); // Initialize native class: cache Method IDs for callbacks

    private native void nativeSurfaceInit(Object surface);

    private native void nativeSurfaceFinalize();

    private native void nativeInstanceInit(String gst_pipeline); //forms the pipeline used to display gstreamer feed.

    private long native_custom_data;      // Native code will use this to keep private data


    private boolean is_playing_desired;   // Whether the user asked to go to PLAYING
    private GStreamerSurfaceView textureView;
    private ImageButton backButton, screenshotButton, videoButton;
    private TextView cameraName;
    private FrameLayout mDecorView;

    private RaspberryPi instance;
    private JSch ssh;
    private Session session;

    // Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(SYSTEM_UI_FLAG_LAYOUT_STABLE
                | SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        // Initialize GStreamer and warn if it fails
        try {
            GStreamer.init(this);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        setContentView(R.layout.activity_camera_view);

        SurfaceView sv = this.findViewById(R.id.surface_video);
        SurfaceHolder sh = sv.getHolder();
        sh.addCallback(this);

        if (savedInstanceState != null) {
            is_playing_desired = savedInstanceState.getBoolean("playing");
            Log.i("GStreamer", "Activity created. Saved state is playing:" + is_playing_desired);
        } else {
            is_playing_desired = false;
            Log.i("GStreamer", "Activity created. There is no saved state, playing: false");
        }

        //_______________________________________________________________________________________________________________
        backButton = findViewById(R.id.back_button);
        screenshotButton = findViewById(R.id.screenshot_button);
        videoButton = findViewById(R.id.video_button);
        cameraName = findViewById(R.id.cameraName);
        mDecorView = findViewById(R.id.root_view);


        Log.i("GStreamer", "Creating Stream");
        loadInstance();
        constructOverlay();
        overlayVisibility();
        pipeline_constructor();
        nativeInit();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            mDecorView.setSystemUiVisibility(
                    SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | SYSTEM_UI_FLAG_FULLSCREEN
                            | SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }


    @Override
    public void onResume() {
        pipeline_constructor();
        nativeInit();
        nativePlay();
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(CameraView.this, MainMenu.class);
        Log.i("CameraView", "Returning to mainmenu.");
        try {
            session.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        startActivity(intent);
        finish();

    }

    protected void onSaveInstanceState(Bundle outState) {
        Log.d("GStreamer", "Saving state, playing:" + is_playing_desired);
        outState.putBoolean("playing", is_playing_desired);
    }

    // Called from native code. This sets the content of the TextView from the UI thread.
    private void setMessage(final String message) {
        Log.i("GStreamer", message);
    }

    // Called from native code. Native code calls this once it has created its pipeline and
    // the main loop is running, so it is ready to accept commands.
    private void onGStreamerInitialized() {
        Log.i("GStreamer", "Gst initialized. Restoring state, playing:" + is_playing_desired);
        // Restore previous playing state
        if (is_playing_desired) {
            nativePlay();
        } else {
            nativePause();
        }

        // Re-enable buttons, now that GStreamer is initialized
        final Activity activity = this;
        runOnUiThread(new Runnable() {
            public void run() {
                is_playing_desired = false;
                nativePlay();
            }
        });
    }

    static {
        System.loadLibrary("gstreamer_android");
        System.loadLibrary("GStreamer");
        nativeClassInit();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d("GStreamer", "Surface changed to format " + format + " width "
                + width + " height " + height);
        nativeSurfaceInit(holder.getSurface());
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("GStreamer", "Surface created: " + holder.getSurface());
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("GStreamer", "Surface destroyed");
        nativeSurfaceFinalize();
    }


    private void invisible() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("GStreamer", "Setting UI Visiblity");
                backButton.setVisibility(INVISIBLE);
                screenshotButton.setVisibility(INVISIBLE);
                cameraName.setVisibility(INVISIBLE);
                videoButton.setVisibility(INVISIBLE);
            }
        });
    }

    private void visible() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("GStreamer", "Setting UI Visiblity");
                backButton.setVisibility(View.VISIBLE);
                screenshotButton.setVisibility(View.VISIBLE);
                cameraName.setVisibility(View.VISIBLE);
                videoButton.setVisibility(View.VISIBLE);

            }
        });
    }

    private void invalidate() {
        backButton.invalidate();
        screenshotButton.invalidate();
        cameraName.invalidate();
    }

    private void loadInstance() {
        Gson gson = new Gson();
        String pi_instance = getIntent().getStringExtra("Instance");
        this.instance = gson.fromJson(pi_instance, RaspberryPi.class);
    }

    //Creates gstreamer pipeline that JNI will use to connect to stream
    private void pipeline_constructor() {

        if (this.instance.getConnectionType().equals("TCP/IP")) {
            String gst_pipeline = "tcpclientsrc host=" + this.instance.getAddress() + " port=" + this.instance.getPort() + " !  queue2 max-size-buffers=1 ! decodebin ! autovideosink sync=false";
            nativeInstanceInit(gst_pipeline);
        } else if (this.instance.getConnectionType().equals("UDP")) {
            String gst_pipeline = "udpsrc host=" + this.instance.getAddress() + " port=" + this.instance.getPort() + " !  rtph264depay ! ffdec_h264 ! xvimagesink";
            nativeInstanceInit(gst_pipeline);
        } else {
            String gst_pipeline = "tcpclientsrc host=" + this.instance.getAddress() + " port=" + this.instance.getPort() + " !  queue2 max-size-buffers=1 !  decodebin ! autovideosink sync=false";
            nativeInstanceInit(gst_pipeline);
        }
    }

    private void constructOverlay() {

        cameraName.setText(instance.getName());
        //ssh = new SSH(instance.getAddress(),instance.getSSHPort(),instance.getSSHUsername(),instance.getSSHPassword());
        //ssh.connect();


        class SSHVerify extends AsyncTask<Void, Void, Boolean> {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    ssh = new JSch();
                    session = ssh.getSession(instance.getSSHUsername(), instance.getAddress(), instance.getSSHPort());
                    session.setPassword(instance.getSSHPassword());
                    session.setConfig("StrictHostKeyChecking", "no");
                    session.connect(30000);
                    return session.isConnected();
                } catch (JSchException e) {
                    Log.i("AddCanera", "An Error has occurred: " + e.toString());
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean s) {
                if (s) {
                    screenshotButton.setColorFilter(getResources().getColor(R.color.White));
                    screenshotButton.setClickable(true);
                    videoButton.setColorFilter(getResources().getColor(R.color.White));
                    videoButton.setClickable(true);
                }
            }
        }
        //new SSHVerify().execute();

        screenshotButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

            }
        });


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CameraView.this, MainMenu.class);
                Log.i("CameraView", "Returning to mainmenu.");
                try {
                    session.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                startActivity(intent);
                finish();
            }
        });
    }


    //Controls system UI (Status & Action Bars)
    private void overlayVisibility() {
        findViewById(R.id.root_view).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (cameraName.getVisibility() == View.VISIBLE && screenshotButton.getVisibility() == View.VISIBLE && backButton.getVisibility() == View.VISIBLE)
                        invisible();
                    else if (cameraName.getVisibility() == View.INVISIBLE && screenshotButton.getVisibility() == View.INVISIBLE && backButton.getVisibility() == View.INVISIBLE)
                        visible();
                    else
                        invalidate();
                }
                return true;
            }
        });
    }
}

