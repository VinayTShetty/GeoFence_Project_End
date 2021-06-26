package com.vithamastech.smartlight.DialogFragementHelper;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.vithamastech.smartlight.PowerSocketCustomObjects.WifiDevice;
import com.vithamastech.smartlight.R;

public class EditTextDialog extends DialogFragment {
    private DialogListener dialogListener;
    public static String messageKey = "message";
    public static String positiveButtonNameKey = "positiveButtonName";
    public static String negativeButtonNameKey = "negativeButtonName";
    public static String textHintKey = "textHintKey";

    public EditTextDialog() {
        super();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_name_edit_dialog, container, false);
        // Set transparent background and no title
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button buttonNegative = view.findViewById(R.id.buttonNegative);
        Button buttonPositive = view.findViewById(R.id.buttonPositive);
        TextView textViewMessage = view.findViewById(R.id.textViewMessage);
        EditText editTextPowerSocketName = view.findViewById(R.id.editTextPowerSocketName);
        String message = "";
        String positiveButtonName = "";
        String negativeButtonName = "";
        String textHint = "";

        if (getArguments() != null) {
            message = getArguments().getString(messageKey);
            positiveButtonName = getArguments().getString(positiveButtonNameKey);
            negativeButtonName = getArguments().getString(negativeButtonNameKey);
            textHint = getArguments().getString(textHintKey);
        }

        textViewMessage.setText(message);
        buttonPositive.setText(positiveButtonName);

        editTextPowerSocketName.setHint(textHint);

        if (negativeButtonName == null || negativeButtonName.isEmpty()) {
            buttonNegative.setVisibility(View.GONE);
        } else {
            buttonNegative.setVisibility(View.VISIBLE);
        }

        if (positiveButtonName == null || positiveButtonName.isEmpty()) {
            buttonPositive.setVisibility(View.GONE);
        } else {
            buttonPositive.setVisibility(View.VISIBLE);
        }

        buttonNegative.setOnClickListener(view1 -> {
            if (dialogListener != null) {
                dialogListener.onCancelEditDialog();
            }
            dismiss();
        });

        buttonPositive.setOnClickListener(v -> {
            String powerSocketName = editTextPowerSocketName.getText().toString().trim();
            if (dialogListener != null) {
                dialogListener.onFinishEditDialog(powerSocketName, false);
            }
            dismiss();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_AppCompat_Light_Dialog_Alert);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppTheme_Dialog_Custom);
        setCancelable(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public interface DialogListener {
        void onFinishEditDialog(String powerSocketName, boolean dialogCancelFlag);

        void onCancelEditDialog();
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
