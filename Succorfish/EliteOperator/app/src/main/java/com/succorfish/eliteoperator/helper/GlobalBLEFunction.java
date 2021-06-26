package com.succorfish.eliteoperator.helper;

/**
 * Created by Jaydeep on 11-01-2018.
 */

public class GlobalBLEFunction {
//    private final static String TAG = MainActivity.class.getSimpleName();
//    static int seq_no = 1;
//    static int self_dev_id = 0x9000;
//    static int dev_all_group_id = 0x0000;
//
//    /* Message Format
//     * [1]   [2-3]   [4-5]   [6-7]    [8-9]  [10-25]
//     * TTL   SEQ NO  DEV ID  DEST ID  CRC    MESH MSG
//    * */
//    public static void TransmitMessageOverMesh(byte ttl, byte[] dest_id, byte val[], byte val_len, boolean isAllGroupDevice) {
//        try {
//            byte msg[] = new byte[10 + val_len];
//            byte msg_index = 0, index;
//            int crc_val = 0;
//
//            if (val_len > 15 || val_len == 0) {
//                return;
//            }
//        /* Initialize the array with zeros */
//            for (index = 0; index < msg.length; index++) {
//                msg[index] = 0;
//            }
//
//        /* Add ttl value */
//            msg[msg_index++] = ttl;
//
//        /* Add Seq No */
//            System.out.println("seq_no-" + seq_no);
//            msg[msg_index++] = (byte) ((seq_no) & 0x00FF);
//            msg[msg_index++] = (byte) ((seq_no >> 8) & 0x00FF);
//
//            seq_no++;
//            if (seq_no == 65535) {
//                seq_no = 1;
//            }
//        /* Add self device id onto msg */
//            msg[msg_index++] = (byte) ((self_dev_id) & 0x00FF);
//            msg[msg_index++] = (byte) ((self_dev_id >> 8) & 0x00FF);
//
//        /* Add dest device id onto msg */
//            if (isAllGroupDevice) {
//                Log.d(TAG, "isFromGroup-> ");
//                msg[msg_index++] = (byte) ((dev_all_group_id) & 0x00FF);
//                msg[msg_index++] = (byte) ((dev_all_group_id >> 8) & 0x00FF);
//            } else {
//                for (index = 0; index < dest_id.length; index++) {
//                    System.out.println("dest_id[" + index + "]--" + dest_id[index]);
//                    msg[msg_index++] = dest_id[index];
//                }
//            }
//        /* Calculate CRC for the message and store it onto the msg */
//            for (index = 0; index < val_len; index++) {
//                crc_val += val[index];
//            }
//            msg[msg_index++] = (byte) ((crc_val) & 0x00FF);
//            msg[msg_index++] = (byte) ((crc_val >> 8) & 0x00FF);
//
//        /* copy the received value onto the msg */
//            for (index = 0; index < val_len; index++) {
//                msg[msg_index++] = val[index];
//            }
//            System.out.println("msg_index-" + msg_index);
//        /* write the message onto the characteristics based on its size */
//            if (msg_index < 20) {
//                if (MainActivity.getIsSDKAbove21()) {
//                    if (AdvertiserService.mBluetoothAdapter == null) {
//                        System.out.println(TAG + "BluetoothAdapter not initialized");
//                        return;
//                    }
//
//                } else {
//                    if (BluetoothLeService.mBluetoothAdapter == null || BluetoothLeService.mBluetoothGatt == null) {
//                        System.out.println(TAG + "BluetoothAdapter not initialized");
//                        return;
//                    }
//                    BluetoothLeService.mContPartChar.setValue(msg);
//                    BluetoothLeService.mBluetoothGatt.writeCharacteristic(BluetoothLeService.mContPartChar);
//                }
//            } else {
//
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /* Add Device (Association)Message Format
//         * [1]   [2-3]   [4-5]   [6-7]    [8-9]  [10-11] [12-17]    [18-19]
//         * TTL   SEQ NO  DEV ID  DEST ID  CRC    OPCODE  BLE ADD    UID(RandomDigit)
//        * */
//    public static void addDeviceOverMesh(byte[] dev_ttl, byte[] dest_id, byte[] ble_add, byte[] randomDigit) {
//        byte msg_index = 0, index;
//
//        byte add_value[] = new byte[10];
//        short opcode = 0x0031;
//        // Add Opcode
//        add_value[0] = (byte) (opcode & 0x00FF);
//        msg_index++;
//        add_value[1] = (byte) ((opcode >> 8) & 0x00FF);
//        msg_index++;
//
//        // Add Device Address
//        for (index = 0; index < ble_add.length; index++) {
//            add_value[msg_index++] = ble_add[index];
//        }
//        add_value[2] = ble_add[1];
//        add_value[3] = ble_add[0];
//        add_value[4] = ble_add[3];
//        add_value[5] = ble_add[2];
//        add_value[6] = ble_add[5];
//        add_value[7] = ble_add[4];
//        System.out.println(TAG + " add_value[2]-" + add_value[2]);
//        System.out.println(TAG + " add_value[3]-" + add_value[3]);
//        System.out.println(TAG + " add_value[4]-" + add_value[4]);
//        System.out.println(TAG + " add_value[5]-" + add_value[5]);
//        System.out.println(TAG + " add_value[6]-" + add_value[6]);
//        System.out.println(TAG + " add_value[7]-" + add_value[7]);
//
////        add_value[8] = (byte) ((randomDigit) & 0x00FF);
////        add_value[9] = (byte) ((randomDigit >> 8) & 0x00FF);
//        for (index = 0; index < randomDigit.length; index++) {
//            add_value[msg_index++] = randomDigit[index];
//        }
//        System.out.println(TAG + " add_value[8]-" + add_value[8]);
//        System.out.println(TAG + " add_value[9]-" + add_value[9]);
//
//        TransmitMessageOverMesh(dev_ttl[0], dest_id, add_value, (byte) 10, false);
//    }
//
//    /* Add Device TO Group Message Format
//             * [1]   [2-3]   [4-5]   [6-7]    [8-9]  [10-11]   [12]    [13-14]
//             * TTL   SEQ NO  DEV ID  DEST ID  CRC    OPCODE  Static byte    UID(RandomDigit)
//            * */
//    public static void addDeviceToGroup(byte dev_ttl, byte[] dest_id, byte[] groupId) {
//        byte msg_index = 0, index;
//        byte add_value[] = new byte[5];
//        short opcode = 0x0008;
//        // Add Opcode
//        add_value[0] = (byte) (opcode & 0x00FF);
//        msg_index++;
//        add_value[1] = (byte) ((opcode >> 8) & 0x00FF);
//        msg_index++;
//        // Static Ox01
//        short temp = 0x01;
//        add_value[2] = (byte) (temp & 0xFF);
//        msg_index++;
//
//        // Add Group Address
//        for (index = 0; index < groupId.length; index++) {
//            add_value[msg_index++] = groupId[index];
//        }
//        add_value[3] = groupId[0];
//        add_value[4] = groupId[1];
//
//        System.out.println(TAG + " add_value[2]-" + add_value[2]);
//        System.out.println(TAG + " add_value[3]-" + add_value[3]);
//        System.out.println(TAG + " add_value[4]-" + add_value[4]);
//
//        TransmitMessageOverMesh(dev_ttl, dest_id, add_value, (byte) 5, false);
//    }
//
//    /**
//     * Enables or disables notification on a give characteristic.
//     *
//     * @param value write the light value.
//     */
//    public static void setLightLevel(byte dev_ttl, short value, byte[] dest_id) {
//        byte light_value[] = new byte[3];
//        short opcode = 0x0052;
//
//        light_value[0] = (byte) (opcode & 0x00FF);
//        light_value[1] = (byte) ((opcode >> 8) & 0x00FF);
//
//        //light_value[2] = (byte)((value >> 8) & 0x00FF);
//        light_value[2] = (byte) (value & 0x00FF);
//
//        Log.d(TAG, "setLightLevel value 0 is " + light_value[2]);
//
//        TransmitMessageOverMesh(dev_ttl, dest_id, light_value, (byte) 3, false);
//    }
//
//    /**
//     * Enables or disables notification on a give characteristic.
//     *
//     * @param color represents rgb values and brightness gives level.
//     */
//    public static void setLightColor(byte dev_ttl, int color, int brightness, byte[] dest_id, boolean isAllGroupDevice) {
//        short opcode = 0x0042;
//        byte light_value[] = new byte[5];
//        float[] hsv = new float[3];
//        int color_to_send;
//        Color.colorToHSV(color, hsv);
//        hsv[2] = ((float) brightness + 1) / 100.0f;
//        color_to_send = Color.HSVToColor(hsv);
//
//        light_value[0] = (byte) (opcode & 0x00FF);
//        light_value[1] = (byte) ((opcode >> 8) & 0x00FF);
//
//        light_value[2] = (byte) (Color.red(color_to_send) & 0xFF);
//        light_value[3] = (byte) (Color.green(color_to_send) & 0xFF);
//        light_value[4] = (byte) (Color.blue(color_to_send) & 0xFF);
//
//        Log.d(TAG, "value 0 -> " + light_value[0]);
//        Log.d(TAG, "value 1 -> " + light_value[1]);
//        Log.d(TAG, "value 2 -> " + light_value[2]);
//        Log.d(TAG, "value 3 -> " + light_value[3]);
//        Log.d(TAG, "value 4 -> " + light_value[4]);
////        Log.d(TAG, "value 5 -> " + light_value[5]);
//
//        TransmitMessageOverMesh(dev_ttl, dest_id, light_value, (byte) 5, isAllGroupDevice);
//    }
//
//    public static void setLightColorRGB(byte dev_ttl, byte red, byte green, byte blue, byte white, byte[] dest_id, boolean isAllGroupDevice) {
//        short opcode = 0x0042;
//        byte light_value[] = new byte[6];
////        float[] hsv = new float[3];
////        int color_to_send;
////        Color.colorToHSV(color, hsv);
//////        hsv[2] = ((float) brightness + 1) / 100.0f;
////        color_to_send = Color.HSVToColor(hsv);
//
//        light_value[0] = (byte) (opcode & 0x00FF);
//        light_value[1] = (byte) ((opcode >> 8) & 0x00FF);
//        light_value[2] = red;
//        light_value[3] = green;
//        light_value[4] = blue;
//        light_value[5] = white;
//
////        light_value[2] = color[0];
////        light_value[3] = color[1];
////        light_value[4] = color[2];
//
//        Log.d(TAG, "value 0 -> " + light_value[0]);
//        Log.d(TAG, "value 1 -> " + light_value[1]);
//        Log.d(TAG, "value 2 -> " + light_value[2]);
//        Log.d(TAG, "value 3 -> " + light_value[3]);
//        Log.d(TAG, "value 4 -> " + light_value[4]);
//        Log.d(TAG, "value 5 -> " + light_value[5]);
////        Log.d(TAG, "value 5 -> " + light_value[5]);
//
//        TransmitMessageOverMesh(dev_ttl, dest_id, light_value, (byte) 6, isAllGroupDevice);
//    }
//
//    /**
//     * Enables or disables notification on a give characteristic.
//     *
//     * @param OnOff represents device on(1) or off(0).
//     */
//    public static void setLightOnOff(byte dev_ttl, byte OnOff, byte[] dest_id, boolean isFromGroup) {
//        short opcode = 0x0055;
//        byte light_value[] = new byte[3];
//
//        light_value[0] = (byte) (opcode & 0x00FF);
//        light_value[1] = (byte) ((opcode >> 8) & 0x00FF);
//
//        light_value[2] = OnOff;
//
//        Log.d(TAG, "value 0 -> " + light_value[0]);
//        Log.d(TAG, "value 1 -> " + light_value[1]);
//        Log.d(TAG, "value 2 -> " + light_value[2]);
//        TransmitMessageOverMesh(dev_ttl, dest_id, light_value, (byte) 3, isFromGroup);
//    }
//
//    /**
//     * Enables or disables notification on a give characteristic.
//     *
//     * @param paternNO represents device color pattern.
//     */
//    public static void changePatternColor(byte dev_ttl, byte paternNO, byte[] dest_id, boolean isFromGroup) {
//        short opcode = 0x0043;
//        byte light_value[] = new byte[3];
//
//        light_value[0] = (byte) (opcode & 0x00FF);
//        light_value[1] = (byte) ((opcode >> 8) & 0x00FF);
//
//        light_value[2] = paternNO;
//
//        Log.d(TAG, "value 0 -> " + light_value[0]);
//        Log.d(TAG, "value 1 -> " + light_value[1]);
//        Log.d(TAG, "value 2 -> " + light_value[2]);
//        TransmitMessageOverMesh(dev_ttl, dest_id, light_value, (byte) 3, isFromGroup);
//    }
//
//    /**
//     * Enables or disables notification on a give characteristic.
//     *
//     * @param isAllGroupDevice represents deassociate all device if true other wise particular device.
//     */
//    public static void resetAllDevice(byte dev_ttl, byte[] dest_id, boolean isAllGroupDevice) {
//        short opcode = 0x0037;
//        byte light_value[] = new byte[2];
//
//        light_value[0] = (byte) (opcode & 0x00FF);
//        light_value[1] = (byte) ((opcode >> 8) & 0x00FF);
//
//        Log.d(TAG, "value 0 -> " + light_value[0]);
//        Log.d(TAG, "value 1 -> " + light_value[1]);
//
//        TransmitMessageOverMesh(dev_ttl, dest_id, light_value, (byte) 2, isAllGroupDevice);
//    }
//
//    /**
//     * Enables or disables notification on a give characteristic.
//     *
//     * @param isAllGroupDevice represents deassociate all device if true other wise particular device.
//     */
//    public static void deleteAllGroupDevice(byte dev_ttl, byte[] dest_id, byte[] group_id, boolean isAllGroupDevice) {
//        short opcode = 0x0037;
//        byte light_value[] = new byte[4];
//
//        light_value[0] = (byte) (opcode & 0x00FF);
//        light_value[1] = (byte) ((opcode >> 8) & 0x00FF);
//        light_value[2] = group_id[0];
//        light_value[3] = group_id[1];
//        Log.d(TAG, "value 0 -> " + light_value[0]);
//        Log.d(TAG, "value 1 -> " + light_value[1]);
//        Log.d(TAG, "value 2 -> " + light_value[2]);
//        Log.d(TAG, "value 3 -> " + light_value[3]);
//        TransmitMessageOverMesh(dev_ttl, dest_id, light_value, (byte) 4, isAllGroupDevice);
//    }
}
