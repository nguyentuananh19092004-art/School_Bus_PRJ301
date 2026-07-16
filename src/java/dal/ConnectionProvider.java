package dal;

import java.sql.Connection;

/**
 * Lớp cung cấp kết nối cơ sở dữ liệu.
 * Kế thừa từ DBContext để lấy ra đối tượng Connection.
 */
public class ConnectionProvider extends DBContext {
    /**
     * Lấy đối tượng Connection hiện tại.
     * @return Đối tượng java.sql.Connection
     */
    public Connection getConnection() {
        return connection;
    }
}
