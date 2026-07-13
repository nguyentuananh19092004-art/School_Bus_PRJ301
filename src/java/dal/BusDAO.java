package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Bus;

public class BusDAO extends DBContext {

    public Connection getConnection() {
        return connection;
    }

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
