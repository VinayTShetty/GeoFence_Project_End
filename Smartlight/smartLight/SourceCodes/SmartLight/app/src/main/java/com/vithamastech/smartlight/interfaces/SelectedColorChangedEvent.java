package com.vithamastech.smartlight.interfaces;

import androidx.annotation.ColorInt;

/**
 * Represents a newly-selected color; used with {@link org.greenrobot.eventbus.EventBus} for
 * internal communication.
 */
public class SelectedColorChangedEvent {

    private @ColorInt int mSelectedColor;

    public SelectedColorChangedEvent(@ColorInt int color) {
        mSelectedColor = color;
    }

    public @ColorInt int getSelectedColor() {
        return mSelectedColor;
    }
}
