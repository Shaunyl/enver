package com.shaunyl.enver.commandline;

import com.shaunyl.enver.exception.ParseException;
import com.shaunyl.enver.command.support.SlashParameterSplitter;
import com.beust.jcommander.internal.Lists;
import com.shaunyl.enver.command.support.CharBooleanValidator;
import java.util.List;
import com.beust.jcommander.Parameter;
import lombok.*;

/**
 *
 * @author Filippo Testino (filippo.testino@gmail.com)
 */
public class Command {

    @Getter @NonNull
    private final Class<? extends CommandAction> cmdClass;

    @Getter @NonNull
    private final String name;

    @Getter
    private String description;

    public Command(final @NonNull Class<? extends CommandAction> cmdClass, final @NonNull String name) {
        this.name = name;
        this.cmdClass = cmdClass;
    }

    public Command withDescription(String description) {
        this.description = description;
        return this;
    }

    public static abstract class CommandAction {

        @Parameter(names = "-password", description = "Enter password", password = true)
        public String password = null;

        @Parameter(names = "-userid", splitter = SlashParameterSplitter.class)
        public List<String> userid = Lists.newArrayList(2);

        @Parameter(names = "-multi", validateWith = CharBooleanValidator.class)
        public String multi = "n";
        
        @Parameter(names = "-parfile")
        public String parfile = "./enver.par";

        @Getter @Setter
        protected int errors, warnings;

        @Getter
        protected long totalelapsedtime;

        @Getter
        protected String taskname;

        @Getter
        protected CommandStatus status;

        public void parse() throws ParseException {
        }

        public void setup() {
        }

        public abstract long start() throws Exception;

        public void takedown() {
        }
    }
}
