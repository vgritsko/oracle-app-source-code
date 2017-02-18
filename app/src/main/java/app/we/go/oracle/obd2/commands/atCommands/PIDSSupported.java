package app.we.go.oracle.obd2.commands.atCommands;


import app.we.go.oracle.obd2.commands.OBDATCommand;

/**
 * Created by Vadim on 12.01.2017.
 */

public class PIDSSupported extends OBDATCommand {

    public PIDSSupported (){

        super ("01 00","Pids Supported");
    }

    @Override
    protected void valuesCalculation() {

    }
}
