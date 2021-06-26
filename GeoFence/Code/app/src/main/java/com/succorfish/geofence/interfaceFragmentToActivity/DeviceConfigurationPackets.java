package com.succorfish.geofence.interfaceFragmentToActivity;

import java.util.ArrayList;

public interface DeviceConfigurationPackets {
    public void deviceConfigurationDetails(String bleAddress, ArrayList<byte[]> configurationList);
}
