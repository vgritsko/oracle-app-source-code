package app.we.go.oracle.obd2.commands.atCommands;

import app.we.go.oracle.obd2.commands.OBDATCommand;

/**
 * Created by Vadim on 07.03.2017.
 */

public class ResetOBDCommand extends OBDATCommand {

    public ResetOBDCommand() {

        super("AT Z", "Reset OBD");

    }

    @Override
    protected void valuesCalculation() {

    }
}
