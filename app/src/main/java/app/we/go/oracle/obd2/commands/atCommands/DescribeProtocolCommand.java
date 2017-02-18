package app.we.go.oracle.obd2.commands.atCommands;


import app.we.go.oracle.obd2.commands.OBDATCommand;

/**
 * Created by Vadim on 09.01.2017.
 */

public class DescribeProtocolCommand extends OBDATCommand {

    public DescribeProtocolCommand() {
        super("AT DP", "describe protocol");
    }
}
