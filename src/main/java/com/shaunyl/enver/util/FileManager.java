package com.shaunyl.enver.util;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Filippo
 */
public interface FileManager {

    int count(String filename);
    
    String read(String filename, String key);
    
    String[] readWithKeys(String filename, String key);

    List<String> readAll(String filename);
    
    Map<String, String> readAllWithKeys(String filename, String keyPrefix);

    Map<String, String[]> readAllWithCompositeKeys(String filename, char splitter);
}
