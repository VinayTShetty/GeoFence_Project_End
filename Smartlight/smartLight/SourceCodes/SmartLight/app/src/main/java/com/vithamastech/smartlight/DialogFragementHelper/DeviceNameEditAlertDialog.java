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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.vithamastech.smartlight.R;

public class DeviceNameEditAlertDialog extends DialogFragment {
    private DialogListener dialogListener;

    public DeviceNameEditAlertDialog() {
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
        View view = inflater.inflate(R.layout.fragment_add_power_socket_dialog, container, false);
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

//        Button buttonNegative = view.findViewById(R.id.buttonNegative);
        Button buttonPositive = view.findViewById(R.id.buttonPositive);
        EditText editTextPowerSocketName = view.findViewById(R.id.editTextPowerSocketName);

//        buttonNegative.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (dialogListener != null) {
//                    dialogListener.onCancelEditDialog();
//                }
//                dismiss();
//            }
//        });

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

//        void onCancelEditDialog();
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
