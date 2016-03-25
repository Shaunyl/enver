package com.shaunyl.enver.io;

import lombok.NonNull;

/**
 *
 * @author Filippo Testino (filippo.testino@gmail.com)
 */
public abstract class IEnverPropertiesFileUpdater {
    
    public abstract void writeSubValueByKey(final String key, final String subprop, final String value) throws Exception;

    public abstract void writeValueByKey(final String key, final String value) throws Exception;
    
    public abstract  void write(@NonNull final String[][] pairs) throws Exception;
    
    public abstract String read() throws Exception;
}
