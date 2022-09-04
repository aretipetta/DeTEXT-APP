package com.apetta.detext_app.navmenu.detection;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.apetta.detext_app.R;

import java.io.ByteArrayOutputStream;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainDetectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainDetectionFragment extends Fragment {

    ImageView imgToBeDetected;
    Uri selectedImageUri;
    ActivityResultLauncher<Intent> resultLauncherForGallery, resultLauncherForCamera;
    Button openGalleryButton, detectImgButton, openCameraButton, captureFrameButton;
    ImageButton removeImgButton;
    private final String[] Permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private final int[] requestCodes = {123, 112};
//    private static final int cameraRequestCode = 123, storageCode = 112;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MainDetectionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DetectImageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainDetectionFragment newInstance(String param1, String param2) {
        MainDetectionFragment fragment = new MainDetectionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_detection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        askForCameraAndStoragePermission();
        imgToBeDetected = view.findViewById(R.id.imgToBeDetected);
        openGalleryButton = view.findViewById(R.id.openGalleryButton);
        openGalleryButton.setVisibility(View.VISIBLE);
        captureFrameButton = view.findViewById(R.id.captureFrameButton);
        captureFrameButton.setVisibility(View.VISIBLE);
        detectImgButton = view.findViewById(R.id.detectImgButton);
        detectImgButton.setVisibility(View.GONE);
        openCameraButton = view.findViewById(R.id.openCameraButton);
        openCameraButton.setVisibility(View.VISIBLE);
        removeImgButton = view.findViewById(R.id.removeImgButton);
        removeImgButton.setVisibility(View.GONE);
        initResultLauncherForGallery();
        initResultLauncherForCamera();
        setListeners();
    }

    public void setListeners() {
        openGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                resultLauncherForGallery.launch(intent);
            }
        });

        openCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                askForCameraAndStoragePermission();
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                resultLauncherForCamera.launch(intent);
            }
        });


        detectImgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ImageResultsActivity.class);
                intent.setData(selectedImageUri);
                startActivity(intent);
            }
        });

        removeImgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedImageUri = null;
                imgToBeDetected.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.upload_img));
                detectImgButton.setVisibility(View.GONE);
                openGalleryButton.setVisibility(View.VISIBLE);
                captureFrameButton.setVisibility(View.VISIBLE);
                openCameraButton.setVisibility(View.VISIBLE);
                removeImgButton.setVisibility(View.GONE);
            }
        });

        captureFrameButton.setOnClickListener(view -> {
            startActivity(new Intent(getContext(), CaptureVideoActivity.class));
//            startActivity(new Intent(getContext(), CameraXActivity.class));
        });
    }

    public void askForCameraAndStoragePermission() {
        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), Permissions, 1);
//            ActivityCompat.requestPermissions(getActivity(), new String[]
//                    {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        else {
//            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            resultLauncherForCamera.launch(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            resultLauncherForCamera.launch(intent);
        }
    }

    public void initResultLauncherForGallery() {
        resultLauncherForGallery = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            selectedImageUri = data.getData();
                            if (selectedImageUri != null) {
                                imgToBeDetected.setBackground(null);
                                imgToBeDetected.setImageURI(selectedImageUri);
                                detectImgButton.setVisibility(View.VISIBLE);
                                openGalleryButton.setVisibility(View.GONE);
                                captureFrameButton.setVisibility(View.GONE);
                                openCameraButton.setVisibility(View.GONE);
                                removeImgButton.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
    }

    public void initResultLauncherForCamera() {
        resultLauncherForCamera = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if(result.getResultCode() == RESULT_OK) {
                            Bitmap img = (Bitmap) result.getData().getExtras().get("data"); // data.getExtras().get("Data");
                            // apothikeush ths eikonas gia na apokthsei uri
                            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                            img.compress(Bitmap.CompressFormat.JPEG, 100, byteArray);
                            String path = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), img, "Image", null);
                            // twra akolouthei h idia diadikasia me prin
                            selectedImageUri = Uri.parse(path);
                            if (selectedImageUri != null) {
                                imgToBeDetected.setBackground(null);
                                imgToBeDetected.setImageURI(selectedImageUri);
                                detectImgButton.setVisibility(View.VISIBLE);
                                openGalleryButton.setVisibility(View.GONE);
                                captureFrameButton.setVisibility(View.GONE);
                                openCameraButton.setVisibility(View.GONE);
                                removeImgButton.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
        );
    }
}

// auto apo katw gia thn camera pou anixneuei ana frame ti exei klp... --> SurfaceView
// https://developer.android.com/guide/topics/media/camera