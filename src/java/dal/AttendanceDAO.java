package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class xử lý nghiệp vụ Điểm danh (Attendance) học sinh lên/xuống xe.
 */
public class AttendanceDAO extends DBContext {

    /**
     * Lưu thông tin điểm danh của một học sinh tại một điểm dừng.
     * @param scheduleID ID của chuyến xe
     * @param maHocSinh Mã số học sinh
     * @param stopID ID của điểm dừng diễn ra điểm danh
     * @param isAbsent true nếu học sinh vắng mặt, false nếu có mặt
     * @param direction "TO_SCHOOL" (chiều đi) hoặc "RETURN_HOME" (chiều về) để ghi nhận đúng trường thời gian (Boarding/Alighting)
     */
    public void insertAttendance(int scheduleID, String maHocSinh, int stopID, boolean isAbsent, String direction) {
        String timeField = "TO_SCHOOL".equals(direction) ? "BoardingTime" : "AlightingTime";
        String sql = "INSERT INTO Attendances (ScheduleID, MaHocSinh, StopID, " + timeField + ", IsAbsent) VALUES (?, ?, ?, GETDATE(), ?)";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, scheduleID);
            st.setString(2, maHocSinh);
            st.setInt(3, stopID);
            st.setBoolean(4, isAbsent);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lấy danh sách mã học sinh đã được điểm danh (bao gồm cả có mặt và vắng mặt) trong một chuyến xe.
     * @param scheduleID ID của chuyến xe
     * @return Danh sách mã học sinh đã điểm danh
     */
    public List<String> getAttendedStudents(int scheduleID) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT MaHocSinh FROM Attendances WHERE ScheduleID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, scheduleID);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("MaHocSinh"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy danh sách mã học sinh đã điểm danh tại các điểm không phải là điểm trường học (thường là các điểm đón/trả tại nhà).
     * @param scheduleID ID chuyến xe
     * @param schoolStopID ID của điểm dừng là trường học
     * @return Danh sách mã học sinh
     */
    public List<String> getStudentsCheckedAtNonSchoolStops(int scheduleID, int schoolStopID) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT MaHocSinh FROM Attendances WHERE ScheduleID = ? AND StopID != ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, scheduleID);
            st.setInt(2, schoolStopID);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("MaHocSinh"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Kiểm tra xem học sinh đã từng lên xe (điểm danh thành công, không vắng mặt) trong bất kỳ chuyến nào của ngày hôm nay hay chưa.
     * @param maHocSinh Mã học sinh cần kiểm tra
     * @return true nếu học sinh đã từng lên xe trong ngày, ngược lại false
     */
    public boolean hasBoardedToday(String maHocSinh) {
        String sql = "SELECT COUNT(*) FROM Attendances a JOIN Schedules s ON a.ScheduleID = s.ScheduleID " +
                     "WHERE a.MaHocSinh = ? AND s.Date = CAST(GETDATE() AS DATE) AND a.IsAbsent = 0";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, maHocSinh);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
