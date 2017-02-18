package app.we.go.oracle.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPreferencesHelper {


    private static final String PENDING = "PENDING";
    private static final String SAMPLE_RATE = "SAMPLE_RATE";
    private static final String FOUR_WHEELER= "FOUR_WHEELER";
    private static final String HANDHELD = "HANDHELD";
    private static final int DEFAULT_SAMPLE_RATE = 50;
    private final SharedPreferences prefs;

    public SharedPreferencesHelper(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }


    public boolean getHasPending() {
        return prefs.getBoolean(PENDING, false);
    }

    public void setPending(boolean pending) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PENDING, pending);
        editor.apply();
    }

    public int getSampleRate() {
        return prefs.getInt(SAMPLE_RATE, DEFAULT_SAMPLE_RATE);
    }

    public void setSampleRate(int sampleRate) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(SAMPLE_RATE, sampleRate);
        editor.apply();
    }


    public boolean getIsFourWheeler() {
        return prefs.getBoolean(FOUR_WHEELER, true);
    }

    public void setFourWheeler(boolean fourWheeler) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(FOUR_WHEELER, fourWheeler);
        editor.apply();
    }

    public boolean getIsHandheld() {
        return prefs.getBoolean(HANDHELD, true);
    }

    public void setHandheld(boolean handheld) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(HANDHELD, handheld);
        editor.apply();
    }

}
