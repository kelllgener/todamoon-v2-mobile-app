package com.toda.todamoon_v2.passenger.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.toda.todamoon_v2.R;
import com.toda.todamoon_v2.driver.ui.ListOfQueue;

public class PassengerHomeFragment extends Fragment {
    private FirebaseFirestore db;
    // Layouts for each barangay
    private LinearLayout layoutPrinza, layoutBarandal, layoutBunggo, layoutBubuyan, layoutPunta, layoutBurol, layoutKayanlog;

    public PassengerHomeFragment() {
        // Required empty public constructor
    }


    public static PassengerHomeFragment newInstance(String param1, String param2) {
        PassengerHomeFragment fragment = new PassengerHomeFragment();
        Bundle args = new Bundle();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_passenger_home, container, false);

        db = FirebaseFirestore.getInstance();

        // Initialize layout references
        layoutPrinza = view.findViewById(R.id.layoutPrinza);
        layoutBarandal = view.findViewById(R.id.layoutBarandal);
        layoutBunggo = view.findViewById(R.id.layoutBunggo);
        layoutBubuyan = view.findViewById(R.id.layoutBubuyan);
        layoutPunta = view.findViewById(R.id.layoutPunta);
        layoutBurol = view.findViewById(R.id.layoutBurol);
        layoutKayanlog = view.findViewById(R.id.layoutKayanlog);

        // Set click listeners for layouts
        setLayoutClickListeners();

        // Fetch and display driver counts
        fetchAndDisplayDriverCounts();

        return view;
    }

    private void setLayoutClickListeners() {
        layoutPrinza.setOnClickListener(v -> openQueueingActivity("Prinza"));
        layoutBarandal.setOnClickListener(v -> openQueueingActivity("Barandal"));
        layoutBunggo.setOnClickListener(v -> openQueueingActivity("Bunggo"));
        layoutBubuyan.setOnClickListener(v -> openQueueingActivity("Bubuyan"));
        layoutPunta.setOnClickListener(v -> openQueueingActivity("Punta"));
        layoutBurol.setOnClickListener(v -> openQueueingActivity("Burol"));
        layoutKayanlog.setOnClickListener(v -> openQueueingActivity("Kay-anlog"));
    }

    private void openQueueingActivity(String barangayName) {
        Intent intent = new Intent(getContext(), ListOfQueue.class);
        intent.putExtra("barangayName", barangayName);
        startActivity(intent);
    }

    private void fetchAndDisplayDriverCounts() {
        db.collection("barangays").addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Toast.makeText(getContext(), "Failed to fetch driver data", Toast.LENGTH_SHORT).show();
                return;
            }

            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                String barangayName = document.getId();
                document.getReference().collection("drivers").addSnapshotListener((queryDocumentSnapshots1, e1) -> {
                    if (e1 != null) {
                        // Handle failure to fetch driver data for a barangay
                        Toast.makeText(getContext(), "Failed to fetch driver data for " + barangayName, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    long driverCount = queryDocumentSnapshots1.size();
                    updateDriverCountInLayout(barangayName, driverCount);
                });
            }

            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                String barangayName = document.getId();
                document.getReference().collection("queue").addSnapshotListener((queryDocumentSnapshots1, e1) -> {
                    if (e1 != null) {
                        // Handle failure to fetch queue data for a barangay
                        Toast.makeText(getContext(), "Failed to fetch queue data for " + barangayName, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    long queueCount = queryDocumentSnapshots1.size();
                    updateDriverCountInQueue(barangayName, queueCount);
                });
            }
        });
    }


    private void updateDriverCountInLayout(String barangayName, long driverCount) {
        switch (barangayName) {
            case "Prinza":
                ((TextView) layoutPrinza.findViewById(R.id.driverCountPrinza)).setText(String.valueOf(driverCount));
                break;
            case "Barandal":
                ((TextView) layoutBarandal.findViewById(R.id.driverCountBarandal)).setText(String.valueOf(driverCount));
                break;
            case "Bunggo":
                ((TextView) layoutBunggo.findViewById(R.id.driverCountBunggo)).setText(String.valueOf(driverCount));
                break;
            case "Bubuyan":
                ((TextView) layoutBubuyan.findViewById(R.id.driverCountBubuyan)).setText(String.valueOf(driverCount));
                break;
            case "Punta":
                ((TextView) layoutPunta.findViewById(R.id.driverCountPunta)).setText(String.valueOf(driverCount));
                break;
            case "Burol":
                ((TextView) layoutBurol.findViewById(R.id.driverCountBurol)).setText(String.valueOf(driverCount));
                break;
            case "Kay-anlog":
                ((TextView) layoutKayanlog.findViewById(R.id.driverCountKayanlog)).setText(String.valueOf(driverCount));
                break;
            default:
                break;
        }
    }

    private void updateDriverCountInQueue(String barangayName, long queueCount) {
        switch (barangayName) {
            case "Prinza":
                ((TextView) layoutPrinza.findViewById(R.id.availableDriverCountPrinza)).setText(String.valueOf(queueCount));
                break;
            case "Barandal":
                ((TextView) layoutBarandal.findViewById(R.id.availableDriverCountBarandal)).setText(String.valueOf(queueCount));
                break;
            case "Bunggo":
                ((TextView) layoutBunggo.findViewById(R.id.availableDriverCountBunggo)).setText(String.valueOf(queueCount));
                break;
            case "Bubuyan":
                ((TextView) layoutBubuyan.findViewById(R.id.availableDriverCountBubuyan)).setText(String.valueOf(queueCount));
                break;
            case "Punta":
                ((TextView) layoutPunta.findViewById(R.id.availableDriverCountPunta)).setText(String.valueOf(queueCount));
                break;
            case "Burol":
                ((TextView) layoutBurol.findViewById(R.id.availableDriverCountBurol)).setText(String.valueOf(queueCount));
                break;
            case "Kay-anlog":
                ((TextView) layoutKayanlog.findViewById(R.id.availableDriverCountKayanlog)).setText(String.valueOf(queueCount));
                break;
            default:
                break;
        }
    }
}