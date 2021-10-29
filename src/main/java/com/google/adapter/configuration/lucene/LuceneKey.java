package com.google.adapter.configuration.lucene;

public enum LuceneKey {
    RFC("rfc"), BAPI("bapi"), ANY("content");
    private String value;
    LuceneKey(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String field(String searchKey) {
        return "field____" + value + "____" + searchKey;
    }

    public String value(String searchKey) {
       return "value____" + value + "____" + searchKey;
    }

    public String any(String searchKey) {
        return "any____" + ANY.getValue() + "____" + this.getValue() + "____" + searchKey;
    }
}