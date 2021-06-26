package com.succorfish.geofence.Fragment;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.succorfish.geofence.BaseFragment.BaseFragment;
import com.succorfish.geofence.MainActivity;
import com.succorfish.geofence.R;
import com.succorfish.geofence.customObjects.ButtonBandConfiguration;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.succorfish.geofence.MainActivity.CONNECTED_BLE_ADDRESS;

public class FragmentBandConfiguration extends BaseFragment {
    private Unbinder unbinder;
    View fragmentBandConfugurationView;
    MainActivity mainActivity;
    ArrayList<ButtonBandConfiguration> buttonBandConfigurationsArrayList=new ArrayList<ButtonBandConfiguration>();
    long buttonSelectedValue;
    private  String connected_bleAddress="";

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
        fragmentBandConfugurationView = inflater.inflate(R.layout.fragment_band_configuration, container, false);
        unbinder = ButterKnife.bind(this, fragmentBandConfugurationView);
        getConnectedBleAddress();
        addAllbuttonToList();
        bottomLayoutVisibility(false);
        return fragmentBandConfugurationView;
    }

    private void bottomLayoutVisibility(boolean hide_true_unhide_false){
        mainActivity.hideBottomLayout(hide_true_unhide_false);
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
        return FragmentBandConfiguration.class.getSimpleName();
    }

    @OnClick(R.id.band_config_save)
    public void bandConfigSave(){
            passButtonSelectedValue();
    }


    private void passButtonSelectedValue() {
        buttonSelectedValue=0;
        for (ButtonBandConfiguration buttonBandConfiguration:buttonBandConfigurationsArrayList) {
            if(buttonBandConfiguration.isButtonChecked()){
                buttonSelectedValue=buttonSelectedValue+ buttonBandConfiguration.getButton_Ifchecked_value();
            }
        }
        mainActivity.preferenceHelper.setConfigutation_BANDCONFIG(connected_bleAddress+" "+FragmentBandConfiguration.class.getSimpleName(),""+buttonSelectedValue);
        mainActivity.replaceFragment(new FragmentSimConfiguration(),connected_bleAddress,"BLE_ADDRESS",false);
    }

    private void getConnectedBleAddress(){
        connected_bleAddress=CONNECTED_BLE_ADDRESS;
    }

    @OnClick({R.id.button_1,R.id.button_2,R.id.button_3,R.id.button_4,R.id.button_5,R.id.button_6,R.id.button_7,R.id.button_8,R.id.button_9,R.id.button_10,R.id.button_11,
            R.id.button_12,R.id.button_13,R.id.button_14,R.id.button_15,R.id.button_16,R.id.button_17,R.id.button_18,R.id.button_19,R.id.button_20,R.id.button_21,
            R.id.button_22,R.id.button_23,R.id.button_24,R.id.button_25,R.id.button_26,R.id.button_27,R.id.button_28
    })
    public void setBandConfigurationView(View view){
        switch (view.getId()){
            case R.id.button_1:
                clickButtonId(R.id.button_1,view.findViewById(R.id.button_1));
                break;
            case R.id.button_2:
                clickButtonId(R.id.button_2,view.findViewById(R.id.button_2));
                break;
            case R.id.button_3:
                clickButtonId(R.id.button_3,view.findViewById(R.id.button_3));
                break;
            case R.id.button_4:
                clickButtonId(R.id.button_4,view.findViewById(R.id.button_4));
                break;
            case R.id.button_5:
                clickButtonId(R.id.button_5,view.findViewById(R.id.button_5));
                break;
            case R.id.button_6:
                clickButtonId(R.id.button_6,view.findViewById(R.id.button_6));
                break;
            case R.id.button_7:
                clickButtonId(R.id.button_7,view.findViewById(R.id.button_7));
                break;
            case R.id.button_8:
                clickButtonId(R.id.button_8,view.findViewById(R.id.button_8));
                break;
            case R.id.button_9:
                clickButtonId(R.id.button_9,view.findViewById(R.id.button_9));
                break;
            case R.id.button_10:
                clickButtonId(R.id.button_10,view.findViewById(R.id.button_10));
                break;
            case R.id.button_11:
                clickButtonId(R.id.button_11,view.findViewById(R.id.button_11));
                break;
            case R.id.button_12:
                clickButtonId(R.id.button_12,view.findViewById(R.id.button_12));
                break;
            case R.id.button_13:
                clickButtonId(R.id.button_13,view.findViewById(R.id.button_13));
                break;
            case R.id.button_14:
                clickButtonId(R.id.button_14,view.findViewById(R.id.button_14));
                break;
            case R.id.button_15:
                clickButtonId(R.id.button_15,view.findViewById(R.id.button_15));
                break;
            case R.id.button_16:
                clickButtonId(R.id.button_16,view.findViewById(R.id.button_16));
                break;
                case R.id.button_17:
                clickButtonId(R.id.button_17,view.findViewById(R.id.button_17));
                break;
            case R.id.button_18:
                clickButtonId(R.id.button_18,view.findViewById(R.id.button_18));
                break;
            case R.id.button_19:
                clickButtonId(R.id.button_19,view.findViewById(R.id.button_19));
                break;
            case R.id.button_20:
                clickButtonId(R.id.button_20,view.findViewById(R.id.button_20));
                break;
            case R.id.button_21:
                clickButtonId(R.id.button_21,view.findViewById(R.id.button_21));
                break;
            case R.id.button_22:
                clickButtonId(R.id.button_22,view.findViewById(R.id.button_22));
                break;
            case R.id.button_23:
                clickButtonId(R.id.button_23,view.findViewById(R.id.button_23));
                break;
            case R.id.button_24:
                clickButtonId(R.id.button_24,view.findViewById(R.id.button_24));
                break;
            case R.id.button_25:
                clickButtonId(R.id.button_25,view.findViewById(R.id.button_25));
                break;
            case R.id.button_26:
                clickButtonId(R.id.button_26,view.findViewById(R.id.button_26));
                break;
            case R.id.button_27:
                clickButtonId(R.id.button_27,view.findViewById(R.id.button_27));
                break;
            case R.id.button_28:
                clickButtonId(R.id.button_28,view.findViewById(R.id.button_28));
                break;


        }
    }

    @OnClick(R.id.fragment_band_configuration_back)
    public void backArrowClick(){
        if(isVisible()){
            mainActivity.replaceFragment(new FragmentSimConfiguration(),connected_bleAddress,"BLE_ADDRESS",false);
        }
    }

    private void addAllbuttonToList(){
        buttonBandConfigurationsArrayList.add(new ButtonBandConfiguration(false,0,R.id.button_1));
        buttonBandConfigurationsArrayList.add(new ButtonBandConfiguration(false,1,R.id.button_2));
        buttonBandConfigurationsArrayList.add(new ButtonBandConfiguration(false,2,R.id.button_3));
        buttonBandConfigurationsArrayList.add(new ButtonBandConfiguration(false,3,R.id.button_4));
        buttonBandConfigurationsArrayList.add(new ButtonBandConfiguration(false,4,R.id.button_5));
        buttonBandConfigurationsArrayList.add(new ButtonBandConfiguration(false,5,R.id.button_6));
        buttonBandConfigurationsArrayList.add(new ButtonBandConfiguration(false,6,R.id.button_7));
        buttonBandConfigurationsArrayList.add(new ButtonBandConfiguration(false,7,R.id.button_8));
        buttonBandConfigurationsArrayList.add(new ButtonBandConfiguration(false,8,R.id.button_9));
        buttonBandConfigurationsArrayList.add(new ButtonBandConfiguration(false,9,R.id.button_10));
        buttonBandConfigurationsArrayList.add(new ButtonBandConfiguration(false,10,R.id.button_11));
        buttonBandConfigurationsArrayList.add(new ButtonBandConfiguration(false,11,R.id.button_12));
        buttonBandConfigurationsArrayList.add(new ButtonBandConfiguration(false,12,R.id.button_13));
        buttonBandConfigurationsArrayList.add(new ButtonBandConfiguration(false,13,R.id.button_14));
        buttonBandConfigurationsArrayList.add(new ButtonBandConfiguration(false,14,R.id.button_15));
        buttonBandConfigurationsArrayList.add(new ButtonBandConfiguration(false,15,R.id.button_16));
        buttonBandConfigurationsArrayList.add(new ButtonBandConfiguration(false,16,R.id.button_17));
        buttonBandConfigurationsArrayList.add(new ButtonBandConfiguration(false,17,R.id.button_18));
        buttonBandConfigurationsArrayList.add(new ButtonBandConfiguration(false,18,R.id.button_19));
        buttonBandConfigurationsArrayList.add(new ButtonBandConfiguration(false,19,R.id.button_20));
        buttonBandConfigurationsArrayList.add(new ButtonBandConfiguration(false,20,R.id.button_21));
        buttonBandConfigurationsArrayList.add(new ButtonBandConfiguration(false,21,R.id.button_22));
        buttonBandConfigurationsArrayList.add(new ButtonBandConfiguration(false,22,R.id.button_23));
        buttonBandConfigurationsArrayList.add(new ButtonBandConfiguration(false,23,R.id.button_24));
        buttonBandConfigurationsArrayList.add(new ButtonBandConfiguration(false,24,R.id.button_25));
        buttonBandConfigurationsArrayList.add(new ButtonBandConfiguration(false,25,R.id.button_26));
        buttonBandConfigurationsArrayList.add(new ButtonBandConfiguration(false,26,R.id.button_27));
        buttonBandConfigurationsArrayList.add(new ButtonBandConfiguration(false,27,R.id.button_28));
    }

    private void clickButtonId(int button_id, Button button){
        for (ButtonBandConfiguration buttonBandConfiguration:buttonBandConfigurationsArrayList) {
            if(buttonBandConfiguration.getButton_id()==button_id){
                if(buttonBandConfiguration.isButtonChecked()){
                    buttonBandConfiguration.setButtonChecked(false);
                    button.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));;
                    buttonBandConfiguration.setButton_Ifchecked_value(0);
                }else {
                    buttonBandConfiguration.setButtonChecked(true);
                    button.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                    buttonBandConfiguration.setButton_Ifchecked_value(buttonBandConfiguration.getButton_label_value());
                   long value= buttonBandConfiguration.getButton_Ifchecked_value();
                }
            }
        }
    }



}
