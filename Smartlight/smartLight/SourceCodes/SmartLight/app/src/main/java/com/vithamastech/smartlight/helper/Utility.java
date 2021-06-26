package com.vithamastech.smartlight.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.SpannableString;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.vithamastech.smartlight.R;
import com.vithamastech.smartlight.Views.ProgressHUD;
import com.vithamastech.smartlight.interfaces.onAlertDialogCallBack;
import com.vithamastech.smartlight.interfaces_dialog.WifiDialogSSIDCallBack;

import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.OkHttpClient;

/**
 * Created by Jaydeep on 13-03-2018.
 * Utility class include different custom alert dialog, check internet connection, hide keyboard, check valid email, Show/hide progress, retrofit clint object
 */
public class Utility {

    Activity mActivity;

    ProgressHUD mProgressHUD;
    Dialog mAlertDialogYesNo;
    Dialog mAlertDialogCallBack;
    Dialog mAlertDialog;

    /* Utility Construction */
    public Utility(Activity mActivity) {
        this.mActivity = mActivity;
    }

    /* Error Custom Message Dialog With No Action */
    public void errorDialog(final String message, final int isSuccess, final boolean isCancelable) {
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mAlertDialog != null && mAlertDialog.isShowing()) {

                    } else {
                        mAlertDialog = new Dialog(mActivity);
                        mAlertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        mAlertDialog.setContentView(R.layout.dialog_success);
                        mAlertDialog.setCancelable(isCancelable);

                        ColorDrawable back = new ColorDrawable(mActivity.getResources().getColor(R.color.colorTransparent));
                        InsetDrawable inset = new InsetDrawable(back, 0);
                        mAlertDialog.getWindow().setBackgroundDrawable(inset);

                        TextView mTextViewTitle = (TextView) mAlertDialog.findViewById(R.id.tv_title);
                        TextView mTextViewMessage = (TextView) mAlertDialog.findViewById(R.id.tv_message);
                        TextView mTextViewOk = (TextView) mAlertDialog.findViewById(R.id.tv_ok);
                        CircleImageView mCircleImageView = (CircleImageView) mAlertDialog.findViewById(R.id.dialog_success_img_icon);
                        if (isSuccess == 0) {
                            mTextViewOk.setBackgroundColor(mActivity.getResources().getColor(R.color.dialogSuccessBackgroundColor));
                            mCircleImageView.setImageResource(R.drawable.ic_success);
                        } else if (isSuccess == 1) {
                            mTextViewOk.setBackgroundColor(mActivity.getResources().getColor(R.color.dialogErrorBackgroundColor));
                            mCircleImageView.setImageResource(R.drawable.ic_failer);
                        } else if (isSuccess == 2) {
                            mTextViewOk.setBackgroundColor(mActivity.getResources().getColor(R.color.dialogErrorBackgroundColor));
                            mCircleImageView.setImageResource(R.drawable.ic_delete_alert);
                        } else {
                            mTextViewOk.setBackgroundColor(mActivity.getResources().getColor(R.color.dialogInfoBackgroundColor));
                            mCircleImageView.setImageResource(R.drawable.ic_alert_warning);
                        }
                        mTextViewMessage.setText(message);
                        mTextViewOk.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mAlertDialog.dismiss();
                            }
                        });
                        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                        lp.copyFrom(mAlertDialog.getWindow().getAttributes());
                        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                        mAlertDialog.show();
                        mAlertDialog.getWindow().setAttributes(lp);

                    }
                }
            });
        }
    }

    /* Error Custom Message Dialog With One Action */
    public void errorDialogWithCallBack(final String message, final int isSuccess, final boolean isCancelable, final onAlertDialogCallBack mCallBack) {
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mAlertDialogCallBack != null && mAlertDialogCallBack.isShowing()) {

                    } else {
                        mAlertDialogCallBack = new Dialog(mActivity);
                        mAlertDialogCallBack.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        mAlertDialogCallBack.setContentView(R.layout.dialog_success);
                        mAlertDialogCallBack.setCancelable(isCancelable);

                        ColorDrawable back = new ColorDrawable(mActivity.getResources().getColor(R.color.colorTransparent));
                        InsetDrawable inset = new InsetDrawable(back, 0);
                        mAlertDialogCallBack.getWindow().setBackgroundDrawable(inset);

                        TextView mTextViewTitle = (TextView) mAlertDialogCallBack.findViewById(R.id.tv_title);
                        TextView mTextViewMessage = (TextView) mAlertDialogCallBack.findViewById(R.id.tv_message);
                        TextView mTextViewOk = (TextView) mAlertDialogCallBack.findViewById(R.id.tv_ok);
                        CircleImageView mCircleImageView = (CircleImageView) mAlertDialogCallBack.findViewById(R.id.dialog_success_img_icon);

                        mTextViewMessage.setText(message);
                        if (isSuccess == 0) {
                            mTextViewOk.setBackgroundColor(mActivity.getResources().getColor(R.color.dialogSuccessBackgroundColor));
                            mCircleImageView.setImageResource(R.drawable.ic_success);
                        } else if (isSuccess == 1) {
                            mTextViewOk.setBackgroundColor(mActivity.getResources().getColor(R.color.dialogErrorBackgroundColor));
                            mCircleImageView.setImageResource(R.drawable.ic_failer);
                        } else if (isSuccess == 2) {
                            mTextViewOk.setBackgroundColor(mActivity.getResources().getColor(R.color.dialogErrorBackgroundColor));
                            mCircleImageView.setImageResource(R.drawable.ic_delete_alert);
                        } else {
                            mTextViewOk.setBackgroundColor(mActivity.getResources().getColor(R.color.dialogInfoBackgroundColor));
                            mCircleImageView.setImageResource(R.drawable.ic_alert_warning);
                        }
                        mTextViewOk.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mAlertDialogCallBack.dismiss();
                                if (mCallBack != null) {
                                    mCallBack.PositiveMethod(mAlertDialogCallBack, 0);
                                }
                            }
                        });
                        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                        lp.copyFrom(mAlertDialogCallBack.getWindow().getAttributes());
                        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                        mAlertDialogCallBack.show();
                        mAlertDialogCallBack.getWindow().setAttributes(lp);
                    }
                }
            });
        }
    }

    /* Error Custom Message Dialog With One Action */
    public void errorDialogWithCallBack(final SpannableString message, final int isSuccess, final boolean isCancelable, final onAlertDialogCallBack mCallBack) {
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mAlertDialogCallBack != null && mAlertDialogCallBack.isShowing()) {

                    } else {
                        mAlertDialogCallBack = new Dialog(mActivity);
                        mAlertDialogCallBack.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        mAlertDialogCallBack.setContentView(R.layout.dialog_success);
                        mAlertDialogCallBack.setCancelable(isCancelable);

                        ColorDrawable back = new ColorDrawable(mActivity.getResources().getColor(R.color.colorTransparent));
                        InsetDrawable inset = new InsetDrawable(back, 0);
                        mAlertDialogCallBack.getWindow().setBackgroundDrawable(inset);

                        TextView mTextViewTitle = (TextView) mAlertDialogCallBack.findViewById(R.id.tv_title);
                        TextView mTextViewMessage = (TextView) mAlertDialogCallBack.findViewById(R.id.tv_message);
                        TextView mTextViewOk = (TextView) mAlertDialogCallBack.findViewById(R.id.tv_ok);
                        CircleImageView mCircleImageView = (CircleImageView) mAlertDialogCallBack.findViewById(R.id.dialog_success_img_icon);

                        mTextViewMessage.setText(message);
                        if (isSuccess == 0) {
                            mTextViewOk.setBackgroundColor(mActivity.getResources().getColor(R.color.dialogSuccessBackgroundColor));
                            mCircleImageView.setImageResource(R.drawable.ic_success);
                        } else if (isSuccess == 1) {
                            mTextViewOk.setBackgroundColor(mActivity.getResources().getColor(R.color.dialogErrorBackgroundColor));
                            mCircleImageView.setImageResource(R.drawable.ic_failer);
                        } else if (isSuccess == 2) {
                            mTextViewOk.setBackgroundColor(mActivity.getResources().getColor(R.color.dialogErrorBackgroundColor));
                            mCircleImageView.setImageResource(R.drawable.ic_delete_alert);
                        } else {
                            mTextViewOk.setBackgroundColor(mActivity.getResources().getColor(R.color.dialogInfoBackgroundColor));
                            mCircleImageView.setImageResource(R.drawable.ic_alert_warning);
                        }
                        mTextViewOk.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mAlertDialogCallBack.dismiss();
                                if (mCallBack != null) {
                                    mCallBack.PositiveMethod(mAlertDialogCallBack, 0);
                                }
                            }
                        });
                        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                        lp.copyFrom(mAlertDialogCallBack.getWindow().getAttributes());
                        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                        mAlertDialogCallBack.show();
                        mAlertDialogCallBack.getWindow().setAttributes(lp);
                    }
                }
            });
        }
    }

    /* Error Custom Message Dialog With Two Action */
    public void errorDialogWithYesNoCallBack(final String dialogTitle, final String dialogMessage, final String positiveBtnCaption, final String negativeBtnCaption, final boolean isCancelable, final int isSuccess, final onAlertDialogCallBack mCallBack) {
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mAlertDialogYesNo != null && mAlertDialogYesNo.isShowing()) {

                    } else {
                        mAlertDialogYesNo = new Dialog(mActivity);
                        mAlertDialogYesNo.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        mAlertDialogYesNo.setContentView(R.layout.dialog_success);
                        mAlertDialogYesNo.setCancelable(isCancelable);

                        ColorDrawable back = new ColorDrawable(mActivity.getResources().getColor(R.color.colorTransparent));
                        InsetDrawable inset = new InsetDrawable(back, 0);
                        mAlertDialogYesNo.getWindow().setBackgroundDrawable(inset);

                        TextView mTextViewTitle = (TextView) mAlertDialogYesNo.findViewById(R.id.tv_title);
                        TextView mTextViewMessage = (TextView) mAlertDialogYesNo.findViewById(R.id.tv_message);
                        TextView mTextViewYes = (TextView) mAlertDialogYesNo.findViewById(R.id.tv_ok);
                        TextView mTextViewNo = (TextView) mAlertDialogYesNo.findViewById(R.id.tv_no);
                        CircleImageView mCircleImageView = (CircleImageView) mAlertDialogYesNo.findViewById(R.id.dialog_success_img_icon);
                        mTextViewTitle.setText(dialogTitle);
                        mTextViewMessage.setText(dialogMessage);
                        mTextViewYes.setText(positiveBtnCaption);
                        mTextViewNo.setText(negativeBtnCaption);
                        mTextViewNo.setVisibility(View.VISIBLE);
                        mTextViewYes.setBackgroundColor(mActivity.getResources().getColor(R.color.dialogSuccessBackgroundColor));
                        mTextViewNo.setBackgroundColor(mActivity.getResources().getColor(R.color.dialogErrorBackgroundColor));
                        if (isSuccess == 0) {
                            mCircleImageView.setImageResource(R.drawable.ic_success);
                        } else if (isSuccess == 1) {
                            mCircleImageView.setImageResource(R.drawable.ic_failer);
                        } else if (isSuccess == 2) {
                            mCircleImageView.setImageResource(R.drawable.ic_delete_alert);
                        } else {
                            mCircleImageView.setImageResource(R.drawable.ic_alert_warning);
                            mTextViewYes.setBackgroundColor(mActivity.getResources().getColor(R.color.dialogInfoBackgroundColor));
                            mTextViewNo.setBackgroundColor(mActivity.getResources().getColor(R.color.dialogNoticeBackgroundColor));
                        }
                        mTextViewYes.setOnClickListener(v -> {
                            mAlertDialogYesNo.dismiss();
                            if (mCallBack != null) {
                                mCallBack.PositiveMethod(mAlertDialogYesNo, 0);
                            }
                        });
                        mTextViewNo.setOnClickListener(v -> {
                            mAlertDialogYesNo.dismiss();
                            if (mCallBack != null) {
                                mCallBack.NegativeMethod(mAlertDialogYesNo, 0);
                            }
                        });
                        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                        lp.copyFrom(mAlertDialogYesNo.getWindow().getAttributes());
                        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                        mAlertDialogYesNo.show();
                        mAlertDialogYesNo.getWindow().setAttributes(lp);
                    }
                }
            });
        }
    }

    /* Check Internet Connection Available or not */
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

    /* Hide Open Activity Keyboard */
    public void hideKeyboard(Activity activity) {
        View view = mActivity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /* Check email address is valid or not */
    public final boolean isValidEmail(String target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    /* Show Progress */
    public void ShowProgress(final String mStringTitle) {
        if (mActivity != null) {
            mActivity.runOnUiThread(() -> {
                if (mProgressHUD != null && mProgressHUD.isShowing()) {

                } else {
                    mProgressHUD = ProgressHUD.showDialog(mActivity, mStringTitle, true, false, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {

                        }
                    });
                }
            });
        }
    }

    /* Hide Progress */
    public void HideProgress() {
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mActivity.isDestroyed()) {
                        return;
                    }
                    if (mProgressHUD != null) {
                        mProgressHUD.dismiss();
                    }
                }
            });
        }
    }

    /* Hide Progress */
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

    /* Retrofit Ok Http Client object */
    public OkHttpClient getSimpleClient() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .readTimeout(2, TimeUnit.MINUTES)
                .build();
        return client;
    }

    /*Convert String to lower case string*/
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
