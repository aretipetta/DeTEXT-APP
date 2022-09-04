package com.apetta.detext_app.navmenu.detection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.apetta.detext_app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class ImageResultsActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseDatabase database;
//    static DetectImage detectImage;


    Uri imgUri;
    Bitmap imgBitmap;
    ArrayList<String> textOfBlocks;
    ArrayList<String> languageOfBlocks;
    ArrayList<String> translatedTexts;
//    ArrayList<Integer> invalidIndexes;
//    ArrayList<Integer> validIndexes; // oi theseis tou textOfBlocks stis opoies to keimeno mporei na metafrastei
//    final ArrayList<String> supportedLanguages =
//        new ArrayList<>(Arrays.asList(getApplicationContext().getResources().getStringArray(R.array.supported_languages)));
    final ArrayList<String> supportedLanguages = new ArrayList<>(
            Arrays.asList("af", "sq", "ca", "hr", "cs", "da", "nl", "en", "et", "fil", "tl", "fi", "fr", "de", "hu", "is",
                    "id", "it", "lv", "lt", "ms", "mo", "pl", "pt", "ro", "sr-Latn", "sk", "sl", "es", "sv", "tr", "vi"));
    TextView totalNumberOfBlocks, numberOfBlock, originalTextOfBlock, translatedTextOfBlock, languageOfBlockTextView;
    Button showTextsButton, saveButton;
    ImageButton previousBlockButton, nextBlockButton, copyOriginalText, copyTranslatedText, fbButton, instaButton, twitterButton;
    ScrollView scrollView;
    ImageView img;
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    int countBlocks;
   // final PackageManager packageManager = getPackageManager();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_results);

        totalNumberOfBlocks = findViewById(R.id.totalNumberOfBlocks);
        totalNumberOfBlocks.setText("Processing. Please, wait.");
        numberOfBlock = findViewById(R.id.numberOfBlock);
        originalTextOfBlock = findViewById(R.id.originalTextOfBlock);
        translatedTextOfBlock = findViewById(R.id.translatedTextOfBlock);
        showTextsButton = findViewById(R.id.showTextsButton);
        showTextsButton.setVisibility(View.INVISIBLE);
        scrollView = findViewById(R.id.scrollViewInImageResults);
        scrollView.setVisibility(View.INVISIBLE);
        previousBlockButton = findViewById(R.id.previousBlockButton);
        previousBlockButton.setVisibility(View.INVISIBLE);
        nextBlockButton = findViewById(R.id.nextBlockButton);
        nextBlockButton.setVisibility(View.INVISIBLE);
        languageOfBlockTextView = findViewById(R.id.languageOfBlock);
        copyOriginalText = findViewById(R.id.copyOriginalText);
        copyTranslatedText = findViewById(R.id.copyTranslatedText);
        fbButton = findViewById(R.id.fbButton);
        instaButton = findViewById(R.id.instaButton);
        twitterButton = findViewById(R.id.twitterButton);
        saveButton = findViewById(R.id.saveButton);
        img = findViewById(R.id.uploadedImg);
        // get data from intent
        byte[] byteArray = getIntent().getByteArrayExtra("bitmap");
        imgBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
//        imgUri = getIntent().getData();
        img.setImageURI(imgUri);
        extractTextFromImage(imgBitmap);
        setListeners();
    }

    public void setListeners() {
        showTextsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countBlocks = 0;
                // στο πάτημα εμφανίζει μόνο το 1ο block
                showTextsButton.setVisibility(View.INVISIBLE);
                // an exei na emfanisei ki alla tote emfanizei to koympi tou epomenou
                if(textOfBlocks.size() > 1) {
//                if(detectImage.getTextOfBlocks().size() > 1) {
                    nextBlockButton.setVisibility(View.VISIBLE);

                }
                showBlockDetails();
                scrollView.setVisibility(View.VISIBLE);
            }
        });

        previousBlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // prwta svhnei ta prohgoumena keimena
                numberOfBlock.setText(null);
                originalTextOfBlock.setText(null);
                translatedTextOfBlock.setText(null);
                // an to countBlocks == 0 tote prepei na eksafanistei giati den exei allo pio prin
                countBlocks--;
                if(countBlocks == 0) {
                    previousBlockButton.setVisibility(View.INVISIBLE);
                    nextBlockButton.setVisibility(View.VISIBLE);
                }
                if(countBlocks == textOfBlocks.size() - 2)  nextBlockButton.setVisibility(View.VISIBLE);
//                if(countBlocks == detectImage.getTextOfBlocks().size() - 2)  nextBlockButton.setVisibility(View.VISIBLE);
                showBlockDetails();
            }
        });

        nextBlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // prwta svhnei ta prohgoumena keimena
                numberOfBlock.setText(null);
                originalTextOfBlock.setText(null);
                translatedTextOfBlock.setText(null);
                // an to countBlocks == textOfBlocks.size() tote eksafanizetai to nextButton
                countBlocks++;
                if(countBlocks == textOfBlocks.size() - 1) {
//                if(countBlocks == detectImage.getTextOfBlocks().size() - 1) {
                    nextBlockButton.setVisibility(View.INVISIBLE);
                }
                // sto 1 prepei na emfanistei to previous
                if(countBlocks == 1) previousBlockButton.setVisibility(View.VISIBLE);
                showBlockDetails();
            }
        });

        copyOriginalText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("OriginalText", originalTextOfBlock.getText().toString());
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(ImageResultsActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });

        copyTranslatedText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("TranslatedText", translatedTextOfBlock.getText().toString());
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(ImageResultsActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });

        fbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAnotherAppIfExists("com.facebook", "https://www.facebook.com/");
            }
        });

        instaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAnotherAppIfExists("com.instagram.android", "https://www.instagram.com/");
            }
        });

        twitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAnotherAppIfExists("com.twitter.android", "https://twitter.com/");
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: create new object 'SavedImage' and save it to database
                // uri to string --> uri.toString()
                //string to uri  --> Uri.parse(str)
                mAuth = FirebaseAuth.getInstance();
                database = FirebaseDatabase.getInstance();
                DatabaseReference dbRef = database.getReference("history/" + mAuth.getCurrentUser().getUid()).push();
                String storagePath = "history/" + mAuth.getCurrentUser().getUid() + "/" + dbRef.getKey() + "img";
                StorageReference sRef = FirebaseStorage.getInstance().getReference().child(storagePath);
                sRef.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // imgUri.toString(),    <- on new SavedImage...
                        dbRef.setValue(new SavedImage(storagePath,textOfBlocks, translatedTexts, languageOfBlocks, formatter.format(new Date())))
//                        dbRef.setValue(new SavedImage(storagePath,detectImage.getTextOfBlocks(), detectImage.getTranslatedTexts(),
//                                        detectImage.getLanguageOfBlocks(), formatter.format(new Date())))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(ImageResultsActivity.this, "Image saved", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(ImageResultsActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
            }
        });
    }

    public void extractTextFromImage(Bitmap bitmap) { // Uri uri
        textOfBlocks = new ArrayList<>();
        languageOfBlocks = new ArrayList<>();
        translatedTexts = new ArrayList<>();
//        DetectImage detectImage = new DetectImage();
////        detectImage.extractTextFromImage(this, uri);
//        detectImage.extractTextFromImage(this, bitmap);
//        if(detectImage.getFoundText()) {
//            detectImage.translateText();
//            Log.d("ielaaa", "in image result... size = " + detectImage.getTextOfBlocks().size());
//            if(detectImage.isReady()) {
//            textOfBlocks = detectImage.getTextOfBlocks();
//            languageOfBlocks = detectImage.getLanguageOfBlocks();
//            translatedTexts = detectImage.getTranslatedTexts();
//                showTextsButton.setVisibility(View.VISIBLE);
//                totalNumberOfBlocks.setText("Press 'Show' for details.");
//            }
//        }





        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        // TODO: edw exw vgalei to try-catch epd den xreiazetai otan to inputImage exei bitmap anti gia uri
//        try {
            // XRHSIMOOOOOOOOOO για text rec
//            https://developers.google.com/ml-kit/vision/text-recognition/android#java
            // identify language
            // https://developers.google.com/ml-kit/language/identification/android
            // gia translation
            // https://developers.google.com/ml-kit/language/translation/android
            /** TODO
             * prwta vriskei to keimeno kai thn glwssa. An h glwssa einai egkurh prosthetei to block se mia lista.
             * Sto telos, afou mazepsei ola ta blocks, kanei thn metafrash gia to kathena
             */
//            InputImage inputImage = InputImage.fromFilePath(getApplicationContext(), uri);
            InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
            Task<Text> result = recognizer.process(inputImage)
                    .addOnCompleteListener(new OnCompleteListener<Text>() {
                        @Override
                        public void onComplete(@NonNull Task<Text> task) {
                            // kanei thn metafrash sthn onComplete...?
//                            translateText();

                            textOfBlocks = new ArrayList<>();
                            for(Text.TextBlock block : task.getResult().getTextBlocks()) {
                                textOfBlocks.add(block.getText());
                            }
                            if(textOfBlocks.size() > 0) {
                                Log.d("ielaaa", "sthn image result to vrhke me bitmap. palios kwdikas");
                                translateText();
                            }

//                            translatedTexts = new ArrayList<>();
//                            languageOfBlocks = new ArrayList<>();
//                            for(int i = 0; i < textOfBlocks.size(); i++) {
//                                translateText(textOfBlocks.get(i));
//                            }
                            // TODO: den douleuei touto
//                            Toast.makeText(ImageResultsActivity.this, "translated texts size = " + translatedTexts.size(), Toast.LENGTH_SHORT).show();


//                            textOfBlocks = new ArrayList<>();
//                            // vriskei gia kathe block thn glwssa tou
//                            // epd tha ta kanei ola pros ta mesa (emfwlevmena) tha prosthetei to keimeno prwta ki an den mporei na to metafrasei tha to afairei
//                            for(Text.TextBlock block : task.getResult().getTextBlocks()) {
//                                textOfBlocks.add(block.getText());
//                            }
//                            Toast.makeText(ImageResultsActivity.this, "after for loop", Toast.LENGTH_SHORT).show();
//                            if(textOfBlocks.size() != 0) {
//                                if(textOfBlocks.size() == 1) totalNumberOfBlocks.setText("Only one block was found");
//                                else totalNumberOfBlocks.setText(textOfBlocks.size() + " blocks were found");
//                                showTextsButton.setVisibility(View.VISIBLE);
//                            }
//                            else {
//                                totalNumberOfBlocks.setText("No text found.");
//                                showTextsButton.setVisibility(View.INVISIBLE);
//                            }
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<Text>() {
                        @Override
                        public void onSuccess(Text text) {
//                            textOfBlocks = new ArrayList<>();
//                            // vriskei gia kathe block thn glwssa tou
//                            // epd tha ta kanei ola pros ta mesa (emfwlevmena) tha prosthetei to keimeno prwta ki an den mporei na to metafrasei tha to afairei
//                            for(Text.TextBlock block : text.getTextBlocks()) {
//                                textOfBlocks.add(block.getText());
//                            }
//                            if(textOfBlocks.size() != 0) {
//                                if(textOfBlocks.size() == 1) totalNumberOfBlocks.setText("Only one block was found");
//                                else totalNumberOfBlocks.setText(textOfBlocks.size() + " blocks were found");
//                                showTextsButton.setVisibility(View.VISIBLE);
//                            }
//                            else {
//                                totalNumberOfBlocks.setText("No text found.");
//                                showTextsButton.setVisibility(View.INVISIBLE);
//                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
//                            Toast.makeText(ImageResultsActivity.this, "Not found...", Toast.LENGTH_SHORT).show();
                        }
                    });
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
    }


    public void showBlockDetails() {
        numberOfBlock.setText("Block " + (countBlocks + 1));
        languageOfBlockTextView.setText(languageOfBlocks.get(countBlocks));
        Log.d("ielaaa", "details sz = " + languageOfBlocks.size());
//        languageOfBlockTextView.setText(detectImage.getLanguageOfBlocks().get(countBlocks));
        originalTextOfBlock.setText(textOfBlocks.get(countBlocks));
//        originalTextOfBlock.setText(detectImage.getTextOfBlocks().get(countBlocks));
        translatedTextOfBlock.setText(translatedTexts.get(countBlocks));
//        translatedTextOfBlock.setText(detectImage.getTranslatedTexts().get(countBlocks));
    }

    // kanei metafrash kai ta krataei ola se listes
    public void translateText() {
        languageOfBlocks = new ArrayList<>();
        translatedTexts = new ArrayList<>();
        LanguageIdentifier languageIdentifier = LanguageIdentification.getClient();
        for(int i = 0; i < textOfBlocks.size(); i++) {
            int j = i;
            languageIdentifier.identifyLanguage(textOfBlocks.get(i))
                    .addOnSuccessListener(new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String lang) {
                            if(supportedLanguages.contains(lang)) {
                                // uparxei kai sunexizei sth metafrash
                                TranslatorOptions options = new TranslatorOptions.Builder()
                                        .setSourceLanguage(TranslateLanguage.fromLanguageTag(lang))
                                        .setTargetLanguage(TranslateLanguage.GREEK)
                                        .build();
                                final Translator translator = Translation.getClient(options);
                                DownloadConditions conditions = new DownloadConditions.Builder()
                                        .requireWifi()
                                        .build();
                                translator.downloadModelIfNeeded(conditions)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                // Model downloaded successfully. Okay to start translating.
                                                // (Set a flag, unhide the translation UI, etc.)
                                                translator.translate(textOfBlocks.get(j))
                                                        .addOnSuccessListener(new OnSuccessListener<String>() {
                                                            @Override
                                                            public void onSuccess(String s) {
                                                                Toast.makeText(ImageResultsActivity.this, "ekane metafrash", Toast.LENGTH_SHORT).show();
                                                                // edw apothikeuei to keimeno, th glwssa kai thn metafrash
                                                                languageOfBlocks.add(Locale.forLanguageTag(lang).getDisplayLanguage());
                                                                translatedTexts.add(s);
                                                                if(languageOfBlocks.size() == textOfBlocks.size() && textOfBlocks.size() == translatedTexts.size()) {
                                                                    // mono tote tha emfanistei to koumpi gia thn emfanish apotelesmatwn
                                                                    showTextsButton.setVisibility(View.VISIBLE);
                                                                    totalNumberOfBlocks.setText("Press 'Show' for details.");
                                                                }
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                languageOfBlocks.add(Locale.forLanguageTag(lang).getDisplayLanguage());
                                                                translatedTexts.add(textOfBlocks.get(j));
                                                                if(languageOfBlocks.size() == textOfBlocks.size() && textOfBlocks.size() == translatedTexts.size()) {
                                                                    // mono tote tha emfanistei to koumpi gia thn emfanish apotelesmatwn
                                                                    showTextsButton.setVisibility(View.VISIBLE);
                                                                    totalNumberOfBlocks.setText("Press 'Show' for details.");
                                                                }
                                                            }
                                                        });
                                            }
                                        });
                            }
                            else {
                                languageOfBlocks.add("Unrecognized language");
                                translatedTexts.add(textOfBlocks.get(j));
                                if(languageOfBlocks.size() == textOfBlocks.size() && textOfBlocks.size() == translatedTexts.size()) {
                                    // mono tote tha emfanistei to koumpi gia thn emfanish apotelesmatwn
                                    showTextsButton.setVisibility(View.VISIBLE);
                                    totalNumberOfBlocks.setText("Press 'Show' for details.");
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            languageOfBlocks.add("Unrecognized language");
                            translatedTexts.add(textOfBlocks.get(j));
                            if(languageOfBlocks.size() == textOfBlocks.size() && textOfBlocks.size() == translatedTexts.size()) {
                                // mono tote tha emfanistei to koumpi gia thn emfanish apotelesmatwn
                                showTextsButton.setVisibility(View.VISIBLE);
                                totalNumberOfBlocks.setText("Press 'Show' for details.");
                            }
                        }
                    });
        }
    }

    public void openAnotherAppIfExists(String packageName, String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage(packageName);
        try{
            startActivity(intent);
        }
        catch(ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        }
    }
}