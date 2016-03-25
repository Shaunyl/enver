package com.shaunyl.enver.database;

import oracle.jdbc.pool.OracleDataSource;
import javax.sql.*;
import java.sql.*;

/**
 *
 * @author Filippo Testino (filippo.testino@gmail.com)
 */
public class ConnectionFactory {

    public static Database[] getDatabases() {
        return Database.values();
    }

    public static DataSource getDataSource(Database dp) throws SQLException {
        DataSource dataSource = null;
        switch (dp) {
            case ORACLE: {
                dataSource = new OracleDataSource();
                break;
            }
        }
        return dataSource;
    }

    public static boolean containsProvider(String test) {
        for (Database dp : Database.values()) {
            if (dp.name().equals(test)) {
                return true;
            }
        }

        return false;
    }

    public static String getStringDatabases() {
        String providers = "";
        for (Database dp : getDatabases()) {
            providers += "  " + dp.name();
        }

        return providers;
    }
    
    public static void close(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            printException(e);
        }
    }

    public static void close(ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (SQLException e) {
            printException(e);
        }
    }

    public static void cancel(Statement... statements) {
        try {
            for (Statement statement : statements) {
                if (statement != null) {
                    statement.cancel();
                }
            }
        } catch (SQLException e) {
            printException(e);
        }
    }

    public static void close(Statement... statements) {
        try {
            for (Statement statement : statements) {
                if (statement != null) {
                    statement.close();
                }
            }
        } catch (SQLException e) {
            printException(e);
        }
    }

    public static void rollback(Connection connection) {
        try {
            if (connection != null) {
                connection.rollback();
            }
        } catch (SQLException e) {
            printException(e);
        }
    }

    public static void commit(Connection connection) {
        try {
            if (connection != null) {
                connection.commit();
            }
        } catch (SQLException e) {
            printException(e);
        }
    }

    public static void printException(Exception e) {
        System.out.println("Exception caught! Exiting ..");
        System.out.println("error message: " + e.getMessage());
    }
}
