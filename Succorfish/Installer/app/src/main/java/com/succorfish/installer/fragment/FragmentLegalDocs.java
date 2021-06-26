package com.succorfish.installer.fragment;

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

public class FragmentLegalDocs extends Fragment implements OnPageChangeListener, OnLoadCompleteListener {
    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;
    PDFView pdfView;
    Integer pageNumber = 0;
    String pdfFileName;
    public static final String SAMPLE_FILE = "Succorfish_Land.pdf";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_install_guide, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mActivity.mTextViewTitle.setText(getResources().getString(R.string.str_dashboard_menu_legal_docs));
        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
        mActivity.mImageViewAdd.setVisibility(View.GONE);
        mActivity.mRelativeLayoutBottomMenu.setVisibility(View.GONE);
        pdfView = (PDFView) mViewRoot.findViewById(R.id.pdfView);
        displayFromAsset(SAMPLE_FILE);
        return mViewRoot;
    }

    /*Display Image From Asset */
    private void displayFromAsset(String assetFileName) {
        pdfFileName = assetFileName;

        pdfView.fromAsset(SAMPLE_FILE)
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
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
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
