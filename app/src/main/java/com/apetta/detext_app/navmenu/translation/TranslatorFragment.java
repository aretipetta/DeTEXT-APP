package com.apetta.detext_app.navmenu.translation;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import android.widget.Toast;

import com.apetta.detext_app.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.time.LocalDate;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TranslatorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TranslatorFragment extends Fragment {

    Spinner dropdownSrcLang, dropdownTargetLang;
    EditText srcText, targetText;
    Button translateBtn;
    FirebaseDatabase database;

    ArrayAdapter<CharSequence> adapter;
    String[] languagesTags;
    int srcLangPosition, targetLangPosition;
    // ta 2 prwta einai gia to translation
    String srcLangTag, targetLangTag;



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
//                        && !srcText.getText().toString().matches("^[0-9]$")) {
                    translateBtn.setVisibility(View.VISIBLE);
                }
                else translateBtn.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                targetText.setText("Please, wait.");
                translateWord();
            }
        });

        dropdownSrcLang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                srcLangTag = languagesTags[position];
                Toast.makeText(getContext(), "src lang = " + srcLangTag, Toast.LENGTH_SHORT).show();
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
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.fromLanguageTag(srcLangTag))
                .setTargetLanguage(TranslateLanguage.fromLanguageTag(targetLangTag))
                .build();
        final Translator translator = Translation.getClient(options);
        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();
        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        translator.translate(srcText.getText().toString())
                                .addOnSuccessListener(new OnSuccessListener<String>() {
                                    @Override
                                    public void onSuccess(String s) {
                                        targetText.setText(s);
                                    }
                                })
                                .addOnFailureListener(e -> { })
                                .addOnCompleteListener(task -> {
                                    saveTranslationStats();
                                });
                    }
                })
                .addOnFailureListener(e -> { });
    }

    public void saveTranslationStats() {
        TranslationStats translationStats = new TranslationStats("somewhere", LocalDate.now().getMonth().toString(),
                srcText.getText().toString(), targetText.getText().toString(),
                dropdownSrcLang.getSelectedItem().toString(), dropdownTargetLang.getSelectedItem().toString());
        database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = database.getReference("stats/fromTranslation/");
        dbRef.push().setValue(translationStats)
                .addOnSuccessListener(view -> {
//                     TODO: na kanei den kserw ti apla to kitrinizw gia na kserw oti kati prepei na ftiaksw
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {}
                });
    }
}