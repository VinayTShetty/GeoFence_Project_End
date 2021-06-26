package com.succorfish.geofence.DialogFragmentHelper;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.succorfish.geofence.R;
import com.succorfish.geofence.adapter.SOSdialogFragmentAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ShowSOSDialogFragment extends DialogFragment {
    SOSdialogFragmentAdapter soSdialogFragmentAdapter;
    private FragmentActivity fragmentActivity;
    private Unbinder unbinder;
    private ArrayList<String> sosMessageList=new ArrayList<String>();
    @BindView(R.id.popup_sent_msg_recyclerview_msg)
    RecyclerView sosMessageListRecycleView;
    private DialogListener dialogListener;
    public ShowSOSDialogFragment() {

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        fragmentActivity = getActivity();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sospopup, container, false);
        unbinder = ButterKnife.bind(this, view);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        add_SOSMessages();
        setUpRecycleView();
        SOS_messagesItemClick();
        return view;
    }

    private void setUpRecycleView() {
        soSdialogFragmentAdapter=new SOSdialogFragmentAdapter(sosMessageList);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        sosMessageListRecycleView.setLayoutManager(mLayoutManager);
        sosMessageListRecycleView.setAdapter(soSdialogFragmentAdapter);
    }

    private void SOS_messagesItemClick(){
        soSdialogFragmentAdapter.setOnItemClickListner_sosMessage(new SOSdialogFragmentAdapter.sosItemListMesageListInterface() {
            @Override
            public void sosMessageItem(int postion) {
                System.out.println(" Message Clicked= "+sosMessageList.get(postion).toString());
                if(dialogListener!=null){
                    dialogListener.onSOSMessageSelected(sosMessageList.get(postion).toString(),false);
                    dismiss();
                }
            }
        });
    }

    private void add_SOSMessages(){
        for (int i = 0; i <5 ; i++) {
            sosMessageList.add("SOS MESSAGES "+i);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_AppCompat_Light_Dialog_Alert);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppTheme_Dialog_CustomShort);
        setCancelable(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    @OnClick(R.id.popup_sent_msg_textview_done)
    public void onDoneButtonClicked() {
        dismiss();
    }

    @OnClick(R.id.popup_sent_msg_textview_cancel)
    public void onCancelButtonClicked() {
        dismiss();
    }

    public interface DialogListener {
        void onSOSMessageSelected(String messageSelected, boolean dialogCancelFlag);
    }
    public void setDialogListener(DialogListener dialogListener_loc) {
        this.dialogListener = dialogListener_loc;
    }
}
