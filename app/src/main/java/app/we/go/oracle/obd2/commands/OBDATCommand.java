package app.we.go.oracle.obd2.commands;

import app.we.go.oracle.obd2.settings.Globals;

/**
 * Created by Vadim on 16.01.2017.
 */

public class OBDATCommand extends OBDCommand {
    public OBDATCommand (String command,String commandDescription){
        super(command,commandDescription);
        responseDelayMs= Globals.RESPONSE_DELAY_MS_AT_CMD;
    }

    public OBDATCommand(String command) {
        super(command,"");
    }





    @Override
    protected void valuesCalculation() {

    }

    @Override
    public String getCalculatedResult() {

        return getRawResult();
    }

    protected void fillBuffer() {}
}

