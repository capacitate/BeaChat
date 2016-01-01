package com.example.junhong.beachat;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements BeaconConsumer{

    private ListView lv;
    private ArrayList<HashMap<String, String>> beaconList;
    private BeaconManager beaconManager;
    private final int STOP = 10;
    private static final ScheduledExecutorService delayed_work = Executors.newSingleThreadScheduledExecutor();

    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lv = (ListView)findViewById(R.id.near_beacon_list);
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id){
            case R.id.action_settings:
                return true;

            case R.id.action_refresh:
                //find near beacon list and make a HashMap<String, String>
                Log.i(TAG, "action_refresh");
                try {
//                    final Region myRegion = new Region("myRangingUniqueId", null, null, null);
                    beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
//                    beaconManager.stopRangingBeaconsInRegion(myRegion);
                    Runnable stop_scan = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Log.i(TAG, "stop the ranging");
                                beaconManager.stopRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    };

                    delayed_work.schedule(stop_scan, STOP, TimeUnit.SECONDS);

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBeaconServiceConnect() {
        Log.i(TAG, "onBeaconServiceConnect");

        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i(TAG, "did Enter the Region");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "did Exit the Region");
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {

            }
        });

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
                Log.i(TAG, "did RangeBeacons in Region");
            }
        });
    }
}
