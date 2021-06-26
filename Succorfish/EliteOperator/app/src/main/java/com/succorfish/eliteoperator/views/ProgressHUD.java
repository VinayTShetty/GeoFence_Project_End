package com.succorfish.eliteoperator.views;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.github.ybq.android.spinkit.SpinKitView;
import com.github.ybq.android.spinkit.SpriteFactory;
import com.github.ybq.android.spinkit.Style;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.succorfish.eliteoperator.R;


public class ProgressHUD extends Dialog {
    public static String mStringMessage;

    public ProgressHUD(Activity context) {
        super(context);
    }

    public ProgressHUD(Activity context, int theme) {
        super(context, theme);
    }


    public void onWindowFocusChanged(boolean hasFocus) {
        SpinKitView mSpinKitViewProgress = (SpinKitView) findViewById(R.id.spinkit_progress);
        Sprite drawable = SpriteFactory.create(Style.FADING_CIRCLE);
        mSpinKitViewProgress.setIndeterminateDrawable(drawable);
        TextView mTextViewTitle = (TextView) findViewById(R.id.progress_title);
        mTextViewTitle.setText(mStringMessage);
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


    public static ProgressHUD showDialog(Activity context, String title, boolean indeterminate, boolean cancelable,
                                         OnCancelListener cancelListener) {
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
