package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Bus;

/**
 * DAO class quản lý dữ liệu Xe bus (Buses) và Lịch sử bảo dưỡng xe (BusMaintenances).
 * Thực hiện các thao tác CRUD và kiểm tra trạng thái xe.
 */
public class BusDAO extends DBContext {

    /**
     * Lấy đối tượng Connection hiện tại đang được sử dụng.
     * @return Connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Lấy danh sách tất cả các xe đang hoạt động (không bao gồm xe "Đã xóa").
     * @return Danh sách đối tượng Bus
     */
    public List<Bus> getAllBuses() {
        List<Bus> list = new ArrayList<>();
        String sql = "SELECT * FROM Buses WHERE Status != N'Đã xóa'";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                Bus b = new Bus(
                        rs.getInt("BusID"),
                        rs.getString("LicensePlate"),
                        rs.getInt("Capacity"),
                        rs.getString("Status")
                );
                list.add(b);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return list;
    }

    /**
     * Lấy danh sách toàn bộ xe trong hệ thống, bao gồm cả những xe đã bị xóa (phục vụ mục đích thống kê/lịch sử).
     * @return Danh sách đối tượng Bus
     */
    public List<Bus> getAllBusesIncludingDeleted() {
        List<Bus> list = new ArrayList<>();
        String sql = "SELECT * FROM Buses";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                Bus b = new Bus(
                        rs.getInt("BusID"),
                        rs.getString("LicensePlate"),
                        rs.getInt("Capacity"),
                        rs.getString("Status")
                );
                list.add(b);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return list;
    }

    /**
     * Lấy danh sách xe trong một ngày cụ thể, kèm theo việc tính toán Trạng thái động (DynamicStatus).
     * Trạng thái động có thể là: "Bảo dưỡng/Sửa chữa" (nếu có lịch bảo dưỡng), "Hoạt động" (nếu có lịch chạy), hoặc "Sẵn sàng" (trống lịch).
     * Đồng thời lấy lý do bảo dưỡng gần nhất (MaintenanceDescription).
     * @param date Ngày cần lấy thông tin
     * @return Danh sách xe với trạng thái động được cập nhật
     */
    public List<Bus> getBusesByDate(java.sql.Date date) {
        List<Bus> list = new ArrayList<>();
        String sql = "SELECT b.*, " +
                     "CASE " +
                     "  WHEN b.Status = N'Bảo dưỡng/Sửa chữa' THEN N'Bảo dưỡng/Sửa chữa' " +
                     "  WHEN EXISTS (" +
                     "      SELECT 1 FROM BusMaintenances bm " +
                     "      WHERE bm.BusID = b.BusID AND (bm.MaintenanceDate = ? OR bm.MaintenanceDate = DATEADD(day, -1, ?) OR bm.MaintenanceDate = DATEADD(day, -2, ?))" +
                     "  ) THEN N'Bảo dưỡng/Sửa chữa' " +
                     "  WHEN EXISTS (" +
                     "      SELECT 1 FROM Schedules s " +
                     "      WHERE s.Date = ? AND (s.BusID = b.BusID OR s.ReplacementBusID = b.BusID) AND s.Status != 'CANCELLED'" +
                     "  ) THEN N'Hoạt động' " +
                     "  ELSE N'Sẵn sàng' " +
                     "END AS DynamicStatus, " +
                     "  (SELECT TOP 1 Description FROM BusMaintenances bm WHERE bm.BusID = b.BusID AND (bm.MaintenanceDate = ? OR bm.MaintenanceDate = DATEADD(day, -1, ?) OR bm.MaintenanceDate = DATEADD(day, -2, ?)) ORDER BY MaintenanceDate DESC, CreatedAt DESC) AS MaintenanceDescription " +
                     "FROM Buses b " +
                     "WHERE b.Status != N'Đã xóa'";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setDate(1, date);
            st.setDate(2, date);
            st.setDate(3, date);
            st.setDate(4, date);
            st.setDate(5, date);
            st.setDate(6, date);
            st.setDate(7, date);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                Bus b = new Bus(
                        rs.getInt("BusID"),
                        rs.getString("LicensePlate"),
                        rs.getInt("Capacity"),
                        rs.getString("DynamicStatus"),
                        rs.getString("MaintenanceDescription")
                );
                list.add(b);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return list;
    }

    /**
     * Lấy thông tin chi tiết của một chiếc xe dựa trên ID.
     * @param id ID của xe
     * @return Đối tượng Bus, hoặc null nếu không tìm thấy
     */
    public Bus getBusById(int id) {
        String sql = "SELECT * FROM Buses WHERE BusID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, id);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return new Bus(
                        rs.getInt("BusID"),
                        rs.getString("LicensePlate"),
                        rs.getInt("Capacity"),
                        rs.getString("Status")
                );
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return null;
    }

    /**
     * Thêm một chiếc xe mới vào cơ sở dữ liệu.
     * @param b Đối tượng Bus chứa thông tin xe (Biển số, sức chứa, trạng thái)
     */
    public void insertBus(Bus b) {
        String sql = "INSERT INTO Buses (LicensePlate, Capacity, Status) VALUES (?, ?, ?)";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, b.getLicensePlate());
            st.setInt(2, b.getCapacity());
            st.setString(3, b.getStatus());
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    /**
     * Cập nhật thông tin của một chiếc xe đã tồn tại.
     * @param b Đối tượng Bus chứa thông tin mới (dựa trên BusID)
     */
    public void updateBus(Bus b) {
        String sql = "UPDATE Buses SET LicensePlate=?, Capacity=?, Status=? WHERE BusID=?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, b.getLicensePlate());
            st.setInt(2, b.getCapacity());
            st.setString(3, b.getStatus());
            st.setInt(4, b.getBusID());
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    /**
     * Xóa mềm (Soft delete) một chiếc xe bằng cách đổi trạng thái thành "Đã xóa"
     * và đổi biển số để không bị trùng lặp khi thêm xe mới.
     * @param id ID của xe cần xóa
     */
    public void deleteBus(int id) {
        String sql = "UPDATE Buses SET Status = N'Đã xóa', LicensePlate = CONCAT(LEFT(LicensePlate, 8), '_d', BusID) WHERE BusID=?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, id);
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    /**
     * Kiểm tra xem một biển số xe đã tồn tại trong hệ thống chưa (ngoại trừ chiếc xe đang kiểm tra).
     * @param licensePlate Biển số cần kiểm tra
     * @param excludeBusId ID xe cần loại trừ (dùng khi cập nhật, truyền -1 nếu là thêm mới)
     * @return true nếu đã tồn tại, false nếu chưa
     */
    public boolean checkLicensePlateExist(String licensePlate, int excludeBusId) {
        String sql = "SELECT 1 FROM Buses WHERE LicensePlate = ? AND BusID != ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, licensePlate);
            st.setInt(2, excludeBusId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return false;
    }

    /**
     * Thêm một bản ghi lịch sử bảo dưỡng cho xe.
     * @param busID ID của xe
     * @param date Ngày đi bảo dưỡng
     * @param description Lý do/mô tả bảo dưỡng
     * @return true nếu thêm thành công, ngược lại false
     */
    public boolean insertBusMaintenance(int busID, java.sql.Date date, String description) {
        String sql = "INSERT INTO BusMaintenances (BusID, MaintenanceDate, Description) VALUES (?, ?, ?)";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, busID);
            st.setDate(2, date);
            st.setString(3, description);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println(e);
        }
        return false;
    }

    /**
     * Xóa một bản ghi lịch sử bảo dưỡng của xe trong một ngày cụ thể.
     * @param busID ID của xe
     * @param date Ngày bảo dưỡng cần xóa
     * @return true nếu xóa thành công, ngược lại false
     */
    public boolean deleteBusMaintenance(int busID, java.sql.Date date) {
        String sql = "DELETE FROM BusMaintenances WHERE BusID = ? AND MaintenanceDate = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, busID);
            st.setDate(2, date);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println(e);
        }
        return false;
    }

    /**
     * Kiểm tra xem xe có đang được xếp lịch chạy trong tương lai hay không.
     * Dùng để ngăn chặn việc xóa xe đang có lịch.
     * @param busID ID của xe
     * @return true nếu có lịch chạy từ ngày hiện tại trở đi, ngược lại false
     */
    public boolean hasFutureSchedule(int busID) {
        String sql = "SELECT 1 FROM Schedules WHERE (BusID = ? OR ReplacementBusID = ?) AND Date >= CAST(GETDATE() AS DATE)";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, busID);
            st.setInt(2, busID);
            ResultSet rs = st.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println(e);
        }
        return false;
    }

    /**
     * Lấy danh sách toàn bộ lịch sử bảo dưỡng của một chiếc xe, sắp xếp từ mới nhất đến cũ nhất.
     * @param busID ID của xe
     * @return Danh sách các bản ghi bảo dưỡng (BusMaintenance)
     */
    public List<model.BusMaintenance> getBusMaintenances(int busID) {
        List<model.BusMaintenance> list = new ArrayList<>();
        String sql = "SELECT * FROM BusMaintenances WHERE BusID = ? ORDER BY CreatedAt DESC";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, busID);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                list.add(new model.BusMaintenance(
                        rs.getInt("BusID"),
                        rs.getDate("MaintenanceDate"),
                        rs.getString("Description"),
                        rs.getTimestamp("CreatedAt")
                ));
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return list;
    }
}
