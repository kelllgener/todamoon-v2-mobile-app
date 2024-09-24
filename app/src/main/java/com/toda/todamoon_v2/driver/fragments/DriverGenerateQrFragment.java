package com.toda.todamoon_v2.driver.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.toda.todamoon_v2.R;
import com.toda.todamoon_v2.utils.FirebaseUtil;
import com.toda.todamoon_v2.utils.ImageDownloader;
import android.widget.ProgressBar;
import android.widget.Toast;

public class DriverGenerateQrFragment extends Fragment {
    private FirebaseFirestore db;
    private FirebaseUser user;
    private ImageDownloader imageDownloader;
    private ImageView qrCodeImage;
    private ProgressBar loadingIndicator;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        imageDownloader = new ImageDownloader(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Firebase components
        FirebaseAuth mAuth = FirebaseUtil.getFirebaseAuthInstance();
        db = FirebaseUtil.getFirebaseFirestoreInstance();
        user = mAuth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.driver_generate_qr_fragment, container, false);

        qrCodeImage = view.findViewById(R.id.qrCodeImageView);
        loadingIndicator = view.findViewById(R.id.loadingIndicator);
        Button learnMore = view.findViewById(R.id.learnMoreButton);
        Button addToGallery = view.findViewById(R.id.addToGalleryButton);

        learnMore.setOnClickListener(v -> showBottomSheetDialog());
        addToGallery.setOnClickListener(v -> downloadImage());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadQrCodeImage();  // Ensure the view is fully created before loading the QR code image
    }

    private void loadQrCodeImage() {
        if (user != null && isAdded() && getView() != null) {  // Ensure the fragment is attached and the view is valid
            String userId = user.getUid();

            // Start by showing the loading indicator
            loadingIndicator.setVisibility(View.VISIBLE);
            qrCodeImage.setVisibility(View.GONE);  // Hide the ImageView until the data is loaded

            db.collection("qr_code").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String qrCodeUrl = documentSnapshot.getString("qrCodeUrl");

                            // Ensure the fragment is still attached before loading the image
                            if (isAdded() && getActivity() != null && qrCodeUrl != null) {
                                Glide.with(DriverGenerateQrFragment.this)
                                        .load(qrCodeUrl)
                                        .into(qrCodeImage);

                                // Hide the loading indicator and show the QR code image
                                loadingIndicator.setVisibility(View.GONE);
                                qrCodeImage.setVisibility(View.VISIBLE);
                            }
                        } else {
                            // Document doesn't exist, hide loading indicator
                            loadingIndicator.setVisibility(View.GONE);
                        }
                    })
                    .addOnFailureListener(e -> {
                        loadingIndicator.setVisibility(View.GONE);  // Hide the loading indicator on failure
                    });
        }
    }

    @SuppressLint("InflateParams")
    private void showBottomSheetDialog() {
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_layout, null);

        TextView txtUID = bottomSheetView.findViewById(R.id.txtUID);
        TextView txtName = bottomSheetView.findViewById(R.id.txtName);
        TextView txtStatus = bottomSheetView.findViewById(R.id.txtStatus);

        if (user != null && isAdded()) {
            String userId = user.getUid();
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String uid = documentSnapshot.getString("uid");
                            String name = documentSnapshot.getString("name");
                            Boolean status = documentSnapshot.getBoolean("inQueue");
                            String statusText = (status != null && status) ? "In Queue" : "Not In Queue";

                            txtUID.setText(uid);
                            txtName.setText(name);
                            txtStatus.setText(statusText);

                            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
                            bottomSheetDialog.setContentView(bottomSheetView);
                            bottomSheetDialog.show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed to Load Data.", Toast.LENGTH_SHORT).show());
        }
    }

    private void downloadImage() {
        if (user != null && isAdded()) {
            String userId = user.getUid();
            StorageReference qrCodeRef = FirebaseUtil.getQrCodeReference(userId);
            imageDownloader.downloadAndSaveImage(qrCodeRef);
        }
    }
}
