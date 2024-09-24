package com.toda.todamoon_v2.driver.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.toda.todamoon_v2.R;
import com.toda.todamoon_v2.utils.FirebaseUtil;
import com.toda.todamoon_v2.utils.LoadingDialogUtil;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditProfileDetails extends AppCompatActivity {
    private LoadingDialogUtil loadingDialogUtil;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private TextInputEditText txtName, txtPhoneNumber, txtTricycleNumber;
    private Spinner spinnerBarangay;

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
        setContentView(R.layout.activity_edit_profile_details);

        loadingDialogUtil = new LoadingDialogUtil(this);

        // Initialize Firebase
        FirebaseAuth mAuth = FirebaseUtil.getFirebaseAuthInstance();
        db = FirebaseUtil.getFirebaseFirestoreInstance();
        user = mAuth.getCurrentUser();

        // Initialize UI Components
        txtName = findViewById(R.id.txt_name);
        txtPhoneNumber = findViewById(R.id.txt_phone_number);
        txtTricycleNumber = findViewById(R.id.txt_tricycle_number);
        spinnerBarangay = findViewById(R.id.spinner_barangay);
        ImageButton btnSave = findViewById(R.id.btn_save);
        ImageButton btnBack = findViewById(R.id.btn_back_to_settings);

        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            loadingDialogUtil.showLoadingDialog();
            updateUser();
        });

        //Functions
        loadUserData();
        spinner();
    }

    private void spinner() {
        String[] barangays = new String[]{"Barandal", "Bubuyan", "Bunggo", "Burol", "Kay-anlog", "Prinza", "Punta"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, barangays) {

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;

                // Get the selected item position from the spinner
                int selectedPosition = spinnerBarangay.getSelectedItemPosition();

                // Set gray color for the currently selected item
                if (position == selectedPosition) {
                    textView.setTextColor(Color.GRAY);
                } else {
                    textView.setTextColor(Color.BLACK);
                }

                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBarangay.setAdapter(adapter);
    }


    private void loadUserData() {
        if(user != null) {
            String userId = user.getUid();
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if(documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String phoneNumber = documentSnapshot.getString("phoneNumber");
                            String tricycleNumber = documentSnapshot.getString("tricycleNumber");
                            String barangay = documentSnapshot.getString("barangay");

                            txtName.setText(name);
                            txtPhoneNumber.setText(phoneNumber);
                            txtTricycleNumber.setText(tricycleNumber);

                            // Set the spinner to the user's barangay
                            if (barangay != null) {
                                ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerBarangay.getAdapter();
                                int position = adapter.getPosition(barangay);
                                if (position >= 0) {
                                    spinnerBarangay.setSelection(position);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("", "Failed to fetch user data: ", e));
        }
    }

    private void updateUser() {
        // Ensure the user is authenticated
        if (user != null) {
            String userId = user.getUid();

            // Gather updated data from input fields
            String updatedName = txtName.getText().toString().trim();
            String updatedPhoneNumber = txtPhoneNumber.getText().toString().trim();
            String updatedTricycleNumber = txtTricycleNumber.getText().toString().trim();
            String updatedBarangay = spinnerBarangay.getSelectedItem().toString();

            // Get current barangay before updating the user data
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String currentBarangay = documentSnapshot.getString("barangay");
                            boolean inQueue = documentSnapshot.getBoolean("inQueue");

                            if (Boolean.FALSE.equals(inQueue)) {
                                // If barangay has changed, update barangay in Firestore
                                if (!currentBarangay.equals(updatedBarangay)) {
                                    updateBarangay(userId, currentBarangay, updatedBarangay);
                                }
                                // Create a map to hold the updated values
                                Map<String, Object> updatedData = new HashMap<>();
                                updatedData.put("name", updatedName);
                                updatedData.put("phoneNumber", updatedPhoneNumber);
                                updatedData.put("tricycleNumber", updatedTricycleNumber);
                                updatedData.put("barangay", updatedBarangay);

                                // Update Firestore with new data
                                db.collection("users").document(userId)
                                        .update(updatedData)
                                        .addOnSuccessListener(aVoid -> {
                                            loadingDialogUtil.hideLoadingDialog();
                                            Toast.makeText(this, "User profile updated successfully", Toast.LENGTH_SHORT).show();
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            // Update failed, handle the error
                                            Log.e("EditProfileDetails", "Error updating user profile", e);
                                        });
                            } else {
                                Toast.makeText(this, "Profile update unsuccessful. Please exit the queue before making changes.", Toast.LENGTH_SHORT).show();
                                loadingDialogUtil.hideLoadingDialog();
                                finish();
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("EditProfileDetails", "Error fetching user data", e));
        }
    }

    private void updateBarangay(String userID, String currentBarangay, String newBarangay) {
        if (user != null) {
            String userId = user.getUid();
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            boolean inQueue = documentSnapshot.getBoolean("inQueue");
                            String tricycleNumber = documentSnapshot.getString("tricycleNumber");
                            String uid = documentSnapshot.getString("uid");

                            // Step 1: Delete the user from the current barangay
                            db.collection("barangays").document(currentBarangay)
                                    .collection("drivers").document(userID)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("EditProfileDetails", "User successfully removed from " + currentBarangay);

                                        // Step 2: Add the user to the new barangay
                                        Map<String, Object> driverData = new HashMap<>();
                                        driverData.put("name", name);
                                        driverData.put("inQueue", inQueue);
                                        driverData.put("tricycleNumber", tricycleNumber);
                                        driverData.put("uid", uid);

                                        db.collection("barangays").document(newBarangay)
                                                .collection("drivers").document(userID)
                                                .set(driverData)
                                                .addOnSuccessListener(aVoid1 -> Log.d("EditProfileDetails", "User successfully added to " + newBarangay))
                                                .addOnFailureListener(e -> Log.e("EditProfileDetails", "Error adding user to " + newBarangay, e));
                                    })
                                    .addOnFailureListener(e -> Log.e("EditProfileDetails", "Error removing user from " + currentBarangay, e));
                        }
                    })
                    .addOnFailureListener(e -> Log.e("", "Failed to fetch user data: ", e));
        }

    }




}