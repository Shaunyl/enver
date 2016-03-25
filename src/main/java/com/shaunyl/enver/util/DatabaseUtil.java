package com.shaunyl.enver.util;

import com.shaunyl.enver.BeanFactory;
import com.shaunyl.enver.Paths;
import com.shaunyl.enver.database.ConnectionFactory;
import com.shaunyl.enver.database.Database;
import com.shaunyl.enver.exception.DatabaseException;
import com.shaunyl.enver.exception.UnexpectedEnverException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import javax.sql.DataSource;
import oracle.jdbc.pool.OracleDataSource;

/**
 *
 * @author Filippo Testino (filippo.testino@gmail.com)
 */
public class DatabaseUtil {

    private static final FileManager fileManager = BeanFactory.getInstance().getBean(FileManager.class);

    private static final String SINGLE_TASK_KEY = "enver.url";

    private static final String BLOB_FORMATTER = "<BLOB>";

    private static final SimpleDateFormat TIMESTAMP_FORMATTER = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd-MMM-yyyy");

    private static Iterator<Entry<String, String>> iterator;

    public static Connection buildConnection(String database, String[] userid, boolean multi, boolean autocommit, int seconds) throws DatabaseException {
        Database dataprovider = Database.valueOf(database); //FIXME

        String url, currentKey;
        if (multi) {
            if (iterator == null) {
                Map<String, String> values = fileManager.readAllWithKeys(Paths.MULTI_CONNECTION_FILE, "");
                Set<Entry<String, String>> set = values.entrySet();
                iterator = set.iterator();
            }
            iterator.hasNext();
            Entry<String, String> mapEntry = iterator.next();
            currentKey = mapEntry.getKey();
            url = mapEntry.getValue();

        } else {

//            String URL = "jdbc:oracle:thin:" + user + "/" + password + "@192.168.1.17:1521:testdb";
            String[] hash = fileManager.readWithKeys(Paths.SINGLE_CONNECTION_FILE, SINGLE_TASK_KEY);
            url = hash[1];
//            String replace = "jdbc:oracle:thin:";
//            String subString = url.substring(url.indexOf("@"));
//            url = replace + user + "/" + password + subString;

            currentKey = hash[0];
        }

        Connection connection;
        try {
            DataSource datasource = ConnectionFactory.getDataSource(dataprovider);
            if (datasource.getClass().equals(OracleDataSource.class)) {

                Properties props = new Properties();
                props.put("user", userid[0]);
                props.put("password", userid[1]);
                if ("sys".equals(userid[0])) {
                    props.put("internal_logon", "sysdba");
                }

                ((OracleDataSource) datasource).setConnectionProperties(props);
                ((OracleDataSource) datasource).setURL(url);
            }

            datasource.setLoginTimeout(seconds);
            connection = datasource.getConnection();
            connection.setAutoCommit(autocommit);

        } catch (SQLException sqle) {
            throw new DatabaseException(sqle.getMessage(), sqle, currentKey);
        }
        return connection;
    }

    public static List<String> getAllTables(Connection conn, String schema) {
        Statement stmt = null;
        ResultSet rs = null;
        List<String> tables = new ArrayList<String>();
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT table_name FROM dba_tables WHERE owner = '" + schema + "'");
            while (rs.next()) {
                tables.add(rs.getString(1));
            }
        } catch (SQLException e) {
            throw new UnexpectedEnverException(e.getMessage(), e);
        } finally {
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(rs);
        }
        return tables;
    }

    public static String getTableSize(final Statement statement, final String schema, final String table, boolean scope) {
        String size = "";
//        int count = 0;
        try {
            ResultSet rs = statement.executeQuery(String.format("SELECT bytes FROM %s"
                    + " WHERE segment_name = UPPER('%s') AND segment_type = 'TABLE'"
                    + " %s AND owner = UPPER('%s')"
                    , scope ? "user_segments" : "dba_segments", table, scope ? "--" : "", schema));
            while (rs.next()) {
                Double bytes = rs.getDouble(1);
                if (bytes > 1024) {
                    size = bytes / (double) 1024 + "KB";
                } else if (bytes > 104578) {
                    size = bytes / (double) 1048576 + "MB";
                } else if (bytes > 104578 * 1024) {
                    size = bytes / (double) (1048576 * 1024) + "GB";
                }
//                rs.next();
//                count = rs.getInt(1);
            }
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 942) {
                throw new RuntimeException(ex.getMessage() + "Internal Error..", ex);
            }
        }
//        return info + "\t\t" + count + " rows";
        return size;
    }

    public static ResultSet exportTable(final Statement statement, final String schema, final String table) {
        try {
            return statement.executeQuery(String.format("SELECT * FROM %s.%s", schema, table));
        } catch (SQLException ex) {
            throw new RuntimeException(ex.getMessage() + " (\"" + schema + "." + table + "\")", ex);
        }
    }

    @Deprecated
    public static List<String> buildQuerySelectAllTables(Connection conn, String schema) {
        Statement stmt = null;
        ResultSet rs = null;
        List<String> tables = new ArrayList<String>();
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT table_name FROM dba_tables WHERE owner = '" + schema + "'");
            while (rs.next()) {
                tables.add(String.format("SELECT * FROM %s.%s", schema, rs.getString(1)));
            }
        } catch (SQLException e) {
            throw new UnexpectedEnverException(e.getMessage(), e);
        } finally {
            ConnectionFactory.close(stmt);
            ConnectionFactory.close(rs);
        }
        return tables;
    }

    public static String getColumnValue(ResultSet rs, int colType, int colIndex)
            throws SQLException, IOException {

        String value = "";

        switch (colType) {
            case Types.BIT:
                Object bit = rs.getObject(colIndex);
                if (bit != null) {
                    value = String.valueOf(bit);
                }
                break;
            case Types.BOOLEAN:
                boolean b = rs.getBoolean(colIndex);
                if (!rs.wasNull()) {
                    value = Boolean.valueOf(b).toString();
                }
                break;
            case Types.CLOB:
                Clob c = rs.getClob(colIndex);
                if (c != null) {
                    value = GeneralUtil.readClob(c);
                }
                break;
            case Types.BIGINT:
            case Types.DECIMAL:
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.REAL:
            case Types.NUMERIC:
                BigDecimal bd = rs.getBigDecimal(colIndex);
                if (bd != null) {
                    value = "" + bd.doubleValue();
                }
                break;
            case Types.INTEGER:
            case Types.TINYINT:
            case Types.SMALLINT:
                int intValue = rs.getInt(colIndex);
                if (!rs.wasNull()) {
                    value = "" + intValue;
                }
                break;
            case Types.JAVA_OBJECT:
                Object obj = rs.getObject(colIndex);
                if (obj != null) {
                    value = String.valueOf(obj);
                }
                break;
            case Types.DATE:
                java.sql.Date date = rs.getDate(colIndex);
                if (date != null) {
                    value = DATE_FORMATTER.format(date);
                }
                break;
            case Types.TIME:
                Time t = rs.getTime(colIndex);
                if (t != null) {
                    value = t.toString();
                }
                break;
            case Types.TIMESTAMP:
                Timestamp tstamp = rs.getTimestamp(colIndex);
                if (tstamp != null) {
                    value = TIMESTAMP_FORMATTER.format(tstamp);
                }
                break;
            case Types.LONGVARCHAR:
            case Types.VARCHAR:
            case Types.CHAR:
                value = rs.getString(colIndex);
                break;
            case Types.BLOB:
                value = BLOB_FORMATTER;
                break;
            default:
                value = "";
        }

        if (value == null) {
            value = "";
        }

        return value;
    }

    public static List<String> getColumnTypes(Connection connection, String schema, String table) throws SQLException {
        List<String> columns = new ArrayList<String>();
        Statement statement = connection.createStatement();
        String sql = String.format("SELECT data_type, column_name FROM dba_tab_columns WHERE owner = '%s' AND table_name = '%s' ORDER BY column_id", schema.toUpperCase(), table.toUpperCase());
        ResultSet localRs = statement.executeQuery(sql);
        while (localRs.next()) {
            columns.add(localRs.getString("DATA_TYPE"));
        }

        ConnectionFactory.close(statement);
        ConnectionFactory.close(localRs);

        return columns;
    }

    //FIXME: performance issue: check metadata at every insert..
    public static void setPreparedStatement(Connection connection, PreparedStatement prepared, List<String> columnTypes, Object[] record, SimpleDateFormat sdf) throws SQLException {
        for (int j = 0; j < record.length; j++) {
            if (columnTypes.get(j).matches("CHAR|VARCHAR|VARCHAR2")) {
                prepared.setString(j + 1, record[j].toString());
            } else if ("NUMBER".equals(columnTypes.get(j))) {
                if (record[j].toString().isEmpty()) {
                    prepared.setNull(j + 1, Types.NUMERIC);
                } else {
                    prepared.setBigDecimal(j + 1, BigDecimal.valueOf(Double.parseDouble(record[j].toString())));
                }
            } else if (columnTypes.get(j).matches("TIMESTAMP\\([0-9]+\\)|DATE")) {
                if (record[j].toString().isEmpty()) {
                    prepared.setNull(j + 1, Types.TIMESTAMP);
                } else {
                    java.util.Date date;
                    try {
                        date = sdf.parse(record[j].toString());
                    } catch (ParseException ex) {
                        throw new UnexpectedEnverException(ex.getMessage());
                    }
                    prepared.setTimestamp(j + 1, new Timestamp(date.getTime()));
                }
            } else if (columnTypes.get(j).matches("CLOB")) {
                if (record[j].toString().isEmpty()) {
                    prepared.setNull(j + 1, Types.CLOB);
                } else {
                    Clob clob = connection.createClob();
                    clob.setString(1, record[j].toString());
                    prepared.setClob(j + 1, clob);
                }
            }
        }
    }

    public static void deleteFromTable(Connection connection, String table) throws SQLException {
        Statement statement = connection.createStatement();
        String sql = "DELETE " + table;
        statement.execute(sql);
        statement.close();
    }
}
