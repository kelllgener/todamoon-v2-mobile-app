package com.toda.todamoon_v2.passenger.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.toda.todamoon_v2.R;
import com.toda.todamoon_v2.passenger.ui.LoginPassenger;

import de.hdodenhof.circleimageview.CircleImageView;

public class PassengerSettingsFragment extends Fragment {

    private TextView txtName, txtEmail;
    private CircleImageView imageProfile;
    private View logLayout;
    private static final String ARG_ROLE = "role";
    private static final String ARG_EMAIL = "email";
    private static final String ARG_NAME = "name";
    private static final String ARG_PROFILE_URI = "profileUri";

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

        // Set logout button click listener
        logLayout.setOnClickListener(v -> showLogoutConfirmationDialog());

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
        FirebaseAuth.getInstance().signOut();

        // Navigate back to the login screen
        Intent intent = new Intent(getActivity(), LoginPassenger.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        getActivity().finish(); // Close the current activity
    }
}