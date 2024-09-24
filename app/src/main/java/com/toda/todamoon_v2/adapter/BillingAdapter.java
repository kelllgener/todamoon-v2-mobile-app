package com.toda.todamoon_v2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.toda.todamoon_v2.R;
import com.toda.todamoon_v2.model.BillingModel;

import java.util.List;
import java.util.Locale;

public class BillingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_DATE_HEADER = 0;
    private static final int TYPE_TRANSACTION_ITEM = 1;

    private final Context context;
    private final List<Object> records;

    public BillingAdapter(Context context, List<Object> records) {
        this.context = context;
        this.records = records;
    }

    @Override
    public int getItemViewType(int position) {
        Object record = records.get(position);
        if (record instanceof String) {
            return TYPE_DATE_HEADER;
        } else if (record instanceof BillingModel) {
            return TYPE_TRANSACTION_ITEM;
        }
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_DATE_HEADER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_date_header, parent, false);
            return new DateHeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.billing_item_records, parent, false);
            return new BillingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DateHeaderViewHolder) {
            String date = (String) records.get(position);
            ((DateHeaderViewHolder) holder).sectionDate.setText(date);
        } else if (holder instanceof BillingViewHolder) {
            BillingModel record = (BillingModel) records.get(position);
            ((BillingViewHolder) holder).recordTitle.setText(record.getDescription());
            ((BillingViewHolder) holder).recordDateTime.setText(record.getFormattedTimestamp());
            ((BillingViewHolder) holder).recordAmount.setText(String.format(Locale.getDefault(), "+₱%.2f", record.getAmount()));
        }
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    // ViewHolder for date header
    static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView sectionDate;

        DateHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            sectionDate = itemView.findViewById(R.id.sectionDate);
        }
    }

    // ViewHolder for billing records
    static class BillingViewHolder extends RecyclerView.ViewHolder {
        TextView recordTitle;
        TextView recordDateTime;
        TextView recordAmount;

        BillingViewHolder(@NonNull View itemView) {
            super(itemView);
            recordTitle = itemView.findViewById(R.id.record_title);
            recordDateTime = itemView.findViewById(R.id.record_date_time);
            recordAmount = itemView.findViewById(R.id.record_amount);
        }
    }
}
