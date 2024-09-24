package com.toda.todamoon_v2.driver.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.toda.todamoon_v2.R;
import com.toda.todamoon_v2.adapter.TransactionAdapter;
import com.toda.todamoon_v2.model.TransactionModel;
import com.toda.todamoon_v2.utils.FirebaseUtil;

import java.util.ArrayList;
import java.util.List;

public class QueueEntryFragment extends Fragment {
    private FirebaseFirestore db;
    private FirebaseUser user;
    private TransactionAdapter adapter;
    private List<Object> transactionList;
    private ProgressBar loadingIndicator;
    private RecyclerView recyclerView;

    public QueueEntryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAuth mAuth = FirebaseUtil.getFirebaseAuthInstance();
        db = FirebaseUtil.getFirebaseFirestoreInstance();
        user = mAuth.getCurrentUser();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_queue_entry, container, false);

        loadingIndicator = view.findViewById(R.id.loadingIndicator);
        recyclerView = view.findViewById(R.id.transaction_history_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        transactionList = new ArrayList<>();
        adapter = new TransactionAdapter(getContext(), transactionList);
        recyclerView.setAdapter(adapter);

        // Load data into RecyclerView
        loadTransactionHistory();

        return view;
    }

    private void loadTransactionHistory() {
        if (loadingIndicator == null || recyclerView == null) {
            return; // Safety check
        }

        loadingIndicator.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        if (user != null) {
            String userId = user.getUid();
            CollectionReference transactionsRef = db.collection("users").document(userId)
                    .collection("queueing-transactions");

            transactionsRef.orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                return;
                            }

                            if (snapshots != null && !snapshots.isEmpty()) {
                                List<Object> tempList = new ArrayList<>();
                                String currentDate = null;

                                for (QueryDocumentSnapshot doc : snapshots) {
                                    TransactionModel transaction = doc.toObject(TransactionModel.class);
                                    String transactionDate = transaction.getFormattedTimestamp();

                                    if (!transactionDate.equals(currentDate)) {
                                        currentDate = transactionDate;
                                        tempList.add(currentDate); // Add new date header
                                    }

                                    tempList.add(transaction); // Add the transaction itself
                                }

                                transactionList.clear();
                                transactionList.addAll(tempList);
                                adapter.notifyDataSetChanged();

                                loadingIndicator.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                            } else {
                                loadingIndicator.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                            }
                        }
                    });
        }
    }
}
