package com.toda.todamoon_v2.passenger.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.toda.todamoon_v2.R;
import com.toda.todamoon_v2.ResetPassword;
import com.toda.todamoon_v2.driver.ui.DriverLanguage;
import com.toda.todamoon_v2.driver.ui.LoginDriver;
import com.toda.todamoon_v2.passenger.ui.LoginPassenger;
import com.toda.todamoon_v2.passenger.ui.PassengerLanguage;

import de.hdodenhof.circleimageview.CircleImageView;

public class PassengerSettingsFragment extends Fragment {

    private TextView txtName, txtEmail;
    private CircleImageView imageProfile;
    private View logLayout, langLayout, resetPassLayout;
    private static final String ARG_ROLE = "role";
    private static final String ARG_EMAIL = "email";
    private static final String ARG_NAME = "name";
    private static final String ARG_PROFILE_URI = "profileUri";
    private static final String SHARED_PREFS = "sharedPrefs";

    public PassengerSettingsFragment() {
        // Required empty public constructor
    }


    public static PassengerSettingsFragment newInstance(String email, String name, String profileUri) {
        PassengerSettingsFragment fragment = new PassengerSettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        args.putString(ARG_NAME, name);
        args.putString(ARG_PROFILE_URI, profileUri);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_passenger_settings, container, false);

        // Initialize views
        txtName = view.findViewById(R.id.profName);
        txtEmail = view.findViewById(R.id.profEmail);
        imageProfile = view.findViewById(R.id.profileImg);
        logLayout = view.findViewById(R.id.logoutLayout);
        langLayout = view.findViewById(R.id.passengerLanguageLayout);
        resetPassLayout = view.findViewById(R.id.resetPasswordLayout);

        // Set logout button click listener
        logLayout.setOnClickListener(v -> showLogoutConfirmationDialog());
        langLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PassengerLanguage.class);
            startActivity(intent);
        });

        resetPassLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ResetPassword.class);
            startActivity(intent);
        });

        // Retrieve user data based on role
        Bundle args = getArguments();
        if (args != null) {
            String email = args.getString(ARG_EMAIL);
            String name = args.getString(ARG_NAME);
            String profileUri = args.getString(ARG_PROFILE_URI);

            // Set user data to views
            txtName.setText(name);
            txtEmail.setText(email);
            Glide.with(PassengerSettingsFragment.this).load(profileUri).into(imageProfile);
        }

        return view;
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

        FirebaseAuth.getInstance().signOut();
        // Navigate back to the login screen
        Intent intent = new Intent(getActivity(), LoginPassenger.class);
        startActivity(intent);
        getActivity().finish(); // Close the current activity
    }
}