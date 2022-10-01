package com.apetta.detext_app.navmenu.detection;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.apetta.detext_app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;

public class LiveCaptionActivity extends CameraActivity {

    private int count_frames;  // counter for frames. detection will be applied every 10 frames

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
        setContentView(R.layout.activity_live_caption);
        openCVCameraView = findViewById(R.id.openCVSurface);
        openCVCameraView.setVisibility(SurfaceView.VISIBLE);
        openCVCameraView.setCvCameraViewListener(cameraViewListener2);
        count_frames = 0;
    }


    private CameraBridgeViewBase.CvCameraViewListener2 cameraViewListener2 = new CameraBridgeViewBase.CvCameraViewListener2() {
        @Override
        public void onCameraViewStarted(int width, int height) { }

        @Override
        public void onCameraViewStopped() { }

        @Override
        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
            /*checks for text every 10 frames*/
            if(count_frames == 10){
                Mat realMat = rotateFrame(inputFrame.rgba());   // rotate mat
                Bitmap bitmap = matToBitmap(realMat);   // get bitmap from mat
                getTextFromImage(bitmap);
                count_frames = 0;
                return inputFrame.rgba();
            }
            else count_frames ++;
            return null;
        }
    };


    /**
     * This method rotates the input mat
     * @param mat matrix
     * @return
     */
    public Mat rotateFrame(Mat mat) {
        Mat rotatedMat = new Mat();
        Core.rotate(mat, rotatedMat, Core.ROTATE_90_CLOCKWISE);
        return rotatedMat;
    }

    /**
     * Convert a Mat to a Bitmap
     * @param mat
     * @return bitmap converted by mat
     */
    public Bitmap matToBitmap(Mat mat) {
        final Mat rgba = mat;
        final Bitmap bitmap = Bitmap.createBitmap(rgba.cols(), rgba.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rgba, bitmap, true);
        return bitmap;
    }

    /**
     * Redirect to new activity if any text is found on image
     * @param bitmap
     */
    public void getTextFromImage(Bitmap bitmap) {
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
        Task<Text> result = recognizer.process(inputImage)
                .addOnCompleteListener(task -> {
                    if(task.getResult().getTextBlocks().size() > 0) {
                        Intent intent = new Intent(getApplicationContext(), ImageResultsActivity.class);
                        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bStream);
                        byte[] byteArray = bStream.toByteArray();
                        intent.putExtra("bitmap", byteArray);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnSuccessListener(text -> { })
                .addOnFailureListener(e -> { });
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
    }
}