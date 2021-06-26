package com.vithamastech.smartlight.helper;

import android.graphics.Color;
import androidx.annotation.ColorInt;

/**
 * General-purpose class tor color functions
 */
public final class ColorUtil {
    private ColorUtil() {
        //Util class
    }

    /**
     * Computes if the color is considered "dark"; used to determine if the foreground
     * image (the checkmark) should be white or black.
     * <p/>
     * Based on http://stackoverflow.com/a/24810681/2444312.
     *
     * @return true if the color is "dark"
     */
    public static boolean isColorDark(@ColorInt int color) {
        double brightness = Color.red(color) * 0.299 +
                Color.green(color) * 0.587 +
                Color.blue(color) * 0.114;
        return brightness < 160;
    }

    @ColorInt
    public static int getRippleColor(@ColorInt int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = hsv[2] * 0.4f;
        return Color.HSVToColor(hsv);
    }
    public static boolean isColorOverBlack(@ColorInt int color) {
        double brightness = Color.red(color) * 0.1 +
                Color.green(color) * 0.1 +
                Color.blue(color) * 0.1;
        return brightness < 0.7;
    }
}
