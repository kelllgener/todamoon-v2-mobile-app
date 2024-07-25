package com.toda.todamoon_v2.model;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        Calendar now = Calendar.getInstance();

        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        String timeString = timeFormat.format(date);

        if (isSameDay(calendar, now)) {
            return "Today, " + timeString;
        } else {
            now.add(Calendar.DATE, -1);
            if (isSameDay(calendar, now)) {
                return "Yesterday, " + timeString;
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, h:mm a", Locale.getDefault());
                return dateFormat.format(date);
            }
        }
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}
