package com.shaunyl.enver.command;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.internal.Lists;
import com.shaunyl.enver.DateFormats;
import com.shaunyl.enver.command.writer.core.MonTbsAutoWriter;
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
public class MonTbsAuto extends DatabaseCommandAction implements IParameterValidator {

    @Parameter(required = true, arity = 1, validateWith = MonTbsAuto.class)
    private List<String> cmd = Lists.newArrayList(1);

    @Parameter(names = "-directory", arity = 1)
    protected String directory = ".";

    protected String filename = "";

    private String query;

    private MonTbsAutoWriter writer = null;

    @Parameter(names = "-threshold", arity = 1)
    private int threshold = 85;

    public MonTbsAuto() {
        super(null);
    }

    public MonTbsAuto(final CommandStatus status) {
        super(status);
    }

    @Override
    public void validate(String name, String value) throws ParameterException {
        if (!"montbsauto".equals(value)) {
            throw new ParameterException("Parameter " + name
                    + " should be \"montbsauto\" (found " + value + ")");
        }
    }

    @Override
    public void parse() throws ParseException {
        super.parse();
    }

    @Override
    public void setup() {
        super.setup();
                query = "SELECT (SELECT name FROM v$database) database_name, d.tablespace_name, d.status status, a.bytes / 1048576 size_mb,"
                + "  TO_NUMBER(DECODE(m.aut_max_mb, 0, NULL, m.aut_max_mb)) max_mb,"
                + "  (a.bytes - DECODE(f.bytes, NULL, 0, f.bytes)) / 1048576 used_mb,"
                + "  (a.bytes / 1048576 - (a.bytes - DECODE(f.bytes, NULL, 0, f.bytes)) / 1048576) free_mb,"
                + "  ROUND(((a.bytes - DECODE(f.bytes, NULL, 0, f.bytes)) * 100) / a.bytes) used_pct, m.autoextensible,"
                + "  DECODE(ROUND(((a.bytes - DECODE(f.bytes, NULL, 0, f.bytes)) / 1048576) / TO_NUMBER(DECODE(m.aut_max_mb, 0, NULL, m.aut_max_mb)) * 100), null, -1,"
                + "    ROUND(((a.bytes - DECODE(f.bytes, NULL, 0, f.bytes)) / 1048576) / TO_NUMBER(DECODE(m.aut_max_mb, 0, NULL, m.aut_max_mb)) * 100)) auto_used_pct"
                + " FROM sys.dba_tablespaces d, sys.sm$ts_avail a, sys.sm$ts_free f,"
                + "  (SELECT x.tablespace_name, SUM(y.maxbytes / 1048576) aut_max_mb, SUM(DECODE(y.autoextensible, 'NO', 1, 0)) autoextensible"
                + "   FROM sys.dba_tablespaces x, sys.dba_data_files y"
                + "   WHERE x.tablespace_name = y.tablespace_name"
                + "   GROUP BY x.tablespace_name) m"
                + " WHERE d.tablespace_name = a.tablespace_name"
                + "  AND f.tablespace_name(+) = d.tablespace_name"
                + "  AND d.tablespace_name = m.tablespace_name"
                + "  AND d.contents = 'PERMANENT'"
                + " ORDER BY 10 DESC";

        filename = String.format("%s/%s-%s.txt", directory, this.instance, GeneralUtil.getCurrentDate(DateFormats.SQUELCHED_TIMEDATE));

        try {
            writer = new MonTbsAutoWriter(new FileWriter(filename), threshold);
            writer.setInstance(instance);
        } catch (IOException e) {
            throw new UnexpectedEnverException("ERR: " + e.getMessage(), e);
        }

        try {
            statement = connection.createStatement();
        } catch (SQLException e) {
            throw new UnexpectedEnverException("ERR: " + e.getMessage(), e);
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
            throw new UnexpectedEnverException("ERR: " + e.getMessage(), e);
        } catch (IOException e) {
            status.print("Report not generated due to errors.");
            throw new UnexpectedEnverException("ERR: " + e.getMessage(), e);
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
