package com.shaunyl.enver.io;

import java.io.*;
import java.util.Properties;
import java.util.regex.*;
import lombok.NonNull;

/**
 *
 * @author Filippo
 */
public class EnverConnectionStringsPropertiesFileUpdater extends IEnverPropertiesFileUpdater {

    private String file = "config/connectionStrings.properties";

    private static final String DEFAULT_COMMENT = " This information property list file contains connection strings information for ENVER.\n All the keys should not change because they are referenced in the source code.\n ---";

    private static final String REGEX = "jdbc:oracle:thin:(.*?)/(.*?)@(.*?):(\\d+):(.*?)$";
    
    private static final String REGEX_SERVICE = "jdbc:oracle:thin:(.*?)/(.*?)@(.*?):(\\d+)/(.*?)$";

    @Override
    public String read() throws IOException, Exception {
        FileInputStream in = new FileInputStream(file);
        Properties props = new Properties();
        props.load(in);
        in.close();
        String prop = props.getProperty("enver.url");

        return prop;
    }

    @Override
    public void write(@NonNull final String[][] pairs) throws Exception {
        FileInputStream in = new FileInputStream(file);
        Properties props = new Properties();
        props.load(in);
        String prop = props.getProperty("enver.url");
        in.close();
        Pattern p = Pattern.compile(REGEX_SERVICE);
        Matcher m = p.matcher(prop);
        boolean valid = m.find();
        if (!valid) {
            throw new Exception("Some stuff in this property list file may be corrupted. No changes were commited.");
        }

        FileOutputStream out = new FileOutputStream(file);
        String user = m.group(1), password = m.group(2), host = m.group(3), port = m.group(4), sid = m.group(5);
        for (String[] pair : pairs) {
            String property = pair[0];
            String value = pair[1];
            if ("user".equals(property)) {
                user = value;
            }
            if ("pass".equals(property)) {
                password = value;
            }
            if ("host".equals(property)) {
                host = value;
            }
            if ("port".equals(property)) {
                port = value;
            }
            if ("sid".equals(property)) {
                sid = value;
            }
        }
        props.setProperty("enver.url", String.format("jdbc:oracle:thin:%s/%s@%s:%d:%s", user, password, host, Integer.parseInt(port), sid));

        props.store(out, DEFAULT_COMMENT);
        out.close();
    }

    @Override
    public void writeSubValueByKey(@NonNull final String key, @NonNull final String subprop, @NonNull final String value) throws Exception {
        FileInputStream in = new FileInputStream(file);
        Properties props = new Properties();
        props.load(in);
        String prop = props.getProperty(key);
        in.close();
        Pattern p = Pattern.compile(REGEX);
        Matcher m = p.matcher(prop);
        boolean valid = m.find();
        if (!valid) {
            throw new Exception("Some stuff in this property list file may be corrupted. No changes were commited.");
        }

        String user = "user".equals(subprop) ? value : m.group(1);
        String password = "password".equals(subprop) ? value : m.group(2);
        String host = "host".equals(subprop) ? value : m.group(3);
        String port = "port".equals(subprop) ? value : m.group(4);
        String schema = "schema".equals(subprop) ? value : m.group(5);
        FileOutputStream out = new FileOutputStream(file);
        props.setProperty(key, String.format("jdbc:oracle:thin:%s/%s@%s:%d:%s", user, password, host, Integer.parseInt(port), schema));

        props.store(out, DEFAULT_COMMENT);
        out.close();
    }

    @Override
    public void writeValueByKey(String key, String value) throws Exception {
    }
}
