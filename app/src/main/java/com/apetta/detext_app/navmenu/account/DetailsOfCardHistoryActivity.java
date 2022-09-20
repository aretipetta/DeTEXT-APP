package com.apetta.detext_app.navmenu.account;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.apetta.detext_app.R;
import com.apetta.detext_app.alertDialogs.ProgressAlertDialog;
import com.apetta.detext_app.navmenu.detection.ImageResultsActivity;
import com.apetta.detext_app.navmenu.detection.SavedImage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class DetailsOfCardHistoryActivity extends AppCompatActivity {
    SavedImage savedImage;
    TextView language, originalText, translatedText, date, numberOfBlock;
    ImageView image;
    ImageButton prevButton, nextButton, copyOriginalText, copyTranslatedText, fbBtn, instaBtn, twitterBtn;
    int counter;
    HashMap<String, Bitmap> imgsMap;
    ProgressAlertDialog progressAlertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_of_card_history);

        savedImage = (SavedImage) getIntent().getExtras().get("savedImage");
        language = findViewById(R.id.languageValueDetailsHistory);
        originalText = findViewById(R.id.originalTextValueDetailsHistory);
        translatedText = findViewById(R.id.translatedTextValueDetailsHistory);
        date = findViewById(R.id.dateDetailsHistory);
        numberOfBlock = findViewById(R.id.numberOfBlockDetailsHistory);
        image = findViewById(R.id.imgDetailsHistory);
        prevButton = findViewById(R.id.previousBtnDetailsHistory);
        prevButton.setVisibility(View.INVISIBLE);
        nextButton = findViewById(R.id.nextBtnDetailsHistory);
        nextButton.setVisibility(View.INVISIBLE);
        copyOriginalText = findViewById(R.id.copyOrgnlTxtDetails);
        copyTranslatedText = findViewById(R.id.copyTranslTxtDetails);
        fbBtn = findViewById(R.id.fbShareDetails);
        instaBtn = findViewById(R.id.instaShareDetails);
        twitterBtn = findViewById(R.id.twitterShareDetails);
        progressAlertDialog = new ProgressAlertDialog(this, getString(R.string.loading));
        progressAlertDialog.show();
        setListeners();
        loadDataOnControls();
    }

    /* Sets listeners to activity's widgets */
    public void setListeners() {
        prevButton.setOnClickListener(view -> {
            counter--;
            if(counter == 0) {
                prevButton.setVisibility(View.INVISIBLE);
                nextButton.setVisibility(View.VISIBLE);
            }
            if(counter == savedImage.getSourceBlocks().size() - 2) nextButton.setVisibility(View.VISIBLE);
            showDetailsOfBlock();
        });

        nextButton.setOnClickListener(view -> {
            counter++;
            if(counter == savedImage.getSourceBlocks().size() - 1) nextButton.setVisibility(View.INVISIBLE);
            if(counter == 1) prevButton.setVisibility(View.VISIBLE);
            showDetailsOfBlock();
        });

        copyOriginalText.setOnClickListener(view -> {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("OriginalText", originalText.getText().toString());
            clipboardManager.setPrimaryClip(clipData);
            Toast.makeText(DetailsOfCardHistoryActivity.this, getString(R.string.copied), Toast.LENGTH_SHORT).show();
        });

        copyTranslatedText.setOnClickListener(view -> {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("TranslatedText", translatedText.getText().toString());
            clipboardManager.setPrimaryClip(clipData);
            Toast.makeText(DetailsOfCardHistoryActivity.this, getString(R.string.copied), Toast.LENGTH_SHORT).show();
        });

        fbBtn.setOnClickListener(view -> { openAnotherAppIfExists("com.facebook", "https://www.facebook.com/"); });

        instaBtn.setOnClickListener(view -> { openAnotherAppIfExists("com.instagram.android", "https://www.instagram.com/"); });

        twitterBtn.setOnClickListener(view -> { openAnotherAppIfExists("com.twitter.android", "https://twitter.com/"); });
    }

    /* shows the details of a history record */
    public void showDetailsOfBlock() {
        numberOfBlock.setText(getString(R.string.block) + (counter + 1));
        language.setText(savedImage.getLanguageOfBlock().get(counter));
        originalText.setText(savedImage.getSourceBlocks().get(counter));
        translatedText.setText(savedImage.getTranslatedBlocks().get(counter));
    }

    /* loads data on activity's widgets */
    public void loadDataOnControls() {
        counter = 0;
        imgsMap = new HashMap<>();
        String imgPath = savedImage.getStoragePath();
        StorageReference sRef = FirebaseStorage.getInstance().getReference().child(imgPath);
        sRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
            if(!imgsMap.containsKey(imgPath))
                imgsMap.put(imgPath, BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
            image.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
            date.setText(savedImage.getDate());
            if(savedImage.getSourceBlocks().size() > 1) nextButton.setVisibility(View.VISIBLE);
            progressAlertDialog.dismiss();
            showDetailsOfBlock();
        });
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