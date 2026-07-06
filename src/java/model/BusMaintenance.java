package model;

import java.sql.Date;
import java.sql.Timestamp;

public class BusMaintenance {
    private int maintenanceID;
    private int busID;
    private Date maintenanceDate;
    private String description;
    private Timestamp createdAt;

    public BusMaintenance() {
    }

    public BusMaintenance(int maintenanceID, int busID, Date maintenanceDate, String description, Timestamp createdAt) {
        this.maintenanceID = maintenanceID;
        this.busID = busID;
        this.maintenanceDate = maintenanceDate;
        this.description = description;
        this.createdAt = createdAt;
    }

    public int getMaintenanceID() {
        return maintenanceID;
    }

    public void setMaintenanceID(int maintenanceID) {
        this.maintenanceID = maintenanceID;
    }

    public int getBusID() {
        return busID;
    }

    public void setBusID(int busID) {
        this.busID = busID;
    }

    public Date getMaintenanceDate() {
        return maintenanceDate;
    }

    public void setMaintenanceDate(Date maintenanceDate) {
        this.maintenanceDate = maintenanceDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
