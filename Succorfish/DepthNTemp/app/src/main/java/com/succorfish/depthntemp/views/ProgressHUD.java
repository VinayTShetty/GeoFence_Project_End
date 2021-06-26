package com.succorfish.depthntemp.views;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.succorfish.depthntemp.R;

/*Progress display*/
public class ProgressHUD extends Dialog {
    public static String mStringMessage;
    TextView mTextViewCount;
    static boolean isShowProgressCount = false;

    public ProgressHUD(Activity context) {
        super(context);
    }

    public ProgressHUD(Activity context, int theme) {
        super(context, theme);
    }


    public void onWindowFocusChanged(boolean hasFocus) {
//        mProgressBar = (ProgressBar) findViewById(R.id.spinkit_progress);
//        Sprite drawable = SpriteFactory.create(Style.FADING_CIRCLE);
//        mSpinKitViewProgress.setIndeterminateDrawable(drawable);
        TextView mTextViewTitle = (TextView) findViewById(R.id.progress_title);
        mTextViewCount = (TextView) findViewById(R.id.progress_count);
        mTextViewTitle.setText(mStringMessage);
        mTextViewCount.setText("(" + 0 + "%)");
        if (isShowProgressCount) {
            mTextViewCount.setVisibility(View.VISIBLE);
        }
    }

    public void updateProgress(int progressCount) {
        if (mTextViewCount != null) {
            mTextViewCount.setText("(" + progressCount + "%)");
        }
    }
//	public void setMessage(CharSequence message) {
//		if(message != null && message.length() > 0) {
//			findViewById(R.id.message).setVisibility(View.VISIBLE);
//			TextView txt = (TextView)findViewById(R.id.message);
//			txt.setText(message);
////			txt.invalidate();
////		}
//	}

//	public static ProgressHUD show(Context context, CharSequence message, boolean indeterminate, boolean cancelable,
//			OnCancelListener cancelListener) {
//		ProgressHUD dialog = new ProgressHUD(context,R.style.ProgressHUD);
//		dialog.setTitle("");
//		dialog.setContentView(R.layout.progress_hud);
//		if(message == null || message.length() == 0) {
//			dialog.findViewById(R.id.message).setVisibility(View.GONE);
//		} else {
//			TextView txt = (TextView)dialog.findViewById(R.id.message);
//			txt.setText(message);
//		}
//		dialog.setCancelable(cancelable);
//		dialog.setOnCancelListener(cancelListener);
//		dialog.getWindow().getAttributes().gravity=Gravity.CENTER;
//		WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
//		lp.dimAmount=0.2f;
//		dialog.getWindow().setAttributes(lp);
//		//dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
//		dialog.show();
//		return dialog;
//	}


    public static ProgressHUD showDialog(Activity context, String title, boolean indeterminate, boolean cancelable, boolean showProgress,
                                         OnCancelListener cancelListener) {
        isShowProgressCount = showProgress;
        ProgressHUD dialog = new ProgressHUD(context);
        dialog.setTitle("");
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.progress_hud);
        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(cancelListener);
        dialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        ColorDrawable back = new ColorDrawable(context.getResources().getColor(R.color.colorTransparent));
        InsetDrawable inset = new InsetDrawable(back, 0);
        dialog.getWindow().setBackgroundDrawable(inset);
//        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
//        lp.dimAmount = 0.2f;
//        dialog.getWindow().setAttributes(lp);
        //dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        mStringMessage = title;
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);
        dialog.show();
        return dialog;
    }
}
