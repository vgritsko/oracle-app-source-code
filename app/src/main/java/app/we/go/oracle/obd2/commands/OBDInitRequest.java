package app.we.go.oracle.obd2.commands;

import java.util.ArrayList;

import app.we.go.oracle.obd2.commands.atCommands.DescribeProtocolNumberCommand;
import app.we.go.oracle.obd2.commands.atCommands.EchoOffCommand;
import app.we.go.oracle.obd2.commands.atCommands.HeadersOffCommand;
import app.we.go.oracle.obd2.commands.atCommands.LineFeedOffCommand;
import app.we.go.oracle.obd2.commands.atCommands.ProtocolCommand;
import app.we.go.oracle.obd2.commands.atCommands.ResetOBDCommand;
import app.we.go.oracle.obd2.commands.atCommands.SpacesOffCommand;

/**
 * Created by Vadim on 16.01.2017.
 */

public class OBDInitRequest extends OBDRequest {

    DescribeProtocolNumberCommand protocolNumberCommand;
    public OBDInitRequest () {

        protocolNumberCommand=new DescribeProtocolNumberCommand();

        commandsCollection = new ArrayList<>();
        commandsCollection.add(new ResetOBDCommand());
        commandsCollection.add(new EchoOffCommand());
        commandsCollection.add(new LineFeedOffCommand());
        commandsCollection.add(new SpacesOffCommand());
        commandsCollection.add(new HeadersOffCommand());
        commandsCollection.add(new ProtocolCommand(OBDProtocols.AUTO));

    }
}
