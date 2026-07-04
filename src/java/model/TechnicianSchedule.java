package model;

import java.sql.Date;
import java.sql.Timestamp;

public class TechnicianSchedule {
    private int techScheduleID;
    private int technicianID;
    private Date date;
    private Timestamp createdAt;
    private String status;
    
    // Additional field for display
    private String technicianName;

    public TechnicianSchedule() {
    }

    public TechnicianSchedule(int techScheduleID, int technicianID, Date date, Timestamp createdAt, String status) {
        this.techScheduleID = techScheduleID;
        this.technicianID = technicianID;
        this.date = date;
        this.createdAt = createdAt;
        this.status = status;
    }

    public int getTechScheduleID() {
        return techScheduleID;
    }

    public void setTechScheduleID(int techScheduleID) {
        this.techScheduleID = techScheduleID;
    }

    public int getTechnicianID() {
        return technicianID;
    }

    public void setTechnicianID(int technicianID) {
        this.technicianID = technicianID;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getTechnicianName() {
        return technicianName;
    }

    public void setTechnicianName(String technicianName) {
        this.technicianName = technicianName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
