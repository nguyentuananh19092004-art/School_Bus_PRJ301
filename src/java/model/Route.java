package model;

public class Route {
    private int routeID;
    private String routeCode;
    private String routeName;
    private String description;

    public Route() {
    }

    public Route(int routeID, String routeCode, String routeName, String description) {
        this.routeID = routeID;
        this.routeCode = routeCode;
        this.routeName = routeName;
        this.description = description;
    }

    public int getRouteID() {
        return routeID;
    }

    public void setRouteID(int routeID) {
        this.routeID = routeID;
    }

    public String getRouteCode() {
        return routeCode;
    }

    public void setRouteCode(String routeCode) {
        this.routeCode = routeCode;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
