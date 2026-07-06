package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Bus;

public class BusDAO extends DBContext {

    public List<Bus> getAllBus() {
        List<Bus> list = new ArrayList<>();
        String sql = "SELECT * FROM Buses ORDER BY BusID";
        try (PreparedStatement st = connection.prepareStatement(sql); ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                list.add(new Bus(rs.getInt("BusID"), rs.getString("LicensePlate"), rs.getInt("Capacity"), rs.getString("Status")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Bus> getAllBusesIncludingDeleted() {
        List<Bus> list = new ArrayList<>();
        String sql = "SELECT * FROM Buses ORDER BY BusID";
        try (PreparedStatement st = connection.prepareStatement(sql); ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                list.add(new Bus(rs.getInt("BusID"), rs.getString("LicensePlate"), rs.getInt("Capacity"), rs.getString("Status")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Bus getBusById(int id) {
        String sql = "SELECT * FROM Buses WHERE BusID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return new Bus(rs.getInt("BusID"), rs.getString("LicensePlate"), rs.getInt("Capacity"), rs.getString("Status"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insertBus(Bus bus) {
        String sql = "INSERT INTO Buses (LicensePlate, Capacity, Status) VALUES (?, ?, ?)";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, bus.getLicensePlate());
            st.setInt(2, bus.getCapacity());
            st.setString(3, bus.getStatus() == null ? "Sẵn sàng" : bus.getStatus());
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBus(Bus bus) {
        String sql = "UPDATE Buses SET LicensePlate = ?, Capacity = ?, Status = ? WHERE BusID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, bus.getLicensePlate());
            st.setInt(2, bus.getCapacity());
            st.setString(3, bus.getStatus());
            st.setInt(4, bus.getBusID());
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteBus(int id) {
        String sql = "DELETE FROM Buses WHERE BusID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, id);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkLicensePlateExist(String licensePlate) {
        String sql = "SELECT 1 FROM Buses WHERE LicensePlate = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, licensePlate);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkLicensePlateExist(String licensePlate, int excludeBusId) {
        String sql = "SELECT 1 FROM Buses WHERE LicensePlate = ? AND BusID <> ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, licensePlate);
            st.setInt(2, excludeBusId);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean changeStatus(int busId, String status) {
        String sql = "UPDATE Buses SET Status = ? WHERE BusID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, status);
            st.setInt(2, busId);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Bus> getAvailableBus() {
        List<Bus> list = new ArrayList<>();
        String sql = "SELECT * FROM Buses WHERE Status IN (N'Sẵn sàng', N'Hoạt động') ORDER BY BusID";
        try (PreparedStatement st = connection.prepareStatement(sql); ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                list.add(new Bus(rs.getInt("BusID"), rs.getString("LicensePlate"), rs.getInt("Capacity"), rs.getString("Status")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean canDeleteBus(int busId) {
        String sql = "SELECT 1 FROM Schedules WHERE BusID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, busId);
            try (ResultSet rs = st.executeQuery()) {
                return !rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Bus> getBusesByDate(java.sql.Date date) {
        List<Bus> list = new ArrayList<>();
        String sql = "SELECT DISTINCT b.BusID, b.LicensePlate, b.Capacity, b.Status FROM Buses b LEFT JOIN Schedules s ON s.BusID = b.BusID AND s.Date = ? ORDER BY b.BusID";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setDate(1, date);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    list.add(new Bus(rs.getInt("BusID"), rs.getString("LicensePlate"), rs.getInt("Capacity"), rs.getString("Status")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
