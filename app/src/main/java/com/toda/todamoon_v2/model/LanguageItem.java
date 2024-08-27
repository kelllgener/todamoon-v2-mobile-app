package com.toda.todamoon_v2.model;

public class LanguageItem {
    private int flagResourceId;
    private String languageName;
    private String country;
    private String displayLanguage;
    private String languageCode;  // New property

    public LanguageItem(int flagResourceId, String languageName, String country, String displayLanguage, String languageCode) {
        this.flagResourceId = flagResourceId;
        this.languageName = languageName;
        this.country = country;
        this.displayLanguage = displayLanguage;
        this.languageCode = languageCode;
    }

    public int getFlagResourceId() {
        return flagResourceId;
    }

    public String getLanguageName() {
        return languageName;
    }

    public String getCountry() {
        return country;
    }

    public String getDisplayLanguage() {
        return displayLanguage;
    }

    public String getLanguageCode() {
        return languageCode;
    }
}
