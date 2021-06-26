package com.succorfish.geofence.interfaceFragmentToActivity;

import java.util.ArrayList;

public interface SimConfigurationPackets {
    public void SimConfigurationDetails(String bleAddress, ArrayList<byte[]> simconfigurationList);
}
