package com.succorfish.geofence.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.succorfish.geofence.BaseFragment.BaseFragment;
import com.succorfish.geofence.MainActivity;
import com.succorfish.geofence.R;
import com.succorfish.geofence.interfaceFragmentToActivity.IndustrySpeificConfigurationPackets;
import com.succorfish.geofence.interfaceFragmentToActivity.WifiConfigurationPackets;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.succorfish.geofence.MainActivity.CONNECTED_BLE_ADDRESS;
import static com.succorfish.geofence.blecalculation.WifiConfiguration.wifiConfigurationDataArrayList;
import static com.succorfish.geofence.blecalculation.WifiConfiguration.wifiConfigurationDataSet;
import static com.succorfish.geofence.utility.Utility.radioButtonSelectedValue;

public class FragmentWifiConfiguration extends BaseFragment {
    private Unbinder unbinder;
    View fragmentWifiConfigurationView;
    MainActivity mainActivity;
    String connectedBleAddress="";
    WifiConfigurationPackets fragmentWifiConfigurationPacketsnterface;
    /**
     * Enable/disable wifi
     */
    @BindView(R.id.wifi_disable)
    RadioButton wifi_disable_radioButton;
    @BindView(R.id.wifi_enable)
    RadioButton wifi_enable_radioButton;
    @BindView(R.id.wifi_unchange)
    RadioButton wifi_unchange_radioButton;
    /**
     *Wifi logging
     */
    @BindView(R.id.wifi_loggingFirmware_disable)
    RadioButton wifi_loggingFirmware_disable_radioButton;
    @BindView(R.id.wifi_loggingFirmware_enable)
    RadioButton wifi_loggingFirmware_enable_radioButton;
    @BindView(R.id.wifi_loggingFirmware_unchange)
    RadioButton wifi_loggingFirmware_unchange_radioButton;


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
        fragmentWifiConfigurationView = inflater.inflate(R.layout.fragment_wifi_configuration, container, false);
        unbinder = ButterKnife.bind(this, fragmentWifiConfigurationView);
        bottomLayoutVisibility(false);
        interfaceIntialization();
        getConnectedBleAddress();
        return fragmentWifiConfigurationView;
    }

    private void bottomLayoutVisibility(boolean hide_true_unhide_false){
        mainActivity.hideBottomLayout(hide_true_unhide_false);
    }

    private void interfaceIntialization() {
        fragmentWifiConfigurationPacketsnterface=(WifiConfigurationPackets) getActivity();
    }



    private void getConnectedBleAddress(){
        connectedBleAddress=CONNECTED_BLE_ADDRESS;
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
        unbinder.unbind();
        System.gc();
    }
    @Override
    public String toString() {
        return FragmentWifiConfiguration.class.getSimpleName();
    }

    @OnClick(R.id.fragment_wifi_back)
    public void wifiConfigBack(){
        if(isVisible()){
            mainActivity.onBackPressed();
        }
    }

    @OnClick(R.id.save_wifi_config)
    public void WifiSaveClick(){
        processUISendValues();
    }

    private void processUISendValues() {
        byte enable_disable_wifi=radioButtonSelectedValue(wifi_disable_radioButton,wifi_enable_radioButton,wifi_unchange_radioButton);
        byte firmwareLog=radioButtonSelectedValue(wifi_loggingFirmware_disable_radioButton,wifi_loggingFirmware_enable_radioButton,wifi_loggingFirmware_unchange_radioButton);
        if(fragmentWifiConfigurationPacketsnterface!=null){
            fragmentWifiConfigurationPacketsnterface.wifiConfigurationDetails(connectedBleAddress,wifiConfigurationDataArrayList(enable_disable_wifi,firmwareLog));
        }
    }
}
