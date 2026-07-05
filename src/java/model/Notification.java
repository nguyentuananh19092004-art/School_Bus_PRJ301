package model;

import java.sql.Timestamp;

public class Notification {
    private int notifID;
    private int userID;
    private String username;
    private String message;
    private Timestamp createdAt;
    private boolean isRead;

    public Notification() {
    }

    public Notification(int notifID, int userID, String username, String message, Timestamp createdAt, boolean isRead) {
        this.notifID = notifID;
        this.userID = userID;
        this.username = username;
        this.message = message;
        this.createdAt = createdAt;
        this.isRead = isRead;
    }

    public int getNotifID() {
        return notifID;
    }

    public void setNotifID(int notifID) {
        this.notifID = notifID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }
}
