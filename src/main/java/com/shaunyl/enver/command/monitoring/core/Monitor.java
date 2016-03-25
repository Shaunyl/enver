package com.shaunyl.enver.command.monitoring.core;

import com.beust.jcommander.Parameter;
import com.shaunyl.enver.BeanFactory;
import com.shaunyl.enver.command.DatabaseCommandAction;
import com.shaunyl.enver.command.writer.IEnverWriter;
import com.shaunyl.enver.commandline.CommandStatus;
import com.shaunyl.enver.exception.ParseException;
import java.io.IOException;

/**
 *
 * @author Shaunyl
 */
@Deprecated
public abstract class Monitor extends DatabaseCommandAction {

    @Parameter(names = "-directory", arity = 1)
    protected String directory = ".";

    protected IEnverWriter writer;

    private final IMonitorMethod iMonitorMethod = BeanFactory.getInstance().getBean(IMonitorMethod.class);

    public Monitor(final CommandStatus status) {
        super(status);
    }

    @Override
    public void parse() throws ParseException {
        super.parse();
    }

    @Override
    public void setup() {
        super.setup();
    }

    @Override
    public void taskAtomic(final int i) throws IOException {
        resultSet = iMonitorMethod.monitor();
        status.print(iMonitorMethod.getStatus());
    }
}
