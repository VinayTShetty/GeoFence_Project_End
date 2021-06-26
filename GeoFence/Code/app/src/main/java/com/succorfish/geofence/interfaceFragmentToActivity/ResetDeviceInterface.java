package com.succorfish.geofence.interfaceFragmentToActivity;

import java.util.ArrayList;

public interface ResetDeviceInterface {
    public void resetDevicePacketSend(String bleaddress, ArrayList<byte[]> resetpacketList);
}
