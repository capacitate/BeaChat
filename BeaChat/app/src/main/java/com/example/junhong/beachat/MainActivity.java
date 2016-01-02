package com.example.junhong.beachat;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

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
    private ListAdapter lv_adapter;
    private final int STOP = 10;
    private static final ScheduledExecutorService delayed_work = Executors.newSingleThreadScheduledExecutor();

    private String TAG_UUID = "uuid";
    private String TAG_MAJOR = "major";
    private String TAG_MINOR = "minor";
    private String TAG_RSSI = "rssi";
    private String TAG_TXPW = "tx_pw";
    private String TAG_PROXIMITY = "proximity";

    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lv = (ListView)findViewById(R.id.near_beacon_list);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               /* //Item click sample code for sending email
                HashMap<String, String> hash = (HashMap<String, String>)lv.getItemAtPosition(position);
                String email_addr = hash.get("email");
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("message/rfc822");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{(email_addr)});

                try {
                    getActivity().startActivity(emailIntent);
                } catch(android.content.ActivityNotFoundException ex){
                    Toast.makeText(getActivity(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }*/
            }
        });

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);

        beaconList = new ArrayList<HashMap<String, String>>();
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
                beaconList.clear();
                try {
                    final Region myRegion = new Region("myRangingUniqueId", null, null, null);
                    beaconManager.startRangingBeaconsInRegion(myRegion);
                    Runnable stop_scan = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Log.i(TAG, "stop the ranging");
                                beaconManager.stopRangingBeaconsInRegion(myRegion);
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
                if(collection.size() > 0){
                    Log.i(TAG, "collection.size() : " + collection.size());  //UUID
                    for(Beacon near_beacon : collection){
                        Log.i(TAG, "UUID : " + near_beacon.getId1());  //UUID
                        Log.i(TAG, "MAJOR : " + near_beacon.getId2()); //MAJOR
                        Log.i(TAG, "MINOR : " + near_beacon.getId3()); //MINOR
                        Log.i(TAG, "Proximity : " + near_beacon.getDistance());    //PROXIMITY
                        Log.i(TAG, "TX_PW : " + near_beacon.getTxPower()); //TX_PW

                        String uuid = near_beacon.getId1().toString();
                        String major = near_beacon.getId2().toString();
                        String minor = near_beacon.getId3().toString();
                        String proximity = String.valueOf(near_beacon.getDistance());
                        String tx_pw = String.valueOf(near_beacon.getTxPower());
                        String rssi = String.valueOf(near_beacon.getRssi());

                        HashMap<String, String> beacon = new HashMap<String,String>();

                        beacon.put(TAG_UUID, uuid);
                        beacon.put(TAG_MAJOR, major);
                        beacon.put(TAG_MINOR, minor);
                        beacon.put(TAG_PROXIMITY, proximity);
                        beacon.put(TAG_TXPW, tx_pw);
                        beacon.put(TAG_RSSI, rssi);

                        beaconList.add(beacon);
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateList();
                    }
                });
            }
        });
    }

    public void updateList(){
        lv_adapter = new SimpleAdapter(getApplicationContext(), beaconList, R.layout.beacon_item_list,
                new String[]{TAG_UUID, TAG_MAJOR, TAG_MINOR, TAG_RSSI, TAG_TXPW, TAG_PROXIMITY},
                new int[] {R.id.uuid_content, R.id.major_content, R.id.minor_content, R.id.rssi_content, R.id.tx_pw_content, R.id.proximity_content});

        lv.setAdapter(lv_adapter);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        beaconManager.unbind(this);
    }
}
