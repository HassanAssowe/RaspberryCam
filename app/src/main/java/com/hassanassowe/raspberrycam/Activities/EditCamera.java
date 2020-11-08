package com.hassanassowe.raspberrycam.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.hassanassowe.raspberrycam.Classes.RaspberryPi_Instance.RaspberryPi;
import com.hassanassowe.raspberrycam.Classes.RaspberryPi_Instance.RaspberryPi_Save_Load;
import com.hassanassowe.raspberrycam.R;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.util.ArrayList;
import java.util.UUID;

/*
The AddCamera class is responsible for the registration of a Raspberry Pi Device.
A device can be registered and assigned:
-Name
-Connection Type
-IP Address
-Connection Port
-Highlight Color

A device can also be given advanced properties to further add functionality to it:
-SSH Username
-SSH Password
-SSH Port

Provided with SSH Information, the class can configure a Raspberry PI with a raspivid service to which video settings can be added:
-Stream Resolution
-Bit Rate
-Frame Rate
-Screenshot Resolution
 */
public class EditCamera extends AppCompatActivity {

    private static ArrayList<RaspberryPi> raspberryPiInstances; //Represents our current list of Registered Raspberry Pi Devices. To which we will add to.
    private RaspberryPi currentRaspberryPi;
    private RaspberryPi raspberryPi; //Represents the device being added, will be populated w/ information & added to mRaspberryPi_Instances


    private static TextInputLayout nameField, IPField, portField, connectionTypeField, resolutionField, usernameField, passwordField, SSHPortField, bitrateField, framerateField, screenshotField;

    private static final String[] connectionTypes = new String[]{"TCP/IP"};
    private static final String[] resolutionTypes = new String[]{"640x480", "1280x720", "1920x1080"};
    private static final String[] formatTypes = new String[]{"PNG", "JPG"};

    private String connectionType, resolutionType, formatType;
    private AutoCompleteTextView connectionOutlinedExposedDropdown, formatOutlinedExposedDropdown, resolutionOutlinedExposedDropdown;


    private RadioButton selectedButton;
    private RadioGroup radioGroupHightlightColor;

    private CheckBox autoConfigureCheckBox;

    private ConstraintLayout advancedSettings;
    private RelativeLayout dividerTwo;
    private ImageView indicator2;
    private TextView advancedSettingsText;

    private Button register;

    private Context context;

    private MaterialToolbar topAppBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_instance);

        topAppBar = findViewById(R.id.menuAppBar);
        topAppBar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);

        setSupportActionBar(topAppBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        topAppBar.setNavigationOnClickListener(
                v -> finish()
        );

        context = this;
        //Load Instances to append to.
        raspberryPiInstances = new ArrayList<>();
        RaspberryPi_Save_Load.loadData(this, raspberryPiInstances);

        nameField = findViewById(R.id.NameField);
        connectionTypeField = findViewById(R.id.ConnectionTypeDropDown);
        IPField = findViewById(R.id.AddressField);
        portField = findViewById(R.id.PortField);
        register = findViewById(R.id.register_button);
        usernameField = findViewById(R.id.UsernameField);
        passwordField = findViewById(R.id.PasswordField);
        SSHPortField = findViewById(R.id.SSHPortField);
        resolutionField = findViewById(R.id.ScreenshotResolutionDropDown);
        framerateField = findViewById(R.id.FramerateField);
        bitrateField = findViewById(R.id.BitrateField);
        screenshotField = findViewById(R.id.ScreenshotFormatDropDown);

        selectedButton = findViewById(R.id.radio_button_1);
        radioGroupHightlightColor = findViewById(R.id.radioGroup_highlightColor);

        connectionOutlinedExposedDropdown = findViewById(R.id.filled_exposed_dropdown_connection);
        resolutionOutlinedExposedDropdown = findViewById(R.id.filled_exposed_dropdown_resolution);
        formatOutlinedExposedDropdown = findViewById(R.id.filled_exposed_dropdown_format);

        advancedSettings = findViewById(R.id.advanced_settings);
        advancedSettingsText = findViewById(R.id.advanced_settings_text);
        dividerTwo = findViewById(R.id.divider_two);
        indicator2 = findViewById(R.id.indicator2);


        autoConfigureCheckBox = findViewById(R.id.autoConfigure_checkbox);

        connectionExposedDropDown(this);
        screenshotFormatExposedDropDown(this);
        resolutionFormatExposedDropDown(this);

        dividerTwo = findViewById(R.id.divider_two);

        advancedDropDownListener(this, (Activity) EditCamera.this);
        autoConfigureListener();
        EditInstance();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.camera_app_bar, menu);
        return true;
    }

    //listener for our action bar icons, functionality on their click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.verify_ssh:
                verifyssh();
                return true;

        }
        return false;
    }

    public void EditInstance() {
        Bundle extras = getIntent().getExtras();
        if (extras.containsKey("EditCamera")) {
            Gson gson = new Gson();
            String pi_instance = getIntent().getStringExtra("EditCamera");
            int position = getIntent().getIntExtra("Position", -1);

            this.currentRaspberryPi = gson.fromJson(pi_instance, RaspberryPi.class);
            this.raspberryPi = this.currentRaspberryPi;
            register.setText("EDIT CAMERA");
            MaterialToolbar menuAppBar = findViewById(R.id.menuAppBar);
            menuAppBar.setTitle("Edit Camera");

            //Populating activity with camera data.
            instanceDataInflate(raspberryPi);

            //Handle edit
            register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clearAndUnFocus();
                    MaterialAlertDialogBuilder firstTime = new MaterialAlertDialogBuilder(EditCamera.this);
                    firstTime.setTitle(getResources().getString(R.string.editCamera))
                            .setMessage(getResources().getString(R.string.confirm_supporting_text))
                            .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onClick(DialogInterface dialogInterface, int d) {
                            if (registerCamera()) {
                                Log.i("EditInstance", "Altering  Current Values");
                                raspberryPiInstances.set(position, raspberryPi);
                                RaspberryPi_Save_Load.saveData(EditCamera.this, raspberryPiInstances);
                                onBackPressed();
                            }
                        }
                    }).show();
                }
            });
            extras.remove("EditInstance");
            extras.remove("Position");
        }
    }

    private void instanceDataInflate(RaspberryPi instance) {
        //Populating activity with camera data.
        nameField.getEditText().setText(instance.getName());
        IPField.getEditText().setText(instance.getAddress());
        portField.getEditText().setText(Integer.toString(instance.getPort()));
        connectionType = instance.getConnectionType();
        for (int i = 0; i < radioGroupHightlightColor.getChildCount(); i++) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.i("EditInstance", Integer.toHexString(((RadioButton) radioGroupHightlightColor.getChildAt(i)).getButtonTintList().getDefaultColor()).substring(2) + " == " + instance.getTint());
                if (Integer.toHexString(((RadioButton) radioGroupHightlightColor.getChildAt(i)).getButtonTintList().getDefaultColor()).substring(2).equals(instance.getTint())) {
                    ((RadioButton) radioGroupHightlightColor.getChildAt(i)).setChecked(true);
                    break;
                }
            }
        }
        if (instance.getSSHUsername() != null & instance.getSSHPassword() != null & instance.getSSHPort() != -1) {
            usernameField.getEditText().setText(instance.getSSHUsername());
            passwordField.getEditText().setText(instance.getSSHPassword());
            SSHPortField.getEditText().setText(Integer.toString(instance.getSSHPort()));

            advancedSettings.setVisibility(View.VISIBLE);
        }
        if (instance.getHeight() != -1 & instance.getWidth() != -1 & instance.getBitRate() != -1 & instance.getFrameRate() != -1 & instance.getScreenshotFormat() != null) {
            autoConfigureCheckBox.setChecked(true);

            resolutionField.getEditText().setText(instance.getWidth() + "x" + instance.getHeight());
            bitrateField.getEditText().setText(Float.toString(instance.getBitRate()));
            framerateField.getEditText().setText(Integer.toString(instance.getFrameRate()));
            screenshotField.getEditText().setText(instance.getScreenshotFormat());

        }
    }

    /*ERROR CHECK HANDLERS*/
    private static Boolean nameFieldHandler() {
        if (nameField.getEditText().getText().toString().isEmpty()) {
            nameField.setError("A Name Is Required");
            Log.i("AddInstance - Handler", "Name InValid");
            return false;
        }
        Log.i("AddInstance - Handler", "Name Valid");
        return true;
    }

    private Boolean conectionTypeHandler() {
        if (connectionType.isEmpty()) {
            Log.i("AddInstance - Handler", "Connection Dropdown Empty");
            connectionOutlinedExposedDropdown.setError("Selection Required");
            return false;
        }
        Log.i("AddInstance - Handler", "Connection Dropdown Selected");
        return true;
    }

    private static Boolean addressFieldHandler() {
        String IPV4_PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
        String IPV6_PATTERN = "(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))\n";
        if (!IPField.getEditText().getText().toString().matches(IPV4_PATTERN)) {
            IPField.setError("Invalid IP Address");
            Log.i("AddInstance - Handler", "IPAddress InValid");
            return false;
        }

        if (IPField.getEditText().getText().toString().isEmpty()) {
            IPField.setError("Required");
            Log.i("AddInstance - Handler", "IPAddress InValid");
            return false;
        }
        Log.i("AddInstance - Handler", "IPAddress Valid");
        return true;
    }

    private static Boolean portFieldHandler() {
        if (portField.getEditText().getText().toString().isEmpty()) {
            portField.setError("Required");
            Log.i("AddInstance - Handler", "PortField InValid");
            return false;
        }
        try {
            Integer.parseInt(portField.getEditText().getText().toString());
            Log.i("AddInstance - Handler", "PortField Valid");
            return true;
        } catch (NumberFormatException e) {
            portField.setError("Invalid Port Number");
            Log.i("AddInstance - Handler", "PortField InValid");
            return false;
        }
    }

    private String highlightColorHandler() {
        int id = radioGroupHightlightColor.getCheckedRadioButtonId();
        selectedButton = radioGroupHightlightColor.findViewById(id);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.i("EditCamera", "Color Selected = " + selectedButton.getButtonTintList().getDefaultColor() + " Default Color = " + R.attr.colorButtonNormal);
            }
            if (selectedButton.getId() == R.id.radio_button_1)
                return null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return Integer.toHexString(selectedButton.getButtonTintList().getDefaultColor()).substring(2);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private Boolean SSHUsernameHandler() {
        boolean b = true;
        if (!usernameField.getEditText().getText().toString().isEmpty()) {
            if (IPField.getEditText().getText().toString().isEmpty()) {
                IPField.setError("IP Address is Empty");
                b = false;
            }
            if (passwordField.getEditText().getText().toString().isEmpty()) {
                passwordField.setError("Required");
                b = false;
            }
            if (SSHPortField.getEditText().getText().toString().isEmpty()) {
                SSHPortField.setError("Required");
                b = false;
            }
            return b;
        } else if (!usernameField.getEditText().getText().toString().isEmpty() & addressFieldHandler() & SSHPasswordHandler() & SSHPortHandler()) {
            return true;
        } else if (usernameField.getEditText().getText().toString().isEmpty() & autoConfigureCheckBox.isChecked() & (!bitrateField.getEditText().toString().isEmpty() || !framerateField.getEditText().toString().isEmpty())) {
            usernameField.setError("Required");
            return false;
        }
        return false;
    }

    private boolean SSHPasswordHandler() {
        Boolean b = true;
        if (!passwordField.getEditText().getText().toString().isEmpty()) {
            if (IPField.getEditText().getText().toString().isEmpty()) {
                IPField.setError("IP Address is Empty");
                b = false;
            }
            if (usernameField.getEditText().getText().toString().isEmpty()) {
                usernameField.setError("Required");
                b = false;
            }
            if (SSHPortField.getEditText().getText().toString().isEmpty()) {
                SSHPortField.setError("Required");
                b = false;
            }
            return b;
        } else if (!passwordField.getEditText().getText().toString().isEmpty() && addressFieldHandler() && SSHUsernameHandler() && SSHPortHandler()) {
            return true;
        } else if (passwordField.getEditText().getText().toString().isEmpty() & autoConfigureCheckBox.isChecked() & (!bitrateField.getEditText().toString().isEmpty() || !framerateField.getEditText().toString().isEmpty())) {
            passwordField.setError("Required");
            return false;
        }
        return false;
    }

    private boolean SSHPortHandler() {
        Boolean b = true;
        if (!SSHPortField.getEditText().getText().toString().isEmpty()) {
            if (IPField.getEditText().getText().toString().isEmpty()) {
                IPField.setError("IP Address is Empty");
                b = false;
            }
            if (usernameField.getEditText().getText().toString().isEmpty()) {
                usernameField.setError("Required");
                b = false;
            }
            if (passwordField.getEditText().getText().toString().isEmpty()) {
                passwordField.setError("Required");
                b = false;
            }
            return b;
        } else if (!SSHPortField.getEditText().getText().toString().isEmpty() && addressFieldHandler() && SSHUsernameHandler() && SSHPasswordHandler()) {
            return true;
        } else if (SSHPortField.getEditText().getText().toString().isEmpty() & autoConfigureCheckBox.isChecked() & (!bitrateField.getEditText().toString().isEmpty() || !framerateField.getEditText().toString().isEmpty())) {
            SSHPortField.setError("Required");
            return false;
        }
        return false;
    }

    private Boolean resolutionFormatTypeHandler() {
        if (resolutionType.isEmpty() && autoConfigureCheckBox.isChecked()) {
            Log.i("AddInstance - Handler", "Format Dropdown Empty");
            return false;
        }
        Log.i("AddInstance - Handler", "Format Dropdown Selected");
        return true;
    }

    private Boolean bitrateHandler() {
        if (autoConfigureCheckBox.isChecked() && bitrateField.getEditText().getText().toString().isEmpty()) {
            bitrateField.setError("Required");
            return false;
        }else if (autoConfigureCheckBox.isChecked() && (Integer.parseInt(bitrateField.getEditText().getText().toString())) > 25) {
            bitrateField.setError("Bitrate above 25Mbps");
            return false;
        }
        return true;
    }

    private Boolean framerateHandler() {
        if (autoConfigureCheckBox.isChecked() && framerateField.getEditText().getText().toString().isEmpty()) {
            framerateField.setError("Required");
            return false;
        }
        return true;
    }

    private Boolean screenshotFormatTypeHandler() {
        if (formatType.isEmpty() && autoConfigureCheckBox.isChecked()) {
            Log.i("AddInstance - Handler", "Format Dropdown Empty");
            return false;
        }
        Log.i("AddInstance - Handler", "Format Dropdown Selected");
        return true;
    }


    private void connectionExposedDropDown(Context context) {
        ArrayAdapter adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, connectionTypes);
        connectionOutlinedExposedDropdown.setAdapter(adapter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            connectionOutlinedExposedDropdown.setText(connectionTypes[0], false);
            connectionType = connectionTypes[0];
        }

        connectionOutlinedExposedDropdown.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                connectionType = charSequence.toString();
                Log.i("AddInstance", "ConnectionType = " + connectionType);

            }

            @Override
            public void afterTextChanged(Editable editable) {
                ArrayAdapter adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, connectionTypes);
                connectionOutlinedExposedDropdown.setAdapter(adapter);

            }
        });
    }

    private void screenshotFormatExposedDropDown(Context context) {
        ArrayAdapter adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, formatTypes);
        formatOutlinedExposedDropdown.setAdapter(adapter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            formatOutlinedExposedDropdown.setText(formatTypes[0], false);
            formatType = formatTypes[0];
        }

        formatOutlinedExposedDropdown.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                formatType = charSequence.toString();
                Log.i("EditCamera", "ConnectionType = " + formatType);

            }

            @Override
            public void afterTextChanged(Editable editable) {
                ArrayAdapter adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, formatTypes);
                formatOutlinedExposedDropdown.setAdapter(adapter);

            }
        });
    }

    private void resolutionFormatExposedDropDown(Context context) {
        ArrayAdapter adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, resolutionTypes);
        resolutionOutlinedExposedDropdown.setAdapter(adapter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            resolutionOutlinedExposedDropdown.setText(resolutionTypes[0], false);
            resolutionType = resolutionTypes[0];
        }

        resolutionOutlinedExposedDropdown.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                resolutionType = charSequence.toString();
                Log.i("EditCamera", "ConnectionType = " + resolutionType);

            }

            @Override
            public void afterTextChanged(Editable editable) {
                ArrayAdapter adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, resolutionTypes);
                resolutionOutlinedExposedDropdown.setAdapter(adapter);

            }
        });

    }

    private void advancedDropDownListener(Context context, Activity mActivity) { //Advanced Settings dropdown (Exposes List if many options are available in the future)
        dividerTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (advancedSettings.getVisibility() == View.GONE) {
                    mActivity.runOnUiThread(new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void run() {
                            indicator2.setImageResource(R.drawable.ic_close_24dp);
                            indicator2.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary));
                            advancedSettings.setVisibility(View.VISIBLE);
                        }
                    });
                    Log.i("AddInstance", String.valueOf(advancedSettings.getVisibility()));

                } else {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            indicator2.setImageResource(R.drawable.ic_open_24dp);
                            indicator2.clearColorFilter();
                            advancedSettings.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }

    private void autoConfigureListener() { //This enables or disables Auto-Configure Settings based on a checkbox.
        autoConfigureCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (screenshotField.isEnabled() == false && resolutionField.isEnabled() == false && bitrateField.isEnabled() == false && framerateField.isEnabled() == false) {
                    screenshotField.setEnabled(true);
                    resolutionField.setEnabled(true);
                    bitrateField.setEnabled(true);
                    framerateField.setEnabled(true);
                } else {
                    screenshotField.setEnabled(false);
                    //screenshotField.getEditText().getText().clear();
                    screenshotField.setErrorEnabled(false);

                    resolutionField.setEnabled(false);
                    //resolutionField.getEditText().getText().clear();
                    resolutionField.setErrorEnabled(false);

                    bitrateField.setEnabled(false);
                    bitrateField.getEditText().getText().clear();
                    bitrateField.setErrorEnabled(false);

                    framerateField.setEnabled(false);
                    framerateField.getEditText().getText().clear();
                    framerateField.setErrorEnabled(false);
                }

            }
        });

    }

    private void clearAndUnFocus() {
        try {
            getCurrentFocus().clearFocus();
        } catch (Exception e) {
            e.printStackTrace();
        }
        nameField.setErrorEnabled(false);
        connectionTypeField.setErrorEnabled(false);
        IPField.setErrorEnabled(false);
        portField.setErrorEnabled(false);
        usernameField.setErrorEnabled(false);
        passwordField.setErrorEnabled(false);
        SSHPortField.setErrorEnabled(false);
        screenshotField.setErrorEnabled(false);
        resolutionField.setErrorEnabled(false);
        bitrateField.setErrorEnabled(false);
        framerateField.setErrorEnabled(false);
    }

    private void verifyssh() {
        clearAndUnFocus();
        if (usernameField.getEditText().getText().toString().isEmpty())
            usernameField.setError("Required");
        if (passwordField.getEditText().getText().toString().isEmpty())
            passwordField.setError("Required");
        if (SSHPortField.getEditText().getText().toString().isEmpty())
            SSHPortField.setError("Required");

        if (advancedSettings.getVisibility() == View.GONE) {
            runOnUiThread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void run() {
                    indicator2.setImageResource(R.drawable.ic_close_24dp);
                    indicator2.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary));
                    advancedSettings.setVisibility(View.VISIBLE);
                }
            });
            Log.i("AddInstance", String.valueOf(advancedSettings.getVisibility()));

        }
        if (addressFieldHandler() & SSHUsernameHandler() & SSHPasswordHandler() & SSHPortHandler()) {
            JSch ssh = new JSch();
            class SSHVerify extends AsyncTask<Void, Void, Boolean> {
                ProgressDialog dialog = new ProgressDialog(EditCamera.this);

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    dialog.setMessage("Verifying SSH...");
                    dialog.setCancelable(false);
                    dialog.show();
                }

                @Override
                protected Boolean doInBackground(Void... params) {
                    try {
                        Session session = ssh.getSession(usernameField.getEditText().getText().toString(), IPField.getEditText().getText().toString(), Integer.parseInt(SSHPortField.getEditText().getText().toString()));
                        session.setPassword(passwordField.getEditText().getText().toString());
                        session.setConfig("StrictHostKeyChecking", "no");
                        session.connect(30000);
                        if (session.isConnected()) {
                            session.disconnect();
                            return true;
                        } else
                            return false;
                    } catch (JSchException e) {
                        Log.i("AddCanera", "An Error has occurred: " + e.toString());
                        e.printStackTrace();
                        return false;
                    }
                }

                @Override
                protected void onPostExecute(Boolean s) {
                    if (dialog.isShowing())
                        dialog.dismiss();

                    if (s)
                        Snackbar.make(findViewById(R.id.coordinatorRoot), "SSH connection successful ✓", Snackbar.LENGTH_LONG).setBackgroundTint(getResources().getColor(R.color.colorAccent)).show();
                    else
                        Snackbar.make(findViewById(R.id.coordinatorRoot), "SSH connection unsuccessful ✕", Snackbar.LENGTH_LONG).setBackgroundTint(getResources().getColor(R.color.colorPrimary)).show();


                }

            }
            new SSHVerify().execute();


        }
    }


    private Boolean registerCamera() {
        if (nameFieldHandler() & conectionTypeHandler() & addressFieldHandler() & portFieldHandler() & !SSHUsernameHandler() & !SSHPasswordHandler() & !SSHPortHandler() & !autoConfigureCheckBox.isChecked()) {
            raspberryPi = new RaspberryPi.RaspberryPiBuilder(UUID.randomUUID().toString(), nameField.getEditText().getText().toString(), connectionType, IPField.getEditText().getText().toString(),
                    Integer.parseInt(portField.getEditText().getText().toString()), highlightColorHandler()).build();

            return true;
        } else if (nameFieldHandler() & conectionTypeHandler() & addressFieldHandler() & portFieldHandler() & SSHUsernameHandler() & SSHPasswordHandler() & SSHPortHandler()) {
            raspberryPi = new RaspberryPi.RaspberryPiBuilder(UUID.randomUUID().toString(), nameField.getEditText().getText().toString(), connectionType, IPField.getEditText().getText().toString(),
                    Integer.parseInt(portField.getEditText().getText().toString()), highlightColorHandler())
                    .setSSHUsername(usernameField.getEditText().getText().toString()).setSSHPassword(passwordField.getEditText().getText().toString()).setSSHPort(Integer.parseInt(SSHPortField.getEditText().getText().toString())).build();
            return true;
        } else if (nameFieldHandler() & conectionTypeHandler() & addressFieldHandler() & portFieldHandler() & SSHUsernameHandler() & SSHPasswordHandler() & SSHPortHandler() &
                autoConfigureCheckBox.isChecked() &
                resolutionFormatTypeHandler() & bitrateHandler() & framerateHandler() & screenshotFormatTypeHandler()) {
            raspberryPi = new RaspberryPi.RaspberryPiBuilder(UUID.randomUUID().toString(), nameField.getEditText().getText().toString(), connectionType, IPField.getEditText().getText().toString(),
                    Integer.parseInt(portField.getEditText().getText().toString()), highlightColorHandler())
                    .setSSHUsername(usernameField.getEditText().getText().toString()).setSSHPassword(passwordField.getEditText().getText().toString()).setSSHPort(Integer.parseInt(SSHPortField.getEditText().getText().toString()))
                    .setWidth(Integer.parseInt(resolutionType.split("x")[0])).setHeight(Integer.parseInt(resolutionType.split("x")[1]))
                    .setBitRate(Integer.parseInt(bitrateField.getEditText().getText().toString())).setFrameRate(Integer.parseInt(framerateField.getEditText().getText().toString())).setScreenshotFormat(formatType).build();
            return true;
        } else {
            return false;
        }
    }
}