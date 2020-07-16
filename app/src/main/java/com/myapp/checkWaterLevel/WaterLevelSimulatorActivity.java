package com.myapp.checkWaterLevel;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.widget.CheckBox;

public class WaterLevelSimulatorActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_level_simulator);

        CheckBox btoothStatusCheckbox = findViewById(R.id.BtoothStatusCheckbox);
        btoothStatusCheckbox.setChecked(true);
        btoothStatusCheckbox.setEnabled(false);

        CheckBox sensorStatusChecbox = findViewById(R.id.SensorStatusCheckBox);
        sensorStatusChecbox.setChecked(true);
        sensorStatusChecbox.setEnabled(false);
    }
}