package com.succorfish.geofence.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.succorfish.geofence.BaseFragment.BaseFragment;
import com.succorfish.geofence.MainActivity;
import com.succorfish.geofence.R;
import com.succorfish.geofence.adapter.FragmentHistoryAdapter;
import com.succorfish.geofence.adapter.FragmentUARTSingleSelectionAdapter;
import com.succorfish.geofence.customObjects.SimDetails;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.succorfish.geofence.MainActivity.CONNECTED_BLE_ADDRESS;

public class FragmentUARTConfiguration extends BaseFragment {
    private Unbinder unbinder;
    View fragmetnUARTconfigurationView;
    MainActivity mainActivity;
    String connectedBleAddress="";
    /**
     *RecycleView Parts.
     */
    FragmentUARTSingleSelectionAdapter uartSingleSelectionAdapter;
    @BindView(R.id.uart_configuration_single_selection)
    RecyclerView fragmentUartSingleRecycleView;
    ArrayList<SimDetails> simDetailsArrayListCollection=new ArrayList<SimDetails>();
    int postionSelected=-1;
    private  String connected_bleAddress="";

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
        fragmetnUARTconfigurationView = inflater.inflate(R.layout.fragment_uart_configuration, container, false);
        unbinder = ButterKnife.bind(this, fragmetnUARTconfigurationView);
        bottomLayoutVisibility(false);
        load_UARTlist();
        getConnectedBleAddress();
        setUpRecycleView();
        UARTitemClick();
        return fragmetnUARTconfigurationView;
    }

    private void bottomLayoutVisibility(boolean hide_true_unhide_false){
        mainActivity.hideBottomLayout(hide_true_unhide_false);
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public String toString() {
        return FragmentUARTConfiguration.class.getSimpleName();
    }

    @OnClick(R.id.sim_confi_save)
    public void onclickSaveConfig(){
        String simDetails="";
        if(postionSelected!=-1){
             simDetails=simDetailsArrayListCollection.get(postionSelected).getSimDetails();
            mainActivity.preferenceHelper.setConfigutation_BANDCONFIG(connected_bleAddress+" "+FragmentUARTConfiguration.class.getSimpleName(),""+simDetails);
            mainActivity.replaceFragment(new FragmentSimConfiguration(),null,null,false);
        }else {
            mainActivity.replaceFragment(new FragmentSimConfiguration(),null,null,false);
            makePreferrenceValueDefault();
        }

    }

    private void makePreferrenceValueDefault() {
        mainActivity.preferenceHelper.setConfigutation_UART(connected_bleAddress+" "+FragmentUARTConfiguration.class.getSimpleName(),"");
    }

    private void getConnectedBleAddress(){
        connected_bleAddress=CONNECTED_BLE_ADDRESS;
    }

    private void setUpRecycleView(){
        uartSingleSelectionAdapter = new FragmentUARTSingleSelectionAdapter(simDetailsArrayListCollection);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        fragmentUartSingleRecycleView.setLayoutManager(mLayoutManager);
        fragmentUartSingleRecycleView.setAdapter(uartSingleSelectionAdapter);
    }
    private void UARTitemClick(){
        uartSingleSelectionAdapter.setOnItemClickListner_Simdetails(new FragmentUARTSingleSelectionAdapter.UARTDetailsHolderInterface() {
            @Override
            public void simDetailsClick(int  position) {
                postionSelected=position;
            }
        });
    }
    private void load_UARTlist(){
        simDetailsArrayListCollection.add(new SimDetails("1. LTE Cat M1",false,(byte)0X01));
        simDetailsArrayListCollection.add(new SimDetails("2. LTE Cat NB1",false,(byte)0X02));
        simDetailsArrayListCollection.add(new SimDetails("3. GPRS / eGPRS",false,(byte)0X03));
        simDetailsArrayListCollection.add(new SimDetails("4. GPRS / eGPRS & LTE Cat NB1",false,(byte)0X04));
        simDetailsArrayListCollection.add(new SimDetails("5. GPRS / eGPRS & LTE Cat M1",false,(byte)0X05));
        simDetailsArrayListCollection.add(new SimDetails("6. LTE Cat NB1 & LTE Cat M1",false,(byte)0X06));
        simDetailsArrayListCollection.add(new SimDetails("7. LTE Cat NB1 & GPRS / eGPRS",false,(byte)0X07));
        simDetailsArrayListCollection.add(new SimDetails("8. LTE Cat M1 & LTE Cat NB1",false,(byte)0X08));
        simDetailsArrayListCollection.add(new SimDetails("9. LTE Cat M1 & GPRS / eGPRS",false,(byte)0X09));
        simDetailsArrayListCollection.add(new SimDetails("10. GPRS / eGPRS & LTE Cat NB1 & LTE Cat M1",false,(byte)0X0A));
        simDetailsArrayListCollection.add(new SimDetails("11. GPRS / eGPRS & LTE Cat M1 & LTE Cat NB1",false,(byte)0X0B));
        simDetailsArrayListCollection.add(new SimDetails("12. LTE Cat NB1 & GPRS / eGPRS & LTE Cat M1",false,(byte)0X0C));
        simDetailsArrayListCollection.add(new SimDetails("13. LTE Cat NB1 & LTE Cat M1 & GPRS / eGPRS",false,(byte)0X0D));
        simDetailsArrayListCollection.add(new SimDetails("14. LTE Cat M1 & LTE Cat NB1 & GPRS / eGPRS",false,(byte)0X0E));
        simDetailsArrayListCollection.add(new SimDetails("15. LTE Cat M1 & GPRS / eGPRS & LTE Cat NB1",false,(byte)0X0F));
    }

    @OnClick (R.id.fragment_urat_back)
    public void UART_backClick(){
        if(isVisible()){
          mainActivity.onBackPressed();
        }
    }
}
