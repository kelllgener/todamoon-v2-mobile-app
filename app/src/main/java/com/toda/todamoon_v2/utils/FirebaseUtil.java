package com.toda.todamoon_v2.utils;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseUtil {

    private static FirebaseAuth mAuth;
    private static FirebaseStorage storage;
    private static FirebaseFirestore db;
    private static FirebaseDatabase database;

    private FirebaseUtil() {
        // Private constructor to prevent instantiation
    }

    public static FirebaseAuth getFirebaseAuthInstance() {
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }
        return mAuth;
    }

    public static FirebaseStorage getFirebaseStorageInstance() {
        if (storage == null) {
            storage = FirebaseStorage.getInstance();
        }
        return storage;
    }

    public static FirebaseFirestore getFirebaseFirestoreInstance() {
        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }
        return db;
    }

    public static FirebaseDatabase getFirebaseDatabaseInstance() {
        if (database == null) {
            database = FirebaseDatabase.getInstance();
        }
        return database;
    }

    public static GoogleSignInOptions getGoogleSignInOptions(String clientId) {
        return new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientId)
                .requestEmail()
                .build();
    }

    public static StorageReference getQrCodeReference(String driverUid) {
        return getFirebaseStorageInstance().getReference().child("qrcodes/" + driverUid + ".png");
    }
}
