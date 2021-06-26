package com.succorfish.eliteoperator.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.succorfish.eliteoperator.R;
import com.succorfish.eliteoperator.interfaces.onAlertDialogCallBack;
import com.succorfish.eliteoperator.views.ProgressHUD;

import de.hdodenhof.circleimageview.CircleImageView;


public class Utility {

    Activity mActivity;
    Dialog mAlertDialogCallBack;
    ProgressHUD mProgressHUD;
    Dialog mAlertDialog;
    Dialog mAlertDialogYesNo;

    public Utility(Activity mActivity) {
        this.mActivity = mActivity;
    }


    public void errorDialog(String message, int isSuccess) {
        if (mActivity != null) {
            if (mAlertDialog != null && mAlertDialog.isShowing()) {

            } else {
                mAlertDialog = new Dialog(mActivity);
                mAlertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                mAlertDialog.setContentView(R.layout.dialog_success);
                mAlertDialog.setCancelable(false);

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
                } else {
                    mTextViewOk.setBackgroundColor(mActivity.getResources().getColor(R.color.dialogErrorBackgroundColor));
                    mCircleImageView.setImageResource(R.drawable.ic_failer);
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

    }

    public void errorDialogWithCallBack(String message, int isSuccess, boolean isCancelable, final onAlertDialogCallBack mCallBack) {
        if (mActivity != null) {
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
                } else {
                    mTextViewOk.setBackgroundColor(mActivity.getResources().getColor(R.color.dialogErrorBackgroundColor));
                    mCircleImageView.setImageResource(R.drawable.ic_failer);
                }
                mTextViewOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAlertDialogCallBack.dismiss();
                        mCallBack.PositiveMethod(mAlertDialogCallBack, 0);
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
    }

    public void errorDialogWithYesNoCallBack(String dialogTitle, String message, String yesTitle, String noTitle, int isSuccess,final onAlertDialogCallBack mCallBack) {
        if (mActivity != null) {
            if (mAlertDialogYesNo != null && mAlertDialogYesNo.isShowing()) {

            } else {
                mAlertDialogYesNo = new Dialog(mActivity);
                mAlertDialogYesNo.requestWindowFeature(Window.FEATURE_NO_TITLE);
                mAlertDialogYesNo.setContentView(R.layout.dialog_success);
                mAlertDialogYesNo.setCancelable(false);

                ColorDrawable back = new ColorDrawable(mActivity.getResources().getColor(R.color.colorTransparent));
                InsetDrawable inset = new InsetDrawable(back, 0);
                mAlertDialogYesNo.getWindow().setBackgroundDrawable(inset);

                TextView mTextViewTitle = (TextView) mAlertDialogYesNo.findViewById(R.id.tv_title);
                TextView mTextViewMessage = (TextView) mAlertDialogYesNo.findViewById(R.id.tv_message);
                TextView mTextViewYes = (TextView) mAlertDialogYesNo.findViewById(R.id.tv_ok);
                TextView mTextViewNo = (TextView) mAlertDialogYesNo.findViewById(R.id.tv_no);
                CircleImageView mCircleImageView = (CircleImageView) mAlertDialogYesNo.findViewById(R.id.dialog_success_img_icon);
                mTextViewTitle.setText(dialogTitle);
                mTextViewMessage.setText(message);
                mTextViewYes.setText(yesTitle);
                mTextViewNo.setText(noTitle);
                mTextViewNo.setVisibility(View.VISIBLE);

                if (isSuccess == 0) {
                    mTextViewNo.setBackgroundColor(mActivity.getResources().getColor(R.color.dialogSuccessBackgroundColor));
                    mCircleImageView.setImageResource(R.drawable.ic_success);
                } else {
                    mTextViewNo.setBackgroundColor(mActivity.getResources().getColor(R.color.dialogErrorBackgroundColor));
                    mCircleImageView.setImageResource(R.drawable.ic_failer);
                }

                mTextViewYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAlertDialogYesNo.dismiss();
                        mCallBack.PositiveMethod(mAlertDialogYesNo, 0);
                    }
                });
                mTextViewNo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAlertDialogYesNo.dismiss();
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

    public void ShowProgress(final String mStringTitle) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressHUD != null && mProgressHUD.isShowing()) {

                } else {
                    mProgressHUD = ProgressHUD.showDialog(mActivity, mStringTitle,true, false, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {

                        }
                    });
                }
            }
        });

    }
    public void HideProgress() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressHUD != null) {
                    mProgressHUD.dismiss();
                }
            }
        });

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

//    public OkHttpClient getClient() {
//        OkHttpClient client = new OkHttpClient.Builder()
////                .connectTimeout(5, TimeUnit.MINUTES)
////                .readTimeout(5, TimeUnit.MINUTES)
//                .connectTimeout(1, TimeUnit.MINUTES)
//                .writeTimeout(5, TimeUnit.MINUTES)
//                .readTimeout(2, TimeUnit.MINUTES)
//                .build();
//        return client;
//    }

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
