package com.succorfish.depthntemp.helper;

import java.util.Calendar;
import java.util.Date;

/*Date Utils*/
public class DateUtils {
    public static Date MAX_DATE;

    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameDay(cal1, cal2);
    }

    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        } else if (cal1.get(0) == cal2.get(0) && cal1.get(1) == cal2.get(1) && cal1.get(6) == cal2.get(6)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isToday(Date date) {
        return isSameDay(date, Calendar.getInstance().getTime());
    }

    public static boolean isToday(Calendar cal) {
        return isSameDay(cal, Calendar.getInstance());
    }

    public static boolean isBeforeDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isBeforeDay(cal1, cal2);
    }

    public static boolean isBeforeDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        } else if (cal1.get(0) < cal2.get(0)) {
            return true;
        } else {
            if (cal1.get(0) > cal2.get(0)) {
                return false;
            }
            if (cal1.get(1) < cal2.get(1)) {
                return true;
            }
            if (cal1.get(1) > cal2.get(1)) {
                return false;
            }
            if (cal1.get(6) >= cal2.get(6)) {
                return false;
            }
            return true;
        }
    }

    public static boolean isAfterDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isAfterDay(cal1, cal2);
    }

    public static boolean isAfterDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        } else if (cal1.get(0) < cal2.get(0)) {
            return false;
        } else {
            if (cal1.get(0) > cal2.get(0)) {
                return true;
            }
            if (cal1.get(1) < cal2.get(1)) {
                return false;
            }
            if (cal1.get(1) > cal2.get(1)) {
                return true;
            }
            if (cal1.get(6) > cal2.get(6)) {
                return true;
            }
            return false;
        }
    }

    public static boolean isWithinDaysFuture(Date date, int days) {
        if (date == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return isWithinDaysFuture(cal, days);
    }

    public static boolean isWithinDaysFuture(Calendar cal, int days) {
        if (cal == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        Calendar today = Calendar.getInstance();
        Calendar future = Calendar.getInstance();
        future.add(6, days);
        return isAfterDay(cal, today) && !isAfterDay(cal, future);
    }

    public static Date getStart(Date date) {
        return clearTime(date);
    }

    public static Date clearTime(Date date) {
        if (date == null) {
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(11, 0);
        c.set(12, 0);
        c.set(13, 0);
        c.set(14, 0);
        return c.getTime();
    }

    public static boolean hasTime(Date date) {
        if (date == null) {
            return false;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        if (c.get(11) > 0) {
            return true;
        }
        if (c.get(12) > 0) {
            return true;
        }
        if (c.get(13) > 0) {
            return true;
        }
        if (c.get(14) > 0) {
            return true;
        }
        return false;
    }

    public static Date getEnd(Date date) {
        if (date == null) {
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(11, 23);
        c.set(12, 59);
        c.set(13, 59);
        c.set(14, 999);
        return c.getTime();
    }

    public static Date max(Date d1, Date d2) {
        if (d1 == null && d2 == null) {
            return null;
        }
        if (d1 == null) {
            return d2;
        }
        return (d2 == null || d1.after(d2)) ? d1 : d2;
    }

    public static Date min(Date d1, Date d2) {
        if (d1 == null && d2 == null) {
            return null;
        }
        if (d1 == null) {
            return d2;
        }
        return (d2 == null || d1.before(d2)) ? d1 : d2;
    }

    static {
        MAX_DATE = new Date(Long.MAX_VALUE);
    }
}
