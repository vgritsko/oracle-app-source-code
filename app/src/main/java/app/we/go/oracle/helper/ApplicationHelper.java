package app.we.go.oracle.helper;

import android.app.Application;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by Vadim on 21.02.2017.
 */

public  class ApplicationHelper extends Application {


    public static GoogleApiClient mGoogleApiClient;

    public static GoogleApiClient getmGoogleApiClient() {
        return  mGoogleApiClient;
    }


}
