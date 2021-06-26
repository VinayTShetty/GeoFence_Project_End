package com.vithamastech.smartlight.fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.drawerlayout.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.R;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class FragmentContactUs extends Fragment {
    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;

    private static final int REQUEST_CALL = 1;
    Intent callIntent;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_contact_us, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mActivity.mTextViewTitle.setText(R.string.activity_main_menu_contact_us);
        mActivity.mImageViewBack.setVisibility(View.GONE);
//        mActivity.mImageViewConnectionStatus.setVisibility(View.VISIBLE);
        mActivity.showBackButton(false);

        return mViewRoot;
    }

    @OnClick(R.id.frg_contact_us_rl_address)
    public void onAddressClick(View mView) {
        if (isAdded()) {
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                    Uri.parse("http://maps.google.com/maps?q=" + "Vithamas Technologies," + "136/D," + "Chandana Chetan Complex," + "Abhishek Road," + " Vijayanagar 2nd Stage ," + " Mysuru-570016," + " Karnataka, " + "India"));
            startActivity(intent);
        }
    }

//    @OnClick(R.id.frg_contact_us_rl_call)
//    public void onCallClick(View mView) {
//        if (isAdded()) {
//            String mStringUserPhone = getResources().getString(R.string.frg_contact_us_mobile);
//            if (mStringUserPhone != null && !mStringUserPhone.equals("")) {
//                mStringUserPhone = mStringUserPhone.replace("-", "");
//                mStringUserPhone = mStringUserPhone.replace(" ", "");
//                callIntent = new Intent(Intent.ACTION_DIAL);
//                callIntent.setData(Uri.parse("tel:" + mStringUserPhone));
//                if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//                    ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
//                } else {
//                    startActivity(callIntent);
//                }
//            }
//        }
//    }

    @OnClick(R.id.frg_contact_us_rl_email)
    public void onEmailClick(View mView) {
        if (isAdded()) {
            try {
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setData(Uri.parse("mailto:"));
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{getResources().getString(R.string.frg_contact_us_email)});
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                sendIntent.putExtra(Intent.EXTRA_TEXT, "");
                if (sendIntent.resolveActivity(mActivity.getPackageManager()) != null) {
                    startActivity(sendIntent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @OnClick(R.id.frg_contact_us_rl_website)
    public void onWebsiteClick(View mView) {
        if (isAdded()) {
            openUrl(getResources().getString(R.string.app_name), getResources().getString(R.string.frg_contact_us_website));
        }
    }

    @OnClick(R.id.frg_contact_us_fb)
    public void onFbClick(View mView) {
        if (isAdded()) {
            openUrl(getResources().getString(R.string.app_name), getResources().getString(R.string.frg_contact_us_facebook));
        }
    }

//    @OnClick(R.id.frg_contact_us_twitter)
//    public void onTwitterClick(View mView) {
//        if (isAdded()) {
//            openUrl(getResources().getString(R.string.app_name),getResources().getString(R.string.frg_contact_us_twitter));
//        }
//    }

//    @OnClick(R.id.frg_contact_us_google)
//    public void onGoogleClick(View mView) {
//        if (isAdded()) {
//            openUrl(getResources().getString(R.string.app_name),getResources().getString(R.string.frg_contact_us_google));
//        }
//    }

    @OnClick(R.id.frg_contact_us_linked_in)
    public void onLinkedInClick(View mView) {
        if (isAdded()) {
            openUrl(getResources().getString(R.string.app_name), getResources().getString(R.string.frg_contact_us_linkedIn));
        }
    }

    @OnClick(R.id.frg_contact_us_youtube)
    public void onYouTubeClick(View mView) {
        if (isAdded()) {
            openUrl(getResources().getString(R.string.app_name), getResources().getString(R.string.frg_contact_us_youtube));
        }
    }

    @OnClick(R.id.frg_contact_us_instagram)
    public void onInstagramClick(View mView) {
        if (isAdded()) {
            openUrl(getResources().getString(R.string.app_name), getResources().getString(R.string.frg_contact_us_instagram));
        }
    }

//    @OnClick(R.id.frg_contact_us_whats_app)
//    public void onWhatsAppClick(View mView) {
//        if (isAdded()) {
//            try {
//                openWhatsApp(mView);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }


    private void openUrl(String title, String url) {
        FragmentWebView mFragmentWebview = new FragmentWebView();
        Bundle mBundle = new Bundle();
        mBundle.putString("intent_title", title);
        mBundle.putString("intent_url", url);
        mActivity.replacesFragment(mFragmentWebview, true, mBundle, 0);
//        try {
//            String url = getResources().getString(R.string.frg_contact_us_youtube);
//            Intent i = new Intent(Intent.ACTION_VIEW);
//            i.setData(Uri.parse(url));
//            startActivity(i);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

//    public void openWhatsApp(View view) {
//        PackageManager pm = mActivity.getPackageManager();
//        try {
//            String toNumber = getResources().getString(R.string.frg_contact_us_whatsApp); // Replace with mobile phone number without +Sign or leading zeros.
//            Intent sendIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + "" + toNumber + "?body=" + ""));
//            sendIntent.setPackage("com.whatsapp");
//            startActivity(sendIntent);
//        } catch (Exception e) {
//            e.printStackTrace();
//            Toast.makeText(mActivity, getResources().getString(R.string.frg_contact_us_whatsApp_not_supported), Toast.LENGTH_LONG).show();
//
//        }
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CALL: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivity(callIntent);
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        mActivity.showBackButton(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        mActivity.mImageViewConnectionStatus.setVisibility(View.GONE);
        unbinder.unbind();
    }
}