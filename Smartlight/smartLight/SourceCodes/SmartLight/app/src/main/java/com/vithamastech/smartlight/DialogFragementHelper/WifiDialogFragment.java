package com.vithamastech.smartlight.DialogFragementHelper;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vithamastech.smartlight.Adapter.WifiDeviceListAdapter;
import com.vithamastech.smartlight.PowerSocketCustomObjects.WifiDevice;
import com.vithamastech.smartlight.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class WifiDialogFragment extends DialogFragment {
    WifiDeviceListAdapter wifiDeviceListAdapter;
    private Unbinder unbinder;
    private ArrayList<WifiDevice> wifiDeviceArrayList = new ArrayList<WifiDevice>();

    @BindView(R.id.recyclerView_wifiConnect)
    RecyclerView wifiListRecycleView;
    Button buttonCancel;
    DialogFragmentWifiListener dialogFragmentWifiListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_AppCompat_Light_Dialog_Alert);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppTheme_Dialog_CustomShort);
        setCancelable(false);
        getDataFromBundle();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wifi_popup_layout, container, false);
        unbinder = ButterKnife.bind(this, view);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        wifiListRecycleView.setLayoutManager(mLayoutManager);
        wifiDeviceListAdapter = new WifiDeviceListAdapter(wifiDeviceArrayList);
        wifiListRecycleView.setAdapter(wifiDeviceListAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        wifiListRecycleView.addItemDecoration(dividerItemDecoration);

        buttonCancel = view.findViewById(R.id.popup_feedback_button_send);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogFragmentWifiListener != null) {
                    dialogFragmentWifiListener.onCancelled();
                }
                dismiss();
            }
        });

        wifiDeviceListAdapter.setOnItemClickListener(new WifiDeviceListAdapter.WifiListInterface() {
            @Override
            public void wifiListSingleItem(int position) {
                if (dialogFragmentWifiListener != null) {
                    dialogFragmentWifiListener.onWifiDeviceClicked(wifiDeviceArrayList.get(position), position);
                }
                dismiss();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public interface DialogFragmentWifiListener {
        void onWifiDeviceClicked(WifiDevice wifiDevice, int position);

        void onCancelled();
    }

    public void setUpDialogListenerWifi(DialogFragmentWifiListener loc_dialogFragmentWifiListener) {
        dialogFragmentWifiListener = loc_dialogFragmentWifiListener;
    }

    private void getDataFromBundle() {
        Bundle wifiListFetched = getArguments();
        wifiDeviceArrayList = (ArrayList<WifiDevice>) wifiListFetched.get(getResources().getString(R.string.Wifi_list));
    }
}