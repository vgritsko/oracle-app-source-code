package app.we.go.oracle.obd2.commands.atCommands;

import app.we.go.oracle.obd2.commands.OBDATCommand;

/**
 * Created by Vadim on 02.02.2017.
 */

public class HeadersOffCommand extends OBDATCommand {

    public HeadersOffCommand( ) {
        super("AT H0","Headers off command");
    }

}
