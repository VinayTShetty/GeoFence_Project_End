package com.succorfish.installer.interfaces;

import com.succorfish.installer.Vo.VoGetDeviceInfo;
import com.succorfish.installer.Vo.VoServerInstallation;

/**
 * Created by Jaydeep on 17-03-2018.
 */

public interface onBackWithInstallationData {

    public void onBackWithInstallData(VoServerInstallation mVoServerInstallation);

    public void onBackWithInstallData(VoGetDeviceInfo mVoGetDeviceInfo);

    public void onBackWithInstallData(String mStringimei);
}
