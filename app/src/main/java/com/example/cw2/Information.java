package com.example.cw2;

public class Information {

    private String link;
    private String text;

    public Information() {
    }

    public Information(String link, String text) {
        this.link = link;
        this.text = text;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
