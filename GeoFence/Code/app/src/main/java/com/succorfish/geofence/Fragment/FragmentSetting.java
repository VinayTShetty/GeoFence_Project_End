package com.succorfish.geofence.Fragment;

import android.Manifest;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.succorfish.geofence.BaseFragment.BaseFragment;
import com.succorfish.geofence.LoginActivity;
import com.succorfish.geofence.MainActivity;
import com.succorfish.geofence.R;
import com.succorfish.geofence.adapter.FragmentSettingTimeAdapter;
import com.succorfish.geofence.blecalculation.Blecalculation;
import com.succorfish.geofence.customObjects.CustBluetootDevices;
import com.succorfish.geofence.dialog.DialogProvider;
import com.succorfish.geofence.interfaceActivityToFragment.DFUFileSelectedValid_Invalid;
import com.succorfish.geofence.interfaceActivityToFragment.GeoFenceDialogAlertShow;
import com.succorfish.geofence.interfaceFragmentToActivity.PassBuzzerVolumeToDevice;
import com.succorfish.geofence.interfaceFragmentToActivity.ResetDeviceInterface;
import com.succorfish.geofence.interfaces.ResetDeviceDialogCallBack;
import com.succorfish.geofence.interfaces.onAlertDialogCallBack;
import com.succorfish.geofence.utility.Utility;

import static com.succorfish.geofence.MainActivity.CONNECTED_BLE_ADDRESS;
import static com.succorfish.geofence.MainActivity.START_ACTIVITY_REQUEST_CODE;
import static com.succorfish.geofence.blecalculation.ResetDevice.resetDeviceFirmware;
import static com.succorfish.geofence.utility.Utility.ble_on_off;

import java.util.ArrayList;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
public class FragmentSetting extends BaseFragment {
    private final int internalStorageReadPermissionRequestCode = 100;
    View fragmentSettingView;
    private Unbinder unbinder;
    MainActivity mainActivity;
    BottomSheetBehavior bottomSheetfragmentbehaviour;
    private BottomSheetDialog mBottomSheetDialogFragmentSetting;
    @BindView(R.id.beacon_setting_bottom_sheet)
    RelativeLayout relativeLayoutBottomSheet;
    @BindView(R.id.fragment_setting_timeResult)
    TextView batterySelection;
    PassBuzzerVolumeToDevice passBuzzerVolumeToDevice;
    String connected_bleAddress="";
    DialogProvider dialogProvider;
    ResetDeviceInterface resetDeviceInterface;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity=(MainActivity)getActivity();
        interfaceIntialization();
    }

    private void interfaceIntialization(){
        passBuzzerVolumeToDevice = (PassBuzzerVolumeToDevice) getActivity();
        resetDeviceInterface=(ResetDeviceInterface)getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentSettingView=inflater.inflate(R.layout.fragment_setting,container,false);
        unbinder= ButterKnife.bind(this,fragmentSettingView);
        bottomLayoutVisibility(false);
        bottomSheetfragmentbehaviour =BottomSheetBehavior.from(relativeLayoutBottomSheet);
        getConnectedBleAddress();
        intializeDialog();
        checkTimeValueFromPreference();
        geoFenceAlertImplementation();
        return fragmentSettingView;
    }

    private void bottomLayoutVisibility(boolean hide_true_unhide_false){
        mainActivity.hideBottomLayout(hide_true_unhide_false);
    }

    private void getConnectedBleAddress(){
        connected_bleAddress=CONNECTED_BLE_ADDRESS;
     //   connected_bleAddress="EB:A9:F8:DD:D8:52";
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
        unbinder.unbind();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openFilesStorage();
        }else {
            askStoragePermission();
        }
    }

    private void askStoragePermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            readExternalStoragePermisionToast();
        } else {
            externalStorageSetting();
        }
    }

    private void externalStorageSetting() {
        Utility utility = new Utility();
        utility.showPermissionDialog(getActivity(),"Permission Denied for Reading File");
    }

    private void readExternalStoragePermisionToast() {
        Utility utility = new Utility();
        utility.showTaost(getActivity(), "Permission Denied", getResources().getDrawable(R.drawable.ic_location_not_enabled));
    }

    @Override
    public String toString() {
        return FragmentSetting.class.getSimpleName();
    }
    @OnClick(R.id.fragment_setting_BuzzerTimelayout)
    public void clickTimeSelection(){
        openBotttomSheetFragment();
    }
    private void checkTimeValueFromPreference(){
       /**
        * It is used to check the TimeStamp for the particular Ble Address
        */


          String bleAddress=  CONNECTED_BLE_ADDRESS;
          if((bleAddress!=null) &&(bleAddress.length()>0)){
            String savedTimeSTamp=mainActivity.preferenceHelper.getBleBuzzerStamp(bleAddress);
            if(savedTimeSTamp.length()>0){
                batterySelection.setText(savedTimeSTamp+" sec");
            }
          }

    }
    private void openBotttomSheetFragment(){
        if (bottomSheetfragmentbehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetfragmentbehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        mBottomSheetDialogFragmentSetting = new BottomSheetDialog(mainActivity);
        View view = getLayoutInflater().inflate(R.layout.dialog_bottomsheet, null);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.dialog_bottomsheet_recyclerView);
        TextView mTextViewTitle = (TextView) view.findViewById(R.id.dialog_bottomsheet_textview_title);
        mTextViewTitle.setText(R.string.fragment_setting_dialog_select);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(mainActivity));
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mainActivity, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        ArrayList<String> timeSlectionList = new ArrayList<>();
        timeSlectionList.add("1");
        timeSlectionList.add("2");
        timeSlectionList.add("3");
        timeSlectionList.add("4");
        timeSlectionList.add("5");
        timeSlectionList.add("6");
        timeSlectionList.add("7");
        timeSlectionList.add("8");
        timeSlectionList.add("9");
        timeSlectionList.add("10");
        FragmentSettingTimeAdapter fragmentSettingTimeAdapter =new FragmentSettingTimeAdapter(timeSlectionList);
        recyclerView.setAdapter(fragmentSettingTimeAdapter);
        mBottomSheetDialogFragmentSetting.setContentView(view);
        mBottomSheetDialogFragmentSetting.show();
        mBottomSheetDialogFragmentSetting.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mBottomSheetDialogFragmentSetting = null;
            }
        });
        fragmentSettingTimeAdapter.setTimeSlectionItemClick(new FragmentSettingTimeAdapter.TimeSelectionInterface() {
            @Override
            public void timeSelection(String batteryVolts) {
                if(batterySelection!=null){
                    batterySelection.setText(batteryVolts);
                }
                if(mBottomSheetDialogFragmentSetting!=null){
                    mBottomSheetDialogFragmentSetting.cancel();
                    bottomSheetfragmentbehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                mainActivity.preferenceHelper.setBleBuzzerStamp(connected_bleAddress,batteryVolts);
                if(passBuzzerVolumeToDevice!=null){
                    passBuzzerVolumeToDevice.timeIntervalGiven(connected_bleAddress,batteryVolts);
                }
            }
        });
    }
    @OnClick(R.id.fragment_history_back)
    public void settingbackPress(){
        mainActivity.onBackPressed();
    }

    private void intializeDialog() {
        dialogProvider = new DialogProvider(getActivity());
    }
    private void geoFenceAlertImplementation() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.mainActivity_container);
        mainActivity.setUpGeoFenceAlertDialogInterface(new GeoFenceDialogAlertShow() {
            @Override
            public void showDialogInterface(String ruleVioation, String bleAddress, String message_one, String messageTwo, String timeStamp) {
                if (isAdded() && isVisible() && fragment instanceof FragmentSetting) {
                    dialogProvider.showGeofenceAlertDialog(ruleVioation, bleAddress, message_one, messageTwo, 3, false, new onAlertDialogCallBack() {
                        @Override
                        public void PositiveMethod(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            /**
                             * Open map Fragment.
                             */
                            mainActivity.replaceFragment(new FragmentMap(), timeStamp, new FragmentMap().toString(), false);
                        }

                        @Override
                        public void NegativeMethod(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                } else {
                }
            }
        });
        mainActivity.setUpDFUFileSelectedValid_Invalid(new DFUFileSelectedValid_Invalid() {
            @Override
            public void SelecetedFileForDFU(boolean selectedFileType_true_false, Uri selectedFileURI, String filepath, String fileExtensionType) {
                if(selectedFileType_true_false){
                    /**
                     * Pop up Dialog For OTA Update..
                     */
                    OTA_updateConferm_YES_NO(selectedFileURI,filepath,fileExtensionType);
                }
            }
        });


    }
    @OnClick(R.id.device_configuration)
    public void showDeviceConfiguratoinScreen(){
        mainActivity.replaceFragment(new FragmentDeviceConfiguration(), null, null, false);
    }
    @OnClick(R.id.sim_configuration)
    public void showSimCOnfigurationScreen(){
        makePreferrenceValueDefault();
        mainActivity.replaceFragment(new FragmentSimConfiguration(), null, null, false);
    }

    private void makePreferrenceValueDefault() {
        mainActivity.preferenceHelper.setConfigutation_BANDCONFIG(connected_bleAddress+" "+FragmentBandConfiguration.class.getSimpleName(),"");
        mainActivity.preferenceHelper.setConfigutation_UART(connected_bleAddress+" "+FragmentUARTConfiguration.class.getSimpleName(),"");
        mainActivity.preferenceHelper.setSimSelected(connected_bleAddress+" "+getResources().getString(R.string.pre_ESIM),false);
        mainActivity.preferenceHelper.setSimSelected(connected_bleAddress+" "+getResources().getString(R.string.pre_NANO),false);
    }

    @OnClick(R.id.server_configuration)
    public void setServerConfiguration(){
        mainActivity.replaceFragment(new FragmentServerConfiguration(),null , null, false);
    }
    @OnClick(R.id.industry_specific_configuration)
    public void industryDeviceConfiguration(){
        mainActivity.replaceFragment(new FragmentIndustrySpecificConfig(),null , null, false);
    }
    @OnClick(R.id.wifi_configuration)
    public void wifiConfiguration(){
        mainActivity.replaceFragment(new FragmentWifiConfiguration(),null , null, false);
    }

    @OnClick(R.id.ota_update)
    public void otaUpdateClick(){
        checkPermissionGiven();
    }

    private void checkPermissionGiven() {
        if (isAdded()) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                /**
                 * opening of the file logic here
                 */
                openFilesStorage();
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, internalStorageReadPermissionRequestCode);
            }
        }
    }

    @OnClick(R.id.reset_device)
    public void resetDevice(){
        dialogProvider.resetDeviceDialog(new ResetDeviceDialogCallBack() {
            @Override
            public void PositiveMethod(DialogInterface dialog, int id) {
                if(resetDeviceInterface!=null){
                    resetDeviceInterface.resetDevicePacketSend(connected_bleAddress, resetDeviceFirmware());
                }
            }

            @Override
            public void NegativeMethod(DialogInterface dialog, int id) {
                System.out.println("NO");
            }
        });
    }

    private void openFilesStorage(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        getActivity().startActivityForResult(intent, START_ACTIVITY_REQUEST_CODE);
    }


    private void OTA_updateConferm_YES_NO(Uri selectedFileURI,String filepath,String fileExtensionType){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
        builder.setTitle("OTA update ");
        builder.setCancelable(false);
        builder.setMessage("Are you sure ?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                /**
                 * Continue OTA update
                 */
                if(ble_on_off()){
                    mainActivity.startDFUUpdate(selectedFileURI,filepath,connected_bleAddress);
                    dialog.dismiss();
                }

            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }


}
