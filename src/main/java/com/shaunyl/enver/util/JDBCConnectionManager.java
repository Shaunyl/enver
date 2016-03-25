package com.shaunyl.enver.util;

import com.shaunyl.enver.database.ConnectionFactory;
import com.shaunyl.enver.database.Database;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.DataSource;
import oracle.jdbc.pool.OracleDataSource;

public class JDBCConnectionManager implements ConnectionManager {

    @Override
    public Connection createConnection(DataSource datasource) throws SQLException {
        return datasource.getConnection();
    }

    @Override
    public DataSource createDatasource(Database database, String url, String user, String passwd) {
        DataSource datasource = null;
        try {
            datasource = ConnectionFactory.getDataSource(database);
            if (datasource.getClass().equals(OracleDataSource.class)) {

                Properties props = new Properties();
                props.put("user", user);
                props.put("password", passwd);
                if ("sys".equals(user.toLowerCase())) {
                    props.put("internal_logon", "sysdba");
                }

                ((OracleDataSource) datasource).setConnectionProperties(props);
                ((OracleDataSource) datasource).setURL(url);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("" + ex.getMessage(), ex); // fixme
        }
        return datasource;
    }

    // readConnectionString
    // readConnectionStrings
}
