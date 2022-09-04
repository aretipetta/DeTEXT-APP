package com.apetta.detext_app.navmenu.detection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.view.PreviewView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Size;

import com.apetta.detext_app.R;

import java.io.ByteArrayOutputStream;

public class CameraXActivity extends AppCompatActivity implements ImageAnalysis.Analyzer{

    private PreviewView previewView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_xactivity);
        previewView = findViewById(R.id.cameraX);
    }

    /**
     * 1. https://www.youtube.com/watch?v=IrwhjDtpIU0
     * 2. https://www.youtube.com/watch?v=4vv2PtfdWRQ
     * to 2o einai shmantiko mallon
     */
    @Override
    public void analyze(@NonNull ImageProxy image) {
        // image processing for the current frame
//        if (image.getImage() == null) return;
        if(previewView.getBitmap() == null) return;
        DetectImage detectImage = new DetectImage();
        // bitmap to uri
        Uri uri = bitmapToUri(getApplicationContext(), previewView.getBitmap());
        detectImage.extractTextFromImage(getApplicationContext(), previewView.getBitmap());
        deleteFileByUri(uri);
        if(detectImage.getFoundText()) {
            // TODO: na stamataei kai na anoigei neo activity me to uri pou prepei na ftiaksei...
//                    Toast.makeText(CaptureVideoActivity.this, "vrhke keimeno", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), ImageResultsActivity.class));
            onStop();
        }
    }

    private Uri bitmapToUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        return Uri.parse(MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "tempFile", null));
    }

    private void deleteFileByUri(Uri uri) {
        getContentResolver().delete(uri, null, null);
    }


    @SuppressLint("UnsafeOptInUsageError")
    @Nullable
    @Override
    public Size getTargetResolutionOverride() {
        return ImageAnalysis.Analyzer.super.getTargetResolutionOverride();
    }

    @SuppressLint("UnsafeOptInUsageError")
    @Override
    public int getTargetCoordinateSystem() {
        return ImageAnalysis.Analyzer.super.getTargetCoordinateSystem();
    }

    @SuppressLint("UnsafeOptInUsageError")
    @Override
    public void updateTransform(@Nullable Matrix matrix) {
        ImageAnalysis.Analyzer.super.updateTransform(matrix);
    }

}