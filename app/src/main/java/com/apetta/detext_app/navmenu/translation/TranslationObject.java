package com.apetta.detext_app.navmenu.translation;

public class TranslationObject {

    private String country, locality, month, year, sourceWord, translatedWord, sourceLang, translateLang;

    public TranslationObject() {}

    public TranslationObject(String country, String locality, String month, String year, String sourceWord,
                             String translatedWord, String sourceLang, String translateLang) {
        setCountry(country);
        setLocality(locality);
        setMonth(month);
        setYear(year);
        setSourceWord(sourceWord);
        setTranslatedWord(translatedWord);
        setSourceLang(sourceLang);
        setTranslateLang(translateLang);

    }

    public void setCountry(String country) {this.country = country;}

    public void setLocality(String locality) {this.locality = locality;}

    public void setMonth(String month) {
        this.month = month;
    }

    public void setYear(String year) {this.year = year;}

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

    public String getCountry() {return country;}

    public String getLocality() {return locality;}

    public String getMonth() {
        return month;
    }

    public String getYear() {return year;}

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
