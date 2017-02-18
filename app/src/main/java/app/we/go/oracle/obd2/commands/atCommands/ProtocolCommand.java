package app.we.go.oracle.obd2.commands.atCommands;


import app.we.go.oracle.obd2.commands.OBDATCommand;
import app.we.go.oracle.obd2.commands.OBDProtocols;

/**
 * Created by Vadim on 09.01.2017.
 */

public class ProtocolCommand extends OBDATCommand {

    private OBDProtocols obdProtocol;

    public  ProtocolCommand  (final OBDProtocols protocol){

        super ("AT SP"+protocol.getValue(),"Select protocol command");
        obdProtocol=protocol;
    }

    @Override
    protected void valuesCalculation() {

    }

}
