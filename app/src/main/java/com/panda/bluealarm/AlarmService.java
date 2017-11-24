package com.panda.bluealarm;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Vibrator;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;

/**
 * Created by Panda on 1/23/2017.
 */

public class AlarmService extends Service
{
    // Time period between two vibration events
    private final static int VIBRATE_DELAY_TIME = 2000;
    // Vibrate for 1000 milliseconds
    private final static int DURATION_OF_VIBRATION = 1000;
    private Vibrator mVibrator;

    private Handler mHandler = new Handler();
    public static Camera cam = null;
    private Runnable mVibrationRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            mVibrator.vibrate(DURATION_OF_VIBRATION);
            // Provide loop for vibration
            mHandler.postDelayed(mVibrationRunnable,
                    DURATION_OF_VIBRATION + VIBRATE_DELAY_TIME);
        }
    };

    public AlarmService()
    {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        HandlerThread ht = new HandlerThread("alarm_service");
        ht.start();
        mHandler = new Handler(ht.getLooper());

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if( mVibrator.hasVibrator()) {
            mHandler.post(mVibrationRunnable);
        }

        cam_falsh_on();
        return super.onStartCommand(intent, flags, startId);
    }

    public void cam_falsh_on()
    {
        try {
            if (getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_FLASH)) {
                cam = Camera.open();
                Parameters p = cam.getParameters();
                p.setFlashMode(Parameters.FLASH_MODE_TORCH);
                cam.setParameters(p);
                cam.startPreview();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void cam_flash_off()
    {
        try {
            if (getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_FLASH)) {
                cam.stopPreview();
                cam.release();
                cam = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy()
    {
        mVibrator.cancel();
        mHandler.removeCallbacksAndMessages(null);
        cam_flash_off();
        super.onDestroy();
    }
}
