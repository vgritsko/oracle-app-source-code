package app.we.go.oracle.obd2.commands.currentDataCommands;

import app.we.go.oracle.obd2.commands.OBDCommand;

/**
 * Created by Vadim on 17.01.2017.
 */

public class RuntimeEngineStartCommand extends OBDCommand {

    private int runtimeEngine = -1;

    public RuntimeEngineStartCommand() {

        super ("01 1F","runtime engine start");
    }

    @Override
    protected void valuesCalculation() {

        runtimeEngine = buffer.get(2) * 256 + buffer.get(3);

    }

    @Override
    public String getCalculatedResult() {

        return String.valueOf(runtimeEngine);
    }
}
