package com.vithamastech.smartlight.PowerSocketUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by muataz medini on 7/22/2016.
 */
public class DateUtils {

    public static long getUnixTimeStamp(String str_date) {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        long retVal = 0;

        try {
            date = (Date) formatter.parse(str_date);
            retVal = date.getTime() / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return retVal;
    }

    public static String getStringFromUnixTimeStamp(long timeInSeconds) {
        Date date = new Date(timeInSeconds * 1000);
        String retVal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        return retVal;
    }

    public static String getStringFromUnixTimeStampMthName(long timeInSeconds) {
        Date date = new Date(timeInSeconds * 1000);
        String retVal = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss").format(date);
        return retVal;
    }

    public static String getTimeandDateSecs(int number_of_hours, String reset_what, String preferredDateFormat) {
        DateFormat dateFormat = new SimpleDateFormat(preferredDateFormat, Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, number_of_hours);

        switch (reset_what) {
            case "hour":
                cal.set(Calendar.HOUR_OF_DAY, 0);
            case "minute":
                cal.set(Calendar.MINUTE, 0);
            case "second":
                cal.set(Calendar.SECOND, 0);
                break;
            case "eod":
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
            default:
        }
        return dateFormat.format(cal.getTime());
    }

    public static String getFormattedToday(String inp_date_format) {
        DateFormat dateFormat = new SimpleDateFormat(inp_date_format);
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
    }

    public static String addDate(String input_date, int noOfDays) {
        DateFormat dateFormat = new SimpleDateFormat(input_date, Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, noOfDays);
        return dateFormat.format(cal.getTime());
    }

    public static String addSeconds(String timeStamp, int seconds, String dateFormat) {
        DateFormat df = new SimpleDateFormat(dateFormat, Locale.getDefault());
        Date date = null;
        try {
            date = df.parse(timeStamp);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Calendar cal = Calendar.getInstance();
        if (date != null) {
            cal.setTime(date);
            cal.add(Calendar.SECOND, seconds);
            return df.format(cal.getTime());
        }
        return null;
    }

    public static String addSeconds(long epoch, int seconds, String dateFormat) {
        DateFormat df = new SimpleDateFormat(dateFormat, Locale.getDefault());
        Date date = new Date(epoch);
        Calendar cal = Calendar.getInstance();

        cal.setTime(date);
        cal.add(Calendar.SECOND, seconds);
        return df.format(cal.getTime());
    }

    public static String addSecondsToCurrentDate(String dateFormat, int seconds) {
        DateFormat df = new SimpleDateFormat(dateFormat, Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, seconds);
        return df.format(cal.getTime());
    }

    public static String subtractDate(String input_date, int noOfDays) {
        DateFormat dateFormat = new SimpleDateFormat(input_date, Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -noOfDays);
        return dateFormat.format(cal.getTime());
    }

    public static String getDate(int number_of_hours) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, number_of_hours);

        System.out.println(dateFormat.format(cal.getTime()));
        return dateFormat.format(cal.getTime());
    }

//    public static String convertDateFromDateTime(String date_time) {
//        String str_date = date_time;
//        DateFormat formatter;
//        Date date = null;
//        formatter = new SimpleDateFormat("yyyy-MM-dd");
//        try {
//            date = formatter.parse(str_date);
//        } catch (ParseException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return formatter.format(date);
//    }

    public static Date convertDateFromDateTime(String date_time) {

        DateFormat formatter;
        formatter = new SimpleDateFormat("yyyy-MM-dd");
        String date_str = formatter.format(date_time);
        Date date = null;
        try {
            date = formatter.parse(date_str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String getStartOfCurrentMonth(String datePattern) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1);
        DateFormat df = new SimpleDateFormat(datePattern, Locale.getDefault());
        return df.format(c.getTime());
    }

    public static String getLastDayOfMonth(String input_date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();

        try {
            cal.setTime(dateFormat.parse(input_date));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Integer calc_day = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        cal.set(Calendar.DAY_OF_MONTH, calc_day);
//		console.log("getDayOfMonth", dateFormat.format(cal.getTime()));

        return dateFormat.format(cal.getTime());
    }

    public static Map<String, String> getMonths(int noOfMonths, String preferredTimeStamp, String sort) {
        // perform check for only 11 months
        Map<String, String> retVal = new LinkedHashMap<String, String>();
        Date referenceDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(referenceDate);

        DateFormat dfMonthName = new SimpleDateFormat("MMM", Locale.getDefault());
        DateFormat dfTimeStamp = new SimpleDateFormat(preferredTimeStamp, Locale.getDefault());

        switch (sort) {
            case "asc":
                // First put Last Month
                c.add(Calendar.MONTH, -noOfMonths + 1);
                retVal.put(dfMonthName.format(c.getTime()), dfTimeStamp.format(c.getTime()));
                for (int i = 0; i < noOfMonths - 1; i++) {
                    c.add(Calendar.MONTH, 1);
                    retVal.put(dfMonthName.format(c.getTime()), dfTimeStamp.format(c.getTime()));
                }
                break;
            case "desc":
            default:
                // First put Current Month
                c.add(Calendar.MONTH, 0);
                retVal.put(dfMonthName.format(c.getTime()), dfTimeStamp.format(c.getTime()));
                for (int i = 0; i < noOfMonths - 1; i++) {
                    c.add(Calendar.MONTH, -1);
                    retVal.put(dfMonthName.format(c.getTime()), dfTimeStamp.format(c.getTime()));
                }
                break;
        }
        return retVal;
    }

    public static String getDatefromMonthNumber(String dateFormatStr, int monthIndex) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MONTH, monthIndex - 1);
        Date date = c.getTime();
        DateFormat df = new SimpleDateFormat(dateFormatStr, Locale.getDefault());
        return df.format(date);
    }

    public static String getFirstDayOfMonth(String dateFormatStr, String inputTimeStamp) {
        DateFormat df;
        Calendar c = Calendar.getInstance();
        df = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        Date date = new Date();
        try {
            date = df.parse(inputTimeStamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        df = new SimpleDateFormat("M", Locale.getDefault());
        c.set(Calendar.MONTH, Integer.parseInt(df.format(date)) - 1);
        c.set(Calendar.DAY_OF_MONTH, 1);
        df = new SimpleDateFormat("yyyy", Locale.getDefault());
        c.set(Calendar.YEAR, Integer.parseInt(df.format(date)));
        df = new SimpleDateFormat(dateFormatStr, Locale.getDefault());
        return df.format(c.getTime());
    }

    public static String getLastDayOfMonth(String dateFormatStr, String inputTimeStamp) {
        DateFormat df;
        Calendar c = Calendar.getInstance();
        df = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        Date date = new Date();
        try {
            date = df.parse(inputTimeStamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        df = new SimpleDateFormat("M", Locale.getDefault());
        c.set(Calendar.MONTH, Integer.parseInt(df.format(date)) - 1);

        int lastDay = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        c.set(Calendar.DAY_OF_MONTH, lastDay);

        df = new SimpleDateFormat("yyyy", Locale.getDefault());
        c.set(Calendar.YEAR, Integer.parseInt(df.format(date)));

        df = new SimpleDateFormat(dateFormatStr, Locale.getDefault());
        return df.format(c.getTime());
    }

    public static String getDateFromEpoch(long epoch, String dateFormatStr) {
        if (dateFormatStr == null || dateFormatStr.isEmpty()) {
            return null;
        }
        DateFormat dateFormat = new SimpleDateFormat(dateFormatStr, Locale.getDefault());
        Date date = new Date(epoch);
        return dateFormat.format(date);
    }
}