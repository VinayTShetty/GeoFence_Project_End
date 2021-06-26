package com.succorfish.geofence.adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.succorfish.geofence.R;
import com.succorfish.geofence.customObjects.SimDetails;
import java.util.ArrayList;
import butterknife.BindView;
import butterknife.ButterKnife;

public class FragmentUARTSingleSelectionAdapter extends RecyclerView.Adapter<FragmentUARTSingleSelectionAdapter.UARTDetailsViewHolder> {
    private ArrayList<SimDetails> simDetailsArrayList;
    private Context context;
    private int checkedPosition = -1;
    UARTDetailsHolderInterface fragmentSimeSlecion_UARTDetailsHolderInterface;
    public FragmentUARTSingleSelectionAdapter(ArrayList<SimDetails> loc_historyList){
        this.simDetailsArrayList=loc_historyList;
    }
    @NonNull
    @Override
    public UARTDetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context=parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.fragment_uart_single_selection, parent, false);
        return new UARTDetailsViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(@NonNull UARTDetailsViewHolder simdetails_holder, int position) {
        simdetails_holder.bindDetails(simDetailsArrayList.get(position));
    }
    @Override
    public int getItemCount() {
        return simDetailsArrayList.size();
    }
    public class UARTDetailsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        @BindView(R.id.sim_name_textview)
        TextView uartSelectionName;
        @BindView(R.id.simSelection_imageView)
        ImageView uartSelectionbox;
        public UARTDetailsViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }
        void bindDetails(SimDetails simDetails){
            uartSelectionName.setText(simDetails.getSimDetails());
            if (checkedPosition == -1) {
                uartSelectionbox.setVisibility(View.GONE);
            } else {
                if (checkedPosition == getAdapterPosition()) {
                    uartSelectionbox.setVisibility(View.VISIBLE);
                } else {
                    uartSelectionbox.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void onClick(View v) {
            uartSelectionbox.setVisibility(View.VISIBLE);
            if (checkedPosition != getAdapterPosition()) {
                notifyItemChanged(checkedPosition);
                int postionClicekd=getAdapterPosition();
                checkedPosition = postionClicekd;
                if(fragmentSimeSlecion_UARTDetailsHolderInterface!=null){
                    fragmentSimeSlecion_UARTDetailsHolderInterface.simDetailsClick(postionClicekd);
                }
            }
        }
    }
    public SimDetails getSelectedSimDetails(){
        if (checkedPosition != -1) {
            return simDetailsArrayList.get(checkedPosition);
        }
        return null;
    }
    public interface UARTDetailsHolderInterface {
        public void simDetailsClick(int positon);
    }
    public void setOnItemClickListner_Simdetails(UARTDetailsHolderInterface loc_simDetailsViewHolder){
        this.fragmentSimeSlecion_UARTDetailsHolderInterface =loc_simDetailsViewHolder;
    }
}
