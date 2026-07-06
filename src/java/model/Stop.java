package model;

import java.math.BigDecimal;

public class Stop {
    private int stopID;
    private String stopName;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;

    public Stop() {
    }

    public Stop(int stopID, String stopName, String address, BigDecimal latitude, BigDecimal longitude) {
        this.stopID = stopID;
        this.stopName = stopName;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getStopID() {
        return stopID;
    }

    public void setStopID(int stopID) {
        this.stopID = stopID;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public void setLatitude(String value) {
        this.latitude = value == null || value.trim().isEmpty() ? null : new BigDecimal(value.trim());
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public void setLongitude(String value) {
        this.longitude = value == null || value.trim().isEmpty() ? null : new BigDecimal(value.trim());
    }
}
