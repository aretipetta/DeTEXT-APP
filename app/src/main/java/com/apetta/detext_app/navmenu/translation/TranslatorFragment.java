package com.apetta.detext_app.navmenu.translation;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.apetta.detext_app.R;
import com.apetta.detext_app.alertDialog.ProgressAlertDialog;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TranslatorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TranslatorFragment extends Fragment {

    Spinner dropdownSrcLang, dropdownTargetLang;
    EditText srcText, targetText;
    Button translateBtn;
//    ConstraintLayout translatorConstrLayout;
    FirebaseDatabase database;

    ArrayAdapter<CharSequence> adapter;
    String[] languagesTags;
    int srcLangPosition, targetLangPosition;
    String srcLangTag, targetLangTag, sourceWord, translatedWord;

    LocationManager locationManager;
    LocationListener locationListener;
    String country, locality;

    ProgressAlertDialog progressAlertDialog;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TranslatorFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TranslatorFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TranslatorFragment newInstance(String param1, String param2) {
        TranslatorFragment fragment = new TranslatorFragment();
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
        return inflater.inflate(R.layout.fragment_translator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        srcText = view.findViewById(R.id.srcText);
        targetText = view.findViewById(R.id.targetText);
        translateBtn = view.findViewById(R.id.translateButton);
        translateBtn.setVisibility(View.INVISIBLE);
        dropdownSrcLang = view.findViewById(R.id.dropdownSrcLang);
        dropdownTargetLang = view.findViewById(R.id.dropdownTargetLang);
//        translatorConstrLayout = view.findViewById(R.id.translatorConstrLayout);
        languagesTags = getContext().getResources().getStringArray(R.array.languages_tags);
        srcLangPosition = 0;
        targetLangPosition = 0;
        setAdaptersToDropdowns();
        setListeners();
    }

    public void setAdaptersToDropdowns() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapter = ArrayAdapter.createFromResource(getContext(), R.array.languages, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        dropdownSrcLang.setAdapter(adapter);
        dropdownTargetLang.setAdapter(adapter);
    }

    public void setListeners() {
        srcText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(srcText.getText().toString().length() != 0 && !srcText.getText().toString().trim().equals("")
                        && !srcText.getText().toString().matches(".*\\d.*")) {
                    translateBtn.setVisibility(View.VISIBLE);
                }
                else translateBtn.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        translateBtn.setOnClickListener(v -> {
            sourceWord = srcText.getText().toString();
            targetText.setText("...");
            translateWord();
        });

        dropdownSrcLang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                srcLangTag = languagesTags[position];
                srcLangPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        dropdownTargetLang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                targetLangTag = languagesTags[position];
                targetLangPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    public void translateWord() {
        translateBtn.setEnabled(false);
        progressAlertDialog = new ProgressAlertDialog(getContext(), getString(R.string.wait));
        progressAlertDialog.show();
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.fromLanguageTag(srcLangTag))
                .setTargetLanguage(TranslateLanguage.fromLanguageTag(targetLangTag))
                .build();
        final Translator translator = Translation.getClient(options);
        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();
        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(unused -> translator.translate(sourceWord)
                        .addOnSuccessListener(s -> translatedWord = s)
                        .addOnFailureListener(e -> { })
                        .addOnCompleteListener(task -> {
                            getCountryAndLocality();
                        }))
                .addOnFailureListener(e -> {
                    progressAlertDialog.dismiss();
                    new AlertDialog.Builder(getContext())
                            .setTitle(getString(R.string.failed))
                            .setMessage(getString(R.string.transl_failed))
                            .show();
                });
    }

    private void getCountryAndLocality() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            saveTranslationStats();
            return;
        }
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                country = null;
                locality = null;
                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                List<Address> addressList;
                try {
                    addressList = geocoder.getFromLocation(location.getLatitude(), location.getLatitude(), 1);
                    country = addressList.get(0).getCountryName();
                    locality = addressList.get(0).getLocality();
                    saveTranslationStats();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {}

            @Override
            public void onProviderEnabled(String s) {}

            @Override
            public void onProviderDisabled(String s) {}
        };

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            progressAlertDialog.dismiss();
            targetText.setText(translatedWord);
            translateBtn.setEnabled(true);
            return;
        }
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, Looper.myLooper());
        else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, Looper.myLooper());
    }

    private void saveTranslationStats() {
        TranslationObject translationObject = new TranslationObject(country, locality, LocalDate.now().getMonth().toString(),
                Integer.toString(LocalDate.now().getYear()), sourceWord.toLowerCase(), translatedWord.toLowerCase(),
                dropdownSrcLang.getSelectedItem().toString(), dropdownTargetLang.getSelectedItem().toString());
        database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = database.getReference("stats/fromTranslation/");
        dbRef.push().setValue(translationObject)
                .addOnSuccessListener(view -> {
                    progressAlertDialog.dismiss();
                    targetText.setText(translatedWord);
                    translateBtn.setEnabled(true);
                })
                .addOnFailureListener(e -> { progressAlertDialog.dismiss(); });
    }
}