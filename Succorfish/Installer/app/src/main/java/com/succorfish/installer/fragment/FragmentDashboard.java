package com.succorfish.installer.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.libRG.CustomTextView;
import com.succorfish.installer.MainActivity;
import com.succorfish.installer.MyApplication;
import com.succorfish.installer.R;
import com.succorfish.installer.Vo.VoQuestion;
import com.succorfish.installer.Vo.VoQuestionAns;
import com.succorfish.installer.Vo.VoVessel;
import com.succorfish.installer.helper.PreferenceHelper;
import com.succorfish.installer.helper.Utility;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Jaydeep on 16-02-2018.
 */

public class FragmentDashboard extends Fragment {
    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;
    @BindView(R.id.fragment_dashboard_relativlayout_main)
    RelativeLayout mRelativeLayoutMain;
    @BindView(R.id.fragment_dashboard_textview_welcome)
    TextView mTextViewWelcome;
    @BindView(R.id.fragment_dashboard_textview_total_installation)
    TextView mTextViewInstallationCount;
    @BindView(R.id.fragment_dashboard_textview_unsync_count)
    CustomTextView mCustomTextViewUnSyncCount;
    @BindView(R.id.fragment_dashboard_imageview_company_logo)
    ImageView mImageViewHeaderCompanyLogo;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_dashboard, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mActivity.mToolbar.setVisibility(View.GONE);
        mActivity.mTextViewTitle.setText(getResources().getString(R.string.str_dashboard_title));
        mActivity.mImageViewBack.setVisibility(View.GONE);
        mActivity.mImageViewAdd.setVisibility(View.GONE);
        mActivity.mTextViewTitle.setVisibility(View.GONE);
        mActivity.mRelativeLayoutBottomMenu.setVisibility(View.VISIBLE);
        mTextViewWelcome.setText(String.format(getResources().getString(R.string.str_dashboard_welcome), PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserName() + ""));

        getInstallationCount();
        String unSyncCount = mActivity.getUnSyncedCount();
        if (unSyncCount != null && !unSyncCount.equalsIgnoreCase("")) {
            if (Integer.parseInt(unSyncCount) > 0) {
                mCustomTextViewUnSyncCount.setVisibility(View.VISIBLE);
                mCustomTextViewUnSyncCount.setText(unSyncCount);
            } else {
                mCustomTextViewUnSyncCount.setVisibility(View.GONE);
            }
        } else {
            mCustomTextViewUnSyncCount.setVisibility(View.GONE);
        }
        return mViewRoot;
    }

    /*Get Install Record Count*/
    public void getInstallationCount() {
        String result = "0";
        String urlString = "SELECT count(*) from " + mActivity.mDbHelper.mTableInstall + " where " + mActivity.mDbHelper.mFieldInstallIsSync + "= '1'" + " AND " + mActivity.mDbHelper.mFieldInstallUserId + "= '" + PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserId() + "'";
        result = mActivity.mDbHelper.getCountRecordByQuery(urlString) + "";
        System.out.println("result-" + result);
        if (Integer.parseInt(result) > 1) {
            mTextViewInstallationCount.setText(String.format(getResources().getString(R.string.str_dashboard_total_installations), result + ""));
        } else {
            mTextViewInstallationCount.setText(String.format(getResources().getString(R.string.str_dashboard_total_installation), result + ""));
        }
    }
//    public void getInstallationCount() {
//        mActivity.mUtility.hideKeyboard(mActivity);
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("user_id", mActivity.mPreferenceHelper.getUserId());
//        System.out.println("params-" + params.toString());
//        System.out.println("paramsHeader-" + mActivity.mPreferenceHelper.getAccessToken());
//        Call<VoChangePassword> mLogin = mActivity.mApiService.getInstallationCount(params, mActivity.mPreferenceHelper.getAccessToken());
//        System.out.println("URL-" + mLogin.request().url().toString());
//        mLogin.enqueue(new Callback<VoChangePassword>() {
//            @Override
//            public void onResponse(Call<VoChangePassword> call, Response<VoChangePassword> response) {
//                VoChangePassword mScanDeviceData = response.body();
//                Gson gson = new Gson();
//                String json = gson.toJson(mScanDeviceData);
//                System.out.println("response mScanDeviceData---------" + json);
//                if (mScanDeviceData != null && mScanDeviceData.getResponse().equalsIgnoreCase("true")) {
//                    if (mScanDeviceData.getData() != null && !mScanDeviceData.getData().equalsIgnoreCase("") && !mScanDeviceData.getData().equalsIgnoreCase("null")) {
//                        mActivity.mPreferenceHelper.setInstallationCount(mScanDeviceData.getData());
//                        if (isAdded()) {
//                            if (Integer.parseInt(mActivity.mPreferenceHelper.getInstallationCount()) > 1) {
//                                mTextViewInstallationCount.setText(String.format(getResources().getString(R.string.str_dashboard_total_installations), mActivity.mPreferenceHelper.getInstallationCount() + ""));
//                            } else {
//                                mTextViewInstallationCount.setText(String.format(getResources().getString(R.string.str_dashboard_total_installation), mActivity.mPreferenceHelper.getInstallationCount() + ""));
//                            }
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<VoChangePassword> call, Throwable t) {
//
//            }
//        });
//    }

    @OnClick(R.id.fragment_dashboard_cardview_new_installation)
    public void onNewInstallationClick(View mView) {
        if (isAdded()) {
            Gson gson = new Gson();
            VoQuestion mVoQuestion = gson.fromJson(Utility.fetchQuestionnaireJson(mActivity, "questions_list.json"), VoQuestion.class);
            mActivity.mListQuestion = mVoQuestion.getQuestions();
            mActivity.mIntInstallationId = 0;
            mActivity.mInstallUnInstallInspectStatus = 0;

            Bundle mBundle = new Bundle();
            mBundle.putBoolean("mIntent_is_from_un_sync", false);
            mActivity.replacesFragment(new FragmentHealthNsafety(), true, "Finish_Back", mBundle, 1);
//            mActivity.mIntInstallationId = 0;
//            FragmentNewInstallation mFragmentNewInstallation = new FragmentNewInstallation();
//            mActivity.replacesFragment(mFragmentNewInstallation, true, null, 1);
        }
    }

    @OnClick(R.id.fragment_dashboard_cardview_uninstall)
    public void onUninstallClick(View mView) {
        if (isAdded()) {
            Gson gson = new Gson();
            VoQuestion mVoQuestion = gson.fromJson(Utility.fetchQuestionnaireJson(mActivity, "questions_list.json"), VoQuestion.class);
            mActivity.mListQuestion = mVoQuestion.getQuestions();
            mActivity.mInstallUnInstallInspectStatus = 1;
            mActivity.mIntUnInstallationId = 0;
            Bundle mBundle = new Bundle();
            mBundle.putBoolean("mIntent_is_from_un_sync", false);
            mActivity.replacesFragment(new FragmentHealthNsafety(), true, "Finish_Back", mBundle, 1);
//            mActivity.mIntUnInstallationId = 0;
//            FragmentUninstall mFragmentUninstall = new FragmentUninstall();
//            mActivity.replacesFragment(mFragmentUninstall, true, null, 1);
        }
    }

    @OnClick(R.id.fragment_dashboard_cardview_unsynced)
    public void onUnSyncedClick(View mView) {
        if (isAdded()) {
            mActivity.replacesFragment(new FragmentUnsynced(), true, null, 1);
        }
    }

    @OnClick(R.id.fragment_dashboard_cardview_install_guide)
    public void onInstallGuideClick(View mView) {
        if (isAdded()) {
            FragmentInstallGuideList mFragmentInstallGuideList = new FragmentInstallGuideList();
            mActivity.replacesFragment(mFragmentInstallGuideList, true, null, 1);
        }
    }

    @OnClick(R.id.fragment_dashboard_cardview_inspection)
    public void onInspectionClick(View mView) {
        if (isAdded()) {
            Gson gson = new Gson();
            VoQuestion mVoQuestion = gson.fromJson(Utility.fetchQuestionnaireJson(mActivity, "questions_list.json"), VoQuestion.class);
            mActivity.mListQuestion = mVoQuestion.getQuestions();
            mActivity.mInstallUnInstallInspectStatus = 2;
            mActivity.mIntInspectionId = 0;
            Bundle mBundle = new Bundle();
            mBundle.putBoolean("mIntent_is_from_un_sync", false);
            mActivity.replacesFragment(new FragmentHealthNsafety(), true, "Finish_Back", mBundle, 1);
//            mActivity.mIntInspectionId = 0;
//            FragmentInspections mFragmentInspection = new FragmentInspections();
//            mActivity.replacesFragment(mFragmentInspection, true, null, 1);
        }
    }

    @OnClick(R.id.fragment_dashboard_cardview_legal_docs)
    public void onLegalDocsClick(View mView) {
        if (isAdded()) {
            FragmentLegalDocList mFragmentLegalDocList = new FragmentLegalDocList();
            mActivity.replacesFragment(mFragmentLegalDocList, true, null, 1);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        mActivity.mToolbar.setVisibility(View.VISIBLE);
        mActivity.mTextViewTitle.setVisibility(View.VISIBLE);
    }
}
