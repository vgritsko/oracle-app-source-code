package app.we.go.oracle.obd2.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;

import java.util.Set;

/**
 * Created by Vadim on 17.01.2017.
 */

public class BondedBluetoothDevicesDialog extends Dialog implements View.OnClickListener  {

    private Activity activityInstance;
    private Dialog dialog;
    private Button connect;
    private Set<BluetoothDevice> mBondedBluetoothDevices;
    private String [] mBondedBluetoothDeviceNames;
    private BluetoothAdapter mBlueToothAdapter;


    private BondedDevicesDialogListener dialogListener;

    public BondedDevicesDialogListener getDialogListener() {
        return dialogListener;
    }

    public void setDialogListener(BondedDevicesDialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }



    public BondedBluetoothDevicesDialog(Activity activity)
    {
        super(activity);
        this.activityInstance=activity;
        mBlueToothAdapter=BluetoothAdapter.getDefaultAdapter();
        mBondedBluetoothDevices= mBlueToothAdapter.getBondedDevices();


        if (this.mBondedBluetoothDevices.size()>0)
        {
            this.mBondedBluetoothDeviceNames = new String [this.mBondedBluetoothDevices.size()];

            int counter =0;
            for (BluetoothDevice device :  mBondedBluetoothDevices)
            {
                mBondedBluetoothDeviceNames[counter] = device.getName();
                counter++;
            }

        }



    }

    public Dialog CreateDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(activityInstance);
        builder.setTitle("Select a device to connect");
        builder.setItems(mBondedBluetoothDeviceNames, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                dialogListener.UserSelectedValue(mBondedBluetoothDeviceNames[item]);

            }
        });

        return  builder.create();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

    }
}
