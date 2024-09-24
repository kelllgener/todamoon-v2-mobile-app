package com.toda.todamoon_v2.driver.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.toda.todamoon_v2.R;
import com.toda.todamoon_v2.ResetPassword;
import com.toda.todamoon_v2.DriverLanguage;
import com.toda.todamoon_v2.driver.ui.DriverProfile;
import com.toda.todamoon_v2.driver.ui.LoginDriver;
import com.toda.todamoon_v2.TermsAndPolicies;
import com.toda.todamoon_v2.utils.FirebaseUtil;

import de.hdodenhof.circleimageview.CircleImageView;

public class DriverSettingsFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private TextView txtName, txtEmail;
    private CircleImageView imageProfile;
    private static final String SHARED_PREFS = "sharedPrefs";
    private ProgressBar loadingIndicator;

    public DriverSettingsFragment() {
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
        View view = inflater.inflate(R.layout.driver_settings_fragment, container, false);

        // Initialize views
        txtName = view.findViewById(R.id.profile_name_driver);
        txtEmail = view.findViewById(R.id.profile_email_driver);
        imageProfile = view.findViewById(R.id.profile_image_driver);
        loadingIndicator = view.findViewById(R.id.loadingIndicator); // Loading indicator initialization
        View logLayout = view.findViewById(R.id.logout_layout_driver);
        View langLayout = view.findViewById(R.id.language_layout_driver);
        View resetPassLayout = view.findViewById(R.id.reset_password_layout);
        View accountLayout = view.findViewById(R.id.account_content_driver);
        View termsAndPolicyLayout = view.findViewById(R.id.terms_and_policy_layout);

        accountLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), DriverProfile.class);
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

        termsAndPolicyLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TermsAndPolicies.class);
            startActivity(intent);
        });

        // Set logout button click listener
        logLayout.setOnClickListener(v -> showLogoutConfirmationDialog());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Retrieve user data
        loadUserData();
    }

    private void loadUserData() {
        // Show the loading indicator when starting the data load
        loadingIndicator.setVisibility(View.VISIBLE);
        imageProfile.setVisibility(View.GONE);
        if (user != null && isAdded() && getView() != null) {
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
                            Glide.with(DriverSettingsFragment.this)
                                    .load(profileImageUrl)
                                    .into(imageProfile);

                            // Hide the loading indicator after the data is loaded
                            loadingIndicator.setVisibility(View.GONE);
                            imageProfile.setVisibility(View.VISIBLE);
                        } else {
                            // Hide the loading indicator if no data is found
                            loadingIndicator.setVisibility(View.GONE);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("DriverSettingsFragment", "Failed to fetch user data: ", e);
                        // Hide the loading indicator if there's an error
                        loadingIndicator.setVisibility(View.GONE);
                    });
        } else {
            // Hide the loading indicator if the user is null
            loadingIndicator.setVisibility(View.GONE);
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
        Intent intent = new Intent(getActivity(), LoginDriver.class);
        startActivity(intent);
        requireActivity().finish(); // Close the current activity
    }
}
