package com.shaunyl.enver.util;

import com.shaunyl.enver.database.Database;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 *
 * @author Shaunyl
 */
public interface ConnectionManager {

    Connection createConnection(DataSource datasource) throws SQLException;

    public DataSource createDatasource(Database database, String url, String user, String passwd);
    
}
