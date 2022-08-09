package it.units;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SingletonConnection {
    private static Connection connection = null;
    protected SingletonConnection() {}
    static Connection getConnection() throws SQLException {
        if (connection == null) {
            connection = DriverManager.getConnection(Main.connection_string);
        }
        return connection;
    }
}
