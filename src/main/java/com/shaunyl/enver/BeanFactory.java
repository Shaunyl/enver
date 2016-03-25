/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.shaunyl.enver;

import com.shaunyl.enver.command.export.core.ExportQuery;
import com.shaunyl.enver.command.export.core.ExportTable;
import com.shaunyl.enver.command.export.core.IExportMethod;
import com.shaunyl.enver.util.ConnectionManager;
import com.shaunyl.enver.util.FileManager;
import com.shaunyl.enver.util.JDBCConnectionManager;
import com.shaunyl.enver.util.PropertiesFileManager;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Provider;

/**
 *
 * @author Filippo
 */
public class BeanFactory {

    private static final BeanFactory instance = new BeanFactory();

    private final static Map<Class<?>, Object> map = new HashMap<Class<?>, Object>();

    static {
        map.put(FileManager.class, new PropertiesFileManager());
        map.put(ConnectionManager.class, new JDBCConnectionManager());
        map.put(IExportMethod.class, new ExportQuery());
        map.put(IExportMethod.class, new ExportTable());
    }

    public <T> T getBean(Class<T> clazz) {
        @SuppressWarnings("unchecked")
        T bean = (T) map.get(clazz);
        if (bean == null) {
            throw new RuntimeException("Not found " + clazz);
        }
        return bean;
    }

    public <T> Provider<T> getProvider(final Class<T> clazz) {
        return new Provider<T>() {
            @Override
            public T get() {
                return getBean(clazz);
            }
        };
    }

    public static BeanFactory getInstance() {
        return instance;
    }
}
