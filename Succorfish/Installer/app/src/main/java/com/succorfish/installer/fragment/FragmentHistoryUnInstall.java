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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.succorfish.installer.MainActivity;
import com.succorfish.installer.MyApplication;
import com.succorfish.installer.R;
import com.succorfish.installer.Vo.VoInstallationResponse;
import com.succorfish.installer.Vo.VoUnInstall;
import com.succorfish.installer.db.DataHolder;
import com.succorfish.installer.helper.PreferenceHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Jaydeep on 27-02-2018.
 */

public class FragmentHistoryUnInstall extends Fragment {
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
    RecyclerView mRecyclerViewUnInstallation;
    @BindView(R.id.fragment_history_install_swipeRefreshLayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    public ArrayList<VoUnInstall> mArrayListUnInstallList = new ArrayList<>();
    UnInstallationAdapter mUnInstallationAdapter;
    private boolean isCalling = true;
    LinearLayoutManager mLayoutManager;
    private SimpleDateFormat mSimpleDateFormatDateDisplay;
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
        mSimpleDateFormatDateDisplay = new SimpleDateFormat(PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedDateFormat() + " HH:mm", Locale.getDefault());
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.colorAccent,
                R.color.colorGreen,
                R.color.colorBlue,
                R.color.colorWhiteLight);
        mCalendarCurrentTime = Calendar.getInstance();
        mUnInstallationAdapter = new UnInstallationAdapter();
        mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
        mRecyclerViewUnInstallation.setLayoutManager(mLayoutManager);
        mRecyclerViewUnInstallation.setAdapter(mUnInstallationAdapter);
        if (mActivity.mUtility.haveInternet()) {
            isCalling = true;
            /*get un install history from server*/
            GetServerUnInstallationHistory(true);
        } else {
            isCalling = true;
            /*get un install history from local db*/
            getDBUnInstallationList();
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
                        getDBUnInstallationList();
                    } else {
                        if (searchString.length() >= 3) {
                            getDBSearchUnInstallationList(searchString);
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
                    getDBUnInstallationList();
                } else {
                    if (searchString.length() >= 3) {
                        getDBSearchUnInstallationList(searchString);
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
                        getDBUnInstallationList();
                    } else {
                        if (searchString.length() >= 3) {
                            getDBSearchUnInstallationList(searchString);
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
                        getDBUnInstallationList();
                    } else {
                        mSwipeRefreshLayout.setRefreshing(false);
                        GetServerUnInstallationHistory(true);
                    }
                }
            }
        });
        return mViewRoot;
    }

    /*Get un installation history list from local database*/
    private void getDBUnInstallationList() {
        mCalendarCurrentTime = Calendar.getInstance();
        DataHolder mDataHolder;
        mArrayListUnInstallList = new ArrayList<>();
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableUnInstall + " where " + mActivity.mDbHelper.mFieldUnInstallIsSync + "= 1" + " AND " + mActivity.mDbHelper.mFieldUnInstallUserId + "= '" + PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserId() + "'" + " order by " + mActivity.mDbHelper.mFieldUnInstallSDateTimeStamp + " DESC";
            System.out.println("Local url " + url);
            mDataHolder = mActivity.mDbHelper.read(url);
            if (mDataHolder != null) {
                System.out.println("Local Device List " + url + " : " + mDataHolder.get_Listholder().size());
                for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
                    VoUnInstall mVoUnInstall = new VoUnInstall();
                    mVoUnInstall.setUninst_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallLocalId));
                    mVoUnInstall.setUninst_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallServerId));
                    mVoUnInstall.setUninst_user_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallUserId));
                    mVoUnInstall.setUninst_device_type_imei_no(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallDeviceIMEINo));
                    mVoUnInstall.setUninst_device_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallDeviceServerId));
                    mVoUnInstall.setUninst_device_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallDeviceLocalId));
                    mVoUnInstall.setUninst_device_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallDeviceName));
                    mVoUnInstall.setUninst_device_warranty_status(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallDeviceWarrantStatus));
                    mVoUnInstall.setUninst_device_type_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallDeviceTypeName));
                    mVoUnInstall.setUninst_vessel_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallVesselLocalId));
                    mVoUnInstall.setUninst_vessel_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallVesselServerId));
                    mVoUnInstall.setUninst_vessel_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallVesselName));
                    mVoUnInstall.setUninst_vessel_regi_no(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallVesselRegNo));
                    mVoUnInstall.setUninst_owner_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallOwnerName));
                    mVoUnInstall.setUninst_owner_address(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallOwnerAddress));
                    mVoUnInstall.setUninst_owner_city(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallOwnerCity));
                    mVoUnInstall.setUninst_owner_state(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallOwnerState));
                    mVoUnInstall.setUninst_owner_zipcode(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallOwnerZipcode));
                    mVoUnInstall.setUninst_owner_email(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallOwnerEmail));
                    mVoUnInstall.setUninst_owner_mobile_no(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallOwnerMobileNo));
                    mVoUnInstall.setUninst_local_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallLocalSignUrl));
                    mVoUnInstall.setUninst_server_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallServerSignUrl));
                    mVoUnInstall.setUninst_local_uninstaller_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallLocalUninstallerSignUrl));
                    mVoUnInstall.setUninst_server_uninstaller_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallServerUninstallerSignUrl));
                    mVoUnInstall.setUninst_pdf_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallPdfUrl));
                    mVoUnInstall.setUninst_created_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallCreatedDate));
                    mVoUnInstall.setUninst_updated_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallUpdatedDate));
                    mVoUnInstall.setUninst_is_sync(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallIsSync));
                    mVoUnInstall.setUninst_status(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallStatus));
                    mVoUnInstall.setUninst_date_time(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallDateTime));
                    mVoUnInstall.setUninst_date_timestamp(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallSDateTimeStamp));
                    System.out.println("JD-DB-PDF-" + mVoUnInstall.getUninst_pdf_url());
                    mArrayListUnInstallList.add(mVoUnInstall);
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
        if (mUnInstallationAdapter == null) {
            mUnInstallationAdapter = new UnInstallationAdapter();
            mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
            mRecyclerViewUnInstallation.setLayoutManager(mLayoutManager);
            mRecyclerViewUnInstallation.setAdapter(mUnInstallationAdapter);
        } else {
            mUnInstallationAdapter.notifyDataSetChanged();
        }
        mUnInstallationAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
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

    /*Search un installation list from local database*/
    private void getDBSearchUnInstallationList(String string_search) {
        DataHolder mDataHolder;
        mArrayListUnInstallList = new ArrayList<>();
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableUnInstall + " where " + mActivity.mDbHelper.mFieldUnInstallIsSync + "= 1" + " AND " + mActivity.mDbHelper.mFieldUnInstallUserId + "= '" + PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserId() + "'" + " AND " + mActivity.mDbHelper.mFieldUnInstallVesselName + " like '%" + string_search + "%'" + " OR " + mActivity.mDbHelper.mFieldUnInstallVesselRegNo + " like '%" + string_search + "%'" + " order by " + mActivity.mDbHelper.mFieldUnInstallSDateTimeStamp + " DESC";
            System.out.println("Local url " + url);
            mDataHolder = mActivity.mDbHelper.read(url);
            if (mDataHolder != null) {
                System.out.println("Local Device List " + url + " : " + mDataHolder.get_Listholder().size());
                for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
                    VoUnInstall mVoUnInstall = new VoUnInstall();
                    mVoUnInstall.setUninst_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallLocalId));
                    mVoUnInstall.setUninst_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallServerId));
                    mVoUnInstall.setUninst_user_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallUserId));
                    mVoUnInstall.setUninst_device_type_imei_no(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallDeviceIMEINo));
                    mVoUnInstall.setUninst_device_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallDeviceServerId));
                    mVoUnInstall.setUninst_device_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallDeviceLocalId));
                    mVoUnInstall.setUninst_device_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallDeviceName));
                    mVoUnInstall.setUninst_device_warranty_status(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallDeviceWarrantStatus));
                    mVoUnInstall.setUninst_device_type_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallDeviceTypeName));
                    mVoUnInstall.setUninst_vessel_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallVesselLocalId));
                    mVoUnInstall.setUninst_vessel_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallVesselServerId));
                    mVoUnInstall.setUninst_vessel_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallVesselName));
                    mVoUnInstall.setUninst_vessel_regi_no(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallVesselRegNo));
                    mVoUnInstall.setUninst_owner_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallOwnerName));
                    mVoUnInstall.setUninst_owner_address(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallOwnerAddress));
                    mVoUnInstall.setUninst_owner_city(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallOwnerCity));
                    mVoUnInstall.setUninst_owner_state(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallOwnerState));
                    mVoUnInstall.setUninst_owner_zipcode(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallOwnerZipcode));
                    mVoUnInstall.setUninst_owner_email(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallOwnerEmail));
                    mVoUnInstall.setUninst_owner_mobile_no(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallOwnerMobileNo));
                    mVoUnInstall.setUninst_local_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallLocalSignUrl));
                    mVoUnInstall.setUninst_server_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallServerSignUrl));
                    mVoUnInstall.setUninst_local_uninstaller_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallLocalUninstallerSignUrl));
                    mVoUnInstall.setUninst_server_uninstaller_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallServerUninstallerSignUrl));
                    mVoUnInstall.setUninst_pdf_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallPdfUrl));
                    mVoUnInstall.setUninst_created_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallCreatedDate));
                    mVoUnInstall.setUninst_updated_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallUpdatedDate));
                    mVoUnInstall.setUninst_is_sync(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallIsSync));
                    mVoUnInstall.setUninst_status(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallStatus));
                    mVoUnInstall.setUninst_date_time(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallDateTime));
                    mVoUnInstall.setUninst_date_timestamp(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallSDateTimeStamp));

                    mArrayListUnInstallList.add(mVoUnInstall);
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
        if (mUnInstallationAdapter != null) {
            mUnInstallationAdapter.notifyDataSetChanged();
        }
        checkAdapterIsEmpty();
        isCalling = false;
        mSwipeRefreshLayout.setRefreshing(false);
    }

    /*API to Get un Installation history from server*/
    public void GetServerUnInstallationHistory(final boolean isShowProgress) {
        mActivity.mUtility.hideKeyboard(mActivity);
        if (isShowProgress) {
            mActivity.mUtility.ShowProgress();
        }
        Call<String> mVoInstallationHistoryCall = mActivity.mApiService.getAllUnInstallationHistory(PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserId());
        mVoInstallationHistoryCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (isAdded()) {
                    Gson gson = new Gson();
                    if (response.code() == 200 || response.isSuccessful()) {
                        System.out.println("response mUnInstallationHistoryData---------" + response.body());
                        try {
                            TypeToken<List<VoInstallationResponse>> token = new TypeToken<List<VoInstallationResponse>>() {
                            };
                            List<VoInstallationResponse> mVoInstallationHistory = gson.fromJson(response.body().toString(), token.getType());
                            System.out.println("response mUnInstallationHistoryData...... " + new Gson().toJson(mVoInstallationHistory));
                            if (mVoInstallationHistory != null) {
                                if (mVoInstallationHistory.size() > 0) {
                                    Calendar cal = Calendar.getInstance();
                                    ContentValues mContentValues;
                                    Map<String, String> attachedFile;
                                    for (int i = 0; i < mVoInstallationHistory.size(); i++) {
                                        mContentValues = new ContentValues();
                                        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallServerId, mVoInstallationHistory.get(i).getId());
                                        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallUserId, PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserId());
                                        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallDeviceIMEINo, mVoInstallationHistory.get(i).getDeviceImei());
                                        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallDeviceServerId, mVoInstallationHistory.get(i).getDeviceId());
                                        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallDeviceLocalId, "1");
                                        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallDeviceName, "");
                                        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallDeviceWarrantStatus, "");
                                        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallDeviceTypeName, mVoInstallationHistory.get(i).getDeviceType());
                                        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallDateTime, mVoInstallationHistory.get(i).getDate());

                                        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallVesselServerId, mVoInstallationHistory.get(i).getRealAssetId());
                                        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallVesselName, mVoInstallationHistory.get(i).getRealAssetName());
                                        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallVesselRegNo, mVoInstallationHistory.get(i).getRealAssetRegNo());
                                        if (mVoInstallationHistory.get(i).getContactInfo() != null) {
                                            mContentValues.put(mActivity.mDbHelper.mFieldUnInstallOwnerName, mVoInstallationHistory.get(i).getContactInfo().getName());
                                            mContentValues.put(mActivity.mDbHelper.mFieldUnInstallOwnerAddress, mVoInstallationHistory.get(i).getContactInfo().getAddress());
                                            mContentValues.put(mActivity.mDbHelper.mFieldUnInstallOwnerCity, mVoInstallationHistory.get(i).getContactInfo().getCity());
                                            mContentValues.put(mActivity.mDbHelper.mFieldUnInstallOwnerState, mVoInstallationHistory.get(i).getContactInfo().getState());
                                            mContentValues.put(mActivity.mDbHelper.mFieldUnInstallOwnerZipcode, mVoInstallationHistory.get(i).getContactInfo().getZipCode());
                                            mContentValues.put(mActivity.mDbHelper.mFieldUnInstallOwnerEmail, mVoInstallationHistory.get(i).getContactInfo().getEmail());
                                            mContentValues.put(mActivity.mDbHelper.mFieldUnInstallOwnerMobileNo, mVoInstallationHistory.get(i).getContactInfo().getTelephone());
                                        }

                                        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallLocalSignUrl, "");
                                        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallServerSignUrl, "");
                                        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallLocalUninstallerSignUrl, "");
                                        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallServerUninstallerSignUrl, "");
                                        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallPdfUrl, "");
                                        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallUpdatedDate, mVoInstallationHistory.get(i).getDate());
                                        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallSDateTimeStamp, mVoInstallationHistory.get(i).getDate());
                                        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallStatus, "1");
                                        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallIsSync, "1");
                                        attachedFile = mVoInstallationHistory.get(i).getAttachedFilesList();
                                        System.out.println("JD-SIZE " + attachedFile.size());
                                        for (Map.Entry<String, String> entry : attachedFile.entrySet()) {
                                            if (entry.getValue().toString().contains(".pdf")) {
                                                System.out.println("JD-PDF-" + entry.getKey() + "/" + mVoInstallationHistory.get(i).getId());
                                                mContentValues.put(mActivity.mDbHelper.mFieldUnInstallPdfUrl, entry.getKey() + "/" + mVoInstallationHistory.get(i).getId());
                                            }
                                        }
                                        String isUnInstallExistInDB = CheckRecordExistInUnInstallDB(mVoInstallationHistory.get(i).getId());
                                        if (isUnInstallExistInDB.equalsIgnoreCase("-1")) {
                                            mContentValues.put(mActivity.mDbHelper.mFieldUnInstallCreatedDate, mVoInstallationHistory.get(i).getDate());
                                            mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableUnInstall, mContentValues);
                                            System.out.println("Device UnInstall added In Local Db");
                                        } else {
                                            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableUnInstall, mContentValues, mActivity.mDbHelper.mFieldUnInstallLocalId + "=?", new String[]{isUnInstallExistInDB});
                                            System.out.println("Device UnInstall updated In Local Db");
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    mActivity.mUtility.HideProgress();
                    getDBUnInstallationList();
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
                    getDBUnInstallationList();
                    mSwipeRefreshLayout.setRefreshing(false);
                    isCalling = false;
                }
            }
        });
    }

    /*Check record exist in uninstall table or not*/
    public String CheckRecordExistInUnInstallDB(String serverUnInstallId) {
        DataHolder mDataHolder = new DataHolder();
        String url = "select * from " + mActivity.mDbHelper.mTableUnInstall + " where " + mActivity.mDbHelper.mFieldUnInstallServerId + "= '" + serverUnInstallId + "'";
        mDataHolder = mActivity.mDbHelper.read(url);
        if (mDataHolder != null) {
            System.out.println(" Install List : " + url + " : " + mDataHolder.get_Listholder().size());
            if (mDataHolder != null && mDataHolder.get_Listholder().size() != 0) {
                return mDataHolder.get_Listholder().get(0).get(mActivity.mDbHelper.mFieldUnInstallServerId);
            } else {
                return "-1";
            }
        }
        return "-1";
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

    private void checkAdapterIsEmpty() {
        if (mArrayListUnInstallList != null) {
            if (mUnInstallationAdapter.getItemCount() == 0) {
                mTextViewNoListFound.setVisibility(View.VISIBLE);
                mRecyclerViewUnInstallation.setVisibility(View.GONE);
            } else {
                mTextViewNoListFound.setVisibility(View.GONE);
                mRecyclerViewUnInstallation.setVisibility(View.VISIBLE);
            }
        } else {
            mTextViewNoListFound.setVisibility(View.VISIBLE);
            mRecyclerViewUnInstallation.setVisibility(View.GONE);
        }
    }

    /*Un Installation Adapter*/
    public class UnInstallationAdapter extends RecyclerView.Adapter<UnInstallationAdapter.ViewHolder> {

        @Override
        public UnInstallationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_history_install_list, parent, false);
            return new UnInstallationAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final UnInstallationAdapter.ViewHolder mViewHolder, final int position) {
            if (isAdded()) {
                if (mArrayListUnInstallList.get(position).getUninst_device_type_imei_no() != null && !mArrayListUnInstallList.get(position).getUninst_device_type_imei_no().equalsIgnoreCase("")) {
                    mViewHolder.mTextViewIMEINo.setText(mArrayListUnInstallList.get(position).getUninst_device_type_imei_no());
                } else {
                    mViewHolder.mTextViewIMEINo.setText("-NA-");
                }
                if (mArrayListUnInstallList.get(position).getUninst_vessel_name() != null && !mArrayListUnInstallList.get(position).getUninst_vessel_name().equalsIgnoreCase("")) {
                    mViewHolder.mTextViewVesselName.setText(mArrayListUnInstallList.get(position).getUninst_vessel_name());
                } else {
                    mViewHolder.mTextViewVesselName.setText("-NA-");
                }
                if (mArrayListUnInstallList.get(position).getUninst_date_time() != null && !mArrayListUnInstallList.get(position).getUninst_date_time().equalsIgnoreCase("") && !mArrayListUnInstallList.get(position).getUninst_date_time().equalsIgnoreCase("null")) {
                    try {
                        mViewHolder.mTextViewTime.setVisibility(View.VISIBLE);
                        Calendar mCalendar = Calendar.getInstance();
                        mCalendar.setTimeInMillis(Long.parseLong(mArrayListUnInstallList.get(position).getUninst_date_time()));
                        long mCurrentTime = mCalendarCurrentTime.getTimeInMillis();
                        long diff = mCurrentTime - mCalendar.getTimeInMillis();

                        long diffSeconds = diff / 1000 % 60;
                        long diffMinutes = diff / (60 * 1000) % 60;
                        long diffHours = diff / (60 * 60 * 1000) % 24;
                        long diffDays = diff / (24 * 60 * 60 * 1000);

                        String timePostfix = "";
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
                if (mArrayListUnInstallList.get(position).getUninst_vessel_regi_no() != null && !mArrayListUnInstallList.get(position).getUninst_vessel_regi_no().equalsIgnoreCase("")) {
                    mViewHolder.mTextViewVesselRegNo.setText(mArrayListUnInstallList.get(position).getUninst_vessel_regi_no());
                } else {
                    mViewHolder.mTextViewVesselRegNo.setText("-NA-");
                }
                if (mArrayListUnInstallList.get(position).getUninst_device_type_name() != null && !mArrayListUnInstallList.get(position).getUninst_device_type_name().equalsIgnoreCase("")) {
                    mViewHolder.mTextViewType.setText(mArrayListUnInstallList.get(position).getUninst_device_type_name());
                } else {
                    mViewHolder.mTextViewType.setText("-NA-");
                }

                mViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mArrayListUnInstallList != null) {
                            if (position < mArrayListUnInstallList.size()) {
                                FragmentHistoryDetails mFragmentHistoryDetails = new FragmentHistoryDetails();
                                Bundle mBundle = new Bundle();
                                mBundle.putInt("intent_history_type", 1);
                                mBundle.putSerializable("intent_uninstall_data", mArrayListUnInstallList.get(position));
                                mActivity.replacesFragment(mFragmentHistoryDetails, true, mBundle, 1);
                            }
                        }
                    }
                });
            }

        }

        @Override
        public int getItemCount() {
            return mArrayListUnInstallList.size();
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
