package com.vithamastech.smartlight.Vo;

import java.io.Serializable;

/* Alarm Days Getter Setter*/
public class VoAlarmDays implements Serializable {

    String days_name = "";
    boolean is_day_checked = false;

    public String getDays_name() {
        return days_name;
    }

    public void setDays_name(String days_name) {
        this.days_name = days_name;
    }

    public boolean getIsIs_day_checked() {
        return is_day_checked;
    }

    public void setIs_day_checked(boolean is_day_checked) {
        this.is_day_checked = is_day_checked;
    }
}
