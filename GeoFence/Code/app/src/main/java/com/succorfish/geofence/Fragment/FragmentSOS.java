package com.succorfish.geofence.Fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.succorfish.geofence.BaseFragment.BaseFragment;
import com.succorfish.geofence.DialogFragmentHelper.ShowSOSDialogFragment;
import com.succorfish.geofence.MainActivity;
import com.succorfish.geofence.R;
import com.succorfish.geofence.RoomDataBaseEntity.ChatInfo;
import com.succorfish.geofence.adapter.FragmentChattingAdapter;
import com.succorfish.geofence.customObjects.ChattingObject;
import com.succorfish.geofence.dialog.DialogProvider;
import com.succorfish.geofence.interfaceActivityToFragment.ChatDeliveryACK;
import com.succorfish.geofence.interfaceActivityToFragment.GeoFenceDialogAlertShow;
import com.succorfish.geofence.interfaceActivityToFragment.PassChatObjectToFragment;
import com.succorfish.geofence.interfaceFragmentToActivity.MessageChatPacket;
import com.succorfish.geofence.interfaces.onAlertDialogCallBack;

import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import static com.succorfish.geofence.MainActivity.CONNECTED_BLE_ADDRESS;
import static com.succorfish.geofence.MainActivity.roomDBHelperInstance;
import static com.succorfish.geofence.blecalculation.MessageCalculation.endMessagePacket;
import static com.succorfish.geofence.blecalculation.MessageCalculation.messageDataArray;
import static com.succorfish.geofence.blecalculation.MessageCalculation.startMessagepacket_message;
import static com.succorfish.geofence.utility.Utility.getDateWithtime;
import static com.succorfish.geofence.utility.Utility.getTimeStamp4bytesToBle;
import static com.succorfish.geofence.utility.Utility.getTimeStampMilliSecondd;
import static com.succorfish.geofence.utility.Utility.hideKeyboardFrom;
import static com.succorfish.geofence.utility.Utility.splitString;
public class FragmentSOS extends BaseFragment {
    View fragmentChattingView;
    MainActivity mainActivity;
    private Unbinder unbinder;
    DialogProvider dialogProvider;
    @BindView(R.id.chat_recycleView)
    RecyclerView chatRecycleView;
    @BindView(R.id.frg_send_message_textview_message)
    EditText message_text_view;
    @BindView(R.id.frg_send_message_imageview_message_send)
    ImageView sendButton_message;
    String connected_bleAddress = "";
    private FragmentChattingAdapter fragmentChattingAdapter;
    private ArrayList<ChattingObject> chattingObjectList = new ArrayList<ChattingObject>();
    MessageChatPacket messageChatPacket;
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
        fragmentChattingView = inflater.inflate(R.layout.fragment_sos, container, false);
        unbinder = ButterKnife.bind(this, fragmentChattingView);
        bottomLayoutVisibility(false);
        getConnectedBleAddress();
        interfaceIntialization();
        intializeDialog();
  //      geoFenceAlertImplementation();
        setupRecycleView(chattingObjectList);
      //  getDBMessageList(connected_bleAddress);
        interfaceImplementation_CallBack();
        longClick();
        return fragmentChattingView;
    }
    private void bottomLayoutVisibility(boolean hide_true_unhide_false){
        mainActivity.hideBottomLayout(hide_true_unhide_false);
    }

    private void interfaceIntialization() {
        messageChatPacket = (MessageChatPacket) getActivity();
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
        return FragmentChatting.class.getSimpleName();
    }

    @OnClick(R.id.close_icon)
    public void chatOnBackPressed() {
        mainActivity.onBackPressed();
    }



    private void intializeDialog() {
        dialogProvider = new DialogProvider(getActivity());
    }

    private void geoFenceAlertImplementation() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.mainActivity_container);
        mainActivity.setUpGeoFenceAlertDialogInterface(new GeoFenceDialogAlertShow() {
            @Override
            public void showDialogInterface(String ruleVioation, String bleAddress, String message_one, String messageTwo, String timeStamp) {
                if (isAdded() && isVisible() && fragment instanceof FragmentChatting) {
                    dialogProvider.showGeofenceAlertDialog(ruleVioation, bleAddress, message_one, messageTwo, 3, false, new onAlertDialogCallBack() {
                        @Override
                        public void PositiveMethod(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            /**
                             * Open map Fragment.
                             */
                            mainActivity.replaceFragment(new FragmentMap(), timeStamp, new FragmentMap().toString(), false);
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

    @OnClick(R.id.frg_send_message_textview_message)
    public void showPopUpOnTextMessageClick() {

    }


    @OnClick(R.id.sos_message_popUp)
    public void SOS_messsageClick(){
        ShowSOSDialogFragment sosDialogFragment=new ShowSOSDialogFragment();
        FragmentTransaction ft = mainActivity.getSupportFragmentManager().beginTransaction();
        Fragment prev = mainActivity.getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }

        sosDialogFragment.show(ft, "dialog");

        sosDialogFragment.setDialogListener(new ShowSOSDialogFragment.DialogListener() {
            @Override
            public void onSOSMessageSelected(String messageSelected, boolean dialogCancelFlag) {
                message_text_view.setText(messageSelected);
            }
        });
    }

    private void showPopupDialog() {
        dialogProvider.showAlertDialog(getActivity(), "Select GSM OR IRIDIUM", "");
    }
  /*  private void showMessageTypePopUp(){
            dialogProvider.showMessageChatPopUp(getActivity(), new ChatMessageText() {
                @Override
                public void PositiveMethod(DialogInterface dialog, int id, String typedMessage) {
                    if(typedMessage.length()>0){
                        dialog.dismiss();
                       String typedMesssageDemo="Counting letters and characters are more or less impossible for a normal text. Even if a site only allows a limited number of characters.A while back I needed12";
                        hideKeyBoard(typedMesssageDemo);//typedMessage
                        insertChat_To_table(typedMesssageDemo,getTimeStamp());//typedMessage
                    }
                }
                @Override
                public void NegativeMethod(DialogInterface dialog, int id) {
                }
            });
    }*/

    private void getConnectedBleAddress() {
     //   connected_bleAddress = CONNECTED_BLE_ADDRESS;
           connected_bleAddress = "d4225031e495";
    }

    private void insertChat_To_table(String message_typed, String timeStampDataBase, String timeStampSequenceNumberBle) {
        String date_time = getDateWithtime();
        String delivery_status = getString(R.string.fragment_chat_message_mesaage_full_message_recieved_by_device);
        showUiChangesInRecycleView(getResources().getString(R.string.fragment_chat_message_mesaage_outgoing_message), message_typed, date_time, delivery_status, timeStampDataBase, date_time.substring(11, 16), connected_bleAddress.replace(":", "").toLowerCase(), timeStampSequenceNumberBle);
        if (connected_bleAddress.length() > 1) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    int sequenceNumber = Integer.parseInt(timeStampSequenceNumberBle);
                    ChatInfo chatInfo = new ChatInfo();
                    chatInfo.setFrom_name(getString(R.string.fragment_chat_owner_name));
                    chatInfo.setTo_name(getString(R.string.fragment_chat_server_name));
                    chatInfo.setMsg_txt(message_typed);
                    chatInfo.setTime(date_time);
                    chatInfo.setStatus(delivery_status);
                    chatInfo.setSequence("" + sequenceNumber);
                    chatInfo.setIdentifier(connected_bleAddress.replace(":", "").toLowerCase());
                    chatInfo.setTimeStamp(timeStampDataBase);
                  //  roomDBHelperInstance.get_Chat_info_dao().insert_ChatInfo(chatInfo);

                }
            });
        }
    }
    private void showUiChangesInRecycleView(String mode,
                                            String message_typed,
                                            String date_time,
                                            String deliveryStatus,
                                            String timeStampDataBase,
                                            String time_chat, String connected_bleAddress, String timeStampSequenceNumberBle) {
        ChattingObject chattingObject = new ChattingObject();
        chattingObject.setMode(mode);
        chattingObject.setMessage(message_typed);
        chattingObject.setDate(date_time);
        chattingObject.setDelivery_status(deliveryStatus);
        chattingObject.setTimeStamp(timeStampDataBase);
        chattingObject.setTime_chat(time_chat);
        chattingObject.setBleAddress(connected_bleAddress);
        chattingObject.setSequenceNumber(timeStampSequenceNumberBle);
        chattingObjectList.add(chattingObject);
        fragmentChattingAdapter.notifyDataSetChanged();
        if (chattingObjectList.size() > 0) {
            chatRecycleView.scrollToPosition(chattingObjectList.size() - 1);
        }

    }

    private void hideKeyBoard(String messageTyped) {
        hideKeyboardFrom(getActivity(), fragmentChattingView);

    }

    private void sendMessageToBleDevice(String messageTyped, String timeStampDataBase, String timeStampSequenceNumberToBle) {
       // processMessageListAndSend(connected_bleAddress, messageTyped, timeStampDataBase, timeStampSequenceNumberToBle);
    }

    private ArrayList<byte[]> PrepareEntireMessagePacketList(String bleAddressConnected, String messageTyped, String timeStampSequenceNumberBle) {

        ArrayList<byte[]> entireMessagePacekt = new ArrayList<byte[]>();
        List<String> listOfString_startPacket = splitString(messageTyped, 12);
        /**
         * Start packet process in this logic.
         */
        System.out.println("SEQUENCE_NUMBER SEND "+timeStampSequenceNumberBle);
        entireMessagePacekt.add(startMessagepacket_message(listOfString_startPacket.size(), messageTyped.length(), timeStampSequenceNumberBle, timeStampSequenceNumberBle)); //start message packet.
        /**
         * Data pacekts in this logic.
         */
        int indexPosition = 0;
        for (String individualMessage_Of_12 : listOfString_startPacket) {
            indexPosition++;
            entireMessagePacekt.add(messageDataArray(indexPosition, individualMessage_Of_12.length(), individualMessage_Of_12));
        }
        /**
         * end packet process ing in this logic
         */
        String channelId="";
      /*  boolean gsm_radio_button_checked = radiobutton_gsm.isChecked();
        boolean iridium_radio_button_checked = radiobutton_gsm.isChecked();
        if (gsm_radio_button_checked) {
            channelId="GSM";
        } else if (iridium_radio_button_checked) {
            channelId="IRIDIUM";
        }*/
        entireMessagePacekt.add(endMessagePacket(entireMessagePacekt.size() + 1, channelId));
        return entireMessagePacekt;
    }

    private void processMessageListAndSend(String bleAddress, String messageTyped, String timeStampDatabase, String timeStampSequenceNumberble) {
        ArrayList<byte[]> arrayListEntireMessageByteArray = PrepareEntireMessagePacketList(connected_bleAddress, messageTyped, timeStampSequenceNumberble);
        if (messageChatPacket != null) {
            /**
             * passing the data pacekt through interface to MainActivity...
             */
            messageChatPacket.messagePacketArray(bleAddress, arrayListEntireMessageByteArray);
        }
    }

    private void getDBMessageList(String connectedBleAddress) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                String bleAddress = connectedBleAddress.replace(":", "").toLowerCase();
                List<ChatInfo> chatInfoList = roomDBHelperInstance.get_Chat_info_dao().getAll_ChatsFromBleAddress(bleAddress);
                for (ChatInfo chatInfo : chatInfoList) {
                    ChattingObject chattingObject = new ChattingObject();
                    if (chatInfo.getFrom_name().equalsIgnoreCase(getString(R.string.fragment_chat_owner_name)) && (chatInfo.getTo_name().equalsIgnoreCase(getString(R.string.fragment_chat_server_name)))) {
                        chattingObject.setMode(getResources().getString(R.string.fragment_chat_message_mesaage_outgoing_message));// Incomming
                    } else if (chatInfo.getFrom_name().equalsIgnoreCase(getString(R.string.fragment_chat_server_name)) && (chatInfo.getTo_name().equalsIgnoreCase(getString(R.string.fragment_chat_owner_name)))) {
                        chattingObject.setMode(getResources().getString(R.string.fragment_chat_message_mesaage_incomming_message));// Incomming
                    }
                    chattingObject.setMessage(chatInfo.getMsg_txt());
                    chattingObject.setTime_chat(chatInfo.getTime().substring(11, 16));
                    chattingObject.setDelivery_status(chatInfo.getStatus());
                    chattingObject.setTimeStamp(chatInfo.getTimeStamp());
                    chattingObject.setSequenceNumber(chatInfo.getSequence());
                    chattingObject.setBleAddress(chatInfo.getIdentifier());
                    chattingObjectList.add(chattingObject);
                    chatRecycleView.post(new Runnable() {
                        @Override
                        public void run() {
                            fragmentChattingAdapter.notifyDataSetChanged();
                            if (chattingObjectList.size() > 0) {
                                chatRecycleView.scrollToPosition(chattingObjectList.size() - 1);
                            }
                        }
                    });
                }
            }
        });
    }

    private void setupRecycleView(ArrayList<ChattingObject> chattingObjectList) {
        fragmentChattingAdapter = new FragmentChattingAdapter(chattingObjectList,getActivity());
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        chatRecycleView.setLayoutManager(mLayoutManager);
        chatRecycleView.setAdapter(fragmentChattingAdapter);
        if (chattingObjectList.size() > 0) {
            chatRecycleView.scrollToPosition(chattingObjectList.size() - 1);
        }
    }

    private int getItemPositionFromSequenceNumber(String bleAddress, String sequenceNumber) {
        int itemPostion = -1;
        if (isVisible()) {
            if ((chattingObjectList != null) && (chattingObjectList.size() > 0) && (!(chattingObjectList.isEmpty()))) {
                for (int i = 0; i < chattingObjectList.size(); i++) {
                    if((chattingObjectList.get(i).getBleAddress().equalsIgnoreCase(bleAddress))&&(chattingObjectList.get(i).getSequenceNumber().equalsIgnoreCase(sequenceNumber))){
                        itemPostion=i;
                    }
                }
            }
        }
        return itemPostion;
    }

    private void interfaceImplementation_CallBack() {
        mainActivity.setUpchatDeliveryACK(new ChatDeliveryACK() {
            @Override
            public void chatDeliveryStatus(String bleAddress, String sequenceNumber, String messageStatus) {
                int postionObtained = getItemPositionFromSequenceNumber(bleAddress, sequenceNumber);
                if (postionObtained != -1) {
                    chattingObjectList.get(postionObtained).setDelivery_status(messageStatus);
                    fragmentChattingAdapter.notifyDataSetChanged();
                }
            }
        });

        mainActivity.setUpPassChatObjectToFragment(new PassChatObjectToFragment() {
            @Override
            public void ChatObjetShare(ChattingObject chattingObject) {
                chattingObjectList.add(chattingObject);
                fragmentChattingAdapter.notifyDataSetChanged();
                chatRecycleView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (chattingObjectList.size() > 0) {
                            chatRecycleView.scrollToPosition(chattingObjectList.size() - 1);
                        }
                    }
                });

            }
        });
    }

    @OnClick(R.id.frg_send_message_imageview_message_send)
    public void OnMessageClickSend() {
        String typedMessage = message_text_view.getText().toString();
            if (typedMessage.length() > 0) {
                //  String typedMesssageDemo="Counting letters and characters are more or less impossible for a normal text. Even if a site only allows a limited number of characters.A while back I needed12";

                String timeStamp_Sequence_NumberToTble = getTimeStamp4bytesToBle();
                String timeStampDateBase = getTimeStampMilliSecondd();
                //  String timeStampDateBase = DateUtilsMyHelper.getCurrentDate(DateUtilsMyHelper.dateFormatStandard);
                hideKeyBoard(typedMessage);//typedMessage
                insertChat_To_table(typedMessage, timeStampDateBase, timeStamp_Sequence_NumberToTble);
                makeEditTextEmpty();
          //      sendMessageToBleDevice(typedMessage, timeStampDateBase, timeStamp_Sequence_NumberToTble);
            }

    }

    private void makeEditTextEmpty() {
        message_text_view.setText("");
    }

    private void longClick() {
        message_text_view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });
    }

}