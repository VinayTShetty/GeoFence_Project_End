package com.vithamastech.smartlight.Views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.vithamastech.smartlight.R;
import com.vithamastech.smartlight.helper.DrawableUtilities;

/* Pick Color from any image.*/
public class ColorPicker extends RelativeLayout {

    private ImageView ivColorPicker;
    private ImageView viewBg;
    private Bitmap drawableToBitmap;
    //    private View viewOverlay;
    private boolean isTouchable;
    //    private int ctScale = 347;
    private ColorSelectedListener colorSelectedListener;

    public ColorSelectedListener getColorSelectedListener() {
        return colorSelectedListener;
    }

    public void setColorSelectedListener(ColorSelectedListener colorSelectedListener) {
        this.colorSelectedListener = colorSelectedListener;
    }


    public ColorPicker(Context context) {
        this(context, null, -1);
    }

    public ColorPicker(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public ColorPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    /*Initialize view*/
    private void initView(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.content_color_picker, this, true);
        ivColorPicker = view.findViewById(R.id.iv_color_picker);
        viewBg = view.findViewById(R.id.view_bg);
//        viewOverlay = view.findViewById(R.id.view_overlay);
    }

    /* Set Color in Picker*/
    private void setColorInPicker(int[] colorXY) {
        int colorX = colorXY[0];
        int colorY = colorXY[1];

        colorXY[0] = ((colorXY[0] * viewBg.getWidth()) / drawableToBitmap.getWidth()) - (ivColorPicker.getWidth() / 2);
        colorXY[1] = ((colorXY[1] * viewBg.getHeight()) / drawableToBitmap.getHeight()) - (ivColorPicker.getHeight() / 2);

        if (colorXY[0] < 0) {
            colorXY[0] = 0;
        }
        if (colorXY[1] < 0) {
            colorXY[1] = 0;
        }
        int finalColor = drawableToBitmap.getPixel(colorX, colorY);

        ivColorPicker.setX(colorXY[0]);
        ivColorPicker.setY(colorXY[1]);
        ((GradientDrawable) ivColorPicker.getBackground()).setColor(finalColor);
    }

    /* get Selected Color interface*/
    public interface ColorSelectedListener {

        void onColorSelected(@ColorInt int color, boolean isTapUp);
    }

    public void setGradientView(int resource, boolean isFullScreen) {
        if (resource == -1) {
            throw new RuntimeException("resource cannot be null");

        }
        drawableToBitmap = DrawableUtilities.
                drawableToBitmap(ContextCompat.getDrawable(getContext(), resource));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            viewBg.setImageBitmap(drawableToBitmap);
        }
        if (isFullScreen) {
            viewBg.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
        } else {
            viewBg.getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        }
        viewBg.requestLayout();
        setScreenTouchListener();
    }

    /* View Touch screen event*/
    public void setScreenTouchListener() {
        viewBg.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ivColorPicker.setVisibility(VISIBLE);
                ColorPicker.this.getParent().requestDisallowInterceptTouchEvent(true);
                int y = (int) (event.getY() - ivColorPicker.getHeight() / 2);
                int x = (int) (event.getX() - ivColorPicker.getWidth() / 2);

                if (x < -ivColorPicker.getWidth() / 2) {
                    x = -ivColorPicker.getWidth() / 2;
                }
                if (y < -ivColorPicker.getHeight() / 2) {
                    y = -ivColorPicker.getHeight() / 2;
                }
                if (x > viewBg.getWidth() - ivColorPicker.getWidth() / 2) {
                    x = viewBg.getWidth() - ivColorPicker.getWidth() / 2;
                }
                if (y > viewBg.getHeight() - ivColorPicker.getHeight() / 2) {
                    y = viewBg.getHeight() - ivColorPicker.getHeight() / 2;
                }
                ivColorPicker.setX(x);
                ivColorPicker.setY(y);

                float newy = ((event.getY()) / viewBg.getHeight()) * drawableToBitmap.getHeight();
                float newx = ((event.getX() / viewBg.getWidth()) * drawableToBitmap.getWidth());

                if (newx < 0) {
                    newx = 0;
                }
                if (newy < 0) {
                    newy = 0;
                }
                if (newx >= drawableToBitmap.getWidth()) {
                    newx = drawableToBitmap.getWidth() - 1;
                }
                if (newy >= drawableToBitmap.getHeight()) {
                    newy = drawableToBitmap.getHeight() - 1;
                }
                int color = drawableToBitmap.getPixel((int) newx,
                        (int) newy);
                ((GradientDrawable) ivColorPicker.getBackground()).setColor(color);

                if (colorSelectedListener != null) {
                    colorSelectedListener.onColorSelected(color, false);
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    ColorPicker.this.getParent().requestDisallowInterceptTouchEvent(false);
                    if (colorSelectedListener != null) {
                        colorSelectedListener.onColorSelected(color, true);
                    }
                }
                return true;
            }
        });
    }

    // TODO may be required in future feature development
    public void setViewOverlay(int overlayVisibility) {
//        viewOverlay.setVisibility(overlayVisibility);
//        isTouchable = viewOverlay.getVisibility() != VISIBLE;
    }

    /**
     * This method will provide custom shape to the picker
     * @param resource resource should be a gradient drawable
     */
    public void setColorPicker(int resource) {
        if (resource != -1) {
            ivColorPicker.setBackgroundResource(resource);
            ivColorPicker.setVisibility(INVISIBLE);
        } else {
            throw new RuntimeException("resource cannot be null");
        }
    }
}

