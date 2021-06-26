package com.succorfish.fisherman.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.succorfish.fisherman.LoginActivity;
import com.succorfish.fisherman.MainActivity;
import com.succorfish.fisherman.R;
import com.succorfish.fisherman.Vo.VoEventService;
import com.succorfish.fisherman.Vo.VoLastInstallation;
import com.succorfish.fisherman.Vo.VoVessel;
import com.succorfish.fisherman.interfaces.onAlertDialogCallBack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentAsset extends Fragment {
    View mViewRoot;
    private Unbinder unbinder;
    MainActivity mActivity;

    private SimpleDateFormat mSimpleDateFormatDateDisplay;

    ArrayList<VoVessel> mVoVesselList = new ArrayList<>();
    private ArrayList<VoVessel> mVoVesselListTemp = new ArrayList<>();
    AssetListAdapter mAssetListAdapter;
    @BindView(R.id.fragment_asset_rv)
    RecyclerView mRecyclerView;
    @BindView(R.id.fragment_asset_tv_no_asset)
    TextView mTextViewNoAsset;
    @BindView(R.id.fragment_search_item_mSearch)
    SearchView mSearchView;
    LinearLayoutManager mLayoutManager;
//    GridLayoutManager  mLayoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mVoVesselList = new ArrayList<>();
        mVoVesselListTemp = new ArrayList<>();
        mSimpleDateFormatDateDisplay = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        mActivity = (MainActivity) getActivity();
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_test_device, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);

        mActivity.mTextViewTitle.setVisibility(View.GONE);
        mActivity.mImageViewBack.setVisibility(View.GONE);
        mActivity.mImageViewHeaderLogo.setVisibility(View.VISIBLE);
        mActivity.mImageViewDrawer.setVisibility(View.VISIBLE);
        mActivity.mImageViewAdd.setVisibility(View.VISIBLE);


        if (mActivity.mUtility.haveInternet()) {
            getVesselAssetList();
        } else {
            mActivity.mUtility.errorDialog(getResources().getString(R.string.str_no_internet_connection));
        }
        mActivity.mImageViewAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.mUtility.hideKeyboard(mActivity);
                mActivity.mUtility.errorDialogWithYesNoCallBack("Refresh Asset", "Are you sure you want to refresh asset list?", "Yes", "No", true, 0, new onAlertDialogCallBack() {
                    @Override
                    public void PositiveMethod(DialogInterface dialog, int id) {
                        if (mActivity.mUtility.haveInternet()) {
                            mSearchView.setQuery("", false);
                            getVesselAssetList();
                        } else {
                            mActivity.mUtility.errorDialog(getResources().getString(R.string.str_no_internet_connection));
                        }
                    }

                    @Override
                    public void NegativeMethod(DialogInterface dialog, int id) {

                    }
                });
            }
        });
        mSearchView.setQuery("", false);
        mSearchView.setIconified(true);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mActivity.mUtility.hideKeyboard(mActivity);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (mAssetListAdapter != null) {
                    mAssetListAdapter.getFilter().filter(query);
                }
                return false;
            }
        });
//        mSearchView.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
//                if (isAdded()) {
//                    String text = mEditTextSearch.getText().toString().trim();
//                    if (mAssetListAdapter != null) {
//                        if (!text.equalsIgnoreCase("")) {
//                            mAssetListAdapter.getFilter().filter(text);
//                            mActivity.runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    mAssetListAdapter.notifyDataSetChanged();
//                                }
//                            });
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });
        return mViewRoot;
    }

    public class WrapContentLinearLayoutManager extends LinearLayoutManager {
        public WrapContentLinearLayoutManager(Context context) {
            super(context);
        }

        public WrapContentLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        public WrapContentLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
    }

    public void getVesselAssetList() {
        mActivity.mUtility.ShowProgress();
        mActivity.mUtility.hideKeyboard(mActivity);
        mSearchView.clearFocus();
        Map<String, Object> jsonParams = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            jsonParams = new ArrayMap<>();
        } else {

        }
        //put something inside the map, could be null
        jsonParams.put("filters", "");
        jsonParams.put("jsonFunction", "");
        jsonParams.put("value", "");
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), (new JSONObject(jsonParams)).toString());

        Call<String> mLogin = mActivity.mApiService.getAllAssetList("OBJECT", body);
        System.out.println("URL-" + mLogin.request().url().toString());
        mLogin.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (isAdded()) {
                    Gson gson = new Gson();
                    if (response.code() == 200 || response.isSuccessful()) {
                        mVoVesselList = new ArrayList<>();
                        mVoVesselListTemp = new ArrayList<>();
                        System.out.println("response mVesselData---------" + response.body());
                        try {
                            Object json = new JSONTokener(response.body().toString()).nextValue();
                            if (json instanceof JSONObject) {
                                System.out.println("JSON_OBJECT");
                            } else if (json instanceof JSONArray) {
                                System.out.println("JSONArray");
                                TypeToken<List<VoVessel>> token = new TypeToken<List<VoVessel>>() {
                                };
                                List<VoVessel> mVoVesselListResponse = gson.fromJson(response.body(), token.getType());
//                        String json = gson.toJson(mVoVesselListResponse);
//                        System.out.println("response mVesselList---------" + json);
                                if (mVoVesselListResponse != null) {
                                    if (mVoVesselListResponse.size() > 0) {
                                        mVoVesselList.addAll(mVoVesselListResponse);
                                        mVoVesselListTemp.addAll(mVoVesselListResponse);


                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } else if (response.code() == 401) {
                        mActivity.mUtility.errorDialogWithCallBack(getResources().getString(R.string.str_logout_session_expired), new onAlertDialogCallBack() {
                            @Override
                            public void PositiveMethod(DialogInterface dialog, int id) {
                                mActivity.mPreferenceHelper.ResetPrefData();
                                Intent mIntent = new Intent(mActivity, LoginActivity.class);
                                startActivity(mIntent);
                                mActivity.finish();
                            }

                            @Override
                            public void NegativeMethod(DialogInterface dialog, int id) {

                            }
                        });
                    } else {
                        mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_someting_wrong));
                    }
                    if (mAssetListAdapter == null) {
                        mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
                        mRecyclerView.setLayoutManager(mLayoutManager);
                        mAssetListAdapter = new AssetListAdapter();
                        mRecyclerView.setAdapter(mAssetListAdapter);
                        mAssetListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                            @Override
                            public void onChanged() {
                                super.onChanged();
                                checkAdapterIsEmpty();
                            }
                        });
                    } else {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    mAssetListAdapter.notifyDataSetChanged();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    checkAdapterIsEmpty();
                }

                mActivity.mUtility.HideProgress();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                mActivity.mUtility.HideProgress();
//                showMessageRedAlert(getCurrentFocus(), getResources().getString(R.string.str_server_error_try_again), getResources().getString(R.string.str_ok));
            }
        });
    }

//    public void updateView(int position, VoVessel mVoVessel, MainActivity activity) {
//        this.mActivity = activity;
//        this.mVoVessel = mVoVessel;
//        // do something to update the fragment
//        mStringDeviceId = mVoVessel.getDeviceId();
//        System.out.println("JD- updateView1 " + position);
//        System.out.println("JD- getDeviceId " + mVoVessel.getDeviceId());
////        mButtonCall.performClick();
//        if (mActivity.mUtility.haveInternet()) {
//            if (mStringDeviceId != null && !mStringDeviceId.equalsIgnoreCase("") && !mStringDeviceId.equalsIgnoreCase("null")) {
//                getTestInstallInformation(true, mStringDeviceId);
//            } else {
//                displayErrorWithGoBack();
//            }
//        } else {
//            mActivity.mUtility.errorDialog(getResources().getString(R.string.str_no_internet_connection));
//        }
//
//    }

    private void getTestInstallInformation(boolean showProgress, final String mStringDeviceId, final int listPosition) {
        mActivity.mUtility.hideKeyboard(mActivity);
        mVoVesselListTemp.get(listPosition).setStatus(0);
        mActivity.mUtility.hideKeyboard(getActivity());
        mSearchView.clearFocus();
        if (showProgress)
            mActivity.mUtility.ShowProgress();
        Call<String> mVoLastInstallationCall = mActivity.mApiService.getTestDetailsAPI(mStringDeviceId);
        System.out.println("URL-" + mVoLastInstallationCall.request().url().toString());
        mVoLastInstallationCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (isAdded()) {
                    mActivity.mUtility.HideProgress();
                    Gson gson = new Gson();
                    if (response.code() == 200 || response.isSuccessful()) {
                        System.out.println("response mAddInstData---------" + response.body().toString());
                        if (response.body() != null && !response.body().equalsIgnoreCase("") && !response.body().equalsIgnoreCase("null")) {
                            VoLastInstallation mVoLastInstallation = gson.fromJson(response.body(), VoLastInstallation.class);
//                            String json = gson.toJson(mVoLastInstallation);
//                            System.out.println("response mVoLastInstallationData---------" + json);
                            if (mVoLastInstallation != null) {
                                if (mVoLastInstallation.getLat() != null && !mVoLastInstallation.getLat().equalsIgnoreCase("") && !mVoLastInstallation.getLat().equalsIgnoreCase("null")) {
                                    mVoVesselListTemp.get(listPosition).setLat(mVoLastInstallation.getLat());
                                }
                                if (mVoLastInstallation.getLng() != null && !mVoLastInstallation.getLng().equalsIgnoreCase("") && !mVoLastInstallation.getLng().equalsIgnoreCase("null")) {
                                    mVoVesselListTemp.get(listPosition).setLng(mVoLastInstallation.getLng());
                                }
                                if (mVoLastInstallation.getGeneratedDate() != null && !mVoLastInstallation.getGeneratedDate().equalsIgnoreCase("") && !mVoLastInstallation.getGeneratedDate().equalsIgnoreCase("null")) {
                                    try {
                                        System.out.println("generatedDate - " + mVoLastInstallation.getGeneratedDate());
                                        Calendar calCurrentDate = Calendar.getInstance(Locale.getDefault());
                                        Calendar cal = Calendar.getInstance(Locale.getDefault());
                                        cal.setTimeInMillis(Long.parseLong(mVoLastInstallation.getGeneratedDate()));
                                        mVoVesselListTemp.get(listPosition).setLastInstallatDate(mSimpleDateFormatDateDisplay.format(cal.getTime()));
                                        long different = calCurrentDate.getTimeInMillis() - cal.getTimeInMillis();
                                        if (different <= 7200000) {
                                            mVoVesselListTemp.get(listPosition).setStatus(2);
                                        } else {
                                            mVoVesselListTemp.get(listPosition).setStatus(3);
                                        }
                                        if (mActivity.mUtility.haveInternet()) {
                                            mActivity.mUtility.ShowProgress();
                                            getEventServiceInformation(0, mStringDeviceId, listPosition);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                            } else {
                                displayErrorWithGoBack();
                            }
                        } else {
                            mVoVesselListTemp.get(listPosition).setStatus(1);
                        }
                        if (mAssetListAdapter != null) {
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        mAssetListAdapter.notifyDataSetChanged();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    } else {
                        displayErrorWithGoBack();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                mActivity.mUtility.HideProgress();
                displayErrorWithGoBack();
            }
        });
    }

    private void displayErrorWithGoBack() {
        mActivity.mUtility.errorDialogWithCallBack(getResources().getString(R.string.str_server_error_someting_wrong), new onAlertDialogCallBack() {
            @Override
            public void PositiveMethod(DialogInterface dialog, int id) {

            }

            @Override
            public void NegativeMethod(DialogInterface dialog, int id) {

            }
        });
    }

//    private void displayTestData() {
////        if (mStringImeiNo != null && !mStringImeiNo.equalsIgnoreCase("")) {
////            mTextViewIMEICode.setText(mStringImeiNo);
////        } else {
////            mTextViewIMEICode.setText("NA");
////        }
//        if (mVoLastInstallation.getGeneratedDate() != null && !mVoLastInstallation.getGeneratedDate().equalsIgnoreCase("") && !mVoLastInstallation.getGeneratedDate().equalsIgnoreCase("null")) {
//            try {
//                System.out.println("generatedDate - " + mVoLastInstallation.getGeneratedDate());
//                Calendar calCurrentDate = Calendar.getInstance(Locale.UK);
//                Calendar cal = Calendar.getInstance(Locale.UK);
//                cal.setTimeInMillis(Long.parseLong(mVoLastInstallation.getGeneratedDate()));
//                mTextViewGeneratedDate.setText(mSimpleDateFormatDateDisplay.format(cal.getTime()));
//                long different = calCurrentDate.getTimeInMillis() - cal.getTimeInMillis();
//                if (different <= 7200000) {
//                    mIntInstalletionStatus = 2;
//                    mTextViewStatus.setText("READY TO GO FISHING");
//                    mTextViewStatus.setTextColor(getResources().getColor(R.color.colorGreen));
//                    mImageViewStatus.setImageResource(R.drawable.ic_boat_green);
//                    mImageViewStatusInfo.setVisibility(View.GONE);
////                    mUtility.errorDialogWithCallBack("READY TO GO FISHING", new onAlertDialogCallBack() {
////                        @Override
////                        public void PositiveMethod(DialogInterface dialog, int id) {
////
////                        }
////
////                        @Override
////                        public void NegativeMethod(DialogInterface dialog, int id) {
////
////                        }
////                    });
//                } else {
//                    mImageViewStatusInfo.setVisibility(View.VISIBLE);
//                    mIntInstalletionStatus = 1;
//                    mTextViewStatus.setText("CAN'T GO FISHING");
//                    mTextViewStatus.setTextColor(getResources().getColor(R.color.colorRed));
//                    mImageViewStatus.setImageResource(R.drawable.ic_boat_red);
////                    mUtility.errorDialogWithCallBack("CAN'T GO FISHING. There was no position report in last 2 hours.", new onAlertDialogCallBack() {
////                        @Override
////                        public void PositiveMethod(DialogInterface dialog, int id) {
////
////                        }
////
////                        @Override
////                        public void NegativeMethod(DialogInterface dialog, int id) {
////
////                        }
////                    });
//                }
//            } catch (Exception e) {
//                mTextViewGeneratedDate.setText("NA");
//                e.printStackTrace();
//            }
//        } else {
//            mTextViewGeneratedDate.setText("NA");
//        }
//
//        if (mVoLastInstallation.getLat() != null && !mVoLastInstallation.getLat().equalsIgnoreCase("") && !mVoLastInstallation.getLat().equalsIgnoreCase("null")) {
//            mTextViewLatitude.setText(mVoLastInstallation.getLat());
//            mStringLatitude = mVoLastInstallation.getLat();
//        } else {
//            mTextViewLatitude.setText("NA");
//        }
//        if (mVoLastInstallation.getLng() != null && !mVoLastInstallation.getLng().equalsIgnoreCase("") && !mVoLastInstallation.getLng().equalsIgnoreCase("null")) {
//            mTextViewLongitude.setText(mVoLastInstallation.getLng());
//            mStringLongitude = mVoLastInstallation.getLng();
//        } else {
//            mTextViewLongitude.setText("NA");
//        }
//    }

    private void checkAdapterIsEmpty() {
        if (mAssetListAdapter != null) {
            System.out.println("mDeviceListAdapter.getItemCount()-" + mAssetListAdapter.getItemCount());
            if (mAssetListAdapter.getItemCount() == 0) {
                mTextViewNoAsset.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            } else {
                mTextViewNoAsset.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    String batteryLabel = "";
    String gigoLabel = "";

    public class AssetListAdapter extends RecyclerView.Adapter<AssetListAdapter.ViewHolder> implements Filterable {

        @Override
        public AssetListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_asset_list_item, parent, false);
            return new AssetListAdapter.ViewHolder(itemView);
        }

        @Override
        public int getItemCount() {
            return mVoVesselListTemp.size();
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int relativePosition) {
            if (mVoVesselListTemp.get(relativePosition).getName() != null && !mVoVesselListTemp.get(relativePosition).getName().equalsIgnoreCase("")) {
                holder.mTextViewAssetName.setText(mVoVesselListTemp.get(relativePosition).getName());
            } else {
                holder.mTextViewAssetName.setText("");
            }
            holder.mLinearLayoutEventData.setVisibility(View.GONE);
            if (mVoVesselListTemp.get(relativePosition).getStatus() == 0) {
                holder.mImageViewStatus.setColorFilter(ContextCompat.getColor(mActivity, R.color.colorWhite));
                holder.mTextViewStatusMsg.setTextColor(ContextCompat.getColor(mActivity, R.color.colorWhite));
                holder.mTextViewStatusMsg.setText("WAITING FOR STATUS");
                holder.mTextViewReportDate.setText("NA");
            } else if (mVoVesselListTemp.get(relativePosition).getStatus() == 1) {
                holder.mImageViewStatus.setColorFilter(ContextCompat.getColor(mActivity, R.color.colorWhite));
                holder.mTextViewStatusMsg.setTextColor(ContextCompat.getColor(mActivity, R.color.colorWhite));
                holder.mTextViewStatusMsg.setText("WAITING FOR STATUS");
                holder.mTextViewReportDate.setText("NA");
            } else if (mVoVesselListTemp.get(relativePosition).getStatus() == 2) {
                holder.mLinearLayoutEventData.setVisibility(View.VISIBLE);
                holder.mImageViewStatus.setColorFilter(ContextCompat.getColor(mActivity, R.color.colorGreen));
                holder.mTextViewStatusMsg.setTextColor(ContextCompat.getColor(mActivity, R.color.colorGreen));
                holder.mTextViewStatusMsg.setText("READY TO GO!");
                if (mVoVesselListTemp.get(relativePosition).getLastInstallatDate() != null && !mVoVesselListTemp.get(relativePosition).getLastInstallatDate().equalsIgnoreCase("")) {
                    holder.mTextViewReportDate.setText(mVoVesselListTemp.get(relativePosition).getLastInstallatDate());
                } else {
                    holder.mTextViewReportDate.setText("NA");
                }
            } else if (mVoVesselListTemp.get(relativePosition).getStatus() == 3) {
                holder.mLinearLayoutEventData.setVisibility(View.VISIBLE);
                holder.mImageViewStatus.setColorFilter(ContextCompat.getColor(mActivity, R.color.colorRed));
                holder.mTextViewStatusMsg.setTextColor(ContextCompat.getColor(mActivity, R.color.colorRed));
                holder.mTextViewStatusMsg.setText("NO REPORTS IN PAST 2 HOURS");
                if (mVoVesselListTemp.get(relativePosition).getLastInstallatDate() != null && !mVoVesselListTemp.get(relativePosition).getLastInstallatDate().equalsIgnoreCase("")) {
                    holder.mTextViewReportDate.setText(mVoVesselListTemp.get(relativePosition).getLastInstallatDate());
                } else {
                    holder.mTextViewReportDate.setText("NA");
                }
            } else {
                holder.mImageViewStatus.setColorFilter(ContextCompat.getColor(mActivity, R.color.colorWhite));
                holder.mTextViewStatusMsg.setTextColor(ContextCompat.getColor(mActivity, R.color.colorWhite));
                holder.mTextViewStatusMsg.setText("WAITING FOR STATUS");
                holder.mTextViewReportDate.setText("NA");
            }
            if (mVoVesselListTemp.get(relativePosition).getBattery() != null && !mVoVesselListTemp.get(relativePosition).getBattery().equalsIgnoreCase("")) {
                holder.mTextViewBattery.setText(Html.fromHtml("<b><font color='#FFFFFF'>" + getResources().getString(R.string.str_battery) + "</font></b> " + mVoVesselListTemp.get(relativePosition).getBatteryPercentage() + "% at " + mVoVesselListTemp.get(relativePosition).getBattery()));
            } else {
                holder.mTextViewBattery.setText(Html.fromHtml("<b><font color='#FFFFFF'>" + getResources().getString(R.string.str_battery) + "</font></b> " + "-NA-"));
            }
            if (mVoVesselListTemp.get(relativePosition).getPowerApply() != null && !mVoVesselListTemp.get(relativePosition).getPowerApply().equalsIgnoreCase("")) {
                if (mVoVesselListTemp.get(relativePosition).getPowerSourceType().equals("X00")) {
                    batteryLabel = "Power removed : ";
                } else if (mVoVesselListTemp.get(relativePosition).getPowerSourceType().equals("X01")) {
                    batteryLabel = "Rechargable power applied : ";
                } else if (mVoVesselListTemp.get(relativePosition).getPowerSourceType().equals("X02")) {
                    batteryLabel = "Power applied : ";
                } else {
                    batteryLabel = "Power removed : ";
                }
                holder.mTextViewPowerApply.setText(Html.fromHtml("<b><font color='#FFFFFF'>" + batteryLabel + "</font></b>" + mVoVesselListTemp.get(relativePosition).getPowerApply()));
            } else {
                batteryLabel = "Power removed : ";
                holder.mTextViewPowerApply.setText(Html.fromHtml("<b><font color='#FFFFFF'>" + batteryLabel + "</font></b>" + "-NA-"));
            }
            if (mVoVesselListTemp.get(relativePosition).getRfid() != null && !mVoVesselListTemp.get(relativePosition).getRfid().equalsIgnoreCase("")) {
                holder.mTextViewRFID.setText(Html.fromHtml("<b><font color='#FFFFFF'>" + getResources().getString(R.string.str_RFID) + "</font></b> " + mVoVesselListTemp.get(relativePosition).getRfid()));
            } else {
                holder.mTextViewRFID.setText(Html.fromHtml("<b><font color='#FFFFFF'>" + getResources().getString(R.string.str_RFID) + "</font></b> " + "-NA-"));
            }
            if (mVoVesselListTemp.get(relativePosition).getGearOut() != null && !mVoVesselListTemp.get(relativePosition).getGearOut().equalsIgnoreCase("")) {
                if (mVoVesselListTemp.get(relativePosition).getGigoType().equals("1")) {
                    gigoLabel = "Gear OUT : ";
                } else {
                    gigoLabel = "Gear IN : ";
                }
                holder.mTextViewGearOut.setText(Html.fromHtml("<b><font color='#FFFFFF'>" + gigoLabel + "</font></b>" + mVoVesselListTemp.get(relativePosition).getGearOut()));
            } else {
                gigoLabel = "Gear : ";
                holder.mTextViewGearOut.setText(Html.fromHtml("<b><font color='#FFFFFF'>" + gigoLabel + "</font></b>" + "-NA-"));
            }
//            if (mVoVesselListTemp.get(relativePosition).getPowerSourceType() != null && !mVoVesselListTemp.get(relativePosition).getPowerSourceType().equalsIgnoreCase("")) {
//                if (mVoVesselListTemp.get(relativePosition).getPowerSourceType().equals("X00")) {
//                    holder.mTextViewPowerApplyLbl.setText("Battery power");
//                } else if (mVoVesselListTemp.get(relativePosition).getPowerSourceType().equals("X01")) {
//                    holder.mTextViewPowerApplyLbl.setText("USB connected");
//                } else if (mVoVesselListTemp.get(relativePosition).getPowerSourceType().equals("X02")) {
//                    holder.mTextViewPowerApplyLbl.setText("External connected");
//                }
//            }
//            if (mVoVesselListTemp.get(relativePosition).getGigoType() != null && !mVoVesselListTemp.get(relativePosition).getGigoType().equalsIgnoreCase("")) {
//                if (mVoVesselListTemp.get(relativePosition).getGigoType().equals("1")) {
//                    holder.mTextViewGearOutLbl.setText("Gear OUT");
//                } else {
//                    holder.mTextViewGearOutLbl.setText("Gear IN");
//                }
//            }
//            holder.mImageViewInfo.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    mActivity.mUtility.hideKeyboard(mActivity);
//                    if (mVoVesselListTemp != null) {
//                        if (relativePosition < mVoVesselListTemp.size()) {
//                            String mStrDisplayMsg;
//                            if (mVoVesselListTemp.get(relativePosition).getStatus() == 0) {
//                                mStrDisplayMsg = "WAITING FOR STATUS";
//                            } else if (mVoVesselListTemp.get(relativePosition).getStatus() == 1) {
//                                mStrDisplayMsg = "Device has never submitted any positional data to the system";
//                            } else if (mVoVesselListTemp.get(relativePosition).getStatus() == 2) {
//                                mStrDisplayMsg = "READY TO GO FISHING";
//                            } else if (mVoVesselListTemp.get(relativePosition).getStatus() == 3) {
//                                mStrDisplayMsg = "CAN'T GO FISHING. There was no position report in last 2 hours.";
//                            } else {
//                                mStrDisplayMsg = "WAITING FOR STATUS";
//                            }
//                            mActivity.mUtility.errorDialogWithCallBack(mStrDisplayMsg, new onAlertDialogCallBack() {
//                                @Override
//                                public void PositiveMethod(DialogInterface dialog, int id) {
//
//                                }
//
//                                @Override
//                                public void NegativeMethod(DialogInterface dialog, int id) {
//
//                                }
//                            });
//
//                        }
//                    }
//                }
//            });

            holder.mLinearLayoutHelp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mActivity.mUtility.hideKeyboard(mActivity);
                    if (isAdded()) {
                        if (mActivity != null) {
                            FragmentHelp mFragmentHelp = new FragmentHelp();
                            FragmentAsset mFragmentAsset = new FragmentAsset();
                            mActivity.replacesFragment(mFragmentAsset, mFragmentHelp, true, null);
                        }
                    }
                }
            });
            holder.mLinearLayoutReload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.mUtility.hideKeyboard(mActivity);
                    if (mActivity.mUtility.haveInternet()) {
                        if (mVoVesselListTemp.get(relativePosition).getDeviceId() != null && !mVoVesselListTemp.get(relativePosition).getDeviceId().equalsIgnoreCase("") && !mVoVesselListTemp.get(relativePosition).getDeviceId().equalsIgnoreCase("null")) {
                            getTestInstallInformation(true, mVoVesselListTemp.get(relativePosition).getDeviceId(), relativePosition);
                        } else {
                            displayErrorWithGoBack();
                        }
                    } else {
                        mActivity.mUtility.errorDialog(getResources().getString(R.string.str_no_internet_connection));
                    }
                }
            });
            holder.mLinearLayoutMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mActivity.mUtility.hideKeyboard(mActivity);
                    if (mVoVesselListTemp != null) {
                        if (relativePosition < mVoVesselListTemp.size()) {
                            System.out.println("mStringLatitude-" + mVoVesselListTemp.get(relativePosition).getLat());
                            System.out.println("mStringLongitude-" + mVoVesselListTemp.get(relativePosition).getLng());
                            if (mVoVesselListTemp.get(relativePosition).getLat() != null && !mVoVesselListTemp.get(relativePosition).getLat().equalsIgnoreCase("") && mVoVesselListTemp.get(relativePosition).getLng() != null && !mVoVesselListTemp.get(relativePosition).getLng().equalsIgnoreCase("")) {
                                try {
                                    String geoUri = "http://maps.google.com/maps?q=loc:" + mVoVesselListTemp.get(relativePosition).getLat() + "," + mVoVesselListTemp.get(relativePosition).getLng() + " (" + mVoVesselListTemp.get(relativePosition).getRegNo() + ")";
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
                                    startActivity(intent);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                mActivity.mUtility.errorDialog("Location data not found.");
                            }
                        }
                    }

                }
            });

        }

        // ItemViewHolder Class for Items in each Section
        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.raw_asset_tv_asset_name)
            TextView mTextViewAssetName;
            @BindView(R.id.raw_asset_tv_lbl_status_msg)
            TextView mTextViewStatusMsg;
            @BindView(R.id.raw_asset_tv_generated_date)
            TextView mTextViewReportDate;
            @BindView(R.id.raw_asset_tv_battery)
            TextView mTextViewBattery;
            @BindView(R.id.raw_asset_tv_rfid)
            TextView mTextViewRFID;
            @BindView(R.id.raw_asset_tv_power_apply)
            TextView mTextViewPowerApply;
            //            @BindView(R.id.raw_asset_tv_lbl_power_apply)
//            TextView mTextViewPowerApplyLbl;
            @BindView(R.id.raw_asset_tv_gear_out)
            TextView mTextViewGearOut;
            @BindView(R.id.raw_asset_ll_event_data)
            LinearLayout mLinearLayoutEventData;
            @BindView(R.id.raw_asset_iv_status)
            ImageView mImageViewStatus;

            @BindView(R.id.raw_asset_ll_help)
            LinearLayout mLinearLayoutHelp;
            @BindView(R.id.raw_asset_ll_reload)
            LinearLayout mLinearLayoutReload;
            @BindView(R.id.raw_asset_ll_map)
            LinearLayout mLinearLayoutMap;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {
                    String charString = charSequence.toString();
                    if (charString.isEmpty() || charString.length() == 0) {
                        mVoVesselListTemp.clear();
                        mVoVesselListTemp.addAll(mVoVesselList);
                    } else {
                        ArrayList<VoVessel> filteredList = new ArrayList<>();
                        for (VoVessel mVoItem : mVoVesselList) {
                            if (mVoItem.getName().toLowerCase().contains(charString) || mVoItem.getName().toLowerCase().contains(charString)) {
                                filteredList.add(mVoItem);
                            }
                        }
                        mVoVesselListTemp = filteredList;
                    }

                    FilterResults filterResults = new FilterResults();
                    filterResults.values = mVoVesselListTemp;
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                    mVoVesselListTemp = (ArrayList<VoVessel>) filterResults.values;
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                notifyDataSetChanged();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            };
        }

    }

    int tempServiceType = 0;

    private void getEventServiceInformation(final int serviceType, final String mStringDeviceId, final int listPosition) {
        mActivity.mUtility.hideKeyboard(mActivity);
        String serviceCall = "";
        if (serviceType == 0) {
            serviceCall = "CHARGING";
        } else if (serviceType == 1) {
            serviceCall = "LOW_BATTERY";
        } else if (serviceType == 2) {
            serviceCall = "GIGO";
        }

        Call<String> mVoEventServiceCall = mActivity.mApiService.getEventServices(serviceCall, mStringDeviceId);
        System.out.println("URL-" + mVoEventServiceCall.request().url().toString());
        mVoEventServiceCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (isAdded()) {
                    tempServiceType = serviceType;
                    tempServiceType++;
                    if (serviceType == 2) {
                        mActivity.mUtility.HideProgress();
                    }
                    Gson gson = new Gson();
                    if (response.code() == 200 || response.isSuccessful()) {
                        System.out.println("response EventService-" + response.body().toString());
                        if (response.body() != null && !response.body().toString().equalsIgnoreCase("") && !response.body().equalsIgnoreCase("null")) {
                            VoEventService mVoEventService = gson.fromJson(response.body(), VoEventService.class);
//                            String json = gson.toJson(mVoLastInstallation);
//                            System.out.println("response mVoLastInstallationData---------" + json);
                            if (mVoEventService != null) {
                                if (mVoEventService.getGenerated() != null && !mVoEventService.getGenerated().equalsIgnoreCase("") && !mVoEventService.getGenerated().equalsIgnoreCase("null")) {
                                    try {
                                        Calendar cal = Calendar.getInstance(Locale.getDefault());
                                        cal.setTimeInMillis(Long.parseLong(mVoEventService.getGenerated()));
                                        if (serviceType == 0) {
                                            // Power Supply
                                            mVoVesselListTemp.get(listPosition).setPowerApply(mSimpleDateFormatDateDisplay.format(cal.getTime()));
                                            for (String entry : mVoEventService.getArgs().keySet()) {
                                                if (entry.equals("state")) {
                                                    mVoVesselListTemp.get(listPosition).setPowerSourceType(mVoEventService.getArgs().get(entry));
                                                }
                                            }
                                        } else if (serviceType == 1) {
                                            // Battery
                                            mVoVesselListTemp.get(listPosition).setBattery(mSimpleDateFormatDateDisplay.format(cal.getTime()));
                                            for (String entry : mVoEventService.getArgs().keySet()) {
                                                if (entry.equals("percentage")) {
                                                    mVoVesselListTemp.get(listPosition).setBatteryPercentage(mVoEventService.getArgs().get(entry));
                                                }
                                            }
                                        } else if (serviceType == 2) {
                                            // Gear In/Out
                                            mVoVesselListTemp.get(listPosition).setGearOut(mSimpleDateFormatDateDisplay.format(cal.getTime()));
                                            for (String entry : mVoEventService.getArgs().keySet()) {
                                                if (entry.equals("transitionType")) {
                                                    mVoVesselListTemp.get(listPosition).setGigoType(mVoEventService.getArgs().get(entry));
                                                }
                                            }
                                        } else if (serviceType == 3) {
                                            mVoVesselListTemp.get(listPosition).setRfid(mSimpleDateFormatDateDisplay.format(cal.getTime()));
                                        }
                                        if (mAssetListAdapter != null) {
                                            mActivity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        mAssetListAdapter.notifyDataSetChanged();
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
//                            try {
//                                Object mObject = new JSONTokener(response.body().toString()).nextValue();
//                                if (mObject instanceof JSONObject) {
//                                    System.out.println("JSON_OBJECT");
//                                    displayErrorWithGoBack();
//                                } else if (mObject instanceof JSONArray) {
//                                    System.out.println("JSONArray");
//                                    TypeToken<List<VoEventService>> token = new TypeToken<List<VoEventService>>() {
//                                    };
//                                    List<VoEventService> mResponse = gson.fromJson(response.body().toString(), token.getType());
//                                    if (mResponse != null) {
//                                        if (mResponse.size() > 0) {
//                                            Calendar cal = Calendar.getInstance(Locale.getDefault());
//                                            for (int i = 0; i < mResponse.size(); i++) {
//                                                if (mResponse.get(i).getGenerated() != null && !mResponse.get(i).getGenerated().equalsIgnoreCase("")) {
//                                                    try {
//                                                        cal.setTimeInMillis(Long.parseLong(mResponse.get(i).getGenerated()));
//                                                        if (mResponse.get(i).getType().equalsIgnoreCase("CHARGING")) {
//                                                            // Power Supply
//                                                            mVoVesselListTemp.get(listPosition).setPowerApply(mSimpleDateFormatDateDisplay.format(cal.getTime()));
//                                                            for (String entry : mResponse.get(i).getArgs().keySet()) {
//                                                                if (entry.equals("state")) {
//                                                                    mVoVesselListTemp.get(listPosition).setPowerSourceType(mResponse.get(i).getArgs().get(entry));
//                                                                }
//                                                            }
//                                                        } else if (mResponse.get(i).getType().equalsIgnoreCase("LOW_BATTERY")) {
//                                                            // Battery
//                                                            mVoVesselListTemp.get(listPosition).setBattery(mSimpleDateFormatDateDisplay.format(cal.getTime()));
//                                                            for (String entry : mResponse.get(i).getArgs().keySet()) {
//                                                                if (entry.equals("percentage")) {
//                                                                    mVoVesselListTemp.get(listPosition).setBatteryPercentage(mResponse.get(i).getArgs().get(entry));
//                                                                }
//                                                            }
//                                                        } else if (mResponse.get(i).getType().equalsIgnoreCase("GIGO")) {
//                                                            // Gear In/Out
//                                                            mVoVesselListTemp.get(listPosition).setGearOut(mSimpleDateFormatDateDisplay.format(cal.getTime()));
//                                                            for (String entry : mResponse.get(i).getArgs().keySet()) {
//                                                                if (entry.equals("transitionType")) {
//                                                                    mVoVesselListTemp.get(listPosition).setGigoType(mResponse.get(i).getArgs().get(entry));
//                                                                }
//                                                            }
//                                                        } else if (mResponse.get(i).getType().equalsIgnoreCase("RFID")) {
//                                                            // FRID
//                                                            mVoVesselListTemp.get(listPosition).setRfid(mSimpleDateFormatDateDisplay.format(cal.getTime()));
//                                                        }
//                                                    } catch (Exception e) {
//                                                        e.printStackTrace();
//                                                    }
//                                                }
//
//                                            }
//
////                                            for (Map.Entry<String, String> entry : jsonObject.entrySet()) {
////
////                                                String key = entry.getKey();
////                                                DataValues value = gson.fromJson(entry.getValue(), DataValues.class);
////                                                products.put(key, value);
////                                            }
//                                        }
//                                    }
//                                }
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
                        }

                    }
                    if (tempServiceType < 3) {
                        getEventServiceInformation(tempServiceType, mStringDeviceId, listPosition);
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                tempServiceType = serviceType;
                tempServiceType++;
                if (serviceType == 2) {
                    mActivity.mUtility.HideProgress();
                }
                if (tempServiceType < 3) {
                    getEventServiceInformation(tempServiceType, mStringDeviceId, listPosition);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mActivity.mImageViewHeaderLogo.setVisibility(View.GONE);
        unbinder.unbind();
    }
}
