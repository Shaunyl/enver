package com.shaunyl.enver.command;

import lombok.Getter;
import lombok.NonNull;

/**
 *
 * @author Shaunyl
 */
public class Query {
    @Getter @NonNull
    private final String sqltext;
    
    @Getter
    private Object[] parameters = null;
    
    public Query(final @NonNull String sqltext) {
        this.sqltext = sqltext;
    }
    
    public Query(final @NonNull String sqltext, Object... parameters) {
        this.parameters = parameters;
        this.sqltext = String.format(sqltext, parameters);
    }
}
