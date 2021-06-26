package com.succorfish.installer.fragment;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.succorfish.installer.MainActivity;
import com.succorfish.installer.MyApplication;
import com.succorfish.installer.R;
import com.succorfish.installer.Vo.VoInstallation;
import com.succorfish.installer.Vo.VoInstallationPhoto;
import com.succorfish.installer.db.DataHolder;
import com.succorfish.installer.helper.GPSTracker;
import com.succorfish.installer.helper.PreferenceHelper;
import com.succorfish.installer.interfaces.onBackPressWithAction;
import com.succorfish.installer.interfaces.onFragmentBackPress;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Jaydeep on 22-02-2018.
 */

public class FragmentNewInstallationTwo extends Fragment implements View.OnClickListener {

    View mViewRoot;
    MainActivity mActivity;

    public Button backBT;
    public Button nextBT;
    RelativeLayout mRelativeLayoutBottomSheet;
    RelativeLayout mRelativeLayoutInsDate;
    RelativeLayout mRelativeLayoutSelectPower;
    RelativeLayout mRelativeLayoutInstallationPhoto;
    LinearLayout mLinearLayoutVesselAsset;
    TextView mTextViewPhotoCount;
    TextView mTextViewSelectPower;
    TextView mTextViewInstallationDate;
    TextView mTextViewVesselName;
    TextView mTextViewVesselRegNo;
    EditText mEditTextLatitude;
    EditText mEditTextLongitude;
    EditText mEditTextLocation;
    String mStringSelectPower = "";
    String mStringInstallationDate = "";
    String mStringVesselID = "";
    String mStringVesselName = "";
    String mStringVesselRegiNo = "";
    String mStringLatitude = "";
    String mStringLongitude = "";
    String mStringLocation = "";
    private OnStepTwoListener mListener;
    ArrayList<String> mArrayListPowerSource = new ArrayList<>();
    PowerAdapter mPowerAdapter;
    BottomSheetBehavior mBottomSheetBehavior;
    private BottomSheetDialog mBottomSheetDialog;
    private Calendar newCalendar;
    public ArrayList<VoInstallationPhoto> mArrayListPhotoList = new ArrayList<>();
    //    private boolean isAllPhotoImageExis = true;
    int imageDisplayCount = 0;
    private SimpleDateFormat mSimpleDateFormatDateDisplay;

    public FragmentNewInstallationTwo() {
        // Required empty public constructor
    }

    public static FragmentNewInstallationTwo newInstance() {
        return new FragmentNewInstallationTwo();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        newCalendar = Calendar.getInstance();
        mStringInstallationDate = newCalendar.getTimeInMillis() + "";
        if (mActivity.mGpsTracker != null) {
            mStringLatitude = mActivity.mGpsTracker.getLatitude() + "";
            mStringLongitude = mActivity.mGpsTracker.getLongitude() + "";
        }
        if (mActivity.mIntInstallationId != 0) {
            System.out.println("From Edit");
            getDBInstallationList();
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_new_installation_two, container, false);
        backBT = mViewRoot.findViewById(R.id.backBT);
        nextBT = mViewRoot.findViewById(R.id.nextBT);
        mSimpleDateFormatDateDisplay = new SimpleDateFormat(PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedDateFormat() + " HH:mm", Locale.getDefault());

        mRelativeLayoutBottomSheet = (RelativeLayout) mViewRoot.findViewById(R.id.fragment_new_ins_two_relativelayout_bottomsheet);
        mRelativeLayoutInsDate = (RelativeLayout) mViewRoot.findViewById(R.id.fragment_new_ins_two_relativelayout_date);
        mRelativeLayoutSelectPower = (RelativeLayout) mViewRoot.findViewById(R.id.fragment_new_ins_two_relativelayout_select_power);
        mRelativeLayoutInstallationPhoto = (RelativeLayout) mViewRoot.findViewById(R.id.fragment_new_ins_two_relativelayout_installation_photo);

        mTextViewSelectPower = (TextView) mViewRoot.findViewById(R.id.fragment_new_ins_two_textview_select_power);
        mTextViewInstallationDate = (TextView) mViewRoot.findViewById(R.id.fragment_new_ins_two_textview_date);
        mTextViewVesselName = (TextView) mViewRoot.findViewById(R.id.fragment_new_ins_two_textview_vessel_asset_name);
        mTextViewVesselRegNo = (TextView) mViewRoot.findViewById(R.id.fragment_new_ins_two_textview_regi_no);
        mTextViewPhotoCount = (TextView) mViewRoot.findViewById(R.id.fragment_new_ins_two_textview_installation_photo);

        mEditTextLatitude = (EditText) mViewRoot.findViewById(R.id.fragment_new_ins_two_editext_latitude);
        mEditTextLongitude = (EditText) mViewRoot.findViewById(R.id.fragment_new_ins_two_editext_longitude);
        mEditTextLocation = (EditText) mViewRoot.findViewById(R.id.fragment_new_ins_two_editext_device_location);

        mLinearLayoutVesselAsset = (LinearLayout) mViewRoot.findViewById(R.id.fragment_new_ins_two_linearlayout_vessel_asset);
        mBottomSheetBehavior = BottomSheetBehavior.from(mRelativeLayoutBottomSheet);

        try {
            Calendar mCalendar = Calendar.getInstance();
            mCalendar.setTimeInMillis(Long.parseLong(mStringInstallationDate));
            mTextViewInstallationDate.setText(mSimpleDateFormatDateDisplay.format(mCalendar.getTime()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mStringLatitude != null && !mStringLatitude.equalsIgnoreCase("") && mStringLatitude.equalsIgnoreCase("0.0")) {
            if (mActivity.mGpsTracker != null) {
                mActivity.mGpsTracker = new GPSTracker(mActivity);
                mStringLatitude = mActivity.mGpsTracker.getLatitude() + "";
                mStringLongitude = mActivity.mGpsTracker.getLongitude() + "";
            }
        }
        if (mStringLongitude != null && !mStringLongitude.equalsIgnoreCase("") && mStringLongitude.equalsIgnoreCase("0.0")) {
            if (mActivity.mGpsTracker != null) {
                mStringLatitude = mActivity.mGpsTracker.getLatitude() + "";
                mStringLongitude = mActivity.mGpsTracker.getLongitude() + "";
            }
        }
        mEditTextLatitude.setText(mStringLatitude);
        mEditTextLongitude.setText(mStringLongitude);

        mEditTextLatitude.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        mEditTextLongitude.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        System.out.println("mStringVesselName-" + mStringVesselName);
        System.out.println("mStringVesselRegiNo-" + mStringVesselRegiNo);
        mTextViewVesselName.setText(mStringVesselName);
        mTextViewVesselRegNo.setText(mStringVesselRegiNo);
        mTextViewSelectPower.setText(mStringSelectPower);
        mEditTextLocation.setText(mStringLocation);
        getDBInstallationPhotoList();

        mArrayListPowerSource = new ArrayList<>();
        // WHILE CHANGE ALSO CHANGE IN ADD INSTALLATION API.
        mArrayListPowerSource.add("Constant 6-36V");
        mArrayListPowerSource.add("Periodic 6-36V");
        mArrayListPowerSource.add("Constant regulated 6-36V");
        mArrayListPowerSource.add("Periodic regulated 6-36V");
        mArrayListPowerSource.add("Solar");
        mArrayListPowerSource.add("Internal rechargeable battery");

//        mActivity.setOnBackFrgPress(new onFragmentBackPress() {
//            @Override
//            public void onFragmentBackPress(Fragment mFragment) {
//                if (mFragment instanceof FragmentNewInstallation) {
//                    System.out.println("BackKKTwo");
//                    backBT.performClick();
//                    int currentPage = mActivity.mViewPagerInstallation.getCurrentItem();
//                    if (currentPage == 1) {
//                        mStringLatitude = mEditTextLatitude.getText().toString().trim();
//                        mStringLongitude = mEditTextLongitude.getText().toString().trim();
//                        mStringLocation = mEditTextLocation.getText().toString().trim();
//                        Calendar calBack = Calendar.getInstance();
//                        ContentValues mContentValuesBack = new ContentValues();
//                        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallDateTime, mStringInstallationDate);
//                        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallLatitude, mStringLatitude);
//                        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallLongitude, mStringLongitude);
//                        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallCountryCode, "");
//                        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallCountryName, "");
//                        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallVesselLocalId, "");
//                        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallVesselServerId, mStringVesselID);
//                        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallVesselName, mStringVesselName);
//                        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallVesselRegNo, mStringVesselRegiNo);
//                        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallPower, mStringSelectPower);
//                        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallLocation, mStringLocation);
//                        String isExistInDBBack = CheckRecordExistInInstallDB(mActivity.mIntInstallationId + "");
//                        if (isExistInDBBack.equalsIgnoreCase("-1")) {
//                            mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallCreatedDate, calBack.getTime() + "");
//                            int isInsertInstall = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableInstall, mContentValuesBack);
//                            if (isInsertInstall != -1) {
//                                System.out.println("Device Install Added In Local Db");
//                            } else {
//                                System.out.println("Device Install Adding In Local DB");
//                            }
//                        } else {
//                            mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallUpdatedDate, calBack.getTime() + "");
//                            String[] mArray = new String[]{isExistInDBBack};
//                            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInstall, mContentValuesBack, mActivity.mDbHelper.mFieldInstallLocalId + "=?", mArray);
//                            System.out.println("Device updated In Local Db");
//                        }
//                    }
//                    mActivity.mViewPagerInstallation.setCurrentItem(0, true);
//                }
//            }
//        });

        return mViewRoot;
    }

    /*get installation record from database*/
    private void getDBInstallationList() {
        DataHolder mDataHolder;
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableInstall + " where " + mActivity.mDbHelper.mFieldInstallIsSync + "= 0" + " AND " + mActivity.mDbHelper.mFieldInstallLocalId + "= '" + mActivity.mIntInstallationId + "'";
            System.out.println("Local url " + url);
            mDataHolder = mActivity.mDbHelper.read(url);
            if (mDataHolder != null) {
                System.out.println("Local Device List " + url + " : " + mDataHolder.get_Listholder().size());
                for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
                    VoInstallation mVoInstallation = new VoInstallation();
                    mVoInstallation.setInst_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallLocalId));
                    mVoInstallation.setInst_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallServerId));
                    mVoInstallation.setInst_user_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallUserId));
                    mVoInstallation.setInst_device_imei_no(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallDeviceIMEINo));
                    mVoInstallation.setInst_device_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallDeviceServerId));
                    mVoInstallation.setInst_device_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallDeviceLocalId));
                    mVoInstallation.setInst_device_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallDevicName));
                    mVoInstallation.setInst_device_warranty_status(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallDeviceWarranty_status));
                    mVoInstallation.setInst_device_type_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallDeviceTypeName));
                    mVoInstallation.setInst_help_no(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallHelpNo));
                    mVoInstallation.setInst_date_time(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallDateTime));
                    mVoInstallation.setInst_latitude(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallLatitude));
                    mVoInstallation.setInst_longitude(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallLongitude));
                    mVoInstallation.setInst_country_code(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallCountryCode));
                    mVoInstallation.setInst_country_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallCountryName));
                    mVoInstallation.setInst_vessel_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallVesselLocalId));
                    mVoInstallation.setInst_vessel_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallVesselServerId));
                    mVoInstallation.setInst_vessel_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallVesselName));
                    mVoInstallation.setInst_vessel_regi_no(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallVesselRegNo));
                    mVoInstallation.setInst_power(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallPower));
                    mVoInstallation.setInst_location(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallLocation));
                    mVoInstallation.setInst_owner_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallOwnerName));
                    mVoInstallation.setInst_owner_address(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallOwnerAddress));
                    mVoInstallation.setInst_owner_city(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallOwnerCity));
                    mVoInstallation.setInst_owner_state(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallOwnerState));
                    mVoInstallation.setInst_owner_zipcode(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallOwnerZipcode));
                    mVoInstallation.setInst_owner_email(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallOwnerEmail));
                    mVoInstallation.setInst_owner_mobile_no(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallOwnerMobileNo));
                    mVoInstallation.setInst_local_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallLocalSignUrl));
                    mVoInstallation.setInst_server_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallServerSignUrl));
                    mVoInstallation.setInst_local_installer_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallLocalInstallerSignUrl));
                    mVoInstallation.setInst_server_installer_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallServerInstallerSignUrl));
                    mVoInstallation.setInst_pdf_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallPdfUrl));
                    mVoInstallation.setInst_created_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallCreatedDate));
                    mVoInstallation.setInst_updated_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallUpdatedDate));
                    mVoInstallation.setInst_is_sync(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallIsSync));
                    mVoInstallation.setIs_install(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallStatus));
                    mVoInstallation.setInst_date_timestamp(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallDateTimeStamp));
                    mStringInstallationDate = mVoInstallation.getInst_date_time();
                    mStringLatitude = mVoInstallation.getInst_latitude();
                    mStringLongitude = mVoInstallation.getInst_longitude();
                    mStringVesselName = mVoInstallation.getInst_vessel_name();
                    mStringVesselID = mVoInstallation.getInst_vessel_server_id();
                    mStringVesselRegiNo = mVoInstallation.getInst_vessel_regi_no();
                    mStringSelectPower = mVoInstallation.getInst_power();
                    mStringLocation = mVoInstallation.getInst_location();
                    if (mStringVesselRegiNo == null || mStringVesselRegiNo.equalsIgnoreCase("") || mStringVesselRegiNo.equalsIgnoreCase("null")) {
                        mStringVesselRegiNo = "NA";
                    }
                    if (mStringInstallationDate == null || mStringInstallationDate.equalsIgnoreCase("")) {
                        mStringInstallationDate = newCalendar.getTimeInMillis() + "";
                    }
                    if (mStringInstallationDate != null && !mStringInstallationDate.equalsIgnoreCase("") && !mStringInstallationDate.equalsIgnoreCase("null")) {
                        try {
                            newCalendar.setTimeInMillis(Long.parseLong(mStringInstallationDate));
                            mStringInstallationDate = newCalendar.getTimeInMillis() + "";
                            mTextViewInstallationDate.setText(mSimpleDateFormatDateDisplay.format(newCalendar.getTime()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (mStringLatitude == null || mStringLatitude.equalsIgnoreCase("")) {
                        if (mActivity.mGpsTracker != null) {
                            mStringLatitude = mActivity.mGpsTracker.getLatitude() + "";
                        }
                    }
                    if (mStringLongitude == null || mStringLongitude.equalsIgnoreCase("")) {
                        if (mActivity.mGpsTracker != null) {
                            mStringLongitude = mActivity.mGpsTracker.getLongitude() + "";
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*Get Installation photo from db*/
    private void getDBInstallationPhotoList() {
        DataHolder mDataHolder;
//        isAllPhotoImageExis = true;
        mArrayListPhotoList = new ArrayList<>();
        imageDisplayCount = 0;
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableInstallerPhoto + " where " + mActivity.mDbHelper.mFieldInstLocalId + "= '" + mActivity.mIntInstallationId + "'";
            System.out.println("Local url " + url);
            mDataHolder = mActivity.mDbHelper.read(url);
            if (mDataHolder != null) {
                System.out.println("Local Photo List " + url + " : " + mDataHolder.get_Listholder().size());
                for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
                    VoInstallationPhoto mVoInstallationPhoto = new VoInstallationPhoto();
                    mVoInstallationPhoto.setInst_photo_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstPhotoLocalID));
                    mVoInstallationPhoto.setInst_photo_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstPhotoServerID));
                    mVoInstallationPhoto.setInst_photo_local_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstPhotoLocalURL));
                    mVoInstallationPhoto.setInst_photo_server_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstPhotoServerURL));
                    mVoInstallationPhoto.setInst_photo_type(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstPhotoType));
                    mVoInstallationPhoto.setInst_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstLocalId));
                    mVoInstallationPhoto.setInst_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstServerId));
                    mVoInstallationPhoto.setInst_photo_user_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstPhotoUserId));
                    mVoInstallationPhoto.setInst_photo_created_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstPhotoCreatedDate));
                    mVoInstallationPhoto.setInst_photo_update_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstPhotoUpdateDate));
                    mVoInstallationPhoto.setIs_sync(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstPhotoIsSync));
                    if (mVoInstallationPhoto.getInst_photo_local_url() != null && !mVoInstallationPhoto.getInst_photo_local_url().equalsIgnoreCase("")) {
                        mVoInstallationPhoto.setIsHasImage(true);
                        File mFileImagePath = new File(mVoInstallationPhoto.getInst_photo_local_url());
                        if (mFileImagePath != null && mFileImagePath.exists()) {
                            imageDisplayCount = imageDisplayCount + 1;
                        }
                    } else {
                        mVoInstallationPhoto.setIsHasImage(false);
                    }
                    System.out.println("TWoURL-" + mVoInstallationPhoto.getInst_photo_local_url());
                    mArrayListPhotoList.add(mVoInstallationPhoto);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mTextViewPhotoCount.setText(String.valueOf(imageDisplayCount));
    }

    /*Show date choose dialog*/
    private void showDateDialog() {
        if (mStringInstallationDate != null && !mStringInstallationDate.equalsIgnoreCase("")) {
            newCalendar = Calendar.getInstance();
            try {
                newCalendar.setTimeInMillis(Long.parseLong(mStringInstallationDate));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        DatePickerDialog mDatePickerDialog = new DatePickerDialog(mActivity, R.style.AppCompatAlertDialogStyle, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                mStringInstallationDate = newDate.getTimeInMillis() + "";
                mTextViewInstallationDate.setText(mSimpleDateFormatDateDisplay.format(newDate.getTime()));
            }
        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        mDatePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        mDatePickerDialog.show();
    }

    /*Show bottom dialog*/
    private void showBottomSheetDialog() {
        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        mBottomSheetDialog = new BottomSheetDialog(mActivity);
        View view = getLayoutInflater().inflate(R.layout.dialog_bottomsheet, null);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.dialog_bottomsheet_recyclerView);
        TextView mTextViewTitle = (TextView) view.findViewById(R.id.dialog_bottomsheet_textview_title);
        mTextViewTitle.setText(R.string.str_frg_two_select_power);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mPowerAdapter = new PowerAdapter();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mPowerAdapter);
        mBottomSheetDialog.setContentView(view);
        mBottomSheetDialog.show();
        mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mBottomSheetDialog = null;

            }
        });
    }

    /*Power list adapter*/
    public class PowerAdapter extends RecyclerView.Adapter<PowerAdapter.ViewHolder> {

        @Override
        public PowerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_spinner_item, parent, false);
            return new PowerAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final PowerAdapter.ViewHolder mViewHolder, final int position) {
            mViewHolder.mTextViewName.setText(mArrayListPowerSource.get(position));
            mViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mArrayListPowerSource != null) {
                        if (position < mArrayListPowerSource.size()) {
                            mBottomSheetDialog.cancel();
                            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                            mStringSelectPower = mArrayListPowerSource.get(position);
                            mTextViewSelectPower.setText(mStringSelectPower);
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mArrayListPowerSource.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.raw_spinner_item_textview_item)
            TextView mTextViewName;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        backBT.setOnClickListener(this);
        nextBT.setOnClickListener(this);
        mRelativeLayoutInsDate.setOnClickListener(this);
        mRelativeLayoutSelectPower.setOnClickListener(this);
        mLinearLayoutVesselAsset.setOnClickListener(this);
        mRelativeLayoutInstallationPhoto.setOnClickListener(this);
        mBottomSheetBehavior = BottomSheetBehavior.from(mRelativeLayoutBottomSheet);
    }

    @Override
    public void onPause() {
        super.onPause();
        backBT.setOnClickListener(null);
        nextBT.setOnClickListener(null);
        mRelativeLayoutInsDate.setOnClickListener(null);
        mRelativeLayoutSelectPower.setOnClickListener(null);
        mLinearLayoutVesselAsset.setOnClickListener(null);
        mRelativeLayoutInstallationPhoto.setOnClickListener(null);
    }

    /*Handle Back button*/
    public void onBackPressCall(MainActivity mActivity) {
        mStringLatitude = mEditTextLatitude.getText().toString().trim();
        mStringLongitude = mEditTextLongitude.getText().toString().trim();
        mStringLocation = mEditTextLocation.getText().toString().trim();
        Calendar calBack = Calendar.getInstance();
        ContentValues mContentValuesBack = new ContentValues();
        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallDateTime, mStringInstallationDate);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallLatitude, mStringLatitude);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallLongitude, mStringLongitude);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallCountryCode, "");
        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallCountryName, "");
        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallVesselLocalId, "");
        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallVesselServerId, mStringVesselID);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallVesselName, mStringVesselName);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallVesselRegNo, mStringVesselRegiNo);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallPower, mStringSelectPower);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallLocation, mStringLocation);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallUpdatedDate, calBack.getTimeInMillis() + "");
        String[] mArray = new String[]{mActivity.mIntInstallationId + ""};
        mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInstall, mContentValuesBack, mActivity.mDbHelper.mFieldInstallLocalId + "=?", mArray);
        System.out.println("Device updated In Local Db");
        System.out.println("2-BACK-JD");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.backBT:
                /*Handle Back press*/
                mStringLatitude = mEditTextLatitude.getText().toString().trim();
                mStringLongitude = mEditTextLongitude.getText().toString().trim();
                mStringLocation = mEditTextLocation.getText().toString().trim();
                Calendar calBack = Calendar.getInstance();
                ContentValues mContentValuesBack = new ContentValues();
                mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallDateTime, mStringInstallationDate);
                mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallLatitude, mStringLatitude);
                mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallLongitude, mStringLongitude);
                mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallCountryCode, "");
                mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallCountryName, "");
                mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallVesselLocalId, "");
                mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallVesselServerId, mStringVesselID);
                mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallVesselName, mStringVesselName);
                mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallVesselRegNo, mStringVesselRegiNo);
                mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallPower, mStringSelectPower);
                mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallLocation, mStringLocation);
                String isExistInDBBack = CheckRecordExistInInstallDB(mActivity.mIntInstallationId + "");
                if (isExistInDBBack.equalsIgnoreCase("-1")) {
                    mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallCreatedDate, calBack.getTimeInMillis() + "");
                    int isInsertInstall = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableInstall, mContentValuesBack);
                    if (isInsertInstall != -1) {
                        System.out.println("Device Install Added In Local Db");
                    } else {
                        System.out.println("Device Install Adding In Local DB");
                    }
                } else {
                    mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallUpdatedDate, calBack.getTimeInMillis() + "");
                    String[] mArray = new String[]{isExistInDBBack};
                    mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInstall, mContentValuesBack, mActivity.mDbHelper.mFieldInstallLocalId + "=?", mArray);
                    System.out.println("Device updated In Local Db");
                }

                if (mListener != null)
                    mListener.onBackTwoPressed(this);
                break;

            case R.id.nextBT:
                /*on Click next button*/
                mStringLatitude = mEditTextLatitude.getText().toString().trim();
                mStringLongitude = mEditTextLongitude.getText().toString().trim();
                mStringLocation = mEditTextLocation.getText().toString().trim();
                System.out.println("mStringInstallationDate-" + mStringInstallationDate);
                if (mStringInstallationDate == null || mStringInstallationDate.equalsIgnoreCase("")) {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_two_select_ins_date));
                    return;
                }
                if (mStringLatitude == null || mStringLatitude.equalsIgnoreCase("")) {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_two_enter_latitude));
                    return;
                }
                if (mStringLongitude == null || mStringLongitude.equalsIgnoreCase("")) {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_two_enter_longitude));
                    return;
                }
                if (mStringVesselName == null || mStringVesselName.equalsIgnoreCase("")) {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_two_select_vessel_asset_select_name));
                    return;
                }
                if (mStringVesselRegiNo == null || mStringVesselRegiNo.equalsIgnoreCase("") || mStringVesselRegiNo.equalsIgnoreCase("null")) {
                    mStringVesselRegiNo = "NA";
                }
                if (mStringSelectPower == null || mStringSelectPower.equalsIgnoreCase("")) {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_two_select_device_power));
                    return;
                }
                if (mStringLocation == null || mStringLocation.equalsIgnoreCase("")) {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_two_enter_location));
                    return;
                }

                if (mArrayListPhotoList != null && mArrayListPhotoList.size() > 0) {
                    int imageCount = 0;
                    for (int i = 0; i < mArrayListPhotoList.size(); i++) {
                        if (mArrayListPhotoList.get(i).getInst_photo_local_url() != null && !mArrayListPhotoList.get(i).getInst_photo_local_url().equalsIgnoreCase("") && !mArrayListPhotoList.get(i).getInst_photo_local_url().equalsIgnoreCase("null")) {
                            File mFileImagePath = new File(mArrayListPhotoList.get(i).getInst_photo_local_url());
                            if (mFileImagePath != null && mFileImagePath.exists()) {
                                imageCount = imageCount + 1;
                            }
                        }
                    }
                    if (imageCount != 4) {
                        mActivity.mUtility.errorDialog("Please add 4 photos of installation.");
                        return;
                    }
                } else {
                    mActivity.mUtility.errorDialog("Please add 4 photos of installation.");
                    return;
                }

//                if (imageCount > 0) {
//                    if (!isAllPhotoImageExis) {
//                        mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_two_add_missing_image));
//                        return;
//                    }
//                } else {
//                    mActivity.mUtility.errorDialog("Please add at least one photo");
//                    return;
//                }
                Calendar cal = Calendar.getInstance();
                ContentValues mContentValues = new ContentValues();
                mContentValues.put(mActivity.mDbHelper.mFieldInstallDateTime, mStringInstallationDate);
                mContentValues.put(mActivity.mDbHelper.mFieldInstallLatitude, mStringLatitude);
                mContentValues.put(mActivity.mDbHelper.mFieldInstallLongitude, mStringLongitude);
                mContentValues.put(mActivity.mDbHelper.mFieldInstallCountryCode, "");
                mContentValues.put(mActivity.mDbHelper.mFieldInstallCountryName, "");
                mContentValues.put(mActivity.mDbHelper.mFieldInstallVesselLocalId, "");
                mContentValues.put(mActivity.mDbHelper.mFieldInstallVesselServerId, mStringVesselID);
                mContentValues.put(mActivity.mDbHelper.mFieldInstallVesselName, mStringVesselName);
                mContentValues.put(mActivity.mDbHelper.mFieldInstallVesselRegNo, mStringVesselRegiNo);
                mContentValues.put(mActivity.mDbHelper.mFieldInstallPower, mStringSelectPower);
                mContentValues.put(mActivity.mDbHelper.mFieldInstallLocation, mStringLocation);
                String isExistInDB = CheckRecordExistInInstallDB(mActivity.mIntInstallationId + "");
                if (isExistInDB.equalsIgnoreCase("-1")) {
                    mContentValues.put(mActivity.mDbHelper.mFieldInstallCreatedDate, cal.getTimeInMillis() + "");
                    int isInsertInstall = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableInstall, mContentValues);
                    if (isInsertInstall != -1) {
                        System.out.println("Device Install Added In Local Db");
                    } else {
                        System.out.println("Device Install Adding In Local DB");
                    }
                } else {
                    mContentValues.put(mActivity.mDbHelper.mFieldInstallUpdatedDate, cal.getTimeInMillis() + "");
                    String[] mArray = new String[]{isExistInDB};
                    mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInstall, mContentValues, mActivity.mDbHelper.mFieldInstallLocalId + "=?", mArray);
                    System.out.println("Device updated In Local Db");
                }

                if (mListener != null)
                    mListener.onNextTwoPressed(this);
                break;
            case R.id.fragment_new_ins_two_relativelayout_date:
                showDateDialog();
                break;
            case R.id.fragment_new_ins_two_linearlayout_vessel_asset:
                FragmentVesselAsset mFragmentVesselAsset = new FragmentVesselAsset();
                Bundle mBundle = new Bundle();
                mBundle.putString("mIntent_accountId", mActivity.mStringDeviceAccountId);
                mBundle.putBoolean("mIntent_isFromInstall", true);
                mFragmentVesselAsset.setOnScanResultSet(new onBackPressWithAction() {
                    @Override
                    public void onBackWithAction(String scanResult) {

                    }

                    @Override
                    public void onBackWithAction(String value1, String value2) {

                    }

                    @Override
                    public void onBackWithAction(String vesselID, String vesselName, String vesselPort) {
                        mStringVesselID = vesselID;
                        mStringVesselName = vesselName;
                        mStringVesselRegiNo = vesselPort;
                        if (mStringVesselRegiNo == null || mStringVesselRegiNo.equalsIgnoreCase("") || mStringVesselRegiNo.equalsIgnoreCase("null")) {
                            mStringVesselRegiNo = "NA";
                        }
                        mTextViewVesselName.setText(mStringVesselName);
                        mTextViewVesselRegNo.setText(mStringVesselRegiNo);
                    }
                });
                mActivity.replacesFragment(mFragmentVesselAsset, true, mBundle, 1);
                break;
            case R.id.fragment_new_ins_two_relativelayout_select_power:
                if (isAdded()) {
                    showBottomSheetDialog();
                }
                break;
            case R.id.fragment_new_ins_two_relativelayout_installation_photo:
                FragmentInstallationPhoto mFragmentInstallationPhoto = new FragmentInstallationPhoto();
                mActivity.replacesFragment(mFragmentInstallationPhoto, true, null, 1);
                break;
        }
    }
    /*Check record exist or not*/
    public String CheckRecordExistInInstallDB(String localInstallId) {
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
        mRelativeLayoutInsDate = null;
        mRelativeLayoutSelectPower = null;
        mLinearLayoutVesselAsset = null;
        mRelativeLayoutInstallationPhoto = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mActivity.mUtility.hideKeyboard(mActivity);
    }

}
