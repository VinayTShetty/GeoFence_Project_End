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

package com.succorfish.installer.helper;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String SMART_MESH_FIRST_CHAR = "0001d100-ab00-11e1-9b23-00025b00a5a5";
    public static String SMART_MESH_CONT_CHAR = "0002d100-ab00-11e1-9b23-00025b00a5a5";

//    public static String SMART_MESH_COMMAND_CHAR = "0003d100-ab00-11e1-9b23-00025b00a5a5";
//    public static String SMART_MESH_DATA_CHAR = "0004d100-ab00-11e1-9b23-00025b00a5a5";

    //    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String SERVICE_SMART_MESH_SERVICE = "0000d100-ab00-11e1-9b23-00025b00a5a5";

    static {
        // Sample Services.
        attributes.put(SERVICE_SMART_MESH_SERVICE, "Smart Mesh Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Sample Characteristics.
        attributes.put(SMART_MESH_FIRST_CHAR, "Smart Mesh First Part");
        attributes.put(SMART_MESH_CONT_CHAR, "Smart Mesh Cont Part");
//        attributes.put(SMART_MESH_COMMAND_CHAR, "Smart Mesh Command");
//        attributes.put(SMART_MESH_DATA_CHAR, "Smart Mesh Data");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
