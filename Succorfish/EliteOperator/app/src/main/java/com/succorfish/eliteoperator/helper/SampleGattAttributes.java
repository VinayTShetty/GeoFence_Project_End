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

package com.succorfish.eliteoperator.helper;

import java.util.HashMap;
import java.util.UUID;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();

    public static String ELITE_START_STOP_SYNC_CHAR = "0000AB03-0100-0800-0008-05F9B34FB000";
    public static String ELITE_SYNC_CONTACT_CHAR = "0000AB02-0100-0800-0008-05F9B34FB000";
    public static String ELITE_SYNC_SURFACE_MSG_CHAR = "0000AB01-0100-0800-0008-05F9B34FB000";
    public static String ELITE_SYNC_DIVER_MSG_CHAR = "0000ab04-0100-0800-0008-05F9B34FB000";
    public static String ELITE_SEND_MSG_CHAR = "0000AB03-0100-0800-0008-05F9B34FB000";

    public static String SERVICE_ELITE_SERVICE = "0000AB00-0100-0800-0008-05F9B34FB000";

    public static String Battery_Service_UUID = "0000180F-0000-1000-8000-00805f9b34fb";
    public static String Battery_Level_UUID = "00002a19-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    static {
        // Sample Services.
        attributes.put(SERVICE_ELITE_SERVICE, "Elite Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        attributes.put(Battery_Service_UUID, "Battery Service");
        // Sample Characteristics.
        attributes.put(ELITE_START_STOP_SYNC_CHAR, "Elite Start Stop");
        attributes.put(ELITE_SYNC_CONTACT_CHAR, "Elite Sync Contact");
        attributes.put(ELITE_SYNC_SURFACE_MSG_CHAR, "Elite Surface Message");
        attributes.put(ELITE_SYNC_DIVER_MSG_CHAR, "Elite Diver Message");
        attributes.put(ELITE_SEND_MSG_CHAR, "Elite Send Message");
        attributes.put(Battery_Level_UUID, "Battery Level");

        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
