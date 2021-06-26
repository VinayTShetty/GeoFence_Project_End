package com.vithamastech.smartlight.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.vithamastech.smartlight.IconSelector;
import com.vithamastech.smartlight.R;

import java.util.ArrayList;
import java.util.List;

public class SocketIconAdapter extends RecyclerView.Adapter<SocketIconAdapter.SocketIconViewHolder> {

    private List<IconSelector> iconList;
    private OnIconSelectedListener onIconSelectedListener;

    public SocketIconAdapter(List<IconSelector> iconList) {
        this.iconList = iconList;
        if (this.iconList == null) {
            this.iconList = new ArrayList<>();
        }
    }

    public SocketIconAdapter() {
        this.iconList = new ArrayList<>();
    }

    @NonNull
    @Override
    public SocketIconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_socket_customization_icon, parent, false);
        return new SocketIconViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SocketIconViewHolder holder, int position) {
        IconSelector iconSelector = iconList.get(position);
        holder.imageViewSocketIcon.setImageResource(iconSelector.source);

        if (iconSelector.isSelected) {
            holder.layoutIconDisplay.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.rectangle_border_selected));
        } else {
            holder.layoutIconDisplay.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.rectangle_border_unselected));
        }
    }

    @Override
    public int getItemCount() {
        return iconList.size();
    }

    public class SocketIconViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView imageViewSocketIcon;
        private ConstraintLayout layoutIconDisplay;

        public SocketIconViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewSocketIcon = itemView.findViewById(R.id.imageViewSocketIcon);
            layoutIconDisplay = itemView.findViewById(R.id.layoutIconDisplay);
            layoutIconDisplay.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int selectedPosition = this.getLayoutPosition();
            IconSelector iconSelector = iconList.get(getLayoutPosition());
            iconSelector.isSelected = !iconSelector.isSelected;

            for (int i = 0; i < iconList.size(); i++) {
                if (selectedPosition != i) {
                    IconSelector icon = iconList.get(i);
                    icon.isSelected = false;
                }
            }

            notifyDataSetChanged();

            if (onIconSelectedListener != null) {
                onIconSelectedListener.onIconSelected(iconSelector, selectedPosition);
            }
        }
    }

    public IconSelector update(int imageType) {
        IconSelector foundIconSelector = null;
        for (int i = 0; i < iconList.size(); i++) {
            IconSelector iconSelector = iconList.get(i);
            if (iconSelector.source == imageType) {
                foundIconSelector = iconSelector;
                foundIconSelector.isSelected = true;
                notifyDataSetChanged();
            }
        }
        return foundIconSelector;
    }

    public List<IconSelector> getIconList() {
        return this.iconList;
    }

    public interface OnIconSelectedListener {
        public void onIconSelected(IconSelector iconSelector, int position);
    }

    public void setOnIconSelectedListener(OnIconSelectedListener onIconSelectedListener) {
        this.onIconSelectedListener = onIconSelectedListener;
    }
}
