package com.vithamastech.smartlight.DialogFragementHelper;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.vithamastech.smartlight.Adapter.SocketIconAdapter;
import com.vithamastech.smartlight.GridSpacingItemDecoration;
import com.vithamastech.smartlight.IconSelector;
import com.vithamastech.smartlight.PowerSocketUtils.Socket;
import com.vithamastech.smartlight.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BottomSheetSocketCustomization extends BottomSheetDialogFragment {
    public static final String TAG = "ActionBottomDialog";

    private OnSocketCustomizationSelectedListener onSocketCustomizationSelectedListener;
    private EditText editTextSocketName;
    private RecyclerView recyclerView;
    private SocketIconAdapter adapter;
    private IconSelector selectedIcon;
    Button buttonSave, buttonCancel;

    public static BottomSheetSocketCustomization newInstance() {
        return new BottomSheetSocketCustomization();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme);
        // For old version
        //        setCancelable(false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Objects.requireNonNull(getDialog()).setCanceledOnTouchOutside(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_socket_customization, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String socketName = null;
        int imageSelected = 0;

        Bundle bundle = getArguments();
        if (bundle != null) {
            Socket socket = (Socket) bundle.getSerializable("SelectedSocket");
            if (socket != null) {
                socketName = socket.socketName;
                imageSelected = socket.imageType;
            }
        }

        editTextSocketName = view.findViewById(R.id.editTextSocketName);
        buttonCancel = view.findViewById(R.id.buttonCancel);
        buttonSave = view.findViewById(R.id.buttonSave);

        recyclerView = view.findViewById(R.id.recyclerView);

//        ViewCompat.setNestedScrollingEnabled(recyclerView, true);
        recyclerView.setHasFixedSize(true);
//        recyclerView.setNestedScrollingEnabled(false);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SocketIconAdapter(getIconsList());
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        adapter.setOnIconSelectedListener((iconSelector, position) -> selectedIcon = iconSelector);

        editTextSocketName.setText(socketName);
        selectedIcon = adapter.update(imageSelected);

        buttonSave.setOnClickListener(v -> {
            if (selectedIcon != null) {
                if (selectedIcon.isSelected) {
                    String newSocketName = editTextSocketName.getText().toString().trim();
                    if (!newSocketName.isEmpty()) {
                        if (onSocketCustomizationSelectedListener != null) {
                            onSocketCustomizationSelectedListener.onSocketCustomized(selectedIcon, newSocketName);
                            dismiss();
                        }
                    } else {
                        Toast.makeText(getContext(), "Please type a valid new name for socket.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Please select an icon.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Please select an icon.", Toast.LENGTH_SHORT).show();
            }
        });

        buttonCancel.setOnClickListener(v -> dismiss());
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

    private List<IconSelector> getIconsList() {
        List<IconSelector> iconsList = new ArrayList<>();
        iconsList.add(new IconSelector(R.drawable.group_1));
        iconsList.add(new IconSelector(R.drawable.group_2));
        iconsList.add(new IconSelector(R.drawable.socket_icon_1));
        iconsList.add(new IconSelector(R.drawable.socket_icon_2));
        iconsList.add(new IconSelector(R.drawable.socket_icon_3));
        iconsList.add(new IconSelector(R.drawable.socket_icon_4));
        iconsList.add(new IconSelector(R.drawable.socket_icon_5));
        iconsList.add(new IconSelector(R.drawable.socket_icon_6));
        iconsList.add(new IconSelector(R.drawable.socket_icon_7));
        iconsList.add(new IconSelector(R.drawable.socket_icon_8));
        iconsList.add(new IconSelector(R.drawable.socket_icon_9));
        iconsList.add(new IconSelector(R.drawable.socket_icon_10));
        iconsList.add(new IconSelector(R.drawable.socket_icon_11));
        iconsList.add(new IconSelector(R.drawable.socket_icon_12));
        iconsList.add(new IconSelector(R.drawable.socket_icon_13));
        iconsList.add(new IconSelector(R.drawable.socket_icon_14));
        iconsList.add(new IconSelector(R.drawable.socket_icon_15));
        iconsList.add(new IconSelector(R.drawable.socket_icon_16));
        iconsList.add(new IconSelector(R.drawable.socket_icon_17));
        return iconsList;
    }

    /*
    For Full screen Modal Bottom Sheet
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        bottomSheetDialog.setOnShowListener(dialog -> {
            BottomSheetDialog dialogc = (BottomSheetDialog) dialog;
            // When using AndroidX the resource can be found at com.google.android.material.R.id.design_bottom_sheet
            FrameLayout bottomSheet = dialogc.findViewById(com.google.android.material.R.id.design_bottom_sheet);

            BottomSheetBehavior<FrameLayout> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
            bottomSheetBehavior.setPeekHeight(Resources.getSystem().getDisplayMetrics().heightPixels);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });

        // For old version
//        // handle back button
//        bottomSheetDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
//            @Override
//            public boolean onKey(final DialogInterface dialog, final int keyCode, final KeyEvent event) {
//                if (keyCode == KeyEvent.KEYCODE_BACK) {
//                    dialog.dismiss();
//                }
//                return true;
//            }
//        });

        return bottomSheetDialog;
    }

    public interface OnSocketCustomizationSelectedListener {
        public void onSocketCustomized(IconSelector iconSelector, String newSocketName);
    }

    public void setOnSocketCustomizationSelectedListener(OnSocketCustomizationSelectedListener onSocketCustomizationSelectedListener) {
        this.onSocketCustomizationSelectedListener = onSocketCustomizationSelectedListener;
    }

    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
}