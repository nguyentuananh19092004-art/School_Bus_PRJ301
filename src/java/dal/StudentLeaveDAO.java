package dal;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StudentLeaveDAO extends DBContext {
    public void insertLeave(String maHocSinh, String leaveDate) {
        String sql = "INSERT INTO StudentLeaves (MaHocSinh, LeaveDate) VALUES (?, ?)";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, maHocSinh);
            st.setString(2, leaveDate);
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
}