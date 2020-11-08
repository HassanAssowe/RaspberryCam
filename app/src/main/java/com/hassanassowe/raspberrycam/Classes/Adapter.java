// The ADAPTER is designated to handle the entities that make up our recyclerview (each instance of a camera on the mainmenu)
package com.hassanassowe.raspberrycam.Classes;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.hassanassowe.raspberrycam.Activities.EditCamera;
import com.hassanassowe.raspberrycam.Classes.RaspberryPi_Instance.RaspberryPi;
import com.hassanassowe.raspberrycam.R;

import java.io.IOException;
import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter<Adapter.AdapterViewHolder> {
    private final ArrayList<RaspberryPi> instances;
    private final ArrayList<CameraListEntity> mRaspberry_list;
    private OnItemClickListener mListener;
    private OnItemLongClickListener mLongListener;
    private final Context context;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mLongListener = listener;
    }

    public static class AdapterViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextLayanan;
        public TextView mTextDocker;
        public ImageView mImageLayanan;
        public MenuItem delete;
        public ImageButton mVertButton;
        public RelativeLayout mParentList;
        public ImageView mIndicator;

        public AdapterViewHolder(View itemView, OnItemClickListener listener, OnItemLongClickListener longListener) {
            super(itemView);
            mImageLayanan = itemView.findViewById(R.id.image_layanan);
            mTextLayanan = itemView.findViewById(R.id.text_layanan);
            mTextDocker = itemView.findViewById(R.id.text_dokter);
            delete = itemView.findViewById(R.id.delete);
            mVertButton = itemView.findViewById(R.id.vertButton);
            mParentList = itemView.findViewById(R.id.parent_list);
            mIndicator = itemView.findViewById(R.id.indicator);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (longListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            longListener.onItemLongClick(position);
                        }
                    }
                    return true;
                }
            });
        }
    }

    public Adapter(ArrayList<CameraListEntity> raspberry_list, Context context, ArrayList<RaspberryPi> RaspberryPi_Instance) {
        mRaspberry_list = raspberry_list;
        instances = RaspberryPi_Instance;
        this.context = context;
    }

    @Override
    public AdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.raspberrycam_list_entity, parent, false);
        AdapterViewHolder AVH = new AdapterViewHolder(v, mListener, mLongListener);
        return AVH;
    }

    @Override
    public void onBindViewHolder(AdapterViewHolder holder, int position) {
        CameraListEntity currentItem = mRaspberry_list.get(position);

        holder.mImageLayanan.setImageResource(currentItem.getMimagelayanan());
        holder.mTextLayanan.setText(currentItem.getMtextlayanan());
        holder.mTextDocker.setText(currentItem.getMtextdokter());

        //Handles the color highlight of a given recyclerview item (camera).
        if (currentItem.getTint() != null) {
            Log.i("Adapter", currentItem.getTint());
            int highlightColor = Color.parseColor("#66"+currentItem.getTint());
            holder.mParentList.setBackgroundColor(highlightColor);
        }

        //Handles the indicator (GREEN = ACTIVE, RED = INACTIVE) of a given recyclerview item (camera). CURRENTLY CREATING ISSUES
        class InstanceThread extends AsyncTask<Void, Void, Boolean> {
            @Override
            protected Boolean doInBackground(Void... Void) {
                try {
                    return (new Ping().Ping(instances.get(position), 1000));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean s) {
                if (s) {
                    ImageViewCompat.setImageTintList(holder.mIndicator, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.indicatorPositive)));
                    Log.i("Adapter", "Ping InstanceThread Sucessful");
                } else {
                    ImageViewCompat.setImageTintList(holder.mIndicator, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.indicatorNegative)));
                    Log.i("Adapter", "Ping InstanceThread Unsucessful");
                }
            }
        }
        new InstanceThread().execute();


        //Handles the vertical button of a given recyclerview item. Enabling the dropdown menu & its functionality.
        holder.mVertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //creating a popup menu
                PopupMenu popup = new PopupMenu(view.getContext(), holder.mVertButton);
                //inflating menu from xml resource
                popup.inflate(R.menu.listentity_menu);
                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    Intent intent;

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.edit:
                                Gson gson = new Gson();
                                String instance = gson.toJson(instances.get(position));
                                Log.i("Adapter", instances.get(position).getName());

                                Intent intent = new Intent(context, EditCamera.class);
                                intent.putExtra("EditCamera", instance);
                                intent.putExtra("Position", position);
                                context.startActivity(intent);
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popup.show();

            }
        });
    }

    @Override
    public int getItemCount() {
        return mRaspberry_list.size();
    }
}
