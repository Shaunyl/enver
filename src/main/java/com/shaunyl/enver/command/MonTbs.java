package com.shaunyl.enver.command;

import com.shaunyl.enver.commandline.CommandStatus;
import com.shaunyl.enver.command.support.CharBooleanValidator;
import com.shaunyl.enver.DateFormats;
import com.shaunyl.enver.exception.ParseException;
import com.shaunyl.enver.exception.UnexpectedEnverException;
import com.shaunyl.enver.command.writer.core.MonTbsWriter;
import com.shaunyl.enver.util.GeneralUtil;
import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.CommaParameterSplitter;
import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.validators.PositiveInteger;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Filippo
 */
@Parameters(separators = "=")
public class MonTbs extends DatabaseCommandAction implements IParameterValidator {

    @Parameter(required = true, arity = 1, validateWith = MonTbs.class)
    private List<String> cmd = Lists.newArrayList(1);

    @Parameter(names = "-directory", arity = 1)
    protected String directory = ".";

    protected String filename = "";

    @Parameter(names = "-critical", arity = 1, validateWith = PositiveInteger.class)
    public Integer critical = 95;

    @Parameter(names = "-warning", arity = 1, validateWith = PositiveInteger.class)
    public Integer warning = 85;

    @Parameter(names = "-undo", validateWith = CharBooleanValidator.class)
    public String undo = "y";

    @Parameter(names = "-exclude", splitter = CommaParameterSplitter.class, variableArity = true)
    protected List<String> exclude = Lists.newArrayList();

    private String query;

    private MonTbsWriter writer = null;

    private String comment = "--", comment2 = "--", inexclude = "";

    public MonTbs() {
        super(null);
    }

    public MonTbs(final CommandStatus status) {
        super(status);
    }

    @Override
    public void validate(String name, String value) throws ParameterException {
        if (!"montbs".equals(value)) {
            throw new ParameterException("Parameter " + name
                    + " should be \"montbs\" (found " + value + ")");
        }
    }

    @Override
    public void parse() throws ParseException {
        super.parse();
        if (warning < 1 || warning > 99) {
            throw new ParseException("Parameter \"warning\" need to be in the range from 1 to 99.");
        }
        if (critical < 1 || critical > 99) {
            throw new ParseException("Parameter \"critical\" need to be in the range from 1 to 99.");
        }

        if (!exclude.isEmpty()) {
            inexclude = "(";
        }
        for (int i = 0; i < exclude.size(); i++) {
            comment = "";
            inexclude += "'" + exclude.get(i).toUpperCase() + "'";

            if (i == exclude.size() - 1) {
                inexclude += ")";
            } else {
                inexclude += ",";
            }
        }
    }

    @Override
    public void setup() {
        super.setup();

        String inundo = "('UNDO')";
        if ("n".equals(undo)) {
            comment2 = "";
        }

        query = "SELECT (SELECT name FROM v$database) database_name, d.tablespace_name, "
                + "a.bytes size_bytes, a.bytes - DECODE(f.bytes, NULL, 0, f.bytes) used_bytes, "
                + "a.bytes - (a.bytes - DECODE(f.bytes, NULL, 0, f.bytes)) free_bytes, "
                + "TO_CHAR((((a.bytes - DECODE(f.bytes, NULL, 0, f.bytes)) / 1048576) * 100) / (a.bytes / 1048576), '999.99') used_pct, "
                + "sysdate last_updated\nFROM sys.dba_tablespaces d, sys.sm$ts_avail a, sys.sm$ts_free f "
                + "WHERE d.tablespace_name = a.tablespace_name AND f.tablespace_name (+) = d.tablespace_name \n"
                + comment + " AND a.tablespace_name NOT IN " + inexclude + "\n"
                + " " + comment2 + " AND d.contents NOT IN " + inundo + "\n"
                + " AND (((a.bytes - DECODE(f.bytes, NULL, 0, f.bytes)) / 1048576) * 100) / (a.bytes / 1048576) > " + warning
                + "\nORDER BY 6 DESC";

        filename = String.format("%s/%s-%s.txt", directory, this.instance, GeneralUtil.getCurrentDate(DateFormats.SQUELCHED_TIMEDATE));

        try {
            writer = new MonTbsWriter(new FileWriter(filename), warning, critical, undo, exclude);
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
