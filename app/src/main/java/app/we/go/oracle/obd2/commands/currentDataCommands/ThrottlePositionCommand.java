package app.we.go.oracle.obd2.commands.currentDataCommands;

import app.we.go.oracle.obd2.commands.OBDCommand;

/**
 * Created by Vadim on 17.01.2017.
 */

public class ThrottlePositionCommand extends OBDCommand {

    private float throttlePosition = -1.0f;


    public ThrottlePositionCommand(){

        super ("01 11","throttle position");

    }
    @Override
    protected void valuesCalculation() {

        throttlePosition=(buffer.get(2)*100)/255.0f;

    }

    @Override
    public String getCalculatedResult() {

        int roundValue=Math.round(throttlePosition);
        return String.valueOf(roundValue);
    }
}
