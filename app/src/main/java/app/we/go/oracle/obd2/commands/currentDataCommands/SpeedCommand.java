package app.we.go.oracle.obd2.commands.currentDataCommands;

import app.we.go.oracle.obd2.commands.OBDCommand;

/**
 * Created by Vadim on 17.01.2017.
 */

public class SpeedCommand extends OBDCommand {

    private int speed = -1;

    public  SpeedCommand () {
        super ("01 0D", "Speed");
    }

    @Override
    protected void valuesCalculation() {

        speed=buffer.get(2);
    }

    @Override
    public String getCalculatedResult() {
        return String.valueOf(speed);
    }

}