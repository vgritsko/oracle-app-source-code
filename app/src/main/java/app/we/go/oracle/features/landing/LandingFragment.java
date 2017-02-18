package app.we.go.oracle.features.landing;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import app.we.go.oracle.R;
import app.we.go.oracle.features.common.MainActivity;
import app.we.go.oracle.features.common.PairedBluetoothDevicesListActivity;
import app.we.go.oracle.features.recording.RecordingCallbacks;
import app.we.go.oracle.features.recording.SensorsToRecord;
import app.we.go.oracle.obd2.OBDSenderReciverService;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by apapad on 23/11/16.
 */

public class LandingFragment extends Fragment {

    private static final String ARG_FOUR_WHEELER = "FOUR_WHEELER";
    private static final String ARG_HANDHELD = "HANDHELD";
    @BindView(R.id.sensor_accelerometer)
    SensorRow accelerometer;
    @BindView(R.id.sensor_gyroscope)
    SensorRow gyroscope;
    @BindView(R.id.sensor_gps)
    SensorRow gps;
    @BindView(R.id.sensor_compass)
    SensorRow compass;
    @BindView(R.id.orientation_vector)
    SensorRow rotationVector;
    @BindView(R.id.sensor_proximity)
    SensorRow proximity;
    @BindView(R.id.sensor_magnetometer)
    SensorRow magnetometer;

    @BindView(R.id.four_wheeler)
    RadioButton fourWheelerRadioButton;
    @BindView(R.id.two_wheeler)
    RadioButton twoWheelerRadioButton;

    @BindView(R.id.handheld)
    RadioButton handheldRadioButton;
    @BindView(R.id.docked)
    RadioButton dockedRadioButton;

    private SensorCollection sensorAvailability;
    private Dialog progressDialog;
    private boolean sensorsInitialized;

    private BluetoothAdapter mBluetooothAdapter;
    private OBDSenderReciverService obdService;


    public LandingFragment() {
    }

    public static LandingFragment newInstance(boolean fourWheeler, boolean handheld) {
        LandingFragment f = new LandingFragment();
        Bundle b = new Bundle();
        b.putBoolean(ARG_FOUR_WHEELER, fourWheeler);
        b.putBoolean(ARG_HANDHELD, handheld);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.landing_layout, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (sensorsInitialized) {
            onAvailabilityReady();
        } else {
            progressDialog = ProgressDialog.show(getActivity(), "",
                    getActivity().getString(R.string.loading_checking_sensors), true);

            progressDialog.show();
        }


        final SensorRow.SensorCheckedListener compassListener = new SensorRow.SensorCheckedListener() {
            @Override
            public void onSensorCheckStatusChanged(boolean checked) {
                if (sensorAvailability.hasOrientation()) {
                    if (accelerometer.isChecked() && magnetometer.isChecked()) {
                        compass.setAvailable(true);
                    } else {
                        compass.setChecked(false);
                        compass.setAvailable(false);
                    }
                }
            }
        };
        accelerometer.setListener(compassListener);
        magnetometer.setListener(compassListener);


        fourWheelerRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ((SharedPreferencesValueChanged) getActivity()).onFourWheelerValueChange(isChecked);
            }
        });
        handheldRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ((SharedPreferencesValueChanged) getActivity()).onHandheldValueChange(isChecked);
            }
        });


        obdService = new OBDSenderReciverService(getActivity());
        MainActivity acitivity = (MainActivity) getActivity();

        if (acitivity.getOBDService()==null) {
            obdService = new OBDSenderReciverService(getActivity());
            acitivity.setOBDService(obdService);

            mBluetooothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (mBluetooothAdapter != null) {
                if (mBluetooothAdapter.isEnabled()) {

                    Intent serverIntent = new Intent(getActivity(), PairedBluetoothDevicesListActivity.class);
                    startActivityForResult(serverIntent, 0);
                }
            }
        }



    }

    @Override
    public void onResume() {
        super.onResume();


        if (!fourWheelerRadioButton.isChecked() && !twoWheelerRadioButton.isChecked()) {
            fourWheelerRadioButton.setChecked(getArguments().getBoolean(ARG_FOUR_WHEELER));
            twoWheelerRadioButton.setChecked(!getArguments().getBoolean(ARG_FOUR_WHEELER));
        }

        if (!handheldRadioButton.isChecked() && !dockedRadioButton.isChecked()) {
            handheldRadioButton.setChecked(getArguments().getBoolean(ARG_HANDHELD));
            dockedRadioButton.setChecked(!getArguments().getBoolean(ARG_HANDHELD));
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sensorAvailability = (SensorCollection) getActivity();
    }


    @OnClick(R.id.start_recording_button)
    public void  onStartRecording() {
        SensorsToRecord sensorsToRecord = new SensorsToRecord();
        sensorsToRecord.setAccelerometer(accelerometer.isChecked());
        sensorsToRecord.setGyroscope(gyroscope.isChecked());
        sensorsToRecord.setOrientation(compass.isChecked());
        sensorsToRecord.setRotationVector(rotationVector.isChecked());
        sensorsToRecord.setProximity(proximity.isChecked());
        sensorsToRecord.setMagnetometer(magnetometer.isChecked());
        sensorsToRecord.setGps(gps.isChecked());
        sensorsToRecord.setLinearAcceleration(true);


        ((RecordingCallbacks) getActivity()).startRecording(sensorsToRecord,
                fourWheelerRadioButton.isChecked(),
                handheldRadioButton.isChecked());
    }

    public void onAvailabilityReady() {
        sensorsInitialized = true;

        accelerometer.setAvailable(sensorAvailability.hasAccelerometer());
        gyroscope.setAvailable(sensorAvailability.hasGyroscope());
        gps.setAvailable(sensorAvailability.hasGps());
        compass.setAvailable(sensorAvailability.hasOrientation());
        rotationVector.setAvailable(sensorAvailability.hasRotationVector());
        proximity.setAvailable(sensorAvailability.hasProximity());
        magnetometer.setAvailable(sensorAvailability.hasMagnetometer());

        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public interface SharedPreferencesValueChanged {
        void onFourWheelerValueChange(boolean value);
        void onHandheldValueChange(boolean value);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (resultCode == Activity.RESULT_OK){
            startOBDSenderReciverService(data);
        }

    }


    private void startOBDSenderReciverService(Intent data) {

        String address = data.getExtras().
                getString(PairedBluetoothDevicesListActivity.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = mBluetooothAdapter.getRemoteDevice(address);
        obdService.start(device);
    }

    public void onDestroy () {
        if (obdService!=null) {
            obdService.stop();
        }
        super.onDestroy();

    }

}
