package com.succorfish.geofence.customA2_object;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.succorfish.geofence.utility.Utility.removePreviousZero;

public class GeoFenceObjectData implements Serializable {

    /**
     *  1st Packet parameter:-
     *  Geo Id,
     *  Geo Size
     *  GeoFence Type
     *  Radius_Vertices
     *  TimeStamp
     */
    private int geoId;
    private int geosize;
    private String geoFenceType;
    private double radius_vertices;

    private String FirmwareTimeStamp;

    public int getGeoId() {
        return Integer.parseInt(removePreviousZero(""+geoId));
    }

    public void setGeoId(int geoId) {
        this.geoId =Integer.parseInt(removePreviousZero(""+geoId));
    }

    public int getGeosize() {
        return geosize;
    }

    public void setGeosize(int geosize) {
        this.geosize = geosize;
    }

    public String getGeoFenceType() {
        return geoFenceType;
    }

    public void setGeoFenceType(String geoFenceType) {
        this.geoFenceType = geoFenceType;
    }

    public double getRadius_vertices() {
        return radius_vertices;
    }

    public void setRadius_vertices(double radius_vertices) {
        this.radius_vertices = radius_vertices;
    }
    /**
     * 2 nd Packet Parameter:-
     * a)latitude
     * b)longitude.
     */
    private  List<LatLong> latLong;
    public List<LatLong> getLatLongInstance(){
      return   latLong=new ArrayList<LatLong>();
    }

    public List<LatLong> getLatLong() {
        return latLong;
    }

    public void setLatLong(List<LatLong> latLong) {
        this.latLong = latLong;
    }

    /**
     * 3 rd Packet Parameters
     * Rules
     */

    private int numberOfRules;
    private String gsm_ReportingTime;
    private String iridium_reportingTime;

    public String getGsm_ReportingTime() {
        return gsm_ReportingTime;
    }

    public void setGsm_ReportingTime(String gsm_ReportingTime) {
        this.gsm_ReportingTime = gsm_ReportingTime;
    }

    public String getIridium_reportingTime() {
        return iridium_reportingTime;
    }

    public void setIridium_reportingTime(String iridium_reportingTime) {
        this.iridium_reportingTime = iridium_reportingTime;
    }

    public int getNumberOfRules() {
        return numberOfRules;
    }
    public void setNumberOfRules(int numberOfRules) {
        this.numberOfRules = numberOfRules;
    }

    /**
     * 4 the Packet Parameter
     */

    List<RuleId_Value_ActionBitMask> ruleId_value_actionBitMasks;
    public List<RuleId_Value_ActionBitMask> getInstanceOfruleId_value_actionBitMasks(){
        return new ArrayList<RuleId_Value_ActionBitMask>();
    }

    public List<RuleId_Value_ActionBitMask> getRuleId_value_actionBitMasks() {
        return ruleId_value_actionBitMasks;
    }

    public void setRuleId_value_actionBitMasks(List<RuleId_Value_ActionBitMask> ruleId_value_actionBitMasks) {
        this.ruleId_value_actionBitMasks = ruleId_value_actionBitMasks;
    }

    public String getFirmwareTimeStamp() {
        return FirmwareTimeStamp;
    }

    public void setFirmwareTimeStamp(String firmwareTimeStamp) {
        FirmwareTimeStamp = firmwareTimeStamp;
    }

}
