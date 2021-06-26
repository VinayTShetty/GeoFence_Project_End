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
import com.succorfish.installer.Vo.VoInspection;
import com.succorfish.installer.Vo.VoInstallationResponse;
import com.succorfish.installer.db.DataHolder;
import com.succorfish.installer.helper.PreferenceHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

public class FragmentHistoryInspection extends Fragment {
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
    public ArrayList<VoInspection> mArrayListInspectionList = new ArrayList<>();
    UnInstallationAdapter mUnInstallationAdapter;
    private boolean isCalling = true;
    LinearLayoutManager mLayoutManager;
    private SimpleDateFormat mSimpleDateFormatDate;
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
        mSimpleDateFormatDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        mSimpleDateFormatDateDisplay = new SimpleDateFormat(PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedDateFormat() + " HH:mm", Locale.getDefault());

        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.colorAccent,
                R.color.colorGreen,
                R.color.colorBlue,
                R.color.colorWhiteLight);
        mCalendarCurrentTime = Calendar.getInstance();
        mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
        mUnInstallationAdapter = new UnInstallationAdapter();
        mRecyclerViewUnInstallation.setLayoutManager(mLayoutManager);
        mRecyclerViewUnInstallation.setAdapter(mUnInstallationAdapter);
        if (mActivity.mUtility.haveInternet()) {
            isCalling = true;
            /*get inspection history from server*/
            GetServerInspectionHistory(true);
        } else {
            isCalling = true;
            /*get inspection history from local db*/
            getDBInspectionList();
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
                        getDBInspectionList();
                    } else {
                        if (searchString.length() >= 3) {
                            getDBSearchInspectionList(searchString);
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
                    getDBInspectionList();
                } else {
                    if (searchString.length() >= 3) {
                        getDBSearchInspectionList(searchString);
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
                        getDBInspectionList();
                    } else {
                        if (searchString.length() >= 3) {
                            getDBSearchInspectionList(searchString);
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
                        getDBInspectionList();
                    } else {
                        mSwipeRefreshLayout.setRefreshing(false);
                        GetServerInspectionHistory(true);
                    }
                }
            }
        });
        return mViewRoot;
    }

    /*Get Inspection history list from local database*/
    private void getDBInspectionList() {
        mCalendarCurrentTime = Calendar.getInstance();
        DataHolder mDataHolder;
        mArrayListInspectionList = new ArrayList<>();
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableInspection + " where " + mActivity.mDbHelper.mFieldInspectionIsSync + "= 1" + " AND " + mActivity.mDbHelper.mFieldInspectionUserId + "= '" + PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserId() + "'" + " order by " + mActivity.mDbHelper.mFieldInspectionDateTimeStamp + " DESC";
            System.out.println("Local url " + url);
            mDataHolder = mActivity.mDbHelper.read(url);
            if (mDataHolder != null) {
                System.out.println("Local Device List " + url + " : " + mDataHolder.get_Listholder().size());
                for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
                    VoInspection mVoInspection = new VoInspection();
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

                    mArrayListInspectionList.add(mVoInspection);
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

    /*Search inspection list from local database*/
    private void getDBSearchInspectionList(String string_search) {
        DataHolder mDataHolder;
        mArrayListInspectionList = new ArrayList<>();
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableInspection + " where " + mActivity.mDbHelper.mFieldInspectionIsSync + "= 1" + " AND " + mActivity.mDbHelper.mFieldInspectionUserId + "= '" + PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserId() + "'" + " AND " + mActivity.mDbHelper.mFieldInspectionVesselName + " like '%" + string_search + "%'" + " OR " + mActivity.mDbHelper.mFieldInspectionVesselRegNo + " like '%" + string_search + "%'" + " order by " + mActivity.mDbHelper.mFieldInspectionDateTimeStamp + " DESC";
            System.out.println("Local url " + url);
            mDataHolder = mActivity.mDbHelper.read(url);
            if (mDataHolder != null) {
                System.out.println("Local Device List " + url + " : " + mDataHolder.get_Listholder().size());
                for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
                    VoInspection mVoInspection = new VoInspection();
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

                    mArrayListInspectionList.add(mVoInspection);
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
        isCalling = false;
        checkAdapterIsEmpty();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    /*API to Get Inspection history from server*/
    public void GetServerInspectionHistory(final boolean isShowProgress) {
        mActivity.mUtility.hideKeyboard(mActivity);
        if (isShowProgress) {
            mActivity.mUtility.ShowProgress();
        }
        Call<String> mVoInstallationHistoryCall = mActivity.mApiService.getAllInspectionHistory(PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserId());
        mVoInstallationHistoryCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (isAdded()) {
                    Gson gson = new Gson();
                    if (response.code() == 200 || response.isSuccessful()) {
                        System.out.println("response mInspHistoryData---------" + response.body());
                        try {
                            TypeToken<List<VoInstallationResponse>> token = new TypeToken<List<VoInstallationResponse>>() {
                            };
                            List<VoInstallationResponse> mVoInstallationHistory = gson.fromJson(response.body().toString(), token.getType());
                            System.out.println("response mInspHistoryData...... " + new Gson().toJson(mVoInstallationHistory));
                            if (mVoInstallationHistory != null) {
                                if (mVoInstallationHistory.size() > 0) {
                                    for (int i = 0; i < mVoInstallationHistory.size(); i++) {
                                        ContentValues mContentValues = new ContentValues();
                                        mContentValues.put(mActivity.mDbHelper.mFieldInspectionServerId, mVoInstallationHistory.get(i).getId());
                                        mContentValues.put(mActivity.mDbHelper.mFieldInspectionUserId, PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserId());
                                        mContentValues.put(mActivity.mDbHelper.mFieldInspectionDeviceIMEINo, mVoInstallationHistory.get(i).getDeviceImei());
                                        mContentValues.put(mActivity.mDbHelper.mFieldInspectionDeviceServerId, mVoInstallationHistory.get(i).getDeviceId());
                                        mContentValues.put(mActivity.mDbHelper.mFieldInspectionDeviceLocalId, "1");
                                        mContentValues.put(mActivity.mDbHelper.mFieldInspectionDevicName, "");
                                        mContentValues.put(mActivity.mDbHelper.mFieldInspectionDeviceWarranty_status, "");
                                        mContentValues.put(mActivity.mDbHelper.mFieldInspectionDeviceTypeName, mVoInstallationHistory.get(i).getDeviceType());
                                        mContentValues.put(mActivity.mDbHelper.mFieldInspectionDateTime, mVoInstallationHistory.get(i).getDate());

                                        mContentValues.put(mActivity.mDbHelper.mFieldInspectionVesselServerId, mVoInstallationHistory.get(i).getRealAssetId());
                                        mContentValues.put(mActivity.mDbHelper.mFieldInspectionVesselName, mVoInstallationHistory.get(i).getRealAssetName());
                                        mContentValues.put(mActivity.mDbHelper.mFieldInspectionVesselRegNo, mVoInstallationHistory.get(i).getRealAssetRegNo());

                                        if (mVoInstallationHistory.get(i).getContactInfo() != null) {
                                            mContentValues.put(mActivity.mDbHelper.mFieldInspectionOwnerName, mVoInstallationHistory.get(i).getContactInfo().getName());
                                            mContentValues.put(mActivity.mDbHelper.mFieldInspectionOwnerAddress, mVoInstallationHistory.get(i).getContactInfo().getAddress());
                                            mContentValues.put(mActivity.mDbHelper.mFieldInspectionOwnerCity, mVoInstallationHistory.get(i).getContactInfo().getCity());
                                            mContentValues.put(mActivity.mDbHelper.mFieldInspectionOwnerState, mVoInstallationHistory.get(i).getContactInfo().getState());
                                            mContentValues.put(mActivity.mDbHelper.mFieldInspectionOwnerZipcode, mVoInstallationHistory.get(i).getContactInfo().getZipCode());
                                            mContentValues.put(mActivity.mDbHelper.mFieldInspectionOwnerEmail, mVoInstallationHistory.get(i).getContactInfo().getEmail());
                                            mContentValues.put(mActivity.mDbHelper.mFieldInspectionOwnerMobileNo, mVoInstallationHistory.get(i).getContactInfo().getTelephone());
                                        }

                                        mContentValues.put(mActivity.mDbHelper.mFieldInspectionResult, mVoInstallationHistory.get(i).getNotes());
                                        mContentValues.put(mActivity.mDbHelper.mFieldInspectionActionTaken, mVoInstallationHistory.get(i).getOperation());
                                        mContentValues.put(mActivity.mDbHelper.mFieldInspectionWarrentyReturn, "");
                                        mContentValues.put(mActivity.mDbHelper.mFieldInspectionLocalSignUrl, "");
                                        mContentValues.put(mActivity.mDbHelper.mFieldInspectionServerSignUrl, "");
                                        mContentValues.put(mActivity.mDbHelper.mFieldInspectionLocalInspectorSignUrl, "");
                                        mContentValues.put(mActivity.mDbHelper.mFieldInspectionServerInspectorSignUrl, "");
                                        mContentValues.put(mActivity.mDbHelper.mFieldInspectionPdfUrl, "");
                                        mContentValues.put(mActivity.mDbHelper.mFieldInspectionUpdatedDate, mVoInstallationHistory.get(i).getDate());
                                        mContentValues.put(mActivity.mDbHelper.mFieldInspectionDateTimeStamp, mVoInstallationHistory.get(i).getDate());
                                        mContentValues.put(mActivity.mDbHelper.mFieldInspectionStatus, "1");
                                        mContentValues.put(mActivity.mDbHelper.mFieldInspectionIsSync, "1");
                                        Map<String, String> attachedFile = mVoInstallationHistory.get(i).getAttachedFilesList();
                                        for (Map.Entry<String, String> entry : attachedFile.entrySet()) {
                                            if (entry.getValue().toString().contains(".pdf")) {
                                                mContentValues.put(mActivity.mDbHelper.mFieldInspectionPdfUrl, entry.getKey() + "/" + mVoInstallationHistory.get(i).getId());
                                            }
                                        }
                                        String isInstallExistInDB = CheckRecordExistInInspectionDB(mVoInstallationHistory.get(i).getId());
                                        if (isInstallExistInDB.equalsIgnoreCase("-1")) {
                                            mContentValues.put(mActivity.mDbHelper.mFieldInspectionCreatedDate, mVoInstallationHistory.get(i).getDate());
                                            int isInsertInstall = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableInspection, mContentValues);
                                            if (isInsertInstall != -1) {
                                                System.out.println("Device Inspection Added In Local Db");
                                            } else {
                                                System.out.println("Device Inspection Adding In Local DB");
                                            }
                                        } else {
                                            String[] mArray = new String[]{isInstallExistInDB};
                                            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInspection, mContentValues, mActivity.mDbHelper.mFieldInspectionServerId + "=?", mArray);
                                            System.out.println("Device Inspection updated In Local Db");
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    mActivity.mUtility.HideProgress();
                    getDBInspectionList();
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
                    getDBInspectionList();
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

    /*Check record exist in inspection table or not*/
    public String CheckRecordExistInInspectionDB(String serverInspId) {
        DataHolder mDataHolder = new DataHolder();
        String url = "select * from " + mActivity.mDbHelper.mTableInspection + " where " + mActivity.mDbHelper.mFieldInspectionServerId + "= '" + serverInspId + "'";
        mDataHolder = mActivity.mDbHelper.read(url);
        if (mDataHolder != null) {
            System.out.println(" Install List : " + url + " : " + mDataHolder.get_Listholder().size());
            if (mDataHolder != null && mDataHolder.get_Listholder().size() != 0) {
                return mDataHolder.get_Listholder().get(0).get(mActivity.mDbHelper.mFieldInspectionServerId);
            } else {
                return "-1";
            }
        }
        return "-1";
    }

    private void checkAdapterIsEmpty() {
        if (mArrayListInspectionList != null) {
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

    /*Adapter*/
    public class UnInstallationAdapter extends RecyclerView.Adapter<UnInstallationAdapter.ViewHolder> {

        @Override
        public UnInstallationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_history_install_list, parent, false);
            return new UnInstallationAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final UnInstallationAdapter.ViewHolder mViewHolder, final int position) {
            if (isAdded()) {
                if (mArrayListInspectionList.get(position).getInsp_device_imei_no() != null && !mArrayListInspectionList.get(position).getInsp_device_imei_no().equalsIgnoreCase("")) {
                    mViewHolder.mTextViewIMEINo.setText(mArrayListInspectionList.get(position).getInsp_device_imei_no());
                } else {
                    mViewHolder.mTextViewIMEINo.setText("-NA-");
                }
                if (mArrayListInspectionList.get(position).getInsp_vessel_name() != null && !mArrayListInspectionList.get(position).getInsp_vessel_name().equalsIgnoreCase("")) {
                    mViewHolder.mTextViewVesselName.setText(mArrayListInspectionList.get(position).getInsp_vessel_name());
                } else {
                    mViewHolder.mTextViewVesselName.setText("-NA-");
                }
                if (mArrayListInspectionList.get(position).getInsp_date_time() != null && !mArrayListInspectionList.get(position).getInsp_date_time().equalsIgnoreCase("") && !mArrayListInspectionList.get(position).getInsp_date_time().equalsIgnoreCase("null")) {
                    try {
                        mViewHolder.mTextViewTime.setVisibility(View.VISIBLE);
                        Calendar mCalendar = Calendar.getInstance();
                        mCalendar.setTimeInMillis(Long.parseLong(mArrayListInspectionList.get(position).getInsp_date_time()));
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
                if (mArrayListInspectionList.get(position).getInsp_vessel_regi_no() != null && !mArrayListInspectionList.get(position).getInsp_vessel_regi_no().equalsIgnoreCase("")) {
                    mViewHolder.mTextViewVesselRegNo.setText(mArrayListInspectionList.get(position).getInsp_vessel_regi_no());
                } else {
                    mViewHolder.mTextViewVesselRegNo.setText("-NA-");
                }
                if (mArrayListInspectionList.get(position).getInsp_device_type_name() != null && !mArrayListInspectionList.get(position).getInsp_device_type_name().equalsIgnoreCase("")) {
                    mViewHolder.mTextViewType.setText(mArrayListInspectionList.get(position).getInsp_device_type_name());
                } else {
                    mViewHolder.mTextViewType.setText("-NA-");
                }

                mViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mArrayListInspectionList != null) {
                            if (position < mArrayListInspectionList.size()) {
                                FragmentHistoryDetails mFragmentHistoryDetails = new FragmentHistoryDetails();
                                Bundle mBundle = new Bundle();
                                mBundle.putInt("intent_history_type", 2);
                                mBundle.putSerializable("intent_inspection_data", mArrayListInspectionList.get(position));
                                mActivity.replacesFragment(mFragmentHistoryDetails, true, mBundle, 1);
                            }
                        }
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mArrayListInspectionList.size();
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
