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

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private Context context;
    private List<TransactionModel> transactions;

    public TransactionAdapter(Context context, List<TransactionModel> transactions) {
        this.context = context;
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TransactionModel transaction = transactions.get(position);

        holder.transactionTitle.setText(transaction.getDescription());
        holder.transactionDateTime.setText(transaction.getFormattedTimestamp());
        holder.transactionAmount.setText(String.format(Locale.getDefault(), "-₱%.2f", transaction.getAmount()));
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView transactionTitle;
        TextView transactionDateTime;
        TextView transactionAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionTitle = itemView.findViewById(R.id.transaction_title);
            transactionDateTime = itemView.findViewById(R.id.transaction_date_time);
            transactionAmount = itemView.findViewById(R.id.transaction_amount);
        }
    }
}
