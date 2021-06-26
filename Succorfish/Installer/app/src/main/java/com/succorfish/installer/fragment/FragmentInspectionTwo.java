package com.succorfish.installer.fragment;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.succorfish.installer.MainActivity;
import com.succorfish.installer.R;
import com.succorfish.installer.Vo.VoInspection;
import com.succorfish.installer.db.DataHolder;
import com.succorfish.installer.interfaces.onNewInstallationBackNext;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Jaydeep on 22-02-2018.
 */

public class FragmentInspectionTwo extends Fragment implements View.OnClickListener {

    View mViewRoot;
    MainActivity mActivity;
    Button backBT;
    Button nextBT;
    EditText mEditTextOwnerName;
    EditText mEditTextOwnerAddress;
    EditText mEditTextOwnerCity;
    EditText mEditTextOwnerState;
    EditText mEditTextOwnerZipcode;
    EditText mEditTextOwnerEmail;
    EditText mEditTextOwnerPhoneNo;
    String mStringOwnerName = "";
    String mStringOwnerAddress = "";
    String mStringOwnerCity = "";
    String mStringOwnerState = "";
    String mStringOwnerZipcode = "";
    String mStringOwnerEmail = "";
    String mStringOwnerPhoneNo = "";

    private OnStepTwoListener mListener;
    private long mLastClickTime = 0;
    VoInspection mVoInspection;
    boolean isAlradyFillData = false;

    public FragmentInspectionTwo() {
        // Required empty public constructor
    }

    public static FragmentInspectionTwo newInstance() {
        return new FragmentInspectionTwo();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_inspection_two, container, false);

        backBT = mViewRoot.findViewById(R.id.backBT);
        nextBT = mViewRoot.findViewById(R.id.nextBT);
        mEditTextOwnerName = (EditText) mViewRoot.findViewById(R.id.fragment_inspection_two_editext_owner_name);
        mEditTextOwnerAddress = (EditText) mViewRoot.findViewById(R.id.fragment_inspection_two_editext_address_one);
        mEditTextOwnerCity = (EditText) mViewRoot.findViewById(R.id.fragment_inspection_two_editext_owner_city);
        mEditTextOwnerState = (EditText) mViewRoot.findViewById(R.id.fragment_inspection_two_editext_owner_state);
        mEditTextOwnerZipcode = (EditText) mViewRoot.findViewById(R.id.fragment_inspection_two_editext_owner_zipcode);
        mEditTextOwnerEmail = (EditText) mViewRoot.findViewById(R.id.fragment_inspection_two_editext_owner_email);
        mEditTextOwnerPhoneNo = (EditText) mViewRoot.findViewById(R.id.fragment_inspection_two_editext_owner_phoneno);


        mActivity.setNewInstallationBackNext(new onNewInstallationBackNext() {
            @Override
            public void onInstallFirstBack(Fragment fragment) {

            }

            @Override
            public void onInstallFirstNext(Fragment fragment) {
                System.out.println("FirstNExt");
                mActivity.mViewPagerInspection.setCurrentItem(1, true);
                if (mActivity.mIntInspectionId != 0 && !isAlradyFillData) {
                    System.out.println("From Edit");
                    isAlradyFillData = true;
                    getDBInspectionList(true);
                } else {
                    getDBInspectionList(false);
                }

            }

            @Override
            public void onInstallSecondBack(Fragment fragment) {
                System.out.println("SecondBAck");
                mActivity.mViewPagerInspection.setCurrentItem(0, true);
            }

            @Override
            public void onInstallSecondNext(Fragment fragment) {
                System.out.println("SecondNext");
                mActivity.mViewPagerInspection.setCurrentItem(2, true);
            }

            @Override
            public void onInstallThirdBack(Fragment fragment) {
                System.out.println("ThirdBAck");
                mActivity.mViewPagerInspection.setCurrentItem(1, true);
            }

            @Override
            public void onInstallThirdComplete(Fragment fragment) {
                System.out.println("ThirdNext");

            }
        });
        return mViewRoot;
    }

    /*get Inspection data from local db*/
    private void getDBInspectionList(boolean isFromEdit) {
        DataHolder mDataHolder;
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableInspection + " where " + mActivity.mDbHelper.mFieldInspectionIsSync + "= 0" + " AND " + mActivity.mDbHelper.mFieldInspectionLocalId + "= '" + mActivity.mIntInspectionId + "'";
            System.out.println("Local url " + url);
            mDataHolder = mActivity.mDbHelper.read(url);
            if (mDataHolder != null) {
                System.out.println("Local Device List " + url + " : " + mDataHolder.get_Listholder().size());
                for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
                    mVoInspection = new VoInspection();
                    mVoInspection.setInsp_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionLocalId));
                    mVoInspection.setInsp_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionServerId));
                    mVoInspection.setInsp_user_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionUserId));
                    mVoInspection.setInsp_device_imei_no(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionDeviceIMEINo));
                    mVoInspection.setInsp_device_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionDeviceServerId));
                    mVoInspection.setInsp_device_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionDeviceLocalId));
                    mVoInspection.setInsp_device_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionDevicName));
                    mVoInspection.setInsp_device_warranty_status(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionDeviceWarranty_status));
                    mVoInspection.setInsp_device_type_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionDeviceTypeName));
                    mVoInspection.setInsp_vessel_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionVesselLocalId));
                    mVoInspection.setInsp_vessel_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionVesselServerId));
                    mVoInspection.setInsp_vessel_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionVesselName));
                    mVoInspection.setInsp_vessel_regi_no(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionVesselRegNo));
                    mVoInspection.setInsp_owner_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionOwnerName));
                    mVoInspection.setInsp_owner_address(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionOwnerAddress));
                    mVoInspection.setInsp_owner_city(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionOwnerCity));
                    mVoInspection.setInsp_owner_state(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionOwnerState));
                    mVoInspection.setInsp_owner_zipcode(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionOwnerZipcode));
                    mVoInspection.setInsp_owner_email(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionOwnerEmail));
                    mVoInspection.setInsp_owner_mobile_no(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionOwnerMobileNo));
                    mVoInspection.setInsp_result(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionResult));
                    mVoInspection.setInsp_action_taken(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionActionTaken));
                    mVoInspection.setInsp_warranty_return(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionWarrentyReturn));
                    mVoInspection.setInsp_local_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionLocalSignUrl));
                    mVoInspection.setInsp_server_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionServerSignUrl));
                    mVoInspection.setInsp_local_inspector_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionLocalInspectorSignUrl));
                    mVoInspection.setInsp_server_inspector_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionServerInspectorSignUrl));
                    mVoInspection.setInsp_created_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionCreatedDate));
                    mVoInspection.setInsp_updated_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionUpdatedDate));
                    mVoInspection.setInsp_is_sync(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionIsSync));
                    mVoInspection.setInsp_status(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionStatus));
                    mVoInspection.setInsp_pdf_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionPdfUrl));
                    mVoInspection.setInsp_date_time(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionDateTime));
                    mVoInspection.setInsp_date_timestamp(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionDateTimeStamp));
                    mVoInspection.setInsp_owner_sign_sync(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionIsSyncOwnerSign));
                    mVoInspection.setInsp_installer_sign_sync(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionIsSyncInstallerSign));

                    if (isFromEdit) {
                        mStringOwnerName = mVoInspection.getInsp_owner_name();
                        mStringOwnerAddress = mVoInspection.getInsp_owner_address();
                        mStringOwnerCity = mVoInspection.getInsp_owner_city();
                        mStringOwnerState = mVoInspection.getInsp_owner_state();
                        mStringOwnerZipcode = mVoInspection.getInsp_owner_zipcode();
                        mStringOwnerEmail = mVoInspection.getInsp_owner_email();
                        mStringOwnerPhoneNo = mVoInspection.getInsp_owner_mobile_no();

                        mEditTextOwnerName.setText(mStringOwnerName);
                        mEditTextOwnerAddress.setText(mStringOwnerAddress);
                        mEditTextOwnerEmail.setText(mStringOwnerEmail);
                        mEditTextOwnerPhoneNo.setText(mStringOwnerPhoneNo);
                        mEditTextOwnerCity.setText(mStringOwnerCity);
                        mEditTextOwnerState.setText(mStringOwnerState);
                        mEditTextOwnerZipcode.setText(mStringOwnerZipcode);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    /*Save data on back press*/
    public void onBackPressCall(MainActivity mActivity) {
        mStringOwnerName = mEditTextOwnerName.getText().toString().trim();
        mStringOwnerAddress = mEditTextOwnerAddress.getText().toString().trim();
        mStringOwnerEmail = mEditTextOwnerEmail.getText().toString().trim();
        mStringOwnerPhoneNo = mEditTextOwnerPhoneNo.getText().toString().trim();

        mStringOwnerCity = mEditTextOwnerCity.getText().toString().trim();
        mStringOwnerState = mEditTextOwnerState.getText().toString().trim();
        mStringOwnerZipcode = mEditTextOwnerZipcode.getText().toString().trim();

        Calendar calBack = Calendar.getInstance();
        ContentValues mContentValuesBack = new ContentValues();
        mContentValuesBack.put(mActivity.mDbHelper.mFieldInspectionOwnerName, mStringOwnerName);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldInspectionOwnerAddress, mStringOwnerAddress);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldInspectionOwnerCity, mStringOwnerCity);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldInspectionOwnerState, mStringOwnerState);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldInspectionOwnerZipcode, mStringOwnerZipcode);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldInspectionOwnerEmail, mStringOwnerEmail);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldInspectionOwnerMobileNo, mStringOwnerPhoneNo);

        String isExistInDBBack = CheckRecordExistInInspectionDB(mActivity.mIntInspectionId + "");
        if (isExistInDBBack.equalsIgnoreCase("-1")) {
            mContentValuesBack.put(mActivity.mDbHelper.mFieldInspectionCreatedDate, calBack.getTimeInMillis() + "");
            int isInsertInstall = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableInspection, mContentValuesBack);
            if (isInsertInstall != -1) {
                System.out.println("Device Inspection Added In Local Db");
            } else {
                System.out.println("Device Inspection Adding In Local DB");
            }
            mActivity.mIntInspectionId = isInsertInstall;
        } else {
            String[] mArray = new String[]{isExistInDBBack};
            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInspection, mContentValuesBack, mActivity.mDbHelper.mFieldInspectionLocalId + "=?", mArray);
            System.out.println("Device Inspection updated In Local Db");
        }
        System.out.println("Device updated In Local Db");
        System.out.println("2-BACK-JD");
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.backBT:
                mStringOwnerName = mEditTextOwnerName.getText().toString().trim();
                mStringOwnerAddress = mEditTextOwnerAddress.getText().toString().trim();
                mStringOwnerEmail = mEditTextOwnerEmail.getText().toString().trim();
                mStringOwnerPhoneNo = mEditTextOwnerPhoneNo.getText().toString().trim();

                mStringOwnerCity = mEditTextOwnerCity.getText().toString().trim();
                mStringOwnerState = mEditTextOwnerState.getText().toString().trim();
                mStringOwnerState = mEditTextOwnerZipcode.getText().toString().trim();

                Calendar calBack = Calendar.getInstance();
                ContentValues mContentValuesBack = new ContentValues();
                mContentValuesBack.put(mActivity.mDbHelper.mFieldInspectionOwnerName, mStringOwnerName);
                mContentValuesBack.put(mActivity.mDbHelper.mFieldInspectionOwnerAddress, mStringOwnerAddress);
                mContentValuesBack.put(mActivity.mDbHelper.mFieldInspectionOwnerCity, mStringOwnerCity);
                mContentValuesBack.put(mActivity.mDbHelper.mFieldInspectionOwnerState, mStringOwnerState);
                mContentValuesBack.put(mActivity.mDbHelper.mFieldInspectionOwnerZipcode, mStringOwnerZipcode);
                mContentValuesBack.put(mActivity.mDbHelper.mFieldInspectionOwnerEmail, mStringOwnerEmail);
                mContentValuesBack.put(mActivity.mDbHelper.mFieldInspectionOwnerMobileNo, mStringOwnerPhoneNo);

                String isExistInDBBack = CheckRecordExistInInspectionDB(mActivity.mIntInspectionId + "");
                if (isExistInDBBack.equalsIgnoreCase("-1")) {
                    mContentValuesBack.put(mActivity.mDbHelper.mFieldInspectionCreatedDate, calBack.getTimeInMillis() + "");
                    int isInsertInstall = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableInspection, mContentValuesBack);
                    if (isInsertInstall != -1) {
                        System.out.println("Device Inspection Added In Local Db");
                    } else {
                        System.out.println("Device Inspection Adding In Local DB");
                    }
                    mActivity.mIntInspectionId = isInsertInstall;
                } else {
                    String[] mArray = new String[]{isExistInDBBack};
                    mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInspection, mContentValuesBack, mActivity.mDbHelper.mFieldInspectionLocalId + "=?", mArray);
                    System.out.println("Device Inspection updated In Local Db");
                }
                if (mListener != null)
                    mListener.onBackTwoPressed(this);
                break;

            case R.id.nextBT:

                mStringOwnerName = mEditTextOwnerName.getText().toString().trim();
                mStringOwnerAddress = mEditTextOwnerAddress.getText().toString().trim();
                mStringOwnerEmail = mEditTextOwnerEmail.getText().toString().trim();
                mStringOwnerPhoneNo = mEditTextOwnerPhoneNo.getText().toString().trim();

                mStringOwnerCity = mEditTextOwnerCity.getText().toString().trim();
                mStringOwnerState = mEditTextOwnerState.getText().toString().trim();
                mStringOwnerZipcode = mEditTextOwnerZipcode.getText().toString().trim();


                if (mStringOwnerName.equalsIgnoreCase("")) {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_three_enter_owner_name));
                    return;
                }
                if (mStringOwnerAddress.equalsIgnoreCase("")) {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_three_enter_owner_address));
                    return;
                }
//                if (mStringOwnerCity.equalsIgnoreCase("")) {
//                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_three_enter_owner_city));
//                    return;
//                }
//                if (mStringOwnerState.equalsIgnoreCase("")) {
//                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_three_enter_owner_state));
//                    return;
//                }
                if (mStringOwnerZipcode.equalsIgnoreCase("")) {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_three_enter_owner_zipcode));
                    return;
                }
                if (mStringOwnerEmail.equalsIgnoreCase("")) {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_three_enter_owner_email));
                    return;
                }

                if (!mActivity.mUtility.isValidEmail(mStringOwnerEmail)) {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_sign_up_enter_valid_email_address));
                    return;
                }

                if (mStringOwnerPhoneNo.equalsIgnoreCase("")) {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_three_enter_owner_phonono));
                    return;
                }

                Calendar cal = Calendar.getInstance();
                ContentValues mContentValues = new ContentValues();
                mContentValues.put(mActivity.mDbHelper.mFieldInspectionOwnerName, mStringOwnerName);
                mContentValues.put(mActivity.mDbHelper.mFieldInspectionOwnerAddress, mStringOwnerAddress);
                mContentValues.put(mActivity.mDbHelper.mFieldInspectionOwnerCity, mStringOwnerCity);
                mContentValues.put(mActivity.mDbHelper.mFieldInspectionOwnerState, mStringOwnerState);
                mContentValues.put(mActivity.mDbHelper.mFieldInspectionOwnerZipcode, mStringOwnerZipcode);
                mContentValues.put(mActivity.mDbHelper.mFieldInspectionOwnerEmail, mStringOwnerEmail);
                mContentValues.put(mActivity.mDbHelper.mFieldInspectionOwnerMobileNo, mStringOwnerPhoneNo);

                String isExistInDB = CheckRecordExistInInspectionDB(mActivity.mIntInspectionId + "");
                if (isExistInDB.equalsIgnoreCase("-1")) {
                    mContentValues.put(mActivity.mDbHelper.mFieldInspectionCreatedDate, cal.getTimeInMillis() + "");
                    int isInsertInstall = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableInspection, mContentValues);
                    if (isInsertInstall != -1) {
                        System.out.println("Device Inspection Added In Local Db");
                    } else {
                        System.out.println("Device Inspection Adding In Local DB");
                    }
                    mActivity.mIntInspectionId = isInsertInstall;
                } else {
                    String[] mArray = new String[]{isExistInDB};
                    mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInspection, mContentValues, mActivity.mDbHelper.mFieldInspectionLocalId + "=?", mArray);
                    System.out.println("Device Inspection updated In Local Db");
                }
                if (mListener != null) {
                    mListener.onNextTwoPressed(this);
                }
                break;
        }
    }
    /*check record exist or not*/
    public String CheckRecordExistInInspectionDB(String localInspId) {
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
    }

}
