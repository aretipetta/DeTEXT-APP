package com.apetta.detext_app.navmenu.translation;

public class TranslationStats {
    private String location, month, sourceWord, translatedWord, sourceLang, translateLang;

//    public TranslationStats() {}

    public TranslationStats(String location, String month, String sourceWord, String translatedWord, String sourceLang, String translateLang) {
        setLocation(location);
        setMonth(month);
        setSourceWord(sourceWord);
        setTranslatedWord(translatedWord);
        setSourceLang(sourceLang);
        setTranslateLang(translateLang);
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public void setSourceWord(String sourceWord) {
        this.sourceWord = sourceWord;
    }

    public void setTranslatedWord(String translatedWord) {
        this.translatedWord = translatedWord;
    }

    public void setSourceLang(String sourceLang) {
        this.sourceLang = sourceLang;
    }

    public void setTranslateLang(String translateLang) {
        this.translateLang = translateLang;
    }

    public String getLocation() {
        return location;
    }

    public String getMonth() {
        return month;
    }

    public String getSourceWord() {
        return sourceWord;
    }

    public String getTranslatedWord() {
        return translatedWord;
    }

    public String getSourceLang() {
        return sourceLang;
    }

    public String getTranslateLang() {
        return translateLang;
    }
}
