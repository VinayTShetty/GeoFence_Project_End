package com.succorfish.combatdiver.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.succorfish.combatdiver.MainActivity;
import com.succorfish.combatdiver.R;
import com.succorfish.combatdiver.Vo.VoDiverMessage;
import com.succorfish.combatdiver.db.DataHolder;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Jaydeep on 18-01-2018.
 */

public class FragmentSettingPrefrence extends Fragment {

    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;

    @BindView(R.id.frg_setting_pref_textview_heat_one_optional)
    TextView mTextViewHeatOne;
    @BindView(R.id.frg_setting_pref_textview_heat_two_optional)
    TextView mTextViewHeatTwo;
    @BindView(R.id.frg_setting_pref_textview_heat_three_optional)
    TextView mTextViewHeatThree;
    @BindView(R.id.frg_setting_pref_textview_heat_four_optional)
    TextView mTextViewHeatFour;

    private TextView mTextViewNoMessageFound;
    RecyclerView mRecyclerViewMessage;
    private boolean isCalling = true;
    Dialog myDialog;
    ArrayList<VoDiverMessage> mArrayListDiverMessage = new ArrayList<>();
    MessageAdapter mMessageAdapter;
    int mIntHeatPos = 1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_setting_prefrence, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);

        mActivity.mTextViewTitle.setText(getResources().getString(R.string.frg_preference_txt_title));
        mActivity.mImageViewDrawer.setVisibility(View.GONE);
        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
        mActivity.mImageViewPerson.setVisibility(View.GONE);
        mActivity.mTextViewActiveCount.setVisibility(View.GONE);
        mActivity.mImageViewAdd.setVisibility(View.GONE);
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        String queryHeat1 = "select " + mActivity.mDbHelper.mFieldDiverMessageMessage + " from " + mActivity.mDbHelper.mTableCannedDiverMessage + " where " + mActivity.mDbHelper.mFieldDiverMessageId + "= '" + mActivity.mPreferenceHelper.getDeviceHeat1Msg() + "'";
        mTextViewHeatOne.setText(mActivity.mDbHelper.getQueryResult(queryHeat1));
        String queryHeat2 = "select " + mActivity.mDbHelper.mFieldDiverMessageMessage + " from " + mActivity.mDbHelper.mTableCannedDiverMessage + " where " + mActivity.mDbHelper.mFieldDiverMessageId + "= '" + mActivity.mPreferenceHelper.getDeviceHeat2Msg() + "'";
        mTextViewHeatTwo.setText(mActivity.mDbHelper.getQueryResult(queryHeat2));
        String queryHeat3 = "select " + mActivity.mDbHelper.mFieldDiverMessageMessage + " from " + mActivity.mDbHelper.mTableCannedDiverMessage + " where " + mActivity.mDbHelper.mFieldDiverMessageId + "= '" + mActivity.mPreferenceHelper.getDeviceHeat3Msg() + "'";
        mTextViewHeatThree.setText(mActivity.mDbHelper.getQueryResult(queryHeat3));
        String queryHeat4 = "select " + mActivity.mDbHelper.mFieldDiverMessageMessage + " from " + mActivity.mDbHelper.mTableCannedDiverMessage + " where " + mActivity.mDbHelper.mFieldDiverMessageId + "= '" + mActivity.mPreferenceHelper.getDeviceHeat4Msg() + "'";
        mTextViewHeatFour.setText(mActivity.mDbHelper.getQueryResult(queryHeat4));

        return mViewRoot;
    }

    @OnClick(R.id.frg_setting_pref_relativelayout_heat_one)
    public void onClickHeatMessage1(View mView) {
        mIntHeatPos = 1;
        showMessagePopup();
    }

    @OnClick(R.id.frg_setting_pref_relativelayout_heat_two)
    public void onClickHeatMessage2(View mView) {
        mIntHeatPos = 2;
        showMessagePopup();
    }

    @OnClick(R.id.frg_setting_pref_relativelayout_heat_three)
    public void onClickHeatMessage3(View mView) {
        mIntHeatPos = 3;
        showMessagePopup();
    }

    @OnClick(R.id.frg_setting_pref_relativelayout_heat_four)
    public void onClickHeatMessage4(View mView) {
        mIntHeatPos = 4;
        showMessagePopup();
    }

    public void openAddEditDialog() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(mActivity);
        View mView = layoutInflaterAndroid.inflate(R.layout.user_input_dialog_box, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
        alertDialogBuilderUserInput.setView(mView);
        final EditText mEditTextMsg = (EditText) mView.findViewById(R.id.user_input_dialog_edittext_msg);
        TextView mTextViewTitle = (TextView) mView.findViewById(R.id.user_input_dialog_textview_title);
        mTextViewTitle.setText(getResources().getString(R.string.frg_setting_txt_heat_msg));
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                        // ToDo get user input here
                        mActivity.mUtility.hideKeyboard(mActivity);
                        InputMethodManager im = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        im.hideSoftInputFromWindow(mEditTextMsg.getWindowToken(), 0);
                        dialogBox.dismiss();
                        String mStringMsg = mEditTextMsg.getText().toString().trim();
                        if (mStringMsg != null && !mStringMsg.equalsIgnoreCase("")) {

                        } else {
                            mActivity.mUtility.errorDialog("Please enter heat message", 1);
                        }
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                mActivity.mUtility.hideKeyboard(mActivity);
                                InputMethodManager im = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                                im.hideSoftInputFromWindow(mEditTextMsg.getWindowToken(), 0);
                                dialogBox.cancel();
                            }
                        });

        AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
        alertDialogAndroid.show();
    }

    private void showMessagePopup() {
        if (myDialog != null && myDialog.isShowing()) {
            return;
        }
        myDialog = new Dialog(mActivity);
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        myDialog.setContentView(R.layout.popup_sent_message);
        myDialog.setCancelable(false);
        ColorDrawable back = new ColorDrawable(getResources().getColor(R.color.colorPopupTransparent));
        InsetDrawable inset = new InsetDrawable(back, 0);
        myDialog.getWindow().setBackgroundDrawable(inset);

        mTextViewNoMessageFound = (TextView) myDialog
                .findViewById(R.id.popup_sent_msg_textview_nomsg);
        TextView mTextViewDone = (TextView) myDialog.findViewById(R.id.popup_sent_msg_textview_done);
        TextView mTextViewCancel = (TextView) myDialog.findViewById(R.id.popup_sent_msg_textview_cancel);
        TextView mTextViewToMessage = (TextView) myDialog.findViewById(R.id.popup_sent_msg_textview_header_to_msg);
        mRecyclerViewMessage = (RecyclerView) myDialog.findViewById(R.id.popup_sent_msg_recyclerview_msg);
        if (mIntHeatPos == 1) {
            mTextViewToMessage.setText("to assign as Heat 1");
        } else if (mIntHeatPos == 2) {
            mTextViewToMessage.setText("to assign as Heat 2");
        } else if (mIntHeatPos == 3) {
            mTextViewToMessage.setText("to assign as Heat 3");
        } else if (mIntHeatPos == 4) {
            mTextViewToMessage.setText("to assign as Heat 4");
        }
        isCalling = true;
        getDBDiverMessageList();

        mTextViewDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialog.dismiss();
            }
        });
        mTextViewCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(myDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        myDialog.show();
        myDialog.getWindow().setAttributes(lp);
    }

    private void getDBDiverMessageList() {
//        int tableGroupCount = mActivity.mDbHelper.getTableCount(mActivity.mDbHelper.mTableGroup);
        DataHolder mDataHolder;
        mArrayListDiverMessage = new ArrayList<>();
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableCannedDiverMessage;
            System.out.println("Local url " + url);
            mDataHolder = mActivity.mDbHelper.read(url);

            if (mDataHolder != null) {
                System.out.println("Local Device List " + url + " : " + mDataHolder.get_Listholder().size());
                for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
                    VoDiverMessage mVoDiverMessage = new VoDiverMessage();
                    mVoDiverMessage.setId(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDiverMessageId));
                    mVoDiverMessage.setDriver_message_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDiverMessageMSGId));
                    mVoDiverMessage.setMessage(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDiverMessageMessage));
                    mVoDiverMessage.setIs_emergency(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDiverMessageIsEmergency));
                    mVoDiverMessage.setCreated_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDiverMessageCreatedDate));
                    mVoDiverMessage.setUpdated_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDiverMessageUpdatedDate));
                    mVoDiverMessage.setIs_sync(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDiverMessageIsSync));
                    mArrayListDiverMessage.add(mVoDiverMessage);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        mMessageAdapter = new MessageAdapter();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
        mRecyclerViewMessage.setLayoutManager(mLayoutManager);
        mRecyclerViewMessage.setAdapter(mMessageAdapter);

        mMessageAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkAdapterIsEmpty();
            }
        });
        checkAdapterIsEmpty();
        isCalling = false;
    }

    private void checkAdapterIsEmpty() {
        if (mMessageAdapter.getItemCount() == 0) {
            mTextViewNoMessageFound.setVisibility(View.VISIBLE);
            mRecyclerViewMessage.setVisibility(View.GONE);
        } else {
            mTextViewNoMessageFound.setVisibility(View.GONE);
            mRecyclerViewMessage.setVisibility(View.VISIBLE);
        }
    }

    public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

        @Override
        public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_message_list_item, parent, false);
            return new MessageAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MessageAdapter.ViewHolder mViewHolder, final int position) {
            mViewHolder.mTextViewMessage.setText(mArrayListDiverMessage.get(position).getId() + ". " + mArrayListDiverMessage.get(position).getMessage());
            mViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mArrayListDiverMessage != null) {
                        if (position < mArrayListDiverMessage.size()) {
                            if (myDialog != null) {
                                if (myDialog.isShowing()) {
                                    myDialog.dismiss();
                                    if (mIntHeatPos == 1) {
                                        mTextViewHeatOne.setText(mArrayListDiverMessage.get(position).getMessage());
                                        mActivity.mPreferenceHelper.setDeviceHeat1Msg(mArrayListDiverMessage.get(position).getId());
                                    } else if (mIntHeatPos == 2) {
                                        mTextViewHeatTwo.setText(mArrayListDiverMessage.get(position).getMessage());
                                        mActivity.mPreferenceHelper.setDeviceHeat2Msg(mArrayListDiverMessage.get(position).getId());
                                    } else if (mIntHeatPos == 3) {
                                        mTextViewHeatThree.setText(mArrayListDiverMessage.get(position).getMessage());
                                        mActivity.mPreferenceHelper.setDeviceHeat3Msg(mArrayListDiverMessage.get(position).getId());
                                    } else if (mIntHeatPos == 4) {
                                        mTextViewHeatFour.setText(mArrayListDiverMessage.get(position).getMessage());
                                        mActivity.mPreferenceHelper.setDeviceHeat4Msg(mArrayListDiverMessage.get(position).getId());
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mArrayListDiverMessage.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.raw_message_list_item_textview_message)
            TextView mTextViewMessage;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        unbinder.unbind();
    }

}
