package com.succorfish.installer.fragment;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.succorfish.installer.MainActivity;
import com.succorfish.installer.R;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Jaydeep on 27-02-2018.
 */

public class FragmentViewInstallGuide extends Fragment implements OnPageChangeListener, OnLoadCompleteListener {
    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;
    PDFView pdfView;
    int pageNumber = 0;
    public String mStringFilePath = "Succorfish_Land.pdf";
    String mStringTitle = "";
    boolean allowLandscapeMode = false;

    // https://stackoverflow.com/questions/2784847/how-do-i-determine-if-android-can-handle-pdf
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        if (getArguments() != null) {
            mStringFilePath = getArguments().getString("intent_file_url");
            mStringTitle = getArguments().getString("intent_title");
            allowLandscapeMode = getArguments().getBoolean("intent_is_landscape");
        }
        if (allowLandscapeMode) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_install_guide, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mActivity.mTextViewTitle.setText(mStringTitle);
        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
        mActivity.mImageViewAdd.setVisibility(View.GONE);
        mActivity.mRelativeLayoutBottomMenu.setVisibility(View.GONE);
        pdfView = (PDFView) mViewRoot.findViewById(R.id.pdfView);
        displayFromAsset();
        return mViewRoot;
    }

    /*Display pdf from asset*/
    private void displayFromAsset() {
        pdfView.fromAsset(mStringFilePath)
                .defaultPage(pageNumber)
                .enableSwipe(false)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .onPageChange(this)
                .enableAnnotationRendering(false)
                .password(null)
                .onLoad(this)
                .scrollHandle(null)
                .load();


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mActivity.mImageViewBack.setVisibility(View.GONE);
    }

    @Override
    public void loadComplete(int nbPages) {

    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
    }

}
