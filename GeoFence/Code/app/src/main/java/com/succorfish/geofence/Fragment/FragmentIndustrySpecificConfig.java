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
import com.succorfish.geofence.interfaceFragmentToActivity.DeviceConfigurationPackets;
import com.succorfish.geofence.interfaceFragmentToActivity.IndustrySpeificConfigurationPackets;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.succorfish.geofence.MainActivity.CONNECTED_BLE_ADDRESS;
import static com.succorfish.geofence.blecalculation.IndustrySpecificConfiguration.industrySpecifConfigDataSet;
import static com.succorfish.geofence.blecalculation.IndustrySpecificConfiguration.industrySpecificConfigurationPacket;
import static com.succorfish.geofence.utility.Utility.radioButtonSelectedValue;

public class FragmentIndustrySpecificConfig extends BaseFragment {
    private Unbinder unbinder;
    View fragmentIndustrySpecificConfigView;
    MainActivity mainActivity;
    String connectedBleAddress="";
    IndustrySpeificConfigurationPackets fragmentIndustrySpecificConfigInterface;

    /**
     * Flight Mode
     */
    @BindView(R.id.flight_mode_off)
    RadioButton radioButton_light_mode_off;
    @BindView(R.id.flight_mode_cellular)
    RadioButton radioButton_flight_mode_cellular;
    @BindView(R.id.flight_mode_satellite)
    RadioButton radioButton_flight_mode_satellite;
    /**
     * Garage Mode
     */
    @BindView(R.id.garage_mode_off)
    RadioButton radioButton_garage__mode_on;
    @BindView(R.id.garage_mode_cellular)
    RadioButton radioButton_garage__mode_cellular;
    @BindView(R.id.garage_mode_satellite)
    RadioButton radioButton_garage_mode_satellite;
    /**
     * GIGO MODE
     */
    @BindView(R.id.gigo_mode_off)
    RadioButton radioButton_gigo_mode_off;
    @BindView(R.id.gigo_cellular)
    RadioButton radioButton_gigo_cellular;
    @BindView(R.id.gigo_mode_satellite)
    RadioButton radioButton_gigo_mode_satellite;
    /**
     * Depth and Temperatire Mode.
     */
    @BindView(R.id.depth_temperature_off)
    RadioButton radioButton_depth_temperature_off;
    @BindView(R.id.depth_temperature_cellular)
    RadioButton radioButton_depth_temperature_cellular;
    @BindView(R.id.depth_temperature_satellite)
    RadioButton radioButton_depth_temperature_satellite;

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
        fragmentIndustrySpecificConfigView = inflater.inflate(R.layout.fragment_industryspecific_configuration, container, false);
        unbinder = ButterKnife.bind(this, fragmentIndustrySpecificConfigView);
        bottomLayoutVisibility(false);
        interfaceIntialization();
        getConnectedBleAddress();
        return fragmentIndustrySpecificConfigView;
    }

    private void bottomLayoutVisibility(boolean hide_true_unhide_false){
        mainActivity.hideBottomLayout(hide_true_unhide_false);
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
        return FragmentIndustrySpecificConfig.class.getSimpleName();
    }

    @OnClick(R.id.save_insdustry_config)
    public void saveIndustrySpeificConfig(){
        if(isVisible()){
            processUIValuesOnSaceClick();
        }
    }

    private void processUIValuesOnSaceClick(){
        /**
         * Flight Mode
         */
        byte flight_mode_value= (byte) 0xff;
        /**
         * Garage Mode
         */
        byte garage_mode_value=(byte)0xff;
        /**
         * GIGO MODE
         */
        byte GIGO_mode_value=(byte)0xff;
        /**
         * Depth and temperature mode.
         */
        byte depth_temperature_value=(byte)0xff;
        flight_mode_value=radioButtonSelectedValueIndustrySpecificConfiguration(radioButton_light_mode_off,radioButton_flight_mode_cellular,radioButton_flight_mode_satellite);
        garage_mode_value=radioButtonSelectedValueIndustrySpecificConfiguration(radioButton_garage__mode_on,radioButton_garage__mode_cellular,radioButton_garage_mode_satellite);
        /**
         * GIGO MODE
         */
        GIGO_mode_value=radioButtonSelectedValueIndustrySpecificConfiguration(radioButton_gigo_mode_off,radioButton_gigo_cellular,radioButton_gigo_mode_satellite);
        /**
         * Depth and temperature mode.
         */
        depth_temperature_value=radioButtonSelectedValueIndustrySpecificConfiguration(radioButton_depth_temperature_off,radioButton_depth_temperature_cellular,radioButton_depth_temperature_satellite);
        if(fragmentIndustrySpecificConfigInterface!=null){
            fragmentIndustrySpecificConfigInterface.industrySpcificConfigurationDetails(connectedBleAddress,industrySpecificConfigurationPacket(flight_mode_value,garage_mode_value,GIGO_mode_value,depth_temperature_value));
        }
    }

    private void interfaceIntialization(){
        fragmentIndustrySpecificConfigInterface=(IndustrySpeificConfigurationPackets) getActivity();
    }

    @OnClick(R.id.fragment_industry_back)
    public void backImageClick(){
        mainActivity.onBackPressed();
    }

    private static byte radioButtonSelectedValueIndustrySpecificConfiguration(RadioButton on,RadioButton cellular,RadioButton unchanged){
        byte value= (byte) 0xff;
        if(on.isChecked()){
            value=00;
        }else if(cellular.isChecked()){
            value=01;
        }else if(unchanged.isChecked()) {
            value= (byte) 0x02;
        }
        return value;
    }


}
