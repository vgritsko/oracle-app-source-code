package app.we.go.oracle.features.common;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

import app.we.go.oracle.R;

public class PairedBluetoothDevicesListActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paired_bluetooth_devices_list);

        setResult(Activity.RESULT_CANCELED);

        ArrayAdapter<String> pairedDevicesArrayAdapter =
                new ArrayAdapter<String>(this,R.layout.device_name);
        ListView pairedDevicesListView = (ListView) findViewById(R.id.paired_devices);
        pairedDevicesListView.setAdapter(pairedDevicesArrayAdapter);
        pairedDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                mBluetoothAdapter.cancelDiscovery();

                // Get the device MAC address, which is the last 17 chars in the View
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);

                Intent intent = new Intent();
                intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

                setResult(Activity.RESULT_OK, intent);
                finish();

            }
        });

        mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices= mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size()>0) {
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
        else {
            String noDevices =getResources().getString(R.string.no_paired_devices);
            pairedDevicesArrayAdapter.add(noDevices);
        }
    }


}
