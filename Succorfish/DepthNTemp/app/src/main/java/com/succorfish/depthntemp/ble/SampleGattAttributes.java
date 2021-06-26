/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.succorfish.depthntemp.ble;

import java.util.HashMap;
import java.util.UUID;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String DNT_GET_DATA_CHAR = "0000ad01-d102-11e1-9b23-00025b002b2b";
    public static String DNT_SET_SETTING_COMMAND_CHAR = "0000ad03-d102-11e1-9b23-00025b002b2b";
    public static String DNT_GET_SETTING_DATA_CHAR = "0000ad04-d102-11e1-9b23-00025b002b2b";

//    public static String DNT_START_STOP = "0000ad03-d102-11e1-9b23-00025b002b2b";
//    public static String DNT_PRES_DATA = "0000ad02-d102-11e1-9b23-00025b002b2b";
//    public static String DNT_STAT_PRES_INT = "0000ad04-d102-11e1-9b23-00025b002b2b";
//    public static String DNT_MOV_PRES_INT = "0000ad05-d102-11e1-9b23-00025b002b2b";
//    public static String DNT_ERASE_PRES_DATA = "0000ad06-d102-11e1-9b23-00025b002b2b";
//    public static String DNT_GPS_TRACK_INT = "0000ad07-d102-11e1-9b23-00025b002b2b";
//    public static String DNT_BLE_CUTOFF = "0000ad08-d102-11e1-9b23-00025b002b2b";
//    public static String EXPORT_MODE = "0000ad0a-d102-11e1-9b23-00025b002b2b";
//    public static String UTC_TIME = "0000ad0b-d102-11e1-9b23-00025b002b2b";
//    public static String BATTERY_LEVEL = "00002a19-0000-1000-8000-00805f9b34fb";

    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    // Services
    public static String APP_GET_DATA_SERVICE = "0000ad00-d102-11e1-9b23-00025b00a5a5";
    //    public static String APP_SERVICE = "0000ad01-e102-11e1-9b23-00025b00a5a5";
    public static String MANUFACTURE_SERVICE = "00002a29-0000-1000-8000-00805f9b34fb";
    public static String DEVICE_INFORMATION_SERVICE = "0000180a-0000-1000-8000-00805f9b34fb";
    public static String BATTERY_SERVICE = "0000180f-0000-1000-8000-00805f9b34fb";

//    public final static UUID UUID_DNT_PRES_DATA =
//            UUID.fromString(com.succorfish.DepthNTemp.ble.SampleGattAttributes.DNT_PRES_DATA);

    public final static UUID UUID_DNT_GET_DATA =
            UUID.fromString(SampleGattAttributes.DNT_GET_DATA_CHAR);
    public final static UUID UUID_DNT_SET_SETTING_COMMAND_DATA =
            UUID.fromString(SampleGattAttributes.DNT_SET_SETTING_COMMAND_CHAR);
    public final static UUID UUID_DNT_GET_SETTING_DATA =
            UUID.fromString(SampleGattAttributes.DNT_GET_SETTING_DATA_CHAR);

    //    public final static UUID UUID_DNT_START_STOP =
//            UUID.fromString(SampleGattAttributes.DNT_START_STOP);
//
//    public final static UUID UUID_DNT_STAT_PRES_INT =
//            UUID.fromString(SampleGattAttributes.DNT_STAT_PRES_INT);
//
//    public final static UUID UUID_DNT_MOV_PRES_INT =
//            UUID.fromString(SampleGattAttributes.DNT_MOV_PRES_INT);
//
//    public final static UUID UUID_DNT_ERASE_PRES_DATA =
//            UUID.fromString(SampleGattAttributes.DNT_ERASE_PRES_DATA);
//
//    public final static UUID UUID_DNT_GPS_TRACK_INT =
//            UUID.fromString(SampleGattAttributes.DNT_GPS_TRACK_INT);
//
//    public final static UUID UUID_DNT_BLE_CUTOFF =
//            UUID.fromString(SampleGattAttributes.DNT_BLE_CUTOFF);
//
//    public final static UUID UUID_EXPORT_MODE =
//            UUID.fromString(SampleGattAttributes.EXPORT_MODE);
//
//    public final static UUID UUID_UTC_TIME =
//            UUID.fromString(SampleGattAttributes.UTC_TIME);
//
//    public final static UUID UUID_BATTERY_LEVEL =
//            UUID.fromString(SampleGattAttributes.BATTERY_LEVEL);
    static {
        // Sample Services.
//        attributes.put(APP_SERVICE, "DNT Service");
        attributes.put(APP_GET_DATA_SERVICE, "DNT GET DATA Service");
        attributes.put(DEVICE_INFORMATION_SERVICE, "Device Information Service");
        attributes.put(BATTERY_SERVICE, "Battery Service");
        attributes.put(MANUFACTURE_SERVICE, "Manufacturer Name String");
        // Sample Characteristics.
        attributes.put(DNT_GET_DATA_CHAR, "DNT get data Characteristics");
        attributes.put(DNT_SET_SETTING_COMMAND_CHAR, "DNT Setting command Characteristics");
        attributes.put(DNT_GET_SETTING_DATA_CHAR, "DNT Setting data Characteristics");
//        attributes.put(DNT_START_STOP, "DNT Start Stop Measurements");
//        attributes.put(DNT_PRES_DATA, "DNT Temperature Pressure Data");
//        attributes.put(DNT_STAT_PRES_INT, "DNT Stationary Pressure Measurement Interval");
//        attributes.put(DNT_MOV_PRES_INT, "DNT Moving Pressure Measurement Interval");
//        attributes.put(DNT_ERASE_PRES_DATA, "DNT Erase Pressure Data");
//        attributes.put(DNT_GPS_TRACK_INT, "DNT GPS Tracking Interval");
//        attributes.put(DNT_BLE_CUTOFF, "DNT GPS BLE Cutoff Reading");
//        attributes.put(EXPORT_MODE, "Export Mode");
//        attributes.put(UTC_TIME, "UTC Time");
//        attributes.put(BATTERY_LEVEL, "Battery Level");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
