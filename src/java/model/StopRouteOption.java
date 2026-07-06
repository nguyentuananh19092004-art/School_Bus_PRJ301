package model;

import java.sql.Time;

public class StopRouteOption {
    private int routeID;
    private int stopID;
    private int stopOrder;
    private Time estimatedTime;
    private Time returnTime;
    private Stop stop;

    public StopRouteOption() {
    }

    public StopRouteOption(int routeID, int stopID, int stopOrder, Time estimatedTime, Time returnTime) {
        this.routeID = routeID;
        this.stopID = stopID;
        this.stopOrder = stopOrder;
        this.estimatedTime = estimatedTime;
        this.returnTime = returnTime;
    }

    public int getRouteID() {
        return routeID;
    }

    public void setRouteID(int routeID) {
        this.routeID = routeID;
    }

    public int getStopID() {
        return stopID;
    }

    public void setStopID(int stopID) {
        this.stopID = stopID;
    }

    public int getStopOrder() {
        return stopOrder;
    }

    public void setStopOrder(int stopOrder) {
        this.stopOrder = stopOrder;
    }

    public Time getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(Time estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public Time getReturnTime() {
        return returnTime;
    }

    public void setReturnTime(Time returnTime) {
        this.returnTime = returnTime;
    }

    public Stop getStop() {
        return stop;
    }

    public void setStop(Stop stop) {
        this.stop = stop;
    }
}
