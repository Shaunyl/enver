package com.shaunyl.enver.command.monitoring.core;

import java.sql.ResultSet;

/**
 *
 * @author Shaunyl
 */
@Deprecated
public interface IMonitorMethod {

    public ResultSet monitor();
    public String getStatus();
}
