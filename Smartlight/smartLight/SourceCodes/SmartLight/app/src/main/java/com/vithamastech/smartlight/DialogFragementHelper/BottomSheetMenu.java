package com.vithamastech.smartlight.DialogFragementHelper;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.vithamastech.smartlight.R;

public class BottomSheetMenu extends BottomSheetDialogFragment {
    public static final String TAG = "ActionBottomDialog";

    private OnMenuItemClickListener onMenuItemClickListener;

    public static BottomSheetMenu newInstance() {
        return new BottomSheetMenu();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().setCanceledOnTouchOutside(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_menu_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textViewCancel = view.findViewById(R.id.bottomSheetCancel);
        TextView textViewSocketCustomization = view.findViewById(R.id.textViewSetNameAndImage);
        TextView textViewAlarm = view.findViewById(R.id.textViewAlarm);

        textViewCancel.setOnClickListener(v -> dismiss());

        textViewSocketCustomization.setOnClickListener(v -> {
            if (onMenuItemClickListener != null) {
                onMenuItemClickListener.onSocketCustomizationClicked();
                dismiss();
            }
//                BottomSheetForSetNameAndImage BottomSheetForSetNameAndImage = new BottomSheetForSetNameAndImage();
//                BottomSheetForSetNameAndImage.show(getFragmentManager(),"setModel");
        });

        textViewAlarm.setOnClickListener(v -> {
            if (onMenuItemClickListener != null) {
                onMenuItemClickListener.onAlarmMenuClicked();
            }
            dismiss();
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public interface OnMenuItemClickListener {
        public void onAlarmMenuClicked();

        public void onSocketCustomizationClicked();
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener onMenuItemClickListener) {
        this.onMenuItemClickListener = onMenuItemClickListener;
    }
}