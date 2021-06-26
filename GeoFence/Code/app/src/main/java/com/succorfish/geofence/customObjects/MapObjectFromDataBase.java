package com.succorfish.geofence.customObjects;

public class MapObjectFromDataBase {
    private String geoFenceType;
    private double circular_fence_lat;
    private double circular_fence_long;
    private double circular_fence_radius_vertices;
    private String breach_message_one;
    private String breach_message_two;
    private String alias_Name;
    private double breach_latitude;
    private double breach_longitude;
    private String geoFence_Id;
    private String rule_Name;


    public String getGeoFenceType() {
        return geoFenceType;
    }

    public void setGeoFenceType(String geoFenceType) {
        this.geoFenceType = geoFenceType;
    }

    public double getCircular_fence_lat() {
        return circular_fence_lat;
    }

    public void setCircular_fence_lat(double circular_fence_lat) {
        this.circular_fence_lat = circular_fence_lat;
    }

    public double getCircular_fence_long() {
        return circular_fence_long;
    }

    public void setCircular_fence_long(double circular_fence_long) {
        this.circular_fence_long = circular_fence_long;
    }

    public double getCircular_fence_radius_vertices() {
        return circular_fence_radius_vertices;
    }

    public void setCircular_fence_radius_vertices(double circular_fence_radius_vertices) {
        this.circular_fence_radius_vertices = circular_fence_radius_vertices;
    }

    public String getBreach_message_one() {
        return breach_message_one;
    }

    public void setBreach_message_one(String breach_message_one) {
        this.breach_message_one = breach_message_one;
    }

    public String getBreach_message_two() {
        return breach_message_two;
    }

    public void setBreach_message_two(String breach_message_two) {
        this.breach_message_two = breach_message_two;
    }

    public String getAlias_Name() {
        return alias_Name;
    }

    public void setAlias_Name(String alias_Name) {
        this.alias_Name = alias_Name;
    }

    public double getBreach_latitude() {
        return breach_latitude;
    }

    public void setBreach_latitude(double breach_latitude) {
        this.breach_latitude = breach_latitude;
    }

    public double getBreach_longitude() {
        return breach_longitude;
    }

    public void setBreach_longitude(double breach_longitude) {
        this.breach_longitude = breach_longitude;
    }
    public String getGeoFence_Id() {
        return geoFence_Id;
    }

    public void setGeoFence_Id(String geoFence_Id) {
        this.geoFence_Id = geoFence_Id;
    }

    public String getRule_Name() {
        return rule_Name;
    }

    public void setRule_Name(String rule_Name) {
        this.rule_Name = rule_Name;
    }

}
