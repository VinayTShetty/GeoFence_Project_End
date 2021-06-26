package com.succorfish.depthntemp.vo;

import java.io.Serializable;

/*Heat map getter setter*/
public class VoHeatMap implements Serializable {

    String heatName = "";
    int color;
    int minValue;
    int maxValue;
    int minValueFahrenheit;
    int maxValueFahrenheit;
    boolean isSelectable = true;

    public String getHeatName() {
        return heatName;
    }

    public void setHeatName(String heatName) {
        this.heatName = heatName;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getMinValue() {
        return minValue;
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }

    public int getMinValueFahrenheit() {
        return minValueFahrenheit;
    }

    public void setMinValueFahrenheit(int minValueFahrenheit) {
        this.minValueFahrenheit = minValueFahrenheit;
    }

    public int getMaxValueFahrenheit() {
        return maxValueFahrenheit;
    }

    public void setMaxValueFahrenheit(int maxValueFahrenheit) {
        this.maxValueFahrenheit = maxValueFahrenheit;
    }

    public boolean getIsSelectable() {
        return isSelectable;
    }

    public void setIsSelectable(boolean selectable) {
        isSelectable = selectable;
    }
}
