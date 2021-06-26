package com.succorfish.geofence.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.succorfish.geofence.R;
import com.succorfish.geofence.interfaces.ChatMessageText;
import com.succorfish.geofence.interfaces.ResetDeviceDialogCallBack;
import com.succorfish.geofence.interfaces.onAlertDialogCallBack;
import com.succorfish.geofence.interfaces.onDeviceNameAlert;

import de.hdodenhof.circleimageview.CircleImageView;
public class DialogProvider {
    Activity mActivity;
    Dialog mAlertDialogYesNo;
    Dialog mAlertDialogCallBack;
    Dialog mAlertDialog;
    Dialog name_saveDialogCallBack;
    Dialog resetDeviceCallBack;
    public DialogProvider(Activity mActivity) {
        this.mActivity = mActivity;
    }
    public void showGeofenceAlertDialog(final String headerRuleVioation, final String deviceName_withBleAddress,String message_One,String message_Two, final int isSuccess, final boolean isCancelable, final onAlertDialogCallBack mCallBack) {
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mAlertDialogCallBack != null && mAlertDialogCallBack.isShowing()) {
                        mAlertDialogCallBack.dismiss();
                        mAlertDialogCallBack = new Dialog(mActivity);
                        mAlertDialogCallBack.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        mAlertDialogCallBack.setContentView(R.layout.dialog_success);
                        mAlertDialogCallBack.setCancelable(isCancelable);

                        ColorDrawable back = new ColorDrawable(mActivity.getResources().getColor(R.color.colorTransparent));
                        InsetDrawable inset = new InsetDrawable(back, 0);
                        mAlertDialogCallBack.getWindow().setBackgroundDrawable(inset);

                        TextView mtextViewHeaderRuleVioation = (TextView) mAlertDialogCallBack.findViewById(R.id.header_rule_violation);

                        TextView mTextViewble_address = (TextView) mAlertDialogCallBack.findViewById(R.id.ble_device_address);
                        TextView mTextViewMessage_one = (TextView) mAlertDialogCallBack.findViewById(R.id.tv_message_one);
                        TextView mTextViewMessage_two = (TextView) mAlertDialogCallBack.findViewById(R.id.tv_message_two);

                        TextView mTextViewOk = (TextView) mAlertDialogCallBack.findViewById(R.id.tv_ok);
                        TextView mTexViewno=(TextView)mAlertDialogCallBack.findViewById(R.id.tv_no);
                        CircleImageView mCircleImageView = (CircleImageView) mAlertDialogCallBack.findViewById(R.id.dialog_success_img_icon);

                        mtextViewHeaderRuleVioation.setText(headerRuleVioation+"!");
                        mTextViewble_address.setText(deviceName_withBleAddress);
                        mTextViewMessage_one.setText(message_One);
                        mTextViewMessage_two.setText(message_Two);
                        if (isSuccess == 0) {
                            mTextViewOk.setBackgroundColor(mActivity.getResources().getColor(R.color.dialogSuccessBackgroundColor));
                            mCircleImageView.setImageResource(R.drawable.dialog_sucess_img_icon);
                        } else if (isSuccess == 1) {
                            mTextViewOk.setBackgroundColor(mActivity.getResources().getColor(R.color.dialogSuccessBackgroundColor));
                            mCircleImageView.setImageResource(R.drawable.ic_failer_vector);
                        } else if (isSuccess == 2) {
                            mTextViewOk.setBackgroundColor(mActivity.getResources().getColor(R.color.dialogSuccessBackgroundColor));
                            mCircleImageView.setImageResource(R.drawable.ic_delete_forever_black);
                        } else {
                            mTextViewOk.setBackgroundColor(mActivity.getResources().getColor(R.color.colorWhite));
                            mCircleImageView.setImageResource(R.drawable.ic_warning);
                        }
                        mTextViewOk.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mAlertDialogCallBack.dismiss();
                                mCallBack.PositiveMethod(mAlertDialogCallBack, 1);
                            }
                        });

                        mTexViewno.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mAlertDialogCallBack.dismiss();
                                mCallBack.NegativeMethod(mAlertDialogCallBack,1);
                            }
                        });
                        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                        lp.copyFrom(mAlertDialogCallBack.getWindow().getAttributes());
                        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                        mAlertDialogCallBack.show();
                        mAlertDialogCallBack.getWindow().setAttributes(lp);
                    } else {
                        mAlertDialogCallBack = new Dialog(mActivity);
                        mAlertDialogCallBack.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        mAlertDialogCallBack.setContentView(R.layout.dialog_success);
                        mAlertDialogCallBack.setCancelable(isCancelable);

                        ColorDrawable back = new ColorDrawable(mActivity.getResources().getColor(R.color.colorTransparent));
                        InsetDrawable inset = new InsetDrawable(back, 0);
                        mAlertDialogCallBack.getWindow().setBackgroundDrawable(inset);

                        TextView mtextViewHeaderRuleVioation = (TextView) mAlertDialogCallBack.findViewById(R.id.header_rule_violation);

                        TextView mTextViewble_address = (TextView) mAlertDialogCallBack.findViewById(R.id.ble_device_address);
                        TextView mTextViewMessage_one = (TextView) mAlertDialogCallBack.findViewById(R.id.tv_message_one);
                        TextView mTextViewMessage_two = (TextView) mAlertDialogCallBack.findViewById(R.id.tv_message_two);

                        TextView mTextViewOk = (TextView) mAlertDialogCallBack.findViewById(R.id.tv_ok);
                        TextView mTexViewno=(TextView)mAlertDialogCallBack.findViewById(R.id.tv_no);
                        CircleImageView mCircleImageView = (CircleImageView) mAlertDialogCallBack.findViewById(R.id.dialog_success_img_icon);

                        mtextViewHeaderRuleVioation.setText(headerRuleVioation+"!");
                        mTextViewble_address.setText(deviceName_withBleAddress);
                        mTextViewMessage_one.setText(message_One);
                        mTextViewMessage_two.setText(message_Two);
                        if (isSuccess == 0) {
                            mTextViewOk.setBackgroundColor(mActivity.getResources().getColor(R.color.dialogSuccessBackgroundColor));
                            mCircleImageView.setImageResource(R.drawable.dialog_sucess_img_icon);
                        } else if (isSuccess == 1) {
                            mTextViewOk.setBackgroundColor(mActivity.getResources().getColor(R.color.dialogSuccessBackgroundColor));
                            mCircleImageView.setImageResource(R.drawable.ic_failer_vector);
                        } else if (isSuccess == 2) {
                            mTextViewOk.setBackgroundColor(mActivity.getResources().getColor(R.color.dialogSuccessBackgroundColor));
                            mCircleImageView.setImageResource(R.drawable.ic_delete_forever_black);
                        } else {
                            mTextViewOk.setBackgroundColor(mActivity.getResources().getColor(R.color.colorWhite));
                            mCircleImageView.setImageResource(R.drawable.ic_warning);
                        }
                        mTextViewOk.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mAlertDialogCallBack.dismiss();
                                mCallBack.PositiveMethod(mAlertDialogCallBack, 0);
                            }
                        });

                        mTexViewno.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mAlertDialogCallBack.dismiss();
                                mCallBack.NegativeMethod(mAlertDialogCallBack,0);
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
    public static void ExitDialog(final Activity ac, String dialogMsg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ac, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Exit");
        builder.setCancelable(false);
        builder.setMessage(dialogMsg);
        builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ac.finish();
            }
        });
        builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();

    }
    public void enterNameDialog(final onDeviceNameAlert mCallBack) {
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (name_saveDialogCallBack != null && name_saveDialogCallBack.isShowing()){ }
                    else {
                        name_saveDialogCallBack = new Dialog(mActivity);
                        name_saveDialogCallBack.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        name_saveDialogCallBack.setContentView(R.layout.device_name);
                        name_saveDialogCallBack.setCancelable(true);
                        ColorDrawable back = new ColorDrawable(mActivity.getResources().getColor(R.color.colorTransparent));
                        InsetDrawable inset = new InsetDrawable(back, 0);
                        name_saveDialogCallBack.getWindow().setBackgroundDrawable(inset);
                        TextView deviceNameEditText=(TextView)name_saveDialogCallBack.findViewById(R.id.devicename);
                        Button okbutton = (Button) name_saveDialogCallBack.findViewById(R.id.okbutton);
                        Button cancelButton=(Button)name_saveDialogCallBack.findViewById(R.id.cancel_button);
                        okbutton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                name_saveDialogCallBack.dismiss();
                                String deviceNameString=deviceNameEditText.getText().toString();
                                mCallBack.PositiveMethod(name_saveDialogCallBack, 1,deviceNameString);
                            }
                        });

                        cancelButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                name_saveDialogCallBack.dismiss();
                                mCallBack.NegativeMethod(name_saveDialogCallBack,1);
                            }
                        });
                        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                        lp.copyFrom(name_saveDialogCallBack.getWindow().getAttributes());
                        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                        name_saveDialogCallBack.show();
                        name_saveDialogCallBack.getWindow().setAttributes(lp);
                    }
                }
            });
        }
    }

    public void showAlertDialog (Activity activity,String headerTitle,String title){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(headerTitle);
        builder.setMessage(title);        // add a button
        builder.setPositiveButton("OK", null);        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void resetDeviceDialog(final ResetDeviceDialogCallBack resetDeviceDialogCallBack){
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (resetDeviceCallBack != null && resetDeviceCallBack.isShowing()){ }
                    else {
                        resetDeviceCallBack = new Dialog(mActivity);
                        resetDeviceCallBack.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        resetDeviceCallBack.setContentView(R.layout.reset_device_layout);
                        resetDeviceCallBack.setCancelable(true);
                        ColorDrawable back = new ColorDrawable(mActivity.getResources().getColor(R.color.colorTransparent));
                        InsetDrawable inset = new InsetDrawable(back, 0);
                        resetDeviceCallBack.getWindow().setBackgroundDrawable(inset);
                        Button okbutton = (Button) resetDeviceCallBack.findViewById(R.id.okbutton);
                        Button cancelButton=(Button)resetDeviceCallBack.findViewById(R.id.cancel_button);
                        okbutton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                resetDeviceDialogCallBack.PositiveMethod(resetDeviceCallBack, 1);
                                resetDeviceCallBack.dismiss();
                            }
                        });

                        cancelButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                resetDeviceDialogCallBack.NegativeMethod(resetDeviceCallBack, 1);
                                resetDeviceCallBack.dismiss();

                            }
                        });
                        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                        lp.copyFrom(resetDeviceCallBack.getWindow().getAttributes());
                        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                        resetDeviceCallBack.show();
                        resetDeviceCallBack.getWindow().setAttributes(lp);
                    }
                }
            });
        }
    }

    public void errorDialog(final String message) {
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mAlertDialog != null && mAlertDialog.isShowing()) {

                    } else {
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
                        builder.setMessage(message).setCancelable(false).setPositiveButton("ok", new DialogInterface.OnClickListener() {
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

    public void errorDialogWithCallBack(final String title,final String message, final int isSuccess, final boolean isCancelable, final onAlertDialogCallBack mCallBack) {
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mAlertDialogCallBack != null && mAlertDialogCallBack.isShowing()) {

                    } else {
                        mAlertDialogCallBack = new Dialog(mActivity);
                        mAlertDialogCallBack.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        mAlertDialogCallBack.setContentView(R.layout.dialog_layout);
                        mAlertDialogCallBack.setCancelable(isCancelable);

                        ColorDrawable back = new ColorDrawable(mActivity.getResources().getColor(R.color.colorTransparent));
                        InsetDrawable inset = new InsetDrawable(back, 0);
                        mAlertDialogCallBack.getWindow().setBackgroundDrawable(inset);

                        TextView mTextViewTitle = (TextView) mAlertDialogCallBack.findViewById(R.id.tv_title);
                        TextView mTextViewMessage = (TextView) mAlertDialogCallBack.findViewById(R.id.tv_message);
                        TextView mTextViewOk = (TextView) mAlertDialogCallBack.findViewById(R.id.tv_ok);
                        CircleImageView mCircleImageView = (CircleImageView) mAlertDialogCallBack.findViewById(R.id.dialog_success_img_icon);

                        mTextViewMessage.setText(message);
                        mTextViewTitle.setText(title);
                        if (isSuccess == 0) {
                            mTextViewOk.setBackgroundColor(mActivity.getResources().getColor(R.color.activate_color_button));
                            mCircleImageView.setImageResource(R.drawable.dialog_sucess_img_icon);
                        } else if (isSuccess == 1) {
                            mTextViewOk.setBackgroundColor(mActivity.getResources().getColor(R.color.dialogSuccessBackgroundColor));
                            mCircleImageView.setImageResource(R.drawable.ic_failer_vector);
                        } else if (isSuccess == 2) {
                            mTextViewOk.setBackgroundColor(mActivity.getResources().getColor(R.color.dialogSuccessBackgroundColor));
                            mCircleImageView.setImageResource(R.drawable.ic_delete_forever_black);
                        } else {
                            mTextViewOk.setBackgroundColor(mActivity.getResources().getColor(R.color.dialogSuccessBackgroundColor));
                            mCircleImageView.setImageResource(R.drawable.ic_warning);
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
            });
        }
    }



}
