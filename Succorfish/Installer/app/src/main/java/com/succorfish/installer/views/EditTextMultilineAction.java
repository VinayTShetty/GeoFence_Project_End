package com.succorfish.installer.views;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class EditTextMultilineAction extends AppCompatEditText {

    public EditTextMultilineAction(Context context) {
        super(context);
    }

    public EditTextMultilineAction(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextMultilineAction(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection conn = super.onCreateInputConnection(outAttrs);
        outAttrs.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;
        return conn;
    }

}
