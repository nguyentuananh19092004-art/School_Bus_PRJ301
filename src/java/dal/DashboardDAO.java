package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardDAO extends DBContext {

    public int countTable(String tableName) {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return 0;
    }

    public int countUsersByRole(String role) {
        String sql = "SELECT COUNT(*) FROM Users WHERE Role = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, role);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return 0;
    }
}
