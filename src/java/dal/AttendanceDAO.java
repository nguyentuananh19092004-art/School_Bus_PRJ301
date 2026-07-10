package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO extends DBContext {

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
