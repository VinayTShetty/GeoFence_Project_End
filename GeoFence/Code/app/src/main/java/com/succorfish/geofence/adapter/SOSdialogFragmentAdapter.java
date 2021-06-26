package com.succorfish.geofence.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.succorfish.geofence.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SOSdialogFragmentAdapter extends RecyclerView.Adapter<SOSdialogFragmentAdapter.SOSItemViewHolder> {
    private ArrayList<String> sosMessagesList;
    private Context context;
    sosItemListMesageListInterface sosItemListMesageListInterface;

    public SOSdialogFragmentAdapter(ArrayList<String> sosMessagesList_loc){
        this.sosMessagesList=sosMessagesList_loc;
    }

    @NonNull
    @Override
    public SOSItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context=parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.dialog_fragment_sos_messagelist, parent, false);

        return new SOSItemViewHolder(itemView);
    }



    @Override
    public void onBindViewHolder(@NonNull SOSItemViewHolder sosItemViewHolder, int position) {
        sosItemViewHolder.bindDetails(sosMessagesList.get(position));
    }

    @Override
    public int getItemCount() {
        return sosMessagesList.size();
    }

    public class SOSItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        @BindView(R.id.sos_message)
        TextView messageItemTextView;
        void bindDetails(String name_For_EachItem){
            messageItemTextView.setText(name_For_EachItem);
        }

        public SOSItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(sosItemListMesageListInterface!=null){
                sosItemListMesageListInterface.sosMessageItem(getAdapterPosition());
            }
        }
    }

    public interface sosItemListMesageListInterface{
        public void sosMessageItem(int postion);
    }
    public void setOnItemClickListner_sosMessage(sosItemListMesageListInterface sosItemListMesageListInterface_loc){
       this. sosItemListMesageListInterface=sosItemListMesageListInterface_loc;
    }
}
