package com.vithamastech.smartlight.fragments;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FragmentWebView extends Fragment {
    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;
    @BindView(R.id.fragment_webview)
    WebView mWebView;
    SwipeRefreshLayout mSwipeRefreshLayout;
    String mStringTitle = "";
    String mStringURL = "";
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        if (getArguments() != null) {
            mStringTitle = getArguments().getString("intent_title");
            mStringURL = getArguments().getString("intent_url");
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_webview, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);

        mActivity.mTextViewTitle.setText(mStringTitle);
        mActivity.mImageViewBack.setVisibility(View.GONE);
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mActivity.mToolbar.setBackgroundColor(getResources().getColor(R.color.colorBlack));
        mActivity.showBackButton(true);
        mSwipeRefreshLayout = (SwipeRefreshLayout) mViewRoot.findViewById(R.id.fragment_webview_swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.colorBlueText,
                R.color.colorBlack);
        if (mActivity.mUtility.haveInternet()) {
            mWebView.setWebViewClient(new MyBrowser());
            startWebView(mStringURL);
            mWebView.setWebChromeClient(new WebChromeClient() {
                public void onProgressChanged(WebView view, int progress) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    if (progress < 100) {
                        mActivity.mUtility.ShowProgress("Please wait..");
                    }

                    if (progress == 100) {
                        mActivity.mUtility.HideProgress();
                    }
                }
            });
        } else {
            mSwipeRefreshLayout.setRefreshing(false);
            mActivity.mUtility.errorDialog(getResources().getString(R.string.str_no_internet_connection), 3, true);
        }
        /*Refresh web page*/
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mActivity.mUtility.haveInternet()) {
                    mWebView.setWebViewClient(new MyBrowser());
                    startWebView(mStringURL);
                    mWebView.setWebChromeClient(new WebChromeClient() {
                        public void onProgressChanged(WebView view, int progress) {
                            mSwipeRefreshLayout.setRefreshing(false);
                            if (progress < 100) {
                                mActivity.mUtility.ShowProgress("Please wait..");
                            }

                            if (progress == 100) {
                                mActivity.mUtility.HideProgress();
                            }
                        }
                    });
                } else {
                    mSwipeRefreshLayout.setRefreshing(false);
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_no_internet_connection), 3, true);
                }
            }
        });
        /*Handle back press of web page*/
        mWebView.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK
                        && event.getAction() == MotionEvent.ACTION_UP
                        && mWebView.canGoBack()) {
                    mWebView.goBack();
                    return true;
                }
                return false;
            }
        });
        return mViewRoot;
    }

    private void startWebView(String mStringWebURL) {
        mWebView.getSettings().setLoadsImagesAutomatically(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
        mWebView.getSettings().getAllowContentAccess();
        mWebView.getSettings().setUseWideViewPort(false);
        mWebView.getSettings().setTextSize(WebSettings.TextSize.NORMAL);
        mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mWebView.loadUrl(mStringWebURL);

    }

    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        mActivity.mToolbar.setBackgroundColor(getResources().getColor(R.color.colorTransparent));
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        mActivity.mImageViewBack.setVisibility(View.GONE);
    }
}
