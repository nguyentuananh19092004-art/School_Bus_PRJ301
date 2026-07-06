package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import model.Route;
import model.Stop;
import model.StopRouteOption;

public class RouteDAO extends DBContext {

    public List<Route> getAllRoute() {
        List<Route> list = new ArrayList<>();
        String sql = "SELECT * FROM Routes ORDER BY RouteID";
        try (PreparedStatement st = connection.prepareStatement(sql); ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                list.add(new Route(rs.getInt("RouteID"), rs.getString("RouteCode"), rs.getString("RouteName"), rs.getString("Description")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Route getRouteById(int id) {
        String sql = "SELECT * FROM Routes WHERE RouteID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return new Route(rs.getInt("RouteID"), rs.getString("RouteCode"), rs.getString("RouteName"), rs.getString("Description"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insertRoute(Route route) {
        String sql = "INSERT INTO Routes (RouteCode, RouteName, Description) VALUES (?, ?, ?)";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, route.getRouteCode());
            st.setString(2, route.getRouteName());
            st.setString(3, route.getDescription());
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateRoute(Route route) {
        String sql = "UPDATE Routes SET RouteCode = ?, RouteName = ?, Description = ? WHERE RouteID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, route.getRouteCode());
            st.setString(2, route.getRouteName());
            st.setString(3, route.getDescription());
            st.setInt(4, route.getRouteID());
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteRoute(int id) {
        String sql = "DELETE FROM Routes WHERE RouteID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, id);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkRouteCodeExist(String routeCode) {
        String sql = "SELECT 1 FROM Routes WHERE RouteCode = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, routeCode);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkRouteCodeExist(String routeCode, int excludeRouteId) {
        String sql = "SELECT 1 FROM Routes WHERE RouteCode = ? AND RouteID <> ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, routeCode);
            st.setInt(2, excludeRouteId);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<StopRouteOption> getStopsByRoute(int routeId) {
        List<StopRouteOption> list = new ArrayList<>();
        String sql = "SELECT rs.RouteID, rs.StopID, rs.StopOrder, rs.EstimatedTime, rs.ReturnTime, s.StopName, s.Address, s.Latitude, s.Longitude " +
                     "FROM RouteStops rs JOIN Stops s ON rs.StopID = s.StopID WHERE rs.RouteID = ? ORDER BY rs.StopOrder";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, routeId);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    Stop stop = new Stop(rs.getInt("StopID"), rs.getString("StopName"), rs.getString("Address"), rs.getBigDecimal("Latitude"), rs.getBigDecimal("Longitude"));
                    StopRouteOption option = new StopRouteOption(rs.getInt("RouteID"), rs.getInt("StopID"), rs.getInt("StopOrder"), rs.getTime("EstimatedTime"), rs.getTime("ReturnTime"));
                    option.setStop(stop);
                    list.add(option);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean saveRouteStops(int routeId, List<StopRouteOption> stops) {
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement deleteSt = connection.prepareStatement("DELETE FROM RouteStops WHERE RouteID = ?")) {
                deleteSt.setInt(1, routeId);
                deleteSt.executeUpdate();
            }
            if (stops != null && !stops.isEmpty()) {
                String sql = "INSERT INTO RouteStops (RouteID, StopID, StopOrder, EstimatedTime, ReturnTime) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement insertSt = connection.prepareStatement(sql)) {
                    for (StopRouteOption option : stops) {
                        insertSt.setInt(1, routeId);
                        insertSt.setInt(2, option.getStopID());
                        insertSt.setInt(3, option.getStopOrder());
                        insertSt.setTime(4, option.getEstimatedTime());
                        insertSt.setTime(5, option.getReturnTime());
                        insertSt.addBatch();
                    }
                    insertSt.executeBatch();
                }
            }
            connection.commit();
            connection.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ignored) {
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    public boolean updateRouteStops(int routeId, List<StopRouteOption> stops) {
        return saveRouteStops(routeId, stops);
    }
}
