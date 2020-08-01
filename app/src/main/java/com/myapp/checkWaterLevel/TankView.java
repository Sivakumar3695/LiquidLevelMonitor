package com.myapp.checkWaterLevel;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TankView extends View
{
    Paint paint = new Paint();
    Paint incomingWaterPipe = new Paint();
    Paint incomingWaterPipeFlow = new Paint();
    int finalWidth;
    int finalHeight;

    int tankHeight = 900;

    int leftStart = 150;
    int topStart = 200;
    int rightMost;

    int tankHeightEnd = topStart + tankHeight;
    int incomingPipeRight = leftStart + 100;
    int incomingPipe2Bottom = topStart + 200;
    int incomingPipe_1_Flow_top = topStart + 105;
    int incomingPipe_1_Flow_Right = leftStart -50;
    int incomingPipe_2_Flow_Top = topStart + 145;
    int incomingPipe_2_Flow_Bottom = topStart + 145;
    int mainWaterLevel = tankHeightEnd;//tankHeight;
    boolean isWaterFlowInProgress = false;
    boolean stopFlowInProgress = false;
    int multiplier = 10;

    AnimatorSet startAnimator = new AnimatorSet();
    AnimatorSet stopAnimator = new AnimatorSet();

    int sensorFailureCount = 0;

    public TankView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(Color.LTGRAY);
        incomingWaterPipe.setColor(Color.WHITE);
        incomingWaterPipe.setStyle(Paint.Style.FILL);

        if (finalWidth < 1000)
        {
            topStart = 100;
            leftStart = 100;
            tankHeight = 720;
            multiplier = 8;
        }

        initPipeDimensions();
        HttpGetRequest httpGetRequest = new HttpGetRequest();
        httpGetRequest.execute();

        incomingWaterPipeFlow.setColor(Color.parseColor("#a5f3eb"));
        //startWaterFlow();

    }

    private void initPipeDimensions()
    {
        tankHeightEnd = topStart + tankHeight;
        incomingPipeRight = leftStart + 100;
        incomingPipe2Bottom = topStart + 200;
        incomingPipe_1_Flow_top = topStart + 105;
        incomingPipe_1_Flow_Right = leftStart -50;
        incomingPipe_2_Flow_Top = topStart + 145;
        incomingPipe_2_Flow_Bottom = topStart + 145;
        mainWaterLevel = tankHeightEnd;
    }

    @Override public void onDraw(Canvas canvas)
    {
//      Log.v("Test", "FinalWidth:" + finalWidth + ", FinalHeight:" + finalHeight);
        Log.v("Test", "Main Level:" + mainWaterLevel);
        canvas.drawRect(leftStart,topStart,rightMost, tankHeightEnd, paint);  //--> Main Tank

        //the input for the following will be obtained from Bluetooth connected to Arduino!
        canvas.drawRect(leftStart,mainWaterLevel,rightMost, tankHeightEnd, incomingWaterPipeFlow); //--> Main Tank filled with Water

        canvas.drawRect(leftStart - 50,(tankHeightEnd)-100,leftStart,(tankHeightEnd)-50, paint); //--> Left Bottom Outlet
        canvas.drawRect((rightMost),topStart+30,(rightMost)+50,topStart+80, paint); // --> Right Top Outlet

        canvas.drawRect(leftStart-50,topStart+100,incomingPipeRight,topStart+150, incomingWaterPipe);// -> Incoming Water Pipe (horizontal)
        canvas.drawRect(incomingPipeRight-50,topStart+150,incomingPipeRight,incomingPipe2Bottom, incomingWaterPipe);// --> Incoming water pipe (vertical)

        int incomingWaterPipe1Level = isWaterFlowInProgress ? topStart+105 : stopFlowInProgress ? incomingPipe_1_Flow_top : getInletOutletWaterLevel(topStart+105, topStart+145);
        int horizontalPipeFlowRight = isWaterFlowInProgress ? incomingPipe_1_Flow_Right : incomingPipeRight-5;
        canvas.drawRect(leftStart-50,incomingWaterPipe1Level, horizontalPipeFlowRight,topStart+145, incomingWaterPipeFlow); // --> Incoming water flow (horizontal)

        int incomingWaterPipe2Level = (isWaterFlowInProgress || stopFlowInProgress) ? incomingPipe_2_Flow_Top : getInletOutletWaterLevel(incomingPipe_2_Flow_Top, incomingPipe2Bottom);
        int verticalPipeFlowBottom = (isWaterFlowInProgress || stopFlowInProgress) ? incomingPipe_2_Flow_Bottom : incomingPipe2Bottom;
        canvas.drawRect(incomingPipeRight-45,incomingWaterPipe2Level,incomingPipeRight-5, verticalPipeFlowBottom, incomingWaterPipeFlow); // --> Incoming water flow (vertical)

        int leftBottomOutletWaterLevel = getInletOutletWaterLevel(tankHeightEnd -90, tankHeightEnd -60);
        canvas.drawRect(leftStart-50,leftBottomOutletWaterLevel,leftStart, tankHeightEnd -60, incomingWaterPipeFlow); //--> Left Bottom Outlet Water flow

        int rightTopOutletWaterLevel = getInletOutletWaterLevel(topStart+40, topStart+70);
        canvas.drawRect((rightMost),rightTopOutletWaterLevel,(rightMost)+50,topStart+70, incomingWaterPipeFlow); // --> Right Top Outlet Water flow

        if (mainWaterLevel == topStart)
        {
            WaterLevelSimulatorActivity.motorToggle.setChecked(false);
        }
    }

    private int getInletOutletWaterLevel(int start, int end)
    {
        if (mainWaterLevel <= start)
        {
            return start;
        }
        if (mainWaterLevel >= start && mainWaterLevel <= end)
        {
            return mainWaterLevel;
        }
        return end;
    }

    public void startWaterFlow()
    {
        isWaterFlowInProgress = true;
        stopFlowInProgress = false;

        incomingPipe_1_Flow_Right = leftStart-50;
        incomingPipe_2_Flow_Bottom = topStart+145;
        incomingPipe_2_Flow_Top = topStart+145;

        final PropertyValuesHolder prop_pipe_1_right = PropertyValuesHolder.ofInt("PROP_PIPE_1_RIGHT", leftStart-50, incomingPipeRight-5);
        ObjectAnimator pipe1Animator = new ObjectAnimator();
        pipe1Animator.setValues(prop_pipe_1_right);
        pipe1Animator.setDuration(2000);
        pipe1Animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                incomingPipe_1_Flow_Right = (int) animation.getAnimatedValue("PROP_PIPE_1_RIGHT");
                invalidate();
            }
        });

        final PropertyValuesHolder prop_pipe_2_bottom = PropertyValuesHolder.ofInt("PROP_PIPE_2_BOTTOM", topStart+145, tankHeightEnd);
        ObjectAnimator pipe2Animator = new ObjectAnimator();
        pipe2Animator.setValues(prop_pipe_2_bottom);
        pipe2Animator.setDuration(2000);
        pipe2Animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                incomingPipe_2_Flow_Bottom = (int) animation.getAnimatedValue("PROP_PIPE_2_BOTTOM");
                invalidate();
            }
        });

//        final PropertyValuesHolder main_water_level_prop = PropertyValuesHolder.ofInt("MAIN_WATER_LEVEL_PROP", tankHeight, 200);
//        ObjectAnimator mainWaterLevelAnimator = new ObjectAnimator();
//        mainWaterLevelAnimator.setValues(main_water_level_prop);
//        mainWaterLevelAnimator.setDuration(2000);
//        mainWaterLevelAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                mainWaterLevel = (int) animation.getAnimatedValue("MAIN_WATER_LEVEL_PROP");
//                invalidate();
//            }
//        });

        startAnimator.playSequentially(pipe1Animator, pipe2Animator);
        startAnimator.start();
        new HttpGetRequest().execute();
    }

    public void stopWaterFlow()
    {
        if (startAnimator.isRunning())
        {
            startAnimator.cancel();
        }
        stopFlowInProgress = true;
        isWaterFlowInProgress = false;

        incomingPipe_2_Flow_Top = topStart+145;
        incomingPipe_1_Flow_top = topStart+105;

        if (getInletOutletWaterLevel(topStart+100, topStart+150) < (topStart + 100))
        {
            return;
        }

        int allowedTopValue = getInletOutletWaterLevel(topStart+105, topStart+145);
        final PropertyValuesHolder prop_pipe_1_top = PropertyValuesHolder.ofInt("PROP_PIPE_1_TOP", topStart+105, allowedTopValue);
        ObjectAnimator pipe1Animator = new ObjectAnimator();
        pipe1Animator.setValues(prop_pipe_1_top);
        pipe1Animator.setDuration(2000);
        pipe1Animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                incomingPipe_1_Flow_top = (int) animation.getAnimatedValue("PROP_PIPE_1_TOP");
                invalidate();
            }
        });

        int allowedBottomValue = getInletOutletWaterLevel(topStart+145, topStart+200) < mainWaterLevel ? mainWaterLevel : getInletOutletWaterLevel(topStart+145, topStart+200);
        final PropertyValuesHolder prop_pipe_2_bottom = PropertyValuesHolder.ofInt("PROP_PIPE_2_TOP", topStart+145, allowedBottomValue);
        ObjectAnimator pipe2Animator = new ObjectAnimator();
        pipe2Animator.setValues(prop_pipe_2_bottom);
        pipe2Animator.setDuration(2000);
        pipe2Animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                incomingPipe_2_Flow_Top = (int) animation.getAnimatedValue("PROP_PIPE_2_TOP");
                invalidate();
            }
        });

        stopAnimator.playSequentially(pipe1Animator, pipe2Animator);
        stopAnimator.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.v("Chart onMeasure w", MeasureSpec.toString(widthMeasureSpec));
        Log.v("Chart onMeasure h", MeasureSpec.toString(heightMeasureSpec));

        int desiredWidth = getSuggestedMinimumWidth() + getPaddingLeft() + getPaddingRight();
        int desiredHeight = getSuggestedMinimumHeight() + getPaddingTop() + getPaddingBottom();

        finalWidth = resolveSize(desiredWidth, widthMeasureSpec);
        finalHeight = resolveSize(desiredHeight, heightMeasureSpec);
        rightMost = finalWidth < 1000 ? finalWidth*9/10 : finalWidth*3/4;
        setMeasuredDimension(finalWidth, finalHeight);
    }

    public class HttpGetRequest extends AsyncTask<String, Void, String>
    {
        public static final String REQUEST_METHOD = "GET";
        public static final int READ_TIMEOUT = 15000;
        public static final int CONNECTION_TIMEOUT = 15000;
        public static final String URL = "http://192.168.1.1/getDistance";
        @Override
        protected String doInBackground(String... params)
        {
            String result;
            String inputLine;
            try
            {
                //Create a URL object holding our url
                URL myUrl = new URL(URL);
                //Create a connection
                HttpURLConnection connection =(HttpURLConnection) myUrl.openConnection();
                //Set methods and timeouts
                connection.setRequestMethod(REQUEST_METHOD);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);

                //Connect to our url
                connection.connect();
                //Create a new InputStreamReader
                InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
                //Create a new buffered reader and String Builder
                BufferedReader reader = new BufferedReader(streamReader);
                StringBuilder stringBuilder = new StringBuilder();
                //Check if the line we are reading is not null
                while((inputLine = reader.readLine()) != null)
                {
                    stringBuilder.append(inputLine);
                }
                //Close our InputStream and Buffered reader
                reader.close();
                streamReader.close();
                //Set our result equal to our stringBuilder
                result = stringBuilder.toString();
                int resultInt = (Integer.valueOf(result));
                if (!stopFlowInProgress && resultInt != 0)
                {
                    sensorFailureCount = 0;
                    mainWaterLevel = topStart + (multiplier * resultInt);
                }
                else if (resultInt == 0)
                {
                    sensorFailureCount++;
                }

            }
            catch(IOException e)
            {
                sensorFailureCount++;
                e.printStackTrace();
                result = null;
            }
            return result;
        }
        protected void onPostExecute(String result)
        {
            Handler repetitionHandler = new Handler();

            if (sensorFailureCount >= 5)
                WaterLevelSimulatorActivity.setSensorStatus(WaterLevelSimulatorActivity.SensorStatus.NOT_WORKING.code);
            else if (result != null && !result.equals("0"))
            {
                WaterLevelSimulatorActivity.setSensorStatus(WaterLevelSimulatorActivity.SensorStatus.WORKING.code);
                invalidate();
            }
            else
                WaterLevelSimulatorActivity.setSensorStatus(WaterLevelSimulatorActivity.SensorStatus.CHECKING.code);

            if (isWaterFlowInProgress || (result == null || result.equals("0")))
            {
                repetitionHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new HttpGetRequest().execute();
                        invalidate();
                    }
                }, 3000);
                super.onPostExecute(result);
            }
        }
    }
}
