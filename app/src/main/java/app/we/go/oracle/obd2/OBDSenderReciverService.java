package app.we.go.oracle.obd2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import app.we.go.oracle.features.common.Constants;
import app.we.go.oracle.obd2.commands.OBDCurrentDataRequest;
import app.we.go.oracle.obd2.commands.OBDInitRequest;
import app.we.go.oracle.obd2.commands.OBDRequest;

/**
 * Created by Vadim on 18.01.2017.
 */

public class OBDSenderReciverService {

    private final BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private  BluetoothConnectingThread mBluetoothConnectingThread;
    private  BluetoothSendRecieveDataThread mBluetoothSendRecieveThread;
    private int mState;

    public static final int STATE_STOPPED=0;
    public static final int STATE_CONNECTING=1;
    public static final int STATE_CONNECTED=2;

    public OBDSenderReciverService (Context context) {

        mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        mHandler=new Handler();
    }

    private synchronized  void setState (int state) {
        mState=state;
    }

    public synchronized  int getState () {
        return mState;
    }

    public  void setHandler(Handler handler) {

        mHandler=handler;
    }


    public  synchronized void start (BluetoothDevice device) {

        if (mState==STATE_CONNECTING) {
            mBluetoothConnectingThread.cancel();
            mBluetoothConnectingThread=null;
        }

        mBluetoothConnectingThread=new BluetoothConnectingThread(device);
        mBluetoothConnectingThread.start();
        setState(STATE_CONNECTING );

    }

    public synchronized void stop () {

        if (mBluetoothConnectingThread!=null) {
            mBluetoothConnectingThread.cancel();
            mBluetoothConnectingThread=null;
        }

        if (mBluetoothSendRecieveThread!=null) {
            mBluetoothSendRecieveThread.cancel();
            mBluetoothSendRecieveThread=null;

        }

        setState(STATE_STOPPED );
    }


    public  synchronized void startSendRecieveOBDData (BluetoothSocket socket) {

        if (mBluetoothConnectingThread!=null) {
            mBluetoothConnectingThread.cancel();
            mBluetoothConnectingThread=null;
        }

        if (mBluetoothSendRecieveThread!=null) {
            mBluetoothConnectingThread.cancel();
            mBluetoothConnectingThread=null;
        }

        mBluetoothSendRecieveThread=new BluetoothSendRecieveDataThread(socket);
        mBluetoothSendRecieveThread.start();
        setState(STATE_CONNECTED);

    }


    private class BluetoothConnectingThread extends Thread {

        private final BluetoothDevice mmBluetoothDevice;
        private final BluetoothSocket mmBluetoothSocket;

        public BluetoothConnectingThread (BluetoothDevice device){

            mmBluetoothDevice=device;
            BluetoothSocket tempSocket=null;

            //connect to bluetooth  socket
            // connect only reflection

            try {

                Method m = mmBluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});  // connect only reflection
                tempSocket= (BluetoothSocket) m.invoke(mmBluetoothDevice, 1);
            }
            catch (NoSuchMethodException e) {

                Log.e(getClass().getSimpleName(),e.getMessage());
            }
            catch (InvocationTargetException e) {

                Log.e(getClass().getSimpleName(),e.getMessage());
            }
            catch (IllegalAccessException e){

                Log.e(getClass().getSimpleName(),e.getMessage());
            }
            // end reflection

            mmBluetoothSocket =tempSocket;
        }

        public void run () {

            mBluetoothAdapter.cancelDiscovery();

            try {
                mmBluetoothSocket.connect();
            }
            catch (IOException e) {

                try {
                    mmBluetoothSocket.close();
                }  catch (IOException ex) {
                    Log.e(getClass().getSimpleName(),ex.getMessage());
                }

               return;
            }

            synchronized (OBDSenderReciverService.this) {
                mBluetoothConnectingThread=null;
            }

            startSendRecieveOBDData(mmBluetoothSocket);

        }

        public void cancel () {

            try {
                mmBluetoothSocket.close();
            }
            catch (IOException e) {
                Log.e(getClass().getSimpleName(),e.getMessage(),e);
            }
        }
    }


    class BluetoothSendRecieveDataThread extends Thread {

        private BluetoothSocket mmBluetoothSocket;
        private InputStream inputStream;
        private OutputStream outputStream;
        private OBDRequest initRequest;
        private OBDRequest pidDataRequest;

        public BluetoothSendRecieveDataThread (BluetoothSocket bluetoothSocket) {

            mmBluetoothSocket =bluetoothSocket;

            try {
                inputStream= mmBluetoothSocket.getInputStream();
                outputStream= mmBluetoothSocket.getOutputStream();
            }

            catch (IOException e) {

                Log.e(getClass().getSimpleName(),e.getMessage());
            }

            initRequest= new OBDInitRequest();
            pidDataRequest= new OBDCurrentDataRequest();

        }

        public void run () {

            try {

                initRequest.Run(inputStream, outputStream);


            }

            catch (IOException e) {

                Log.e(getClass().getSimpleName(),e.getMessage());
            }


            while (mState==STATE_CONNECTED) {


                try {
                    pidDataRequest.Run(inputStream, outputStream);
                    Message m = mHandler.obtainMessage(Constants.MESSAGE_OBD, pidDataRequest);
                    mHandler.sendMessage(m);
                }


                catch (IOException e) {

                    Log.e(getClass().getSimpleName(),e.getMessage());

                }
            }
        }


         public void cancel () {

            try {

                mmBluetoothSocket.close();
            }

            catch (IOException e) {
                Log.e(getClass().getSimpleName(),e.getMessage(),e);
            }
        }
    }
}




