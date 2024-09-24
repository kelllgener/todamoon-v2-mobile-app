package com.toda.todamoon_v2.driver.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.toda.todamoon_v2.R;
import com.toda.todamoon_v2.adapter.DriverAdapter;
import com.toda.todamoon_v2.model.DriverQueueingModel;
import com.toda.todamoon_v2.utils.AndroidUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListOfQueue extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView driverListRecyclerView;
    private DriverAdapter driverAdapter;
    private List<DriverQueueingModel> driverList;
    private TextView txt_no_avail_tric;
    ImageView btn_back_to_queue;
    private static final String TAG = "QueueingActivity";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(updateBaseContextLocale(newBase));
    }

    private Context updateBaseContextLocale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("settings", MODE_PRIVATE);
        String languageCode = prefs.getString("selected_language", "en"); // Default to English

        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = context.getResources().getConfiguration();
        config.setLocale(locale);

        return context.createConfigurationContext(config);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_queue);

        db = FirebaseFirestore.getInstance();

        TextView txtBarangayName = findViewById(R.id.driverListTextView);
        txt_no_avail_tric = findViewById(R.id.noAvailableTricTextView);
        btn_back_to_queue = findViewById(R.id.btn_back_to_queue);

        btn_back_to_queue.setOnClickListener(v -> finish());

        driverListRecyclerView = findViewById(R.id.driverListRecyclerView);
        driverListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        driverList = new ArrayList<>();
        driverAdapter = new DriverAdapter(driverList);
        driverListRecyclerView.setAdapter(driverAdapter);

        String barangayName = getIntent().getStringExtra("barangayName");
        if (barangayName != null) {
            txtBarangayName.setText(barangayName + " Queueing List");
            fetchAndDisplayDrivers(barangayName);
        } else {
            Toast.makeText(this, "Barangay name is missing", Toast.LENGTH_SHORT).show();
        }

        // Add RecyclerView divider
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(driverListRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        driverListRecyclerView.addItemDecoration(dividerItemDecoration);

        TextView dateTimeTextView = findViewById(R.id.realTimeDateTime);
        AndroidUtil.startDateTimeUpdater(dateTimeTextView);
    }

    private void fetchAndDisplayDrivers(String barangayName) {
        db.collection("barangays").document(barangayName).collection("queue")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Failed to fetch drivers: ", e);
                        Toast.makeText(this, "Failed to fetch drivers", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snapshots != null) {
                        driverList.clear(); // Clear the list before adding new data
                        for (QueryDocumentSnapshot doc : snapshots) {
                            DriverQueueingModel driver = doc.toObject(DriverQueueingModel.class);
                            driverList.add(driver);
                        }

                        // Update the RecyclerView visibility based on the data
                        if (driverList.isEmpty()) {
                            driverListRecyclerView.setVisibility(View.GONE);
                            txt_no_avail_tric.setVisibility(View.VISIBLE);
                        } else {
                            driverListRecyclerView.setVisibility(View.VISIBLE);
                            txt_no_avail_tric.setVisibility(View.GONE);
                        }
                        driverAdapter.notifyDataSetChanged(); // Notify adapter of data changes
                    }
                });
    }


}