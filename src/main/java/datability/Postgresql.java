package datability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Postgresql implements Database {

    private static final Logger LOG = LoggerFactory.getLogger(Postgresql.class);
    private final Connection connection;

    public Postgresql(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Database disableNotNulls(String... tables) {
        try {
            for (String table : tables) {
                for (String column : findNotNullColumns(table)) {
                    LOG.debug("Disabling not-null on column '{}' of table '{}'", column, table);
                    executeSql("ALTER TABLE " + table + " ALTER " + column + " DROP NOT NULL");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }

        return this;
    }

    private List<String> findNotNullColumns(String table) throws SQLException {
        List<String> notNullsColumns = new ArrayList<>();

        String sql = "SELECT DISTINCT column_name FROM INFORMATION_SCHEMA.COLUMNS" +
                " WHERE column_name IS NOT NULL" +
                " AND columns.table_name = '" + table + "'" +
                " AND columns.is_nullable='NO'";

        try {
            notNullsColumns.addAll(getFirstColumnValues(sql));
        } catch (SQLException e) {
            throw new DatabaseException(sql, e);
        }

        notNullsColumns.removeAll(findPrimaryKeys(table));

        return notNullsColumns;
    }

    private List<String> findPrimaryKeys(String table) throws SQLException {
        return getFirstColumnValues("SELECT a.attname" +
                " FROM pg_index i JOIN pg_attribute a ON a.attrelid = i.indrelid AND a.attnum = ANY (i.indkey)" +
                " WHERE i.indrelid = '" + table + "' :: REGCLASS AND i.indisprimary");

    }

    private List<String> getFirstColumnValues(String sql) throws SQLException {
        List<String> values = new ArrayList<>();
        ResultSet rs = connection.createStatement().executeQuery(sql);
        while (rs.next()) {
            values.add(rs.getString(1));
        }
        return values;
    }

    private void executeSql(String sql) {
        try {
            connection.createStatement().execute(sql);
        } catch (SQLException e) {
            throw new DatabaseException(sql, e);
        }
    }
}
