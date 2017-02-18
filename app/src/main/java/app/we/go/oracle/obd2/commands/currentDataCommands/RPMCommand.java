package app.we.go.oracle.obd2.commands.currentDataCommands;

import app.we.go.oracle.obd2.commands.OBDCommand;

/**
 * Created by Vadim on 16.01.2017.
 */

public class RPMCommand extends OBDCommand {

    private int rpm = -1;

    public  RPMCommand () {

        super("01 0C", "RPM");
    }

    @Override
    protected void valuesCalculation() {

        rpm = (buffer.get(2)*256+buffer.get(3))/4;
    }

    @Override
    public String getCalculatedResult() {

        return String.valueOf(rpm);
    }



}