package com.vithamastech.smartlight.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.vithamastech.smartlight.PowerSocketCustomObjects.PowerSocket;
import com.vithamastech.smartlight.R;
import com.vithamastech.smartlight.console;

import java.util.ArrayList;
import java.util.List;

public class FoundPowerSocketAdapter extends RecyclerView.Adapter<FoundPowerSocketAdapter.ViewHolder> {

    private List<PowerSocket> powerSocketList = null;
    private OnBLEDeviceClickedCallback callback;

    public FoundPowerSocketAdapter(ArrayList<PowerSocket> powerSocketList) {
        this.powerSocketList = powerSocketList;
    }

    public FoundPowerSocketAdapter() {
        this.powerSocketList = new ArrayList<>();
    }

    @NonNull
    @Override
    public FoundPowerSocketAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_add_device_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoundPowerSocketAdapter.ViewHolder holder, int position) {
        PowerSocket powerSocket = powerSocketList.get(position);

        holder.appCompatCheckBox.setVisibility(View.GONE);

        String deviceName = powerSocket.bleName;
        if (deviceName == null) {
            deviceName = "Unknown device";
        }

        holder.textViewDeviceName.setText(deviceName);

        String deviceAddress = powerSocket.bleAddress;
        if (deviceAddress == null) {
            deviceAddress = "";
        }
        holder.textViewDeviceAddress.setText(deviceAddress);

        String monitorAddStatus = powerSocket.addButton;
        if (monitorAddStatus == null) {
            monitorAddStatus = "Connect";
        }
        holder.textViewAddDevice.setText(monitorAddStatus);

        boolean isAssociated = powerSocket.isAssociated;
        int color = Color.parseColor("#2d8659");
        if (isAssociated) {
            color = Color.parseColor("#696969");
        }
        holder.textViewDeviceAddress.setTextColor(color);

        int imagePath = 0;
        switch (powerSocket.deviceType) {
            case 0x400: // Socket
                imagePath = R.drawable.ic_default_powerstrip_icon;
                break;
            default:
                imagePath = R.drawable.ic_default_pic;
                break;
        }
        holder.imageViewDevice.setImageResource(imagePath);
    }

    public void addDevice(PowerSocket powerSocket) {
        if (!powerSocketList.contains(powerSocket)) {
            powerSocketList.add(powerSocket);
            notifyItemInserted(powerSocketList.size() - 1);
        }
    }

    public void addDevices(List<PowerSocket> powerSocketList) {
        for (PowerSocket powerSocket : powerSocketList) {
            if (!this.powerSocketList.contains(powerSocket)) {
                this.powerSocketList.add(powerSocket);
            }
        }
        notifyDataSetChanged();
    }

    public void removeDevice(PowerSocket powerSocket) {
        int index = powerSocketList.indexOf(powerSocket);
        if (index > -1) {
            powerSocketList.remove(index);
            notifyItemRemoved(index);
            notifyItemRangeChanged(index, powerSocketList.size());
        }
    }

    public void clear() {
        powerSocketList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return powerSocketList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView textViewDeviceName;
        public TextView textViewDeviceAddress;
        public TextView textViewAddDevice;
        public ImageView imageViewDevice;
        public AppCompatCheckBox appCompatCheckBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDeviceName = itemView.findViewById(R.id.raw_add_device_item_textview_devicename);
            textViewDeviceAddress = itemView.findViewById(R.id.raw_add_device_item_textview_deviceid);
            textViewAddDevice = itemView.findViewById(R.id.raw_add_device_item_textview_add_remove);
            imageViewDevice = itemView.findViewById(R.id.raw_add_device_imageview_device);
            appCompatCheckBox = itemView.findViewById(R.id.raw_add_device_list_item_checkbox);
            textViewAddDevice.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.raw_add_device_item_textview_add_remove) {
                if (callback != null) {
                    int itemPosition = getAdapterPosition();
                    callback.onBLEDeviceClicked(powerSocketList.get(itemPosition), itemPosition);
                }
            }
        }
    }

    public void setOnBLEDeviceClickedCallback(OnBLEDeviceClickedCallback callback) {
        this.callback = callback;
    }

    public interface OnBLEDeviceClickedCallback {
        public void onBLEDeviceClicked(PowerSocket powerSocket, int itemPosition);
    }
}