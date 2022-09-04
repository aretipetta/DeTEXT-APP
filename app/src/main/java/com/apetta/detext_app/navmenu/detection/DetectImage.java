package com.apetta.detext_app.navmenu.detection;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import org.opencv.calib3d.StereoSGBM;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DetectImage {
    final ArrayList<String> supportedLanguages = new ArrayList<>(
            Arrays.asList("af", "sq", "ca", "hr", "cs", "da", "nl", "en", "et", "fil", "tl", "fi", "fr", "de", "hu", "is",
                    "id", "it", "lv", "lt", "ms", "mo", "pl", "pt", "ro", "sr-Latn", "sk", "sl", "es", "sv", "tr", "vi"));
    ArrayList<String> textOfBlocks, languageOfBlocks, translatedTexts;
    private boolean getIt = false;
    private static boolean foundText = false;

    public DetectImage() {
        textOfBlocks = new ArrayList<>();
        languageOfBlocks = new ArrayList<>();
        translatedTexts = new ArrayList<>();
    }


    public void extractTextFromImage(Context context, Uri uri) {
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        try {
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

            InputImage inputImage = InputImage.fromFilePath(context, uri);
            Task<Text> result = recognizer.process(inputImage)
                    .addOnCompleteListener(new OnCompleteListener<Text>() {
                        @Override
                        public void onComplete(@NonNull Task<Text> task) {
                            // kanei thn metafrash sthn onComplete...?
                            Log.d("ielaaa", "mesa sthn detect....  sz = " + task.getResult().getTextBlocks().size());
                            for(Text.TextBlock block : task.getResult().getTextBlocks()) {
                                textOfBlocks.add(block.getText());
                            }
                            // edw exei parei ola ta keimena opote epistrefei th lista
//                            getIt = true;
                            if(textOfBlocks.size() > 0){
                                foundText = true;
                            }
//                            if(textOfBlocks.size() > 0) translateText();
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<Text>() {
                        @Override
                        public void onSuccess(Text text) { }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) { }
                    });
        }
        catch (IOException e) { e.printStackTrace(); }
    }

    public void translateText() {
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
                                                                // edw apothikeuei to keimeno, th glwssa kai thn metafrash
                                                                languageOfBlocks.add(Locale.forLanguageTag(lang).getDisplayLanguage());
                                                                translatedTexts.add(s);
                                                                if(languageOfBlocks.size() == textOfBlocks.size() && textOfBlocks.size() == translatedTexts.size()) {
                                                                    // mono tote tha emfanistei to koumpi gia thn emfanish apotelesmatwn
//                                                                    showTextsButton.setVisibility(View.VISIBLE);
//                                                                    totalNumberOfBlocks.setText("Press 'Show' for details.");
                                                                    getIt = true;
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
//                                                                    showTextsButton.setVisibility(View.VISIBLE);
//                                                                    totalNumberOfBlocks.setText("Press 'Show' for details.");
                                                                    getIt = true;
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
//                                    showTextsButton.setVisibility(View.VISIBLE);
//                                    totalNumberOfBlocks.setText("Press 'Show' for details.");
                                    getIt = true;
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
//                                showTextsButton.setVisibility(View.VISIBLE);
//                                totalNumberOfBlocks.setText("Press 'Show' for details.");
                                getIt = true;
                            }
                        }
                    });
        }
    }

    public boolean isReady() {
        return languageOfBlocks.size() == textOfBlocks.size() && textOfBlocks.size() == translatedTexts.size();
    }

    public ArrayList<String> getTextOfBlocks() {
        return textOfBlocks;
    }

    public ArrayList<String> getLanguageOfBlocks() {
        return languageOfBlocks;
    }

    public ArrayList<String> getTranslatedTexts() {
        return translatedTexts;
    }

    public boolean getFoundText() {
        return this.foundText;
    }
}
