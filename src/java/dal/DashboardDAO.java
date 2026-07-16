package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO class xử lý các truy vấn đếm số lượng (thống kê) cho trang Dashboard.
 * Kế thừa từ DBContext để kết nối cơ sở dữ liệu.
 */
public class DashboardDAO extends DBContext {

    /**
     * Đếm tổng số bản ghi trong một bảng cụ thể.
     * @param tableName Tên bảng cần đếm (ví dụ: "Buses", "Users")
     * @return Tổng số bản ghi trong bảng, hoặc 0 nếu có lỗi xảy ra
     */
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

    /**
     * Đếm tổng số lượng người dùng dựa trên vai trò (Role) cụ thể.
     * @param role Vai trò của người dùng (ví dụ: "ADMIN", "DRIVER")
     * @return Số lượng người dùng có vai trò tương ứng, hoặc 0 nếu có lỗi
     */
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
