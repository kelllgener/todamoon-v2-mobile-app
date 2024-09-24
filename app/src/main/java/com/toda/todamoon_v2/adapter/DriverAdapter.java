package com.toda.todamoon_v2.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.toda.todamoon_v2.R;
import com.toda.todamoon_v2.model.DriverQueueingModel;

import java.util.List;

public class DriverAdapter extends RecyclerView.Adapter<DriverAdapter.DriverViewHolder> {

    private final List<DriverQueueingModel> driverList;

    public DriverAdapter(List<DriverQueueingModel> driverList) {
        this.driverList = driverList;
    }

    @NonNull
    @Override
    public DriverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.queue_list, parent, false);
        return new DriverViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DriverViewHolder holder, int position) {
        DriverQueueingModel driver = driverList.get(position);
        holder.driverNameTextView.setText(driver.getName());
        holder.driverTricNumberTextView.setText(driver.getTricycleNumber());

        // Set the queue number
        int queueNumber = position + 1; // Increment position by 1 to start from 1 instead of 0
        holder.queueNumberTextView.setText(String.valueOf(queueNumber));

        // Display the formatted join time
        holder.joinTime.setText(driver.getFormattedJoinTime());
    }

    @Override
    public int getItemCount() {
        return driverList.size();
    }

    static class DriverViewHolder extends RecyclerView.ViewHolder {
        public TextView queueNumberTextView;
        public TextView driverNameTextView;
        public TextView driverTricNumberTextView;
        public TextView joinTime;

        DriverViewHolder(View view) {
            super(view);
            queueNumberTextView = view.findViewById(R.id.queue_number);
            driverNameTextView = view.findViewById(R.id.driverNameTextView);
            driverTricNumberTextView = view.findViewById(R.id.driverTricNumberTextView);
            joinTime = itemView.findViewById(R.id.driverDateTextView);
        }
    }
}
