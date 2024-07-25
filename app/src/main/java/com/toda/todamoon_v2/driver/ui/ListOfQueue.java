package com.toda.todamoon_v2.driver.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.toda.todamoon_v2.R;
import com.toda.todamoon_v2.adapter.DriverAdapter;
import com.toda.todamoon_v2.model.DriverQueueingModel;
import com.toda.todamoon_v2.utils.AndroidUtil;

import java.util.ArrayList;
import java.util.List;

public class ListOfQueue extends AppCompatActivity {

    private FirebaseFirestore db;
    private String barangayName;
    private TextView txtBarangayName;
    private RecyclerView driverListRecyclerView;
    private DriverAdapter driverAdapter;
    private List<DriverQueueingModel> driverList;
    private TextView txt_no_avail_tric;
    private TextView dateTimeTextView;

    private static final String TAG = "QueueingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_queue);

        txtBarangayName = findViewById(R.id.driverListTextView);
        txt_no_avail_tric = findViewById(R.id.noAvailableTricTextView);
        ImageView btn_back_to_queue = findViewById(R.id.btn_back_to_queue);

        btn_back_to_queue.setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        driverListRecyclerView = findViewById(R.id.driverListRecyclerView);
        driverListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        driverList = new ArrayList<>();
        driverAdapter = new DriverAdapter(driverList);
        driverListRecyclerView.setAdapter(driverAdapter);

        barangayName = getIntent().getStringExtra("barangayName");

        if (barangayName != null) {
            txtBarangayName.setText(barangayName + " Queueing List");
            fetchAndDisplayDrivers(barangayName);
        } else {
            Toast.makeText(this, "Barangay name is missing", Toast.LENGTH_SHORT).show();
        }

        // Add RecyclerView divider
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(driverListRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        driverListRecyclerView.addItemDecoration(dividerItemDecoration);

        dateTimeTextView = findViewById(R.id.realTimeDateTime);
        AndroidUtil.startDateTimeUpdater(dateTimeTextView);
    }

    private void fetchAndDisplayDrivers(String barangayName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference queueRef = db.collection("barangays").document(barangayName).collection("queue");

        // Modify the query to filter by "inQueue" field and order by "joinTime" field
        queueRef.whereEqualTo("inQueue", true)
                .orderBy("joinTime", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Failed to fetch drivers: ", e);
                        return;
                    }

                    driverList.clear(); // Clear the list before adding new data
                    if (snapshots != null && !snapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            DriverQueueingModel driver = doc.toObject(DriverQueueingModel.class);
                            driverList.add(driver);
                        }
                        if (driverList.isEmpty()) {
                            driverListRecyclerView.setVisibility(View.GONE);
                            txt_no_avail_tric.setVisibility(View.VISIBLE);
                        } else {
                            driverListRecyclerView.setVisibility(View.VISIBLE);
                            txt_no_avail_tric.setVisibility(View.GONE);
                        }
                        driverAdapter.notifyDataSetChanged();
                    } else {
                        // No drivers in queue
                        Log.d(TAG, "No tricycles available in queue.");
                        Toast.makeText(this, "No tricycles available.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI() {
        if (driverList.isEmpty()) {
            driverListRecyclerView.setVisibility(View.GONE);
            txt_no_avail_tric.setVisibility(View.VISIBLE);
        } else {
            driverListRecyclerView.setVisibility(View.VISIBLE);
            txt_no_avail_tric.setVisibility(View.GONE);
        }
        driverAdapter.notifyDataSetChanged();
    }
}