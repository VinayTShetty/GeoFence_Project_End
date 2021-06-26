package com.vithamastech.smartlight.fragments;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.drawerlayout.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Jaydeep on 23-12-2017.
 */

public class FragmentAboutUs extends Fragment {
    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;
    @BindView(R.id.frg_about_us_tv_app_version)
    TextView mTextViewAppVersion;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_about_us, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mActivity.mTextViewTitle.setText(R.string.frg_settings_about_us);
        mActivity.mImageViewBack.setVisibility(View.GONE);
//        mActivity.mImageViewConnectionStatus.setVisibility(View.VISIBLE);
        mActivity.showBackButton(false);
        try {
            PackageInfo pInfo = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0);
            mTextViewAppVersion.setText(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            mTextViewAppVersion.setText("1.0.1");
        }
        return mViewRoot;
    }


    @OnClick(R.id.frg_about_us_ll_about_us)
    public void onAboutUsClick(View mView) {
        if (isAdded()) {
            FragmentWebView mFragmentWebView = new FragmentWebView();
            Bundle mBundle = new Bundle();
            mBundle.putString("intent_title", getResources().getString(R.string.frg_settings_about_us));
            mBundle.putString("intent_url", getResources().getString(R.string.frg_contact_about_us));
            mActivity.replacesFragment(mFragmentWebView, true, mBundle, 0);
        }
    }

    @OnClick(R.id.frg_about_us_ll_app_privacy_policy)
    public void onPrivacyPolicyClick(View mView) {
        if (isAdded()) {
//            ReadFileContent bt = new ReadFileContent();
//            bt.execute("http://vithamastech.com/smartlight/JdUrl.txt");

            FragmentWebView mFragmentWebView = new FragmentWebView();
            Bundle mBundle = new Bundle();
            mBundle.putString("intent_title", getResources().getString(R.string.frg_settings_app_privacy_policy));
            mBundle.putString("intent_url", getResources().getString(R.string.frg_contact_us_app_privacy_policy));
            mActivity.replacesFragment(mFragmentWebView, true, mBundle, 0);
        }
    }

//    private class ReadFileContent extends AsyncTask<String, Integer, String> {
//
//        ProgressDialog pd;
//
//        protected void onPreExecute() {
//            super.onPreExecute();
//            pd = new ProgressDialog(mActivity);
//            pd.setTitle("Reading the text file");
//            pd.setMessage("Please wait.");
//            pd.setCancelable(true);
//            pd.setIndeterminate(false);
//            pd.show();
//        }
//
//        protected String doInBackground(String... params) {
//            String text = "";
//            URL url;
//            try {
//                url = new URL(params[0]);
//                HttpURLConnection con = (HttpURLConnection) url.openConnection();
//                InputStream is = con.getInputStream();
//                BufferedReader br = new BufferedReader(new InputStreamReader(is));
//                String line;
//                while ((line = br.readLine()) != null) {
//                    text += (line+"\n");
//                }
//                br.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//                if (pd != null) pd.dismiss();
//            }
//            return text;
//        }
//
//        protected void onPostExecute(String result) {
//            if (pd != null)
//                pd.dismiss();
//            System.out.println("Result=" + result);
//        }
//    }

    @Override
    public void onResume() {
        mActivity.showBackButton(false);
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
//        mActivity.mImageViewConnectionStatus.setVisibility(View.GONE);
    }
}
