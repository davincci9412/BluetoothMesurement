package com.panda.bluealarm;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.minew.beaconset.BluetoothState;
import com.minew.beaconset.MinewBeaconManager;

public class MainActivity extends AppCompatActivity
{
    private boolean isDetecting;
    private TextView textViewDetectState, textIntro;
    private Button On_Off_Button;
    private PreferenceHelper myhelper;
    private ProgressDialog progress;

    private MinewBeaconManager mMinewBeaconManager;
    private static final int REQUEST_ENABLE_BT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initManager();
        checkBluetooth();

        myhelper = new PreferenceHelper(this);
        isDetecting = myhelper.getIsDetecting();
        textViewDetectState = (TextView)findViewById(R.id.textViewDetectState);
        textIntro = (TextView)findViewById(R.id.textIntro);
        On_Off_Button = (Button)findViewById(R.id.on_off_button);
        On_Off_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String state;
                if(isDetecting) state = "Stop Detecting...";
                else state = "Start Detecting...";

                progress = ProgressDialog.show(MainActivity.this, null, state);
                Thread timerThread = new Thread()
                {
                    public void run()
                    {
                        try{
                            sleep(1000);
                        }catch(InterruptedException e){
                            e.printStackTrace();
                        }finally{
                            MainActivity.this.finish();
                        }
                    }
                };
                timerThread.start();
                setDetectEnabled(!isDetecting);
                MainActivity.this.finish();
            }
        });

        if(isDetecting)
        {
            textViewDetectState.setText(R.string.encoding);
            textIntro.setText(R.string.intro_text);
            textIntro.append( "\n\n\n" + "Click bellow button to stop detecting" );
            On_Off_Button.setText( R.string.stopEncoding );
        }
        else
        {
            textViewDetectState.setText(R.string.notencoding);
            textIntro.setText(R.string.intro_text);
            textIntro.append( "\n\n\n" + "Click below button to start detecting" );
            On_Off_Button.setText( R.string.startEncoding );
        }
    }

    private void setDetectEnabled(boolean enable)
    {
        isDetecting = enable;
        myhelper.putIsDetecting(isDetecting);
        Intent intent = new Intent(this, BTScanService.class);
        if (enable)
        {
            // start detect service
            startService(intent);
        }
        else
        {
            // stop detect service
            stopService(intent);
        }
    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }


    private void initManager()
    {
        mMinewBeaconManager = MinewBeaconManager.getInstance(this);
    }
    /**
     * check Bluetooth state
     */
    private void checkBluetooth()
    {
        BluetoothState bluetoothState = mMinewBeaconManager.checkBluetoothState();
        switch (bluetoothState)
        {
            case BluetoothStateNotSupported:
                Toast.makeText(this, "Not Support BLE", Toast.LENGTH_LONG).show();
                finish();
                break;
            case BluetoothStatePowerOff:
                showBLEDialog();
                break;
            case BluetoothStatePowerOn:
                break;
        }
    }

    private void showBLEDialog()
    {
        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    }
}
