package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Route;

/**
 * DAO class quản lý dữ liệu Tuyến đường (Routes).
 * Chịu trách nhiệm lấy thông tin các tuyến xe và kết hợp với điểm dừng.
 */
public class RouteDAO extends DBContext {

    /**
     * Lấy danh sách toàn bộ các tuyến đường có trong hệ thống.
     * @return Danh sách đối tượng Route
     */
    public List<Route> getAllRoutes() {
        List<Route> list = new ArrayList<>();
        String sql = "SELECT * FROM Routes";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                Route r = new Route(
                        rs.getInt("RouteID"),
                        rs.getString("RouteCode"),
                        rs.getString("RouteName"),
                        rs.getString("Description")
                );
                list.add(r);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return list;
    }

    /**
     * Lấy danh sách kết hợp giữa Điểm dừng và Tuyến đường (loại trừ điểm trường học).
     * Hàm này thường được dùng để hiển thị các tùy chọn điểm đón/trả khi gán học sinh vào tuyến.
     * @return Danh sách các tùy chọn StopRouteOption
     */
    public List<model.StopRouteOption> getStopRouteOptions() {
        List<model.StopRouteOption> list = new ArrayList<>();
        String sql = "SELECT s.StopID, r.RouteID, s.StopName, s.Address, r.RouteName, rs.EstimatedTime, rs.ReturnTime, s.Latitude, s.Longitude " +
                     "FROM RouteStops rs " +
                     "JOIN Stops s ON rs.StopID = s.StopID " +
                     "JOIN Routes r ON rs.RouteID = r.RouteID " +
                     "WHERE s.StopName NOT LIKE N'%Trường%' " +
                     "ORDER BY s.StopName, rs.EstimatedTime";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                list.add(new model.StopRouteOption(
                        rs.getInt("StopID"),
                        rs.getInt("RouteID"),
                        rs.getString("StopName"),
                        rs.getString("Address"),
                        rs.getString("RouteName"),
                        rs.getTime("EstimatedTime"),
                        rs.getTime("ReturnTime"),
                        rs.getDouble("Latitude"),
                        rs.getDouble("Longitude")
                ));
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return list;
    }
}
