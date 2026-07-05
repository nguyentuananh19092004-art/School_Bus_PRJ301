package dal;

import model.Notification;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO extends DBContext {

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

    public void markAsRead(int notifID) {
        String sql = "UPDATE Notifications SET IsRead = 1 WHERE NotifID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, notifID);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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
