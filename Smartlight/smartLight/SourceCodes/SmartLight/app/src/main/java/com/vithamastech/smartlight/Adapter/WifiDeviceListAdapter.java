package com.vithamastech.smartlight.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vithamastech.smartlight.PowerSocketCustomObjects.WifiDevice;
import com.vithamastech.smartlight.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WifiDeviceListAdapter extends RecyclerView.Adapter<WifiDeviceListAdapter.WifiItemViewHolder> {
    private ArrayList<WifiDevice> wifiDevicesList;
    WifiListInterface wifiListInterface;
    private Context context;

    public  WifiDeviceListAdapter(ArrayList<WifiDevice> loc_wifiDeviceArrayList){
        this.wifiDevicesList=loc_wifiDeviceArrayList;
    }

    @NonNull
    @Override
    public WifiItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context=parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.wifi_list_item, parent, false);
        return new WifiItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull WifiItemViewHolder wifiItemViewHolder, int position) {
        wifiItemViewHolder.bindDetails(wifiDevicesList.get(position));
    }

    @Override
    public int getItemCount() {
        return wifiDevicesList.size();
    }

    public class WifiItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        @BindView(R.id.wifiName)
        TextView wifiDeviceName;
        public WifiItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(wifiListInterface!=null){
                wifiListInterface.wifiListSingleItem(getAdapterPosition());
            }
        }

        void bindDetails(WifiDevice wifiDevice){
            wifiDeviceName.setText(wifiDevice.getWifiSSID());
        }
    }

    public interface WifiListInterface{
        public void wifiListSingleItem(int postion);
    }
    public void setOnItemClickListener(WifiListInterface loc_wifiListInterface){
        wifiListInterface=loc_wifiListInterface;
    }
}
