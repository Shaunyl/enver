package com.shaunyl.enver.exception;

/**
 *
 * @author Filippo Testino (filippo.testino@gmail.com)
 */
public class DatabaseException extends Exception {

    private static final long serialVersionUID = 1L;
    
    private String key;
    
    public String getKey() {
        return this.key;
    }
    
    public DatabaseException() {
    }

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public DatabaseException(String message, Throwable cause, String key) {
        super(message, cause);
        this.key = key;
    }    

    public DatabaseException(Throwable cause) {
        super(cause);
    }
}
