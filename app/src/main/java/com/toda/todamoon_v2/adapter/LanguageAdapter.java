package com.toda.todamoon_v2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.toda.todamoon_v2.R;
import com.toda.todamoon_v2.model.LanguageItem;

import java.util.List;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder> {

    private Context context;
    private List<LanguageItem> languageList;
    private String selectedLanguageCode;
    private OnLanguageSelectedListener listener;

    public interface OnLanguageSelectedListener {
        void onLanguageSelected(String languageCode);
    }

    public LanguageAdapter(Context context, List<LanguageItem> languageList, String selectedLanguageCode, OnLanguageSelectedListener listener) {
        this.context = context;
        this.languageList = languageList;
        this.selectedLanguageCode = selectedLanguageCode;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LanguageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_language, parent, false);
        return new LanguageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageViewHolder holder, int position) {
        LanguageItem languageItem = languageList.get(position);

        holder.flagImageView.setImageResource(languageItem.getFlagResourceId());
        holder.languageTextView.setText(languageItem.getLanguageName() + " " + languageItem.getCountry());
        holder.displayLanguageTextView.setText(languageItem.getDisplayLanguage());

        if (languageItem.getLanguageCode().equals(selectedLanguageCode)) {
            holder.checkImageView.setVisibility(View.VISIBLE);
        } else {
            holder.checkImageView.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            listener.onLanguageSelected(languageItem.getLanguageCode());
        });
    }

    @Override
    public int getItemCount() {
        return languageList.size();
    }

    static class LanguageViewHolder extends RecyclerView.ViewHolder {
        ImageView flagImageView;
        TextView languageTextView;
        TextView displayLanguageTextView;
        ImageView checkImageView;

        public LanguageViewHolder(@NonNull View itemView) {
            super(itemView);
            flagImageView = itemView.findViewById(R.id.iv_flag);
            languageTextView = itemView.findViewById(R.id.tv_language);
            displayLanguageTextView = itemView.findViewById(R.id.tv_display_language);
            checkImageView = itemView.findViewById(R.id.iv_check);
        }
    }
}

