package com.shaunyl.enver.command.export.core;

import com.shaunyl.enver.commandline.CommandStatus;
import com.shaunyl.enver.command.support.SemicolonParameterSplitter;
import com.shaunyl.enver.exception.ParseException;
import com.shaunyl.enver.command.writer.IEnverWriter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.CommaParameterSplitter;
import com.beust.jcommander.internal.Lists;
import com.shaunyl.enver.BeanFactory;
import com.shaunyl.enver.command.DatabaseCommandAction;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Shaunyl
 */
public abstract class Export extends DatabaseCommandAction {

    @Parameter(names = "-schema", arity = 1)
    protected String schema;

    @Parameter(names = "-tables", splitter = CommaParameterSplitter.class, variableArity = true)
    protected List<String> tables = Lists.newArrayList();

    @Parameter(names = "-queries", splitter = SemicolonParameterSplitter.class, variableArity = true)
    protected List<String> queries = Lists.newArrayList();

    // Patterns:
    //  %i: instance
    //  %u: username
    //  example: %u_%i-query%n.txt
    // default: %u-query-%n.txt
    @Parameter(names = "-filename", arity = 1)
    protected String filename;

    @Parameter(names = "-directory", arity = 1)
    protected String directory = ".";

    protected IEnverWriter writer;

    protected String extension = "txt";

    protected List<String> entities = Lists.newArrayList();

    protected int querycount, tablecount, items;

    private boolean scope = true;

    private IExportMethod iExportMethod = BeanFactory.getInstance().getBean(IExportMethod.class);

    public Export(final CommandStatus status) {
        super(status);
    }

    @Override
    public void parse() throws ParseException {

//        status.print("parsing parameters...");
        tablecount = tables.size();
        querycount = queries.size();
        if (querycount + tablecount == 0) {
            throw new ParseException("error: at least, one of the following parameters needs to be specified: queries, tables");
        }
        if (querycount * tablecount > 0) {
            throw new ParseException("error: only one of the following parameters at time can be used: queries, tables");
        }
//        iExportMethod.parse(); FIXME
        if (schema != null && querycount > 0) {
            throw new ParseException("error: parameter \"schema\" cannot be used in conjunction with query mode");
        }
        // status.print("Found duplicate values for parameter \"tables\". Those ones will be ignored."); // FIXME: do it first

        if (!entities.addAll(tables)) {
            entities.addAll(queries);
        }
        removeDuplicates();
        items = entities.size();
        this.setCycle(items);
        super.parse();
    }

    @Override
    public void setup() {
        super.setup();
        if (tablecount > 0) {
            iExportMethod = new ExportTable();
            status.print("Export Mode: table");
            if (items < querycount + tablecount) {
                status.print("note: duplicates found for parameter \"tables\". They will be ignored..");
            }
        } else {
            iExportMethod = new ExportQuery();
            status.print("Export Mode: query");
            if (items < querycount + tablecount) {
                status.print("note: duplicates found for parameter \"query\". They will be ignored..");
            }
        }
        iExportMethod.setObjects(entities);
        scope = user.equals(schema);
        try {
            this.statement = connection.createStatement();
            iExportMethod.setStatement(statement);
        } catch (SQLException ex) {
            throw new RuntimeException("Cannot connect..", ex);
        }
        status.print("");
    }

    @Override
    public void taskAtomic(final int i) throws IOException {

        final String[] progress = new String[1];
        resultSet = iExportMethod.export(new Callback() {
            @Override
            public void notifyProgress(final String message) {
//                status.print(message);
                progress[0] = message;
            }
        }, schema == null ? user : schema, i, scope); //FIXME!!!!

        try {
            int rows = writer.writeAll(resultSet, true);
            status.print(progress[0] + "\t" + rows + " rows");
        } catch (Exception e) {
            status.print(progress[0]);
//            status.print("Warnings:\n  > %s", e.getMessage());
            warnings += 1;
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private void removeDuplicates() {
        Set<String> hs = new TreeSet<String>();
        for (String entity : entities) {
            hs.add(entity);
        }
        entities.clear();
        for (String entity : hs) {
            entities.add(entity);
        }
    }
}
