/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.shaunyl.enver.commandline;

import java.io.IOException;
import java.io.PrintStream;

/**
 *
 * @author Filippo
 */
public interface ICommandStatus {

    void setStatus(PrintStream stream, final String msg) throws IOException;

    void print(final String message, final Object... parameters);
}
