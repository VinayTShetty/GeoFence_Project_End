package com.succorfish.geofence.customA2_object;

import java.io.Serializable;

public class LatLong implements Serializable {
    double latitude;
    double longitude;

   public LatLong(double loc_latitude,double loc_longitude){
        this.latitude=loc_latitude;
        this.longitude=loc_longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
