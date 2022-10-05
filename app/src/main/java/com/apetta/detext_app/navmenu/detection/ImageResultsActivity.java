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
import com.apetta.detext_app.alertDialog.ProgressAlertDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class ImageResultsActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseDatabase database;

    Bitmap imgBitmap;
    ArrayList<String> textOfBlocks;
    ArrayList<String> languageOfBlocks;
    ArrayList<String> translatedTexts;
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
    ProgressAlertDialog progressAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_results);
        totalNumberOfBlocks = findViewById(R.id.totalNumberOfBlocks);
        totalNumberOfBlocks.setText("");
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
        img.setImageBitmap(imgBitmap);
        extractTextFromImage(imgBitmap);
        setListeners();
    }

    /**
     * This method sets listeners to activity's widgets
     */
    public void setListeners() {
        showTextsButton.setOnClickListener(v -> {
            countBlocks = 0;
            showTextsButton.setVisibility(View.INVISIBLE);
            totalNumberOfBlocks.setVisibility(View.INVISIBLE);
            if(textOfBlocks.size() > 1) nextBlockButton.setVisibility(View.VISIBLE);
            showBlockDetails();
            scrollView.setVisibility(View.VISIBLE);
        });

        previousBlockButton.setOnClickListener(v -> {
            numberOfBlock.setText(null);
            originalTextOfBlock.setText(null);
            translatedTextOfBlock.setText(null);
            countBlocks--;
            if(countBlocks == 0) {
                previousBlockButton.setVisibility(View.INVISIBLE);
                nextBlockButton.setVisibility(View.VISIBLE);
            }
            if(countBlocks == textOfBlocks.size() - 2)  nextBlockButton.setVisibility(View.VISIBLE);
            showBlockDetails();
        });

        nextBlockButton.setOnClickListener(v -> {
            numberOfBlock.setText(null);
            originalTextOfBlock.setText(null);
            translatedTextOfBlock.setText(null);
            countBlocks++;
            if(countBlocks == textOfBlocks.size() - 1) {
                nextBlockButton.setVisibility(View.INVISIBLE);
            }
            if(countBlocks == 1) previousBlockButton.setVisibility(View.VISIBLE);
            showBlockDetails();
        });

        copyOriginalText.setOnClickListener(v -> {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("OriginalText", originalTextOfBlock.getText().toString());
            clipboardManager.setPrimaryClip(clipData);
            Toast.makeText(ImageResultsActivity.this, getString(R.string.copied), Toast.LENGTH_SHORT).show();
        });

        copyTranslatedText.setOnClickListener(v -> {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("TranslatedText", translatedTextOfBlock.getText().toString());
            clipboardManager.setPrimaryClip(clipData);
            Toast.makeText(ImageResultsActivity.this, getString(R.string.copied), Toast.LENGTH_SHORT).show();
        });

        fbButton.setOnClickListener(v -> openAnotherAppIfExists("com.facebook", "https://www.facebook.com/"));

        instaButton.setOnClickListener(v -> openAnotherAppIfExists("com.instagram.android", "https://www.instagram.com/"));

        twitterButton.setOnClickListener(v -> openAnotherAppIfExists("com.twitter.android", "https://twitter.com/"));

        saveButton.setOnClickListener(v -> {
            progressAlertDialog = new ProgressAlertDialog(this, getString(R.string.saving));
            progressAlertDialog.show();
            mAuth = FirebaseAuth.getInstance();
            database = FirebaseDatabase.getInstance();
            DatabaseReference dbRef = database.getReference("history/" + mAuth.getCurrentUser().getUid()).push();
            String storagePath = "history/" + mAuth.getCurrentUser().getUid() + "/" + dbRef.getKey();
            StorageReference sRef = FirebaseStorage.getInstance().getReference().child(storagePath);
            ByteArrayOutputStream bStream = new ByteArrayOutputStream();
            imgBitmap.compress(Bitmap.CompressFormat.PNG, 100, bStream);
            byte[] imgToBeSaved = bStream.toByteArray();
            sRef.putBytes(imgToBeSaved)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            dbRef.setValue(new SavedImage(storagePath,textOfBlocks, translatedTexts, languageOfBlocks, formatter.format(new Date())))
                                    .addOnSuccessListener(unused -> progressAlertDialog.dismiss())
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(ImageResultsActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                        progressAlertDialog.dismiss();});
                        }
                    })
                    .addOnSuccessListener(taskSnapshot -> { })
            .addOnFailureListener(e -> {
                Toast.makeText(ImageResultsActivity.this, getString(R.string.could_not_upload), Toast.LENGTH_SHORT).show();
                progressAlertDialog.dismiss();
            });
        });
    }

    /**
     * This method extracts the text from an input image. This image is passed as a bitmap
     * @param bitmap bitmap of the input image
     */
    public void extractTextFromImage(Bitmap bitmap) {
        progressAlertDialog = new ProgressAlertDialog(this, getString(R.string.wait));
        progressAlertDialog.show();
        textOfBlocks = new ArrayList<>();
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
        Task<Text> result = recognizer.process(inputImage)
//                .addOnCompleteListener(task -> {
//
//                })
                .addOnSuccessListener(text -> {
                    textOfBlocks = new ArrayList<>();
                    for(Text.TextBlock block : text.getTextBlocks()) {
                        textOfBlocks.add(block.getText());
                    }
                    if(textOfBlocks.size() > 0) {
                        translateText();
                    }
                    else {
                        showTextsButton.setVisibility(View.INVISIBLE);
                        totalNumberOfBlocks.setText(getString(R.string.found_nothing));
                        progressAlertDialog.dismiss();
                    }
                })
                .addOnFailureListener(e -> {
                    progressAlertDialog.dismiss();
                });
    }

    /**
     * This method shows the details for every block. That means that for each block, it shows the
     * original text of it, the language of this text and the translated text.
     */
    public void showBlockDetails() {
        numberOfBlock.setText(getString(R.string.block) + (countBlocks + 1));
        languageOfBlockTextView.setText(languageOfBlocks.get(countBlocks));
        originalTextOfBlock.setText(textOfBlocks.get(countBlocks));
        translatedTextOfBlock.setText(translatedTexts.get(countBlocks));
    }

    /**
     * This method translates the text of all the blocks that have been detected.
     * The list of the blocks' texts has already been set up. So, for each block's text, this method
     * finds the language of the block's text and after that translates it to the default language
     * which is greek. These data are stored to 2 lists, one for the language of the block's text
     * and one for the translated text.
     */
    public void translateText() {
        languageOfBlocks = new ArrayList<>();
        translatedTexts = new ArrayList<>();
        LanguageIdentifier languageIdentifier = LanguageIdentification.getClient();
        for(int i = 0; i < textOfBlocks.size(); i++) {
            int j = i;
            languageIdentifier.identifyLanguage(textOfBlocks.get(i))
                    .addOnSuccessListener(lang -> {
                        if(supportedLanguages.contains(lang)) {
                            TranslatorOptions options = new TranslatorOptions.Builder()
                                    .setSourceLanguage(TranslateLanguage.fromLanguageTag(lang))
                                    .setTargetLanguage(TranslateLanguage.GREEK)
                                    .build();
                            final Translator translator = Translation.getClient(options);
                            DownloadConditions conditions = new DownloadConditions.Builder()
                                    .requireWifi()
                                    .build();
                            translator.downloadModelIfNeeded(conditions)
                                    .addOnSuccessListener(unused -> {
                                        // Model downloaded successfully. Okay to start translating.
                                        // (Set a flag, unhide the translation UI, etc.)
                                        translator.translate(textOfBlocks.get(j))
                                                .addOnSuccessListener(s -> {
                                                    languageOfBlocks.add(Locale.forLanguageTag(lang).getDisplayLanguage());
                                                    translatedTexts.add(s);
                                                    if(languageOfBlocks.size() == textOfBlocks.size() && textOfBlocks.size() == translatedTexts.size()) {
                                                        showTextsButton.setVisibility(View.VISIBLE);
                                                        progressAlertDialog.dismiss();
                                                        totalNumberOfBlocks.setText(getString(R.string.for_details));
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    languageOfBlocks.add(Locale.forLanguageTag(lang).getDisplayLanguage());
                                                    translatedTexts.add(textOfBlocks.get(j));
                                                    if(languageOfBlocks.size() == textOfBlocks.size() && textOfBlocks.size() == translatedTexts.size()) {
                                                        showTextsButton.setVisibility(View.VISIBLE);
                                                        progressAlertDialog.dismiss();
                                                        totalNumberOfBlocks.setText(getString(R.string.for_details));
                                                    }
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        languageOfBlocks.add(getString(R.string.unrecognized));
                                        translatedTexts.add(textOfBlocks.get(j));
                                        if(languageOfBlocks.size() == textOfBlocks.size() && textOfBlocks.size() == translatedTexts.size()) {
                                            showTextsButton.setVisibility(View.VISIBLE);
                                            progressAlertDialog.dismiss();
                                            totalNumberOfBlocks.setText(getString(R.string.for_details));
                                        }
                                    });
                        }
                        else {
                            languageOfBlocks.add(getString(R.string.unrecognized));
                            translatedTexts.add(textOfBlocks.get(j));
                            if(languageOfBlocks.size() == textOfBlocks.size() && textOfBlocks.size() == translatedTexts.size()) {
                                showTextsButton.setVisibility(View.VISIBLE);
                                progressAlertDialog.dismiss();
                                totalNumberOfBlocks.setText(getString(R.string.for_details));
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        languageOfBlocks.add(getString(R.string.unrecognized));
                        translatedTexts.add(textOfBlocks.get(j));
                        if(languageOfBlocks.size() == textOfBlocks.size() && textOfBlocks.size() == translatedTexts.size()) {
                            showTextsButton.setVisibility(View.VISIBLE);
                            progressAlertDialog.dismiss();
                            totalNumberOfBlocks.setText(getString(R.string.for_details));
                        }
                    });
        }
    }

    /**
     * This method tries to open an app like instagram, twitter and facebook if it exists
     * or else the app opens on default browser
     * @param packageName the package name of the certain app
     * @param url the url of the certain app
     */
    private void openAnotherAppIfExists(String packageName, String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage(packageName);
        try { startActivity(intent); }
        catch(ActivityNotFoundException e) { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
    }
}