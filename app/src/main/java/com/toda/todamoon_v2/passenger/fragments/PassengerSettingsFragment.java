package com.toda.todamoon_v2.passenger.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.toda.todamoon_v2.R;
import com.toda.todamoon_v2.ResetPassword;
import com.toda.todamoon_v2.DriverLanguage;
import com.toda.todamoon_v2.TermsAndPolicies;
import com.toda.todamoon_v2.passenger.ui.LoginPassenger;
import com.toda.todamoon_v2.passenger.ui.PassengerProfile;
import com.toda.todamoon_v2.utils.FirebaseUtil;

import de.hdodenhof.circleimageview.CircleImageView;

public class PassengerSettingsFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private TextView txtName, txtEmail;
    private CircleImageView imageProfile;
    private static final String SHARED_PREFS = "sharedPrefs";

    public PassengerSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Firebase components
        mAuth = FirebaseUtil.getFirebaseAuthInstance();
        db = FirebaseUtil.getFirebaseFirestoreInstance();
        user = mAuth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_passenger_settings, container, false);

        // Initialize views
        txtName = view.findViewById(R.id.profile_name_passenger);
        txtEmail = view.findViewById(R.id.profile_email_passenger);
        imageProfile = view.findViewById(R.id.profile_image_passenger);
        View logLayout = view.findViewById(R.id.logout_layout_passenger);
        View langLayout = view.findViewById(R.id.language_layout_passenger);
        View resetPassLayout = view.findViewById(R.id.reset_password_layout);
        View accountLayout = view.findViewById(R.id.account_content_passenger);
        View termsAndPolicies = view.findViewById(R.id.terms_and_policy_layout);

        // Set logout button click listener
        accountLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PassengerProfile.class);
            startActivity(intent);
        });

        resetPassLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ResetPassword.class);
            startActivity(intent);
        });

        langLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), DriverLanguage.class);
            startActivity(intent);
        });

        termsAndPolicies.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TermsAndPolicies.class);
            startActivity(intent);
        });

        logLayout.setOnClickListener(v -> showLogoutConfirmationDialog());

        loadUserData();

        return view;
    }

    private void loadUserData() {
        if (user != null) {
            String userId = user.getUid();
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String email = documentSnapshot.getString("email");
                            String profileImageUrl = documentSnapshot.getString("profileImage");

                            // Set user data to views
                            txtName.setText(name);
                            txtEmail.setText(email);
                            Glide.with(PassengerSettingsFragment.this).load(profileImageUrl).into(imageProfile);
                        }
                    })
                    .addOnFailureListener(e -> Log.e("DriverSettingsFragment", "Failed to fetch user data: ", e));
        }
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> signOut())
                .setNegativeButton("No", null)
                .show();
    }
    private void signOut() {
        // Set the value in SharedPreferences to false
        SharedPreferences prefs = requireActivity().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("isLoggedIn", ""); // Assuming you are tracking the login state with "isLoggedIn"
        editor.apply();

        mAuth.signOut();
        // Navigate back to the login screen
        Intent intent = new Intent(getActivity(), LoginPassenger.class);
        startActivity(intent);
        getActivity().finish(); // Close the current activity
    }
}