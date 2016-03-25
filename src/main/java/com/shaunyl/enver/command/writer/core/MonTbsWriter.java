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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.NonNull;
import lombok.Setter;

/**
 *
 * @author Filippo
 */
public class MonTbsWriter implements IEnverWriter {

    private Writer rawWriter;

    private PrintWriter printer;

    private int cthreshold, wthreshold;

    private static final String IS_UNDO = "y";

    private String undo;

    @Setter
    private String instance = "<n.p.>";

    private List<String> exclude = null;

    boolean isCritical, isWarning = false;

    public MonTbsWriter(Writer writer, int wthreshold, int cthreshold) {
        this(writer, wthreshold, cthreshold, IS_UNDO, new ArrayList<String>());
    }

    public MonTbsWriter(Writer writer, int wthreshold, int cthreshold, String undo, List<String> exclude) {
        this.wthreshold = wthreshold;
        this.cthreshold = cthreshold;
        this.undo = undo;
        this.exclude = exclude;
        this.rawWriter = writer;
        this.printer = new PrintWriter(writer);
    }

    protected void writeFooter(boolean isSuccess) {
        writeNext("\nJob completed " + (isSuccess ? "successfully " : "with errors ") + GeneralUtil.getCurrentDate(DateFormats.DASH_TIMEDATE));
    }

    protected void ignoreTbsList() {
        String tbsc = "";
        if ("n".equals(undo)) {
            tbsc += "UNDO";
            writeNext("Ignoring tablespace contents -> " + tbsc);
        }
        String tbsl = "";
        boolean isExclude = false;
        if (!exclude.isEmpty()) {
            tbsl = "Other tablespaces to ignore -> '" + exclude.get(0) + "'";
            isExclude = true;
        }
        for (int i = 1; i < exclude.size(); i++) {
            tbsl += ", '" + exclude.get(i) + "'";
        }
        if (isExclude) {
            writeNext(tbsl);
        }
    }

    protected void thresholdUsed() {
        writeNext("Thresholds used -> " + cthreshold + " (critical), " + wthreshold + " (warning)");
    }

    protected void writeHeader() {
        writeNext("Enver Tablespace Report\n");
    }

    protected boolean elaborate(String[] record) {
        float csize = Float.parseFloat(record[5]);

        if (csize > cthreshold) {
            if (!isCritical) {
                writeNext("Check for criticals:");
            }
            retrieveTbsInfo(record);
            isCritical = true;
            return true;
        }

        if (csize > wthreshold) {
            if (!isWarning) {
                writeNext("\nCheck for warnings:");
            }
            retrieveTbsInfo(record);
            isWarning = true;
            return true;
        }
        return false;
    }

    private void retrieveTbsInfo(String[] record) {

        double size_mb = Double.parseDouble(record[2]) / 1048576;
        double free_mb = Double.parseDouble(record[4]) / 1048576;
        DecimalFormat formatter = new DecimalFormat("#,###.00");
        String size = formatter.format(size_mb);
        String free = formatter.format(free_mb);
        String buffer = String.format("  %-10s%-38s%11.2f%%  %-30s",
                instance,
                record[1] + "[" + size + "]",
                Float.valueOf(record[5]),
                "(" + free + ")");
        writeNext(new String[]{buffer});
    }

    @Override
    public int writeAll(@NonNull final ResultSet rs, boolean includeColumnNames) throws SQLException, IOException {

        writeHeader();
        boolean alarm = false;
        int rows = 0;
        try {
            ResultSetMetaData metadata = rs.getMetaData();

            int columnCount = metadata.getColumnCount();

            writeNext(new String[]{"Starting MonTbs on instance " + instance + " at " + GeneralUtil.getCurrentDate(DateFormats.DASH_TIMEDATE)});
            ignoreTbsList();
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
                rows++;
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
        writeNext(new String[]{"Starting MonTbs on instance " + instance + " at " + GeneralUtil.getCurrentDate(DateFormats.DASH_TIMEDATE)});
        ignoreTbsList();
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
