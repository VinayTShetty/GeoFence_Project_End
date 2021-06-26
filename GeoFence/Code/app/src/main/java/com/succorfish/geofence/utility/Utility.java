package com.succorfish.geofence.utility;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;


import com.succorfish.geofence.R;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import es.dmoral.toasty.Toasty;

import static com.succorfish.geofence.blecalculation.ByteConversion.bytesToHex;
import static com.succorfish.geofence.blecalculation.MessageCalculation.endMessagePacket;

public class Utility {
    public static boolean ble_on_off(){
        boolean ble_on_off=false;
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            ble_on_off=false;
        } else if (!mBluetoothAdapter.isEnabled()) {
            // Bluetooth is not enabled :)
            ble_on_off=false;
        } else {
            // Bluetooth is enabled
            ble_on_off=true;
        }
        return ble_on_off;
    }
   public void showTaost(Context context, String message, Drawable image){
       Toasty.normal(context, message, image).show();
   }

   public void showPermissionDialog(final Context context,String headerTitle){
       AlertDialog.Builder builder = new AlertDialog.Builder(context);
       builder.setCancelable(false)
               .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                               Uri.fromParts("package", context.getPackageName(), null));
                       intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                       context.startActivity(intent);
                   }
               })
               .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       //  Action for 'NO' Button
                       dialog.cancel();

                   }
               });
       //Creating dialog box
       AlertDialog alert = builder.create();
       //Setting the title manually
       alert.setTitle(headerTitle);
       alert.show();
   }

    public static String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static  String getCurrenTimeStamp(){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return ""+timestamp.getTime();
    }

    public static String removePreviousZero(String input){
        return input.replaceFirst ("^0*", "");
    }

    public static String ConvertminsToHours(int mins){
        String totalDuration="";
        int d=mins/24;
        int m=mins%24;
        if(d>0){
            totalDuration="Days = "+totalDuration+d;
            totalDuration="hours= "+totalDuration+m;
        }else {
            totalDuration=""+m;
        }
        return totalDuration;
    }

    public static String removeTimeStamp(String entireData){
        return entireData.substring(0, entireData.length() - 13);
    }

    public static  boolean checklocationPermissionGiven(){
        boolean permissionGiven=false;
        return permissionGiven;
    }


    public void showProgressDialog(){

    }
    public void cancelProgreessDialog(){

    }

    /**
     *
     *
     * link:-https://www.tutorialspoint.com/get-the-substring-after-the-first-occurrence-of-a-separator-in-java#:~:text=We%20want%20the%20substring%20after%20the%20first%20occurrence%20of%20the%20separator%20i.e.&text=For%20that%2C%20first%20you%20need,indexOf(separator)%3B%20System.
     */
    public static String getID_From_ArrayList(String arraylistItem){
        String separator =":";
        int sepPos = arraylistItem.lastIndexOf(separator);
        if (sepPos == -1) {
            System.out.println("");
        }
        return arraylistItem.substring(0,sepPos);
    }

    /**
     *
     *
     * https://www.tutorialspoint.com/get-the-substring-before-the-last-occurrence-of-a-separator-in-java#:~:text=String%20str%20%3D%20%22David%2DWarner,Use%20the%20lastIndexOf()%20method.&text=String%20separator%20%3D%22%2D%22%3B%20int%20sepPos%20%3D%20str.
     */
    public static String get_TimeStamp_ArrayList(String arrylistItem){
        String separator =":";
        int sepPos = arrylistItem.indexOf(separator);
        if (sepPos == -1) {
            System.out.println("");
        }
        return arrylistItem.substring(sepPos+separator.length());
    }


    public static BluetoothAdapter getBluetoothAdapter()
    {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter==null){
            return null;
        }
        if (!bluetoothAdapter.isEnabled()) {
            return null;
        }
        if(bluetoothAdapter.isEnabled()){
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        return  bluetoothAdapter;
    }

    /**
     *
     * Time Format :-2020-10-27 16:05:54
     */
    public static String getDateWithtime(){
        Date today = new Date();
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateToStr = format.format(today);
//        return  dateToStr.substring(11,16);// only fetching time so
        return  dateToStr;// only fetching time so
    }
    /**
     *
     * timeStamp example:-1603802952
     */
   /* public static String getTimeStamp(){
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        return  ts;
    }*/

    public static String getTimeStampMilliSecondd(){
        Long tsLong = System.currentTimeMillis();
        String ts = tsLong.toString();
        return  ts;
    }

    public static String getTimeStamp4bytesToBle(){
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        return  ts;
    }


    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    /**
     * Used to split the String Equally into 12 parts.
     */
    public static List<String> splitString(String messageText, int splitLength){
        List<String> ret = new ArrayList<String>((messageText.length() + splitLength - 1) / splitLength);
        for (int start = 0; start < messageText.length(); start += splitLength) {
            ret.add(messageText.substring(start, Math.min(messageText.length(), start + splitLength)));
        }
        return ret;
    }
    /**
     * Convert String to bytes.
     */
    public static byte [] ConvertStringToByteArray(String inputString){
        byte[] byteArrray = inputString.getBytes();
        return byteArrray;
    }

    public static boolean checkEditTextDataIsEmpty(EditText editTextInput){
        boolean result=false;
        if((editTextInput.getText().toString().length()>0)&&(!editTextInput.getText().toString().equalsIgnoreCase(""))&&(!editTextInput.getText().toString().matches(""))){
            result=true;
        }
        return result;
    }

 /*   public static  byte convertStringToByte(String covertToByte){
        byte value=00;
        value=Byte.parseByte(covertToByte);
        return value;
    }*/


    /**
     *It is used to convert byteArray to HexString and Store in ArrayList.
     */
    public static ArrayList<String> getHexArrayList(ArrayList<byte []> byteArraylist){
        ArrayList<String> hexArraylist=new ArrayList<String>();
        for (byte [] eachArray: byteArraylist) {
            hexArraylist.add(bytesToHex(eachArray));
        }
        return hexArraylist;
    }

    public static void showTost(Context context,String message_toast){
        Toast.makeText(context,message_toast,Toast.LENGTH_SHORT).show();
    }

    public static byte radioButtonSelectedValue(RadioButton on,RadioButton cellular,RadioButton unchanged){
        byte value= (byte) 0xff;
        if(on.isChecked()){
            value=00;
        }else if(cellular.isChecked()){
            value=01;
        }else if(unchanged.isChecked()) {
            value= (byte) 0xff;
        }
        return value;
    }

    public void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }



}