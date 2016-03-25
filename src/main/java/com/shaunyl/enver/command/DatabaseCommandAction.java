package com.shaunyl.enver.command;

import com.shaunyl.enver.Paths;
import com.shaunyl.enver.BeanFactory;
import com.shaunyl.enver.commandline.CommandStatus;
import com.shaunyl.enver.commandline.Command;
import com.shaunyl.enver.database.ConnectionFactory;
import com.shaunyl.enver.database.Database;
import com.shaunyl.enver.exception.ParseException;
import com.shaunyl.enver.exception.TaskException;
import com.shaunyl.enver.util.ConnectionManager;
import com.shaunyl.enver.util.FileManager;
import com.shaunyl.enver.util.GeneralUtil;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import lombok.Getter;
import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 *
 * @author Filippo
 */
public class DatabaseCommandAction extends Command.CommandAction {

    @Getter
    protected int cycle = 1;

    @Getter
    protected Connection connection;

    @Getter
    protected CallableStatement callable;

    @Getter
    protected PreparedStatement prepared;

    @Getter
    protected Statement statement;

    @Getter
    protected ResultSet resultSet;

    @Getter
    protected boolean isTaskCancelled;

    @Getter
    protected String host, instance, user;

    private final ConnectionManager connManager = BeanFactory.getInstance().getBean(ConnectionManager.class);

    private final FileManager fileManager = BeanFactory.getInstance().getBean(FileManager.class);

    private static final String SINGLE_TASK_KEY = "enver.url"; // temp

    private Iterator<Map.Entry<String, String>> iterator = null;

    public DatabaseCommandAction(final CommandStatus status) {
        this.status = status;
    }

    private String generateStatus(final double i) {
        return round(i * 100) + "% " + "(" + getPartialTime() + " ms)";
    }

    private double round(double value) {
        return new BigDecimal(value).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public void setCycle(int value) {
        this.cycle = value;
    }

    @Override
    public void parse() throws ParseException {
//        status.print("parse().. completed..");
    }

    @Override
    public void setup() {
        Database database = Database.ORACLE;

        String url;
        boolean isMulti = "y".equals(multi);
        // fixme.. sta roba non va qui, ma in un manager a parte
        if (isMulti) {
            if (iterator == null) { // fix perché piglia sempre il primo così perché lo si rimette a null
                Map<String, String> values = fileManager.readAllWithKeys(Paths.MULTI_CONNECTION_FILE, "");
                Set<Map.Entry<String, String>> set = values.entrySet();
                iterator = set.iterator();
            }
            iterator.hasNext();
            Map.Entry<String, String> mapEntry = iterator.next();
            url = mapEntry.getValue();

        } else {
//            if (password == null) {
//                password = userid.get(1);
//            }
//            user = userid.get(0);
            String[] hash = fileManager.readWithKeys(Paths.SINGLE_CONNECTION_FILE, SINGLE_TASK_KEY);
            url = hash[1];
        }
        
        try {
            Map<String, String> map;
            if (isMulti) {
                map = GeneralUtil.parseConnectionString(url);
            } else {
                map = GeneralUtil.parseConnectionString(url);
            }
            user = map.get("user").toUpperCase();
            password = map.get("password");
            instance = map.get("sid").toUpperCase();
            host = map.get("host").toUpperCase();
        } catch (Exception ex) {
            System.out.println("Internal error: " + ex.getMessage());
        }

        DataSource datasource = connManager.createDatasource(database, url, user, password);
        try {
            // other datasource properties..
            datasource.setLoginTimeout(15);
        } catch (SQLException ex) {
            throw new RuntimeException("" + ex.getMessage(), ex);
        }

        try {
            connection = connManager.createConnection(datasource);
        } catch (SQLException ex) {
            throw new RuntimeException(String.format("   Attemp to connect to \"%s\"@\"%s\" failed:\n     > %s", instance, host, ex.getMessage()), ex);
        }

        System.out.println(String.format("Connected to \"%s\"@\"%s\" as \"%s\"", instance, host, user));
        System.out.println("------");
        
        try { // FIXME
            statement = connection.createStatement();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseCommandAction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void takedown() {
        ConnectionFactory.commit(connection);
        ConnectionFactory.close(prepared, callable, statement);
        ConnectionFactory.close(resultSet);
        ConnectionFactory.close(connection);
        status.print("------");
        status.print("Disconnected from instance.");
    }
    long startTime;

    @Override
    public long start() throws Exception {
        parse();
        setup();

        Signal.handle(new Signal("INT"), new SignalHandler() {
            @Override
            public void handle(Signal sig) {
                ConnectionFactory.cancel(callable, statement, prepared);
                isTaskCancelled = true;
                status.print("User requested cancel of current operation.");
                status.print("Dump file could be partially unloaded.");
                ConnectionFactory.close(callable, statement, prepared);
                ConnectionFactory.close(resultSet);
                ConnectionFactory.rollback(connection);
                ConnectionFactory.close(connection);
            }
        });

        long elapsedTimeDownload = -1;
        startTime = System.currentTimeMillis();

        taskTimed();

        elapsedTimeDownload = System.currentTimeMillis() - startTime;
        this.totalelapsedtime = elapsedTimeDownload;
        takedown();

        return elapsedTimeDownload;
    }

    protected long getPartialTime() {
        return System.currentTimeMillis() - startTime;
    }

    public void taskTimed() throws IOException, SQLException, TaskException {
//        status.print("Partial time: " + getPartialTime() + " ms");
        boolean print;
        int feedback = 1; // temp
        for (int i = 0; i < cycle; i++) {
            print = status != null && (i % feedback) == 0;
            if (print) {
                status.setStatus(System.out, generateStatus((double) i / cycle));
            }
            taskAtomic(i);
        }

        status.setStatus(System.out, generateStatus(1));
    }

    public void taskAtomic(final int i) throws SQLException, TaskException, IOException {
    }
}
