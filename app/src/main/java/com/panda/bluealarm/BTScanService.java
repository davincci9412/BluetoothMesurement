package com.panda.bluealarm;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import java.util.Collection;

/**
 * Created by Tiger on 1/20/2017.
 */

public class BTScanService extends Service implements BeaconConsumer
{
    private final int ALARM_DISTANCE = 5;
    private final int ALARM_RSSI = -80;
    private BluetoothAdapter BTAdapter;
    private BlueFoundReceiver receiver ;

    protected static final String TAG = "BlueAlarm";
    private BeaconManager beaconManager ;

    public BTScanService(){     };

    @Override
    public void onCreate()
    {
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.bind(this);

        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        receiver = new BlueFoundReceiver();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(receiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d("service", "I starting...");
        int res = super.onStartCommand(intent, flags, startId);
        BTAdapter.startDiscovery();
        return res;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class BlueFoundReceiver extends BroadcastReceiver
    {
        String blue_uuid = null;
        String blue_name = null;
        String blue_tmp_name = null;
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            if(BluetoothDevice.ACTION_FOUND.equals(action))
            {
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                blue_name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                blue_uuid = intent.getStringExtra(BluetoothDevice.EXTRA_UUID);
                Log.e( "service", "name= "+ blue_name + ", " + "UUID= " + blue_uuid + ", " + rssi);
                if(!blue_name.equals(blue_tmp_name) && rssi > ALARM_RSSI)
                {
                    blue_tmp_name = blue_name;
                    Intent alarmIntent =new Intent(BTScanService.this, AlarmActivity.class);
                    alarmIntent.putExtra("blue_name", blue_name);
                    alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(alarmIntent);
                }
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                Log.e("service", "finished");
                BTAdapter.startDiscovery();
            }
        }
    }

    @Override
    public void onDestroy()
    {
        BTAdapter.cancelDiscovery();
        beaconManager.unbind(this);
        super.onDestroy();
    }

    @Override
    public void onBeaconServiceConnect()
    {
        final Region region = new Region("myBeacons", null, null, null);
        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                try {
                    Log.d(TAG, "didEnterRegion");
                    beaconManager.startRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didExitRegion(Region region) {
                try {
                    Log.d(TAG, "didExitRegion");
                    beaconManager.stopRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {

            }
        });
        beaconManager.setRangeNotifier(new RangeNotifier() {
            String beacon_name;
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                for(Beacon oneBeacon : beacons) {
                    Log.e(TAG, "distance: " + oneBeacon.getDistance() + " id:" + oneBeacon.getId1() + "/" + oneBeacon.getId2() + "/" + oneBeacon.getId3());
                    if( oneBeacon.getDistance() < ALARM_DISTANCE )
                    {
                        if(!oneBeacon.getBluetoothName().equals(beacon_name))
                        {
                            beacon_name = oneBeacon.getBluetoothName();
                            Intent alarmIntent =new Intent(BTScanService.this, AlarmActivity.class);
                            alarmIntent.putExtra("blue_name", beacon_name);
                            alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(alarmIntent);
                        }
                    }
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
