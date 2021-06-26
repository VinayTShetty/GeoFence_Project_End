package com.succorfish.geofence.adapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import com.succorfish.geofence.R;
import com.succorfish.geofence.customObjects.CustBluetootDevices;
import java.util.ArrayList;
import butterknife.BindView;
import butterknife.ButterKnife;
public class FragmentScanAdapter extends RecyclerView.Adapter<FragmentScanAdapter.ScanItemViewHolder> {
    private ArrayList<CustBluetootDevices> customBluetoothdevices;

    private Context context;
    private ScanOnItemClickInterface onItemClickInterface;
    public FragmentScanAdapter(ArrayList<CustBluetootDevices> loc_customBluetoothdevices) {
        this.customBluetoothdevices=loc_customBluetoothdevices;
    }
    @NonNull
    @Override
    public ScanItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context=parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.fragmentscan_listitem_test, parent, false);
        return new FragmentScanAdapter.ScanItemViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(@NonNull ScanItemViewHolder scanItemViewHolder, int position) {
                scanItemViewHolder.bindBluetoothDeviceDetails(customBluetoothdevices.get(position),scanItemViewHolder);
    }
    @Override
    public int getItemCount() {
        return customBluetoothdevices.size();
    }
    
    public class ScanItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            @BindView(R.id.bleAddress)
            TextView bleAddress;
            @BindView(R.id.device_name)
            TextView device_name;
            @BindView(R.id.connected_status)
            TextView connection_status;
            /**
         * Connection Layout Status.
         */
            @BindView(R.id.connectionLayout_changes)
            LinearLayout linearLayoutConnected_layout;
            /**
         * Connection Layout Status Item
         *
         */
            @BindView(R.id.layout_separator)
            LinearLayout separatorLine;


            @BindView(R.id.connection_layout_message)
            LinearLayout linearLayoutConnected_messsage;
            @BindView(R.id.connection_layout_geofence)
            LinearLayout linearLayoutConnected_geofence;
            @BindView(R.id.connection_layout_livetracking)
            LinearLayout linearLayoutConnected_liveTracking;
            @BindView(R.id.more_option_overflowMenu)
            LinearLayout linearLayoutConnected_More_overFlowMenu;


        public ScanItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }
        void bindBluetoothDeviceDetails (final  CustBluetootDevices customBluetooth,ScanItemViewHolder scanItemViewHolder){
            bleAddress.setText(customBluetooth.getBleAddress().replace(":","").toLowerCase());
            device_name.setText(customBluetooth.getDeviceName());
            BluetoothDevice bluetoothDevice= customBluetooth.getBluetoothDevice();
            if(customBluetooth.isConnected()){
                linearLayoutConnected_layout.setVisibility(View.VISIBLE);
                connection_status.setText("Disconnect");
                separatorLine.setVisibility(View.VISIBLE);
            }else if(!customBluetooth.isConnected()) {
                connection_status.setText("Connect");
                linearLayoutConnected_layout.setVisibility(View.GONE);
                separatorLine.setVisibility(View.GONE);
            }
            scanItemViewHolder.connection_status.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onItemClickInterface!=null){
                        if((customBluetoothdevices!=null)&&(getAdapterPosition()>=0)){
                            onItemClickInterface.ConnectionStatusClick(customBluetooth,getAdapterPosition());
                        }
                    }

                }
            });

            /**
             * Message Layout click
             */
            scanItemViewHolder.linearLayoutConnected_messsage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onItemClickInterface!=null){
                        System.out.println("clicked message layout satus");
                        if((customBluetoothdevices!=null)&&(getAdapterPosition()>=0)){
                            onItemClickInterface.messagingLayoutClick(customBluetooth,getAdapterPosition());
                        }
                    }
                }
            });
            /**
             * GeoFence layout click
             */
            scanItemViewHolder.linearLayoutConnected_geofence.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onItemClickInterface!=null){
                        System.out.println("clicked geofence layout satus");
                        if((customBluetoothdevices!=null)&&(getAdapterPosition()>=0)){
                            onItemClickInterface.geoFenceLayoutClick(customBluetooth,getAdapterPosition());
                        }
                    }
                }
            });
            /**
             * Live Tracking.
             */
            scanItemViewHolder.linearLayoutConnected_liveTracking.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onItemClickInterface!=null){
                        if((customBluetoothdevices!=null)&&(getAdapterPosition()>=0)){
                            onItemClickInterface.liveTracking(customBluetooth,getAdapterPosition());
                        }
                    }
                }
            });
            /**
             * More OverFlow menu Tracking.
             */
            scanItemViewHolder.linearLayoutConnected_More_overFlowMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                   PopupMenu popup = new PopupMenu(context, linearLayoutConnected_More_overFlowMenu);
                    popup.getMenuInflater().inflate(R.menu.more, popup.getMenu());
                    popup.show();
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.settings:
                                    if(onItemClickInterface!=null){
                                        if((customBluetoothdevices!=null)&&(getAdapterPosition()>=0)){
                                            onItemClickInterface.overFlow_menu_Setting(customBluetooth,getAdapterPosition());
                                        }
                                    }
                                    popup.dismiss();
                                    break;
                                case R.id.sos:
                                    if(onItemClickInterface!=null){
                                        if((customBluetoothdevices!=null)&&(getAdapterPosition()>=0)){
                                            onItemClickInterface.overFlow_menu_SOS(customBluetooth,getAdapterPosition());
                                        }
                                    }
                                    popup.dismiss();
                                    break;
                            }
                            return true;
                        }
                    });
                }
            });


        }
        @Override
        public void onClick(View v) {
        }
    }
    public interface ScanOnItemClickInterface{
        public void ConnectionStatusClick(CustBluetootDevices customBluetoothObject,int ItemSlected);;
        public void messagingLayoutClick(CustBluetootDevices customBluetoothObject,int ItemSlected);
        public void geoFenceLayoutClick(CustBluetootDevices customBluetoothObject,int ItemSlected);
        public void liveTracking(CustBluetootDevices customBluetooth,int postion);
        public void overFlow_menu_Setting(CustBluetootDevices customBluetooth,int postion);
        public void overFlow_menu_SOS(CustBluetootDevices customBluetooth,int postion);
    }
    public void setOnItemClickListner(ScanOnItemClickInterface loc_scanOnItemClickInterface){
            this.onItemClickInterface=loc_scanOnItemClickInterface;
    }

    public void addBluetoothDevices(CustBluetootDevices customBluetooth){
            this.customBluetoothdevices.add(customBluetooth);
            notifyItemInserted(getItemCount());
    }

    public void clearList(){
        this.customBluetoothdevices.clear();
        notifyDataSetChanged();
    }
}
