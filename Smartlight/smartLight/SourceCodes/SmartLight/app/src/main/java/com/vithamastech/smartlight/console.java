package com.vithamastech.smartlight;

/**
 * Created by muataz medini on 8/13/2016.
 * <p/>
 * principle -----> Here an exception is thrown. Then from that exception e, we extract the class name and its function
 * where log is called.
 */
public class console {

    // A controller that can be switched off before building signed apk and releasing it to google play store
    private static final boolean BuildConfig_Debug = true;
    public static final boolean Log_Credentials = false;

    // for logging string data
    public static void log(String LOGTAG, Object object) {
        if (BuildConfig_Debug) {
            try {
                throw new Exception("Exception in logging data");
            } catch (Exception e) {
                String className = e.getStackTrace()[1].getClassName();
                className = className.substring(className.lastIndexOf(".") + 1, className.length());

                android.util.Log.v(LOGTAG, className + "." + e.getStackTrace()[1].getMethodName() + "() ---> " + object);
            }
        }
    }
}

