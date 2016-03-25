package com.shaunyl.enver.commandline;

import java.io.IOException;
import java.io.PrintStream;

/**
 *
 * @author Filippo
 */
public class CommandStatus implements ICommandStatus {

    public CommandStatus() {
    }

    @Override
    public void setStatus(PrintStream stream, String msg) throws IOException {
        stream.println("* status: " + msg);
    }

    @Override
    public void print(final String message, final Object... parameters) {
        System.out.println(String.format(message, parameters));
    }
}
