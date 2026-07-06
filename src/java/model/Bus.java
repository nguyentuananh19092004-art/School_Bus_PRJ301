package model;

public class Bus {
    private int busID;
    private String licensePlate;
    private int capacity;
    private String status;

    public Bus() {
    }

    public Bus(int busID, String licensePlate, int capacity, String status) {
        this.busID = busID;
        this.licensePlate = licensePlate;
        this.capacity = capacity;
        this.status = status;
    }

    public int getBusID() {
        return busID;
    }

    public void setBusID(int busID) {
        this.busID = busID;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
