package com.vithamastech.smartlight.DialogFragementHelper;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.vithamastech.smartlight.PowerSocketCustomObjects.WifiDevice;
import com.vithamastech.smartlight.R;
import com.vithamastech.smartlight.Views.CustomEditText;
import com.vithamastech.smartlight.interfaces.DrawableClickListener;

public class WifiConfigAlertDialog extends DialogFragment {
    private DialogListener dialogListener;
    private boolean isPasswordVisible;

    public static final String WifiDeviceLabel = "WifiDevice";
    private WifiDevice wifiDevice;

    public WifiConfigAlertDialog() {
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
        View view = inflater.inflate(R.layout.fragment_wifi_config_dialog, container, false);
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
        EditText editTextWifiSSID = view.findViewById(R.id.editTextWifiSSID);
        CustomEditText editTextWifiPassword = view.findViewById(R.id.editTextWifiPassword);

        if (getArguments() != null) {
            wifiDevice = (WifiDevice) getArguments().getSerializable(WifiDeviceLabel);
            if (wifiDevice != null) {
                String wifiSSID = "Unknown device";
                if (wifiDevice.wifiSSID != null && !wifiDevice.wifiSSID.isEmpty()) {
                    wifiSSID = wifiDevice.wifiSSID;
                }
                editTextWifiSSID.setText(wifiSSID);
            }
        }

        editTextWifiPassword.setDrawableClickListener(new DrawableClickListener() {
            @Override
            public void onClick(DrawablePosition target) {
                String password = editTextWifiPassword.getText().toString();
                if (isPasswordVisible) {
                    editTextWifiPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    editTextWifiPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    editTextWifiPassword.setText(password);
                    editTextWifiPassword.setSelection(password.length());
                    editTextWifiPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_visibility_24, 0);
                } else {
                    editTextWifiPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    editTextWifiPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                    editTextWifiPassword.setText(password);
                    editTextWifiPassword.setSelection(password.length());
                    editTextWifiPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_visibility_off_24, 0);
                }
                isPasswordVisible = !isPasswordVisible;
            }
        });

        buttonNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialogListener != null) {
                    dialogListener.onCancelled();
                }
                dismiss();
            }
        });

        buttonPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String wifiPassword = editTextWifiPassword.getText().toString().trim();
                if (wifiPassword.isEmpty()) {
                    Toast.makeText(getActivity(), "Please enter valid password", Toast.LENGTH_LONG).show();
                    return;
                }
                if (dialogListener != null) {
                    dialogListener.onFinishEditDialog(wifiDevice, wifiPassword, false);
                }
                dismiss();
            }
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
        void onFinishEditDialog(WifiDevice wifiDevice, String wifiPassword, boolean dialogCancelFlag);

        void onCancelled();
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}