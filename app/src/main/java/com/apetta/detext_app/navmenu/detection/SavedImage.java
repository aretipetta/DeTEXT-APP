package com.apetta.detext_app.navmenu.detection;

import java.io.Serializable;
import java.util.ArrayList;

public class SavedImage implements Serializable {
    // implementation --> for putExtra on intent

    private String storagePath, date;  // , location uri,
    private ArrayList<String> sourceBlocks, translatedBlocks, languageOfBlock;

    public SavedImage(){ }

    public SavedImage(String storagePath, ArrayList<String> sourceBlocks, ArrayList<String> translatedBlocks, ArrayList<String> languageOfBlocks, String date){
//        setUri(uri);
//        String uri,

        setStoragePath(storagePath);
        setSourceBlocks(sourceBlocks);
        setTranslatedBlocks(translatedBlocks);
        setLanguageOfBlock(languageOfBlocks);
        setDate(date);
    }

//    public void setUri(String uri) { this.uri = uri; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
    public void setDate(String date) { this.date = date; }
    public void setSourceBlocks(ArrayList<String> sourceBlocks) { this.sourceBlocks = sourceBlocks; }
    public void setTranslatedBlocks(ArrayList<String> translatedBlocks) { this.translatedBlocks = translatedBlocks; }
    public void setLanguageOfBlock(ArrayList<String> languageOfBlock) { this.languageOfBlock = languageOfBlock; }

//    public String getUri() { return this.uri; }
    public String getStoragePath() { return this.storagePath; }
    public String getDate() { return this.date; }
    public ArrayList<String> getSourceBlocks() { return this.sourceBlocks; }
    public ArrayList<String> getTranslatedBlocks() { return this.translatedBlocks; }
    public ArrayList<String> getLanguageOfBlock() { return this.languageOfBlock; }
}
