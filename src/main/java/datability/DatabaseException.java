package datability;

import java.sql.SQLException;

public class DatabaseException extends RuntimeException {
    public DatabaseException(String sql, SQLException e) {
        super("Error thrown executing sql '" + sql + "'. Message: " + e.getMessage(), e);
    }

    public DatabaseException(Exception e) {
        super(e);
    }

    public DatabaseException(String msg) {
        super(msg);
    }
}
