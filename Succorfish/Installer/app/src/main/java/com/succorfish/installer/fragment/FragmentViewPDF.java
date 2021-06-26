package com.succorfish.installer.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.succorfish.installer.MainActivity;
import com.succorfish.installer.MyApplication;
import com.succorfish.installer.R;
import com.succorfish.installer.helper.PreferenceHelper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Jaydeep on 17-03-2018.
 */

public class FragmentViewPDF extends Fragment implements OnPageChangeListener, OnLoadCompleteListener {
    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;
    PDFView pdfView;
    int pageNumber = 0;

    String mStringPdfURL = "";
    String mStringLocalPdfURL = "";
    String mStringImeiNO = "";
    String mStringVesselName = "";
    String mStringDate = "";
    //    @BindView(R.id.fragment_view_pdf_webview)
//    WebView mWebView;
    int mIntHistoryType = 0;
    Calendar mCalendar;
    private SimpleDateFormat mSimpleDateFormatDateDisplay;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        if (getArguments() != null) {
            mStringLocalPdfURL = getArguments().getString("intent_local_pdf_url");
            mStringPdfURL = getArguments().getString("intent_pdf_url");
            mStringImeiNO = getArguments().getString("intent_imei_no");
            mStringVesselName = getArguments().getString("intent_vessel_name");
            mStringDate = getArguments().getString("intent_date");
            mIntHistoryType = getArguments().getInt("intent_pdf_type");
        }
        mCalendar = Calendar.getInstance();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_view_pdf, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        pdfView = (PDFView) mViewRoot.findViewById(R.id.fragment_view_pdf_pdfView);
        mSimpleDateFormatDateDisplay = new SimpleDateFormat(PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedDateFormat() + " HH:mm", Locale.getDefault());

        if (isAdded()) {
            mActivity.mTextViewTitle.setText(getResources().getString(R.string.str_view_pdf_view_report));
            mActivity.mImageViewBack.setVisibility(View.VISIBLE);
            mActivity.mImageViewAdd.setVisibility(View.VISIBLE);
            mActivity.mRelativeLayoutBottomMenu.setVisibility(View.GONE);
            mActivity.mImageViewAdd.setImageResource(R.drawable.ic_email);
        }
        if (mActivity.mUtility.haveInternet()) {
            displayPdf();
//            mWebView.setWebViewClient(new MyBrowser());
//            System.out.println("mStringPdfURL-" + mStringPdfURL);
//            if (mStringPdfURL != null && !mStringPdfURL.equalsIgnoreCase("") && !mStringPdfURL.equalsIgnoreCase("null")) {
//                startWebView(mStringPdfURL);
//            }
//            mWebView.setWebChromeClient(new WebChromeClient() {
//                public void onProgressChanged(WebView view, int progress) {
//                    if (progress < 100) {
//                        mActivity.mUtility.ShowProgress();
//                    }
//
//                    if (progress == 100) {
//                        mActivity.mUtility.HideProgress();
//                    }
//                }
//            });
        } else {
            mActivity.mUtility.errorDialog(getResources().getString(R.string.str_no_internet_connection));
        }

        mActivity.mImageViewAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAdded()) {
                    if (mStringLocalPdfURL != null && !mStringLocalPdfURL.equalsIgnoreCase("") && !mStringLocalPdfURL.equalsIgnoreCase("null")) {
                        try {
                            String mStringTitle;
                            if (mIntHistoryType == 0) {
                                mStringTitle = "Installation Report";
                            } else if (mIntHistoryType == 1) {
                                mStringTitle = "Uninstallation Report";
                            } else if (mIntHistoryType == 2) {
                                mStringTitle = "Inspection Report";
                            } else {
                                mStringTitle = "Installation Report";
                            }
                            String mStringDateTime = "NA";
                            if (mStringDate != null && !mStringDate.equalsIgnoreCase("") && !mStringDate.equalsIgnoreCase("null")) {
                                try {
                                    mCalendar.setTimeInMillis(Long.parseLong(mStringDate));
                                    mStringDateTime = mSimpleDateFormatDateDisplay.format(mCalendar.getTime());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            Uri pdfPath = Uri.fromFile(new File(mStringLocalPdfURL));
                            String msgData = "Hello, \n\n " + mStringTitle + " of " + mStringImeiNO + " for " + mStringVesselName + " has been completed successfully on " + mStringDateTime + ". Please see the certificate from attachment. \n\n ";
                            Intent sendIntent = new Intent(Intent.ACTION_SEND);
                            sendIntent.setData(Uri.parse("mailto:"));
                            sendIntent.setType("text/plain");
//                            sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"tech@succorfish.com"});
                            sendIntent.putExtra(Intent.EXTRA_SUBJECT, mStringTitle);
                            sendIntent.putExtra(Intent.EXTRA_TEXT, msgData);
                            sendIntent.putExtra(Intent.EXTRA_STREAM, pdfPath);
                            if (sendIntent.resolveActivity(mActivity.getPackageManager()) != null) {
                                mActivity.startActivity(sendIntent);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        return mViewRoot;
    }

    private void displayPdf() {
//            byte[] bytes = mStringFilePath.getBytes("UTF-8");
        byte[] bytes = Base64.decode(mStringPdfURL, Base64.DEFAULT);
        //        pdfView.fromUri(Uri.parse(mStringFilePath))
        pdfView.fromBytes(bytes)
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

    private void startWebView(String myPdfUrl) {
//        String url = "https://docs.google.com/gview?embedded=true&url=" + myPdfUrl;
//        String doc = "https://drive.google.com/viewerng/viewer?embedded=true&url=" + mStringLink;
        //Create new webview Client to show progress dialog
        //When opening a url or click on link
//        mWebView.getSettings().setLoadsImagesAutomatically(true);
//        mWebView.getSettings().setJavaScriptEnabled(true);
//        mWebView.getSettings().setSaveFormData(true);
//        mWebView.getSettings().setDomStorageEnabled(true);
//        mWebView.getSettings().setDatabaseEnabled(true);
//        mWebView.getSettings().setLoadWithOverviewMode(true);
//        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
//        mWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            mWebView.getSettings().getAllowFileAccessFromFileURLs();
//            mWebView.getSettings().getAllowUniversalAccessFromFileURLs();
//            mWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
//            mWebView.getSettings().setAllowFileAccessFromFileURLs(true);
//        }
//
//        mWebView.getSettings().getAllowContentAccess();
//        mWebView.getSettings().setAllowFileAccess(true);
//        mWebView.getSettings().setUseWideViewPort(false);
//        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
//        mWebView.getSettings().setTextSize(WebSettings.TextSize.LARGER);
//        mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
//        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
//        WebViewDatabase.getInstance(getActivity()).clearHttpAuthUsernamePassword();
//        mWebView.setHttpAuthUsernamePassword("v2-staging.succorfish.net", "https://v2-staging.succorfish.net/login/cas", mActivity.mPreferenceHelper.getUName(), mActivity.mPreferenceHelper.getUPassword());
//        mWebView.loadUrl(myPdfUrl);
//        mWebView.setDownloadListener(new DownloadListener() {
//            @Override
//            public void onDownloadStart(String url, String userAgent,
//                                        String contentDisposition, String mimeType,
//                                        long contentLength) {
//
////                String fileRootDirectory = mFileAppReportDirectory.getAbsolutePath();
////                System.out.println("fileRootDirectory-" + fileRootDirectory);
//                System.out.println("Web url-" + url);
//                System.out.println("Web userAgent-" + userAgent);
//                System.out.println("Web contentDisposition-" + contentDisposition);
//                System.out.println("Web mimeType-" + mimeType);
////
//                String outFileName = "TESTJD_" + "_Report.pdf";
//                DownloadManager.Request request = new DownloadManager.Request(
//                        Uri.parse(url));
//                request.setMimeType(mimeType);
//                String cookies = CookieManager.getInstance().getCookie(url);
//                request.addRequestHeader("cookie", cookies);
//                request.addRequestHeader("User-Agent", userAgent);
//                request.setDescription("Downloading Report...");
//                request.setTitle(outFileName);
//                request.allowScanningByMediaScanner();
//                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//                request.setDestinationInExternalPublicDir(
//                        Environment.DIRECTORY_DOWNLOADS, outFileName);
//                DownloadManager dm = (DownloadManager) mActivity.getSystemService(DOWNLOAD_SERVICE);
//                dm.enqueue(request);
////                File mFileDownload = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
////                mFileFolderDirectory = new File(mFileDownload.getAbsolutePath() + "/" + outFileName);
//            }
//        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        mActivity.mImageViewBack.setVisibility(View.GONE);
        mActivity.mImageViewAdd.setVisibility(View.GONE);
        if (isAdded()) {
            mActivity.mImageViewAdd.setImageResource(R.drawable.ic_add);
        }
    }

    @Override
    public void loadComplete(int nbPages) {

    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
    }

//    private class MyBrowser extends WebViewClient {
//        public void onReceivedHttpAuthRequest(WebView view,
//                                              HttpAuthHandler handler, String host, String realm) {
//            handler.proceed(mActivity.mPreferenceHelper.getUName(), mActivity.mPreferenceHelper.getUPassword());
//        }
//
//        @Override
//        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            view.loadUrl(url);
//            return true;
//        }
//
//        @Override
//        public void onPageFinished(WebView view, String url) {
//            super.onPageFinished(view, url);
//            mWebView.loadUrl("javascript:(function() { " +
//                    "document.querySelector('[role=\"toolbar\"]').remove();})()");
//        }
//    }
}
