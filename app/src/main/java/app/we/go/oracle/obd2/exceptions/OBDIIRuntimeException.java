package app.we.go.oracle.obd2.exceptions;

/**
 * Created by Vadim on 16.01.2017.
 */

public class OBDIIRuntimeException extends RuntimeException {

    String command;
    String response;


    public OBDIIRuntimeException(String command, String response) {

        this.command=command;
        this.response=response;
    }

    public String getMessage () {

        return  "Error OBDII protocol, command: "+command+" response: "+response;
    }
}