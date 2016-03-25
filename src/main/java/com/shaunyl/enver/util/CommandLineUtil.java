package com.shaunyl.enver.util;

import com.shaunyl.enver.commandline.CommandLine;
import com.shaunyl.enver.commandline.Command;
import com.shaunyl.enver.DateFormats;
import com.shaunyl.enver.exception.CommandLineException;
import java.io.PrintStream;
import java.util.*;

/**
 *
 * @author Filippo Testino (filippo.testino@gmail.com)
 */
public class CommandLineUtil {

    private static Collection<Command> getCommands(CommandLine shell/*, boolean expanded*/, String... args) throws CommandLineException {
        Collection<Command> commands = new ArrayList<Command>();
        if (args.length == 1) { // ALL COMPACT
            return shell.getCommands().values();
        }

//        int start = expanded ? 2 : 1;
        for (Iterator<Command> it = shell.getCommands().values().iterator(); it.hasNext();) {
            Command command = it.next();
            for (int i = 1; i < args.length; i++) {
                boolean valid = validateCommand(shell, args[i].replace("-", ""));
                if (!valid) {
                    throw new CommandLineException(String.format("The command '%s' is not supported.", args[1]));
                }
                if (command.getName().equalsIgnoreCase(args[i].replace("-", ""))) {
                    commands.add(command);
                }
            }
        }

        return commands;
    }

    public static void printHeadLogFile(final String cmd) {
        System.out.println("");
        System.out.println(String.format("Job [%s] started at %s", cmd, GeneralUtil.getCurrentDate(DateFormats.TIMEONLY)));
        System.out.println("****************************************");
    }

    /**
     * Validates a command, i.e. Checks if a command is supported by the ENVER
     * shell.
     *
     * @param shell An objects that represents the ENVER shell.
     * @param arg The ENVER shell command to validate.
     * @return 'true' if the command is supported. Otherwise it returns 'false'.
     */
    public static boolean validateCommand(CommandLine shell, String arg) {
        Collection<Command> values = shell.getCommands().values();
        for (Iterator<Command> it = values.iterator(); it.hasNext();) {
            Command command = it.next();
            if (command.getName().equalsIgnoreCase(arg)) {
                return true;
            }
        }
        return false;
    }

    public static void printCustomMessage(PrintStream stream, String message) {
        stream.println(message);
    }

//    public static void printCustomMessageWithDate(SessionManager.DefaultSession t, PrintStream stream, String message, String... parameters) {
//        String currentDate = GeneralUtil.getCurrentDate(Envariables.DATE_FORMAT);
//        stream.println("\n[" + currentDate + "]: " + String.format(message, parameters));
//    }
//
    public static void printErrorMessage(Command.CommandAction t, PrintStream stream, String message, long elapsed) {
        String currentDate = GeneralUtil.getCurrentDate(DateFormats.TIMEONLY);
//        String task = t.getTaskname().toUpperCase();
        String result = String.format("Subjob aborted at %s with the following message:\n%s", currentDate, message);
        stream.println("\n****************************************");
        stream.println(result);
        stream.println("Elapsed time: " + elapsed + " ms");
    }

    /**
     * Prints the current release version of ENVER.
     *
     * @param stream The output stream.
     */
    public static void printEnverVersion(PrintStream stream) {
        stream.println("");
        printBanner(stream);
        stream.println("");
        stream.println("Printing Enver version...");
        stream.println("------------------------------------------");
        stream.println(String.format("Beta %s - Production on %s", EnverUtil.getBuildVersion(), EnverUtil.getBuildTimestamp()));
        stream.println("------------------------------------------");
    }

    /**
     * Prints the ENVER shell header.
     *
     * @param stream The output stream.
     */
    public static void printShellHeader(PrintStream stream) {
        stream.println("Enver Shell; enter 'help<RETURN>' for list of supported commands.");
        stream.println(String.format("Version: %s, %s - %s", EnverUtil.getBuildVersion(), EnverUtil.getBuildVersion(), EnverUtil.getBuildTimestamp())); //FIXME
    }

    /**
     * Prints the ENVER banner.
     *
     * @param stream The output stream.
     */
    public static void printBanner(PrintStream stream) {
        stream.println("Copyright (c) 2014 Filippo Testino (Shaunyl).\nAll Right Reserved.");
    }

    /**
     * Prints the results of a runned start.
     *
     * @param t The start executed.
     * @param stream The output stream.
     */
    public static void printTaskResults(Command.CommandAction t, PrintStream stream, boolean isSuccess, long elapsed) {
        int warnings = t.getWarnings();
        int errors = t.getErrors();
//        String task = t.getTaskname().toUpperCase();
        String currentDate = GeneralUtil.getCurrentDate(DateFormats.TIMEONLY);
        String result;
        if (warnings + errors > 0) {
            result = String.format("Job completed at %s with errors (%d) and warnings (%d)", currentDate, errors, warnings);
        } else {
            result = String.format("Job %scompleted at %s %s", isSuccess ? "successfully " : "", currentDate, isSuccess ? "" : "with warning/errors");
        }
        stream.println("\n****************************************");
        stream.println(result);
        stream.println("Elapsed time: " + elapsed + " ms");
        warnings = 0;
        errors = 0;
    }

    public static void printTop(PrintStream stream) {
        stream.println("");
        stream.println(String.format("Enver: Release %s - Production on %s", EnverUtil.getBuildVersion(), EnverUtil.getBuildTimestamp()));
        printBanner(stream);
    }
}
