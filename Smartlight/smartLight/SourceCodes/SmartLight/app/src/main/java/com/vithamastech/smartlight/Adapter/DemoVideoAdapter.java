package com.vithamastech.smartlight.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vithamastech.smartlight.R;
import com.vithamastech.smartlight.Vo.VoYouTubeVideos;

import java.util.ArrayList;
import java.util.List;

public class DemoVideoAdapter extends RecyclerView.Adapter<DemoVideoAdapter.ViewHolder> {

    private List<VoYouTubeVideos> youTubeVideosList;
    private OnDemoVideoAdapterClicked onDemoVideoAdapterClicked;

    public DemoVideoAdapter(List<VoYouTubeVideos> youTubeVideosList) {
        this.youTubeVideosList = youTubeVideosList;
        if (youTubeVideosList == null) {
            this.youTubeVideosList = new ArrayList<>();
        }
    }

    public DemoVideoAdapter() {
        this.youTubeVideosList = new ArrayList<>();
    }

    @NonNull
    @Override
    public DemoVideoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_demo_video_adapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DemoVideoAdapter.ViewHolder holder, int position) {
        VoYouTubeVideos voYouTubeVideos = youTubeVideosList.get(position);
        if (voYouTubeVideos != null) {
            String title = voYouTubeVideos.getTitle();
            if (title == null) {
                title = "Title";
            }
            holder.textViewTitle.setText(title);
        }
    }

    @Override
    public int getItemCount() {
        return youTubeVideosList.size();
    }

    public void update(List<VoYouTubeVideos> youTubeVideosList){
        this.youTubeVideosList = youTubeVideosList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView textViewTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (onDemoVideoAdapterClicked != null) {
                int position = getLayoutPosition();
                onDemoVideoAdapterClicked.onClicked(youTubeVideosList.get(position), position);
            }
        }
    }

    public void setOnDemoVideoAdapterClicked(OnDemoVideoAdapterClicked onDemoVideoAdapterClicked) {
        this.onDemoVideoAdapterClicked = onDemoVideoAdapterClicked;
    }

    public interface OnDemoVideoAdapterClicked {
        void onClicked(VoYouTubeVideos youTubeVideos, int position);
    }
}