package com.succorfish.geofence.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.succorfish.geofence.BaseFragment.BaseFragment;
import com.succorfish.geofence.MainActivity;
import com.succorfish.geofence.R;
import com.succorfish.geofence.dialog.DialogProvider;
import com.succorfish.geofence.interfaceFragmentToActivity.MessageChatPacket;
import com.succorfish.geofence.interfaceFragmentToActivity.ServerConfigurationDataPass;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.succorfish.geofence.MainActivity.CONNECTED_BLE_ADDRESS;
import static com.succorfish.geofence.blecalculation.ServerConfiguration.serverConfiguration_ServerPacket;
import static com.succorfish.geofence.blecalculation.ServerConfiguration.startFristPacket_ServerConfiguration;
import static com.succorfish.geofence.utility.Utility.showTost;
import static com.succorfish.geofence.utility.Utility.splitString;

public class FragmentServerConfiguration extends BaseFragment {

    private Unbinder unbinder;
    View fragmentServerConfigurationView;
    MainActivity mainActivity;

    @BindView(R.id.server_address_EditText)
    EditText server_address;
    @BindView(R.id.keep_aliveInterval_EditText)
    EditText keep_aliveInterval;
    @BindView(R.id.server_port_EditText)
    EditText server_port;
    ServerConfigurationDataPass serverConfigurationDataPass;
    String connected_bleAddress="";
    DialogProvider dialogProvider;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentServerConfigurationView = inflater.inflate(R.layout.fragment_server_configuration, container, false);
        unbinder = ButterKnife.bind(this, fragmentServerConfigurationView);
        bottomLayoutVisibility(false);
        interfaceIntialization();
        getConnectedBleAddress();
        intializeDialog();
        return fragmentServerConfigurationView;
    }

    private void bottomLayoutVisibility(boolean hide_true_unhide_false){
        mainActivity.hideBottomLayout(hide_true_unhide_false);
    }


    private void intializeDialog() {
        dialogProvider = new DialogProvider(getActivity());
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
    @Override
    public String toString() {
        return FragmentServerConfiguration.class.getSimpleName();
    }
    @OnClick(R.id.fragment_server_configuration_back)
    public void backbuttonPresss(){
        mainActivity.onBackPressed();
    }

    private void processAllInputs(){
        String serverAddress=server_address.getText().toString();
        String keepAliveInterval=keep_aliveInterval.getText().toString();
        String serverPort=server_port.getText().toString();
        ArrayList<byte[]> serverConfigurationlist=new ArrayList<byte[]>();
        List<String> packetSplitIn12parts=  splitString(serverAddress,12);
        int indexPosition=0;
        serverConfigurationlist.add(startFristPacket_ServerConfiguration(packetSplitIn12parts.size(),Integer.parseInt(serverPort),Integer.parseInt(keepAliveInterval)));
        for (String individualString:packetSplitIn12parts) {
            indexPosition++;
            serverConfigurationlist.add(serverConfiguration_ServerPacket(indexPosition,individualString));
        }
        if(serverConfigurationDataPass!=null){
            serverConfigurationDataPass.ServerConfigurationPacketArray(connected_bleAddress,serverConfigurationlist);
        }
    }

    @OnClick(R.id.save_server_configuration)
    public void saveServerConfigurationButtonClick(){
            if((server_address.getText().toString().equalsIgnoreCase(""))||
                    (keep_aliveInterval.getText().toString().equalsIgnoreCase(""))||
                    (server_port.getText().toString().equalsIgnoreCase(""))){
                dialogProvider.errorDialog("All fields are Mandatory");
                return;
            }else if((Integer.parseInt(keep_aliveInterval.getText().toString())>240)){
                dialogProvider.errorDialog("Interval Should not be exceed 240");
                return;
            }else if((Integer.parseInt(server_port.getText().toString())>65535)){
                dialogProvider.errorDialog("Port number should not exceed 65535");
                showTost(getActivity(),"Port number should not exceed 65535");
                return;
            }
            else {
                processAllInputs();
            }
    }

    private void interfaceIntialization(){
        serverConfigurationDataPass=(ServerConfigurationDataPass) getActivity();
    }

    private void getConnectedBleAddress(){
        connected_bleAddress=CONNECTED_BLE_ADDRESS;
    }
}