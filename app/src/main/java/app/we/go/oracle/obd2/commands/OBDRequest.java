package app.we.go.oracle.obd2.commands;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.InputMismatchException;

import app.we.go.oracle.obd2.exceptions.OBDIIRuntimeException;

/**
 * Created by Vadim on 16.01.2017.
 */

public abstract class OBDRequest {

    protected ArrayList<OBDCommand> commandsCollection = null;

    public OBDRequest() {


    }




    public void Run (InputStream inStream, OutputStream outStream) throws IOException {

        for (OBDCommand command: commandsCollection ) {
            try{
                command.Run(inStream,outStream);}

            catch (InterruptedException e) {

                Log.e(getClass().getSimpleName(),e.getMessage());

            }

            catch (InputMismatchException e) {

                Log.e(getClass().getSimpleName(),e.getMessage());

            }

            catch (OBDIIRuntimeException e) {

                Log.e(getClass().getSimpleName(),e.getMessage());

            }



        }
    }

    public ArrayList<OBDCommand> getCommandsList () {

        return commandsCollection;
    }
}
