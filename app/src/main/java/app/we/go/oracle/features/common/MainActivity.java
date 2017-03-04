package app.we.go.oracle.features.common;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Toast;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import app.we.go.oracle.features.landing.LandingFragment;
import app.we.go.oracle.features.prelogin.IntroActivity;
import app.we.go.oracle.features.recording.SensorDataWriter;
import app.we.go.oracle.features.recording.SensorDataWriterService;
import app.we.go.oracle.features.recording.SensorsToRecord;
import app.we.go.oracle.helper.ApplicationHelper;
import app.we.go.oracle.obd2.OBDSenderReciverService;
import app.we.go.oracle.obd2.ui.BondedBluetoothDevicesDialog;
import app.we.go.oracle.obd2.ui.BondedDevicesDialogListener;
import app.we.go.oracle.upload.MultiUploadHelper;
import app.we.go.oracle.R;
import app.we.go.oracle.features.recording.RecordingCallbacks;
import app.we.go.oracle.features.recording.RecordingFragment;
import app.we.go.oracle.features.landing.SensorCollection;
import app.we.go.oracle.helper.SharedPreferencesHelper;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements RecordingCallbacks,
        PendingFilesIndicator, SensorCollection, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, SeekBar.OnSeekBarChangeListener,
LandingFragment.SharedPreferencesValueChanged {

    public static final String DROPBOX_APP_KEY = "qtlc787436yde3e";
    public static final String DROPBOX_APP_SECRET = "wcru89rjb2k9iz0";
    public static final String DROPBOX_TOKEN = "J_qdOSK8oeAAAAAAAAAACYVHvmh16nfcGdatU5YDKjG-VXEJps7o3FPDEq99EVxA";

    private static final String TAG_RECORD_FRAGMENT = "REC";
    private static final int REQUEST_CHECK_SETTINGS = 111;
    private static final int PERMISSION_REQ_CODE = 222;
    private static final String TAG_LANDING_FRAGMENT = "LAND";
    private static final String EXTRA_USERNAME = "username";
    private SharedPreferencesHelper sharedPrefs;
    private MenuItem pendingMenuItem;
    private boolean hasMagnetometer;
    private boolean hasAccelerometer;
    private boolean hasGyro;
    private boolean hasRotationVector;
    private boolean hasOrientation;
    private boolean hasProximity;
    private boolean hasLinearAcceleration;
    private DbxClientV2 client;
    private GoogleApiClient mGoogleApiClient;
    private boolean hasLocation;
    private LocationRequest locationRequest;

    private String selectedBondedDeviceName="OBDII";
    private BluetoothSocket mBluetoothSocket;
    private final BluetoothAdapter bluetoothAdapter= BluetoothAdapter.getDefaultAdapter();
    private OBDSenderReciverService obdService;

    private SensorDataWriterService sensorDataWriterService;
    boolean mServiceBound = false;


    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            SensorDataWriterService.LocalBinder binder = (SensorDataWriterService.LocalBinder) service;
            sensorDataWriterService = binder.getService();
            mServiceBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mServiceBound = false;
        }
    };



    public static Intent newIntent(Context context, String username) {
        Intent i = new Intent(context, MainActivity.class);
        i.putExtra(EXTRA_USERNAME, username);
        return i;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        checkSensors();


        sharedPrefs = new SharedPreferencesHelper(this);

        DbxRequestConfig config = DbxRequestConfig.newBuilder("oracle/1.0").build();
        client = new DbxClientV2(config, DROPBOX_TOKEN);

        initLocation();
        LandingFragment f = LandingFragment.newInstance(sharedPrefs.getIsFourWheeler(),
                sharedPrefs.getIsHandheld());
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, f, TAG_LANDING_FRAGMENT).commit();
        }

        Intent serviceIntent = new Intent(this,SensorDataWriterService.class);
        //startService(serviceIntent);
        bindService(serviceIntent, mConnection,Context.BIND_AUTO_CREATE);
    }

    private void initLandingFragment() {
        LandingFragment landingFragment = (LandingFragment) getSupportFragmentManager()
                .findFragmentByTag(TAG_LANDING_FRAGMENT);
        landingFragment.onAvailabilityReady();
    }

    private void initLocation() {

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(ActivityRecognition.API)
                    .build();

            ApplicationHelper.mGoogleApiClient=mGoogleApiClient;
        }

        locationRequest = new LocationRequest();
        locationRequest.setInterval(50);
        locationRequest.setFastestInterval(50);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        checkLocationPermissions();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MainActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        initLandingFragment();
                        break;
                }
            }
        });
    }

    private void checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, PERMISSION_REQ_CODE);
            return;
        } else {
            hasLocation = true;

            initLandingFragment();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQ_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasLocation = true;
            } else {
                hasLocation = false;
            }
            initLandingFragment();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                checkLocationPermissions();
            } else {
                initLandingFragment();
            }
        }
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    private void checkSensors() {
        SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);


        hasAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null;
        hasGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null;
        hasRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null;
        hasOrientation = hasRotationVector;
        hasProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null;
        hasMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null;
        hasLinearAcceleration=mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)!=null;


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        pendingMenuItem = menu.findItem(R.id.pending);
        pendingMenuItem.setVisible(sharedPrefs.getHasPending());

        return true;
    }


    @Override
    protected void onPause() {
        super.onPause();
       // stopRecordingPassive();
        RecordingFragment f = (RecordingFragment) getSupportFragmentManager()
                .findFragmentByTag(TAG_RECORD_FRAGMENT);
        if (f != null) {
            toggleIndicator(true);
        }

    }

    @Override
    public void onBackPressed() {
        boolean hasBeenHandled = stopRecordingPassive();
        if (!hasBeenHandled) {
            super.onBackPressed();
        }
    }

    private boolean stopRecordingPassive() {
        RecordingFragment f = (RecordingFragment) getSupportFragmentManager()
                .findFragmentByTag(TAG_RECORD_FRAGMENT);
        if (f != null) {
            f.stopRecording(false);
            getSupportFragmentManager().popBackStack();
            toggleIndicator(true);
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.pending:
                uploadPending();
                return true;
            case R.id.sample_rate:
                chooseSampleRate();
                return true;
            case R.id.logout:
                FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.signOut();
                Toast.makeText(this, R.string.logged_out, Toast.LENGTH_LONG).show();
                finish();
                startActivity(new Intent(this, IntroActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void chooseSampleRate() {
        SettingsFragment df = new SettingsFragment(sharedPrefs.getSampleRate());
        df.show(getSupportFragmentManager(), null);
    }

    private void uploadPending() {
        final ProgressDialog dialog = ProgressDialog.show(this, "",
                getString(R.string.uploading), true);
        dialog.show();

        File[] files = getFilesDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(SensorDataWriter.BASE_FILENAME);
            }
        });

        MultiUploadHelper multiUploadHelper = new MultiUploadHelper(client, getIntent().getStringExtra(EXTRA_USERNAME), files, new MultiUploadHelper.MultiCallback() {
            @Override
            public void onError() {
                dialog.dismiss();
                Toast.makeText(MainActivity.this, R.string.upload_failed, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete(int length) {
                dialog.dismiss();
                String s = getResources().getQuantityString(R.plurals.upload_multi_success, length, length);
                Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
                toggleIndicator(false);
            }
        });

        multiUploadHelper.start();

    }

    @Override
    public void startRecording(SensorsToRecord sensorsToRecord, boolean fourWheeler, boolean handheld) {

        /*Intent serviceIntent = new Intent(this,SensorDataWriterService.class);
        startService(serviceIntent);
        bindService(serviceIntent, mConnection,Context.BIND_AUTO_CREATE);*/

        RecordingFragment f = RecordingFragment
                .newInstance(getIntent().getStringExtra(EXTRA_USERNAME), sensorsToRecord,
                        fourWheeler, handheld,
                        sharedPrefs.getSampleRate());
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, f, TAG_RECORD_FRAGMENT)
                .addToBackStack(null)
                .commit();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }







    @Override
    public void onRecordingStopped(boolean uploadPending) {
        if (uploadPending) {
            toggleIndicator(true);
        }

        getSupportFragmentManager().popBackStack();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    @Override
    public void toggleIndicator(boolean pending) {
        sharedPrefs.setPending(pending);
        invalidateOptionsMenu();
    }

    @Override
    public boolean hasAccelerometer() {
        return hasAccelerometer;
    }

    @Override
    public boolean hasGyroscope() {
        return hasGyro;
    }

    @Override
    public boolean hasGps() {
        return hasLocation;
    }

    @Override
    public boolean hasOrientation() {
        return hasOrientation;
    }

    @Override
    public boolean hasRotationVector() {
        return hasRotationVector;
    }

    @Override
    public boolean hasProximity() {
        return hasProximity;
    }

    @Override
    public boolean hasMagnetometer() {
        return hasMagnetometer;
    }

    @Override
    public boolean hasLinearAcceleration() {
        return hasLinearAcceleration;
    }

    public DbxClientV2 getClient() {
        return client;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    public LocationRequest getLocationRequest() {
        return locationRequest;
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            sharedPrefs.setSampleRate(50*progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onFourWheelerValueChange(boolean value) {
        sharedPrefs.setFourWheeler(value);
    }

    @Override
    public void onHandheldValueChange(boolean value) {
        sharedPrefs.setHandheld(value);
    }

    public  void setOBDService(OBDSenderReciverService servie) {obdService=servie;}
    public  OBDSenderReciverService getOBDService() {return obdService;}

    public SensorDataWriterService getSensorDataWriterService () {
        return  sensorDataWriterService;
    }

    @Override
    protected void onDestroy() {

        sensorDataWriterService.stopSelf();

        if (mServiceBound) { unbindService(mConnection); }


        super.onDestroy();
    }
}

