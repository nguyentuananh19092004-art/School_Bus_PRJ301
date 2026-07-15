package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.User;

/**
 * DAO class quản lý dữ liệu Người dùng (Users) và Đơn xin phép (UserLeaves).
 * Thực hiện xác thực đăng nhập, phân quyền, và quản lý nhân sự.
 */
public class UserDAO extends DBContext {

    /**
     * Trả về đối tượng Connection hiện tại đang được sử dụng bởi DAO này.
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Kiểm tra xem nhân viên có đang được phân công lịch làm việc trong tương lai hay không.
     * Dùng để ngăn chặn việc xóa hoặc vô hiệu hóa tài khoản nhân viên đang có lịch.
     */
    public boolean hasFutureSchedule(int userID) {
        String sql = "SELECT 1 WHERE EXISTS (SELECT 1 FROM Schedules WHERE (DriverID=? OR MonitorID=? OR HandlingTechID=?) AND Date >= CAST(GETDATE() AS DATE)) OR EXISTS (SELECT 1 FROM TechnicianSchedules WHERE TechnicianID=? AND Date >= CAST(GETDATE() AS DATE))";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, userID);
            st.setInt(2, userID);
            st.setInt(3, userID);
            st.setInt(4, userID);
            ResultSet rs = st.executeQuery();
            return rs.next();
        } catch (SQLException e) {}
        return false;
    }

    /**
     * Lấy danh sách người dùng theo một vai trò cụ thể (ví dụ: "ADMIN", "DRIVER").
     */
    public List<User> getUsersByRole(String role) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM Users WHERE Role = ? AND Status != N'Đã xóa'";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, role);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                User u = new User(
                        rs.getInt("UserID"),
                        rs.getString("Username"),
                        rs.getString("Password"),
                        rs.getString("Role"),
                        rs.getString("FullName"),
                        rs.getString("Phone"),
                        rs.getString("Email"),
                        rs.getString("Status")
                );
                list.add(u);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return list;
    }

    /**
     * Lấy danh sách người dùng theo vai trò trong một ngày cụ thể, kèm trạng thái động.
     * Trạng thái động: "Nghỉ" (nếu có đơn xin nghỉ được duyệt), "Hoạt động" (nếu có lịch chạy/trực), "Sẵn sàng" (nếu rảnh).
     */
    public List<User> getUsersByRoleAndDate(String role, java.sql.Date date) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT u.*, " +
                     "CASE " +
                     "  WHEN EXISTS (" +
                     "      SELECT 1 FROM UserLeaves ul " +
                     "      WHERE ul.UserID = u.UserID AND ul.LeaveDate = ? AND ul.Status = 'APPROVED'" +
                     "  ) THEN N'Nghỉ' " +
                     "  WHEN EXISTS (" +
                     "      SELECT 1 FROM Schedules s " +
                     "      WHERE s.Date = ? AND (s.DriverID = u.UserID OR s.MonitorID = u.UserID)" +
                     "  ) THEN N'Hoạt động' " +
                     "  WHEN EXISTS (" +
                     "      SELECT 1 FROM TechnicianSchedules ts " +
                     "      WHERE ts.Date = ? AND ts.TechnicianID = u.UserID" +
                     "  ) THEN N'Hoạt động' " +
                     "  ELSE N'Sẵn sàng' " +
                     "END AS DynamicStatus " +
                     "FROM Users u WHERE u.Role = ? AND u.Status != N'Đã xóa'";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setDate(1, date);
            st.setDate(2, date);
            st.setDate(3, date);
            st.setString(4, role);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                User u = new User(
                        rs.getInt("UserID"),
                        rs.getString("Username"),
                        rs.getString("Password"),
                        rs.getString("Role"),
                        rs.getString("FullName"),
                        rs.getString("Phone"),
                        rs.getString("Email"),
                        rs.getString("DynamicStatus")
                );
                list.add(u);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return list;
    }
    /**
     * Lấy toàn bộ người dùng đang hoạt động trong hệ thống.
     */
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM Users WHERE Status != N'Đã xóa'";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                list.add(new User(
                        rs.getInt("UserID"),
                        rs.getString("Username"),
                        rs.getString("Password"),
                        rs.getString("Role"),
                        rs.getString("FullName"),
                        rs.getString("Phone"),
                        rs.getString("Email"),
                        rs.getString("Status")
                ));
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return list;
    }

    /**
     * Lấy toàn bộ người dùng trong hệ thống (bao gồm cả những người đã bị xóa mềm).
     */
    public List<User> getAllUsersIncludingDeleted() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM Users";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                list.add(new User(
                        rs.getInt("UserID"),
                        rs.getString("Username"),
                        rs.getString("Password"),
                        rs.getString("Role"),
                        rs.getString("FullName"),
                        rs.getString("Phone"),
                        rs.getString("Email"),
                        rs.getString("Status")
                ));
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return list;
    }

    /**
     * Lấy thông tin chi tiết một người dùng dựa trên UserID.
     */
    public User getUserById(int id) {
        String sql = "SELECT * FROM Users WHERE UserID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, id);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("UserID"),
                        rs.getString("Username"),
                        rs.getString("Password"),
                        rs.getString("Role"),
                        rs.getString("FullName"),
                        rs.getString("Phone"),
                        rs.getString("Email"),
                        rs.getString("Status")
                );
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return null;
    }

    /**
     * Lấy thông tin chi tiết một người dùng dựa trên tên đăng nhập (Username).
     */
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM Users WHERE Username = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, username);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("UserID"),
                        rs.getString("Username"),
                        rs.getString("Password"),
                        rs.getString("Role"),
                        rs.getString("FullName"),
                        rs.getString("Phone"),
                        rs.getString("Email"),
                        rs.getString("Status")
                );
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return null;
    }

    /**
     * Thêm một người dùng mới vào hệ thống.
     */
    public void insertUser(User u) {
        String sql = "INSERT INTO Users (Username, Password, Role, FullName, Phone, Email, Status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, u.getUsername());
            st.setString(2, u.getPassword());
            st.setString(3, u.getRole());
            st.setString(4, u.getFullName());
            st.setString(5, u.getPhone());
            st.setString(6, u.getEmail());
            st.setString(7, u.getStatus() == null ? "Sẵn sàng" : u.getStatus());
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    /**
     * Cập nhật thông tin của người dùng.
     */
    public void updateUser(User u) {
        String sql = "UPDATE Users SET Username=?, Password=?, Role=?, FullName=?, Phone=?, Email=?, Status=? WHERE UserID=?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, u.getUsername());
            st.setString(2, u.getPassword());
            st.setString(3, u.getRole());
            st.setString(4, u.getFullName());
            st.setString(5, u.getPhone());
            st.setString(6, u.getEmail());
            st.setString(7, u.getStatus());
            st.setInt(8, u.getUserID());
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    /**
     * Xóa mềm (Soft delete) người dùng. Đổi trạng thái thành "Đã xóa" và đổi Username/Phone để tránh trùng lặp.
     */
    public void deleteUser(int id) {
        String sql = "UPDATE Users SET Status = N'Đã xóa', Username = CONCAT(LEFT(Username, 35), '_d', UserID), Phone = NULL WHERE UserID=?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, id);
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    /**
     * Kiểm tra thông tin đăng nhập (Authentication) theo tài khoản, mật khẩu và vai trò.
     */
    public boolean checkLogin(String username, String password, String role) {
        String sql = "SELECT * FROM Users WHERE Username = ? AND Password = ? AND Role = ? AND Status != N'Đã xóa'";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, username);
            st.setString(2, password);
            st.setString(3, role);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return false;
    }

    /**
     * Kiểm tra xem tên đăng nhập (Username) đã tồn tại trong hệ thống chưa, ngoại trừ một UserID cụ thể (dùng khi cập nhật).
     */
    public boolean checkUsernameExist(String username, int excludeUserId) {
        String sql = "SELECT 1 FROM Users WHERE Username = ? AND UserID != ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, username);
            st.setInt(2, excludeUserId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return false;
    }

    /**
     * Kiểm tra xem số điện thoại đã tồn tại trong hệ thống chưa, ngoại trừ một UserID cụ thể.
     */
    public boolean checkPhoneExist(String phone, int excludeUserId) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        String sql = "SELECT 1 FROM Users WHERE Phone = ? AND UserID != ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, phone);
            st.setInt(2, excludeUserId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return false;
    }

    /**
     * Nộp một đơn xin nghỉ phép mới cho nhân viên.
     */
    public boolean insertUserLeave(int userID, java.sql.Date date, String reason, String status) {
        String sql = "INSERT INTO UserLeaves (UserID, LeaveDate, Reason, Status) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, userID);
            st.setDate(2, date);
            st.setString(3, reason);
            st.setString(4, status);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println(e);
        }
        return false;
    }

    /**
     * Lấy tên đăng nhập (Username) của người dùng dựa trên UserID.
     */
    public String getUsernameById(int userID) {
        String sql = "SELECT Username FROM Users WHERE UserID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, userID);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getString("Username");
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return null;
    }

    /**
     * Lấy danh sách các đơn xin nghỉ phép đang chờ duyệt (PENDING) của tất cả nhân viên.
     */
    public List<model.UserLeave> getPendingLeaves() {
        List<model.UserLeave> list = new ArrayList<>();
        String sql = "SELECT ul.*, u.FullName, u.Role FROM UserLeaves ul JOIN Users u ON ul.UserID = u.UserID WHERE ul.Status = 'PENDING' ORDER BY ul.CreatedAt DESC";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                model.UserLeave ul = new model.UserLeave(
                    rs.getInt("LeaveID"),
                    rs.getInt("UserID"),
                    rs.getDate("LeaveDate"),
                    rs.getString("Reason"),
                    rs.getString("Status"),
                    rs.getTimestamp("CreatedAt")
                );
                ul.setFullName(rs.getString("FullName"));
                ul.setRole(rs.getString("Role"));
                list.add(ul);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return list;
    }

    /**
     * Cập nhật trạng thái đơn xin nghỉ phép (Duyệt/Từ chối).
     */
    public boolean updateLeaveStatus(int leaveID, String status) {
        String sql = "UPDATE UserLeaves SET Status = ? WHERE LeaveID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, status);
            st.setInt(2, leaveID);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println(e);
        }
        return false;
    }

    /**
     * Lấy thông tin chi tiết một đơn xin nghỉ phép dựa trên LeaveID.
     */
    public model.UserLeave getLeaveById(int leaveID) {
        String sql = "SELECT * FROM UserLeaves WHERE LeaveID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, leaveID);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return new model.UserLeave(
                    rs.getInt("LeaveID"),
                    rs.getInt("UserID"),
                    rs.getDate("LeaveDate"),
                    rs.getString("Reason"),
                    rs.getString("Status"),
                    rs.getTimestamp("CreatedAt")
                );
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return null;
    }

    /**
     * Hủy/Xóa một đơn xin nghỉ phép của nhân viên trong một ngày cụ thể.
     */
    public boolean deleteUserLeave(int userID, java.sql.Date date) {
        String sql = "DELETE FROM UserLeaves WHERE UserID = ? AND LeaveDate = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, userID);
            st.setDate(2, date);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println(e);
        }
        return false;
    }

    /**
     * Cập nhật trạng thái (Status) chung của người dùng (ví dụ: "Hoạt động", "Nghỉ").
     */
    public boolean updateUserStatus(int userID, String status) {
        String sql = "UPDATE Users SET Status = ? WHERE UserID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, status);
            st.setInt(2, userID);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println(e);
        }
        return false;
    }

    /**
     * Kiểm tra xem nhân viên có đơn xin nghỉ phép nào đã được duyệt trong một ngày cụ thể hay không.
     */
    public boolean isLeaveApproved(int userID, java.sql.Date date) {
        String sql = "SELECT 1 FROM UserLeaves WHERE UserID = ? AND LeaveDate = ? AND Status = 'APPROVED'";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, userID);
            st.setDate(2, date);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return false;
    }

    /**
     * Đổi mật khẩu cho người dùng.
     */
    public boolean updatePassword(int userID, String newPassword) {
        String sql = "UPDATE Users SET Password = ? WHERE UserID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, newPassword);
            st.setInt(2, userID);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println(e);
        }
        return false;
    }

    /**
     * Kiểm tra xem Email đã tồn tại trong hệ thống (ở cả bảng Users và HocSinh) chưa, ngoại trừ một UserID cụ thể.
     */
    public boolean checkEmailExist(String email, int excludeUserId) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String sql = "SELECT 1 WHERE EXISTS (SELECT 1 FROM Users WHERE Email = ? AND UserID != ?) OR EXISTS (SELECT 1 FROM HocSinh WHERE Email = ?)";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, email);
            st.setInt(2, excludeUserId);
            st.setString(3, email);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return false;
    }
}
