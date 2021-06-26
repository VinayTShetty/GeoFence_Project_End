package com.succorfish.geofence.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.succorfish.geofence.R;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
public class FragmentSettingTimeAdapter extends RecyclerView.Adapter<FragmentSettingTimeAdapter.BatteryTypeViewHolder> {
    private List<String> batteryList;
    private TimeSelectionInterface timeSelectionInterface;
    public FragmentSettingTimeAdapter(List<String> loc_battteryList) {
        this.batteryList=loc_battteryList;
    }
    @NonNull
    @Override
    public BatteryTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView= LayoutInflater.from(parent.getContext()).inflate(R.layout.batteryselectioitem,parent,false);
        return new BatteryTypeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BatteryTypeViewHolder holder, int position) {
        String batteryName=  batteryList.get(position);
        holder.batteryName.setText(batteryName);
    }
    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {
        return batteryList.size();
    }

    public class BatteryTypeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        @BindView(R.id.install_beacon_batteryName)
        TextView batteryName;

        public BatteryTypeViewHolder(@NonNull View itemView){
            super(itemView);
            ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(timeSelectionInterface!=null){
                timeSelectionInterface.timeSelection(batteryList.get(getAdapterPosition()));
            }
        }
    }
    public interface TimeSelectionInterface{
        public void timeSelection(String batteryVolts);
    }

    public void setTimeSlectionItemClick(TimeSelectionInterface loc_timeSelectionInterface){
            this.timeSelectionInterface=loc_timeSelectionInterface;
    }
}
