package model;
import java.sql.Date;
///aa
public class Schedule {
    private int scheduleID;
    private Date date;
    private String direction;
    private int routeID;
    private int busID;
    private int driverID;
    private int monitorID;
    private String status;
    private String incidentStatus;
    private int replacementBusID;
    private int handlingTechID;

    // Các đối tượng liên kết (để hiển thị chi tiết ra View)
    private Route route;
    private Bus bus;
    private Bus replacementBus;
    private User driver;
    private User monitor;

    public Schedule() {
    }

    public Schedule(int scheduleID, Date date, String direction, int routeID, int busID, int driverID, int monitorID, String status, String incidentStatus) {
        this.scheduleID = scheduleID;
        this.date = date;
        this.direction = direction;
        this.routeID = routeID;
        this.busID = busID;
        this.driverID = driverID;
        this.monitorID = monitorID;
        this.status = status;
        this.incidentStatus = incidentStatus;
        this.replacementBusID = 0;
        this.handlingTechID = 0;
    }

    public Schedule(int scheduleID, Date date, String direction, int routeID, int busID, int driverID, int monitorID, String status, String incidentStatus, int replacementBusID) {
        this.scheduleID = scheduleID;
        this.date = date;
        this.direction = direction;
        this.routeID = routeID;
        this.busID = busID;
        this.driverID = driverID;
        this.monitorID = monitorID;
        this.status = status;
        this.incidentStatus = incidentStatus;
        this.replacementBusID = replacementBusID;
        this.handlingTechID = 0;
    }
    
    public Schedule(int scheduleID, Date date, String direction, int routeID, int busID, int driverID, int monitorID, String status, String incidentStatus, int replacementBusID, int handlingTechID) {
        this.scheduleID = scheduleID;
        this.date = date;
        this.direction = direction;
        this.routeID = routeID;
        this.busID = busID;
        this.driverID = driverID;
        this.monitorID = monitorID;
        this.status = status;
        this.incidentStatus = incidentStatus;
        this.replacementBusID = replacementBusID;
        this.handlingTechID = handlingTechID;
    }

    public int getScheduleID() {
        return scheduleID;
    }

    public void setScheduleID(int scheduleID) {
        this.scheduleID = scheduleID;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public int getRouteID() {
        return routeID;
    }

    public void setRouteID(int routeID) {
        this.routeID = routeID;
    }

    public int getBusID() {
        return busID;
    }

    public void setBusID(int busID) {
        this.busID = busID;
    }

    public int getDriverID() {
        return driverID;
    }

    public void setDriverID(int driverID) {
        this.driverID = driverID;
    }

    public int getMonitorID() {
        return monitorID;
    }

    public void setMonitorID(int monitorID) {
        this.monitorID = monitorID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIncidentStatus() {
        return incidentStatus;
    }

    public void setIncidentStatus(String incidentStatus) {
        this.incidentStatus = incidentStatus;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public Bus getBus() {
        return bus;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }

    public User getDriver() {
        return driver;
    }

    public void setDriver(User driver) {
        this.driver = driver;
    }

    public User getMonitor() {
        return monitor;
    }

    public void setMonitor(User monitor) {
        this.monitor = monitor;
    }

    public int getReplacementBusID() {
        return replacementBusID;
    }

    public void setReplacementBusID(int replacementBusID) {
        this.replacementBusID = replacementBusID;
    }

    public int getHandlingTechID() {
        return handlingTechID;
    }

    public void setHandlingTechID(int handlingTechID) {
        this.handlingTechID = handlingTechID;
    }

    public Bus getReplacementBus() {
        return replacementBus;
    }

    public void setReplacementBus(Bus replacementBus) {
        this.replacementBus = replacementBus;
    }
}
