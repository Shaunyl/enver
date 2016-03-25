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
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import lombok.NonNull;
import lombok.Setter;

/**
 *
 * @author Shaunyl
 */
public class MonTbsAutoWriter implements IEnverWriter {

    private Writer rawWriter;

    private PrintWriter printer;

    private int threshold;

    private String undo;

    @Setter
    private String instance = "<n.p.>";

    public MonTbsAutoWriter(Writer writer, int threshold) {
        this.threshold = threshold;
        this.rawWriter = writer;
        this.printer = new PrintWriter(writer);
    }

    protected void writeFooter(boolean isSuccess) {
        writeNext("\nJob completed " + (isSuccess ? "successfully " : "with errors ") + GeneralUtil.getCurrentDate(DateFormats.DASH_TIMEDATE));
    }

    protected void thresholdUsed() {
        writeNext("Threshold used -> " + threshold);
    }

    protected void writeHeader() {
        writeNext("Alarm Auto Extend - " + instance + "\n");
    }

    protected boolean elaborate(String[] record) {
        float csize = Float.parseFloat(record[9]);

        if (csize > threshold) {
            retrieveTbsInfo(record);
            return true;
        }
        else {
            writeNext("All datafiles are sub-threshold for tablespace " + record[1]);
        }
        return false;
    }

    private void retrieveTbsInfo(String[] record) {
        
        String autoextension = Double.parseDouble(record[8]) == 0 ? "[OK]" : "[WARN(";
        boolean warn = false;
        if ("[WARN(".equals(autoextension)) {
            warn = true;
            autoextension = autoextension + (int)Double.parseDouble(record[8]) + ")]";
        }

        double size_mb = Double.parseDouble(record[3]) / 1024;
        double max_mb = Double.parseDouble(record[4]) / 1024;
        double used_mb = Double.parseDouble(record[5]) / 1024;
        double free_mb = Double.parseDouble(record[6]) / 1024;
        DecimalFormat formatter = new DecimalFormat("#,###.00");
        String size = formatter.format(size_mb);
        String max = formatter.format(max_mb);
        String used = formatter.format(used_mb);
        String free = formatter.format(free_mb);
        String buffer = String.format("  %-10s%-38s%-25s%-23s%11.2f%%",
                instance,
                record[1] + "[" + size + "/" + max + "]",
                "AUTOEXTEND" + autoextension,
                "[" + used + "/" + free + "]",
                // warn ? Float.valueOf(record[7]) : Float.valueOf(record[9]));
                Float.valueOf(record[9]));
        writeNext(new String[]{buffer});
    }
    // TABLESPACE_NAME STATUS SIZE_MB MAX_MB USED_MB FREE_MB USED_PCT AUTOEXTENSIBLE AUTO_USED_PCT
    // BTM_PRO  TBS01[511,00/32768,00]  AUTOEXTEND[OK]    [used/free]  (12/51)

    @Override
    public int writeAll(@NonNull final ResultSet rs, boolean includeColumnNames) throws SQLException, IOException {

        writeHeader();
        boolean alarm = false;
        int rows = 0;
        try {
            ResultSetMetaData metadata = rs.getMetaData();

            int columnCount = metadata.getColumnCount();

            writeNext(new String[]{"Starting MonTbsAuto on instance " + instance + " at " + GeneralUtil.getCurrentDate(DateFormats.DASH_TIMEDATE)});
            thresholdUsed();
            writeNext("\nRetrieving tablespaces info..\n");
            while (rs.next()) {
                String[] nextLine = new String[columnCount];

                for (int i = 0; i < columnCount; i++) {
                    nextLine[i] = DatabaseUtil.getColumnValue(rs, metadata.getColumnType(i + 1), i + 1).trim();
                }
                boolean result = elaborate(nextLine);
                if (result && !alarm) {
                    alarm = true;
                }
            }
        } catch (SQLException ex) {
            writeFooter(false);
            throw new SQLException(ex.getMessage(), ex);
        } catch (IOException ex) {
            writeFooter(false);
            throw new IOException(ex.getMessage(), ex);
        }
        if (!alarm) {
            writeNext(new String[]{"No alarms were detected.\n"});
        }
        writeFooter(true);
        return rows;
    }

    @Override
    public void writeAll(@NonNull final List lines) {
        writeHeader();
        writeNext(new String[]{"Starting MonTbsAuto on instance " + instance + " at " + GeneralUtil.getCurrentDate(DateFormats.DASH_TIMEDATE)});
        thresholdUsed();
        writeNext("\nRetrieving tablespaces info..");
        for (Iterator iter = lines.iterator(); iter.hasNext();) {
            String[] nextLine = (String[]) iter.next();
            elaborate(nextLine);
            writeNext(nextLine);
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
