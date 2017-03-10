package app.we.go.oracle.obd2.commands.atCommands;

import app.we.go.oracle.obd2.commands.OBDATCommand;

/**
 * Created by Vadim on 07.03.2017.
 */

public class LineFeedOffCommand extends OBDATCommand {

    public LineFeedOffCommand() {

        super("AT L0", "LineFeed off");
    }

    @Override
    protected void valuesCalculation() {

    }
}
