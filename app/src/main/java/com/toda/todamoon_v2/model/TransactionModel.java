package com.toda.todamoon_v2.model;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TransactionModel {
    private double amount;
    private Timestamp timestamp;
    private String description;

    public TransactionModel() {
        // Firestore requires a public no-argument constructor
    }

    public TransactionModel(int amount, Timestamp timestamp, String description) {
        this.amount = amount;
        this.timestamp = timestamp;
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }

    public String getFormattedAmount() {
        return String.valueOf(amount);
    }

    public String getFormattedTimestamp() {
        Date date = timestamp.toDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy"); // Format: September 1, 2024
        return dateFormat.format(date);
    }
}
