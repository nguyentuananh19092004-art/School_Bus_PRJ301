package model;

import java.sql.Time;

public class StopRouteOption {
    private int stopID;
    private int routeID;
    private String stopName;
    private String address;
    private String routeName;
    private Time estimatedTime;
    private Time returnTime;
    private double latitude;
    private double longitude;

    public StopRouteOption(int stopID, int routeID, String stopName, String address, String routeName, Time estimatedTime, Time returnTime, double latitude, double longitude) {
        this.stopID = stopID;
        this.routeID = routeID;
        this.stopName = stopName;
        this.address = address;
        this.routeName = routeName;
        this.estimatedTime = estimatedTime;
        this.returnTime = returnTime;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getStopID() { return stopID; }
    public int getRouteID() { return routeID; }
    public String getStopName() { return stopName; }
    public String getAddress() { return address; }
    public String getRouteName() { return routeName; }
    public Time getEstimatedTime() { return estimatedTime; }
    public Time getReturnTime() { return returnTime; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}
