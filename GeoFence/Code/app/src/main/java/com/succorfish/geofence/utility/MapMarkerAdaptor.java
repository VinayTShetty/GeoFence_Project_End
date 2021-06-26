package com.succorfish.geofence.utility;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.succorfish.geofence.R;

public class MapMarkerAdaptor implements GoogleMap.InfoWindowAdapter {

    private Context context;
    private String breachRuleName;
    private String messgeOne;
    private String mesageTwo;

    public MapMarkerAdaptor(Context loc_context,String loc_breachRule,String loc_messageOne,String loc_messageTwo){
    this.context=loc_context;
    this.breachRuleName=loc_breachRule;
    this.messgeOne=loc_messageOne;
    this.mesageTwo=loc_messageTwo;
    }


    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View markerinfoView=inflater.inflate(R.layout.fence_info_window, null);
        TextView name=(TextView)markerinfoView.findViewById(R.id.name);
        TextView details=(TextView)markerinfoView.findViewById(R.id.details);
        name.setText("HeaderRule : "+breachRuleName+"\n"+" Message One: "+messgeOne+"\n"+" Message Two : "+mesageTwo);
        return  markerinfoView;
    }
}
