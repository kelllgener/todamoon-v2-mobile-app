package com.toda.todamoon_v2.driver.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;
import com.toda.todamoon_v2.R;

import java.util.HashMap;
import java.util.Map;


public class JoinQueueFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String userId;
    private String barangayName; // Variable to store the barangay name
    private int terminal_fee = 10;

    public JoinQueueFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.join_queue_fragment, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        userId = auth.getCurrentUser().getUid();

        DocumentReference driverRef = db.collection("users").document(userId);
        driverRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Document exists, retrieve the barangay name
                barangayName = documentSnapshot.getString("barangay");
                if (barangayName != null) {
                    setupButtons(view);
                } else {
                    // Handle the case when barangay is null
                }
            } else {
                // Handle the case when the document doesn't exist
            }
        }).addOnFailureListener(e -> {
            // Handle any errors
        });

        return view;
    }

    private void setupButtons(View view) {
        Button joinQueueButton = view.findViewById(R.id.joinQueueButton);
        Button leaveQueueButton = view.findViewById(R.id.leaveQueueButton);

        joinQueueButton.setOnClickListener(v -> joinQueue(barangayName)); // Pass the barangay name
        leaveQueueButton.setOnClickListener(v -> leaveQueue(barangayName)); // Pass the barangay name
    }


    private void joinQueue(String barangayName) {
        String userId = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(userId);
        DocumentReference barangayRef = db.collection("barangays").document(barangayName);
        CollectionReference queueRef = barangayRef.collection("queue");
        CollectionReference historyRef = db.collection("queueing_history");

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot userSnapshot = transaction.get(userRef);

            boolean inQueue = userSnapshot.getBoolean("inQueue");
            int userBalance = userSnapshot.getLong("balance").intValue();

            if (!inQueue && userBalance >= terminal_fee) {
                // Update inQueue and balance for user
                transaction.update(userRef, "inQueue", true);
                transaction.update(userRef, "balance", userBalance - terminal_fee);

                // Update balance for driver

                // Create a transaction record in user transactions
                Map<String, Object> transactionData = new HashMap<>();
                transactionData.put("amount", terminal_fee);
                transactionData.put("timestamp", FieldValue.serverTimestamp());
                transactionData.put("description", "Queue Entry");

                DocumentReference newTransactionRef = userRef.collection("transactions").document();
                transaction.set(newTransactionRef, transactionData);

                // Add to queue
                Map<String, Object> queueData = new HashMap<>();
                queueData.put("name", userSnapshot.getString("name"));
                queueData.put("tricycleNumber", userSnapshot.getString("tricycleNumber"));
                queueData.put("inQueue", true);
                queueData.put("joinTime", FieldValue.serverTimestamp());
                transaction.set(queueRef.document(userId), queueData);

                // Log to history
                Map<String, Object> historyData = new HashMap<>();
                historyData.put("driverId", userId);
                historyData.put("barangay", barangayName);
                historyData.put("action", "join");
                historyData.put("timestamp", FieldValue.serverTimestamp());
                transaction.set(historyRef.document(), historyData);

            } else if (inQueue) {
                throw new IllegalStateException("Already in queue.");
            } else {
                throw new IllegalStateException("Insufficient balance.");
            }
            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(getActivity(), "Joined queue successfully!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            if (e instanceof IllegalStateException) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Failed to join queue: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void leaveQueue(String barangayName) {
        String userId = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(userId);
        DocumentReference barangayRef = db.collection("barangays").document(barangayName);
        CollectionReference queueRef = barangayRef.collection("queue");
        CollectionReference historyRef = db.collection("queueing_history");

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot userSnapshot = transaction.get(userRef);

            boolean inQueue = userSnapshot.getBoolean("inQueue");

            if (inQueue) {
                // Update inQueue status
                transaction.update(userRef, "inQueue", false);

                // Remove from queue
                transaction.delete(queueRef.document(userId));

                // Create a transaction record
                Map<String, Object> transactionData = new HashMap<>();
                transactionData.put("amount", 0);
                transactionData.put("timestamp", FieldValue.serverTimestamp());
                transactionData.put("description", "Left Queue");

                DocumentReference newTransactionRef = userRef.collection("transactions").document();
                transaction.set(newTransactionRef, transactionData);

                // Log to history
                Map<String, Object> historyData = new HashMap<>();
                historyData.put("driverId", userId);
                historyData.put("barangay", barangayName);
                historyData.put("action", "leave");
                historyData.put("timestamp", FieldValue.serverTimestamp());
                transaction.set(historyRef.document(), historyData);

            } else {
                throw new IllegalStateException("Not in queue.");
            }
            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(getActivity(), "Left queue successfully!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            if (e instanceof IllegalStateException) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Failed to leave queue: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}