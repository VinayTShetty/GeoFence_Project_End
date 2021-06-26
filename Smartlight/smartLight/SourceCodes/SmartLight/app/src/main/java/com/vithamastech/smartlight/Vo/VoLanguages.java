package com.vithamastech.smartlight.Vo;

import java.io.Serializable;

/* Language Date Getter Setter*/
public class VoLanguages implements Serializable {
    String languageName = "";
    String languageCode = "";

    public String getLanguageName() {
        return languageName;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }
}
