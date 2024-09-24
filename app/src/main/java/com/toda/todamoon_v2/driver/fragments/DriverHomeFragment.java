package com.toda.todamoon_v2.driver.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.toda.todamoon_v2.R;
import com.toda.todamoon_v2.adapter.MyPagerAdapter;
import com.toda.todamoon_v2.driver.ui.TransactionHistory;
import com.toda.todamoon_v2.utils.AndroidUtil;
import com.toda.todamoon_v2.utils.FirebaseUtil;

import java.util.Locale;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DriverHomeFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private static final String TAG = "DriverHomeFragment";
    private double balance, terminalFee;
    private TextView balanceTextView, currentFeeTextView, seeAll;
    private ProgressBar loadingIndicatorBalance;
    private ProgressBar loadingIndicatorFee;

    public DriverHomeFragment() {
        // Required empty public constructor
    }

    public static DriverHomeFragment newInstance() {
        return new DriverHomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseUtil.getFirebaseAuthInstance();
        db = FirebaseUtil.getFirebaseFirestoreInstance();
        user = mAuth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.driver_home_fragment, container, false);
        // Initialize UI components
        initUIComponents(view);
        fetchBalanceFromFirestore();
        fetchTerminalFee();
        return view;
    }

    private void initUIComponents(View view) {
        // Display the balance
        balanceTextView = view.findViewById(R.id.accountBalance);
        currentFeeTextView = view.findViewById(R.id.terminalFee);

        loadingIndicatorBalance = view.findViewById(R.id.loadingIndicatorBalance); // Loading indicator initialization
        loadingIndicatorFee = view.findViewById(R.id.loadingIndicatorFee); // Loading indicator initialization

        seeAll = view.findViewById(R.id.seeAllText);

        seeAll.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TransactionHistory.class);
            startActivity(intent);
        });

        TextView dateTextView = view.findViewById(R.id.txtDate);
        AndroidUtil.startDateTimeUpdater(dateTextView);

        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        ViewPager viewPager = view.findViewById(R.id.viewPager);

        // Set up ViewPager with TabLayout
        MyPagerAdapter pagerAdapter = new MyPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        // Set tab titles
        tabLayout.getTabAt(0).setText("Queue");
        tabLayout.getTabAt(1).setText("Billing");
    }


    private void fetchBalanceFromFirestore() {
        loadingIndicatorBalance.setVisibility(View.VISIBLE);
        balanceTextView.setVisibility(View.GONE);
        if (user != null) {
            String userId = user.getUid();
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Double newBalance = documentSnapshot.getDouble("balance");
                            if (newBalance != null) {
                                balance = newBalance;

                                loadingIndicatorBalance.setVisibility(View.GONE);
                                balanceTextView.setVisibility(View.VISIBLE);
                                balanceTextView.setText(String.format(Locale.getDefault(), "₱%.2f", balance));
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to fetch balance: ", e);
                        loadingIndicatorBalance.setVisibility(View.GONE);
                    });
        }
    }

    private void fetchTerminalFee() {
        loadingIndicatorFee.setVisibility(View.VISIBLE);
        currentFeeTextView.setVisibility(View.GONE);
        if (user != null) {
            db.collection("dashboard-counts").document("terminal-fee").get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Double currentFee = documentSnapshot.getDouble("fee");
                            if (currentFee != null) {
                                terminalFee = currentFee;

                                loadingIndicatorFee.setVisibility(View.GONE);
                                currentFeeTextView.setVisibility(View.VISIBLE);

                                currentFeeTextView.setText(String.format(Locale.getDefault(), "₱%.2f", terminalFee));
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to fetch terminal fee: ", e);
                        loadingIndicatorFee.setVisibility(View.GONE);
                    });
        }
    }
}
