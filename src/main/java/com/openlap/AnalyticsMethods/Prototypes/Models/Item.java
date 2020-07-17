package com.openlap.AnalyticsMethods.Prototypes.Models;

/**
 * Created by Arham Muslim
 * on 12-Oct-16.
 */
public class Item {

    private String title;
    private String column;

    public Item() {}
    public Item(String title, String column) {
        this.title = title;
        this.column = column;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }
}
