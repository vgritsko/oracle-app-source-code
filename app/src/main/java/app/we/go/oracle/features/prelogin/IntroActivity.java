package app.we.go.oracle.features.prelogin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ui.ResultCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;

import app.we.go.oracle.R;
import app.we.go.oracle.features.common.MainActivity;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by apapad on 23/11/16.
 */

public class IntroActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 111;
    private FirebaseAuth auth;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            startActivity(MainActivity.newIntent(this, user.getDisplayName()));
            finish();
        } else {

            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setProviders(Arrays.asList(
                                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))

                            .build(),
                    RC_SIGN_IN);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // user is signed in!
            startActivity(MainActivity.newIntent(this, auth.getCurrentUser().getDisplayName()));
            finish();
            return;
        }

        // Sign in canceled
        if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, R.string.error_login_cancelled, Toast.LENGTH_LONG).show();
            return;
        }

        // No network
        if (resultCode == ResultCodes.RESULT_NO_NETWORK) {
            Toast.makeText(this, R.string.error_login_failed, Toast.LENGTH_LONG).show();
            return;
        }

        // User is not signed in. Maybe just wait for the user to press
        // "sign in" again, or show a message.
    }
}
