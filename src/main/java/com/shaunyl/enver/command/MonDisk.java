package com.shaunyl.enver.command;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.CommaParameterSplitter;
import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.validators.PositiveInteger;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.shaunyl.enver.DateFormats;
import com.shaunyl.enver.commandline.CommandStatus;
import com.shaunyl.enver.exception.ParseException;
import com.shaunyl.enver.util.GeneralUtil;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import lombok.Cleanup;

/**
 *
 * @author Shaunyl
 */
@Parameters(separators = "=")
public class MonDisk extends ShellCommandAction implements IParameterValidator {

    @Parameter(required = true, arity = 1, validateWith = MonDisk.class)
    private List<String> cmd = Lists.newArrayList(1);

    @Parameter(names = "-directory", arity = 1)
    protected String directory = ".";

//    @Parameter(names = "-filename", arity = 1)
    protected String filename = "";

    @Parameter(names = "-threshold", arity = 1, validateWith = PositiveInteger.class)
    public Integer threshold = 83;

    @Parameter(names = "-include", splitter = CommaParameterSplitter.class, variableArity = true)
    protected List<String> include = Lists.newArrayList();

    private FileWriter fileWriter;

    @Override
    public void validate(String name, String value) throws ParameterException {
        if (!"mondisk".equals(value)) {
            throw new ParameterException("Parameter " + name
                    + " should be \"mondisk\" (found " + value + ")");
        }
    }

    private String command;

    public MonDisk() {
        super(null);
    }

    public MonDisk(final CommandStatus status) {
        super(status);
    }

    @Override
    public void parse() throws ParseException {
        super.parse();
        if (threshold < 1 || threshold > 99) {
            throw new ParseException("Parameter \"threshold\" need to be in the range from 1 to 99.");
        }

    }

    @Override
    public void setup() {
        super.setup();

        String egrep = "";
        for (int i = 0; i < include.size(); i++) {
            if (i < include.size() - 1) {
                egrep += include.get(i) + "|";
            } else {
                egrep += include.get(i);
            }
        }

//        command = "dos2unix temp > udisk.sh ; chmod u+x udisk.sh ; ./udisk.sh " + threshold + " \"" + egrep + "\"";
        command = "chmod u+x udisk.ksh ; ./udisk.ksh " + threshold + " \"" + egrep + "\"";

        filename = String.format("%s/%s-%s.txt", directory, this.host, GeneralUtil.getCurrentDate(DateFormats.SQUELCHED_TIMEDATE));
        try {
            fileWriter = new FileWriter(filename); // TEMP, creare writer apposta..
        } catch (IOException ex) {
            status.print("Error (%s) IO: %s", host, ex.getMessage());
        }
    }

    @Override
    public void taskAtomic(final int i) throws IOException {
        try {
            fileWriter.write("Enver Disk Usage Report\n\n");
            fileWriter.write("Starting MonDisk on box " + this.host + " at " + GeneralUtil.getCurrentDate(DateFormats.DASH_TIMEDATE) + "\n\n");
            session = jsch.getSession(user, host, port);
            session.setPassword(password);
            session.setConfig(config);
            session.connect();
            status.print("Session established.");

            final Channel channelsftp = session.openChannel("sftp");
            channelsftp.connect();
            ((ChannelSftp) channelsftp).put(new FileInputStream(file), "udisk.ksh", ChannelSftp.OVERWRITE);
            channelsftp.disconnect();

            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            status.print("Executing command:\n > %s", command);
            status.print("  ..on host %s\n", this.host);

            fileWriter.write("Retrieving filesystem usage info..\n\n");
            fileWriter.write("Check for critical level usage:\n");
            channel.connect();
        } catch (JSchException ex) {
            status.print("Error (%s) JSch: %s", host, ex.getMessage());
            return;
        } catch (SftpException ex) {
            status.print("Error (%s) Sftp: %s", host, ex.getMessage());
            return;
        }
        try {
            @Cleanup
            BufferedReader in = new BufferedReader(new InputStreamReader(channel.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                fileWriter.write(line + "\n");
            }
        } catch (IOException ex) {
            status.print("Error (%s) IO: %s", host, ex.getMessage());
        } finally { // TEMP
            status.print("Report successfully generated at %s..\n", filename);
            fileWriter.write("\nJob completed successfully at " + GeneralUtil.getCurrentDate(DateFormats.DASH_TIMEDATE));

            fileWriter.close();
        }
    }

    @Override
    public void takedown() {
        super.takedown();
    }
}
