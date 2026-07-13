package dal;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StudentLeaveDAO extends DBContext {

    public boolean insertLeave(String maHocSinh, Date leaveDate) {
        if (isStudentOnLeave(maHocSinh, leaveDate)) {
            return false; // Prevent duplicate
        }
        String sql = "INSERT INTO StudentLeaves (MaHocSinh, LeaveDate) VALUES (?, ?)";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, maHocSinh);
            st.setDate(2, leaveDate);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isStudentOnLeave(String maHocSinh, Date date) {
        String sql = "SELECT 1 FROM StudentLeaves WHERE MaHocSinh = ? AND LeaveDate = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, maHocSinh);
            st.setDate(2, date);
            java.sql.ResultSet rs = st.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public java.util.List<Date> getUpcomingLeaves(String maHocSinh) {
        java.util.List<Date> leaves = new java.util.ArrayList<>();
        String sql = "SELECT LeaveDate FROM StudentLeaves WHERE MaHocSinh = ? AND LeaveDate >= CAST(GETDATE() AS DATE) ORDER BY LeaveDate ASC";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, maHocSinh);
            java.sql.ResultSet rs = st.executeQuery();
            while (rs.next()) {
                leaves.add(rs.getDate("LeaveDate"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return leaves;
    }
}
