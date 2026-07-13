package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Stop;

public class StopDAO extends DBContext {

    public List<Stop> getStopsByRoute(int routeID) {
        List<Stop> list = new ArrayList<>();
        String sql = "SELECT s.*, rs.StopOrder, rs.EstimatedTime, rs.ReturnTime " +
                     "FROM Stops s " +
                     "JOIN RouteStops rs ON s.StopID = rs.StopID " +
                     "WHERE rs.RouteID = ? " +
                     "ORDER BY rs.EstimatedTime ASC";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, routeID);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                Stop s = new Stop(
                        rs.getInt("StopID"),
                        rs.getString("StopName"),
                        rs.getString("Address"),
                        rs.getDouble("Latitude"),
                        rs.getDouble("Longitude")
                );
                s.setStopOrder(rs.getInt("StopOrder"));
                s.setEstimatedTime(rs.getTime("EstimatedTime"));
                s.setReturnTime(rs.getTime("ReturnTime"));
                list.add(s);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return list;
    }

    public List<Stop> getAllStops() {
        List<Stop> list = new ArrayList<>();
        String sql = "SELECT * FROM Stops";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                list.add(new Stop(
                        rs.getInt("StopID"),
                        rs.getString("StopName"),
                        rs.getString("Address"),
                        rs.getDouble("Latitude"),
                        rs.getDouble("Longitude")
                ));
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return list;
    }

    public int insertStop(Stop s) {
        String sql = "INSERT INTO Stops (StopName, Address, Latitude, Longitude) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement st = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            st.setString(1, s.getStopName());
            st.setString(2, s.getAddress());
            st.setDouble(3, s.getLatitude());
            st.setDouble(4, s.getLongitude());
            st.executeUpdate();
            ResultSet rs = st.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return -1;
    }

    public int getMaxStopOrder(int routeID) {
        String sql = "SELECT MAX(StopOrder) FROM RouteStops WHERE RouteID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, routeID);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return 0;
    }

    public void addStopToRoute(int routeID, int stopID, int stopOrder, String estimatedTime, String returnTime) {
        String sql = "INSERT INTO RouteStops (RouteID, StopID, StopOrder, EstimatedTime, ReturnTime) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, routeID);
            st.setInt(2, stopID);
            st.setInt(3, stopOrder);
            st.setString(4, estimatedTime);
            st.setString(5, returnTime);
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
    public void updateRouteStopTime(int routeID, int stopID, String estimatedTime, String returnTime) {
        String sql = "UPDATE RouteStops SET EstimatedTime = ?, ReturnTime = ? WHERE RouteID = ? AND StopID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, estimatedTime);
            st.setString(2, returnTime);
            st.setInt(3, routeID);
            st.setInt(4, stopID);
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
}
