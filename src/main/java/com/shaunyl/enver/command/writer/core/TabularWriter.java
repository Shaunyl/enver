package com.shaunyl.enver.command.writer.core;

import com.shaunyl.enver.command.writer.IEnverWriter;
import com.shaunyl.enver.exception.TaskException;
import com.shaunyl.enver.util.DatabaseUtil;
import com.shaunyl.enver.util.GeneralUtil;
import java.io.*;
import java.sql.*;
import java.util.*;
import lombok.NonNull;

/**
 *
 * @author Filippo Testino (filippo.testino@gmail.com)
 */
public class TabularWriter implements IEnverWriter {

    private Writer rawWriter;

    private PrintWriter printer;

    private char separator;

    private String endline;

    private Map<String, Integer> colformats;
    
    private Map<Integer, String> cols = new HashMap<Integer, String>();

    /**
     * The default separator to use if none is supplied to the constructor.
     */
    public static final char DEFAULT_SEPARATOR = '-';

    /**
     * Default line terminator uses platform encoding.
     */
    public static final String DEFAULT_LINE_END = "\n";

    public static final int DEFAULT_COLUMN_LENGTH = 25;

    /**
     * Constructs TabularWriter using a dash for the delimiter.
     *
     * @param writer the writer to an underlying Tabular source.
     * @param colformats formats specified for columns.
     */
    public TabularWriter(Writer writer, Map<String, Integer> colformats) {
        this(writer, colformats, DEFAULT_SEPARATOR);
    }

    /**
     * Constructs TabularWriter with supplied separator.
     *
     * @param writer the writer to an underlying Tabular source.
     * @param separator the separator to use for separating header from data.
     */
    public TabularWriter(Writer writer, Map<String, Integer> colformats, char separator) {
        this(writer, colformats, separator, DEFAULT_LINE_END);
    }

    /**
     * Constructs TabularWriter with supplied separator and line ending.
     *
     * @param writer the writer to an underlying Tabular source.
     * @param separator the separator to use for separating header from data.
     * @param endline the line feed terminator to use.
     */
    public TabularWriter(Writer writer, Map<String, Integer> colformats, char separator, String endline) {
        this.rawWriter = writer;
        this.printer = new PrintWriter(writer);
        this.separator = separator;
        this.endline = endline;
        this.colformats = colformats;
    }

    protected void writeColumnNames(@NonNull final ResultSetMetaData metadata)
            throws SQLException {

        int columnCount = metadata.getColumnCount();

        String[] nextLine = new String[columnCount];
        String[] separators = new String[columnCount];
        for (int i = 0; i < columnCount; i++) {
            nextLine[i] = metadata.getColumnName(i + 1);
            if (colformats.containsKey(nextLine[i].toLowerCase())) {
                cols.put(i, nextLine[i]);
            }
        }

        for (int i = 0; i < columnCount; i++) {
            if (cols.containsKey(i)) {
                Object width = colformats.get(cols.get(i).toLowerCase());
                separators[i] = GeneralUtil.repeat(String.valueOf(separator), Integer.parseInt(width.toString()));
            } else {
                separators[i] = GeneralUtil.repeat(String.valueOf(separator), DEFAULT_COLUMN_LENGTH);
            }
        }
        writeNext(nextLine);
        writeNext(separators);
    }

    /**
     * Writes the entire ResultSet to a Tabular file.
     *
     * The caller is responsible for closing the ResultSet.
     *
     * @param rs the recordset to write
     * @param includeColumnNames true if you want column names in the output,
     * false otherwise
     *
     */
    @Override
    public int writeAll(@NonNull final ResultSet rs, boolean includeColumnNames)
            throws SQLException, IOException, TaskException {

        ResultSetMetaData metadata = rs.getMetaData();

        if (includeColumnNames) {
            writeColumnNames(metadata);
        }

        int columnCount = metadata.getColumnCount();

        boolean norows = true;
        int rows = 0;
                
        while (rs.next()) {
            norows = false;
            String[] nextLine = new String[columnCount];

            for (int i = 0; i < columnCount; i++) {
                nextLine[i] = DatabaseUtil.getColumnValue(rs, metadata.getColumnType(i + 1), i + 1);
            }

            writeNext(nextLine);
            rows++;
        }
        if (norows) {
            writeNext(new String[]{ "\nno rows selected" });
        }
        return rows;
    }

    /**
     * Gets a list of supported extensions.
     *
     */
    @Override
    public String[] getValidFileExtensions() {
        return new String[]{ "txt" };
    }

    /**
     * Writes the entire list to a Tabular file. The list is assumed to be a
     * String[].
     *
     * @param lines a List of String[], with each String[] representing a line
     * of the file.
     */
    @Override
    public void writeAll(@NonNull final List lines) {

        for (Iterator iter = lines.iterator(); iter.hasNext();) {
            String[] nextLine = (String[]) iter.next();
            writeNext(nextLine);
        }
    }

    /**
     * Writes the next line to the file.
     *
     * @param nextLine a string array with each element as a separate entry.
     */
    public void writeNext(String[] nextLine) {
        String pattern = "";
        StringBuilder sb = new StringBuilder();

        int len = DEFAULT_COLUMN_LENGTH;

        for (int i = 0; i < nextLine.length; i++) {
            if (cols.containsKey(i)) {
                Object width = colformats.get(cols.get(i).toLowerCase());
                pattern += "%-" + width + "s ";
            } else {
                pattern += "%-" + len + "s ";
            }
        }
        sb.append(String.format(pattern, (Object[]) nextLine));

        sb.append(endline);
        printer.write(sb.toString());
    }

    /**
     * Flush underlying stream to writer.
     *
     * @throws IOException if bad things happen
     */
    public void flush() throws IOException {
        printer.flush();
    }

    /**
     * Close the underlying stream writer flushing any buffered content.
     *
     * @throws IOException if bad things happen
     *
     */
    @Override
    public void close() throws IOException {
        printer.flush();
        printer.close();
        rawWriter.close();
    }
}