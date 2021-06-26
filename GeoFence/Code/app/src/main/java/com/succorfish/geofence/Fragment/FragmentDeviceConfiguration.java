package com.succorfish.geofence.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.succorfish.geofence.BaseFragment.BaseFragment;
import com.succorfish.geofence.MainActivity;
import com.succorfish.geofence.R;
import com.succorfish.geofence.interfaceFragmentToActivity.DeviceConfigurationPackets;
import com.succorfish.geofence.interfaceFragmentToActivity.MessageChatPacket;
import java.util.ArrayList;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.succorfish.geofence.MainActivity.CONNECTED_BLE_ADDRESS;
import static com.succorfish.geofence.blecalculation.DeviceConfiguration.sendCheapsetModeRadioButtonValues;
import static com.succorfish.geofence.blecalculation.DeviceConfiguration.sendData;
import static com.succorfish.geofence.blecalculation.DeviceConfiguration.sendIntervalSeconds;
import static com.succorfish.geofence.utility.Utility.checkEditTextDataIsEmpty;


public class FragmentDeviceConfiguration extends BaseFragment {
    private Unbinder unbinder;
    View fragmentDeviceConfigurationView;
    MainActivity mainActivity;
    @BindView(R.id.fragment_deviceConfig_back)
    ImageView backbutton;
    /**
     * EditText fields.
     */
    @BindView(R.id.gsm_interval_editText)
    EditText gsm_interval_text;
    @BindView(R.id.gsm_timeout_interval_editText)
    EditText gsm_timeout_interval_text;
    @BindView(R.id.gps_interval_editText)
    EditText gps_interval_text;
    @BindView(R.id.gps_timeout_editText)
    EditText gps_timeout_text;
    @BindView(R.id.satellite_interval_editText)
    EditText satellite_interval_text;
    @BindView(R.id.satellite_timeout_editText)
    EditText satellite_timeout_text;
    @BindView(R.id.cheapest_multiplier_editText)
    EditText cheapest_multiplier_text;
    /**
     * Radio Groups
     */
    @BindView(R.id.cheapest_radio_group)
    RadioGroup cheapestRadioGroup;
    @BindView(R.id.ultra_power_mode)
    RadioGroup ultra_power_mode;
    @BindView(R.id.usb_download_mode)
    RadioGroup usb_download_mode;
    @BindView(R.id.iridium_always)
    RadioGroup iridium_always;
    @BindView(R.id.iridium_event)
    RadioGroup iridium_event;
    @BindView(R.id.instant_temper)
    RadioGroup instant_temper;
    @BindView(R.id.waypoint_movement)
    RadioGroup way_point_movement;
    DeviceConfigurationPackets deviceConfigurationPackets;
    String connectedBleAddress="";
    /**
     *
     * RadioButton cheapset mode.
     */
    @BindView(R.id.cheapest_radio_group_on)
    RadioButton cheapest_radio_group_on;
    @BindView(R.id.cheapest_radio_group_off)
    RadioButton cheapest_radio_group_off;
    @BindView(R.id.cheapest_radio_group_unchange)
    RadioButton cheapest_radio_group_unchange;
    /**
     * Ultra power mode
     *
     */
    @BindView(R.id.ultra_power_mode_on)
    RadioButton ultra_power_mode_on;
    @BindView(R.id.ultra_power_mode_off)
    RadioButton ultra_power_mode_off;
    @BindView(R.id.ultra_power_mode_unchange)
    RadioButton ultra_power_mode_unchange;
    /**
     *  USB download mode
     *
     */
    @BindView(R.id.usb_download_mode_on)
    RadioButton usb_download_mode_on;
    @BindView(R.id.usb_download_mode_off)
    RadioButton usb_download_mode_off;
    @BindView(R.id.usb_download_mode_unchange)
    RadioButton usb_download_mode_unchange;
    /**
     * Iridium always ON
     *
     */
    @BindView(R.id.iridium_always_on)
    RadioButton iridium_always_on;
    @BindView(R.id.iridium_always_off)
    RadioButton iridium_always_off;
    @BindView(R.id.iridium_always_unchnage)
    RadioButton iridium_always_unchnage;
    /**
     *iridium_event_on
     */
    @BindView(R.id.iridium_event_on)
    RadioButton iridium_event_on;
    @BindView(R.id.iridium_event_off)
    RadioButton iridium_event_off;
    @BindView(R.id.iridium_event_unchange)
    RadioButton iridium_event_unchange;
    /**
     * Instant Temper
     *
     */
    @BindView(R.id.instant_temper_on)
    RadioButton instant_temper_on;
    @BindView(R.id.instant_temper_off)
    RadioButton instant_temper_off;
    @BindView(R.id.instant_temper_unchange)
    RadioButton instant_temper_unchange;
    /**
     * way point movement
     *
     */


    @BindView(R.id.waypoint_movement_on)
    RadioButton waypoint_movement_on;
    @BindView(R.id.waypoint_movement_off)
    RadioButton waypoint_movement_off;
    @BindView(R.id.waypoint_movement_unchnage)
    RadioButton waypoint_movement_unchnage;

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
        fragmentDeviceConfigurationView = inflater.inflate(R.layout.fragment_device_configuration, container, false);
        unbinder = ButterKnife.bind(this, fragmentDeviceConfigurationView);
        bottomLayoutVisibility(false);
        interfaceIntialization();
        getConnectedBleAddress();
        return fragmentDeviceConfigurationView;
    }

    private void bottomLayoutVisibility(boolean hide_true_unhide_false){
        mainActivity.hideBottomLayout(hide_true_unhide_false);
    }
    private void interfaceIntialization(){
        deviceConfigurationPackets=(DeviceConfigurationPackets) getActivity();
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
        return FragmentDeviceConfiguration.class.getSimpleName();
    }

    @OnClick(R.id.fragment_deviceConfig_back)
    public void backButtonPress() {
        if (isVisible()) {
            mainActivity.onBackPressed();
        }
    }

    @OnClick(R.id.save_text_view)
    public void saveButtonCLick(){
        processAllValuesOfUI();
        Log.d("Device Configuration", "saveButtonCLick: ");
    }


    private void getConnectedBleAddress(){
        connectedBleAddress=CONNECTED_BLE_ADDRESS;
    }
    private void processAllValuesOfUI(){
        /**
         * Interval in Seconds UI process list.
         */
        ArrayList<byte[]> deviceConfigurationArray=new ArrayList<byte[]>();
        int gsm_intervalByteValue=(byte)0XFF;
        int gsm_timeout_intervalByteValue=(byte)0XFF;
        int gps_intervalByteValue=(byte)0XFF;
        int gps_timeoutByteValue=(byte)0XFF;
        int satellite_intervalByteValue=(byte)0XFF;
        int satellite_timeoutByteValue=(byte)0XFF;
        int cheapest_multiplierByteValue=(byte)0XFF;
        if(checkEditTextDataIsEmpty(gsm_interval_text)){
            gsm_intervalByteValue= Integer.parseInt(gsm_interval_text.getText().toString());
        }if(checkEditTextDataIsEmpty(gsm_timeout_interval_text)){
            gsm_timeout_intervalByteValue= Integer.parseInt(gsm_timeout_interval_text.getText().toString());
        }
        if(checkEditTextDataIsEmpty(gps_interval_text)){
            gps_intervalByteValue= Integer.parseInt(gps_interval_text.getText().toString());
        }if(checkEditTextDataIsEmpty(gps_timeout_text)){
            gps_timeoutByteValue= Integer.parseInt(gps_timeout_text.getText().toString());
        }if(checkEditTextDataIsEmpty(satellite_interval_text)){
            satellite_intervalByteValue= Integer.parseInt(satellite_interval_text.getText().toString());
        }if(checkEditTextDataIsEmpty(satellite_timeout_text)){
            satellite_timeoutByteValue= Integer.parseInt(satellite_timeout_text.getText().toString());
        }if(checkEditTextDataIsEmpty(cheapest_multiplier_text)){
            cheapest_multiplierByteValue= Integer.parseInt(cheapest_multiplier_text.getText().toString());
        }
      byte [] intervalArray=  sendIntervalSeconds(gsm_intervalByteValue,
                gsm_timeout_intervalByteValue,
                gps_intervalByteValue,
                gps_timeoutByteValue,
                satellite_intervalByteValue,
                satellite_timeoutByteValue,
                cheapest_multiplierByteValue );

      //  byte [] intervalArray=sendData();

        /**
         * Radio button configure list.
         */

      byte cheapestModeValue=radioButtonSelectedValue(cheapest_radio_group_on,cheapest_radio_group_off,cheapest_radio_group_unchange);
      byte ultraPowerMode=radioButtonSelectedValue(ultra_power_mode_on,ultra_power_mode_off,ultra_power_mode_unchange);
      byte usbDownloadMode=radioButtonSelectedValue(usb_download_mode_on,usb_download_mode_off,usb_download_mode_unchange);
      byte iridurmAlwaysOnValue=radioButtonSelectedValue(iridium_always_on,iridium_always_off,iridium_always_unchnage);
      byte iridiumEventValue=radioButtonSelectedValue(iridium_event_on,iridium_event_off,iridium_event_unchange);
      byte instantTemper=radioButtonSelectedValue(instant_temper_on,instant_temper_off,instant_temper_unchange);
      byte waypoint_movement=radioButtonSelectedValue(waypoint_movement_on,waypoint_movement_off,waypoint_movement_unchnage);

      byte [] radiobuttonSelected=  sendCheapsetModeRadioButtonValues(
              cheapestModeValue,
              ultraPowerMode,
              usbDownloadMode,
              iridurmAlwaysOnValue,
              iridiumEventValue,
              instantTemper,
              waypoint_movement);

        deviceConfigurationArray.add(intervalArray);
        deviceConfigurationArray.add(radiobuttonSelected);
        if(deviceConfigurationPackets!=null){
            deviceConfigurationPackets.deviceConfigurationDetails(connectedBleAddress,deviceConfigurationArray);
        }
    }


    private byte radioButtonSelectedValue(RadioButton on,RadioButton off,RadioButton unchanged){
        byte value= (byte) 0xff;
        if(on.isChecked()){
            value=01;
        }else if(off.isChecked()){
            value=00;
        }else if(unchanged.isChecked()) {
            value= (byte) 0xff;
        }
        return value;
    }
}
