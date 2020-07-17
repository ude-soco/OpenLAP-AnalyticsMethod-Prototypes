package com.openlap.AnalyticsMethods.Prototypes.Models;

public class ClusterData {
	private String name;
	private int itemCount;
	private double att1MaxValue;
	private double att1MinValue;
    private double att2MaxValue;
    private double att2MinValue;
	private double att1CentroidValue;
    private double att2CentroidValue;

    public ClusterData() {
    }

    public ClusterData(String name, int itemCount, double att1MaxValue, double att1MinValue, double att2MaxValue, double att2MinValue) {
        this.name = name;
        this.itemCount = itemCount;
        this.att1MaxValue = att1MaxValue;
        this.att1MinValue = att1MinValue;
        this.att2MaxValue = att2MaxValue;
        this.att2MinValue = att2MinValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public double getAtt1MaxValue() {
        return att1MaxValue;
    }

    public void setAtt1MaxValue(double att1MaxValue) {
        this.att1MaxValue = att1MaxValue;
    }

    public double getAtt1MinValue() {
        return att1MinValue;
    }

    public void setAtt1MinValue(double att1MinValue) {
        this.att1MinValue = att1MinValue;
    }

    public double getAtt2MaxValue() {
        return att2MaxValue;
    }

    public void setAtt2MaxValue(double att2MaxValue) {
        this.att2MaxValue = att2MaxValue;
    }

    public double getAtt2MinValue() {
        return att2MinValue;
    }

    public void setAtt2MinValue(double att2MinValue) {
        this.att2MinValue = att2MinValue;
    }

    public double getAtt1CentroidValue() {
        return att1CentroidValue;
    }

    public void setAtt1CentroidValue(double att1CentroidValue) {
        this.att1CentroidValue = att1CentroidValue;
    }

    public double getAtt2CentroidValue() {
        return att2CentroidValue;
    }

    public void setAtt2CentroidValue(double att2CentroidValue) {
        this.att2CentroidValue = att2CentroidValue;
    }
}
