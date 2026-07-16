package dal;

import model.ScheduleProgress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class quản lý tiến trình/tiến độ của các chuyến xe.
 * Xử lý việc ghi nhận thời gian xe đến các điểm dừng (check-in tại điểm dừng).
 */
public class ScheduleProgressDAO extends DBContext {

    /**
     * Ghi nhận tiến độ: Thêm một bản ghi khi xe đến một điểm dừng cụ thể.
     * Thời gian đến (ArrivalTime) sẽ được tự động lấy là thời gian hiện tại của hệ thống cơ sở dữ liệu (GETDATE()).
     * @param scheduleID ID của lịch trình/chuyến xe
     * @param stopID ID của điểm dừng mà xe vừa tới
     */
    public void insertProgress(int scheduleID, int stopID) {
        String sql = "INSERT INTO ScheduleProgress (ScheduleID, StopID, ArrivalTime) VALUES (?, ?, GETDATE())";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, scheduleID);
            ps.setInt(2, stopID);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lấy danh sách toàn bộ tiến trình của một chuyến xe.
     * @param scheduleID ID của chuyến xe cần lấy tiến trình
     * @return Danh sách các điểm dừng mà xe đã đi qua, sắp xếp theo thời gian mới nhất lên đầu (DESC)
     */
    public List<ScheduleProgress> getProgressBySchedule(int scheduleID) {
        List<ScheduleProgress> list = new ArrayList<>();
        String sql = "SELECT * FROM ScheduleProgress WHERE ScheduleID = ? ORDER BY ArrivalTime DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, scheduleID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ScheduleProgress p = new ScheduleProgress(
                            rs.getInt("ProgressID"),
                            rs.getInt("ScheduleID"),
                            rs.getInt("StopID"),
                            rs.getTimestamp("ArrivalTime")
                    );
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
