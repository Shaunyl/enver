package com.shaunyl.enver.util;

import com.shaunyl.enver.exception.UnexpectedEnverException;
import java.io.*;
import java.util.*;

/**
 *
 * @author Filippo Testino
 */
public class PropertiesFileManager implements FileManager {

    @Override
    public int count(String filename) {
        return this.r(filename).size();
    }

    @Override
    public String read(String filename, String key) {
        return this.r(filename).getProperty(key);
    }

    @Override
    public String[] readWithKeys(String filename, String key) {
        return new String[]{key, this.r(filename).getProperty(key)};
    }

    @Override
    public List<String> readAll(String filename) {
        List<String> values = new ArrayList<String>();

        Properties properties = this.r(filename);
        Enumeration<Object> keys = properties.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = properties.getProperty(key);
            values.add(value);
        }
        return values;
    }

    @Override
    public Map<String, String> readAllWithKeys(String filename, String keyPrefix) {
        Map<String, String> values = new HashMap<String, String>();

        Properties properties = this.r(filename);
        Enumeration<Object> keys = properties.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = properties.getProperty(key);
            values.put(keyPrefix + key, value);
        }
        return values;
    }

    private Properties r(String filename) {
        Properties prop = new Properties();

        try {
            prop.load(new FileInputStream(filename));
        } catch (IOException e) {
            throw new UnexpectedEnverException(e.getMessage());
        }
        return prop;
    }

    @Override
    public Map<String, String[]> readAllWithCompositeKeys(String filename, char splitter) {
        Map<String, String[]> values = new HashMap<String, String[]>();

        Properties properties = this.r(filename);
        Enumeration<Object> keys = properties.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = properties.getProperty(key);
            String[] splits = this.s(value, splitter);
            values.put(key, splits);
        }
        return values;
    }

    private String[] s(String value, char splitter) {
        return value.split(Character.toString(splitter));
    }
}
