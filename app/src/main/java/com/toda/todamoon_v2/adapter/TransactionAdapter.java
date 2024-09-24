package com.toda.todamoon_v2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.toda.todamoon_v2.R;
import com.toda.todamoon_v2.model.TransactionModel;

import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_DATE_HEADER = 0;
    private static final int TYPE_TRANSACTION_ITEM = 1;

    private final Context context;
    private final List<Object> items;

    public TransactionAdapter(Context context, List<Object> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof String) {
            return TYPE_DATE_HEADER;
        } else if (item instanceof TransactionModel) {
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
            View view = LayoutInflater.from(context).inflate(R.layout.list_item_transaction, parent, false);
            return new TransactionViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DateHeaderViewHolder) {
            String date = (String) items.get(position);
            ((DateHeaderViewHolder) holder).sectionDate.setText(date);
        } else if (holder instanceof TransactionViewHolder) {
            TransactionModel transaction = (TransactionModel) items.get(position);
            ((TransactionViewHolder) holder).transactionTitle.setText(transaction.getDescription());
            ((TransactionViewHolder) holder).transactionDateTime.setText(transaction.getFormattedTimestamp());
            ((TransactionViewHolder) holder).transactionAmount.setText(String.format(Locale.getDefault(), "-₱%.2f", transaction.getAmount()));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView sectionDate;

        DateHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            sectionDate = itemView.findViewById(R.id.sectionDate);
        }
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView transactionTitle;
        TextView transactionDateTime;
        TextView transactionAmount;

        TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionTitle = itemView.findViewById(R.id.transaction_title);
            transactionDateTime = itemView.findViewById(R.id.transaction_date_time);
            transactionAmount = itemView.findViewById(R.id.transaction_amount);
        }
    }
}
