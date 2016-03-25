package com.shaunyl.enver.command;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.shaunyl.enver.Paths;
import com.shaunyl.enver.BeanFactory;
import com.shaunyl.enver.commandline.Command;
import com.shaunyl.enver.commandline.CommandStatus;
import com.shaunyl.enver.exception.ParseException;
import com.shaunyl.enver.exception.TaskException;
import com.shaunyl.enver.util.FileManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import lombok.Getter;
import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 *
 * @author Shaunyl
 */
public class ShellCommandAction extends Command.CommandAction {

    @Getter
    protected JSch jsch;
    
    @Getter
    protected int port;
    
    @Getter
    protected File file;

    @Getter
    protected Properties config;
    
    @Getter
    ChannelExec channel = null;

    @Getter
    Session session = null;

    @Getter
    protected boolean isTaskCancelled;

    @Getter
    protected int cycle = 1;

    @Getter
    protected String host, user;

    private final FileManager fileManager = BeanFactory.getInstance().getBean(FileManager.class);

    private Iterator<Map.Entry<String, String[]>> iterator = null;

    public ShellCommandAction(final CommandStatus status) {
        this.status = status;
    }

    private String generateStatus(final double i) {
        return round(i * 100) + "%";
    }

    private double round(double value) {
        return new BigDecimal(value).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public void setCycle(int value) {
        this.cycle = value;
    }

    @Override
    public void parse() throws ParseException {
        status.print("Validation terminated.");
    }

    @Override
    public void setup() {
        boolean isMulti = "y".equals(multi);
        char splitter = ',';
        String[] credentials = new String[3];
        // fixme.. sta roba non va qui, ma in un manager a parte
        if (isMulti) {
            if (iterator == null) { // fix perché piglia sempre il primo così perché lo si rimette a null
                Map<String, String[]> values = fileManager.readAllWithCompositeKeys(Paths.MULTI_SHELL_CONNECTION_FILE, splitter);
                Set<Map.Entry<String, String[]>> set = values.entrySet();
                iterator = set.iterator();
            }
            iterator.hasNext();
            Map.Entry<String, String[]> mapEntry = iterator.next();
            credentials = mapEntry.getValue();
            user = credentials[1];
            host = credentials[0];
            password = credentials[2]; //cripted for security reasons..
        } else {
            //no single connection for now...
        }
        jsch = new JSch();
        config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        file = new File("config/udisk.sh");
        port = 22;
    }

    @Override
    public void takedown() {
        if (channel != null) {
            channel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
        status.print("Session closed.");
    }

    long startTime;

    @Override
    public long start() throws Exception {
        parse();
        setup();

        Signal.handle(new Signal("INT"), new SignalHandler() {
            @Override
            public void handle(Signal sig) {
                isTaskCancelled = true;
                status.print("User requested cancel of current operation.");
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

    public void taskTimed() throws IOException, SQLException, TaskException {
        boolean print;
        int feedback = 1;
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
