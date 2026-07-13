package model;

import java.sql.Timestamp;

public class ScheduleProgress {
    private int progressID;
    private int scheduleID;
    private int stopID;
    private Timestamp arrivalTime;

    private Schedule schedule;
    // We could add a Stop model later if needed

    public ScheduleProgress() {
    }

    public ScheduleProgress(int progressID, int scheduleID, int stopID, Timestamp arrivalTime) {
        this.progressID = progressID;
        this.scheduleID = scheduleID;
        this.stopID = stopID;
        this.arrivalTime = arrivalTime;
    }

    public int getProgressID() {
        return progressID;
    }

    public void setProgressID(int progressID) {
        this.progressID = progressID;
    }

    public int getScheduleID() {
        return scheduleID;
    }

    public void setScheduleID(int scheduleID) {
        this.scheduleID = scheduleID;
    }

    public int getStopID() {
        return stopID;
    }

    public void setStopID(int stopID) {
        this.stopID = stopID;
    }

    public Timestamp getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Timestamp arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }
}
