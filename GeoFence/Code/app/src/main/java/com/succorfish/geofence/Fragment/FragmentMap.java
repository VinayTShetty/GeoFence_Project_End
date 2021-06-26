package com.succorfish.geofence.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.succorfish.geofence.BaseFragment.BaseFragment;
import com.succorfish.geofence.MainActivity;
import com.succorfish.geofence.R;
import com.succorfish.geofence.RoomDataBaseEntity.GeofenceAlert;
import com.succorfish.geofence.RoomDataBaseEntity.PolygonEnt;
import com.succorfish.geofence.customObjects.MapObjectFromDataBase;
import com.succorfish.geofence.dialog.DialogProvider;
import com.succorfish.geofence.interfaceActivityToFragment.GeoFenceDialogAlertShow;
import com.succorfish.geofence.interfaces.onAlertDialogCallBack;

import java.util.ArrayList;
import java.util.List;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.succorfish.geofence.MainActivity.roomDBHelperInstance;

public class FragmentMap extends BaseFragment {
    View fragmentMapView;
    private Unbinder unbinder;
    MainActivity mainActivity;
    MapView mMapView;
    MapObjectFromDataBase mapObjectFromDataBase;
    String SystemtimeStampFrom_DataBase="";
    List<LatLng> polygon_lat_long=new ArrayList<>();
    final Handler handler = new Handler();
    DialogProvider dialogProvider;
    private KProgressHUD hud;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentMapView = inflater.inflate(R.layout.fragment_map, container, false);
        unbinder = ButterKnife.bind(this, fragmentMapView);
        bottomLayoutVisibility(false);
        intializeView();
        Bundle bundle = this.getArguments();
        if (bundle != null && bundle.containsKey(new FragmentMap().toString())) {
            mapObjectFromDataBase=new MapObjectFromDataBase();
            SystemtimeStampFrom_DataBase  = bundle.getString(new FragmentMap().toString());
            loadFenceData_from_DB(SystemtimeStampFrom_DataBase);
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setUpMap(fragmentMapView, savedInstanceState);
        makeNOtificationTagRead();
        geoFenceAlertImplementation();
        showProgressDialog();
        return fragmentMapView;
    }

    private void bottomLayoutVisibility(boolean hide_true_unhide_false){
        mainActivity.hideBottomLayout(hide_true_unhide_false);
    }

    private void makeNOtificationTagRead() {
        if(!(SystemtimeStampFrom_DataBase.equalsIgnoreCase(""))){
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    //Update a column value of the Time Stamp as Read i.e
                    // 1-Read,//0- unread.
                    roomDBHelperInstance.get_GeoFenceAlert_info_dao().updateNotificationTagRead(SystemtimeStampFrom_DataBase);
                }
            });
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        mMapView.onDestroy();
        System.gc();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public String toString() {
        return FragmentMap.class.getSimpleName();
    }

    @OnClick(R.id.fragment_map_back)
    public void backImageClick() {
        mainActivity.onBackPressed();
    }

    private void showProgressDialog() {
        hud
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Loading")
                .setCancellable(false);
        hud.show();
    }

    private void cancelProgressDialog() {
        if (hud != null) {
            if (hud.isShowing()) {
                hud.dismiss();
            }
        }
    }



    private void setUpMap(View fragmentMapView, Bundle savedInstanceState) {
        mMapView = (MapView) fragmentMapView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                if(mapObjectFromDataBase.getGeoFenceType().equalsIgnoreCase("Circular")){
                    showCircularFence(mMap);
                }else if(mapObjectFromDataBase.getGeoFenceType().equalsIgnoreCase("Polygon")){
                    showPolyGonFence(mMap);
                }
            }
        });

    }
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int VectorResId) {
        Drawable VectorDrawable = ContextCompat.getDrawable(context, VectorResId);
        VectorDrawable.setBounds(0, 0, VectorDrawable.getIntrinsicWidth(), VectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(VectorDrawable.getIntrinsicWidth(), VectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canves = new Canvas(bitmap);
        VectorDrawable.draw(canves);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void showCircularFence(GoogleMap mMap) {
        LatLng location = new LatLng(mapObjectFromDataBase.getCircular_fence_lat(),mapObjectFromDataBase.getCircular_fence_long());
        Circle circle = mMap.addCircle(new CircleOptions()
                .center(new LatLng(mapObjectFromDataBase.getCircular_fence_lat(),mapObjectFromDataBase.getCircular_fence_long()))
                .radius(mapObjectFromDataBase.getCircular_fence_radius_vertices())
                .strokeColor(Color.BLACK).strokeWidth(3)
                .fillColor(R.color.colorAccent));
        MarkerOptions markerOptions = new MarkerOptions().position(location);
        mMap.addMarker(markerOptions.icon(bitmapDescriptorFromVector(getActivity(), R.drawable.circular_fence)));
        /**
         * Breach latitude and Longitude. Location Zoom.
         */
        LatLng breach_latitude_longitude = new LatLng(mapObjectFromDataBase.getBreach_latitude(),mapObjectFromDataBase.getBreach_longitude());
        MarkerOptions circular_lat_long = new MarkerOptions();
        circular_lat_long.position(breach_latitude_longitude);
        circular_lat_long.icon(bitmapDescriptorFromVector(getActivity(), R.drawable.map_custom_marker));
        Marker marker =    mMap.addMarker(circular_lat_long);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(breach_latitude_longitude, 19));
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isVisible()){
                    mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                        @Override
                        public View getInfoWindow(Marker marker) {
                            return null;
                        }

                        @Override
                        public View getInfoContents(Marker marker) {
                            View markerinfoView=getLayoutInflater().inflate(R.layout.fence_info_window, null);
                            TextView name=(TextView)markerinfoView.findViewById(R.id.name);
                            TextView details=(TextView)markerinfoView.findViewById(R.id.details);
                            name.setText(mapObjectFromDataBase.getRule_Name()+"\n"+mapObjectFromDataBase.getAlias_Name()+"\n"+mapObjectFromDataBase.getBreach_message_one()+"\n"+mapObjectFromDataBase.getBreach_message_two());
                            cancelProgressDialog();
                            return  markerinfoView;
                        }
                    });
                    marker.showInfoWindow();
                }
            }
        },2000);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {
                        View markerinfoView=getLayoutInflater().inflate(R.layout.fence_info_window, null);
                        TextView name=(TextView)markerinfoView.findViewById(R.id.name);
                        TextView details=(TextView)markerinfoView.findViewById(R.id.details);
                        name.setText(mapObjectFromDataBase.getRule_Name()+"\n"+mapObjectFromDataBase.getAlias_Name()+"\n"+mapObjectFromDataBase.getBreach_message_one()+"\n"+mapObjectFromDataBase.getBreach_message_two());
                        return  markerinfoView;
                    }
                });
                marker.showInfoWindow();
            }
        });
    }

    private void showPolyGonFence(GoogleMap mMap) {
        LatLng breach_latitude_longitude = new LatLng(mapObjectFromDataBase.getBreach_latitude(),mapObjectFromDataBase.getBreach_longitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(breach_latitude_longitude);
        markerOptions.icon(bitmapDescriptorFromVector(getActivity(), R.drawable.map_custom_marker));
        Marker marker=  mMap.addMarker(markerOptions);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(breach_latitude_longitude, 19));
        PolygonOptions polygonOptions = new PolygonOptions().addAll(polygon_lat_long).strokeColor(Color.BLACK).strokeWidth(3).fillColor(R.color.colorAccent);//.strokeWidth(3).clickable(true);
        Polygon polygon = mMap.addPolygon(polygonOptions);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isVisible()){
                    mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                        @Override
                        public View getInfoWindow(Marker marker) {
                            return null;
                        }

                        @Override
                        public View getInfoContents(Marker marker) {
                            View markerinfoView=getLayoutInflater().inflate(R.layout.fence_info_window, null);
                            TextView name=(TextView)markerinfoView.findViewById(R.id.name);
                            TextView details=(TextView)markerinfoView.findViewById(R.id.details);
                            name.setText(mapObjectFromDataBase.getRule_Name()+"\n"+mapObjectFromDataBase.getAlias_Name()+"\n"+mapObjectFromDataBase.getBreach_message_one()+"\n"+mapObjectFromDataBase.getBreach_message_two());
                            cancelProgressDialog();
                            return  markerinfoView;
                        }
                    });
                    marker.showInfoWindow();
                }
            }
        },2000);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {
                        View markerinfoView=getLayoutInflater().inflate(R.layout.fence_info_window, null);
                        TextView name=(TextView)markerinfoView.findViewById(R.id.name);
                        TextView details=(TextView)markerinfoView.findViewById(R.id.details);
                        name.setText(mapObjectFromDataBase.getRule_Name()+"\n"+mapObjectFromDataBase.getAlias_Name()+"\n"+mapObjectFromDataBase.getBreach_message_one()+"\n"+mapObjectFromDataBase.getBreach_message_two());
                        return  markerinfoView;
                    }
                });
                marker.showInfoWindow();
            }
        });
    }
    private void loadFenceData_from_DB(String timeStampFrom_DataBase){
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                         List<GeofenceAlert> geofenceAlertList= roomDBHelperInstance.get_GeoFenceAlert_info_dao().getGeoFeneAlertFromTimeStamp(timeStampFrom_DataBase);
                        for (GeofenceAlert geofenceAlert:geofenceAlertList) {
                            if(geofenceAlert.getGeo_Type().equalsIgnoreCase("Circular")){
                                mapObjectFromDataBase.setGeoFenceType(geofenceAlert.getGeo_Type());
                                mapObjectFromDataBase.setCircular_fence_lat(Double.parseDouble(geofenceAlert.getGeoFence_lat()));
                                mapObjectFromDataBase.setCircular_fence_long(Double.parseDouble(geofenceAlert.getBreach_Long()));
                                mapObjectFromDataBase.setCircular_fence_radius_vertices(Double.parseDouble(geofenceAlert.getGeoFence_radius_vertices()));
                                mapObjectFromDataBase.setBreach_message_one(geofenceAlert.getMessage_one());
                                mapObjectFromDataBase.setBreach_message_two(geofenceAlert.getMessage_two());
                                mapObjectFromDataBase.setAlias_Name(geofenceAlert.getAlias_name_alert());
                                mapObjectFromDataBase.setBreach_latitude(Double.parseDouble(geofenceAlert.getBreach_Lat()));
                                mapObjectFromDataBase.setBreach_longitude(Double.parseDouble(geofenceAlert.getBreach_Long()));
                                mapObjectFromDataBase.setGeoFence_Id(geofenceAlert.getGeofence_ID());
                                mapObjectFromDataBase.setRule_Name(geofenceAlert.getRule_Name());
                            }else if(geofenceAlert.getGeo_Type().equalsIgnoreCase("Polygon")){
                                mapObjectFromDataBase.setGeoFenceType(geofenceAlert.getGeo_Type());
                                mapObjectFromDataBase.setBreach_message_one(geofenceAlert.getMessage_one());
                                mapObjectFromDataBase.setBreach_message_two(geofenceAlert.getMessage_two());
                                mapObjectFromDataBase.setAlias_Name(geofenceAlert.getAlias_name_alert());
                                mapObjectFromDataBase.setBreach_latitude(Double.parseDouble(geofenceAlert.getBreach_Lat()));
                                mapObjectFromDataBase.setBreach_longitude(Double.parseDouble(geofenceAlert.getBreach_Long()));
                                mapObjectFromDataBase.setGeoFence_Id(geofenceAlert.getGeofence_ID());
                                mapObjectFromDataBase.setRule_Name(geofenceAlert.getRule_Name());
                                String firmware_TimeStamp=geofenceAlert.getGeoFence_timestamp();
                                loadPolygonFenceDetails_FromDB(firmware_TimeStamp,mapObjectFromDataBase.getGeoFence_Id());
                            }
                        }
                    }
                });
    }


    private void loadPolygonFenceDetails_FromDB(String timeStamp_to_fetch,String geoFence_ID){
        polygon_lat_long.clear();
      List<PolygonEnt> polygonList= roomDBHelperInstance.get_Polygon_info_dao().getAllPolygonFromTimeStapGeoFenceId(geoFence_ID,timeStamp_to_fetch);
        for (PolygonEnt polygonEnt:polygonList) {
            polygon_lat_long.add(new LatLng(Double.parseDouble(polygonEnt.getLat()), Double.parseDouble(polygonEnt.getLongValue())));

        }
    }
    private void intializeView() {
        hud = KProgressHUD.create(getActivity());
        dialogProvider = new DialogProvider(getActivity());
    }


    private void geoFenceAlertImplementation() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.mainActivity_container);
        mainActivity.setUpGeoFenceAlertDialogInterface(new GeoFenceDialogAlertShow() {
            @Override
            public void showDialogInterface(String ruleVioation, String bleAddress, String message_one, String messageTwo, String timeStamp) {
                if (isAdded() && isVisible() && fragment instanceof FragmentMap) {
                    dialogProvider.showGeofenceAlertDialog(ruleVioation, bleAddress, message_one, messageTwo, 3, false, new onAlertDialogCallBack() {
                        @Override
                        public void PositiveMethod(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            /**
                             * Open map Fragment.
                             */
                            mainActivity.replaceFragment(new FragmentMap(), timeStamp, new FragmentMap().toString(), false);
                        }

                        @Override
                        public void NegativeMethod(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                } else {
                }
            }
        });
    }
}
