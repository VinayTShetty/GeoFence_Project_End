package com.succorfish.fisherman.views;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.WindowManager;

import com.github.ybq.android.spinkit.SpinKitView;
import com.github.ybq.android.spinkit.SpriteFactory;
import com.github.ybq.android.spinkit.Style;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.succorfish.fisherman.R;


public class ProgressHUD extends Dialog {
    public ProgressHUD(Activity context) {
        super(context);
    }

    public ProgressHUD(Activity context, int theme) {
        super(context, theme);
    }


    public void onWindowFocusChanged(boolean hasFocus) {
        SpinKitView mSpinKitViewProgress;
        mSpinKitViewProgress = (SpinKitView) findViewById(R.id.activity_main_spinkit_progress);
        Sprite drawable = SpriteFactory.create(Style.DOUBLE_BOUNCE);
        mSpinKitViewProgress.setIndeterminateDrawable(drawable);
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


    public static ProgressHUD showDialog(Activity context, boolean indeterminate, boolean cancelable,
                                         OnCancelListener cancelListener) {
        ProgressHUD dialog = new ProgressHUD(context);
        dialog.setTitle("");
        dialog.setContentView(R.layout.progress_hud);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(cancelListener);
        dialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.dimAmount = 0.2f;
        dialog.getWindow().setAttributes(lp);
        //dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        dialog.show();
        return dialog;
    }
}
