package dal;

import java.sql.Connection;

public class ConnectionProvider extends DBContext {
    public Connection getConnection() {
        return connection;
    }
}
