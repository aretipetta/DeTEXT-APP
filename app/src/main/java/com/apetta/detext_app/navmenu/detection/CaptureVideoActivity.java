package com.apetta.detext_app.navmenu.detection;

import androidx.appcompat.app.AppCompatActivity;
import com.apetta.detext_app.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;

public class CaptureVideoActivity extends CameraActivity {

    private ImageView imgView;
    private int count_frames;

    private CameraBridgeViewBase openCVCameraView;
    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    openCVCameraView.enableView();
                }break;
                default:
                {
                    super.onManagerConnected(status);
                }break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_video);
        openCVCameraView = (CameraBridgeViewBase) findViewById(R.id.openCVSurface);
        openCVCameraView.setVisibility(SurfaceView.VISIBLE);
        openCVCameraView.setCvCameraViewListener(cameraViewListener2);
        imgView = findViewById(R.id.imageView);
        count_frames = 0;
    }

    // TODO:OOOOOOOOOOOOOOOOOOO
    private CameraBridgeViewBase.CvCameraViewListener2 cameraViewListener2 = new CameraBridgeViewBase.CvCameraViewListener2() {
        @Override
        public void onCameraViewStarted(int width, int height) {

        }

        @Override
        public void onCameraViewStopped() {

        }

        @Override
        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
            // TODO: se ka8e frame pairnei to inputFrame.rgba() kai to kanei bitmap
            // opws deixnei katw sto stackoverflow
            // https://stackoverflow.com/questions/44579822/convert-opencv-mat-to-android-bitmap
            if(count_frames == 10){
//                Toast.makeText(CaptureVideoActivity.this, "eftase sto 10", Toast.LENGTH_SHORT).show();
                // TODO: tote kanei thn anixneush klp
                Mat realMat = rotateFrame(inputFrame.rgba());   // rotate mat
                // get bitmap from mat
                Bitmap bitmap = matToBitmap(realMat);

                DetectImage detectImage = new DetectImage();
                // bitmap to uri
                Uri uri = bitmapToUri(getApplicationContext(), bitmap);
                detectImage.extractTextFromImage(getApplicationContext(), uri);
                boolean b = false;
//                Toast.makeText(CaptureVideoActivity.this, "frame tade...", Toast.LENGTH_SHORT).show();
                if(detectImage.getFoundText()) {
                    // TODO: na stamataei kai na anoigei neo activity me to uri pou prepei na ftiaksei...
//                    Toast.makeText(CaptureVideoActivity.this, "vrhke keimeno", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), ImageResultsActivity.class);
                    intent.setData(uri);
                    b = true;
                    startActivity(intent);
                    finish();
                }
                if(!b) deleteFileByUri(uri);

//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Looper.prepare();
//                        try {
//                            imgView.setImageBitmap(bitmap);
//                            // TODO: elegxos gia text recognition
////                        DetectImage detectImage = new DetectImage();
////                        // bitmap to uri
////                        Uri uri = bitmapToUri(getApplicationContext(), bitmap);
////                        detectImage.extractTextFromImage(getApplicationContext(), uri);
////                        deleteFileByUri(uri);
////                        Toast.makeText(CaptureVideoActivity.this, "frame tade...", Toast.LENGTH_SHORT).show();
////                        if(detectImage.getFoundText()) {
////                            // TODO: na stamataei kai na anoigei neo activity me to uri pou prepei na ftiaksei...
////                            Toast.makeText(CaptureVideoActivity.this, "vrhke keimeno", Toast.LENGTH_SHORT).show();
////                            startActivity(new Intent(getApplicationContext(), ImageResultsActivity.class));
////                            onPause();
////                        }
//                        }
//                        catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        Looper.loop();
//                    }
//                });
                count_frames = 0;
                return inputFrame.rgba();
            }
            else count_frames ++;
            return null;
        }
    };


    public Mat rotateFrame(Mat mat) {
        Mat rotatedMat = new Mat();
        Core.rotate(mat, rotatedMat, Core.ROTATE_90_CLOCKWISE);
        return rotatedMat;
    }

    public Bitmap matToBitmap(Mat mat) {
        Mat imgSource = mat;
        final Mat rgba = mat;
        final Bitmap bitmap = Bitmap.createBitmap(rgba.cols(), rgba.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rgba, bitmap, true);
        return bitmap;
    }

    public Uri bitmapToUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        return Uri.parse(MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "tempFile", null));
    }

    public void deleteFileByUri(Uri uri) {
        getContentResolver().delete(uri, null, null);
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(openCVCameraView != null) openCVCameraView.disableView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLoaderCallback);
        }
        else baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(openCVCameraView != null) openCVCameraView.disableView();
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(openCVCameraView);
//        return super.getCameraViewList();
    }
}