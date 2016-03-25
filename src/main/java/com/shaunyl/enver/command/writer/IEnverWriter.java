package com.shaunyl.enver.command.writer;

import com.shaunyl.enver.exception.TaskException;
import java.io.IOException;
import java.sql.*;
import java.util.List;

/**
 *
 * @author Filippo Testino (filippo.testino@gmail.com)
 * @version 1.2
 */
public interface IEnverWriter {

    public String[] getValidFileExtensions();

    public void writeAll(final List allLines) throws IOException;

    public int writeAll(final ResultSet rs, final boolean includeColumnNames)
            throws SQLException, IOException, TaskException;
    
    public void close() throws IOException;
}
