package com.myapp.checkWaterLevel;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WaterLevelSimulatorActivity extends AppCompatActivity
{
    public static Switch motorToggle;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_level_simulator);

        motorToggle = (Switch) findViewById(R.id.motor_toggle);
        final TankView tankView = findViewById(R.id.custom_tank_view);

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
}