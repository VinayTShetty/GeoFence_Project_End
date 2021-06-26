package com.vithamastech.smartlight.services;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.RequiresApi;
import android.util.Log;

import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.R;
import com.vithamastech.smartlight.helper.BLEUtility;
import com.vithamastech.smartlight.helper.URLCLASS;

import java.util.concurrent.TimeUnit;

/**
 * Manages BLE Advertising independent of the main app.
 * If the app goes off screen (or gets killed completely) advertising can continue because this
 * Service is maintaining the necessary Callback in memory.
 */
public class AdvertiserService extends Service {
    /**
     * A global variable to let AdvertiserFragment check if the Service is running without needing
     * to start or bind to it.
     * This is the best practice method as defined here:
     * https://groups.google.com/forum/#!topic/android-developers/jEvXMWgbgzE
     */

    private static final String TAG = AdvertiserService.class.getSimpleName();
    public BluetoothManager mBluetoothManager;
    public BluetoothAdapter mBluetoothAdapter;
    private static final int FOREGROUND_NOTIFICATION_ID = 1;

    public static boolean running = false;
    public static final String ADVERTISING_FAILED =
            "com.vithamastech.smartlight.advertising_failed";
    public static final String ADVERTISING_FAILED_EXTRA_CODE = "failureCode";
    public static final int ADVERTISING_TIMED_OUT = 6;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private AdvertiseCallback mAdvertiseCallback;
    private Handler mHandler;
    private Runnable timeoutRunnable;
    /**
     * Length of time to allow advertising before automatically shutting off. (10 minutes)
     */
    private long TIME_OUT_MANUAL = TimeUnit.MILLISECONDS.convert(URLCLASS.ADVERTISE_MANUAL_TIMEOUT, TimeUnit.MILLISECONDS);
    private int TIME_OUT_AUTO = Integer.parseInt(URLCLASS.ADVERTISE_MANUAL_TIMEOUT + "") + 10;
    private final IBinder mBinder = new LocalBinder();

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate() {
        running = true;
//        initialize();
        super.onCreate();
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onDestroy() {
        /**
         * Note that onDestroy is not guaranteed to be called quickly or at all. Services exist at
         * the whim of the system, and onDestroy can be delayed or skipped entirely if memory need
         * is critical.
         */

        stopAdvertising();
        super.onDestroy();
    }

        public void setAdvertismentTimeOut(long timeOut) {
        TIME_OUT_MANUAL = TimeUnit.MILLISECONDS.convert(timeOut, TimeUnit.MILLISECONDS);
        TIME_OUT_AUTO = Integer.parseInt(timeOut + "") + 10;
    }

    public class LocalBinder extends Binder {
        public AdvertiserService getService() {
            return AdvertiserService.this;
        }
    }

    /**
     * Required for extending service, but this will be a Started Service only, so no need for
     * binding.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Get references to system Bluetooth objects if we don't have them already.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean initialize() {
        System.out.println(TAG + "Preparing Service Initialize");
        if (mBluetoothLeAdvertiser == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager != null) {
                mBluetoothAdapter = mBluetoothManager.getAdapter();
                if (mBluetoothAdapter != null) {
                    mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
                    System.out.println(TAG + "Preparing Service Started");
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        return super.onUnbind(intent);
    }

    /**
     * Starts a delayed Runnable that will cause the BLE Advertising to timeout and stop after a
     * set amount of time.
     */
    private void setTimeout() {
        mHandler = new Handler(Looper.getMainLooper());
        timeoutRunnable = new Runnable() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
//                Log.d(TAG, "AdvertiserService has reached timeout of " + TIMEOUTMANUAL + " milliseconds, stopping advertising.");
                stopAdvertising();
                stopSelf();
            }
        };
        mHandler.postDelayed(timeoutRunnable, TIME_OUT_MANUAL);
    }

    /* Transmit All Message Packet over Advertisement*/
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void TransmitMessageOverMesh(byte[] allPackets) {
//        System.out.println("OVER ADV.");
        if (mBluetoothAdapter == null) {
            System.out.println("BluetoothAdapter not initialized");
            return;
        }
        if (!BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported()) {
            stopSelf();
            System.out.println("Not Supported");
            return;
        }
        try {
            System.out.println(TAG + "SEND Length-" + allPackets.length + "-PACKET-" + BLEUtility.toHexString(allPackets, true));
            /* write the message onto the characteristics based on its size */
            if (allPackets.length <= 20) {
                startAdvertising(allPackets);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts BLE Advertising Packet.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startAdvertising(byte[] mByteManufactureData) {
//        goForeground();
        stopAdvertising();
//        Log.d(TAG, "Service: Starting Advertising");
        if (mAdvertiseCallback == null) {
            AdvertiseSettings settings = buildAdvertiseSettings();
            AdvertiseData data = buildAdvertiseData(mByteManufactureData);
            mAdvertiseCallback = new SampleAdvertiseCallback();
            if (mBluetoothLeAdvertiser != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mBluetoothLeAdvertiser.startAdvertising(settings, data,
                            mAdvertiseCallback);
                    setTimeout();
                }
            }
        }
    }

    /**
     * Stops BLE Advertising Packet.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void stopAdvertising() {
//        Log.d(TAG, "Service: Stopping Advertising");
        try {
            if (mBluetoothLeAdvertiser != null) {
                if (mAdvertiseCallback != null) {
//                    Log.d(TAG, "Service: Stopped Advertising");
                    mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
                    stopForeground(true);
                    mAdvertiseCallback = null;
                }
            }
            running = false;
            if (mHandler != null) {
                if (timeoutRunnable != null) {
//                    Log.d(TAG, "Service: RemoveHandler Advertising");
                    mHandler.removeCallbacks(timeoutRunnable);
                }
            }
//            Log.d(TAG, "Service: Advertising Stopped");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Move service to the foreground, to avoid execution limits on background processes.
     * <p>
     * Callers should call stopForeground(true) when background work is complete.
     */
    private void goForeground() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        Notification n = new Notification.Builder(this)
                .setContentTitle("Advertising device via Bluetooth")
                .setContentText("This device is discoverable to others nearby.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(FOREGROUND_NOTIFICATION_ID, n);
    }

    /**
     * Returns an AdvertiseData object which includes the Service UUID and Device Name and data.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private AdvertiseData buildAdvertiseData(byte[] customManufactureData) {
        /**
         * Note: There is a strict limit of 31 Bytes on packets sent over BLE Advertisements.
         *  This includes everything put into AdvertiseData including UUIDs, device info, &
         *  arbitrary service or manufacturer data.
         *  Attempting to send packets over this limit will result in a failure with error code
         *  AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE. Catch this error in the
         *  onStartFailure() method of an AdvertiseCallback implementation.
         */
        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
//        ParcelUuid Service_UUID = ParcelUuid
//                .fromString(BLEUtility.mUUIDColorWhiteBrightness);
        //        dataBuilder.addServiceUuid(Service_UUID);
        dataBuilder.addManufacturerData(10, customManufactureData);
        dataBuilder.setIncludeDeviceName(false);
        return dataBuilder.build();
    }

    /**
     * Returns an AdvertiseSettings object set to use low power (to help preserve battery life)
     * and disable the built-in timeout since this code uses its own timeout runnable.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private AdvertiseSettings buildAdvertiseSettings() {
        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        settingsBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        settingsBuilder.setTimeout(TIME_OUT_AUTO);
        settingsBuilder.setConnectable(false);
        return settingsBuilder.build();
    }

    /**
     * Custom callback after Advertising succeeds or fails to start. Broadcasts the error code
     * in an Intent to be picked up by AdvertiserFragment and stops this Service.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private class SampleAdvertiseCallback extends AdvertiseCallback {
        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.d(TAG, "Advertising failed !!!" + errorCode);
            sendFailureIntent(errorCode);
//            stopSelf();
//            startAdvertising(mAdvertisManufactureDataTemp);
        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
//            Log.d(TAG, "Service: Advertising successfully started");
        }
    }

    /**
     * Builds and sends a broadcast intent indicating Advertising has failed. Includes the error
     * code as an extra. This is intended to be picked up by the {@code AdvertiserFragment}.
     */
    private void sendFailureIntent(int errorCode) {
        System.out.println("Advertising Failed-" + errorCode);
        Intent failureIntent = new Intent();
        failureIntent.setAction(ADVERTISING_FAILED);
        failureIntent.putExtra(ADVERTISING_FAILED_EXTRA_CODE, errorCode);
        sendBroadcast(failureIntent);
    }
}
