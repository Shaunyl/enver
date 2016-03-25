package com.shaunyl.enver.command.export;

import com.shaunyl.enver.command.export.core.Export;
import com.shaunyl.enver.commandline.CommandStatus;
import com.shaunyl.enver.exception.ParseException;
import com.shaunyl.enver.exception.UnexpectedEnverException;
import com.shaunyl.enver.command.writer.core.TabularWriter;
import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Filippo
 */
@Parameters(separators = "=")
public class ExportTabular extends Export implements IParameterValidator {

    @Parameter(required = true, arity = 1, validateWith = ExportTabular.class)
    private List<String> cmd = Lists.newArrayList(1);
    
    @DynamicParameter(names = "-C")
    private Map<String, Integer> colformats = Maps.newHashMap();

    int w = 0, e = 0, t = 0;

    public ExportTabular() {
        super(null);
    }

    public ExportTabular(final CommandStatus status) {
        super(status);
    }

    @Override
    public void validate(String name, String value)
            throws ParameterException {
        if (!"exp".equals(value)) {
            throw new ParameterException("Parameter " + name
                    + " should be \"exp\" (found " + value + ")");
        }
    }

    @Override
    public void parse() throws ParseException {
        super.parse(); // parent parsing..
    }

    @Override
    public void setup() {
        super.setup();

        try {
            statement = connection.createStatement();
        } catch (SQLException ex) {
        }
    }

    @Override
    public void taskAtomic(final int i) throws IOException {
        filename = filename == null ? String.format("%s/%s-%s.%s", directory, instance, tablecount > 0 ? tables.get(i) : "query" + (i + 1), extension)
                : String.format("%s/%d-%s", directory, (i + 1), filename);

        try {
            writer = new TabularWriter(new FileWriter(filename), colformats);
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
//        status.print("Elapsed time: " + getPartialTime() + " ms");
//        status.print("************");
    }
}
