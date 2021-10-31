package com.kanok.inserter.model;

public class Article {

    private final String name;
    private final String url;
    private final String text;

    public Article(String name, String url, String text) {
        this.name = name;
        this.url = url;
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getText() {
        return text;
    }
}
