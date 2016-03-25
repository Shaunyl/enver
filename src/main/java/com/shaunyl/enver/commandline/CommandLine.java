package com.shaunyl.enver.commandline;

import com.shaunyl.enver.exception.CommandLineException;
import com.shaunyl.enver.util.CommandLineUtil;
import com.shaunyl.enver.command.export.ExportCSV;
import com.shaunyl.enver.command.export.ExportTabular;
import com.shaunyl.enver.command.MonTbs;
import com.beust.jcommander.JCommander;
import com.shaunyl.enver.Paths;
import com.shaunyl.enver.BeanFactory;
import com.shaunyl.enver.command.MonBakOutput;
import com.shaunyl.enver.command.monitoring.MonBak;
import com.shaunyl.enver.command.MonDisk;
import com.shaunyl.enver.command.MonTbsAuto;
import com.shaunyl.enver.command.MonTbsTrend;
import com.shaunyl.enver.util.FileManager;
import java.io.*;
import java.lang.reflect.Constructor;
import java.util.*;
import lombok.Getter;
import org.apache.commons.io.output.TeeOutputStream;

/**
 *
 * @author Filippo Testino (filippo.testino@gmail.com)
 */
public abstract class CommandLine {

    private FileOutputStream fos;

    private TeeOutputStream tos;

    private PrintStream ps;

    private final FileManager fileManager = BeanFactory.getInstance().getBean(FileManager.class);

    @Getter
    private static final Map<String, Command> commands = new TreeMap<String, Command>();

    static {
        addCommand(new Command(ExportCSV.class, "expcsv"));
        addCommand(new Command(MonTbs.class, "montbs"));
        addCommand(new Command(ExportTabular.class, "exp"));
        addCommand(new Command(MonBak.class, "monbak"));
        addCommand(new Command(MonBakOutput.class, "monbako"));
        addCommand(new Command(MonDisk.class, "mondisk"));
        addCommand(new Command(MonTbsTrend.class, "montbstrend"));
        addCommand(new Command(MonTbsAuto.class, "montbsauto"));
    }

    protected Class<? extends Command.CommandAction> toTask(final String name) {
        return commands.get(name).getCmdClass();
    }

    private static void addCommand(final Command command) {
        commands.put(command.getName(), command);
    }

    public void log(final String filename) throws FileNotFoundException {
        fos = new FileOutputStream(new File(filename));
        tos = new TeeOutputStream(System.out, fos);
        ps = new PrintStream(tos);
        System.setOut(ps);
    }

    public void closelog() throws IOException {
        fos.close();
        tos.close();
        ps.close();
    }

    public int run(String[] args) throws IOException, CommandLineException {
        String cmd = args[0].trim();
        Class<? extends Command.CommandAction> clazz = this.toTask(cmd);
        log("./enver" + cmd.toUpperCase() + ".log");
        CommandLineUtil.printTop(System.out);

        int errCode = -1;
        if (args.length < 1) {
            return errCode;
        }

        Command.CommandAction t = null;

        try {
            Constructor<? extends Command.CommandAction> constructor = clazz.getDeclaredConstructor(CommandStatus.class);
            constructor.setAccessible(true);
            t = constructor.newInstance(new CommandStatus());
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Invalid command class: " + clazz.getName() + ".  It does not provide a constructor. " + "Available constructors are: " + Arrays.toString(clazz.getConstructors()));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to construct command class. " + e.getMessage(), e);
        }

        new JCommander(t, args);

        long totalElapsedTime = 0;
        boolean isSuccess = true;

        boolean isMulti = "y".equals(t.multi);
        int len = 1;
        if (isMulti) { // TEMP!!!!!!!!!!!
            if (t instanceof MonDisk) {
                len = fileManager.count(Paths.MULTI_SHELL_CONNECTION_FILE);
            } else {
                len = fileManager.count(Paths.MULTI_CONNECTION_FILE);
            }
        }
        for (int j = 0; j < len; j++) {
            try {
                CommandLineUtil.printHeadLogFile(cmd);
                long start = t.start();
                totalElapsedTime += start;

            } catch (Exception e) {
                CommandLineUtil.printErrorMessage(t, System.out, e.getMessage(), totalElapsedTime);
                isSuccess = false;
            }
            CommandLineUtil.printTaskResults(t, ps, isSuccess, totalElapsedTime);
            isSuccess = true;
        }

        errCode = 0;

        closelog();
        return errCode;
    }

    private Map<String, String> parseParameterFile(final String filename) throws CommandLineException {
        Map<String, String> matrix = this.fileManager.readAllWithKeys(filename, "-");
        return matrix;
    }

    private String[] mergeParameters(String[] argv) throws CommandLineException {
        String[] args = argv;
        // Load parfile:
        String pars = "";
        String parfilePath = "";
        List<String> temp = new ArrayList<String>();
        temp.add(args[0]);
        for (int i = 1; i < args.length; i += 2) {
            if (args[i].startsWith("-")) {
                pars += args[i];
                if (args[i].equals("-parfile")) {
                    parfilePath = args[i + 1];
                }
            } else {
                throw new IllegalStateException("Failed to parse input args: " + args[i]);
            }
            temp.add(args[i]);
            temp.add(args[i + 1]);
        }
        if (pars.contains("-parfile")) {
            Map<String, String> params = this.parseParameterFile(parfilePath);
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = "-" + entry.getKey();
                if (temp.contains(key)) {
                    throw new IllegalStateException("Duplicate parameter found: " + key);
                } else {
                    temp.add(key);
                    temp.add(entry.getValue());
                }
            }
            args = (String[]) temp.toArray();
            temp = null;
        }
        return args;
    }
}