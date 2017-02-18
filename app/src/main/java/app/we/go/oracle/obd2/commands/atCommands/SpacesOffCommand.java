package app.we.go.oracle.obd2.commands.atCommands;


import app.we.go.oracle.obd2.commands.OBDATCommand;

/**
 * Created by Vadim on 09.01.2017.
 */

public class SpacesOffCommand extends OBDATCommand {

    public SpacesOffCommand() {

        super("AT S0", "Spaces off");
    }

    @Override
    protected void valuesCalculation() {

    }
}
