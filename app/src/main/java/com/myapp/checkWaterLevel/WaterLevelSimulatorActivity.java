package com.myapp.checkWaterLevel;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

public class WaterLevelSimulatorActivity extends AppCompatActivity
{
    public static Switch motorToggle;
    public static ImageView sensorSuccess;
    public static ImageView sensorFailure;
    public static TextView sensorStatusText;
    public static ProgressBar sensorStatusCheck;
    private static boolean isCurrentSensorStatusSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_level_simulator);

        motorToggle = (Switch) findViewById(R.id.motor_toggle);
        final TankView tankView = findViewById(R.id.custom_tank_view);
        sensorSuccess = (ImageView) findViewById(R.id.sensor_status_success);
        sensorFailure = (ImageView) findViewById(R.id.sensor_status_failure);
        sensorStatusText = (TextView) findViewById(R.id.sensor_status_text);
        sensorStatusCheck = (ProgressBar) findViewById(R.id.sensor_status_check);

        sensorStatusCheck
                .getIndeterminateDrawable()
                .setColorFilter(Color.parseColor("#3916A4"), PorterDuff.Mode.SRC_IN);

        if (isCurrentSensorStatusSuccess)
        {
            sensorSuccess.setVisibility(View.VISIBLE);
            sensorStatusText.setText("Sensor Working");

        }

        motorToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    tankView.startWaterFlow();
                }
                else
                {
                    tankView.stopWaterFlow();
                }
            }
        });

    }

    public static void setSensorStatus(int sensorStatusCode)
    {
        if (sensorStatusCode == SensorStatus.WORKING.code)
        {
            if (isCurrentSensorStatusSuccess)
            {
                return;
            }
            isCurrentSensorStatusSuccess = true;
            sensorSuccess.setVisibility(View.VISIBLE);
            sensorFailure.setVisibility(View.INVISIBLE);
            sensorStatusCheck.setVisibility(View.INVISIBLE);
            sensorStatusText.setText("Sensor Working");
        }
        else if (sensorStatusCode == SensorStatus.NOT_WORKING.code)
        {
            isCurrentSensorStatusSuccess = false;
            sensorSuccess.setVisibility(View.INVISIBLE);
            sensorFailure.setVisibility(View.VISIBLE);
            sensorStatusCheck.setVisibility(View.INVISIBLE);
            sensorStatusText.setText("Problem in sensor data");
        }
        else if (sensorStatusCode == SensorStatus.CHECKING.code)
        {
            isCurrentSensorStatusSuccess = false;
            sensorSuccess.setVisibility(View.INVISIBLE);
            sensorFailure.setVisibility(View.INVISIBLE);
            sensorStatusCheck.setVisibility(View.VISIBLE);
            sensorStatusText.setText("Checking Sensor");
        }
    }

    public enum SensorStatus
    {
        CHECKING(0),
        WORKING(1),
        NOT_WORKING(2);

        public final int code;
        SensorStatus(int statusCode)
        {
            this.code = statusCode;
        }
    }
}