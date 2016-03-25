package com.shaunyl.enver.command;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.internal.Lists;
import com.shaunyl.enver.DateFormats;
import com.shaunyl.enver.command.writer.core.MonBakOutputWriter;
import com.shaunyl.enver.commandline.CommandStatus;
import com.shaunyl.enver.exception.UnexpectedEnverException;
import com.shaunyl.enver.util.GeneralUtil;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Shaunyl
 */
@Parameters(separators = "=")
public class MonBakOutput extends DatabaseCommandAction implements IParameterValidator {

    @Parameter(required = true, arity = 1, validateWith = MonBakOutput.class)
    private List<String> cmd = Lists.newArrayList(1);

    private String query = "";

    @Parameter(names = "-directory", arity = 1)
    protected String directory = ".";

    protected String filename = "";

    private MonBakOutputWriter writer = null;

    public MonBakOutput() {
        super(null);
    }

    public MonBakOutput(final CommandStatus status) {
        super(status);
    }

    @Override
    public void validate(String name, String value) throws ParameterException {
        if (!"monbako".equals(value)) {
            throw new ParameterException("Parameter " + name
                    + " should be \"monbako\" (found " + value + ")");
        }
    }

    private Query createQuery() {

//        String sqltext = "SELECT output\n"
//                + " FROM v$rman_output\n"
//                + " WHERE session_recid IN (SELECT session_recid FROM v$rman_status\n"
//                + " WHERE start_time BETWEEN TRUNC(SYSDATE) - 30 AND SYSDATE AND operation = 'BACKUP'\n"
//                + "  AND regexp_like(output, '^RMAN-|^ORA-')) \n"
//                + " ORDER BY recid";
        String sqltext = "select TO_CHAR(rs.start_time, 'dd-MON-yy hh24:mi:ss') start_time, rs.status, ro.output\n"
                + " from v$rman_output ro, v$rman_status rs\n"
                + " where ro.session_recid = rs.session_recid\n"
                + " and start_time between TRUNC(sysdate) - 30 and sysdate\n"
                + " and regexp_like(output, '^RMAN-|^ORA-')\n"
                + " and operation = 'BACKUP'";

        return new Query(sqltext);
    }

    @Override
    public void setup() {
        super.setup();

        query = this.createQuery().getSqltext();
        filename = String.format("%s/%s-%s-%s.txt", directory, this.instance, "BAK_OUTPUT", GeneralUtil.getCurrentDate(DateFormats.SQUELCHED_TIMEDATE));
        try {
            writer = new MonBakOutputWriter(new FileWriter(filename));
            writer.setInstance(instance);
        } catch (IOException e) {
            throw new UnexpectedEnverException("Error (" + this.instance + "): " + e.getMessage(), e);
        }

        try {
            statement = connection.createStatement();
        } catch (SQLException e) {
            throw new UnexpectedEnverException("Error (" + this.instance + "): " + e.getMessage(), e);
        }
    }

    @Override
    public void taskAtomic(final int i) throws IOException {
        try {
            resultSet = statement.executeQuery(query);
            writer.writeAll(resultSet, true);
            status.print("Report file '%s' generated.", filename);
        } catch (SQLException e) {
            status.print("Warnings on '%s' query\n  > %s", query, e.getMessage());
            warnings += 1;
            status.print("Report not generated due to errors.");
            throw new UnexpectedEnverException("ERR (" + this.instance + "): " + e.getMessage(), e);
        } catch (IOException e) {
            status.print("Report not generated due to errors.");
            throw new UnexpectedEnverException("ERR (" + this.instance + "): " + e.getMessage(), e);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                throw new UnexpectedEnverException(e.getMessage(), e);
            }
        }
    }

    @Override
    public void takedown() {
        super.takedown();
    }
}
