package com.succorfish.geofence.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.succorfish.geofence.CustomObjectsAPI.VoVessel;
import com.succorfish.geofence.R;
import com.succorfish.geofence.customObjects.CustBluetootDevices;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FragmentRemoteTrackAdapter extends RecyclerView.Adapter<FragmentRemoteTrackAdapter.FragmentRemoteTrackingViewHolder> {
    private ArrayList<VoVessel>  voVesselArrayList;
    private Context context;
    private RemoteTrackingClickItemInterfcae  remoteTrackingClickItemInterfcae;
   public FragmentRemoteTrackAdapter(ArrayList<VoVessel> voVesselArrayListLoc){
        this.voVesselArrayList=voVesselArrayListLoc;
    }

    @NonNull
    @Override
    public FragmentRemoteTrackingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context=parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.remote_track_list_item, parent, false);
        return new FragmentRemoteTrackingViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FragmentRemoteTrackingViewHolder fragmentRemoteTrackingViewHolder, int position) {
        fragmentRemoteTrackingViewHolder.bindData(voVesselArrayList.get(position),fragmentRemoteTrackingViewHolder);
    }



    @Override
    public int getItemCount() {
       return voVesselArrayList.size();
    }

    public class FragmentRemoteTrackingViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
       @BindView(R.id.remote_track_list_item)
        TextView assetListNames;
        public FragmentRemoteTrackingViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }

        void bindData(final  VoVessel voVessel,FragmentRemoteTrackingViewHolder fragmentRemoteTrackingViewHolder){
            assetListNames.setText(voVessel.getName());
            fragmentRemoteTrackingViewHolder.assetListNames.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(remoteTrackingClickItemInterfcae!=null){
                        remoteTrackingClickItemInterfcae.clickOnAssetName(voVessel,getAdapterPosition());
                    }
                }
            });
        }

        @Override
        public void onClick(View v) {

        }
    }

    public interface RemoteTrackingClickItemInterfcae{
        public void clickOnAssetName(VoVessel voVessel,int ItemSlected);;
    }
    public void setOnItemClickListner(RemoteTrackingClickItemInterfcae remoteTrackingClickItemInterfcae_loc){
       this.remoteTrackingClickItemInterfcae=remoteTrackingClickItemInterfcae_loc;

    }
}
