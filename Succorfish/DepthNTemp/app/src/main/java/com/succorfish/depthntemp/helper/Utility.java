package com.succorfish.depthntemp.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.succorfish.depthntemp.R;
import com.succorfish.depthntemp.interfaces.onAlertDialogCallBack;
import com.succorfish.depthntemp.views.ProgressHUD;

import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Jaydeep on 13-03-2018.
 * Utility class
 */
public class Utility {

    Activity mActivity;

    ProgressHUD mProgressHUD;
    Dialog mAlertDialogYesNo;
    Dialog mAlertDialogCallBack;
    Dialog mAlertDialog;


    public Utility(Activity mActivity) {
        this.mActivity = mActivity;
    }


    public void errorDialog(final String message, final int isSuccess) {
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

    public void errorDialogWithCallBack(final String message, final int isSuccess, final boolean isCancelable, final onAlertDialogCallBack mCallBack) {
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mAlertDialogCallBack != null && mAlertDialogCallBack.isShowing()) {

                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
                        builder.setMessage(message);
                        builder.setCancelable(isCancelable);
                        builder.setPositiveButton(mActivity.getString(R.string.str_ok), new DialogInterface.OnClickListener() {
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

    public void errorDialogWithYesNoCallBack(final String dialogTitle, final String dialogMessage, final String positiveBtnCaption, final String negativeBtnCaption, final boolean isCancelable, final int isSuccess, final onAlertDialogCallBack mCallBack) {
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mAlertDialogCallBack != null && mAlertDialogCallBack.isShowing()) {

                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
                        builder.setTitle(dialogTitle);
                        builder.setMessage(dialogMessage);
                        builder.setCancelable(isCancelable);
                        builder.setPositiveButton(positiveBtnCaption, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mCallBack.PositiveMethod(dialog, which);
                                dialog.dismiss();
                            }
                        });

                        // Setting Negative "NO" Button
                        builder.setNegativeButton(negativeBtnCaption, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Write your code here to invoke NO event
                                mCallBack.NegativeMethod(dialog, which);
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

    public void updateProgressCount(int progress) {
        if (mActivity != null) {
            if (mProgressHUD != null && mProgressHUD.isShowing()) {
                mProgressHUD.updateProgress(progress);
            }
        }
    }

    public void ShowProgress(final String mStringTitle, final boolean isShowProgressCount) {
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mProgressHUD != null && mProgressHUD.isShowing()) {

                    } else {
                        mProgressHUD = ProgressHUD.showDialog(mActivity, mStringTitle, true, false, isShowProgressCount, new DialogInterface.OnCancelListener() {
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
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
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
            View item;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                item = listAdapter.getView(itemPos, null, listView);
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

}
