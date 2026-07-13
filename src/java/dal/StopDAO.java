package dal;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Stop;

public class StopDAO extends DBContext {

    public List<Stop> getAllStop() {
        List<Stop> list = new ArrayList<>();
        String sql = "SELECT * FROM Stops ORDER BY StopID";
        try (PreparedStatement st = connection.prepareStatement(sql); ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                list.add(new Stop(rs.getInt("StopID"), rs.getString("StopName"), rs.getString("Address"), rs.getBigDecimal("Latitude"), rs.getBigDecimal("Longitude")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Stop getStopById(int id) {
        String sql = "SELECT * FROM Stops WHERE StopID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return new Stop(rs.getInt("StopID"), rs.getString("StopName"), rs.getString("Address"), rs.getBigDecimal("Latitude"), rs.getBigDecimal("Longitude"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insertStop(Stop stop) {
        String sql = "INSERT INTO Stops (StopName, Address, Latitude, Longitude) VALUES (?, ?, ?, ?)";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, stop.getStopName());
            st.setString(2, stop.getAddress());
            st.setBigDecimal(3, stop.getLatitude());
            st.setBigDecimal(4, stop.getLongitude());
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStop(Stop stop) {
        String sql = "UPDATE Stops SET StopName = ?, Address = ?, Latitude = ?, Longitude = ? WHERE StopID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, stop.getStopName());
            st.setString(2, stop.getAddress());
            st.setBigDecimal(3, stop.getLatitude());
            st.setBigDecimal(4, stop.getLongitude());
            st.setInt(5, stop.getStopID());
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteStop(int id) {
        String sql = "DELETE FROM Stops WHERE StopID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, id);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkStopNameExist(String stopName) {
        String sql = "SELECT 1 FROM Stops WHERE StopName = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, stopName);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkStopLocationExist(BigDecimal latitude, BigDecimal longitude) {
        String sql = "SELECT 1 FROM Stops WHERE Latitude = ? AND Longitude = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setBigDecimal(1, latitude);
            st.setBigDecimal(2, longitude);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Stop> getStopsByRoute(int routeID) {
        List<Stop> list = new ArrayList<>();
        String sql = "SELECT s.* FROM Stops s " +
                     "JOIN RouteStops rs ON s.StopID = rs.StopID " +
                     "WHERE rs.RouteID = ? " +
                     "ORDER BY rs.StopOrder ASC";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, routeID);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    list.add(new Stop(rs.getInt("StopID"), rs.getString("StopName"), rs.getString("Address"), rs.getBigDecimal("Latitude"), rs.getBigDecimal("Longitude")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
