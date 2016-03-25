package com.shaunyl.enver.exception;

/**
 *
 * @author Filippo Testino (filippo.testino@gmail.com)
 */
public class CommandLineException extends Exception {

    private static final long serialVersionUID = 1L;

    public CommandLineException() {
    }

    public CommandLineException(String message) {
        super(message);
    }

    public CommandLineException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandLineException(Throwable cause) {
        super(cause);
    }
}
