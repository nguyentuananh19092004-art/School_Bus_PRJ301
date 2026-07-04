package model;

import java.sql.Date;
import java.sql.Timestamp;

public class UserLeave {
    private int leaveID;
    private int userID;
    private Date leaveDate;
    private String reason;
    private String status;
    private Timestamp createdAt;

    // Additional fields for JOIN queries
    private String fullName;
    private String role;

    public UserLeave() {
    }

    public UserLeave(int leaveID, int userID, Date leaveDate, String reason, String status, Timestamp createdAt) {
        this.leaveID = leaveID;
        this.userID = userID;
        this.leaveDate = leaveDate;
        this.reason = reason;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getLeaveID() {
        return leaveID;
    }

    public void setLeaveID(int leaveID) {
        this.leaveID = leaveID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public Date getLeaveDate() {
        return leaveDate;
    }

    public void setLeaveDate(Date leaveDate) {
        this.leaveDate = leaveDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
