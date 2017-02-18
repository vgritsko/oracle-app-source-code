package app.we.go.oracle.obd2.commands;

import java.util.ArrayList;

import app.we.go.oracle.obd2.commands.currentDataCommands.RPMCommand;
import app.we.go.oracle.obd2.commands.currentDataCommands.RuntimeEngineStartCommand;
import app.we.go.oracle.obd2.commands.currentDataCommands.SpeedCommand;
import app.we.go.oracle.obd2.commands.currentDataCommands.ThrottlePositionCommand;

/**
 * Created by Vadim on 16.01.2017.
 */

public class OBDCurrentDataRequest extends OBDRequest {


    public OBDCurrentDataRequest () {
        commandsCollection = new ArrayList<>();
        // add new commands here
        commandsCollection.add(new RPMCommand());
        commandsCollection.add(new SpeedCommand());
        commandsCollection.add(new ThrottlePositionCommand());
        commandsCollection.add(new RuntimeEngineStartCommand());
    }


}
