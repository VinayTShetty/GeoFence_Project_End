package com.succorfish.installer.fragment;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.succorfish.installer.MainActivity;
import com.succorfish.installer.MyApplication;
import com.succorfish.installer.R;
import com.succorfish.installer.Vo.VoQuestionAns;
import com.succorfish.installer.db.DataHolder;
import com.succorfish.installer.helper.PreferenceHelper;
import com.succorfish.installer.helper.URLCLASS;
import com.succorfish.installer.interfaces.onNewInstallationBackNext;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Jaydeep on 22-02-2018.
 */

public class FragmentHealthSafetyQuestionTwo extends Fragment implements View.OnClickListener {

    View mViewRoot;
    MainActivity mActivity;
    Button backBT;
    Button nextBT;
    Spinner mSpinnerHazards;
    AppCompatEditText mAppCompatEditTextHazardsAns;

    RecyclerView mRecyclerView;
    QuestionAdapter mQuestionAdapter;
    private OnStepTwoListener mListener;
    ArrayList<VoQuestionAns> mListQuestion = new ArrayList<>();
    List<String> mListHazards = new ArrayList<>();
    HazardsListAdapter mHazardsListAdapter;
    String mStringType = "0";

    public FragmentHealthSafetyQuestionTwo() {
        // Required empty public constructor
    }

    public static FragmentHealthSafetyQuestionTwo newInstance() {
        return new FragmentHealthSafetyQuestionTwo();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_health_safety_ques_two, container, false);

        backBT = mViewRoot.findViewById(R.id.backBT);
        nextBT = mViewRoot.findViewById(R.id.nextBT);
        backBT.setVisibility(View.VISIBLE);
        mRecyclerView = (RecyclerView) mViewRoot.findViewById(R.id.frg_health_safety_ques_two_recyclerView);
        mSpinnerHazards = (Spinner) mViewRoot.findViewById(R.id.frg_health_safety_ques_two_spinner_hazards);
        mAppCompatEditTextHazardsAns = (AppCompatEditText) mViewRoot.findViewById(R.id.frg_health_safety_ques_two_et_hazards_comment);
        mListQuestion = new ArrayList<>();
        mListQuestion.add(mActivity.mListQuestion.get(3));
        mListQuestion.add(mActivity.mListQuestion.get(4));
        mListHazards = Arrays.asList(getResources().getStringArray(R.array.hazards_list));
        mHazardsListAdapter = new HazardsListAdapter();
        mSpinnerHazards.setAdapter(mHazardsListAdapter);
        mSpinnerHazards.setSelection(mActivity.mListQuestion.get(5).getChooseAns(), false);
        try {
            mAppCompatEditTextHazardsAns.setText(mActivity.mListQuestion.get(5).getAnsComment());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mQuestionAdapter = new QuestionAdapter();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mQuestionAdapter);
        mSpinnerHazards.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mActivity.mUtility.hideKeyboard(mActivity);
                return false;
            }
        });
        mActivity.setNewInstallationBackNext(new onNewInstallationBackNext() {
            @Override
            public void onInstallFirstBack(Fragment fragment) {

            }

            @Override
            public void onInstallFirstNext(Fragment fragment) {
                System.out.println("TWO FirstNExt");
                mActivity.mViewPagerQuestion.setCurrentItem(1, true);
                mListQuestion = new ArrayList<>();
                mListQuestion.add(mActivity.mListQuestion.get(3));
                mListQuestion.add(mActivity.mListQuestion.get(4));
                mSpinnerHazards.setSelection(mActivity.mListQuestion.get(5).getChooseAns(), false);
                try {
                    mAppCompatEditTextHazardsAns.setText(mActivity.mListQuestion.get(5).getAnsComment());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onInstallSecondBack(Fragment fragment) {
                System.out.println("TWO SecondBAck");
                mActivity.mViewPagerQuestion.setCurrentItem(0, true);
            }

            @Override
            public void onInstallSecondNext(Fragment fragment) {
                System.out.println("SecondFinish");
            }

            @Override
            public void onInstallThirdBack(Fragment fragment) {

            }

            @Override
            public void onInstallThirdComplete(Fragment fragment) {

            }
        });
        return mViewRoot;
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.backBT:
                /* on Back save all data*/
                mActivity.mUtility.hideKeyboard(mActivity);
                mActivity.mListQuestion.set(3, mListQuestion.get(0));
                mActivity.mListQuestion.set(4, mListQuestion.get(1));
                mActivity.mListQuestion.get(5).setChooseAns((byte) mSpinnerHazards.getSelectedItemPosition());
                mActivity.mListQuestion.get(5).setAnsComment(mAppCompatEditTextHazardsAns.getText().toString().trim());
                mActivity.mListQuestion.get(5).setAnsText(mSpinnerHazards.getSelectedItem().toString());
                if (mListener != null)
                    mListener.onBackTwoPressed(this);
                break;

            case R.id.nextBT:
                /*on next check all valid input data*/
                mActivity.mUtility.hideKeyboard(mActivity);
                for (int i = 0; i < 2; i++) {
                    if (mListQuestion.get(i).getChooseAns() == 2) {
                        if (mListQuestion.get(i).getAnsComment().equalsIgnoreCase("")) {
                            mActivity.mUtility.errorDialog("Please explain the reason for Question " + mListQuestion.get(i).getQuestionNo());
                            return;
                        }
                    }
                }
                if (mAppCompatEditTextHazardsAns.getText().toString().trim().equalsIgnoreCase("")) {
                    mActivity.mUtility.errorDialog("Please describe additional precaution.");
                    return;
                }
                mActivity.mListQuestion.set(3, mListQuestion.get(0));
                mActivity.mListQuestion.set(4, mListQuestion.get(1));
                mActivity.mListQuestion.get(5).setChooseAns((byte) mSpinnerHazards.getSelectedItemPosition());
                mActivity.mListQuestion.get(5).setAnsComment(mAppCompatEditTextHazardsAns.getText().toString().trim());
                mActivity.mListQuestion.get(5).setAnsText(mSpinnerHazards.getSelectedItem().toString());
                mStringType = "0";
//                mActivity.removeAllFragmentFromBack();
                if (mActivity.mInstallUnInstallInspectStatus == 1) {
                    mStringType = "1";
                    saveUnInstallData();
                } else if (mActivity.mInstallUnInstallInspectStatus == 2) {
                    mStringType = "2";
                    saveInspectionData();
                } else {
                    mStringType = "0";
                    saveInstallData();
                }
                if (mListener != null) {
                    mListener.onNextTwoPressed(this);
                }
                break;
        }
    }

    /*Save inspection data in local db*/
    private void saveInspectionData() {
        Calendar cal = Calendar.getInstance();
        ContentValues mContentValues = new ContentValues();
        mContentValues.put(mActivity.mDbHelper.mFieldInspectionUserId, PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserId());
        mContentValues.put(mActivity.mDbHelper.mFieldInspectionUpdatedDate, cal.getTimeInMillis() + "");

        if (mActivity.mIntInspectionId == 0) {
            mContentValues.put(mActivity.mDbHelper.mFieldInspectionCreatedDate, cal.getTimeInMillis() + "");
            mActivity.mIntInspectionId = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableInspection, mContentValues);
        } else {
            String isExistInDB = CheckRecordExistInInspectionDB(mActivity.mIntInspectionId + "");
            if (isExistInDB.equalsIgnoreCase("-1")) {
                mContentValues.put(mActivity.mDbHelper.mFieldInspectionCreatedDate, cal.getTimeInMillis() + "");
                mActivity.mIntInspectionId = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableInspection, mContentValues);
            } else {
                String[] mArray = new String[]{isExistInDB};
                mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInspection, mContentValues, mActivity.mDbHelper.mFieldInspectionLocalId + "=?", mArray);
                System.out.println("Device Inspection updated In Local Db");
            }
        }
        addUpdateQuestionAns(mActivity.mIntInspectionId + "");
        mActivity.getUnSyncedCount();
        mActivity.replacesFragment(new FragmentInspections(), true, null, 1);
    }

    /*Save un installation data in local db*/
    private void saveUnInstallData() {
        Calendar cal = Calendar.getInstance();
        ContentValues mContentValues = new ContentValues();
        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallUserId, PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserId());
        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallUpdatedDate, cal.getTimeInMillis() + "");

        if (mActivity.mIntUnInstallationId == 0) {
            mContentValues.put(mActivity.mDbHelper.mFieldUnInstallCreatedDate, cal.getTimeInMillis() + "");
            mActivity.mIntUnInstallationId = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableUnInstall, mContentValues);
        } else {
            String isExistInDB = CheckRecordExistInUnInstallDB(mActivity.mIntUnInstallationId + "");
            if (isExistInDB.equalsIgnoreCase("-1")) {
                mContentValues.put(mActivity.mDbHelper.mFieldUnInstallCreatedDate, cal.getTimeInMillis() + "");
                mActivity.mIntUnInstallationId = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableUnInstall, mContentValues);
            } else {
                String[] mArray = new String[]{isExistInDB};
                mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableUnInstall, mContentValues, mActivity.mDbHelper.mFieldUnInstallLocalId + "=?", mArray);
                System.out.println("Device updated In Local Db");
            }
        }
        addUpdateQuestionAns(mActivity.mIntUnInstallationId + "");
        mActivity.getUnSyncedCount();
        mActivity.replacesFragment(new FragmentUninstall(), true, null, 1);
    }

    /*Save installation data in local db*/
    private void saveInstallData() {
        Calendar cal = Calendar.getInstance();
        ContentValues mContentValues = new ContentValues();
        mContentValues.put(mActivity.mDbHelper.mFieldInstallUserId, PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserId());
        mContentValues.put(mActivity.mDbHelper.mFieldInstallUpdatedDate, cal.getTimeInMillis() + "");
        if (mActivity.mIntInstallationId == 0) {
            mContentValues.put(mActivity.mDbHelper.mFieldInstallCreatedDate, cal.getTimeInMillis() + "");
            mActivity.mIntInstallationId = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableInstall, mContentValues);
        } else {
            String isExistInDB = CheckRecordExistInInstallDB(mActivity.mIntInstallationId + "");
            if (isExistInDB.equalsIgnoreCase("-1")) {
                mContentValues.put(mActivity.mDbHelper.mFieldInstallCreatedDate, cal.getTimeInMillis() + "");
                mActivity.mIntInstallationId = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableInstall, mContentValues);
            } else {
                String[] mArray = new String[]{isExistInDB};
                mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInstall, mContentValues, mActivity.mDbHelper.mFieldInstallLocalId + "=?", mArray);
                System.out.println("Device updated In Local Db");
            }
        }
        addUpdateQuestionAns(mActivity.mIntInstallationId + "");

        mActivity.getUnSyncedCount();
        mActivity.replacesFragment(new FragmentNewInstallation(), true, null, 1);
    }

    /*Save Install, uninstall, and inspection question and ans*/
    private void addUpdateQuestionAns(String localInsUnInsInspID) {
        Gson gson = new Gson();
        Calendar cal = Calendar.getInstance();
        ContentValues mContentValuesQues = new ContentValues();
        mContentValuesQues.put(mActivity.mDbHelper.mFieldQuesAnsUserID, PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserId());
        mContentValuesQues.put(mActivity.mDbHelper.mFieldQuesAnsInsUninsInspType, mStringType);
        mContentValuesQues.put(mActivity.mDbHelper.mFieldQuesAnsInsUninsInspLocalID, localInsUnInsInspID);
        mContentValuesQues.put(mActivity.mDbHelper.mFieldQuesAnsInsUninsInspServerID, "");
        mContentValuesQues.put(mActivity.mDbHelper.mFieldQuesAnsText, gson.toJson(mActivity.mListQuestion));
        mContentValuesQues.put(mActivity.mDbHelper.mFieldQuesAnsUpdatedDate, cal.getTimeInMillis() + "");
        mContentValuesQues.put(mActivity.mDbHelper.mFieldQuesAnsIsSync, "0");
        String isExistInDB = CheckRecordExistInQuestionAnsDB(String.valueOf(localInsUnInsInspID), mStringType);
        if (isExistInDB.equalsIgnoreCase("-1")) {
            mContentValuesQues.put(mActivity.mDbHelper.mFieldQuesAnsCreatedDate, cal.getTimeInMillis() + "");
            mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableQuestionAnswer, mContentValuesQues);

        } else {
            String[] mArray = new String[]{isExistInDB};
            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableQuestionAnswer, mContentValuesQues, mActivity.mDbHelper.mFieldQuesAnsLocalID + "=?", mArray);
        }
    }

    /*Check record exist in install table or not*/
    private String CheckRecordExistInInstallDB(String localInstallId) {
        DataHolder mDataHolder = new DataHolder();
        String url = "select * from " + mActivity.mDbHelper.mTableInstall + " where " + mActivity.mDbHelper.mFieldInstallLocalId + "= '" + localInstallId + "'";
        mDataHolder = mActivity.mDbHelper.read(url);
        if (mDataHolder != null) {
            System.out.println(" Install List : " + url + " : " + mDataHolder.get_Listholder().size());
            if (mDataHolder != null && mDataHolder.get_Listholder().size() != 0) {
                return mDataHolder.get_Listholder().get(0).get(mActivity.mDbHelper.mFieldInstallLocalId);
            } else {
                return "-1";
            }
        }
        return "-1";
    }

    /*Check record exist in un install table or not*/
    private String CheckRecordExistInUnInstallDB(String localUnInstallId) {
        DataHolder mDataHolder = new DataHolder();
        String url = "select * from " + mActivity.mDbHelper.mTableUnInstall + " where " + mActivity.mDbHelper.mFieldUnInstallLocalId + "= '" + localUnInstallId + "'";
        mDataHolder = mActivity.mDbHelper.read(url);
        if (mDataHolder != null) {
            System.out.println(" Install List : " + url + " : " + mDataHolder.get_Listholder().size());
            if (mDataHolder != null && mDataHolder.get_Listholder().size() != 0) {
                return mDataHolder.get_Listholder().get(0).get(mActivity.mDbHelper.mFieldUnInstallLocalId);
            } else {
                return "-1";
            }
        }
        return "-1";
    }

    /*Check record exist in inspection table or not*/
    private String CheckRecordExistInInspectionDB(String localInspId) {
        DataHolder mDataHolder = new DataHolder();
        String url = "select * from " + mActivity.mDbHelper.mTableInspection + " where " + mActivity.mDbHelper.mFieldInspectionLocalId + "= '" + localInspId + "'";
        mDataHolder = mActivity.mDbHelper.read(url);
        if (mDataHolder != null) {
            System.out.println(" Install List : " + url + " : " + mDataHolder.get_Listholder().size());
            if (mDataHolder != null && mDataHolder.get_Listholder().size() != 0) {
                return mDataHolder.get_Listholder().get(0).get(mActivity.mDbHelper.mFieldInspectionLocalId);
            } else {
                return "-1";
            }
        }
        return "-1";
    }

    /*Check record exist in question and ans table or not*/
    public String CheckRecordExistInQuestionAnsDB(String localId, String Type) {
        DataHolder mDataHolder = new DataHolder();
        String url = "select * from " + mActivity.mDbHelper.mTableQuestionAnswer + " where " + mActivity.mDbHelper.mFieldQuesAnsInsUninsInspLocalID + "= '" + localId + "'" + " AND " + mActivity.mDbHelper.mFieldQuesAnsInsUninsInspType + "= '" + Type + "'";
        mDataHolder = mActivity.mDbHelper.read(url);
        if (mDataHolder != null) {
            System.out.println(" Question Photo List : " + url + " : " + mDataHolder.get_Listholder().size());
            if (mDataHolder != null && mDataHolder.get_Listholder().size() != 0) {
                return mDataHolder.get_Listholder().get(0).get(mActivity.mDbHelper.mFieldQuesAnsLocalID);
            } else {
                return "-1";
            }
        }
        return "-1";
    }

    @Override
    public void onResume() {
        super.onResume();
        backBT.setOnClickListener(this);
        nextBT.setOnClickListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        backBT.setOnClickListener(null);
        nextBT.setOnClickListener(null);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            if (context instanceof OnStepTwoListener) {
                mListener = (OnStepTwoListener) context;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnStepTwoListener {
        void onBackTwoPressed(Fragment fragment);

        void onNextTwoPressed(Fragment fragment);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        backBT = null;
        nextBT = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mActivity.mUtility.hideKeyboard(mActivity);
    }

    /*Question adapter*/
    public class QuestionAdapter extends RecyclerView.Adapter<FragmentHealthSafetyQuestionTwo.QuestionAdapter.ViewHolder> {

        @Override
        public FragmentHealthSafetyQuestionTwo.QuestionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_health_safety_questions, parent, false);
            return new FragmentHealthSafetyQuestionTwo.QuestionAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final FragmentHealthSafetyQuestionTwo.QuestionAdapter.ViewHolder mViewHolder, final int position) {

            mViewHolder.mTextViewQuestionNo.setText("Q" + mListQuestion.get(position).getQuestionNo() + ".");
            mViewHolder.mAppCompatEditTextComment.setText(mListQuestion.get(position).getAnsComment());
            if (mListQuestion.get(position).getQuestionName() != null && !mListQuestion.get(position).getQuestionName().equalsIgnoreCase("")) {
                mViewHolder.mTextViewQuestionName.setText(mListQuestion.get(position).getQuestionName());
            } else {
                mViewHolder.mTextViewQuestionName.setText("NA");
            }
            if (mListQuestion.get(position).getQuestionType() == 1) {
                if (mListQuestion.get(position).getAnsDisplayOption() == 3) {
                    ((RadioButton) mViewHolder.mRadioGroupAns.getChildAt(2)).setVisibility(View.VISIBLE);
                } else {
                    ((RadioButton) mViewHolder.mRadioGroupAns.getChildAt(2)).setVisibility(View.GONE);
                }
                if (mListQuestion.get(position).getChooseAns() == 2) {
                    ((RadioButton) mViewHolder.mRadioGroupAns.getChildAt(1)).setChecked(true);
                    mViewHolder.mAppCompatEditTextComment.setVisibility(View.VISIBLE);
                    mViewHolder.mAppCompatEditTextComment.setText(mListQuestion.get(position).getAnsComment());
                    mListQuestion.get(position).setAnsText(((RadioButton) mViewHolder.mRadioGroupAns.getChildAt(1)).getText().toString());
                } else if (mListQuestion.get(position).getChooseAns() == 3) {
                    ((RadioButton) mViewHolder.mRadioGroupAns.getChildAt(2)).setChecked(true);
                    mListQuestion.get(position).setAnsText(((RadioButton) mViewHolder.mRadioGroupAns.getChildAt(2)).getText().toString());
                } else {
                    ((RadioButton) mViewHolder.mRadioGroupAns.getChildAt(0)).setChecked(true);
                    mListQuestion.get(position).setAnsText(((RadioButton) mViewHolder.mRadioGroupAns.getChildAt(0)).getText().toString());
                }
            }
            mViewHolder.mRadioGroupAns.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    mActivity.mUtility.hideKeyboard(mActivity);
                    mViewHolder.mAppCompatEditTextComment.setVisibility(View.GONE);
                    if (checkedId == R.id.raw_health_safety_rb_ans_no) {
                        mListQuestion.get(position).setChooseAns((byte) 2);
                        mViewHolder.mAppCompatEditTextComment.setVisibility(View.VISIBLE);
                        mViewHolder.mAppCompatEditTextComment.setText(mListQuestion.get(position).getAnsComment());
                        mListQuestion.get(position).setAnsText(((RadioButton) mViewHolder.mRadioGroupAns.getChildAt(1)).getText().toString());
                    } else if (checkedId == R.id.raw_health_safety_rb_ans_na) {
                        mListQuestion.get(position).setChooseAns((byte) 3);
                        mListQuestion.get(position).setAnsText(((RadioButton) mViewHolder.mRadioGroupAns.getChildAt(2)).getText().toString());
                    } else {
                        mListQuestion.get(position).setChooseAns((byte) 1);
                        mListQuestion.get(position).setAnsText(((RadioButton) mViewHolder.mRadioGroupAns.getChildAt(0)).getText().toString());
                    }
                }
            });
            mViewHolder.mAppCompatEditTextComment.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    mListQuestion.get(position).setAnsComment(mViewHolder.mAppCompatEditTextComment.getText().toString().trim());
                }
            });
        }

        @Override
        public int getItemCount() {
            return mListQuestion == null ? 0 : mListQuestion.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.raw_health_safety_tv_qus_no)
            TextView mTextViewQuestionNo;
            @BindView(R.id.raw_health_safety_tv_qus)
            TextView mTextViewQuestionName;
            @BindView(R.id.raw_health_safety_rg_ans)
            RadioGroup mRadioGroupAns;
            @BindView(R.id.raw_health_safety_et_ans_comment)
            AppCompatEditText mAppCompatEditTextComment;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

    /*Hazard list adapter*/
    public class HazardsListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mListHazards.size();
        }

        @Override
        public Object getItem(int position) {
            return mListHazards.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.raw_autocomplete_textview, parent, false);
            }
            TextView mTextViewCategoryName = (TextView) view.findViewById(R.id.autocomplete_tv_city_name);
            mTextViewCategoryName.setText(mListHazards.get(position));
            return view;
        }
    }
}
