package com.openlap.AnalyticsMethods.Prototypes.Models;
/**
 * Created by Arham Muslim
 * on 12-Oct-16.
 */
public class Marks {

    private Integer timestamp;
    private Double mark;

    public Marks() {}
    public Marks(Integer timestamp, Double mark) {
        this.timestamp = timestamp;
        this.mark = mark;
    }

    public Integer getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Integer timestamp) {
        this.timestamp = timestamp;
    }

    public Double getMark() {
        return mark;
    }

    public void setMark(Double mark) {
        this.mark = mark;
    }
}
