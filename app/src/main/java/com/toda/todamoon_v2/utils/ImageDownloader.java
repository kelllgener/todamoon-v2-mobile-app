package com.toda.todamoon_v2.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class ImageDownloader {
    private Context context;

    public ImageDownloader(Context context) {
        this.context = context;
    }

    public void downloadAndSaveImage(StorageReference storageReference) {
        try {
            File localFile = File.createTempFile("qr_code", "png");

            storageReference.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            addImageToGallery(localFile);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Failed to download QR code", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addImageToGallery(File imageFile) {
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

        String savedImagePath = MediaStore.Images.Media.insertImage(
                context.getContentResolver(),
                bitmap,
                "QR Code",
                "Generated QR Code"
        );

        // Notify user of image save status
        if (savedImagePath != null) {
            Toast.makeText(context, "QR code saved to gallery", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed to save QR code to gallery", Toast.LENGTH_SHORT).show();
        }

        // Refresh gallery
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)));
    }
}
