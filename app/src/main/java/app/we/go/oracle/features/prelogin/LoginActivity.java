package app.we.go.oracle.features.prelogin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import app.we.go.oracle.R;

/**
 * Created by apapad on 23/11/16.
 */
public class LoginActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public static Intent newIntent(Context context) {
        Intent i = new Intent(context, LoginActivity.class);
        return i;
    }
}
