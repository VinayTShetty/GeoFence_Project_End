package com.succorfish.fisherman.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;


import com.succorfish.fisherman.R;
import com.succorfish.fisherman.interfaces.onAlertDialogCallBack;
import com.succorfish.fisherman.views.ProgressHUD;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Jaydeep on 21/02/2018.
 */
public class Utility {

    Activity mActivity;

    ProgressHUD mProgressHUD;
    private PreferenceHelper mPreferenceHelper;
    AlertDialog mAlertDialog;
    AlertDialog mAlertDialogCallBack;
    AlertDialog mAlertDialogCallBackYesNO;
    public Utility(Activity mActivity) {
        this.mActivity = mActivity;
        mPreferenceHelper = new PreferenceHelper(mActivity);
    }


    public void errorDialog(String message) {
        if (mActivity != null) {
            if (mAlertDialog != null && mAlertDialog.isShowing()) {

            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
                builder.setMessage(message).setCancelable(true).setPositiveButton(mActivity.getString(R.string.str_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

                mAlertDialog = builder.create();
                mAlertDialog.show();
            }
        }
    }
    public void errorDialogWithCallBack(String message, final onAlertDialogCallBack mCallBack) {
        if (mActivity != null) {
            if (mAlertDialogCallBack != null && mAlertDialogCallBack.isShowing()) {

            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
                builder.setMessage(message).setCancelable(true).setPositiveButton(mActivity.getString(R.string.str_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mCallBack.PositiveMethod(dialog, id);
                        dialog.dismiss();
                    }
                });
                mAlertDialogCallBack = builder.create();
                mAlertDialogCallBack.show();
            }
        }
    }

    public void errorDialogWithYesNoCallBack(final String dialogTitle, final String dialogMessage, final String positiveBtnCaption, final String negativeBtnCaption, final boolean isCancelable, final int isSuccess, final onAlertDialogCallBack mCallBack) {
        if (mActivity != null) {
            if (mAlertDialogCallBackYesNO != null && mAlertDialogCallBackYesNO.isShowing()) {

            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
                builder.setTitle(dialogTitle);
                builder.setMessage(dialogMessage).setCancelable(isCancelable);
                builder.setPositiveButton(positiveBtnCaption, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mCallBack.PositiveMethod(dialog, id);
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(negativeBtnCaption, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mCallBack.NegativeMethod(dialog, id);
                        dialog.dismiss();
                    }
                });
                mAlertDialogCallBackYesNO = builder.create();
                mAlertDialogCallBackYesNO.show();
            }
        }
    }

    public boolean haveInternet() {

        NetworkInfo info = (NetworkInfo) ((ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

        if (info == null || !info.isConnected()) {
            return false;
        }
        if (info.isRoaming()) {
            return true;
        }
        return true;
    }

    public void hideKeyboard(Activity activity) {
        View view = mActivity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public final boolean isValidEmail(String target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public void ShowProgress() {
        if (mProgressHUD != null && mProgressHUD.isShowing()) {

        } else {
            mProgressHUD = ProgressHUD.showDialog(mActivity, true, false, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {

                }
            });
        }
    }

    public void HideProgress() {
        if (mProgressHUD != null) {
            mProgressHUD.dismiss();
        }
    }

    public void AlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
        builder.setMessage(message).setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }


    public boolean setListViewHeightBasedOnItems(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                item.measure(0, 0);
                totalItemsHeight += item.getMeasuredHeight();
            }

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() *
                    (numberOfItems - 1);

            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();

            return true;

        } else {
            return false;
        }

    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;

        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    public OkHttpClient getClient() {
//        final String encoding = Base64.encodeToString((mPreferenceHelper.getUserName() + ":" + mPreferenceHelper.getUserPassword()).getBytes(), Base64.DEFAULT);
//        System.out.println("encoding-"+encoding);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        httpClient.connectTimeout(1, TimeUnit.MINUTES);
        httpClient.readTimeout(2, TimeUnit.MINUTES);
        httpClient.writeTimeout(5, TimeUnit.MINUTES);

        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                String authToken = Credentials.basic("device_test", "dac6hTQXJc");
//                String credentials = mPreferenceHelper.getUserName() + ":" + mPreferenceHelper.getUserPassword();
//                final String basic =
//                        "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.DEFAULT);
                System.out.println("encoding basic-"+authToken);
                Request request = original.newBuilder()
                        .header("Content-Type", "application/json")
                        .header("Authorization", authToken)
                        .method(original.method(), original.body())
                        .build();

                return chain.proceed(request);
            }

        });

        OkHttpClient client = httpClient.build();
        return client;
    }
    public OkHttpClient getClientWithAutho() {
//        final String encoding = Base64.encodeToString((mPreferenceHelper.getUserName() + ":" + mPreferenceHelper.getUserPassword()).getBytes(), Base64.DEFAULT);
//        System.out.println("encoding-"+encoding);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        httpClient.connectTimeout(1, TimeUnit.MINUTES);
        httpClient.readTimeout(2, TimeUnit.MINUTES);
        httpClient.writeTimeout(5, TimeUnit.MINUTES);

        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                String authToken = Credentials.basic(mPreferenceHelper.getUName(), mPreferenceHelper.getUPassword());
//                String credentials = mPreferenceHelper.getUserName() + ":" + mPreferenceHelper.getUserPassword();
//                final String basic =
//                        "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.DEFAULT);
                System.out.println("encoding basic-"+authToken);
                Request request = original.newBuilder()
                        .header("Content-Type", "application/json")
                        .header("Authorization", authToken)
                        .method(original.method(), original.body())
                        .build();

                return chain.proceed(request);
            }

        });

        OkHttpClient client = httpClient.build();
        return client;
    }
    public OkHttpClient getSimpleClient() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .readTimeout(2, TimeUnit.MINUTES)
                .build();
        return client;
    }

    public String toCamelCase(String s) {
        if (s.length() == 0) {
            return s;
        }
        String[] parts = s.split(" ");
        String camelCaseString = "";
        for (String part : parts) {
            if (part.length() > 1)
                camelCaseString = camelCaseString + toProperCase(part) + " ";
            else if (part.equalsIgnoreCase(""))
                camelCaseString = camelCaseString + part.toUpperCase();
            else
                camelCaseString = camelCaseString + part.toUpperCase() + " ";
        }
        return camelCaseString;
    }

    public String toProperCase(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }


    public OkHttpClient getClientWithAutho(final String userName, final String password) {
//        final String encoding = Base64.encodeToString((mPreferenceHelper.getUserName() + ":" + mPreferenceHelper.getUserPassword()).getBytes(), Base64.DEFAULT);
//        System.out.println("encoding-"+encoding);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        httpClient.connectTimeout(1, TimeUnit.MINUTES);
        httpClient.readTimeout(2, TimeUnit.MINUTES);
        httpClient.writeTimeout(5, TimeUnit.MINUTES);

        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                String authToken = Credentials.basic(userName, password);
//                String credentials = mPreferenceHelper.getUserName() + ":" + mPreferenceHelper.getUserPassword();
//                final String basic =
//                        "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.DEFAULT);
                System.out.println("encoding basic-"+authToken);
                Request request = original.newBuilder()
                        .header("Content-Type", "application/json")
                        .header("Authorization", authToken)
                        .method(original.method(), original.body())
                        .build();

                return chain.proceed(request);
            }

        });

        OkHttpClient client = httpClient.build();
        return client;
    }

}
