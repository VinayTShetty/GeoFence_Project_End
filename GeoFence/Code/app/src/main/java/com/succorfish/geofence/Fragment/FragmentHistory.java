package com.succorfish.geofence.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.succorfish.geofence.BaseFragment.BaseFragment;
import com.succorfish.geofence.MainActivity;
import com.succorfish.geofence.R;
import com.succorfish.geofence.RoomDataBaseEntity.GeofenceAlert;
import com.succorfish.geofence.adapter.FragmentHistoryAdapter;
import com.succorfish.geofence.customObjects.HistroyList;
import com.succorfish.geofence.dialog.DialogProvider;
import com.succorfish.geofence.interfaceActivityToFragment.GeoFenceDialogAlertShow;
import com.succorfish.geofence.interfaces.onAlertDialogCallBack;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.succorfish.geofence.MainActivity.roomDBHelperInstance;

public class FragmentHistory extends BaseFragment {
    View fragmentHistoryView;
    private Unbinder unbinder;
    @BindView(R.id.fragmentHistory_recycleView)
    RecyclerView fragmenyhistoryRecycleView;
    private FragmentHistoryAdapter fragmentHistoryAdapter;
    private ArrayList<HistroyList> histroylist = new ArrayList<>();
    DialogProvider dialogProvider;
    MainActivity mainActivity;
    TextView historyReload_ImageButtom;
    private KProgressHUD hud;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentHistoryView = inflater.inflate(R.layout.fragment_history, container, false);
        unbinder = ButterKnife.bind(this, fragmentHistoryView);
        historyReload_ImageButtom=(TextView) fragmentHistoryView.findViewById(R.id.history_reload);
        bottomLayoutVisibility(false);
        intializeViews();
        histroyRelaodImageButtonClick();
        loadHisttoryList();
        setUpRecycleView();
        historyItemClick();
        intializeDialog();
        geoFenceAlertImplementation();
        return fragmentHistoryView;
    }
    private void bottomLayoutVisibility(boolean hide_true_unhide_false){
        mainActivity.hideBottomLayout(hide_true_unhide_false);
    }

    private void histroyRelaodImageButtonClick(){
        historyReload_ImageButtom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(histroylist.size()>0){
                   histroylist.clear();
                   fragmentHistoryAdapter.notifyDataSetChanged();
               }
                showProgressDialog();
                loadHisttoryList();
                execute_Handler_to_cancel_dailog();
            }
        });
    }

    private void intializeViews(){
        hud = KProgressHUD.create(getActivity());
    }

    private void showProgressDialog() {
        hud
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Loading");
        hud.show();
    }

    private void cancelProgressDialog() {
        if (hud != null) {
            if (hud.isShowing()) {
                hud.dismiss();
            }
        }
    }

  private void execute_Handler_to_cancel_dailog(){
      Handler handler = new Handler();
      handler.postDelayed(new Runnable() {
          @Override
          public void run() {
              cancelProgressDialog();
          }
      },500);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public String toString() {
        return FragmentHistory.class.getSimpleName();
    }



    private void setUpRecycleView() {
        fragmentHistoryAdapter = new FragmentHistoryAdapter(histroylist);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        fragmenyhistoryRecycleView.setLayoutManager(mLayoutManager);
        fragmenyhistoryRecycleView.setAdapter(fragmentHistoryAdapter);
    }

    @OnClick(R.id.fragment_history_back)
    public void backImagePress() {
        mainActivity.onBackPressed();
    }

    private void loadHisttoryList() {
        if (histroylist != null) {
            histroylist.clear();
        }
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                List<GeofenceAlert> geofenceAlertList=roomDBHelperInstance.get_GeoFenceAlert_info_dao().getAll_GeoFence_Alert();
                for (GeofenceAlert geofenceAlert:geofenceAlertList) {
                    HistroyList histroyList = new HistroyList();
                    histroyList.setBrachMessage(geofenceAlert.getRule_Name());
                    histroyList.setDateTime(geofenceAlert.getDate_Time());
                    histroyList.setMessage_one(geofenceAlert.getMessage_one());
                    histroyList.setMessage_two(geofenceAlert.getMessage_two());
                    histroyList.setGeoFenceType(geofenceAlert.getGeo_Type());
                    histroyList.setGeoFenceId(Integer.parseInt(geofenceAlert.getGeofence_ID()));
                    histroyList.setBreachlatitude(Double.parseDouble(geofenceAlert.getBreach_Lat()));
                    histroyList.setBreachLongitude(Double.parseDouble(geofenceAlert.getBreach_Long()));
                    histroyList.setTimeStamp(geofenceAlert.getTimeStamp());
                    histroyList.setIsRead(geofenceAlert.getIs_Read());
                    histroyList.setAliasName_forAlert(geofenceAlert.getAlias_name_alert());
                    histroylist.add(histroyList);
                }

                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fragmentHistoryAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

    }

    private void historyItemClick() {
        fragmentHistoryAdapter.setOnItemClickListner(new FragmentHistoryAdapter.HistoryItemClickInterface() {
            @Override
            public void historyitemClick(String  timStamp) {
                mainActivity.replaceFragment(new FragmentMap(),timStamp,new FragmentMap().toString(),false);
            }
        });
    }
    private void intializeDialog() {
        dialogProvider = new DialogProvider(getActivity());
    }
    private void geoFenceAlertImplementation() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.mainActivity_container);
        mainActivity.setUpGeoFenceAlertDialogInterface(new GeoFenceDialogAlertShow() {
            @Override
            public void showDialogInterface(String ruleVioation, String bleAddress, String message_one, String messageTwo,String time_stamp) {
                if (isAdded() && isVisible() && fragment instanceof FragmentHistory) {
                    dialogProvider.showGeofenceAlertDialog(ruleVioation, bleAddress, message_one, messageTwo, 3, false, new onAlertDialogCallBack() {
                        @Override
                        public void PositiveMethod(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            /**
                             * Open map Fragment.
                             */
                            mainActivity.replaceFragment(new FragmentMap(),time_stamp,new FragmentMap().toString(),false);
                        }
                        @Override
                        public void NegativeMethod(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                } else {
                }
            }
        });
    }

    private void loadUARTSimDetails(){

    }

}
