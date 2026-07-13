package model;

import java.sql.Time;

public class Stop {
    private int stopID;
    private String stopName;
    private String address;
    private double latitude;
    private double longitude;

    // Fields for RouteStops join
    private int stopOrder;
    private Time estimatedTime;
    private Time returnTime;

    public Stop() {
    }

    public Stop(int stopID, String stopName, String address, double latitude, double longitude) {
        this.stopID = stopID;
        this.stopName = stopName;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getStopID() { return stopID; }
    public void setStopID(int stopID) { this.stopID = stopID; }

    public String getStopName() { return stopName; }
    public void setStopName(String stopName) { this.stopName = stopName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public int getStopOrder() { return stopOrder; }
    public void setStopOrder(int stopOrder) { this.stopOrder = stopOrder; }

    public Time getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(Time estimatedTime) { this.estimatedTime = estimatedTime; }

    public Time getReturnTime() { return returnTime; }
    public void setReturnTime(Time returnTime) { this.returnTime = returnTime; }
}
