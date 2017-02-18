package app.we.go.oracle.obd2.commands.atCommands;


import app.we.go.oracle.obd2.commands.OBDATCommand;

/**
 * Created by Vadim on 12.01.2017.
 */

public class EchoOffCommand extends OBDATCommand {

    public EchoOffCommand (){

        super ("AT E0","EchoOff Command");
    }

    @Override
    protected void valuesCalculation() {

    }
}
