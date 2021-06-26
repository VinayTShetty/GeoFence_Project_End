
package com.succorfish.depthntemp.views;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;
import com.succorfish.depthntemp.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Custom implementation of the MarkerView.
 *
 */
public class MyMarkerView extends MarkerView {

    private TextView tvContent;
    private long referenceTimestamp;
    private DateFormat mDataFormat;
    private Date mDate;

    public MyMarkerView(Context context, int layoutResource, long referenceTimestamp) {
        super(context, layoutResource);
        this.referenceTimestamp = referenceTimestamp;
        tvContent = findViewById(R.id.tvContent);
        this.mDataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        this.mDate = new Date();

    }

    // callbacks every time the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {

        if (e instanceof CandleEntry) {

            CandleEntry ce = (CandleEntry) e;
            long currentTimestamp = (int) ce.getLow() + referenceTimestamp;
            tvContent.setText("" + getTimeDate(currentTimestamp) + " , " + ce.getHigh());
        } else {
            long currentTimestamp = (int) e.getX() + referenceTimestamp;
            tvContent.setText("" + getTimeDate(currentTimestamp) + " , " + e.getY());
//            tvContent.setText("" + Utils.formatNumber(e.getY(), 0, true));
//
        }

        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }

    private String getTimeDate(long timestamp) {
        try {
            mDate.setTime(timestamp * 1000);
            return mDataFormat.format(mDate);
        } catch (Exception ex) {
            return "xx";
        }
    }

}
