package com.myapp.checkWaterLevel;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class WaterLevelSimulatorActivity extends AppCompatActivity
{
    public static Switch motorToggle;
    public static ImageView sensorSuccess;
    public static ImageView sensorFailure;
    public static TextView sensorStatusText;
    public static ProgressBar sensorStatusCheck;
    private static boolean isCurrentSensorStatusSuccess;

    private static Context baseContext;
    private static AlarmManager alarmManager;
    private static MediaPlayer mMediaPlayer;
    private static Vibrator vibrator;


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

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        baseContext = getBaseContext();
        mMediaPlayer = new MediaPlayer();

        motorToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    tankView.startWaterFlow();
                }
                else
                {
                    if (mMediaPlayer.isPlaying())
                    {
                        mMediaPlayer.stop();
                        vibrator.cancel();
                    }
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

    public static void startAlarmNow()
    {
        if (!mMediaPlayer.isPlaying())
        {
            vibrator = (Vibrator) baseContext.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(2000);

            playSound(baseContext, getAlarmUri());
        }
    }

    private static void playSound(Context context, Uri alert) {
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(context, alert);
            final AudioManager audioManager = (AudioManager) context
                    .getSystemService(Context.AUDIO_SERVICE);
            if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }
        } catch (IOException e) {
            System.out.println("OOPS");
        }
    }

    //Get an alarm sound. Try for an alarm. If none set, try notification,
    //Otherwise, ringtone.
    private static Uri getAlarmUri() {
        Uri alert = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alert == null) {
            alert = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (alert == null) {
                alert = RingtoneManager
                        .getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }
        return alert;
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