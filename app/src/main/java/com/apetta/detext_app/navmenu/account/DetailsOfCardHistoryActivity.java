package com.apetta.detext_app.navmenu.account;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.apetta.detext_app.R;
import com.apetta.detext_app.navmenu.detection.SavedImage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class DetailsOfCardHistoryActivity extends AppCompatActivity {
    SavedImage savedImage;
    TextView language, originalText, translatedText, date, numberOfBlock;
    ImageView image;
    ImageButton prevButton, nextButton;
    int counter;
    HashMap<String, Bitmap> imgsMap;


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
        setListeners();
        loadDataOnControls();
    }

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
    }

    public void showDetailsOfBlock() {
        numberOfBlock.setText("Block " + (counter + 1));
        language.setText(savedImage.getLanguageOfBlock().get(counter));
        originalText.setText(savedImage.getSourceBlocks().get(counter));
        translatedText.setText(savedImage.getTranslatedBlocks().get(counter));
    }

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
            showDetailsOfBlock();
//            numberOfBlock.setText("Block " + (counter + 1));
//            language.setText(savedImage.getLanguageOfBlock().get(counter));
//            originalText.setText(savedImage.getSourceBlocks().get(counter));
//            translatedText.setText(savedImage.getTranslatedBlocks().get(counter));
        });
    }
}