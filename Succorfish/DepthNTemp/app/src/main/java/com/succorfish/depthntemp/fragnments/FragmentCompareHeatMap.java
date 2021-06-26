package com.succorfish.depthntemp.fragnments;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.succorfish.depthntemp.MainActivity;
import com.succorfish.depthntemp.MyApplication;
import com.succorfish.depthntemp.R;
import com.succorfish.depthntemp.db.TablePressureTemperature;
import com.succorfish.depthntemp.helper.HourAxisValueFormatter;
import com.succorfish.depthntemp.helper.PreferenceHelper;
import com.succorfish.depthntemp.views.MyMarkerView;
import com.succorfish.depthntemp.views.VerticalTextView;
import com.succorfish.depthntemp.vo.VoHeatMap;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FragmentCompareHeatMap extends Fragment implements OnChartGestureListener {
    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;
    LineChart mChartTemperature;
    @BindView(R.id.frg_heat_map_tv_temperature)
    VerticalTextView mVerticalTextViewTemperature;
    @BindView(R.id.frg_heat_map_tv_very_high_temp_range)
    TextView mTvTempRangeVeryHigh;
    @BindView(R.id.frg_heat_map_tv_high_temp_range)
    TextView mTvTempRangeHigh;
    @BindView(R.id.frg_heat_map_tv_medium_temp_range)
    TextView mTvTempRangeMedium;
    @BindView(R.id.frg_heat_map_tv_low_temp_range)
    TextView mTvTempRangeLow;
    @BindView(R.id.frg_heat_map_tv_very_low_temp_range)
    TextView mTvTempRangeVeryLow;

    boolean isSingleDataSelect = true;
    boolean isDiveDataSelect = true;
    String mStrDeviceOneBleAdd = "";
    String mStrDeviceTwoBleAdd = "";
    String mStrDiveOneId = "0";
    String mStrDiveTwoId = "0";
    String mStrStartDateTime = "";
    String mStrEndDateTime = "";


    Calendar mCalendarLocalStartTime;
    Calendar mCalendarLocalStartTimeTo;
    Calendar mCalendarLocalEndTimeFrom;
    Calendar mCalendarLocalEndTimeTo;
    List<TablePressureTemperature> mArrayListPresTemp = new ArrayList<>();
    List<TablePressureTemperature> mArrayListPresTempDive2 = new ArrayList<>();
    int mIntStationaryInterval = 1;
    int mIntStationaryIntervalDive2 = 1;
    ArrayList<VoHeatMap> mArrayListHeatMap = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        if (getArguments() != null) {
            isSingleDataSelect = getArguments().getBoolean("mBundle_Is_Single_Data");
            isDiveDataSelect = getArguments().getBoolean("mBundle_Is_Dive_Data");
            mStrDeviceOneBleAdd = getArguments().getString("mBundle_device_one_ble");
            mStrDeviceTwoBleAdd = getArguments().getString("mBundle_device_two_ble");
            mStrDiveOneId = getArguments().getString("mBundle_dive_one_id", "0");
            mStrDiveTwoId = getArguments().getString("mBundle_dive_two_id", "0");
            mStrStartDateTime = getArguments().getString("mBundle_start_date_time");
            mStrEndDateTime = getArguments().getString("mBundle_end_date_time");

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_compare_heat_map, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mChartTemperature = (LineChart) mViewRoot.findViewById(R.id.frg_heat_map_line_chart_heat_map);
        mCalendarLocalStartTime = Calendar.getInstance();
        mCalendarLocalStartTimeTo = Calendar.getInstance();
        mCalendarLocalEndTimeFrom = Calendar.getInstance();
        mCalendarLocalEndTimeTo = Calendar.getInstance();
        mArrayListPresTemp = new ArrayList<>();
        mArrayListHeatMap = PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getAllHeatMapSettingData();
        if (PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedTemperatureType() == 0) {
            mVerticalTextViewTemperature.setText("Temperature in Celsius");
        } else {
            mVerticalTextViewTemperature.setText("Temperature in Fahrenheit");
        }

        new GetDbPressTempDataList().execute();

        for (int i = 0; i < mArrayListHeatMap.size(); i++) {
//            System.out.println("Min-" + mArrayListHeatMap.get(i).getMinValue());
//            System.out.println("Max-" + mArrayListHeatMap.get(i).getMaxValue());
//            System.out.println("Min-F-" + mArrayListHeatMap.get(i).getMinValueFahrenheit());
//            System.out.println("Max-F-" + mArrayListHeatMap.get(i).getMaxValueFahrenheit());
            if (i == 0) {   // Very High Temperature
                if (PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedTemperatureType() == 0) {
                    mTvTempRangeVeryHigh.setText(mArrayListHeatMap.get(i).getMinValue() + " °C to " + mArrayListHeatMap.get(i).getMaxValue() + " °C");
                } else {
                    mTvTempRangeVeryHigh.setText(mArrayListHeatMap.get(i).getMinValueFahrenheit() + " °F to " + mArrayListHeatMap.get(i).getMaxValueFahrenheit() + " °F");
                }
            } else if (i == 1) {    // High Temperature
                if (PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedTemperatureType() == 0) {
                    mTvTempRangeHigh.setText(mArrayListHeatMap.get(i).getMinValue() + " °C to " + mArrayListHeatMap.get(i).getMaxValue() + " °C");
                } else {
                    mTvTempRangeHigh.setText(mArrayListHeatMap.get(i).getMinValueFahrenheit() + " °F to " + mArrayListHeatMap.get(i).getMaxValueFahrenheit() + " °F");
                }
            } else if (i == 2) {    // Medium Temperature
                if (PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedTemperatureType() == 0) {
                    mTvTempRangeMedium.setText(mArrayListHeatMap.get(i).getMinValue() + " °C to " + mArrayListHeatMap.get(i).getMaxValue() + " °C");
                } else {
                    mTvTempRangeMedium.setText(mArrayListHeatMap.get(i).getMinValueFahrenheit() + " °F to " + mArrayListHeatMap.get(i).getMaxValueFahrenheit() + " °F");
                }
            } else if (i == 3) {    // Low Temperature
                if (PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedTemperatureType() == 0) {
                    mTvTempRangeLow.setText(mArrayListHeatMap.get(i).getMinValue() + " °C to " + mArrayListHeatMap.get(i).getMaxValue() + " °C");
                } else {
                    mTvTempRangeLow.setText(mArrayListHeatMap.get(i).getMinValueFahrenheit() + " °F to " + mArrayListHeatMap.get(i).getMaxValueFahrenheit() + " °F");
                }
            } else if (i == 4) {    // Very Low Temperature
                if (PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedTemperatureType() == 0) {
                    mTvTempRangeVeryLow.setText(mArrayListHeatMap.get(i).getMinValue() + " °C to " + mArrayListHeatMap.get(i).getMaxValue() + " °C");
                } else {
                    mTvTempRangeVeryLow.setText(mArrayListHeatMap.get(i).getMinValueFahrenheit() + " °F to " + mArrayListHeatMap.get(i).getMaxValueFahrenheit() + " °F");
                }
            }
        }
        return mViewRoot;
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartLongPressed(MotionEvent me) {

    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {

    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {

    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {

    }

    /*get db pressure temperature data*/
    private class GetDbPressTempDataList extends AsyncTask<String, Integer, List<TablePressureTemperature>> {

        @Override
        protected void onPreExecute() {
            mActivity.showProgress("Please wait..", false);
            super.onPreExecute();
        }

        @Override
        protected List<TablePressureTemperature> doInBackground(String... params) {
//            List<TablePressureTemperature> mTablePressureTemperatures = new ArrayList<>();
//            List<TablePressureTemperature> mTablePressureTemperaturesDive2 = new ArrayList<>();
            if (isSingleDataSelect) {
                if (isDiveDataSelect) {
                    mArrayListPresTemp = mActivity.mAppRoomDatabase.tempPressDao().getAllPressTempDataBySingleDive(Integer.parseInt(mStrDiveOneId));
                    mIntStationaryInterval = mActivity.mAppRoomDatabase.diveDao().getStationaryInterval(Integer.parseInt(mStrDiveOneId));
                } else {
                    mCalendarLocalStartTime.setTimeInMillis(Long.parseLong(mStrStartDateTime));
                    mCalendarLocalStartTimeTo.setTimeInMillis(Long.parseLong(mStrStartDateTime));
                    mCalendarLocalStartTimeTo.set(Calendar.HOUR_OF_DAY, 23);
                    mCalendarLocalStartTimeTo.set(Calendar.MINUTE, 59);
                    mCalendarLocalStartTimeTo.set(Calendar.SECOND, 59);
                    mCalendarLocalStartTimeTo.set(Calendar.MILLISECOND, 999);
                    System.out.println("mCalendarLocalStartTime-" + mCalendarLocalStartTime.getTimeInMillis());
                    System.out.println("mCalendarLocalStartTimeTo-" + mCalendarLocalStartTimeTo.getTimeInMillis());
//                    mCalendarUtcStartTime.setTimeInMillis(mCalendarLocalStartTime.getTimeInMillis());
//                    mCalendarUtcStartTimeTo.setTimeInMillis(mCalendarLocalStartTimeTo.getTimeInMillis());
                    System.out.println("mCalendarLocalStartTime-" + localToUTC(mCalendarLocalStartTime.getTimeInMillis()));
                    System.out.println("mCalendarLocalStartTimeTo-" + localToUTC(mCalendarLocalStartTimeTo.getTimeInMillis()));
                    try {
                        mArrayListPresTemp = mActivity.mAppRoomDatabase.tempPressDao().getAllPressTempDataBySingleDate(mCalendarLocalStartTime.getTimeInMillis(), mCalendarLocalStartTimeTo.getTimeInMillis());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                if (isDiveDataSelect) {
                    mArrayListPresTemp = mActivity.mAppRoomDatabase.tempPressDao().getAllPressTempDataBySingleDive(Integer.parseInt(mStrDiveOneId));
                    mArrayListPresTempDive2 = mActivity.mAppRoomDatabase.tempPressDao().getAllPressTempDataBySingleDive(Integer.parseInt(mStrDiveTwoId));
//                    mTablePressureTemperatures = mActivity.mAppRoomDatabase.tempPressDao().getAllPressTempDataByMultiDive(Integer.parseInt(mStrDiveOneId), Integer.parseInt(mStrDiveTwoId));
                    mIntStationaryInterval = mActivity.mAppRoomDatabase.diveDao().getStationaryInterval(Integer.parseInt(mStrDiveOneId));
                    mIntStationaryIntervalDive2 = mActivity.mAppRoomDatabase.diveDao().getStationaryInterval(Integer.parseInt(mStrDiveTwoId));

                } else {
                    mCalendarLocalStartTime.setTimeInMillis(Long.parseLong(mStrStartDateTime));
                    mCalendarLocalStartTimeTo.setTimeInMillis(Long.parseLong(mStrStartDateTime));
                    mCalendarLocalStartTimeTo.set(Calendar.HOUR_OF_DAY, 23);
                    mCalendarLocalStartTimeTo.set(Calendar.MINUTE, 59);
                    mCalendarLocalStartTimeTo.set(Calendar.SECOND, 59);
                    mCalendarLocalStartTimeTo.set(Calendar.MILLISECOND, 999);

                    mCalendarLocalEndTimeFrom.setTimeInMillis(Long.parseLong(mStrEndDateTime));
                    mCalendarLocalEndTimeTo.setTimeInMillis(Long.parseLong(mStrEndDateTime));
                    mCalendarLocalEndTimeTo.set(Calendar.HOUR_OF_DAY, 23);
                    mCalendarLocalEndTimeTo.set(Calendar.MINUTE, 59);
                    mCalendarLocalEndTimeTo.set(Calendar.SECOND, 59);
                    mCalendarLocalEndTimeTo.set(Calendar.MILLISECOND, 999);

                    System.out.println("mCalendarLocalStartTime-" + mCalendarLocalStartTime.getTimeInMillis());
                    System.out.println("mCalendarLocalStartTimeTo-" + mCalendarLocalStartTimeTo.getTimeInMillis());
//                    mCalendarUtcStartTime.setTimeInMillis(mCalendarLocalStartTime.getTimeInMillis());
//                    mCalendarUtcStartTimeTo.setTimeInMillis(mCalendarLocalStartTimeTo.getTimeInMillis());
                    System.out.println("mCalendarLocalStartTime-" + localToUTC(mCalendarLocalStartTime.getTimeInMillis()));
                    System.out.println("mCalendarLocalStartTimeTo-" + localToUTC(mCalendarLocalStartTimeTo.getTimeInMillis()));

                    mArrayListPresTemp = mActivity.mAppRoomDatabase.tempPressDao().getAllPressTempDataBySingleDate(mCalendarLocalStartTime.getTimeInMillis(), mCalendarLocalStartTimeTo.getTimeInMillis());
                    mArrayListPresTemp.addAll(mActivity.mAppRoomDatabase.tempPressDao().getAllPressTempDataBySingleDate(mCalendarLocalEndTimeFrom.getTimeInMillis(), mCalendarLocalEndTimeTo.getTimeInMillis()));
                }
            }

            if (mArrayListPresTemp == null) {
                mArrayListPresTemp = new ArrayList<>();
            }
            if (mArrayListPresTempDive2 == null) {
                mArrayListPresTempDive2 = new ArrayList<>();
            }
//            mArrayListPresTemp = mTablePressureTemperatures;
//            mArrayListPresTempDive2 = mTablePressureTemperaturesDive2;
            return mArrayListPresTemp;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(List<TablePressureTemperature> mPressureTemperatureList) {
            super.onPostExecute(mPressureTemperatureList);
            initTemperatureData();

        }
    }

    public static long localToUTC(long time) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date(time);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            String strDate = dateFormat.format(date);
//            System.out.println("Local Millis * " + date.getTime() + "  ---UTC time  " + strDate);//correct
            SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date utcDate = dateFormatLocal.parse(strDate);
//            System.out.println("UTC Millis * " + utcDate.getTime() + " ------  " + dateFormatLocal.format(utcDate));
            return utcDate.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return time;
    }

    /*Init temperature graph data*/
    private void initTemperatureData() {
        // no description text
        mChartTemperature.getDescription().setEnabled(false);
        mChartTemperature.setOnChartGestureListener(this);
        // enable touch gestures
        mChartTemperature.setTouchEnabled(true);
        mChartTemperature.setPinchZoom(true);
        mChartTemperature.setBorderColor(Color.WHITE);
        mChartTemperature.setDrawBorders(true);
        // enable scaling and dragging
        mChartTemperature.setDragEnabled(true);
        mChartTemperature.setScaleEnabled(true);
        mChartTemperature.setDrawGridBackground(false);
        mChartTemperature.setHighlightPerDragEnabled(false);

        // set an alternative background color
        mChartTemperature.setBackgroundColor(Color.TRANSPARENT);
//        MyMarkerView mv = new MyMarkerView(mActivity, R.layout.custom_marker_view);
//        mv.setChartView(mChartTemperature); // For bounds control
//        mChartTemperature.setMarker(mv);

        // add data
        setTemperatureData();
        mChartTemperature.setExtraOffsets(0, 0, 0, 0);
        mChartTemperature.getContentRect().set(0, 0, mChartTemperature.getWidth(), mChartTemperature.getHeight());


        XAxis xAxis = mChartTemperature.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setCenterAxisLabels(true);
        xAxis.setGranularity(1f); // one hour
//        xAxis.setValueFormatter(new IAxisValueFormatter() {
//
//            private SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm:ss");
//
//            @Override
//            public String getFormattedValue(float value, AxisBase axis) {
//
//                long millis = TimeUnit.HOURS.toMillis((long) value);
//                return mFormat.format(new Date(millis));
//            }
//        });

        YAxis leftAxis = mChartTemperature.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setTextColor(Color.WHITE);
//        leftAxis.setAxisMinimum(0f);
//        leftAxis.setAxisMaximum(170f);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setCenterAxisLabels(true);
        leftAxis.setYOffset(-9f);
        leftAxis.setTextColor(Color.WHITE);
//        leftAxis.setGranularity(1f);
//        leftAxis.setStartAtZero(true);
        YAxis rightAxis = mChartTemperature.getAxisRight();
        rightAxis.setEnabled(false);
        mChartTemperature.getAxisRight().setDrawGridLines(true);
        mChartTemperature.getAxisLeft().setDrawGridLines(true);
        mChartTemperature.getXAxis().setDrawGridLines(true);
        mChartTemperature.setNoDataText("");
        if (mArrayListPresTemp.size() < 10000 && mArrayListPresTempDive2.size() < 10000) {
            mChartTemperature.animateX(500);
        } else {
            if (mArrayListPresTemp.size() < 10000) {
                mChartTemperature.animateX(500);
            }
        }
        mChartTemperature.invalidate();
    }

    /* Set Temperature Data*/
    private void setTemperatureData() {

        ArrayList<Entry> values = new ArrayList<Entry>();
        ArrayList<Entry> valuesDive2 = new ArrayList<>();
        ArrayList<Integer> colorsPoints = new ArrayList<Integer>();
        ArrayList<Integer> colorsPointsDive2 = new ArrayList<Integer>();
        float mFloatTemperature;
        System.out.println("JD-mArrayListPresTemp-" + mArrayListPresTemp.size());
        System.out.println("JD-mIntStationaryInterval-" + mIntStationaryInterval);
        int i = 0;
        Entry mEntry;
        if (mArrayListPresTemp.size() > 0) {
            HourAxisValueFormatter xAxisFormatter = new HourAxisValueFormatter(mArrayListPresTemp.get(0).getUtcTime() / 1000);
            XAxis xAxis = mChartTemperature.getXAxis();
            xAxis.setValueFormatter(xAxisFormatter);

            MyMarkerView mv = new MyMarkerView(mActivity, R.layout.custom_marker_view, mArrayListPresTemp.get(0).getUtcTime() / 1000);
            mv.setChartView(mChartTemperature); // For bounds control
            mChartTemperature.setMarker(mv);

            mChartTemperature.invalidate();
            i = 0;
            if (PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedTemperatureType() == 0) {
                for (int x = 0; x < mArrayListPresTemp.size(); x++) {
                    try {
                        mFloatTemperature = Float.parseFloat(mArrayListPresTemp.get(x).getTemperature());
                        mFloatTemperature = Float.parseFloat(String.format("%.2f", mFloatTemperature));
                        System.out.println("JD-mFloatTemperatureC-" + mFloatTemperature);
                        mEntry = new Entry((float) i, mFloatTemperature);
                        values.add(mEntry);
                        i = i + mIntStationaryInterval;
                        if ((mFloatTemperature < mArrayListHeatMap.get(4).getMaxValue())) {
                            colorsPoints.add(this.getResources().getColor(R.color.temp_low));
                        } else if ((mFloatTemperature > mArrayListHeatMap.get(3).getMinValue()) && (mFloatTemperature <= mArrayListHeatMap.get(3).getMaxValue())) {
                            colorsPoints.add(this.getResources().getColor(R.color.temp_low_medium));
                        } else if ((mFloatTemperature > mArrayListHeatMap.get(2).getMinValue()) && (mFloatTemperature <= mArrayListHeatMap.get(2).getMaxValue())) {
                            colorsPoints.add(this.getResources().getColor(R.color.temp_medium));
                        } else if ((mFloatTemperature > mArrayListHeatMap.get(1).getMinValue()) && (mFloatTemperature <= mArrayListHeatMap.get(1).getMaxValue())) {
                            colorsPoints.add(this.getResources().getColor(R.color.temp_high_medium));
                        } else {
                            colorsPoints.add(this.getResources().getColor(R.color.temp_high));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                for (int x = 0; x < mArrayListPresTemp.size(); x++) {
                    try {
                        mFloatTemperature = Float.parseFloat(mArrayListPresTemp.get(x).getTemperature_far());
                        mFloatTemperature = Float.parseFloat(String.format("%.2f", mFloatTemperature));
//                        System.out.println("JD-mFloatTemperatureF-" + mFloatTemperature);
                        mEntry = new Entry((float) i, mFloatTemperature);
                        values.add(mEntry);
                        i = i + mIntStationaryInterval;
                        if ((mFloatTemperature < mArrayListHeatMap.get(4).getMaxValueFahrenheit())) {
                            colorsPoints.add(this.getResources().getColor(R.color.temp_low));
                        } else if ((mFloatTemperature > mArrayListHeatMap.get(3).getMinValueFahrenheit()) && (mFloatTemperature <= mArrayListHeatMap.get(3).getMaxValueFahrenheit())) {
                            colorsPoints.add(this.getResources().getColor(R.color.temp_low_medium));
                        } else if ((mFloatTemperature > mArrayListHeatMap.get(2).getMinValueFahrenheit()) && (mFloatTemperature <= mArrayListHeatMap.get(2).getMaxValueFahrenheit())) {
                            colorsPoints.add(this.getResources().getColor(R.color.temp_medium));
                        } else if ((mFloatTemperature > mArrayListHeatMap.get(1).getMinValueFahrenheit()) && (mFloatTemperature <= mArrayListHeatMap.get(1).getMaxValueFahrenheit())) {
                            colorsPoints.add(this.getResources().getColor(R.color.temp_high_medium));
                        } else {
                            colorsPoints.add(this.getResources().getColor(R.color.temp_high));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (!isSingleDataSelect && isDiveDataSelect) {
            if (mArrayListPresTempDive2.size() > 0) {
                if (mArrayListPresTemp.size() == 0) {
                    HourAxisValueFormatter xAxisFormatter = new HourAxisValueFormatter(mArrayListPresTempDive2.get(0).getUtcTime() / 1000);
                    XAxis xAxis = mChartTemperature.getXAxis();
                    xAxis.setValueFormatter(xAxisFormatter);
                    MyMarkerView mv = new MyMarkerView(mActivity, R.layout.custom_marker_view, mArrayListPresTempDive2.get(0).getUtcTime() / 1000);
                    mv.setChartView(mChartTemperature); // For bounds control
                    mChartTemperature.setMarker(mv);
                    mChartTemperature.invalidate();
                }
                i = 0;
                if (PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedTemperatureType() == 0) {
                    for (int x = 0; x < mArrayListPresTempDive2.size(); x++) {
                        try {
                            mFloatTemperature = Float.parseFloat(mArrayListPresTempDive2.get(x).getTemperature());
                            mFloatTemperature = Float.parseFloat(String.format("%.2f", mFloatTemperature));
                            System.out.println("JD-mFloatTemperatureC-" + mFloatTemperature);
                            mEntry = new Entry((float) i, mFloatTemperature);
                            valuesDive2.add(mEntry);
                            i = i + mIntStationaryIntervalDive2;
                            if ((mFloatTemperature < mArrayListHeatMap.get(4).getMaxValue())) {
                                colorsPointsDive2.add(this.getResources().getColor(R.color.temp_low));
                            } else if ((mFloatTemperature > mArrayListHeatMap.get(3).getMinValue()) && (mFloatTemperature <= mArrayListHeatMap.get(3).getMaxValue())) {
                                colorsPointsDive2.add(this.getResources().getColor(R.color.temp_low_medium));
                            } else if ((mFloatTemperature > mArrayListHeatMap.get(2).getMinValue()) && (mFloatTemperature <= mArrayListHeatMap.get(2).getMaxValue())) {
                                colorsPointsDive2.add(this.getResources().getColor(R.color.temp_medium));
                            } else if ((mFloatTemperature > mArrayListHeatMap.get(1).getMinValue()) && (mFloatTemperature <= mArrayListHeatMap.get(1).getMaxValue())) {
                                colorsPointsDive2.add(this.getResources().getColor(R.color.temp_high_medium));
                            } else {
                                colorsPointsDive2.add(this.getResources().getColor(R.color.temp_high));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    for (int x = 0; x < mArrayListPresTempDive2.size(); x++) {
                        try {
                            mFloatTemperature = Float.parseFloat(mArrayListPresTempDive2.get(x).getTemperature_far());
                            mFloatTemperature = Float.parseFloat(String.format("%.2f", mFloatTemperature));
//                            System.out.println("JD-mFloatTemperatureC-" + mFloatTemperature);
                            mEntry = new Entry((float) i, mFloatTemperature);
                            valuesDive2.add(mEntry);
                            i = i + mIntStationaryIntervalDive2;
                            if ((mFloatTemperature < mArrayListHeatMap.get(4).getMaxValueFahrenheit())) {
                                colorsPointsDive2.add(this.getResources().getColor(R.color.temp_low));
                            } else if ((mFloatTemperature > mArrayListHeatMap.get(3).getMinValueFahrenheit()) && (mFloatTemperature <= mArrayListHeatMap.get(3).getMaxValueFahrenheit())) {
                                colorsPointsDive2.add(this.getResources().getColor(R.color.temp_low_medium));
                            } else if ((mFloatTemperature > mArrayListHeatMap.get(2).getMinValueFahrenheit()) && (mFloatTemperature <= mArrayListHeatMap.get(2).getMaxValueFahrenheit())) {
                                colorsPointsDive2.add(this.getResources().getColor(R.color.temp_medium));
                            } else if ((mFloatTemperature > mArrayListHeatMap.get(1).getMinValueFahrenheit()) && (mFloatTemperature <= mArrayListHeatMap.get(1).getMaxValueFahrenheit())) {
                                colorsPointsDive2.add(this.getResources().getColor(R.color.temp_high_medium));
                            } else {
                                colorsPointsDive2.add(this.getResources().getColor(R.color.temp_high));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        if (!isSingleDataSelect && isDiveDataSelect) {
            LineDataSet set1, set2;
            if (mChartTemperature.getData() != null &&
                    mChartTemperature.getData().getDataSetCount() > 0) {
                set1 = (LineDataSet) mChartTemperature.getData().getDataSetByIndex(0);
                set2 = (LineDataSet) mChartTemperature.getData().getDataSetByIndex(1);
                set1.setValues(values);
                set2.setValues(valuesDive2);
                mChartTemperature.getData().notifyDataChanged();
                mChartTemperature.notifyDataSetChanged();
            } else {
                // create a data set and give it a type
                set1 = new LineDataSet(values, "Dive 1");

                set1.setAxisDependency(YAxis.AxisDependency.RIGHT);
                int[] colorsDive1 = new int[]{getResources().getColor(R.color.dive_one)};
                set1.setColors(colorsDive1);
                set1.setCircleColors(colorsPoints);
                set1.setValueTextColor(Color.WHITE);
                set1.setLineWidth(1.5f);
                set1.setDrawCircles(true);
                set1.setDrawValues(true);
                set1.setFillAlpha(65);
                set1.setFillColor(R.color.dive_one);
                set1.setHighLightColor(Color.WHITE);
                set1.setDrawCircleHole(false);
                set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);

                //set1.setFillFormatter(new MyFillFormatter(0f));
                //set1.setDrawHorizontalHighlightIndicator(false);
                //set1.setVisible(false);
                //set1.setCircleHoleColor(Color.WHITE);

                // create a data set and give it a type
                set2 = new LineDataSet(valuesDive2, "Dive 2");
                set2.setAxisDependency(YAxis.AxisDependency.RIGHT);
                int[] colorsDive2 = new int[]{getResources().getColor(R.color.dive_two)};
                set2.setColors(colorsDive2);
                set2.setCircleColors(colorsPointsDive2);
                set2.setValueTextColor(Color.WHITE);
                set2.setLineWidth(1.5f);
                set2.setDrawCircles(true);
                set2.setDrawValues(true);
                set2.setFillAlpha(80);
                set2.setFillColor(R.color.dive_two);
                set2.setHighLightColor(Color.WHITE);
                set2.setDrawCircleHole(false);
                set2.setMode(LineDataSet.Mode.CUBIC_BEZIER);

                //set2.setFillFormatter(new MyFillFormatter(900f));


                // create a data object with the data sets
                LineData data = new LineData(set1, set2);
                data.setValueTextColor(Color.WHITE);
                data.setValueTextSize(9f);

                // set data
                mChartTemperature.setData(data);
            }
        } else {
            // create a data set and give it a type
            LineDataSet set1 = new LineDataSet(values, "Dive 1");
            set1.setAxisDependency(YAxis.AxisDependency.RIGHT);
            int[] colors = new int[]{getResources().getColor(R.color.dive_one)};
            set1.setColors(colors);
            set1.setCircleColors(colorsPoints);
            set1.setValueTextColor(Color.WHITE);
            set1.setLineWidth(1f);
            set1.setDrawCircles(true);
            set1.setDrawValues(true);
            set1.setFillAlpha(80);
            set1.setFillColor(Color.YELLOW);
            set1.setHighLightColor(Color.WHITE);
            set1.setDrawCircleHole(false);
            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            // create a data object with the data sets
            LineData data = new LineData(set1);
            data.setValueTextColor(Color.WHITE);
            data.setValueTextSize(9f);

            // set data
            mChartTemperature.setData(data);
        }
        // get the legend (only possible after setting data)
        Legend l = mChartTemperature.getLegend();
        l.setYOffset(getResources().getDimension(R.dimen._10sdp));
        l.setForm(Legend.LegendForm.LINE);
        l.setTextSize(11f);
        l.setTextColor(Color.WHITE);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);

        mActivity.hideProgress();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        mArrayListAddDevice = null;
        mArrayListHeatMap.clear();
        mArrayListPresTemp.clear();
        mArrayListPresTempDive2.clear();
        mChartTemperature.clear();
        unbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

}
