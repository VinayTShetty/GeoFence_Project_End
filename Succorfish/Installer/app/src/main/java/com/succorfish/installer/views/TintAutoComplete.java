package com.succorfish.installer.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.TintTypedArray;
import android.util.AttributeSet;

/**
 * Created by Jaydeep on 28-02-2018.
 */

public class TintAutoComplete extends android.support.v7.widget.AppCompatAutoCompleteTextView {

    private static final int[] TINT_ATTRS = {
            android.R.attr.background
    };

    public TintAutoComplete(Context context) {
        this(context, null);
    }

    public TintAutoComplete(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    @SuppressLint("RestrictedApi")
    public TintAutoComplete(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, attrs, TINT_ATTRS,
                defStyleAttr, 0);
        setBackgroundDrawable(a.getDrawable(0));
        a.recycle();
    }
}
