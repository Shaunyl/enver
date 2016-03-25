package com.shaunyl.enver.command;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.validators.PositiveInteger;
import com.shaunyl.enver.DateFormats;
import com.shaunyl.enver.command.writer.core.MonTbsTrendWriter;
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
public class MonTbsTrend extends DatabaseCommandAction implements IParameterValidator {

    @Parameter(required = true, arity = 1, validateWith = MonTbsTrend.class)
    private List<String> cmd = Lists.newArrayList(1);

    @Parameter(names = "-directory", arity = 1)
    protected String directory = ".";

    protected String filename = "";

    @Parameter(names = "-deep", arity = 1, validateWith = PositiveInteger.class)
    public Integer deep = 3;

    @Parameter(names = "-threshold", arity = 1, validateWith = PositiveInteger.class)
    public Integer threshold = 341;

    private String query;

    private MonTbsTrendWriter writer = null;

    public MonTbsTrend() {
        super(null);
    }

    public MonTbsTrend(final CommandStatus status) {
        super(status);
    }

    @Override
    public void parse() throws ParseException {
        super.parse();
        if (deep == 0) {
            throw new ParseException("Parameter \"deep\" must be greather than 0.");
        }
    }

    @Override
    public void validate(String name, String value) throws ParameterException {
        if (!"montbstrend".equals(value)) {
            throw new ParameterException("Parameter " + name
                    + " should be \"montbstrend\" (found " + value + ")");
        }
    }

    @Override
    public void setup() {
        super.setup();

//        query = "WITH tbs_delta AS (\n"
//                + "  SELECT /*+ materialize parallel(a, 6) ordered */ b.name,\n"
//                + "  (SELECT name FROM v$database) database_name, rtime,\n"
//                + "  ROUND((tablespace_usedsize *\n"
//                + "   (SELECT value FROM v$parameter WHERE name = 'db_block_size')) / 1048576, 2) -\n"
//                + "    LAG(ROUND((tablespace_usedsize *\n"
//                + "    (SELECT value FROM v$parameter WHERE name = 'db_block_size')) / 1048576, 2))\n"
//                + "      OVER (PARTITION BY b.name ORDER BY a.snap_id) delta_usedsize_mb\n"
//                + "  FROM dba_hist_tbspc_space_usage a\n"
//                + "    JOIN v$tablespace b ON (a.tablespace_id = b.ts#)\n"
//                + "    JOIN dba_tablespaces d ON (d.tablespace_name = b.name)\n"
//                + "  WHERE d.contents = 'PERMANENT' and TRUNC(TO_DATE(rtime, 'mm/dd/yyyy hh24:mi:ss')) > TRUNC(SYSDATE - " + deep + ")\n"
//                + "),\n"
//                + "tbs_free AS (SELECT tablespace_name, bytes free_size FROM sys.sm$ts_free)\n"
//                + " SELECT database_name, name tablespace_name, free_size,\n"
//                + "   MIN(rtime) start_time, MAX(rtime) end_time, TO_CHAR(SUM(delta_usedsize_mb), '999,999,999.99') delta_usedsize_mb\n"
//                + " FROM tbs_delta t, tbs_free s\n"
//                + " WHERE t.name = s.tablespace_name\n"
//                + " HAVING SUM(delta_usedsize_mb) > " + deep + " * " + threshold + "\n"
//                + " GROUP BY database_name, name, free_size\n"
//                + " ORDER BY end_time";
        query = "WITH tbs_delta AS (\n"
                + "  SELECT /*+ materialize ordered */ b.name, rtime,\n"
                + "  ROUND((tablespace_usedsize *\n"
                + "   (SELECT value FROM v$parameter WHERE name = 'db_block_size')) / 1048576, 2) -\n"
                + "    LAG(ROUND((tablespace_usedsize *\n"
                + "    (SELECT value FROM v$parameter WHERE name = 'db_block_size')) / 1048576, 2))\n"
                + "      OVER (PARTITION BY b.name ORDER BY a.snap_id) delta_usedsize_mb\n"
                + "  FROM dba_hist_tbspc_space_usage a\n"
                + "    JOIN v$tablespace b ON (a.tablespace_id = b.ts#)\n"
                + "    JOIN dba_tablespaces d ON (d.tablespace_name = b.name)\n"
                + "  WHERE d.contents = 'PERMANENT' AND TRUNC(TO_DATE(rtime, 'mm/dd/yyyy hh24:mi:ss')) > TRUNC(SYSDATE - 30)\n"
                + "),\n"
                + " tbs_size AS (SELECT tablespace_name, bytes free_size FROM sys.sm$ts_free),\n"
                + " tbs_all AS (\n"
                + " SELECT name tablespace_name, SUM(delta_usedsize_mb) delta_month_mb,\n"
                + "  SUM(delta_usedsize_mb) / 30 daily_trend_last_month_mb\n"
                + " FROM tbs_delta t\n"
                + " GROUP BY name),\n"
                + " tbs_last AS (\n"
                + " SELECT name tablespace_name, SUM(delta_usedsize_mb) delta_days_mb,\n"
                + "  SUM(delta_usedsize_mb) / " + deep + " daily_trend_last_days_mb\n"
                + " FROM tbs_delta t\n"
                + " WHERE TO_DATE(rtime, 'mm/dd/yyyy hh24:mi:ss') > (SYSDATE - " + deep + ")\n"
                + " HAVING SUM(delta_usedsize_mb) > " + deep + " * " + threshold + "\n"
                + " GROUP BY name)\n"
                + " SELECT (SELECT name FROM v$database) database_name, l.tablespace_name, s.free_size, a.delta_month_mb, l.delta_days_mb\n" //", a.daily_trend_last_month_mb, l.daily_trend_last_days_mb\n"
                + " FROM tbs_size s, tbs_all a, tbs_last l\n"
                + " WHERE s.tablespace_name = a.tablespace_name AND a.tablespace_name = l.tablespace_name\n"
                + " ORDER BY 4";

        filename = String.format("%s/%s-%s.txt", directory, this.instance, GeneralUtil.getCurrentDate(DateFormats.SQUELCHED_TIMEDATE));

        try {
            writer = new MonTbsTrendWriter(new FileWriter(filename), deep, threshold);
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
                throw new UnexpectedEnverException("ERR (" + this.instance + "): " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void takedown() {
        super.takedown();
    }
}
