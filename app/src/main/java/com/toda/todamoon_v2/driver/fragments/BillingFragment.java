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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.toda.todamoon_v2.R;
import com.toda.todamoon_v2.adapter.BillingAdapter;
import com.toda.todamoon_v2.model.BillingModel;
import com.toda.todamoon_v2.utils.FirebaseUtil;

import java.util.ArrayList;
import java.util.List;

public class BillingFragment extends Fragment {
    private FirebaseFirestore db;
    private FirebaseUser user;
    private BillingAdapter adapter;
    private List<Object> recordList;
    private ProgressBar loadingIndicator;
    private RecyclerView recyclerView;

    public BillingFragment() {
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
        View view = inflater.inflate(R.layout.fragment_billing, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.billing_record_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recordList = new ArrayList<>();
        adapter = new BillingAdapter(getContext(), recordList);
        recyclerView.setAdapter(adapter);

        loadingIndicator = view.findViewById(R.id.loadingIndicator);

        // Load data into RecyclerView
        loadBillingHistory();

        return view;
    }

    private void loadBillingHistory() {
        if (loadingIndicator == null || recyclerView == null) {
            return; // Safety check
        }

        loadingIndicator.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        if (user != null) {
            String userId = user.getUid();
            CollectionReference transactionsRef = db.collection("users").document(userId)
                    .collection("billing-transactions");

            transactionsRef.orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            return;
                        }
                        if (snapshots != null && !snapshots.isEmpty()) {
                            List<Object> tempList = new ArrayList<>();
                            String currentDate = null;

                            for (QueryDocumentSnapshot doc : snapshots) {
                                BillingModel record = doc.toObject(BillingModel.class);

                                // Get the date formatted as 'September 1, 2024'
                                String recordDate = record.getFormattedTimestamp();

                                // Add a date header only if it's a new date group
                                if (!recordDate.equals(currentDate)) {
                                    currentDate = recordDate; // Update current date
                                    tempList.add(currentDate); // Add new date header
                                }

                                tempList.add(record); // Add the transaction
                            }

                            // Update the adapter with the new list
                            recordList.clear();
                            recordList.addAll(tempList);
                            adapter.notifyDataSetChanged();

                        }
                        loadingIndicator.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    });
        }
    }

}
