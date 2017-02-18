package app.we.go.oracle.features.landing;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import app.we.go.oracle.R;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by apapad on 23/11/16.
 */

public class SensorRow extends LinearLayout {

    @BindView(R.id.sensor_name)
    TextView nameView;

    @BindView(R.id.checkbox)
    CheckBox checkBox;
    private SensorCheckedListener listener;

    public SensorRow(Context context) {
        this(context, null, 0);
    }

    public SensorRow(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SensorRow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.widget_sensor_layout, this, true);

        ButterKnife.bind(this, view);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SensorRow, defStyleAttr, 0);


        String text = "";

        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);

            if (attr == R.styleable.SensorRow_name) {
                text = a.getString(attr);
            }
        }
        nameView.setText(text);

        a.recycle();


        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (listener != null) {
                    listener.onSensorCheckStatusChanged(isChecked);
                }
            }
        });

    }


    public void setListener(SensorCheckedListener listener) {
        this.listener = listener;
    }

    public void setAvailable(boolean available) {
        checkBox.setEnabled(available);
        checkBox.setChecked(available);
        nameView.setTextColor(ContextCompat.getColor(getContext(), available ? R.color.colorPrimaryDark : R.color.light_blue));
        nameView.setTypeface(null, available ? Typeface.BOLD : Typeface.BOLD_ITALIC);

    }

    public boolean isChecked() {
        return checkBox.isChecked();
    }

    public void setChecked(boolean checked) {
        checkBox.setChecked(checked);
    }


    public interface SensorCheckedListener {
        void onSensorCheckStatusChanged(boolean checked);
    }
}
