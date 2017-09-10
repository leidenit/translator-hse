package com.project.mvgugaev.translator.items;

// Tab class(use in history and saved translate)

public class Tab {
    private String tFrom;
    private String tTo;
    private String langs; // For example: ru-en
    private String id;
    private String flag; // States: 1 - history 2 - save 3 - both

    public String getId() { return id; }
    public void setId(String id) {
        this.id = id;
    }

    public String getFlag() {
        return flag;
    }
    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String gettFrom() {
        return tFrom;
    }
    public void settFrom(String tFrom) {
        this.tFrom = tFrom;
    }

    public String gettTo() {
        return tTo;
    }
    public void settTo(String tTo) {
        this.tTo = tTo;
    }

    public String getLangs() {
        return langs;
    }
    public void setLangs(String langs) {
        this.langs = langs;
    }

    public Tab(String _id,String _textFrom, String _textTo, String _langs,String _flag)
    {
        tFrom = _textFrom;
        tTo = _textTo;
        langs = _langs;
        flag = _flag;
        id = _id;
    }
}