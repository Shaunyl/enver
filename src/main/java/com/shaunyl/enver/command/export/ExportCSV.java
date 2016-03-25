package com.shaunyl.enver.command.export;

import com.shaunyl.enver.command.export.core.Export;
import com.shaunyl.enver.commandline.CommandStatus;
import com.shaunyl.enver.exception.ParseException;
import com.shaunyl.enver.exception.UnexpectedEnverException;
import com.shaunyl.enver.command.writer.core.CSVWriter;
import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.validators.PositiveInteger;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Shaunyl
 */
@Parameters(separators = "=")
public class ExportCSV extends Export implements IParameterValidator {

    @Parameter(required = true, arity = 1, validateWith = ExportCSV.class)
    private List<String> cmd = Lists.newArrayList(1);

    @Parameter(names = "-start", arity = 1, validateWith = PositiveInteger.class)
    private Integer start = -1;

    @Parameter(names = "-end", arity = 1, validateWith = PositiveInteger.class)
    private Integer end = -1;
    
    @Parameter(names = "-delimiter", arity = 1)
    private char delimiter = ',';
    
    int w = 0, e = 0, t = 0;
    
    public ExportCSV() {
        super(null);
    }
    
    public ExportCSV(final CommandStatus status) {
           super(status);
    }

    @Override
    public void validate(String name, String value)
            throws ParameterException {
        if (!"expcsv".equals(value)) {
            throw new ParameterException("Parameter " + name
                    + " should be \"expcsv\" (found " + value + ")");
        }
    }

    @Override
    public void parse() throws ParseException {
        status.print("Validating parameters...");
        extension = "csv";

        if (start != -1 || end != -1) {
            status.print("You have been asked to retrieve a range of lines: (%d, %s)", start == -1 ? 1 : start, end == -1 ? ":end" : end + "");
        }
        super.parse();
    }

    @Override
    public void setup() {
        try {
            super.setup();
            statement = connection.createStatement();
        } catch (SQLException ex) {
        }
    }

    @Override
    public void taskAtomic(final int i) throws IOException {
        filename = filename == null ? String.format("%s/%s-%s.%s", directory, instance, tablecount > 0 ? tables.get(i) : "query" + (i + 1), extension)
                : String.format("%s/%d-%s", directory, (i + 1), filename);

        try {
            writer = (delimiter == '\u0000') ? new CSVWriter(new FileWriter(filename), start, end)
                    : new CSVWriter(new FileWriter(filename), delimiter, start, end);
        } catch (IOException e) {
            throw new UnexpectedEnverException(e.getMessage(), e);
        }
        super.taskAtomic(i);
        w = warnings - w;
        e = errors - e;
        if (w + e > 0) {
//            status.print("Data not exported.");
        } else {
//            status.print("Data successfully exported to dump file \"%s\".", filename);
            t++;
        }
        filename = null;
//        status.print("Partial time: " + getPartialTime() + " ms");
        status.print("************");
    }

    @Override
    public void takedown() {
        super.takedown();
    }
}
