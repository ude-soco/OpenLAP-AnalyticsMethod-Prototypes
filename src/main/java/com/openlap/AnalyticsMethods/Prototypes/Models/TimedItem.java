package com.openlap.AnalyticsMethods.Prototypes.Models;

import java.util.Date;

/**
 * Created by Arham Muslim
 * on 12-Oct-16.
 */
public class TimedItem {

    private String title;
    private Date date;

    public TimedItem() {}
    public TimedItem(String title, Date date) {
        this.title = title;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}