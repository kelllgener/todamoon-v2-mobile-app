package com.toda.todamoon_v2.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


public class AndroidUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private Context context;


    // ENCRYPTION UTIL

    public static String encrypt(String data, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes("UTF-8"));
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
    }


    // DATE AND TIME
    public static void startDateTimeUpdater(final TextView view) {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                // Get current date and time
                String currentDateTime = getCurrentDateTime();
                // Update TextView with current date and time
                view.setText(currentDateTime);
                // Call this runnable again after 1 second
                handler.postDelayed(this, 1000);
            }
        });
    }

    public static String getCurrentDateTime() {
        // Get current date and time
        Date currentDate = new Date();
        // Format date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("E | MMM dd, yyyy h:mm:ss a", Locale.getDefault());
        return dateFormat.format(currentDate);
    }



    // METHOD TO HASH THE PASSWORD
    public String hashPassword(String password) {
        try {
            // Create MessageDigest instance for hashing
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // Add password bytes to digest
            md.update(password.getBytes());
            // Get the hashed bytes
            byte[] hashedBytes = md.digest();
            // Convert byte array to Base64 for storage (you can choose a different encoding if needed)
            return java.util.Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null; // Handle error gracefully
        }
    }



    // FUNCTION FOR IMAGE DOWNLOADEER




    // DATE AND TIME FOR TRANSACTIONS
    public static void startDateTimeUpdaterForTransaction(final TextView view) {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                // Get current date and time
                String currentDateTime = getCurrentDateTimeForTransaction();
                // Update TextView with current date and time
                view.setText(currentDateTime);
                // Call this runnable again after 1 second
                handler.postDelayed(this, 1000);
            }
        });
    }

    public static String getCurrentDateTimeForTransaction() {
        // Get current date and time
        Date currentDate = new Date();
        // Format date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("E | MMM dd, yyyy h:mm:ss a", Locale.getDefault());
        return dateFormat.format(currentDate);
    }

    // Convert to QR CODE
    public Bitmap generateQRCode(String text) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bmp;
        } catch (WriterException e) {
            Log.e("RegisterDriver", "Error generating QR code", e);
            return null;
        }
    }








}
