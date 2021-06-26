package com.vithamastech.smartlight.helper;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.R;

public class CustomDialog {

    public static void dispDialog(Activity ac, String dialogMsg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ac, R.style.AppCompatAlertDialogStyle);
        builder.setCancelable(false);
        builder.setMessage(dialogMsg);
        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public static void dispDialogOpenSineUp(final MainActivity ac, String dialogMsg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ac, R.style.AppCompatAlertDialogStyle);
        builder.setCancelable(false);
        builder.setMessage(dialogMsg);
        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public static void ExitDialog(final Activity ac, String dialogMsg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ac, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Exit");
        builder.setCancelable(true);
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

    public static void ExitDialogDefault(final Activity ac, String dialogMsg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ac, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Exit");
        builder.setMessage(dialogMsg);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                ac.finish();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();

    }

    public static void hideKeyBoard(Activity mActivity) {
        View view = mActivity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void Snakbar(MainActivity mView, String strMsg) {

        if (mView != null && mView.mRelativeLayoutMain != null) {
            Snackbar snackbar = Snackbar.make(mView.mRelativeLayoutMain, strMsg, Snackbar.LENGTH_LONG);
            TextView textView = (TextView) snackbar.getView().findViewById(R.id.snackbar_text);
            textView.setTextColor(Color.WHITE);
            snackbar.show();
        }
    }

    public static void SnakbarOk(View mView, String strMsg) {
        if (mView != null && strMsg != null) {
            try {
                final Snackbar snackbar = Snackbar.make(mView, strMsg, Snackbar.LENGTH_LONG);
                snackbar.setAction("Ok", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbar.dismiss();
                    }
                });
                // Changing message text color
                snackbar.setActionTextColor(Color.WHITE);
                // Changing action button text color
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView.findViewById(R.id.snackbar_text);
                textView.setTextColor(Color.YELLOW);
                snackbar.show();
            } catch (Exception e) {
            }
        }
    }
}
