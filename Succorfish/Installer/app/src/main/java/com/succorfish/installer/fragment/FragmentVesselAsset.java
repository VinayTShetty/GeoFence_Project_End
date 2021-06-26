package com.succorfish.installer.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.succorfish.installer.LoginActivity;
import com.succorfish.installer.MainActivity;
import com.succorfish.installer.R;
import com.succorfish.installer.Vo.VoVessel;
import com.succorfish.installer.Vo.VoVesselAsset;
import com.succorfish.installer.db.DataHolder;
import com.succorfish.installer.interfaces.onAlertDialogCallBack;
import com.succorfish.installer.interfaces.onBackPressWithAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Jaydeep on 26-02-2018.
 */

public class FragmentVesselAsset extends Fragment {
    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;

    @BindView(R.id.fragment_vessel_asset_relativelayout_main)
    RelativeLayout mRelativeLayoutMain;
    @BindView(R.id.fragment_vessel_asset_autocompletetextview)
    AppCompatAutoCompleteTextView mAutoCompleteTextViewSearch;
    @BindView(R.id.fragment_vessel_asset_editext_regno)
    EditText mEditTextRegiNo;
    onBackPressWithAction mOnBackPressWithAction;
    ArrayList<VoVesselAsset> mArrayListVesselList = new ArrayList<>();
    String mStringVesselId = "";
    String mStringAccountId = "";
    boolean mBooleanIsFromInstall = false;
    VesselAssetAdapter mVesselAssetAdapter;
    //    ItemAutoTextAdapter mItemAutoTextAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        if (getArguments() != null) {
            mStringAccountId = getArguments().getString("mIntent_accountId");
            mBooleanIsFromInstall = getArguments().getBoolean("mIntent_isFromInstall");
        }
        System.out.println("mStringAccountId-" + mStringAccountId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_vessel_asset, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);

        mActivity.mTextViewTitle.setText(getResources().getString(R.string.str_frg_two_vessel_asset_title));
        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
        mActivity.mImageViewAdd.setVisibility(View.GONE);
        mActivity.mTextViewTitle.setVisibility(View.VISIBLE);
        mActivity.mRelativeLayoutBottomMenu.setVisibility(View.GONE);
        mActivity.mTextViewDone.setVisibility(View.VISIBLE);
        mActivity.mTextViewDone.setText(getResources().getString(R.string.str_save));
        mAutoCompleteTextViewSearch.setThreshold(2);
        mArrayListVesselList = new ArrayList<>();
//        for (int i = 1; i < COUNTRIES.length; i++) {
//            VoVesselAsset mVoVesselAsset = new VoVesselAsset();
//            mVoVesselAsset.setVessel_name(COUNTRIES[i]);
//            mVoVesselAsset.setVessel_reg_no(i + "");
//            mArrayListVesselList.add(mVoVesselAsset);
//        }
//        mVesselAssetAdapter = new VesselAssetAdapter(mActivity, R.layout.activity_main, R.id.raw_spinner_item_textview_item, mArrayListVesselList);
//        mAutoCompleteTextViewSearch.setAdapter(mVesselAssetAdapter);

//        mItemAutoTextAdapter = this.new ItemAutoTextAdapter();
//        mAutoCompleteTextViewSearch.setAdapter(mItemAutoTextAdapter);
//        mAutoCompleteTextViewSearch.setOnItemClickListener(mItemAutoTextAdapter);

        if (mActivity.mUtility.haveInternet()) {
            if (mStringAccountId != null && !mStringAccountId.equalsIgnoreCase("") && !mStringAccountId.equalsIgnoreCase("null")) {
                /*get all vessel list from server*/
                getVesselAssetList();
            } else {
                /*get all vessel list from local db*/
                getDBVesselAssetList();
            }
        } else {
            getDBVesselAssetList();
        }

        mAutoCompleteTextViewSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String text = mAutoCompleteTextViewSearch.getText().toString().trim();
                if (mVesselAssetAdapter != null) {
                    if (!text.equalsIgnoreCase("")) {
                        mVesselAssetAdapter.getFilter().filter(text);
                        mVesselAssetAdapter.notifyDataSetChanged();
//                        ArrayAdapter<VoVesselAsset> myAdapter = (ArrayAdapter<VoVesselAsset>) mAutoCompleteTextViewSearch.getAdapter();
//                        myAdapter.getFilter().filter(charSequence.toString());
//                        if (myAdapter != null) {
//                            myAdapter.notifyDataSetChanged();
//                        }
                    } else {
                        mEditTextRegiNo.setText("");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mAutoCompleteTextViewSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mActivity.mUtility.hideKeyboard(mActivity);
                System.out.println("Name-" + mArrayListVesselList.get(position).getVessel_name());
                System.out.println("RegiId-" + mArrayListVesselList.get(position).getVessel_port_no());
                mEditTextRegiNo.setText(mArrayListVesselList.get(position).getVessel_port_no());
                mStringVesselId = mArrayListVesselList.get(position).getVessel_succorfish_id();
            }
        });
        mActivity.mTextViewDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.mUtility.hideKeyboard(mActivity);
                String mStringVeselName = mAutoCompleteTextViewSearch.getText().toString().trim();
                String mStringVeselRegNo = mEditTextRegiNo.getText().toString().trim();
                if (mStringVeselName.equalsIgnoreCase("")) {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_two_vessel_asset_select_name));
                    return;
                }
                if (mStringVeselRegNo.equalsIgnoreCase("")) {
                    mStringVeselRegNo = "NA";
//                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_two_vessel_asset_enter_vessel_reg_no));
//                    return;
                }
                if (mStringVesselId.equalsIgnoreCase("")) {
                    mActivity.mUtility.errorDialog("Please select vessel/asset from suggested list. You can't enter manually");
                    return;
                }
                if (mOnBackPressWithAction != null) {
                    mOnBackPressWithAction.onBackWithAction(mStringVesselId, mStringVeselName, mStringVeselRegNo);
                }
                mActivity.onBackPressed();
            }
        });
        return mViewRoot;
    }

    /*get All vessel list data from asset*/
    private void getVesselAssetList() {
        System.out.println("latest");
        mActivity.mUtility.ShowProgress();
        mActivity.mUtility.hideKeyboard(mActivity);
        Call<String> mLogin = null;
        if (mBooleanIsFromInstall) {
            mLogin = mActivity.mApiService.getAllAssetList(mStringAccountId, mStringAccountId);
        } else {
            mLogin = mActivity.mApiService.getAllAssetListWithAccount(mStringAccountId, mStringAccountId);
        }
        System.out.println("URL-" + mLogin.request().url().toString());
        mLogin.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (isAdded()) {
                    Gson gson = new Gson();
                    if (response.code() == 200 || response.isSuccessful()) {
                        System.out.println("response mVesselData---------" + response.body());
                        TypeToken<List<VoVessel>> token = new TypeToken<List<VoVessel>>() {
                        };
                        final List<VoVessel> mVoVesselList = gson.fromJson(response.body(), token.getType());
                        String json = gson.toJson(mVoVesselList);
                        if (mVoVesselList != null) {
                            if (mVoVesselList.size() > 0) {
                                VoVesselAsset mVoVesselAsset;
                                for (int i = 0; i < mVoVesselList.size(); i++) {
                                    mVoVesselAsset = new VoVesselAsset();
                                    mVoVesselAsset.setVessel_server_id("");
                                    mVoVesselAsset.setVessel_succorfish_id(mVoVesselList.get(i).getId());
                                    mVoVesselAsset.setVessel_account_id(mVoVesselList.get(i).getAccountId());
                                    mVoVesselAsset.setVessel_name(mVoVesselList.get(i).getName());
                                    mVoVesselAsset.setVessel_reg_no(mVoVesselList.get(i).getRegNo());
                                    mVoVesselAsset.setVessel_port_no(mVoVesselList.get(i).getRegNo());
                                    mVoVesselAsset.setIsSync("1");
                                    mArrayListVesselList.add(mVoVesselAsset);
                                }
                                System.out.println("SIZE-" + mArrayListVesselList.size());
                                if (mVesselAssetAdapter == null) {
                                    System.out.println("SIZE-NULL-" + mArrayListVesselList.size());
                                    mVesselAssetAdapter = new VesselAssetAdapter(mActivity, R.layout.activity_main, R.id.raw_spinner_item_textview_item, mArrayListVesselList);
                                    mAutoCompleteTextViewSearch.setAdapter(mVesselAssetAdapter);
                                } else {
                                    System.out.println("SIZE-NOT NULL " + mArrayListVesselList.size());
                                    mVesselAssetAdapter.notifyDataSetChanged();
                                }
                            } else {
                                getDBVesselAssetList();
                            }
                        } else {
                            getDBVesselAssetList();
                        }
                    } else {
                        getDBVesselAssetList();
                    }
                }
                mActivity.mUtility.HideProgress();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                mActivity.mUtility.HideProgress();
                getDBVesselAssetList();
//                showMessageRedAlert(getCurrentFocus(), getResources().getString(R.string.str_server_error_try_again), getResources().getString(R.string.str_ok));
            }
        });
    }

    /*get vessel asset list from local db*/
    private void getDBVesselAssetList() {
        DataHolder mDataHolder;
        mArrayListVesselList = new ArrayList<>();
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableVesselAsset;
            System.out.println("Local url " + url);
            mDataHolder = mActivity.mDbHelper.read(url);
            if (mDataHolder != null) {
                System.out.println("Local List " + url + " : " + mDataHolder.get_Listholder().size());
                VoVesselAsset mVoVesselAsset;
                for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
                    mVoVesselAsset = new VoVesselAsset();
                    mVoVesselAsset.setVessel_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldVesselServerId));
                    mVoVesselAsset.setVessel_succorfish_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldVesselSuccorfishId));
                    mVoVesselAsset.setVessel_account_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldVesselAccountId));
                    mVoVesselAsset.setVessel_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldVesselName));
                    mVoVesselAsset.setVessel_reg_no(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldVesselRegNo));
                    mVoVesselAsset.setVessel_port_no(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldVesselPortNo));
                    mVoVesselAsset.setIsSync(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldVesselIsSync));
                    mArrayListVesselList.add(mVoVesselAsset);
                }
//                Collections.sort(mArrayListTreatmentLists, new Comparator<TreatmentList>() {
//                    @Override
//                    public int compare(TreatmentList s1, TreatmentList s2) {
//                        return s1.getTreatment_title().compareToIgnoreCase(s1.getTreatment_title());
//                    }
//                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mVesselAssetAdapter == null) {
            mVesselAssetAdapter = new VesselAssetAdapter(mActivity, R.layout.activity_main, R.id.raw_spinner_item_textview_item, mArrayListVesselList);
            mAutoCompleteTextViewSearch.setAdapter(mVesselAssetAdapter);
        } else {
            mVesselAssetAdapter.notifyDataSetChanged();
        }
    }

    public void setOnScanResultSet(onBackPressWithAction mScanResultSet) {
        mOnBackPressWithAction = mScanResultSet;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        mActivity.mUtility.hideKeyboard(mActivity);
        mActivity.mTextViewDone.setVisibility(View.GONE);
    }

    /*vessel list adapter*/
    public class VesselAssetAdapter extends ArrayAdapter<VoVesselAsset> {

        Context context;
        List<VoVesselAsset> items, tempItems, suggestions;

        public VesselAssetAdapter(Context context, int resource, int textViewResourceId, List<VoVesselAsset> items) {
            super(context, resource, textViewResourceId, items);
            this.context = context;
            this.items = items;
            tempItems = new ArrayList<VoVesselAsset>(items); // this makes the difference.
            suggestions = new ArrayList<VoVesselAsset>();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.raw_spinner_item, parent, false);
            }
            VoVesselAsset patient = items.get(position);
            if (patient != null) {
                TextView lblName = (TextView) view.findViewById(R.id.raw_spinner_item_textview_item);
                if (lblName != null)
                    lblName.setText(patient.getVessel_name());
            }
            return view;
        }


        @Override
        public Filter getFilter() {
            return nameFilter;
        }

        /**
         * Custom Filter implementation for custom suggestions we provide.
         */
        Filter nameFilter = new Filter() {

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                String str = ((VoVesselAsset) resultValue).getVessel_name();
                return str;
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                if (constraint != null) {
                    suggestions.clear();
                    for (VoVesselAsset appointment : tempItems) {
                        if (appointment.getVessel_name().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            suggestions.add(appointment);
                        }
                    }
                    FilterResults filterResults = new FilterResults();
                    filterResults.values = suggestions;
                    filterResults.count = suggestions.size();
                    return filterResults;
                } else {
                    return new FilterResults();
                }
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                try {
                    List<VoVesselAsset> filterList = (ArrayList<VoVesselAsset>) results.values;
                    if (results != null && results.count > 0) {
                        clear();
                        for (VoVesselAsset appointment : filterList) {
                            add(appointment);
                        }
                        notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
