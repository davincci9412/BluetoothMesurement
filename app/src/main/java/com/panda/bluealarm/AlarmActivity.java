package com.panda.bluealarm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Created by Panda on 1/23/2017.
 */

public class AlarmActivity extends Activity
{
    private TextView alarm_text;
    private ImageButton stop_alarm;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alam_layout);

        alarm_text = (TextView)findViewById(R.id.Alarm_String);
        stop_alarm = (ImageButton)findViewById(R.id.btnStopAlarm);

        String dev_name = getIntent().getStringExtra("blue_name");
        alarm_text.setText("Detected below bluetooth device...\n" + dev_name);

        stop_alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
               stopAlarm();
            }
        });

        startService(new Intent(getBaseContext(), AlarmService.class));
    }

    @Override
    protected void onUserLeaveHint()
    {
        stopAlarm();
        super.onUserLeaveHint();
    }

    @Override
    public void onBackPressed()
    {
        stopAlarm();
        super.onBackPressed();
    }

    private void stopAlarm()
    {
        stopService(new Intent(getBaseContext(), AlarmService.class));
        finish();
    }
}
