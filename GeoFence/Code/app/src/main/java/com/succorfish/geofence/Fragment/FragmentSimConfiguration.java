package com.succorfish.geofence.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.util.WorkSourceUtil;
import com.succorfish.geofence.BaseFragment.BaseFragment;
import com.succorfish.geofence.MainActivity;
import com.succorfish.geofence.R;
import com.succorfish.geofence.interfaceFragmentToActivity.IndustrySpeificConfigurationPackets;
import com.succorfish.geofence.interfaceFragmentToActivity.SimConfigurationPackets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.succorfish.geofence.MainActivity.CONNECTED_BLE_ADDRESS;
import static com.succorfish.geofence.blecalculation.SimConfiguration.StartSimConfigurationFristPacket;
import static com.succorfish.geofence.blecalculation.SimConfiguration.endPacketSimConfiguration;
import static com.succorfish.geofence.blecalculation.SimConfiguration.simConfigurationDataArray;
import static com.succorfish.geofence.utility.Utility.splitString;

public class FragmentSimConfiguration extends BaseFragment {
    private Unbinder unbinder;
    View fragmentSimConfigurationView;
    MainActivity mainActivity;
    @BindView(R.id.uart_selected_details)
    TextView uartSelecteditem;
    @BindView(R.id.band_configuration_details)
    TextView bandConfigurationSelection;
    private  String connected_bleAddress="";
    Map<String,Byte> mapValuesbandConfiguration;
    byte UART_CONFIGURATION_VALUE;
    int BAND_CONFIGURATION_VALUE;
    private ArrayList<byte[]> completeSimConfigurationPacket;
    @BindView(R.id.e_sim)
    RadioButton esim_radiobutton;
    @BindView(R.id.nano_sim)
    RadioButton nansim_radiobutton;
    @BindView(R.id.sim_configuration_enter_APN_address)
    TextView apn_address_textView;
    @BindView(R.id.sim_configuration_enter_username)
    TextView apn_username_textView;
    @BindView(R.id.sim_configuration_enter_password)
    TextView apn_passowrd_textView;
    SimConfigurationPackets simConfigurationPacketInterface;

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
        fragmentSimConfigurationView = inflater.inflate(R.layout.fragment_sim_configuration, container, false);
        unbinder = ButterKnife.bind(this, fragmentSimConfigurationView);
        bottomLayoutVisibility(false);
        addMapValuesForComparision();
        getConnectedBleAddress();
        setTextUART();
        setTextBAND_CONFIG();
        interfaceIntialization();
        showSimSelectedUI_IfuserAlreadySelected();
        return fragmentSimConfigurationView;
    }

    private void bottomLayoutVisibility(boolean hide_true_unhide_false){
        mainActivity.hideBottomLayout(hide_true_unhide_false);
    }

    private void getConnectedBleAddress(){
        connected_bleAddress=CONNECTED_BLE_ADDRESS;
    }

    private void interfaceIntialization(){
        simConfigurationPacketInterface=(SimConfigurationPackets) getActivity();
    }

    private void setTextUART(){
     String UART_value= mainActivity.preferenceHelper.getConfiguration_BANDCONFIG(connected_bleAddress+" "+ FragmentUARTConfiguration.class.getSimpleName());
                if(!UART_value.equalsIgnoreCase("")){
                    uartSelecteditem.setText("UART configuration\n"+UART_value);
                    UART_CONFIGURATION_VALUE= getValueFromMap(UART_value);
                }else if(mainActivity.preferenceHelper.getConfiguration_BANDCONFIG(connected_bleAddress+" "+ FragmentUARTConfiguration.class.getSimpleName()).equalsIgnoreCase("")){
                    uartSelecteditem.setText("UART configuration");
                }
    }
    private void setTextBAND_CONFIG(){
        String bandConfigurationValue= mainActivity.preferenceHelper.getConfiguration_BANDCONFIG(connected_bleAddress+" "+ FragmentBandConfiguration.class.getSimpleName());
        if(!(bandConfigurationValue.equalsIgnoreCase(""))&&(!bandConfigurationValue.equalsIgnoreCase("0"))){
            bandConfigurationSelection.setText("Band Configuration \n"+bandConfigurationValue);
            BAND_CONFIGURATION_VALUE=Integer.parseInt(bandConfigurationValue);
        }else if(bandConfigurationValue.equalsIgnoreCase("")||(bandConfigurationValue.equalsIgnoreCase("0"))){
            bandConfigurationSelection.setText("Band Configuration");
        }
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
        return FragmentSimConfiguration.class.getSimpleName();
    }
    @OnClick(R.id.fragment_sim_confi_back)
    public void onBackbuttonPressed(){
        if(isVisible()){
            mainActivity.onBackPressed();
        }
    }

    @OnClick(R.id.sim_confi_save)
    public void onCLickSaveSimConfiguration(){
        if(isVisible()){
            if(allFields_Filled()){
                completeSimConfigurationPacket=new ArrayList<byte[]>();
                addFristpacket();
                add_APN_ADDRESS_PACKETS();
                add_USERNAME_ADDRESS_PACKETS();
                add_PASSWORD_ADDRESS_PACKETS();
                add_EndPacketSimConfiguration();
                pass_All_Sim_List_PacketsTo_MainActivity();
            }     else {
                Toast.makeText(getActivity(),"Please select Sim",Toast.LENGTH_SHORT).show();
            }       ;
        }
    }

    private void pass_All_Sim_List_PacketsTo_MainActivity() {
        if(simConfigurationPacketInterface!=null){
            simConfigurationPacketInterface.SimConfigurationDetails(connected_bleAddress,completeSimConfigurationPacket);
        }
    }

    private void add_APN_ADDRESS_PACKETS() {
        List<String> listOfString_startPacket_apn=  splitString(apn_address_textView.getText().toString(),12);
       int sizeOfTotalPackts= listOfString_startPacket_apn.size();
        for (String individualString:listOfString_startPacket_apn) {
            byte opcode=0x02;
            completeSimConfigurationPacket.add(simConfigurationDataArray(opcode,sizeOfTotalPackts,individualString));
            sizeOfTotalPackts--;
        }
    }

    private void add_USERNAME_ADDRESS_PACKETS(){
        List<String> listOfString_startPacket_apn=  splitString(apn_username_textView.getText().toString(),12);
        int sizeOfTotalPackts= listOfString_startPacket_apn.size();
        for (String individualString:listOfString_startPacket_apn) {
            byte opcode=0x03;
            completeSimConfigurationPacket.add(simConfigurationDataArray(opcode,sizeOfTotalPackts,individualString));
            sizeOfTotalPackts--;
        }
    }

    private void add_PASSWORD_ADDRESS_PACKETS(){
        List<String> listOfString_startPacket_apn=  splitString(apn_passowrd_textView.getText().toString(),12);
        int sizeOfTotalPackts= listOfString_startPacket_apn.size();
        for (String individualString:listOfString_startPacket_apn) {
            byte opcode=0x04;
            completeSimConfigurationPacket.add(simConfigurationDataArray(opcode,sizeOfTotalPackts,individualString));
            sizeOfTotalPackts--;
        }
    }
    private void add_EndPacketSimConfiguration(){
        completeSimConfigurationPacket.add(endPacketSimConfiguration());
    }


    @OnClick({R.id.e_sim,R.id.nano_sim})
    public void onRadioButtonSimSelected(RadioButton radioButton){
        switch (radioButton.getId()) {
            case R.id.e_sim:
                mainActivity.preferenceHelper.setSimSelected(connected_bleAddress+" "+getResources().getString(R.string.pre_ESIM),true);
                mainActivity.preferenceHelper.setSimSelected(connected_bleAddress+" "+getResources().getString(R.string.pre_NANO),false);
                break;
            case R.id.nano_sim:
                mainActivity.preferenceHelper.setSimSelected(connected_bleAddress+" "+getResources().getString(R.string.pre_ESIM),false);
                mainActivity.preferenceHelper.setSimSelected(connected_bleAddress+" "+getResources().getString(R.string.pre_NANO),true);
                break;
        }
    }

    private void showSimSelectedUI_IfuserAlreadySelected(){
        if(mainActivity.preferenceHelper.getSimSelected(connected_bleAddress+" "+getResources().getString(R.string.pre_ESIM))){
            esim_radiobutton.setChecked(true);
            nansim_radiobutton.setChecked(false);
        }else if(mainActivity.preferenceHelper.getSimSelected(connected_bleAddress+" "+getResources().getString(R.string.pre_NANO))){
            esim_radiobutton.setChecked(false);
            nansim_radiobutton.setChecked(true);
        }else {
            esim_radiobutton.setChecked(false);
            nansim_radiobutton.setChecked(false);
        }
    }

    private boolean allFields_Filled() {
        boolean result=false;
        if(esim_radiobutton.isChecked()||nansim_radiobutton.isChecked()){
            result=true;
        }
        return result;
    }

    private void addFristpacket() {
        String simSelected="";
        if(esim_radiobutton.isChecked()){
            simSelected="E_SIM";
        }else {
            simSelected="NANO_SIM";
        }
        completeSimConfigurationPacket.add(StartSimConfigurationFristPacket(simSelected,UART_CONFIGURATION_VALUE,BAND_CONFIGURATION_VALUE));
    }

    @OnClick(R.id.fragment_sim_configuration_URAT)
    public void UART_CONFIG_click(){
        mainActivity.replaceFragment(new FragmentUARTConfiguration(),null,null,false);
    }
    @OnClick(R.id.fragment_sim_configuration_band)
    public void bandConfiguration(){
        mainActivity.replaceFragment(new FragmentBandConfiguration(),null,null,false);
    }
    private void addMapValuesForComparision(){
        mapValuesbandConfiguration=new HashMap<String, Byte>();
        mapValuesbandConfiguration.put("1. LTE Cat M1",(byte)0x01);
        mapValuesbandConfiguration.put("2. LTE Cat NB1",(byte)0x02);
        mapValuesbandConfiguration.put("3. GPRS / eGPRS",(byte)0x03);
        mapValuesbandConfiguration.put("4. GPRS / eGPRS & LTE Cat NB1",(byte)0x04);
        mapValuesbandConfiguration.put("5. GPRS / eGPRS & LTE Cat M1",(byte)0x05);
        mapValuesbandConfiguration.put("6. LTE Cat NB1 & LTE Cat M1",(byte)0x06);
        mapValuesbandConfiguration.put("7. LTE Cat NB1 & GPRS / eGPRSM1",(byte)0x07);
        mapValuesbandConfiguration.put("8. LTE Cat M1 & LTE Cat NB1",(byte)0x08);
        mapValuesbandConfiguration.put("9. LTE Cat M1 & GPRS / eGPRS",(byte)0x09);
        mapValuesbandConfiguration.put("10. GPRS / eGPRS & LTE Cat NB1 & LTE Cat M1",(byte)0x0A);
        mapValuesbandConfiguration.put("11. GPRS / eGPRS & LTE Cat M1 & LTE Cat NB1",(byte)0x0B);
        mapValuesbandConfiguration.put("12. LTE Cat NB1 & GPRS / eGPRS & LTE Cat M1",(byte)0x0C);
        mapValuesbandConfiguration.put("13. LTE Cat NB1 & LTE Cat M1 & GPRS / eGPRS",(byte)0x0D);
        mapValuesbandConfiguration.put("14. LTE Cat M1 & LTE Cat NB1 & GPRS / eGPRS",(byte)0x0E);
        mapValuesbandConfiguration.put("15. LTE Cat M1 & GPRS / eGPRS & LTE Cat NB1",(byte)0x0F);
    }
    private byte  getValueFromMap(String key){
      byte bytevalue=  mapValuesbandConfiguration.get(key);
      return bytevalue;
    }

}

