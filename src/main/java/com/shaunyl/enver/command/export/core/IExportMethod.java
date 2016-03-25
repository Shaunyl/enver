package com.shaunyl.enver.command.export.core;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

/**
 *
 * @author Shaunyl
 */
public interface IExportMethod { 

    public ResultSet export(Callback callback, String user, int i, boolean scope);
    public void setObjects(List<String> objects);
    void setStatement(Statement statement);

    public void parse();
}
