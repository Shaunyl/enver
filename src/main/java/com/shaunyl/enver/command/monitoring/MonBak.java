package com.shaunyl.enver.command.monitoring;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.validators.PositiveInteger;
import com.shaunyl.enver.DateFormats;
import com.shaunyl.enver.command.DatabaseCommandAction;
import com.shaunyl.enver.command.Query;
import com.shaunyl.enver.command.support.CharBooleanValidator;
import com.shaunyl.enver.command.writer.core.MonBakWriter;
import com.shaunyl.enver.commandline.CommandStatus;
import com.shaunyl.enver.exception.ParseException;
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
public class MonBak extends DatabaseCommandAction implements IParameterValidator {

    @Parameter(required = true, arity = 1, validateWith = MonBak.class)
    private List<String> cmd = Lists.newArrayList(1);

    @Parameter(names = "-directory", arity = 1)
    protected String directory = ".";

//    @Parameter(names = "-filename", arity = 1)
    protected String filename = "";

    @Parameter(names = "-deep", arity = 1, validateWith = PositiveInteger.class)
    public Integer deep = 3;

    @Parameter(names = "-all", validateWith = CharBooleanValidator.class)
    public String all = "n";

    private String query, comment = "";

    private MonBakWriter writer = null;

    public MonBak() {
        super(null);
    }

    public MonBak(final CommandStatus status) {
        super(status);
    }

    @Override
    public void validate(String name, String value) throws ParameterException {
        if (!"monbak".equals(value)) {
            throw new ParameterException("Parameter " + name
                    + " should be \"monbak\" (found " + value + ")");
        }
    }

    @Override
    public void parse() throws ParseException {
        super.parse();
    }

    private Query createQuery(Integer deep, String comment) {

        String sqltext = "SELECT input_type, status, \n"
                + " TO_CHAR(start_time, 'yyyy-MON-dd hh24:mi:ss'), \n"
                + " ROUND(elapsed_seconds / 3600, 2) elapsed_hours \n"
                + " FROM v$rman_backup_job_details WHERE \n"
                + " start_time > sysdate - %d \n"
                + " %s AND status <> 'COMPLETED' \n"
                + " ORDER BY start_time, 1";
        
        return new Query(sqltext, deep, comment);
    }

    @Override
    public void setup() {
        super.setup();

        boolean isAll = "y".equals(all); //n
        if (isAll) {
            comment = "--"; // if --> n
        }
        query = this.createQuery(deep, comment).getSqltext();
        filename = String.format("%s/%s-%s.txt", directory, this.instance, GeneralUtil.getCurrentDate(DateFormats.SQUELCHED_TIMEDATE));
        try {
            writer = new MonBakWriter(new FileWriter(filename), deep, isAll);
            writer.setInstance(instance);
        } catch (IOException e) {
            throw new UnexpectedEnverException("ERR (" + this.instance + "): " + e.getMessage(), e);
        }

        try {
            statement = connection.createStatement();
        } catch (SQLException e) {
            throw new UnexpectedEnverException("ERR (" + this.instance + "): " + e.getMessage(), e);
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
