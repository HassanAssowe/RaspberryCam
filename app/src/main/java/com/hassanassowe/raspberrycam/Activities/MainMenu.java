package com.hassanassowe.raspberrycam.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.hassanassowe.raspberrycam.Classes.Adapter;
import com.hassanassowe.raspberrycam.Classes.CameraListEntity;
import com.hassanassowe.raspberrycam.Classes.Ping;
import com.hassanassowe.raspberrycam.Classes.RaspberryPi_Instance.RaspberryPi;
import com.hassanassowe.raspberrycam.Classes.RaspberryPi_Instance.RaspberryPi_Save_Load;
import com.hassanassowe.raspberrycam.R;

import org.freedesktop.gstreamer.surfaceview.CameraView;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.stream.IntStream;

public class MainMenu extends AppCompatActivity implements Serializable {
    private ArrayList<RaspberryPi> raspberryPiInstances; //Stored instances of Connected PI's
    private ArrayList<CameraListEntity> ListEntity; //Shared Instance of our RecyclerView

    private MaterialToolbar topAppBar; //Represents our MaterialToolbar in MainMenu. Used for Flating different Toolbars & Button Management.
    private FloatingActionButton addInstance; // Represents our FAB Button.
    private RecyclerView RecyclerView;
    private RecyclerView.LayoutManager LayoutManager;
    private Adapter mAdapter;

    private Menu menu = null;
    private int selectCount = 0;
    private static final int TIMEOUT_LENGTH = 5000;
    private boolean cameraAvailabiliy = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        topAppBar = findViewById(R.id.topAppBar);
        addInstance = findViewById(R.id.addCam);

        raspberryPiInstances = new ArrayList<RaspberryPi>();
        RaspberryPi_Save_Load.loadData(this, raspberryPiInstances);


        setMainMenuAppBar();
        startupHandler(getResources(), this, raspberryPiInstances);
        swipeRefreshLayoutHandler();
        addInstance(this, addInstance);
        instanceCheck();

    }

    @Override
    public void onResume() {
        raspberryPiInstances.clear();
        RaspberryPi_Save_Load.loadData(this, raspberryPiInstances);
        instanceCheck();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (selectCount != 0) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                int index = IntStream.range(0, ListEntity.size()).filter(i -> ListEntity.get(i).getMimagelayanan() == R.drawable.ic_twotone_check_circle_24dp).findFirst().orElse(-1);
                contextualBarDeflate(index);
            }
        } else {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_app_bar, menu);
        return true;
    }

    //listener for our action bar icons, functionality on their click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_icon: //NIGHT MODE HANDLER
                Intent i = new Intent(MainMenu.this, Settings.class);
                startActivity(i);
                return true;

            case R.id.info_icon:
                i = new Intent(MainMenu.this, Information.class);
                startActivity(i);
                return true;
        }
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        this.menu = menu;
        return super.onPrepareOptionsMenu(menu);
    }

    public void setMainMenuAppBar() {
        setSupportActionBar(topAppBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
    }

    //Method initializes required startup processes.
    private void startupHandler(Resources mResources, Context Context, ArrayList<RaspberryPi> raspberryPi_instances) {
        RaspberryPi_Save_Load.loadData(Context, raspberryPi_instances);

    }


    //OnClickListener for addInstance FAB. Starts AddInstance Activity on Click.
    public void addInstance(Context mContext, FloatingActionButton addInstance) {
        addInstance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext, AddCamera.class);
                startActivity(i);
            }
        });
    }

    //Refreshes Menu on swipeRefresh (Recheck for signal, new data etc.)
    private void swipeRefreshLayoutHandler() {
        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                    instanceCheck();

                }
                pullToRefresh.setRefreshing(false); //End Refresh

            }
        });
    }

    //Checks if the ArrayList used by our RecyclerView is populated. If not, Throw MaterialAlertDialogBuilder.
    public void instanceCheck() {
        ConstraintLayout errorCoordinator = findViewById(R.id.error_coordinator);
        if (raspberryPiInstances.isEmpty()) {
            errorCoordinator.setVisibility(View.VISIBLE);
        } else {
            errorCoordinator.setVisibility(View.GONE);
            createInstanceList();
            buildRecyclerView();
        }
    }

    public void createInstanceList() {
        ListEntity = new ArrayList<>();
        for (int i = 0; i < raspberryPiInstances.size(); i++) {
            ListEntity.add(new CameraListEntity(R.drawable.rpi_logo, raspberryPiInstances.get(i).getName(), raspberryPiInstances.get(i).getAddress() + "/" + raspberryPiInstances.get(i).getPort(), raspberryPiInstances.get(i).getTint()));
        }
    }

    public void buildRecyclerView() {
        RecyclerView = findViewById(R.id.my_recycler_view);
        RecyclerView.setHasFixedSize(true);
        LayoutManager = new LinearLayoutManager(this);
        mAdapter = new Adapter(ListEntity, this, raspberryPiInstances);

        RecyclerView.setLayoutManager(LayoutManager);
        RecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) { //PRESSING RecycleView Item
                if (selectCount == 1) {
                    selectCount = 0;
                    topAppBar.setTitle(selectCount + " Item Selected");
                    changeImagelayanan(position, R.drawable.ic_twotone_check_circle_24dp);
                }
                if (selectCount == 0 && cameraAvailabiliy == false) {
                    setMainMenuAppBar();
                    topAppBar.setNavigationIcon(null);
                    changeImagelayanan(position, R.drawable.rpi_logo);
                    cameraAvailabiliy = true;
                } else {
                    class CameraViewTask extends AsyncTask<String, Void, Boolean> {
                        final ProgressDialog dialog = new ProgressDialog(MainMenu.this);

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            dialog.setMessage("Loading...");
                            dialog.setCancelable(true);
                            dialog.show();
                            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {
                                    dialog.dismiss();
                                    cancel(true);
                                }
                            });
                        }

                        @Override
                        protected Boolean doInBackground(String... params) {
                            try {
                                return new Ping().Ping(raspberryPiInstances.get(position), TIMEOUT_LENGTH);
                            } catch (IOException e) {
                                e.printStackTrace();
                                cancel(true);

                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Boolean s) {
                            if (dialog.isShowing())
                                dialog.dismiss();
                            if (s) {
                                Gson gson = new Gson();
                                String instance = gson.toJson(raspberryPiInstances.get(position));

                                Intent intent = new Intent(MainMenu.this, CameraView.class);
                                intent.putExtra("Instance", instance);
                                startActivity(intent);
                                cancel(true);
                            } else {
                                MaterialAlertDialogBuilder errorDialog = new MaterialAlertDialogBuilder(MainMenu.this);
                                errorDialog.setTitle(getResources().getString(R.string.unableToConnect))
                                        .setMessage(getResources().getString(R.string.unable_supporting_text))
                                        .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int d) {
                                                cancel(true);
                                            }
                                        }).show();
                            }
                        }

                    }
                    new CameraViewTask().execute();

                }
            }
        });

        mAdapter.setOnItemLongClickListener(position -> {
            if (selectCount == 0) {
                contextualBarInflate(position);


                topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        contextualBarDeflate(position);
                    }
                });
            }

        });
    }

    public void deleteListEntity(int position) {
        topAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {  // the issue is here
                    case R.id.delete:
                        MaterialAlertDialogBuilder firstTime = new MaterialAlertDialogBuilder(MainMenu.this);
                        firstTime.setTitle(getResources().getString(R.string.deleteCamera))
                                .setMessage(getResources().getString(R.string.delete_supporting_text))
                                .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        contextualBarDeflate(position);
                                        dialogInterface.dismiss();
                                    }
                                })
                                .setPositiveButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int d) {
                                        contextualBarDeflate(position);
                                        ListEntity.remove(position);
                                        raspberryPiInstances.remove(position);
                                        RaspberryPi_Save_Load.saveData(MainMenu.this, raspberryPiInstances);
                                        mAdapter.notifyItemRemoved(position);
                                        instanceCheck();
                                    }
                                }).show();
                        break;

                    case R.id.edit:
                        Gson gson = new Gson();
                        String instance = gson.toJson(raspberryPiInstances.get(position));
                        Log.i("MainMenu", raspberryPiInstances.get(position).getName());

                        Intent intent = new Intent(MainMenu.this, EditCamera.class);
                        intent.putExtra("EditCamera", instance);
                        intent.putExtra("Position", position);
                        contextualBarDeflate(position);
                        startActivity(intent);
                        break;
                }
                return false;
            }
        });
    }


    //Haptic Feedback Handler (Long Clicks)
    private void hapticFeedback() {
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(100);
    }

    private void contextualBarInflate(int position) {
        hapticFeedback();
        topAppBar.getMenu().clear();
        getMenuInflater().inflate(R.menu.menu_contextual_bar, getMenu());
        deleteListEntity(position);
        selectCount = selectCount + 1;
        topAppBar.setTitle(selectCount + " Item Selected");
        topAppBar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);
        changeImagelayanan(position, R.drawable.ic_twotone_check_circle_24dp);
        cameraAvailabiliy = false;
    }

    private void contextualBarDeflate(int position) {
        try {
            selectCount = 0;
            changeImagelayanan(position, R.drawable.rpi_logo);
            topAppBar.setNavigationIcon(null);
            setMainMenuAppBar();
            cameraAvailabiliy = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Menu getMenu() {
        return menu;
    }

    public void changeImagelayanan(int position, int image) {
        ListEntity.get(position).setMimagelayanan(image);
        mAdapter.notifyItemChanged(position);
    }
}


