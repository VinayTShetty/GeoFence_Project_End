package com.succorfish.installer.fragment;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.succorfish.installer.BuildConfig;
import com.succorfish.installer.MainActivity;
import com.succorfish.installer.MyApplication;
import com.succorfish.installer.R;
import com.succorfish.installer.Vo.VoInstallation;
import com.succorfish.installer.Vo.VoInstallationHistory;
import com.succorfish.installer.Vo.VoInstallationResponse;
import com.succorfish.installer.db.DataHolder;
import com.succorfish.installer.helper.PreferenceHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Jaydeep on 09-03-2018.
 */

public class FragmentHistoryInstall extends Fragment {
    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;
    @BindView(R.id.fragment_history_install_editext_search)
    EditText mEditTextSearch;
    @BindView(R.id.fragment_history_install_imageview_search)
    ImageView mImageViewSearch;
    @BindView(R.id.fragment_history_install_textview_no_list)
    TextView mTextViewNoListFound;
    @BindView(R.id.fragment_history_install_recyclerView)
    RecyclerView mRecyclerViewInstallation;
    @BindView(R.id.fragment_history_install_swipeRefreshLayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    public ArrayList<VoInstallation> mArrayListInstallations = new ArrayList<>();
    InstallationAdapter mInstallationAdapter;
    private boolean isCalling = true;
    LinearLayoutManager mLayoutManager;
    private SimpleDateFormat mSimpleDateFormatDateDisplay;
    private SimpleDateFormat mSimpleDateFormatCompare;
    Calendar mCalendarCurrentTime;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_history_install, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);

        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.colorAccent,
                R.color.colorGreen,
                R.color.colorBlue,
                R.color.colorWhiteLight);
        mCalendarCurrentTime = Calendar.getInstance();
        mSimpleDateFormatCompare = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault());
        mSimpleDateFormatDateDisplay = new SimpleDateFormat(PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedDateFormat() + " HH:mm", Locale.getDefault());

        mInstallationAdapter = new InstallationAdapter();
        mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
        mRecyclerViewInstallation.setLayoutManager(mLayoutManager);
        mRecyclerViewInstallation.setAdapter(mInstallationAdapter);
        if (mActivity.mUtility.haveInternet()) {
            isCalling = true;
            /*get install history from server*/
            GetServerInstallationHistory(true);
        } else {
            isCalling = true;
            /*get install history from local db*/
            getDBInstallationList();
        }
        /*Search*/
        mEditTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (isAdded()) {
                    String searchString = mEditTextSearch.getText().toString().toLowerCase().trim();
                    if (searchString.length() == 0) {
                        getDBInstallationList();
                    } else {
                        if (searchString.length() >= 3) {
                            getDBSearchInstallationList(searchString);
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mImageViewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchString = mEditTextSearch.getText().toString().toLowerCase().trim();
                if (searchString.length() == 0) {
                    getDBInstallationList();
                } else {
                    if (searchString.length() >= 3) {
                        getDBSearchInstallationList(searchString);
                    }
                }
            }
        });
        /*Search Action*/
        mEditTextSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String searchString = mEditTextSearch.getText().toString().toLowerCase().trim();
                    if (searchString.length() == 0) {
                        getDBInstallationList();
                    } else {
                        if (searchString.length() >= 3) {
                            getDBSearchInstallationList(searchString);
                        }
                    }
                    return true;
                }
                return false;
            }
        });
        /*Refresh list*/
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isCalling) {
                    isCalling = true;
                    if (!mActivity.mUtility.haveInternet()) {
                        getDBInstallationList();
                    } else {
                        mSwipeRefreshLayout.setRefreshing(false);
                        GetServerInstallationHistory(true);
                    }
                }
            }
        });
        return mViewRoot;
    }
    /*Get installation history list from local database*/
    private void getDBInstallationList() {
        mCalendarCurrentTime = Calendar.getInstance();
        DataHolder mDataHolder;
        mArrayListInstallations = new ArrayList<>();
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableInstall + " where " + mActivity.mDbHelper.mFieldInstallIsSync + "= 1" + " AND " + mActivity.mDbHelper.mFieldInstallUserId + "= '" + PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserId() + "'" + " order by " + mActivity.mDbHelper.mFieldInstallDateTimeStamp + " DESC";
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
                    mArrayListInstallations.add(mVoInstallation);
                }
            }
//                Collections.sort(mArrayListTreatmentLists, new Comparator<TreatmentList>() {
//                    @Override
//                    public int compare(TreatmentList s1, TreatmentList s2) {
//                        return s1.getTreatment_title().compareToIgnoreCase(s1.getTreatment_title());
//                    }
//                });
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mInstallationAdapter == null) {
            mInstallationAdapter = new InstallationAdapter();
            mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
            mRecyclerViewInstallation.setLayoutManager(mLayoutManager);
            mRecyclerViewInstallation.setAdapter(mInstallationAdapter);
        } else {
            mInstallationAdapter.notifyDataSetChanged();
        }
        mInstallationAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkAdapterIsEmpty();
            }
        });
        mSwipeRefreshLayout.setRefreshing(false);
        isCalling = false;
        checkAdapterIsEmpty();
    }
    /*Search installation list from local database*/
    private void getDBSearchInstallationList(String string_search) {
        DataHolder mDataHolder;
        mArrayListInstallations = new ArrayList<>();
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableInstall + " where " + mActivity.mDbHelper.mFieldInstallIsSync + "= 1" + " AND " + mActivity.mDbHelper.mFieldInstallUserId + "= '" + PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserId() + "'" + " AND " + mActivity.mDbHelper.mFieldInstallVesselName + " like '%" + string_search + "%'" + " OR " + mActivity.mDbHelper.mFieldInstallVesselRegNo + " like '%" + string_search + "%'" + " order by " + mActivity.mDbHelper.mFieldInstallDateTimeStamp + " DESC";

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
                    mArrayListInstallations.add(mVoInstallation);
                }
            }
//                Collections.sort(mArrayListTreatmentLists, new Comparator<TreatmentList>() {
//                    @Override
//                    public int compare(TreatmentList s1, TreatmentList s2) {
//                        return s1.getTreatment_title().compareToIgnoreCase(s1.getTreatment_title());
//                    }
//                });
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mInstallationAdapter != null) {
            mInstallationAdapter.notifyDataSetChanged();
        }
        isCalling = false;
        checkAdapterIsEmpty();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    /*API to Get Installation history from server*/
    public void GetServerInstallationHistory(final boolean isShowProgress) {
        mActivity.mUtility.hideKeyboard(mActivity);
        if (isShowProgress) {
            mActivity.mUtility.ShowProgress();
        }
        Call<String> mVoInstallationHistoryCall = mActivity.mApiService.getAllInstallationHistory(PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserId());
        mVoInstallationHistoryCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (isAdded()) {
                    Gson gson = new Gson();
                    if (response.code() == 200 || response.isSuccessful()) {
                        System.out.println("response mInstallationHistoryData---------" + response.body());
                        try {
                            TypeToken<List<VoInstallationResponse>> token = new TypeToken<List<VoInstallationResponse>>() {
                            };
                            List<VoInstallationResponse> mVoInstallationHistory = gson.fromJson(response.body().toString(), token.getType());
                            System.out.println("response mInstallationHistoryData...... " + new Gson().toJson(mVoInstallationHistory));
                            if (mVoInstallationHistory != null) {
                                if (mVoInstallationHistory.size() > 0) {
                                    ContentValues mContentValues;
                                    for (int i = 0; i < mVoInstallationHistory.size(); i++) {
                                        mContentValues = new ContentValues();
                                        mContentValues.put(mActivity.mDbHelper.mFieldInstallServerId, mVoInstallationHistory.get(i).getId());
                                        mContentValues.put(mActivity.mDbHelper.mFieldInstallUserId, PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserId());
                                        mContentValues.put(mActivity.mDbHelper.mFieldInstallDeviceIMEINo, mVoInstallationHistory.get(i).getDeviceImei());
                                        mContentValues.put(mActivity.mDbHelper.mFieldInstallDeviceServerId, mVoInstallationHistory.get(i).getDeviceId());
                                        mContentValues.put(mActivity.mDbHelper.mFieldInstallDeviceLocalId, "1");
                                        mContentValues.put(mActivity.mDbHelper.mFieldInstallDevicName, "");
                                        mContentValues.put(mActivity.mDbHelper.mFieldInstallDeviceWarranty_status, "");
                                        mContentValues.put(mActivity.mDbHelper.mFieldInstallDeviceTypeName, mVoInstallationHistory.get(i).getDeviceType());
                                        mContentValues.put(mActivity.mDbHelper.mFieldInstallDateTime, mVoInstallationHistory.get(i).getDate());
                                        mContentValues.put(mActivity.mDbHelper.mFieldInstallLatitude, "");
                                        mContentValues.put(mActivity.mDbHelper.mFieldInstallLongitude, "");
                                        mContentValues.put(mActivity.mDbHelper.mFieldInstallCountryCode, "");
                                        mContentValues.put(mActivity.mDbHelper.mFieldInstallCountryName, "");

                                        mContentValues.put(mActivity.mDbHelper.mFieldInstallVesselServerId, mVoInstallationHistory.get(i).getRealAssetId());
                                        mContentValues.put(mActivity.mDbHelper.mFieldInstallVesselName, mVoInstallationHistory.get(i).getRealAssetName());
                                        mContentValues.put(mActivity.mDbHelper.mFieldInstallVesselRegNo, mVoInstallationHistory.get(i).getRealAssetRegNo());
                                        mContentValues.put(mActivity.mDbHelper.mFieldInstallPower, mVoInstallationHistory.get(i).getPowerSource());
                                        mContentValues.put(mActivity.mDbHelper.mFieldInstallLocation, mVoInstallationHistory.get(i).getInstallationPlace());

                                        if (mVoInstallationHistory.get(i).getContactInfo() != null) {
                                            mContentValues.put(mActivity.mDbHelper.mFieldInstallOwnerName, mVoInstallationHistory.get(i).getContactInfo().getName());
                                            mContentValues.put(mActivity.mDbHelper.mFieldInstallOwnerAddress, mVoInstallationHistory.get(i).getContactInfo().getAddress());
                                            mContentValues.put(mActivity.mDbHelper.mFieldInstallOwnerCity, mVoInstallationHistory.get(i).getContactInfo().getCity());
                                            mContentValues.put(mActivity.mDbHelper.mFieldInstallOwnerState, mVoInstallationHistory.get(i).getContactInfo().getState());
                                            mContentValues.put(mActivity.mDbHelper.mFieldInstallOwnerZipcode, mVoInstallationHistory.get(i).getContactInfo().getZipCode());
                                            mContentValues.put(mActivity.mDbHelper.mFieldInstallOwnerEmail, mVoInstallationHistory.get(i).getContactInfo().getEmail());
                                            mContentValues.put(mActivity.mDbHelper.mFieldInstallOwnerMobileNo, mVoInstallationHistory.get(i).getContactInfo().getTelephone());
                                        }

                                        mContentValues.put(mActivity.mDbHelper.mFieldInstallLocalSignUrl, "");
                                        mContentValues.put(mActivity.mDbHelper.mFieldInstallServerSignUrl, "");
                                        mContentValues.put(mActivity.mDbHelper.mFieldInstallLocalInstallerSignUrl, "");
                                        mContentValues.put(mActivity.mDbHelper.mFieldInstallServerInstallerSignUrl, "");
                                        mContentValues.put(mActivity.mDbHelper.mFieldInstallPdfUrl, "");
                                        mContentValues.put(mActivity.mDbHelper.mFieldInstallUpdatedDate, mVoInstallationHistory.get(i).getDate());
                                        mContentValues.put(mActivity.mDbHelper.mFieldInstallDateTimeStamp, mVoInstallationHistory.get(i).getDate());
                                        mContentValues.put(mActivity.mDbHelper.mFieldInstallStatus, "1");
                                        mContentValues.put(mActivity.mDbHelper.mFieldInstallIsSync, "1");
                                        mContentValues.put(mActivity.mDbHelper.mFieldInstallIsSyncOwnerSign, "1");
                                        mContentValues.put(mActivity.mDbHelper.mFieldInstallIsSyncInstallerSign, "1");
                                        Map<String, String> attachedFile = mVoInstallationHistory.get(i).getAttachedFilesList();
                                        for (Map.Entry<String, String> entry : attachedFile.entrySet()) {
                                            if (entry.getValue().toString().contains(".pdf")) {
                                                mContentValues.put(mActivity.mDbHelper.mFieldInstallPdfUrl, entry.getKey() + "/" + mVoInstallationHistory.get(i).getId());
                                            }
                                        }

                                        String isInstallExistInDB = CheckRecordExistInInstallDB(mVoInstallationHistory.get(i).getId().toLowerCase());
                                        if (isInstallExistInDB.equalsIgnoreCase("-1")) {
                                            mContentValues.put(mActivity.mDbHelper.mFieldInstallCreatedDate, mVoInstallationHistory.get(i).getDate());
                                            int isInsertInstall = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableInstall, mContentValues);
                                            if (isInsertInstall != -1) {
                                                System.out.println("Device Install Added In Local Db");
                                            } else {
                                                System.out.println("Device Install Adding In Local DB");
                                            }
                                        } else {
                                            String[] mArray = new String[]{isInstallExistInDB};
                                            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInstall, mContentValues, mActivity.mDbHelper.mFieldInstallServerId + "=?", mArray);
                                            System.out.println("Device updated In Local Db");
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                    mActivity.mUtility.HideProgress();
                    getDBInstallationList();
                    mSwipeRefreshLayout.setRefreshing(false);
                    isCalling = false;

                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                if (isAdded()) {
                    t.printStackTrace();
                    mActivity.mUtility.HideProgress();
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_try_again));
                    getDBInstallationList();
                    mSwipeRefreshLayout.setRefreshing(false);
                    isCalling = false;
                }
            }
        });
    }
    /*Check record exist in vessel table or not*/
    public String CheckRecordExistInVesselDB(String serverVesselId) {
        DataHolder mDataHolder = new DataHolder();
        String url = "select * from " + mActivity.mDbHelper.mTableVesselAsset + " where " + mActivity.mDbHelper.mFieldVesselServerId + "= '" + serverVesselId + "'";
        mDataHolder = mActivity.mDbHelper.read(url);
        if (mDataHolder != null) {
            System.out.println(" VesselList : " + url + " : " + mDataHolder.get_Listholder().size());
            if (mDataHolder != null && mDataHolder.get_Listholder().size() != 0) {
                return mDataHolder.get_Listholder().get(0).get(mActivity.mDbHelper.mFieldVesselServerId);
            } else {
                return "-1";
            }
        }
        return "-1";
    }
    /*Check record exist in install table or not*/
    public String CheckRecordExistInInstallDB(String ServerInstallId) {
        DataHolder mDataHolder = new DataHolder();
        String url = "select * from " + mActivity.mDbHelper.mTableInstall + " where " + mActivity.mDbHelper.mFieldInstallServerId + "= '" + ServerInstallId + "'";
        mDataHolder = mActivity.mDbHelper.read(url);
        if (mDataHolder != null) {
            System.out.println(" Install List : " + url + " : " + mDataHolder.get_Listholder().size());
            if (mDataHolder != null && mDataHolder.get_Listholder().size() != 0) {
                return mDataHolder.get_Listholder().get(0).get(mActivity.mDbHelper.mFieldInstallServerId);
            } else {
                return "-1";
            }
        }
        return "-1";
    }

    private void checkAdapterIsEmpty() {
        if (mArrayListInstallations != null) {
            if (mInstallationAdapter.getItemCount() == 0) {
                mTextViewNoListFound.setVisibility(View.VISIBLE);
                mRecyclerViewInstallation.setVisibility(View.GONE);
            } else {
                mTextViewNoListFound.setVisibility(View.GONE);
                mRecyclerViewInstallation.setVisibility(View.VISIBLE);
            }
        } else {
            mTextViewNoListFound.setVisibility(View.VISIBLE);
            mRecyclerViewInstallation.setVisibility(View.GONE);
        }
    }
    /*Installation Adapter*/
    public class InstallationAdapter extends RecyclerView.Adapter<InstallationAdapter.ViewHolder> {

        @Override
        public InstallationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_history_install_list, parent, false);
            return new InstallationAdapter.ViewHolder(itemView);
        }


        @Override
        public void onBindViewHolder(final InstallationAdapter.ViewHolder mViewHolder, final int position) {
            if (isAdded()) {
                if (mArrayListInstallations.get(position).getInst_device_imei_no() != null && !mArrayListInstallations.get(position).getInst_device_imei_no().equalsIgnoreCase("")) {
                    mViewHolder.mTextViewIMEINo.setText(mArrayListInstallations.get(position).getInst_device_imei_no());
                } else {
                    mViewHolder.mTextViewIMEINo.setText("-NA-");
                }
                if (mArrayListInstallations.get(position).getInst_vessel_name() != null && !mArrayListInstallations.get(position).getInst_vessel_name().equalsIgnoreCase("")) {
                    mViewHolder.mTextViewVesselName.setText(mArrayListInstallations.get(position).getInst_vessel_name());
                } else {
                    mViewHolder.mTextViewVesselName.setText("-NA-");
                }
                if (mArrayListInstallations.get(position).getInst_date_time() != null && !mArrayListInstallations.get(position).getInst_date_time().equalsIgnoreCase("") && !mArrayListInstallations.get(position).getInst_date_time().equalsIgnoreCase("null")) {
                    try {
                        mViewHolder.mTextViewTime.setVisibility(View.VISIBLE);
                        Calendar mCalendar = Calendar.getInstance();
                        mCalendar.setTimeInMillis(Long.parseLong(mArrayListInstallations.get(position).getInst_date_time()));
                        long mCurrentTime = mCalendarCurrentTime.getTimeInMillis();
                        long diff = mCurrentTime - mCalendar.getTimeInMillis();

                        long diffSeconds = diff / 1000 % 60;
                        long diffMinutes = diff / (60 * 1000) % 60;
                        long diffHours = diff / (60 * 60 * 1000) % 24;
                        long diffDays = diff / (24 * 60 * 60 * 1000);

                        String timePostfix;
                        if (diffDays >= 1) {
                            if (diffDays == 1) {
                                timePostfix = diffDays + " Day ago";
                            } else {
                                timePostfix = diffDays + " Days ago";
                            }
                        } else if (diffHours >= 1) {
                            timePostfix = diffHours + " Hour ago";
                        } else {
                            if (diffMinutes == 0 && diffSeconds <= 60) {
                                timePostfix = "1 Min ago";
                            } else {
                                timePostfix = diffMinutes + " Min ago";
                            }
                        }
                        mViewHolder.mTextViewTime.setText("(" + timePostfix + ")");
                        mViewHolder.mTextViewInstallationDate.setText(mSimpleDateFormatDateDisplay.format(mCalendar.getTime()));
                    } catch (Exception e) {
                        mViewHolder.mTextViewInstallationDate.setText("-NA-");
                        mViewHolder.mTextViewTime.setVisibility(View.GONE);
                        e.printStackTrace();
                    }
                } else {
                    mViewHolder.mTextViewInstallationDate.setText("-NA-");
                    mViewHolder.mTextViewTime.setVisibility(View.GONE);
                }

                if (mArrayListInstallations.get(position).getInst_vessel_regi_no() != null && !mArrayListInstallations.get(position).getInst_vessel_regi_no().equalsIgnoreCase("")) {
                    mViewHolder.mTextViewVesselRegNo.setText(mArrayListInstallations.get(position).getInst_vessel_regi_no());
                } else {
                    mViewHolder.mTextViewVesselRegNo.setText("-NA-");
                }
                if (mArrayListInstallations.get(position).getInst_device_type_name() != null && !mArrayListInstallations.get(position).getInst_device_type_name().equalsIgnoreCase("")) {
                    mViewHolder.mTextViewType.setText(mArrayListInstallations.get(position).getInst_device_type_name());
                } else {
                    mViewHolder.mTextViewType.setText("-NA-");
                }

                mViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mArrayListInstallations != null) {
                            if (position < mArrayListInstallations.size()) {
                                FragmentHistoryDetails mFragmentHistoryDetails = new FragmentHistoryDetails();
                                Bundle mBundle = new Bundle();
                                mBundle.putInt("intent_history_type", 0);
                                mBundle.putSerializable("intent_install_data", mArrayListInstallations.get(position));
                                mActivity.replacesFragment(mFragmentHistoryDetails, true, mBundle, 1);
                            }
                        }
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mArrayListInstallations.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.raw_history_install_item_textview_vessel_name)
            TextView mTextViewVesselName;
            @BindView(R.id.raw_history_install_item_textview_reg_no)
            TextView mTextViewVesselRegNo;
            @BindView(R.id.raw_history_install_item_textview_type)
            TextView mTextViewType;
            @BindView(R.id.raw_history_install_item_textview_imei_no)
            TextView mTextViewIMEINo;
            @BindView(R.id.raw_history_install_item_textview_date)
            TextView mTextViewInstallationDate;
            @BindView(R.id.raw_history_install_item_textview_time)
            TextView mTextViewTime;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
