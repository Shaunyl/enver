package com.shaunyl.enver.command.export.core;

import com.shaunyl.enver.util.DatabaseUtil;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Shaunyl
 */
@NoArgsConstructor
public class ExportTable implements IExportMethod {

    @Setter
    public List<String> objects;

    @Setter
    public Statement statement;

    @Override
    public ResultSet export(Callback callback, String user, int i, boolean scope) {
        String table = fixTableName(objects.get(i));
        String size = getTableSize(user, table, scope);
        try {
            ResultSet rs = statement.executeQuery(String.format("SELECT * FROM %s.%s", user, table));
            callback.notifyProgress(String.format(". . exported table %s.%-32s %-30s", "\"" + user + "\"", "\"" + table + "\"", size));
            return rs;
        } catch (SQLException ex) {
            callback.notifyProgress(String.format("error: %s.%s - %s", "\"" + user + "\"", "\"" + table + "\"", ex.getMessage().trim()));
            return null; // FIXME
        }
    }

    // FIXME: you ougth to do it first, during the input of the args..
    private String fixTableName(String table) {
        return table.trim().toUpperCase();
    }

    private String getTableSize(String user, String table, boolean scope) {
        return DatabaseUtil.getTableSize(statement, user, table, scope);
    }

    @Override
    public void parse() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
