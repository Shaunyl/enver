package com.shaunyl.enver.command.writer.core;

import com.shaunyl.enver.DateFormats;
import com.shaunyl.enver.command.writer.IEnverWriter;
import com.shaunyl.enver.util.DatabaseUtil;
import com.shaunyl.enver.util.GeneralUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.NonNull;
import lombok.Setter;

/**
 *
 * @author Shaunyl
 */
public class MonBakOutputWriter implements IEnverWriter {

    private Writer rawWriter;

    private PrintWriter printer;

    @Setter
    private String instance = "<n.p.>";
    
    public MonBakOutputWriter(Writer writer) {
        this.rawWriter = writer;
        this.printer = new PrintWriter(writer);
    }
    protected void writeFooter(boolean isSuccess) {
        writeNext("\nJob completed " + (isSuccess ? "successfully " : "with errors ") + GeneralUtil.getCurrentDate(DateFormats.DASH_TIMEDATE));
    }

    protected void writeHeader() {
        writeNext("Enver Detailed Backup Report\n");
    }

    private final List<String> completed = new ArrayList<String>(), failed = new ArrayList<String>();

    private void buildFailureList(String[] record) {
//        if (!"COMPLETED".equals(record[1])) {
            failed.add(retrieveBackupInfo(record));
//        }
    }

    protected void elaborate(String[] record) {
        buildFailureList(record);
    }

    private String retrieveBackupInfo(String[] record) {
        String buffer = String.format("  %-11s %-20s %-25s %-200s",
                instance,
                record[0], record[1], record[2]);
        return buffer;
    }

    @Override
    public int writeAll(@NonNull final ResultSet rs, boolean includeColumnNames) throws SQLException, IOException {

        writeHeader();
        int rows = 0;
        try {
            ResultSetMetaData metadata = rs.getMetaData();

            int columnCount = metadata.getColumnCount();

            writeNext(new String[]{"Starting MonBakOutput on instance " + instance + " at " + GeneralUtil.getCurrentDate(DateFormats.DASH_TIMEDATE)});
            writeNext("\nRetrieving backup info from RMAN views..\n");

            while (rs.next()) {
                String[] nextLine = new String[columnCount];

                for (int i = 0; i < columnCount; i++) {
                    nextLine[i] = DatabaseUtil.getColumnValue(rs, metadata.getColumnType(i + 1), i + 1).trim();
                }
                elaborate(nextLine);
                rows++;
            }

            if (failed.isEmpty()) {
                writeNext("No failures found in v$rman_output view.");
            } else { // failed non vuota, oppure, all=y, oppure entrambe..
//                if (failed.isEmpty()) {
//                    writeNext("Check for failures/runs:");
//                    writeNext("> No failures found.");
//                }
                for (int i = 0; i < failed.size(); i++) {
                    if (i == 0) {
                        writeNext("Check for failures/runs:");
                    }
                    writeNext(failed.get(i));
                }
            }
//            for (int i = 0; i < completed.size(); i++) {
//                if (i == 0) {
//                    writeNext("\nHistory of successfull backup through last " + deep + " days:");
//                }
//                writeNext(completed.get(i));
//            }
            
            // query v$rman_output
            
            
            //
            
        } catch (SQLException ex) {
            writeFooter(false);
            throw new SQLException(ex.getMessage(), ex);
        } catch (IOException ex) {
            writeFooter(false);
            throw new IOException(ex.getMessage(), ex);
        }

        writeFooter(true);
        return rows;
    }

    @Override
    public void writeAll(@NonNull final List lines) {
        writeHeader();
        writeNext(new String[]{"Starting MonBak on instance " + instance + " at " + GeneralUtil.getCurrentDate(DateFormats.DASH_TIMEDATE)});

        writeNext("\nRetrieving backup info from RMAN views..");
        for (Iterator iter = lines.iterator(); iter.hasNext();) {
            String[] nextLine = (String[]) iter.next();
            retrieveBackupInfo(nextLine);
        }
        writeFooter(true);
    }

    public void writeNext(String[] nextLine) {
        StringBuilder sb = new StringBuilder();
        sb.append(nextLine[0]);
        sb.append("\n");
        printer.write(sb.toString());
    }

    public void writeNext(String nextLine) {
        printer.write(nextLine + "\n");
    }

    public void flush() throws IOException {
        printer.flush();
    }

    @Override
    public void close() throws IOException {
        printer.flush();
        printer.close();
        rawWriter.close();
    }

    @Override
    public String[] getValidFileExtensions() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
