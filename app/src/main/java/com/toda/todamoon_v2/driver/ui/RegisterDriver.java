package com.toda.todamoon_v2.driver.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.toda.todamoon_v2.R;
import com.toda.todamoon_v2.model.DriverGetterSetter;
import com.toda.todamoon_v2.utils.AndroidUtil;
import com.toda.todamoon_v2.utils.FirebaseUtil;
import com.toda.todamoon_v2.utils.LoadingDialogUtil;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RegisterDriver extends AppCompatActivity {
    private AndroidUtil androidUtil;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final int STEP_ONE = 1;
    private static final int STEP_TWO = 2;
    private static final int STEP_THREE = 3;
    private static final int STEP_FOUR = 4;
    private ImageButton step1Button, step2Button, step3Button, regButton;
    private View contactLeft, contactRight, OtherRight;
    private TextView stepTwoTextView, stepThreeTextView;
    private ImageButton previousButton, nextButton;
    private FrameLayout stepContentContainer;
    private View stepOneLayout, stepTwoLayout, stepThreeLayout;
    private TextInputEditText firstName, lastName;
    private Spinner signUpBarangay;
    private TextInputEditText tricycleNumber;
    private TextInputEditText email, phoneNumber, password, confirmPassword;
    private CheckBox chkTermsAndCondition;
    private int currentStep = STEP_ONE;
    private int colorPrimaryDark, colorGray, colorWhite;
    private LoadingDialogUtil loadingDialogUtil;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private TextView textViewFileName;
    private Button buttonChooseImage;
    private static final String SECRET_KEY = "Todamoon_drivers";

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

    private void initializeViews() {
        androidUtil = new AndroidUtil();
        loadingDialogUtil = new LoadingDialogUtil(this);

        buttonChooseImage = findViewById(R.id.button_choose_file);
        textViewFileName = findViewById(R.id.textView_file_name);

        // FIRESTORE
        mAuth = FirebaseUtil.getFirebaseAuthInstance();
        db = FirebaseUtil.getFirebaseFirestoreInstance();

        // MULTIFORM
        step1Button = findViewById(R.id.stepOneButton);
        step2Button = findViewById(R.id.stepTwoButton);
        step3Button = findViewById(R.id.stepThreeButton);

        contactLeft = findViewById(R.id.contactLeftView);
        contactRight = findViewById(R.id.contactRightView);
        OtherRight = findViewById(R.id.OtherRightView);

        stepTwoTextView = findViewById(R.id.stepTwoTextView);
        stepThreeTextView = findViewById(R.id.stepThreeTextView);

        previousButton = findViewById(R.id.previousButton);
        nextButton = findViewById(R.id.nextButton);
        regButton = findViewById(R.id.registerButton);

        stepContentContainer = findViewById(R.id.stepContentContainer);

        // Steps layout
        stepOneLayout = LayoutInflater.from(this).inflate(R.layout.form_step_one, null);
        stepTwoLayout = LayoutInflater.from(this).inflate(R.layout.form_step_two, null);
        stepThreeLayout = LayoutInflater.from(this).inflate(R.layout.form_step_three, null);

        // Step one form field
        firstName = stepOneLayout.findViewById(R.id.signupFirstNameText);
        lastName = stepOneLayout.findViewById(R.id.signupLastNameText);

        // Step two form field
        signUpBarangay = stepTwoLayout.findViewById(R.id.signupBarangaySpinner);
        tricycleNumber = stepTwoLayout.findViewById(R.id.signupTricycleText);
        buttonChooseImage = stepTwoLayout.findViewById(R.id.button_choose_file);
        textViewFileName = stepTwoLayout.findViewById(R.id.textView_file_name);

        // Step three form field
        email = stepThreeLayout.findViewById(R.id.signupEmailText);
        phoneNumber = stepThreeLayout.findViewById(R.id.signupPhoneNumberText);
        password = stepThreeLayout.findViewById(R.id.signupPasswordText);
        confirmPassword = stepThreeLayout.findViewById(R.id.signupConfirmPasswordText);

    }

    private void initializeColors() {
        colorPrimaryDark = ContextCompat.getColor(this, R.color.primary_dark);
        colorGray = ContextCompat.getColor(this, R.color.light_gray);
        colorWhite = ContextCompat.getColor(this, R.color.white);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_driver);

        initializeViews();
        showStepContent(currentStep);
        setPrevNextButtonListener();
        spinner();

        buttonChooseImage.setOnClickListener(v -> chooseImage());

    }

    private void spinner() {
        String[] barangays = new String[]{"Select Barangay", "Barandal", "Bubuyan", "Bunggo", "Burol", "Kay-anlog", "Prinza", "Punta"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, barangays) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;

                if (position == 0) {
                    textView.setTextColor(Color.GRAY);
                } else {
                    textView.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        signUpBarangay.setAdapter(adapter);
    }

    private void setPrevNextButtonListener() {
        previousButton.setOnClickListener(v -> {
            if (currentStep > 1) {
                currentStep--; // Decrease currentStep by 1
                showStepContent(currentStep);
            } else {
                finish();
            }
        });
        nextButton.setOnClickListener(v -> {
            if(currentStep == STEP_ONE) {
                if(validateStepOne()) {
                    currentStep++; // Increase currentStep by 1
                    showStepContent(currentStep);
                } else {
                    Toast.makeText(this, "Please fill out all fields with correct credentials.", Toast.LENGTH_SHORT).show();
                }
            }
            else if(currentStep == STEP_TWO) {
                if(validateStepTwo()) {
                    currentStep++; // Increase currentStep by 1
                    showStepContent(currentStep);
                } else {
                    Toast.makeText(this, "Please fill out all fields with correct credentials.", Toast.LENGTH_SHORT).show();
                }
            }
            else if(currentStep == STEP_THREE) {
                if(validateStepThree()) {
                    currentStep++; // Increase currentStep by 1
                    showStepContent(currentStep);
                } else {
                    Toast.makeText(this, "Please fill out all fields with correct credentials.", Toast.LENGTH_SHORT).show();
                }
            }

        });
        // Show the initial step content
        showStepContent(currentStep);
    }

    private void showStepContent(int step) {
        currentStep = step;
        updateStepButtons();

        switch (step) {
            case STEP_ONE:
                showStepOne();
                break;
            case STEP_TWO:
                showStepTwo();
                break;
            case STEP_THREE:
                showStepThree();
                break;
        }
    }
    private void updateStepButtons() {
        step1Button.setEnabled(currentStep != STEP_ONE);
        step2Button.setEnabled(currentStep != STEP_TWO);
        step3Button.setEnabled(currentStep != STEP_THREE);
    }
    private void showStepOne() {
        initializeColors();

        stepContentContainer.removeAllViews();
        stepContentContainer.addView(stepOneLayout);

        currentStep = 1; // Reset currentStep value to one

        step1Button.setClickable(false); // Make step1Button not clickable
        step2Button.setClickable(true); // Make step2Button clickable
        step3Button.setClickable(true); // Make step3Button clickable

        previousButton.setClickable(true); // Make previousButton not clickable
        nextButton.setClickable(true); // Make nextButton clickable

        // Reset other views colors to gray
        contactLeft.setBackgroundColor(colorGray);
        step2Button.setBackgroundTintList(ColorStateList.valueOf(colorGray));
        stepTwoTextView.setTextColor(colorGray);
        contactRight.setBackgroundColor(colorGray);
        step3Button.setBackgroundTintList(ColorStateList.valueOf(colorGray));
        stepThreeTextView.setTextColor(colorGray);
        OtherRight.setBackgroundColor(colorGray);

        // Reset previous button color
        previousButton.setBackgroundResource(R.drawable.button_style);
        previousButton.setColorFilter(colorPrimaryDark);

        // Reset next button color
        nextButton.setImageResource(R.drawable.ic_forward_arrow);
        nextButton.setBackgroundColor(colorPrimaryDark);
        nextButton.setColorFilter(colorWhite);

        nextButton.setVisibility(View.VISIBLE);
        regButton.setVisibility(View.GONE);
    }

    private void showStepTwo() {
        initializeColors();

        stepContentContainer.removeAllViews();
        stepContentContainer.addView(stepTwoLayout);

        currentStep = 2; // Reset currentStep value to two

        // Step button clickable
        step1Button.setClickable(true);
        step2Button.setClickable(false);
        step3Button.setClickable(true);

        previousButton.setClickable(true); // Make previousButton to clickable
        nextButton.setClickable(true); // Make nextButton to clickable

        contactLeft.setBackgroundColor(colorPrimaryDark);
        step2Button.setBackgroundTintList(ColorStateList.valueOf(colorPrimaryDark));
        stepTwoTextView.setTextColor(colorPrimaryDark);

        // Reset other views colors to gray
        contactRight.setBackgroundColor(colorGray);
        step3Button.setBackgroundTintList(ColorStateList.valueOf(colorGray));
        stepThreeTextView.setTextColor(colorGray);
        OtherRight.setBackgroundColor(colorGray);

        //progressBar.setProgress(58);

        // Change previous button color
        previousButton.setBackgroundColor(colorPrimaryDark);
        previousButton.setColorFilter(colorWhite);

        // Reset next button color
        nextButton.setImageResource(R.drawable.ic_forward_arrow); // reset button icon
        nextButton.setBackgroundColor(colorPrimaryDark);
        nextButton.setColorFilter(colorWhite);

        nextButton.setVisibility(View.VISIBLE);
        regButton.setVisibility(View.GONE);
    }


    private void showStepThree() {
        initializeColors();

        stepContentContainer.removeAllViews();
        stepContentContainer.addView(stepThreeLayout);

        currentStep = 3; // Reset currentStep value to three

        // Step button clickable
        step1Button.setClickable(true);
        step2Button.setClickable(true);
        step3Button.setClickable(false);

        previousButton.setClickable(true); // Make previousButton to clickable
        // nextButton.setClickable(false);  // Make nextButton to clickable
        nextButton.setClickable(false);

        contactLeft.setBackgroundColor(colorPrimaryDark);
        step2Button.setBackgroundTintList(ColorStateList.valueOf(colorPrimaryDark));
        stepTwoTextView.setTextColor(colorPrimaryDark);
        contactRight.setBackgroundColor(colorPrimaryDark);
        step3Button.setBackgroundTintList(ColorStateList.valueOf(colorPrimaryDark));
        stepThreeTextView.setTextColor(colorPrimaryDark);
        OtherRight.setBackgroundColor(colorPrimaryDark);

        // Change previous button color
        previousButton.setBackgroundColor(colorPrimaryDark);
        previousButton.setColorFilter(colorWhite);

        // Change next button color
        nextButton.setVisibility(View.GONE);
        regButton.setVisibility(View.VISIBLE);

        regButton.setOnClickListener(v -> {
            if(validateStepThree()) {
                showDialogMessage();
            } else {
                Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateStepOne() {
        // Perform validation for Step One fields
        String firstNameText = firstName.getText().toString().trim();
        String lastNameText = lastName.getText().toString().trim();

        // Check if first name contains only letters (including spaces and enye)
        boolean firstNameValid = !TextUtils.isEmpty(firstNameText) && firstNameText.matches("[a-zA-ZñÑ\\s]+");

        // Check if last name contains only letters (including spaces and enye)
        boolean lastNameValid = !TextUtils.isEmpty(lastNameText) && lastNameText.matches("[a-zA-ZñÑ\\s]+");

        // Show or hide error message based on validation result for first name
        if (!firstNameValid) {
            firstName.setError("Input a correct name.");
        } else {
            firstName.setError(null); // Clear the error
        }

        // Show or hide error message based on validation result for last name
        if (!lastNameValid) {
            lastName.setError("Input a correct name.");
        } else {
            lastName.setError(null); // Clear the error
        }

        // Return true if both first name and last name are valid
        return firstNameValid && lastNameValid;
    }


    private boolean validateStepTwo() {
        // Perform validation for Step Two fields
        return signUpBarangay.getSelectedItemPosition() != 0
                && !TextUtils.isEmpty(textViewFileName.getText().toString())
                && !TextUtils.isEmpty(tricycleNumber.getText().toString());
    }

    private boolean validateStepThree() {
        // Perform validation for Step Three fields
        String emailText = email.getText().toString().trim();
        String phoneNumberText = phoneNumber.getText().toString().trim();
        String passwordText = password.getText().toString().trim();
        String confirmPasswordText = confirmPassword.getText().toString().trim();

        // Check if email, password, and confirm password fields are not empty
        boolean emailValid = !TextUtils.isEmpty(emailText);
        boolean passwordValid = !TextUtils.isEmpty(passwordText);
        boolean confirmPasswordValid = !TextUtils.isEmpty(confirmPasswordText);

        // Check if phone number is exactly 11 digits
        boolean phoneNumberValid = phoneNumberText.length() == 11;

        // Check if password and confirm password match
        boolean passwordsMatch = passwordText.equals(confirmPasswordText);

        // Show error alerts if phone number is not 11 digits or passwords don't match
        if (!phoneNumberValid) {
            phoneNumber.setError("Phone number must be exactly 11 digits");
        } else {
            phoneNumber.setError(null); // Clear the error
        }

        if (!passwordsMatch) {
            Toast.makeText(getApplicationContext(), "Passwords don't match", Toast.LENGTH_SHORT).show();
            confirmPassword.setError("Passwords don't match");
        } else {
            confirmPassword.setError(null); // Clear the error
        }

        // Return false if any field is empty, phone number is not exactly 11 digits,
        // or passwords don't match
        return emailValid && phoneNumberValid && passwordValid && confirmPasswordValid && passwordsMatch;
    }

    private void showDialogMessage() {
        // Inflate dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.terms_and_condition, null);
        chkTermsAndCondition = dialogView.findViewById(R.id.chkTermsCondition);

        // Build dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setTitle(R.string.terms_and_condition_title)
                .setPositiveButton(R.string.register, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Handle user agreement
                        // You can perform necessary actions here, such as enabling registration
                        currentStep = STEP_FOUR;
                        if (!chkTermsAndCondition.isChecked()) {
                            Toast.makeText(RegisterDriver.this, "Please agree to the terms and condition to continue.", Toast.LENGTH_SHORT).show();
                        } else {
                            // Proceed with registration
                            registerDriver();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Handle user disagreement
                        // You may choose to close the app or restrict functionality
                        currentStep = STEP_FOUR;
                        Toast.makeText(RegisterDriver.this, "Registration Cancelled", Toast.LENGTH_SHORT).show();
                    }
                })
                .setCancelable(true);

        // Show dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            String fileName = getFileName(imageUri);
            if (fileName != null) {
                textViewFileName.setText(fileName);
            } else {
                textViewFileName.setText("Unknown file");
            }
        } else {
            textViewFileName.setText("Selection canceled");
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    private void registerDriver() {
        loadingDialogUtil.showLoadingDialog();

        String emails = email.getText().toString().trim();
        String passwordInput = password.getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(emails, passwordInput)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();
                            uploadImageToFirebase(uid, emails, passwordInput);
                        }
                    } else {
                        Toast.makeText(RegisterDriver.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        loadingDialogUtil.hideLoadingDialog();
                    }
                });
    }

    private void uploadImageToFirebase(String uid, String email, String password) {
        if (imageUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference plateImageRef = storageRef.child("plate_images/" + uid + ".png");

            plateImageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        plateImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String plateImageUrl = uri.toString();
                            InputStream profileImageStream = getImageInputStreamFromDrawable(R.drawable.profile_user);
                            uploadProfileImageToStorage(profileImageStream, uid, email, password, plateImageUrl);
                        }).addOnFailureListener(e -> {
                            Log.e("RegisterDriver", "Error getting plate image download URL", e);
                            loadingDialogUtil.hideLoadingDialog();
                        });
                    }).addOnFailureListener(e -> {
                        Log.e("RegisterDriver", "Error uploading plate image", e);
                        loadingDialogUtil.hideLoadingDialog();
                    });
        } else {
            Log.e("RegisterDriver", "No plate image selected");
            loadingDialogUtil.hideLoadingDialog();
        }
    }

    private void uploadProfileImageToStorage(InputStream imageStream, String uid, String email, String password, String plateImageUrl) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference profileImageRef = storageRef.child("profile_images/" + uid + ".png");

        profileImageRef.putStream(imageStream)
                .addOnSuccessListener(taskSnapshot -> {
                    profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String profileImageUrl = uri.toString();
                        DriverGetterSetter newDriver = createDriverObject(email, password, uid, plateImageUrl, profileImageUrl);
                        runRegistrationTransaction(newDriver);
                    }).addOnFailureListener(e -> {
                        Log.e("RegisterDriver", "Error getting profile image download URL", e);
                        loadingDialogUtil.hideLoadingDialog();
                    });
                }).addOnFailureListener(e -> {
                    Log.e("RegisterDriver", "Error uploading profile image", e);
                    loadingDialogUtil.hideLoadingDialog();
                });
    }
    private DriverGetterSetter createDriverObject(String email, String password, String uid, String plateImageUrl, String profileImageUrl) {
        String name = firstName.getText().toString().trim() + " " + lastName.getText().toString().trim();
        String barangay = (String) signUpBarangay.getSelectedItem();
        double initialBalance = 0.00;
        String role = "Driver";
        boolean inQueue = false;

        return new DriverGetterSetter(
                uid, profileImageUrl, name, barangay, tricycleNumber.getText().toString().trim(), email,
                phoneNumber.getText().toString().trim(), plateImageUrl, initialBalance, role, inQueue
        );
    }

    private void runRegistrationTransaction(DriverGetterSetter driver) {
        db.runTransaction(transaction -> {
                    DocumentReference userRef = db.collection("users").document(driver.uid);
                    DocumentReference barangayRef = db.collection("barangays").document(driver.barangay);
                    CollectionReference driversInBarangayRef = barangayRef.collection("drivers");
                    DocumentReference barangayDriverRef = driversInBarangayRef.document(driver.uid);

                    transaction.set(userRef, driver);

                    Map<String, Object> driverInfo = new HashMap<>();
                    driverInfo.put("uid", driver.uid);
                    driverInfo.put("Name", driver.name);
                    driverInfo.put("TricycleNumber", driver.tricycleNumber);
                    driverInfo.put("inQueue", driver.inQueue);
                    transaction.set(barangayDriverRef, driverInfo);

                    return null;
                }).addOnSuccessListener(aVoid -> generateAndStoreQrCode(driver))
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterDriver.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                    loadingDialogUtil.hideLoadingDialog();
                });
    }

    private void generateAndStoreQrCode(DriverGetterSetter driver) {
        try {
            String driverInfo = "uid: " + driver.uid;
            String encryptedDriverInfo = androidUtil.encrypt(driverInfo, SECRET_KEY);

            Bitmap qrCode = androidUtil.generateQRCode(encryptedDriverInfo);

            if (qrCode != null) {
                uploadQrCodeToStorage(driver.uid, qrCode);
            } else {
                Log.e("RegisterDriver", "Failed to generate QR code");
                loadingDialogUtil.hideLoadingDialog();
            }
        } catch (Exception e) {
            Log.e("RegisterDriver", "Error encrypting driver info", e);
            loadingDialogUtil.hideLoadingDialog();
        }
    }

    private void uploadQrCodeToStorage(String driverUid, Bitmap qrCode) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        qrCode.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] qrCodeBytes = baos.toByteArray();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference qrCodeRef = storageRef.child("qrcodes/" + driverUid + ".png");

        qrCodeRef.putBytes(qrCodeBytes).addOnSuccessListener(taskSnapshot -> {
            qrCodeRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();
                saveQrCodeUrlToFirestore(driverUid, downloadUrl);
            }).addOnFailureListener(e -> {
                Log.e("RegisterDriver", "Error getting QR code download URL", e);
                loadingDialogUtil.hideLoadingDialog();
            });
        }).addOnFailureListener(e -> {
            Log.e("RegisterDriver", "Error uploading QR code", e);
            loadingDialogUtil.hideLoadingDialog();
        });
    }

    private void saveQrCodeUrlToFirestore(String driverUid, String downloadUrl) {
        Map<String, Object> qrCodeData = new HashMap<>();
        qrCodeData.put("qrCodeUrl", downloadUrl);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("qr_code").document(driverUid).set(qrCodeData).addOnSuccessListener(aVoid -> {
            loadingDialogUtil.hideLoadingDialog();
            Intent intent = new Intent(RegisterDriver.this, LoginDriver.class);
            startActivity(intent);
            Toast.makeText(RegisterDriver.this, "Registration successful", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            Log.e("RegisterDriver", "Error storing QR code URL", e);
            loadingDialogUtil.hideLoadingDialog();
        });
    }


    public InputStream getImageInputStreamFromDrawable(int drawableId) {
        return getResources().openRawResource(drawableId);
    }


}

