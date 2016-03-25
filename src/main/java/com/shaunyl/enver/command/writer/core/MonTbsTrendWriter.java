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
 * @author Filippo
 */
public class MonTbsTrendWriter implements IEnverWriter {

    private Writer rawWriter;

    private PrintWriter printer;

    private int deep, threshold;

    boolean isChecked = false;

    @Setter
    private String instance = "<n.p.>";

    public MonTbsTrendWriter(Writer writer, int deep, int threshold) {
        this.deep = deep;
        this.threshold = threshold;
        this.rawWriter = writer;
        this.printer = new PrintWriter(writer);
    }

    protected void writeFooter(boolean isSuccess) {
        writeNext("\nJob completed " + (isSuccess ? "successfully " : "with errors ") + GeneralUtil.getCurrentDate(DateFormats.DASH_TIMEDATE));
    }

    protected void thresholdUsed() {
        writeNext("Thresholds used -> " + threshold + "mb per day");
    }

    protected void deepUsed() {
        writeNext("Deep of -> " + deep + " days");
    }

    protected void writeHeader() {
        writeNext("Enver Tablespace Trend Report\n");
    }

    protected boolean elaborate(String[] record) {
        if (!isChecked) {
            writeNext("Check for trends:");
            isChecked = true;
        }
        retrieveTbsInfo(record);
        return true;
    }

    private void retrieveTbsInfo(String[] record) {

        double free_mb = Double.parseDouble(record[2]) / 1048576;
        DecimalFormat formatter = new DecimalFormat("#,###.00");
        String free = formatter.format(free_mb);
        double deltaD = Double.parseDouble(record[4]);
        double deltaM = Double.parseDouble(record[3]);
        double m = 30;
        double d = this.deep;
        double trend_pct = 0;
        char sign = '+';
        if (deltaM >= (deltaD * (m / d))) {//(11.0/503.13)
            trend_pct = ((deltaM - deltaD * (m / d)) / (Math.abs(deltaD) * (m / d))) * 100;
            sign = '-';
        } else {
            trend_pct = ((deltaD * (m / d) - deltaM) / Math.abs(deltaM)) * 100;
        }
                
        // SELECT db, tbs, free_size, delta_month_mb, delta_days_mb
        // x = [Dd * (m/d)] / Dm
        // x = [Dm - Dd * (m/d)] / Dm * 100
        //Dm --> record[3]
        // BAC_PRO  BAC7_RUM[free]    (Dd/Dm) (x)
        //%c%1.2f%%",
        
        String buffer = String.format("  %-10s%-38s%-30s  %-20s%%",
                instance,
                record[1] + "[" + free + "]",
                "(" + record[4] + "/" + record[3] + ")", 
                sign + formatter.format(trend_pct));
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

            writeNext(new String[]{"Starting MonTbsTrend on instance " + instance + " at " + GeneralUtil.getCurrentDate(DateFormats.DASH_TIMEDATE)});
            thresholdUsed();
            deepUsed();
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
            writeNext(new String[]{"No alarm was detected.\n"});
        }
        writeFooter(true);
        return rows;
    }

    @Override
    public void writeAll(@NonNull final List lines) {
        writeHeader();
        writeNext(new String[]{"Starting MonTbsTrend on instance " + instance + " at " + GeneralUtil.getCurrentDate(DateFormats.DASH_TIMEDATE)});
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
