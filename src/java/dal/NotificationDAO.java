package dal;

import model.Notification;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class quản lý Hệ thống Thông báo (Notifications) cho người dùng.
 * Xử lý việc gửi, nhận và đánh dấu trạng thái đọc thông báo.
 */
public class NotificationDAO extends DBContext {

    /**
     * Tạo một thông báo mới gửi đến một người dùng cụ thể.
     * @param username Tên đăng nhập của người nhận
     * @param message Nội dung thông báo
     */
    public void insertNotification(String username, String message) {
        String sql = "INSERT INTO Notifications (UserID, Message) SELECT UserID, ? FROM Users WHERE Username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setNString(1, message);
            ps.setString(2, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lấy danh sách toàn bộ thông báo của một người dùng.
     * @param username Tên đăng nhập của người dùng cần lấy thông báo
     * @return Danh sách các thông báo, sắp xếp theo thời gian mới nhất lên đầu
     */
    public List<Notification> getNotificationsByUsername(String username) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT n.NotifID, n.UserID, u.Username, n.Message, n.CreatedAt, n.IsRead " +
                     "FROM Notifications n JOIN Users u ON n.UserID = u.UserID " +
                     "WHERE u.Username = ? ORDER BY n.CreatedAt DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification(
                            rs.getInt("NotifID"),
                            rs.getInt("UserID"),
                            rs.getString("Username"),
                            rs.getString("Message"),
                            rs.getTimestamp("CreatedAt"),
                            rs.getBoolean("IsRead")
                    );
                    list.add(n);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Đánh dấu một thông báo cụ thể là đã đọc.
     * @param notifID ID của thông báo cần đánh dấu
     */
    public void markAsRead(int notifID) {
        String sql = "UPDATE Notifications SET IsRead = 1 WHERE NotifID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, notifID);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Đánh dấu tất cả các thông báo chưa đọc của một người dùng thành đã đọc.
     * @param username Tên đăng nhập của người dùng
     */
    public void markAllAsRead(String username) {
        String sql = "UPDATE Notifications SET IsRead = 1 WHERE UserID = (SELECT UserID FROM Users WHERE Username = ?) AND IsRead = 0";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
