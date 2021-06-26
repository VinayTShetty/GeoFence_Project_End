package com.vithamastech.smartlight.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vithamastech.smartlight.PowerSocketUtils.Socket;
import com.vithamastech.smartlight.R;

import java.util.List;

public class SwitchControlAdapter extends RecyclerView.Adapter<SwitchControlAdapter.ItemViewHolder> {

    private List<Socket> socketsList;
    private OnSocketStateChangeListener socketStateChangeCallback;
    private OnSocketAlarmClickListener socketAlarmClickCallback;

    public SwitchControlAdapter(List<Socket> socketsList) {
        this.socketsList = socketsList;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_switch_control_layout, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Socket socket = socketsList.get(position);

        holder.textViewSocketName.setText(socket.socketName);

        if (socket.imageType == 0) {
            holder.imageViewSocket.setImageResource(R.drawable.socket);
        } else {
            holder.imageViewSocket.setImageResource(socket.imageType);
        }

        byte socketState = (byte) socket.socketState;
        if (socketState == 0x01) {
            holder.radioButtonOn.setChecked(true);
        } else if (socketState == 0x00) {
            holder.radioButtonOff.setChecked(true);
        } else {
            holder.radioButtonOff.setChecked(true);
        }

        if (socket.shouldWaitForOutput) {
            holder.progressBarWaitMqtt.setVisibility(View.VISIBLE);
        } else {
            holder.progressBarWaitMqtt.setVisibility(View.GONE);
        }

        if (socket.isEnabled) {
            holder.radioButtonOn.setEnabled(true);
            holder.radioButtonOff.setEnabled(true);
        } else {
            holder.radioButtonOn.setEnabled(false);
            holder.radioButtonOff.setEnabled(false);
        }
    }

    @Override
    public int getItemCount() {
        return this.socketsList.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewSocketName;
        public RadioButton radioButtonOn, radioButtonOff;
        public ImageView imageViewMore;
        public ImageView imageViewSocket;
        public ProgressBar progressBarWaitMqtt;

        public ItemViewHolder(View view) {
            super(view);
            textViewSocketName = view.findViewById(R.id.textViewSocketName);
            radioButtonOn = view.findViewById(R.id.radioButtonOn);
            radioButtonOff = view.findViewById(R.id.radioButtonOff);
            imageViewMore = view.findViewById(R.id.imageViewAlarm);
            imageViewSocket = view.findViewById(R.id.imageViewSocket);
            progressBarWaitMqtt = view.findViewById(R.id.progressBarWaitMqtt);

            imageViewMore.setOnClickListener(v -> {
                if (socketAlarmClickCallback != null) {
                    Socket socket = socketsList.get(getAdapterPosition());
                    socketAlarmClickCallback.onSocketAlarmClicked(socket, getAdapterPosition(), (byte) socket.socketState);
                }
            });

            /**
             *  If the previous state is same as "ON", then do not send command
             */
            radioButtonOn.setOnClickListener(v -> {
                Socket selectedSocket = socketsList.get(getLayoutPosition());
                int masterSwitchState = 1;  // Master switch ON
                byte switchState = (byte) selectedSocket.socketState;
                // Avoid sending command on the same state. ie : When user clicks on "ON" button even though it is in "ON" state
                if (switchState == 0) {
                    if (socketStateChangeCallback != null) {
                        selectedSocket.socketState = 1;
                        socketStateChangeCallback.onSocketStateChanged(selectedSocket, getAdapterPosition());
                    }
                }
            });

            /**
             *  If the previous state is same as "OFF", then do not send command
             */
            radioButtonOff.setOnClickListener(v -> {
                Socket selectedSocket = socketsList.get(getLayoutPosition());
                byte switchState = (byte) selectedSocket.socketState;
                // Avoid sending command on the same state. ie : When user clicks on "OFF" button even though it is in "OFF" state
                if (switchState == 1) {
                    if (socketStateChangeCallback != null) {
                        selectedSocket.socketState = 0;
                        socketStateChangeCallback.onSocketStateChanged(selectedSocket, getAdapterPosition());
                    }
                }
            });
        }
    }

    public void update(List<Socket> socketsList) {
        this.socketsList.clear();
        this.socketsList = socketsList;
        notifyDataSetChanged();
    }

    public void update(byte[] socketStatesArray) {
        for (int i = 0; i < socketStatesArray.length; i++) {
            byte socketState = socketStatesArray[i];
            Socket socket = socketsList.get(i);
            socket.socketState = socketState;
            socket.shouldWaitForOutput = false;
        }
        notifyDataSetChanged();
    }

    public void update() {
        notifyDataSetChanged();
    }

    public List<Socket> getSocketsList() {
        return this.socketsList;
    }

    public void setOnSocketStateChangeListener(OnSocketStateChangeListener callback) {
        this.socketStateChangeCallback = callback;
    }

    public void setOnSocketAlarmClickListener(OnSocketAlarmClickListener callback) {
        this.socketAlarmClickCallback = callback;
    }

    public interface OnSocketStateChangeListener {
        public void onSocketStateChanged(Socket socket, int itemPosition);
    }

    public interface OnSocketAlarmClickListener {
        public void onSocketAlarmClicked(Socket socket, int itemPosition, byte state);
    }
}