package com.project.mvgugaev.translator.items;

// Language class

public class Lang {
    private String lableText;
    private String transKey;

    public String getLableText() {
        return lableText;
    }
    public void setLableText(String lableText) {
        this.lableText = lableText;
    }

    public String getTransKey() {
        return transKey;
    }
    public void setTransKey(String transKey) {
        this.transKey = transKey;
    }

    public Lang(String lText, String tKey)
    {
        lableText = lText;
        transKey = tKey;
    }
}
