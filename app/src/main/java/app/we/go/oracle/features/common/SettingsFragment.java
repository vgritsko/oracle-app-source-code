package app.we.go.oracle.features.common;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import app.we.go.oracle.R;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsFragment extends DialogFragment {

    @BindView (R.id.seekBar)
    SeekBar seekBar;
    private int sampleRate;

    public SettingsFragment(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        getDialog().setTitle(getResources().getString(R.string.sample_rate));

        seekBar.setProgress(sampleRate/50);

        seekBar.setOnSeekBarChangeListener((MainActivity) getActivity());

    }

}
