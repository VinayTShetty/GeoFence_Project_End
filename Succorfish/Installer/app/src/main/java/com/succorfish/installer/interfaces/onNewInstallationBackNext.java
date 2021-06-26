package com.succorfish.installer.interfaces;

import android.support.v4.app.Fragment;

/**
 * Created by Jaydeep on 23-02-2018.
 */

public interface onNewInstallationBackNext {
    public void onInstallFirstBack(Fragment fragment);

    public void onInstallFirstNext(Fragment fragment);

    public void onInstallSecondNext(Fragment fragment);

    public void onInstallSecondBack(Fragment fragment);

    public void onInstallThirdBack(Fragment fragment);

    public void onInstallThirdComplete(Fragment fragment);
}
