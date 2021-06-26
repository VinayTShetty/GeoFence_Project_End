package com.succorfish.geofence.adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.succorfish.geofence.R;
import com.succorfish.geofence.customObjects.HistroyList;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

public class FragmentHistoryAdapter extends RecyclerView.Adapter<FragmentHistoryAdapter.HistoryItemViewHolder> {
    private ArrayList<HistroyList> historyList;
    private Context context;
    private HistoryItemClickInterface historyItemClickInterface;
    public FragmentHistoryAdapter(ArrayList<HistroyList> loc_historyList){
        this.historyList=loc_historyList;
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryItemViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

    }

    @NonNull
    @Override
    public HistoryItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context=parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.fragmenthistory_listitem, parent, false);
        return new FragmentHistoryAdapter.HistoryItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryItemViewHolder historyItemViewHolder, int position) {
        historyItemViewHolder.bindHistoryItemsDetails(historyList.get(position));
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public class HistoryItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        @BindView(R.id.breach)
        TextView breachType;
        @BindView(R.id.message)
        TextView messageObtained;
        @BindView(R.id.date_time)
        TextView dateTime;
        @BindView(R.id.geofence_id)
        TextView geoFenceId;
        @BindView(R.id.notification_id)
        TextView notificationUIText;
        @BindView(R.id.alias_name)
        TextView alertAliasname;

        public HistoryItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        void bindHistoryItemsDetails (HistroyList historyData){
            breachType.setText(historyData.getBrachMessage());
            messageObtained.setText(historyData.getMessage_one()+" \n"+historyData.getMessage_two());
            dateTime.setText(historyData.getDateTime());
            geoFenceId.setText("Geofence id: "+historyData.getGeoFenceId());
            alertAliasname.setText(historyData.getAliasName_forAlert());
            if(historyData.getIsRead().equalsIgnoreCase("1")){
                notificationUIText.setVisibility(View.INVISIBLE);
            }else if(historyData.getIsRead().equalsIgnoreCase("0")){
                notificationUIText.setVisibility(View.VISIBLE);
            }
        }


        @Override
        public void onClick(View v) {
            if(historyItemClickInterface!=null){
                historyItemClickInterface.historyitemClick(historyList.get(getAdapterPosition()).getTimeStamp());
            }
        }
    }
    public interface  HistoryItemClickInterface{
        public void historyitemClick(String timeStamp);
    }

    public void setOnItemClickListner(HistoryItemClickInterface loc_historyItemClickInterface){
        this.historyItemClickInterface=loc_historyItemClickInterface;
    }
}
