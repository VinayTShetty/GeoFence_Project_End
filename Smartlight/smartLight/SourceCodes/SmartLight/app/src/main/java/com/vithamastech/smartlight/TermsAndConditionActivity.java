package com.vithamastech.smartlight;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.vithamastech.smartlight.helper.Utility;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TermsAndConditionActivity extends AppCompatActivity {
    @BindView(R.id.activity_tnc_webview)
    WebView mWebView;
    @BindView(R.id.activity_tnc_img_back)
    ImageView mImageViewBack;
    SwipeRefreshLayout mSwipeRefreshLayout;
    Utility mUtility;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        setContentView(R.layout.activity_terms_n_condition);
        ButterKnife.bind(TermsAndConditionActivity.this);
        mUtility = new Utility(TermsAndConditionActivity.this);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_tnc_swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.colorBlueText,
                R.color.colorBlack);
        if (mUtility.haveInternet()) {
            mWebView.setWebViewClient(new MyBrowser());
            startWebView(getResources().getString(R.string.frg_contact_terms_n_condition_link));
            mWebView.setWebChromeClient(new WebChromeClient() {
                public void onProgressChanged(WebView view, int progress) {
                    if (progress < 100) {
                        mUtility.ShowProgress("Please wait..");
                    }

                    if (progress == 100) {
                        mUtility.HideProgress();
                    }
                }
            });
        } else {
            mUtility.errorDialog(getResources().getString(R.string.str_no_internet_connection), 3,true);
        }
        // Refresh Web page
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mUtility.haveInternet()) {
                    mWebView.setWebViewClient(new MyBrowser());
                    startWebView(getResources().getString(R.string.frg_contact_terms_n_condition_link));
                    mWebView.setWebChromeClient(new WebChromeClient() {
                        public void onProgressChanged(WebView view, int progress) {
                            mSwipeRefreshLayout.setRefreshing(false);
                            if (progress < 100) {
                                mUtility.ShowProgress("Please wait..");
                            }

                            if (progress == 100) {
                                mUtility.HideProgress();
                            }
                        }
                    });
                } else {
                    mSwipeRefreshLayout.setRefreshing(false);
                    mUtility.errorDialog(getResources().getString(R.string.str_no_internet_connection), 3,true);
                }
            }
        });

        mImageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (mWebView.canGoBack()) {
                        mWebView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
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
    protected void onResume() {
        super.onResume();
        MyApplication.activityResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyApplication.activityPaused();
    }

}
