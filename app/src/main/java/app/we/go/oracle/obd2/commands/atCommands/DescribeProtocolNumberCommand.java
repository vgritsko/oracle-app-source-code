package app.we.go.oracle.obd2.commands.atCommands;


import app.we.go.oracle.obd2.commands.OBDATCommand;
import app.we.go.oracle.obd2.commands.OBDProtocols;

/**
 * Created by Vadim on 09.01.2017.
 */

public class DescribeProtocolNumberCommand extends OBDATCommand {

    private OBDProtocols obdProtocol= OBDProtocols.AUTO;

    public DescribeProtocolNumberCommand() {
        super("AT DPN", " protocol number");

    }

    @Override
    protected void valuesCalculation() {

        String result= getRawResult();
        char protocolNumber;
        if (result.length()==2) {
            protocolNumber=result.charAt(1);
        }

        else {
            protocolNumber=result.charAt(0);
        }

        OBDProtocols[] protocols = OBDProtocols.values();
        for (OBDProtocols protocol : protocols) {
            if (protocol.getValue() == protocolNumber) {
                this.obdProtocol = protocol;
                break;
            }
        }
    }

    public OBDProtocols getProtocol(){

        return obdProtocol;
    }

}
