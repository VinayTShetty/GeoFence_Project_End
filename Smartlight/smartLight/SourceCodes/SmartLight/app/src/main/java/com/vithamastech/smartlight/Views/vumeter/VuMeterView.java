package com.vithamastech.smartlight.Views.vumeter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.vithamastech.smartlight.R;

import java.util.Random;

/* For Display Music waves*/
public class VuMeterView extends View {
    public static final String LOG_TAG = "VuMeterView";
    public static final int DEFAULT_NUMBER_BLOCK = 3;
    public static final int DEFAULT_NUMBER_RANDOM_VALUES = 10;
    public static final int DEFAULT_BLOCK_SPACING = 20;
    public static final int DEFAULT_SPEED = 10;
    public static final int DEFAULT_STOP_SIZE = 30;
    public static final boolean DEFAULT_START_OFF = false;
    public static final int FPS = 60;
    public static final int STATE_PAUSE = 0;
    public static final int STATE_STOP = 1;
    public static final int STATE_PLAYING = 2;
    private int mColor;
    private int mColor1;
    private int mColor2;
    private int mBlockNumber;
    private float mBlockSpacing;
    private int mSpeed;
    private float mStopSize;
    private Paint mPaint = new Paint();
    private Random mRandom = new Random();
    private int mState;
    private int mBlockWidth;
    private int mDrawPass;
    private int mBlockPass;
    private int mContentHeight;
    private int mContentWidth;
    private int mPaddingLeft;
    private int mPaddingTop;
    private int mPaddingRight;
    private int mPaddingBottom;
    private int mLeft;
    private int mTop;
    private int mRight;
    private float[][] mBlockValues;
    private Dynamics[] mDestinationValues;

    public VuMeterView(Context context) {
        super(context);
        this.init((AttributeSet) null, 0);
    }

    public VuMeterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(attrs, 0);
    }

    public VuMeterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        TypedArray a = this.getContext().obtainStyledAttributes(attrs, R.styleable.vumeter_VuMeterView, defStyle, 0);
        this.mColor = a.getColor(R.styleable.vumeter_VuMeterView_vumeter_backgroundColor, -16777216);
        this.mBlockNumber = a.getInt(R.styleable.vumeter_VuMeterView_vumeter_blockNumber, 3);
        this.mBlockSpacing = a.getDimension(R.styleable.vumeter_VuMeterView_vumeter_blockSpacing, 20.0F);
        this.mSpeed = a.getInt(R.styleable.vumeter_VuMeterView_vumeter_speed, 10);
        this.mStopSize = a.getDimension(R.styleable.vumeter_VuMeterView_vumeter_stopSize, 30.0F);
        boolean startOff = a.getBoolean(R.styleable.vumeter_VuMeterView_vumeter_startOff, false);
        a.recycle();
        this.initialiseCollections();
        this.mPaint.setColor(this.mColor);
        if (startOff) {
            this.mState = 0;
        } else {
            this.mState = 2;
        }

        this.mDrawPass = this.mBlockPass = this.mContentHeight = this.mContentWidth = this.mPaddingLeft = this.mPaddingTop = this.mLeft = this.mTop = this.mPaddingRight = this.mPaddingBottom = this.mRight = 0;
    }

    @SuppressLint({"DrawAllocation"})
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.mPaddingLeft = this.getPaddingLeft();
        this.mPaddingTop = this.getPaddingTop();
        this.mPaddingRight = this.getPaddingRight();
        this.mPaddingBottom = this.getPaddingBottom();
        this.mContentWidth = this.getWidth() - this.mPaddingLeft - this.mPaddingRight;
        this.mContentHeight = this.getHeight() - this.mPaddingTop - this.mPaddingBottom;
        if (this.mBlockWidth == 0) {
            this.mBlockWidth = (int) (((float) this.mContentWidth - (float) (this.mBlockNumber - 1) * this.mBlockSpacing) / (float) this.mBlockNumber);
            if (this.mState == 0) {
                int stopSize = (int) ((float) this.mContentHeight - this.mStopSize);

                for (int i = 0; i < this.mBlockNumber; ++i) {
                    this.mDestinationValues[i] = new Dynamics(this.mSpeed, (float) stopSize);
                    this.mDestinationValues[i].setAtRest(true);
                }
            }
        }

        this.mBlockPass = 0;

        for (this.mBlockPass = 0; this.mBlockPass < this.mBlockNumber; ++this.mBlockPass) {
            this.mLeft = this.mPaddingLeft + this.mBlockPass * this.mBlockWidth;
            this.mLeft = (int) ((float) this.mLeft + this.mBlockSpacing * (float) this.mBlockPass);
            this.mRight = this.mLeft + this.mBlockWidth;
            if (this.mDestinationValues[this.mBlockPass] == null) {
                this.pickNewDynamics(this.mContentHeight, (float) this.mContentHeight * this.mBlockValues[this.mBlockPass][this.mDrawPass]);
            }

            if (this.mDestinationValues[this.mBlockPass].isAtRest() && this.mState == 2) {
                this.changeDynamicsTarget(this.mBlockPass, (float) this.mContentHeight * this.mBlockValues[this.mBlockPass][this.mDrawPass]);
            } else if (this.mState != 0) {
                this.mDestinationValues[this.mBlockPass].update();
            }

            this.mTop = this.mPaddingTop + (int) this.mDestinationValues[this.mBlockPass].getPosition();
            canvas.drawRect((float) this.mLeft, (float) this.mTop, (float) this.mRight, (float) this.mContentHeight, this.mPaint);
        }

        this.postInvalidateDelayed(16L);
    }

    private void updateRandomValues() {
        for (int i = 0; i < this.mBlockNumber; ++i) {
            for (int j = 0; j < 10; ++j) {
                this.mBlockValues[i][j] = this.mRandom.nextFloat();
                if ((double) this.mBlockValues[i][j] < 0.1D) {
                    this.mBlockValues[i][j] = 0.1F;
                }
            }
        }

    }

    private void pickNewDynamics(int max, float position) {
        this.mDestinationValues[this.mBlockPass] = new Dynamics(this.mSpeed, position);
        this.incrementAndGetDrawPass();
        this.mDestinationValues[this.mBlockPass].setTargetPosition((float) max * this.mBlockValues[this.mBlockPass][this.mDrawPass]);
    }

    private void changeDynamicsTarget(int block, float target) {
        this.incrementAndGetDrawPass();
        this.mDestinationValues[block].setTargetPosition(target);
    }

    private int incrementAndGetDrawPass() {
        ++this.mDrawPass;
        if (this.mDrawPass >= 10) {
            this.mDrawPass = 0;
        }

        return this.mDrawPass;
    }

    private void initialiseCollections() {
        this.mBlockValues = new float[this.mBlockNumber][10];
        this.mDestinationValues = new Dynamics[this.mBlockNumber];
        this.updateRandomValues();
    }

    public int getColor() {
        return this.mColor;
    }

    public void setColor(int color, int color1, int color2) {
        this.mColor = color;
        this.mColor1 = color1;
        this.mColor2 = color2;
//        this.mPaint.setColor(this.mColor);
        this.mPaint.setShader(new LinearGradient(0, 0, 0, getHeight(), new int[]{mColor, mColor1, mColor2}, new float[]{0.3f, 0.6f, 1f}, Shader.TileMode.MIRROR));
    }

    public int getBlockNumber() {
        return this.mBlockNumber;
    }

    public void setBlockNumber(int blockNumber) {
        this.mBlockNumber = blockNumber;
        this.initialiseCollections();
        this.mBlockPass = 0;
        this.mBlockWidth = 0;
    }

    public float getBlockSpacing() {
        return this.mBlockSpacing;
    }

    public void setBlockSpacing(float blockSpacing) {
        this.mBlockSpacing = blockSpacing;
        this.mBlockWidth = 0;
    }

    public int getSpeed() {
        return this.mSpeed;
    }

    public void setSpeed(int speed) {
        this.mSpeed = speed;
    }

    public void pause() {
        this.mState = 0;
    }

    public void stop(boolean withAnimation) {
        if (this.mDestinationValues == null) {
            this.initialiseCollections();
        }

        this.mState = 1;
        int collapseSize = (int) ((float) this.mContentHeight - this.mStopSize);
        if (this.mDestinationValues.length > 0) {
            for (int i = 0; i < this.mBlockNumber; ++i) {
                if (this.mDestinationValues[i] != null) {
                    if (withAnimation) {
                        this.mDestinationValues[i].setTargetPosition((float) collapseSize);
                    } else {
                        this.mDestinationValues[i].setPosition((float) collapseSize);
                    }
                }
            }

        }
    }

    public void resume(boolean withAnimation) {
        if (this.mState == 0) {
            this.mState = 2;
        } else {
            this.mState = 2;
            if (!withAnimation) {
                for (int i = 0; i < this.mBlockNumber; ++i) {
                    this.mDestinationValues[i].setPosition((float) this.mContentHeight * this.mBlockValues[i][this.mDrawPass]);
                    this.changeDynamicsTarget(i, (float) this.mContentHeight * this.mBlockValues[i][this.mDrawPass]);
                }
            }

        }
    }
}
