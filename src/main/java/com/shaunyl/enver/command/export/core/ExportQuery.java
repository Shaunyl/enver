package com.shaunyl.enver.command.export.core;

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
public class ExportQuery implements IExportMethod {

    @Setter
    private List<String> objects;

    @Setter
    public Statement statement;

    @Override
    public ResultSet export(Callback callback, String user, int i, boolean scope) {
        String query = objects.get(i).trim();
        try {
            ResultSet rs = statement.executeQuery(query);
            callback.notifyProgress(String.format(". . exported from: [%s]", query));
            return rs;
        } catch (SQLException ex) {
            callback.notifyProgress("error: [" + query + "] - " + ex.getMessage().trim());
//            throw new RuntimeException(ex.getMessage() + " (\"" + query + "\")", ex);
            return null;
        }
    }

    @Override
    public void parse() {
    }
}
