package com.succorfish.geofence.Fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

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
import com.google.gson.Gson;
import com.succorfish.geofence.BaseFragment.BaseFragment;
import com.succorfish.geofence.CustomObjectsAPI.AssetDeatils;
import com.succorfish.geofence.MainActivity;
import com.succorfish.geofence.R;

import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.succorfish.geofence.utility.RetrofitHelperClass.haveInternet;

public class FragmentRemoteTrackingMap extends BaseFragment {
    View fragmentRemoteTrackingView;
    private Unbinder unbinder;
    MainActivity mainActivity;
    MapView mMapView;
    String connected_bleAddress="";
    GoogleMap fgragmentRemoteTrackinggoogleMap;
    String deviceIdToFetchForLatLong=null;
    final Handler handler = new Handler();
    private Handler RemoteTrackingHandler = new Handler();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        /**
         * Fetch Bundle Data.
         */
        getBundleData();
    }

    private void getBundleData() {
     Bundle bundle=   this.getArguments();
     if(bundle!=null){
         deviceIdToFetchForLatLong=  bundle.getString(FragmentRemoteTrackingList.class.getName());
     }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentRemoteTrackingView = inflater.inflate(R.layout.fragment_remotetracking_map, container, false);
        unbinder = ButterKnife.bind(this, fragmentRemoteTrackingView);
        bottomLayoutVisibility(false);
        setUpMap(fragmentRemoteTrackingView, savedInstanceState);
        call_Lat_Long_API_ForDeviceId();
        return fragmentRemoteTrackingView;
    }

    private void call_Lat_Long_API_ForDeviceId() {
        if(deviceIdToFetchForLatLong!=null&&deviceIdToFetchForLatLong.length()>0){
            latLongAPI();
        }
    }

    private void latLongAPI() {
        if(haveInternet(getActivity())){
            Call<String> detailsOfAssetList=   mainActivity.mApiService.getLatLongOfAsset(deviceIdToFetchForLatLong);
            detailsOfAssetList.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    Gson gson = new Gson();
                    if (response.code() == 200 || response.isSuccessful()) {
                        System.out.println("response mAddInstData---------" + response.body().toString());
                        if (response.body() != null && !response.body().equalsIgnoreCase("") && !response.body().equalsIgnoreCase("null")) {
                            AssetDeatils assetDeatil= gson.fromJson(response.body(), AssetDeatils.class);
                            if(assetDeatil!=null){
                                if((assetDeatil.getLat()!=null)&&(!assetDeatil.getLat().equalsIgnoreCase("")&&(assetDeatil.getLng()!=null)&&(!assetDeatil.getLng().equalsIgnoreCase("")))){
                                    double latitude=Double.parseDouble(assetDeatil.getLat());
                                    double longitude=Double.parseDouble(assetDeatil.getLng());
//                                    displayLocationInMap(latitude,longitude);
                                    showUpdatedMarkerInMap(latitude,longitude);
                                    handler.postDelayed(remoteTrackingRunnable, 60000);
                                }
                            }
                        } else {

                        }

                    } else {

                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {

                }
            });
        }
    }


    private void showUpdatedMarkerInMap(Double inputLatitude,Double inputlongitude){
        if(fgragmentRemoteTrackinggoogleMap!=null){
            fgragmentRemoteTrackinggoogleMap.clear();
            fgragmentRemoteTrackinggoogleMap.animateCamera(CameraUpdateFactory.zoomOut());
            LatLng location = new LatLng(inputLatitude,inputlongitude);
            Circle circle = fgragmentRemoteTrackinggoogleMap.addCircle(new CircleOptions()
                    .center(new LatLng(inputLatitude,inputlongitude))
                    .radius(2)
                    .strokeColor(Color.BLACK).strokeWidth(3)
                    .fillColor(R.color.colorAccent));
            MarkerOptions markerOptions = new MarkerOptions().position(location);
            fgragmentRemoteTrackinggoogleMap.addMarker(markerOptions.icon(bitmapDescriptorFromVector(getActivity(), R.drawable.circular_fence)));
            LatLng breach_latitude_longitude = new LatLng(inputLatitude,inputlongitude);
            MarkerOptions circular_lat_long = new MarkerOptions();
            circular_lat_long.position(breach_latitude_longitude);
            circular_lat_long.icon(bitmapDescriptorFromVector(getActivity(), R.drawable.map_custom_marker));
            Marker marker =    fgragmentRemoteTrackinggoogleMap.addMarker(circular_lat_long);
            fgragmentRemoteTrackinggoogleMap.getUiSettings().setRotateGesturesEnabled(true);
            fgragmentRemoteTrackinggoogleMap.getUiSettings().setZoomControlsEnabled(true);
            fgragmentRemoteTrackinggoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(breach_latitude_longitude, 20));
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(isVisible()){
                        fgragmentRemoteTrackinggoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                            @Override
                            public View getInfoWindow(Marker marker) {
                                return null;
                            }

                            @Override
                            public View getInfoContents(Marker marker) {
                                View markerinfoView=getLayoutInflater().inflate(R.layout.fence_info_window, null);
                                TextView name=(TextView)markerinfoView.findViewById(R.id.name);
                                TextView details=(TextView)markerinfoView.findViewById(R.id.details);
                                String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
                                name.setText("Updated At \n"+mydate);
                                return  markerinfoView;
                            }
                        });
                        marker.showInfoWindow();
                    }
                }
            },2000);
            fgragmentRemoteTrackinggoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    fgragmentRemoteTrackinggoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                        @Override
                        public View getInfoWindow(Marker marker) {
                            return null;
                        }
                        @Override
                        public View getInfoContents(Marker marker) {
                            View markerinfoView=getLayoutInflater().inflate(R.layout.fence_info_window, null);
                            TextView name=(TextView)markerinfoView.findViewById(R.id.name);
                            TextView details=(TextView)markerinfoView.findViewById(R.id.details);
                            String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
                            name.setText("Updated At\n"+mydate);
                            return  markerinfoView;
                        }
                    });
                    marker.showInfoWindow();
                }
            });
        }
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int VectorResId) {
        Drawable VectorDrawable = ContextCompat.getDrawable(context, VectorResId);
        VectorDrawable.setBounds(0, 0, VectorDrawable.getIntrinsicWidth(), VectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(VectorDrawable.getIntrinsicWidth(), VectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canves = new Canvas(bitmap);
        VectorDrawable.draw(canves);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void bottomLayoutVisibility(boolean hide_true_unhide_false){
        mainActivity.hideBottomLayout(hide_true_unhide_false);
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
                fgragmentRemoteTrackinggoogleMap=mMap;
            }
        });

    }

    @OnClick(R.id.fragment_remote_tracking_back)
    public void backPressImageButton(){
        mainActivity.onBackPressed();
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
        mMapView.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        handler.removeCallbacks(remoteTrackingRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public String toString() {
        return FragmentRemoteTrackingMap.class.getSimpleName();
    }

    private Runnable remoteTrackingRunnable = new Runnable() {
        @Override
        public void run() {
            if(isVisible()){
                latLongAPI();
            };
        }
    };
}
