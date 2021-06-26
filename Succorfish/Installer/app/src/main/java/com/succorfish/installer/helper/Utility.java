package com.succorfish.installer.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.succorfish.installer.MyApplication;
import com.succorfish.installer.R;
import com.succorfish.installer.interfaces.onAlertDialogCallBack;
import com.succorfish.installer.views.ProgressHUD;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Jaydeep on 21/02/2018.
 * Display custom alert dialog
 *
 */
public class Utility {

    Activity mActivity;

    ProgressHUD mProgressHUD;
    AlertDialog mAlertDialog;
    AlertDialog mAlertDialogCallBack;
    AlertDialog mAlertDialogYesNo;

    public Utility(Activity mActivity) {
        this.mActivity = mActivity;
    }


    public void errorDialog(final String message) {
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mAlertDialog != null && mAlertDialog.isShowing()) {

                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
                        builder.setMessage(message).setCancelable(false).setPositiveButton(mActivity.getString(R.string.str_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

                        mAlertDialog = builder.create();
                        mAlertDialog.show();
                    }
                }
            });
        }
    }

    public void errorDialogWithCallBack(final String message, final onAlertDialogCallBack mCallBack) {
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mAlertDialogCallBack != null && mAlertDialogCallBack.isShowing()) {

                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
                        builder.setMessage(message).setCancelable(false).setPositiveButton(mActivity.getString(R.string.str_ok), new DialogInterface.OnClickListener() {
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
            });
        }
    }

    public void errorDialogWithYesNoCallBack(final String dialogTitle,
                                             final String dialogMessage, final String positiveBtnCaption,
                                             final String negativeBtnCaption, final boolean isCancelable,
                                             final onAlertDialogCallBack mCallBack) {
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mAlertDialogYesNo != null && mAlertDialogYesNo.isShowing()) {

                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
                        builder.setTitle(dialogTitle);
                        builder.setMessage(dialogMessage).setCancelable(isCancelable).setPositiveButton(positiveBtnCaption, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                mCallBack.PositiveMethod(dialog, id);
                                dialog.dismiss();
                            }
                        }).setNegativeButton(negativeBtnCaption, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                mCallBack.NegativeMethod(dialog, id);
                                dialog.dismiss();
                            }
                        });
                        mAlertDialogYesNo = builder.create();
                        mAlertDialogYesNo.show();
                    }
                }
            });
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
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mProgressHUD != null && mProgressHUD.isShowing()) {

                    } else {
                        mProgressHUD = ProgressHUD.showDialog(mActivity, true, false, new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {

                            }
                        });
                    }
                }
            });
        }
    }

    public void HideProgress() {
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mProgressHUD != null) {
                        mProgressHUD.dismiss();
                    }
                }
            });
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
                System.out.println("encoding basic-" + authToken);
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

                String authToken = Credentials.basic(PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUName(), PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUPassword());
//                String credentials = mPreferenceHelper.getUserName() + ":" + mPreferenceHelper.getUserPassword();
//                final String basic =
//                        "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.DEFAULT);
                System.out.println("encoding basic-" + authToken);
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

    public static String fetchQuestionnaireJson(Activity mActivity, String filename) {
        try {
            InputStream is = mActivity.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
