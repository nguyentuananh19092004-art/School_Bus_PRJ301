package dal;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * DAO class quản lý các thao tác liên quan đến việc xin nghỉ phép của học sinh.
 * Cung cấp các phương thức thêm, kiểm tra và lấy danh sách ngày nghỉ.
 */
public class StudentLeaveDAO extends DBContext {

    /**
     * Thêm mới một yêu cầu nghỉ phép cho học sinh vào cơ sở dữ liệu.
     * Hàm này sẽ tự động kiểm tra trùng lặp trước khi thêm.
     * @param maHocSinh Mã học sinh xin nghỉ
     * @param leaveDate Ngày nghỉ phép
     * @return true nếu thêm thành công, false nếu thất bại hoặc đã tồn tại ngày nghỉ này
     */
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

    /**
     * Kiểm tra xem học sinh có đang được duyệt nghỉ phép vào một ngày cụ thể hay không.
     * @param maHocSinh Mã học sinh cần kiểm tra
     * @param date Ngày cần kiểm tra
     * @return true nếu học sinh có lịch nghỉ phép vào ngày đó, ngược lại trả về false
     */
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

    /**
     * Lấy danh sách tất cả các ngày xin nghỉ phép sắp tới của một học sinh.
     * Chỉ lấy các ngày tính từ thời điểm hiện tại trở đi, sắp xếp theo thứ tự tăng dần.
     * @param maHocSinh Mã học sinh cần lấy danh sách
     * @return Danh sách các đối tượng Date biểu diễn các ngày học sinh sẽ nghỉ
     */
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
