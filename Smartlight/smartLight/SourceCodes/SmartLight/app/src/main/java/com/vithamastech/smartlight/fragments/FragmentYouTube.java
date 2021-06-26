package com.vithamastech.smartlight.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.vithamastech.smartlight.Adapter.DemoVideoAdapter;
import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.R;
import com.vithamastech.smartlight.Vo.YoutubeVideosRespData;
import com.vithamastech.smartlight.Vo.VoYouTubeVideos;
import com.vithamastech.smartlight.console;
import com.vithamastech.smartlight.db.DBHelper;
import com.vithamastech.smartlight.db.DataHolder;
import com.vithamastech.smartlight.interfaces.API;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FragmentYouTube extends Fragment {
    private MainActivity activity;
    private DemoVideoAdapter todayFragmentAdapter;
    private boolean isNetworkAvailable;

    public static final String BASE_URL = "http://vithamastech.com/";

    public FragmentYouTube() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_youtube, container, false);
        activity.mTextViewTitle.setText(R.string.activity_main_menu_demo_videos);

        RecyclerView demoVideoRecyclerView = view.findViewById(R.id.demoVideoRecyclerView);
        demoVideoRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        demoVideoRecyclerView.setLayoutManager(layoutManager);
        todayFragmentAdapter = new DemoVideoAdapter();
        demoVideoRecyclerView.setAdapter(todayFragmentAdapter);

        todayFragmentAdapter.setOnDemoVideoAdapterClicked((youTubeVideos, position) -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(youTubeVideos.getYoutube_url().concat(youTubeVideos.getVideo_id())));
            startActivity(intent);
        });

        // Register for Network Connectivity (Internet) change callbacks
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        } else {
            NetworkRequest request = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build();
            connectivityManager.registerNetworkCallback(request, networkCallback);
        }

        return view;
    }

    public void loadOnlineData() {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.build();
        API api = retrofit.create(API.class);
        Call<YoutubeVideosRespData> call = api.getYouTubeVideos();

        call.enqueue(new Callback<YoutubeVideosRespData>() {
            @Override
            public void onResponse(@NonNull Call<YoutubeVideosRespData> call, @NonNull Response<YoutubeVideosRespData> response) {
                YoutubeVideosRespData getYouTubeVideo = response.body();
                if (getYouTubeVideo != null && getYouTubeVideo.getResponse().equalsIgnoreCase("true")) {
                    String deleteAllRecordsQuery = "Delete from " + DBHelper.mTableDemoVideo;
                    activity.mDbHelper.exeQuery(deleteAllRecordsQuery);

                    for (VoYouTubeVideos youTubeVideos : getYouTubeVideo.getData()) {
                        String title = youTubeVideos.getTitle();
                        String url = youTubeVideos.getYoutube_url().concat(youTubeVideos.getVideo_id());

                        try {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(DBHelper.mfield_demo_video_title_name, title);
                            contentValues.put(DBHelper.mfield_demo_video_url, url);

                            activity.mDbHelper.insertRecord(DBHelper.mTableDemoVideo, contentValues);

                            updateUI(getYouTubeVideo.getData());
                            activity.hideProgress();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<YoutubeVideosRespData> call, Throwable t) {
                activity.hideProgress();
//                Toast.makeText(getActivity(), "Error  " + t.getMessage(), Toast.LENGTH_SHORT).show();
                loadOfflineData();
            }
        });
    }

    public void loadOfflineData() {
        List<VoYouTubeVideos> youTubeVideosList = new ArrayList<>();
        try {
            DataHolder dataHolder;
            String query = "Select * from " + DBHelper.mTableDemoVideo;
            dataHolder = activity.mDbHelper.readData(query);

            if (dataHolder != null) {
                VoYouTubeVideos voYouTubeVideos;
                for (int i = 0; i < dataHolder.get_Listholder().size(); i++) {
                    voYouTubeVideos = new VoYouTubeVideos();
                    voYouTubeVideos.setVideo_id(dataHolder.get_Listholder().get(i).get(DBHelper.mFieldDemoVideoId));
                    voYouTubeVideos.setTitle(dataHolder.get_Listholder().get(i).get(DBHelper.mfield_demo_video_title_name));
                    voYouTubeVideos.setYoutube_url(dataHolder.get_Listholder().get(i).get(DBHelper.mfield_demo_video_url));
                    youTubeVideosList.add(voYouTubeVideos);
                }
            }
            updateUI(youTubeVideosList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateUI(List<VoYouTubeVideos> data) {
        activity.runOnUiThread(() -> {
            todayFragmentAdapter.update(data);
        });
    }

    ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            // network available
            isNetworkAvailable = true;
            activity.runOnUiThread(() -> {
                activity.showProgress("Please wait", true);
                loadOnlineData();
            });
        }

        @Override
        public void onLost(@NonNull Network network) {
            // network unavailable
            isNetworkAvailable = false;
            activity.runOnUiThread(() -> {
                loadOfflineData();
                Toast.makeText(activity, "No Internet Connection", Toast.LENGTH_SHORT).show();
            });
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        if (!isNetworkAvailable) {
            loadOfflineData();
        }
    }
}