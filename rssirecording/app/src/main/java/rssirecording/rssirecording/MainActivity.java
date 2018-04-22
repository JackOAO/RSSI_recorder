package rssirecording.rssirecording;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class MainActivity extends AppCompatActivity implements BeaconConsumer {
//    turn on bluetooth
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    protected final String Tag = "BeaconSearch";
//    Thread for handling Lbeacon ID while in a navigation tour
    Thread threadForHandleLbeaconID;
//    flash screen
    private static Handler mHandler;
//    number is not correct
    private static final long SCAN_PERIOD = 1000;
//    time when receive beacon
    private DateFormat df = new SimpleDateFormat("h:mm:ss.SSS");
//    UI text
    private TextView showtxt,showlocation;
    private ScrollView scrollView;
    private String researchdata,get_location;
    private int i = 0;
//    write out data
    private File file;
    private EditText filenamedefine,ScanPeriodUI,SleepTimeUI;
    private int write_location_index;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};
//    Beacon manager for ranging Lbeaon signal
    private BeaconManager beaconManager;
    private Region region;
    private int ScanPeriod = 1000,SleepTime = 2000;
    private Queue<List<String>> data_queue = new LinkedList<>();
    private ana_singal as = new ana_singal();
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        init UI objects
        showtxt = (TextView)findViewById(R.id.textView1);
        showlocation = (TextView)findViewById(R.id.locationtext);
        scrollView = (ScrollView) findViewById(R.id.scrollview1);
        filenamedefine = (EditText) findViewById(R.id.editText);
        ScanPeriodUI = (EditText) findViewById(R.id.editText2);
        SleepTimeUI = (EditText) findViewById(R.id.editText3);
        mHandler = new Handler(); //UI text flash
//        Beacon manager setup
        beaconManager =  BeaconManager.getInstanceForApplication(this);
//        Detect the LBeacon frame:
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-15,i:16-19,i:20-23,p:24-24"));

//        setBeaconLayout("m:2-3=0215,i:4-19,i:20-23,i:24-27,p:28-28"));
//        Detect the Eddystone main identifier (UID) frame:
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19"));

//         Detect the Eddystone telemetry (TLM) frame:
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("x,s:0-1=feaa,m:2-2=20,d:3-3,d:4-5,d:6-7,d:8-11,d:12-15"));

//         Detect the Eddystone URL frame:
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-20"));

        beaconManager.setForegroundScanPeriod(ScanPeriod);
        beaconManager.setForegroundBetweenScanPeriod(SleepTime);
        region = new Region("justGiveMeEverything", null, null, null);
        bluetoothManager = (BluetoothManager)
                getSystemService(Context.BLUETOOTH_SERVICE);
        ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 1001);
//        beaconManager.unbind(this);
        beaconManager.bind(this);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }
    @Override
    public void onBeaconServiceConnect() {
//Start scanning for Lbeacon signal
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                Log.i("AAA","Beacon Size:"+beacons.size());
                if (beacons.size() > 0) {
                    Iterator<Beacon> beaconIterator = beacons.iterator();
                    while (beaconIterator.hasNext()) {
                        Beacon beacon = beaconIterator.next();
                        logBeaconData(beacon);
                    }
                }
            }

        });
        try {
            beaconManager.startRangingBeaconsInRegion(
                    new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("HandlerLeak")
    private Handler mHandler2 = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    showtxt.append(researchdata+"\n");
                    showlocation.setText("Now at :"+get_location);
                    i++;
                    if(i>100) {
                        showtxt.setText("");
                        i=0;
                    }
                    break;
                default:
                    break;
            }
        }
    };

    //    Parser Beacon data
    private void logBeaconData(Beacon beacon) {
        String[] beacondata = new String[]{
                beacon.getId1().toString(),
                beacon.getId2().toString(),
                beacon.getId3().toString(),
                String.valueOf(beacon.getRssi()),
                String.valueOf(beacon.getDistance()),
                String.valueOf(beacon.getBeaconTypeCode()),
                String.valueOf(beacon.getIdentifiers())

        };
        String date = df.format(Calendar.getInstance().getTime());
        researchdata = beacondata[1]+" "+beacondata[2]+"\t"+date+"\t"+beacondata[3];
//        wrtieFileOnInternalStorage(filenamedefine.getText()+".txt",researchdata);
        List<String> data_list = Arrays.asList(beacondata[1].concat(beacondata[2]),beacondata[3]);
        data_queue.offer(data_list);
        if (data_queue.size() > 10){
            data_queue.poll();
        }
//        List tmpQ = new ArrayList(data_queue);
        get_location = as.ana_singal_1(data_queue);
//        as.ana_singal_2(data_queue);
//        Log.i("Queue4", tmpQ.toString());
        Message msg = new Message();
        msg.what = 1;
        mHandler2.sendMessage(msg);
            Log.i("AAA","beacon:"+researchdata);
    }
//    output file

    public void Clickevent(View view){
//    Toast.makeText(this,
//            "Button Clicked", Toast.LENGTH_LONG).show();
        switch (view.getId()){
            case R.id.start:
                filenamedefine.setClickable(false);
                ScanPeriodUI.setClickable(false);
                SleepTimeUI.setClickable(false);
                data_queue.clear();
                ScanPeriod = Integer.valueOf(ScanPeriodUI.getText().toString());
                SleepTime = Integer.valueOf(SleepTimeUI.getText().toString());
                beaconManager.setForegroundScanPeriod(ScanPeriod);
                beaconManager.setForegroundBetweenScanPeriod(SleepTime);
                showtxt.setText("");
                write_location_index = 0;
                beaconManager.bind(MainActivity.this);
                Log.i("AAA","start click");
                break;
            case R.id.stop:
                filenamedefine.setClickable(true);
                ScanPeriodUI.setClickable(true);
                SleepTimeUI.setClickable(true);
                beaconManager.unbind(MainActivity.this);
                break;
            case R.id.range_A:
                wrtieFileOnInternalStorage(filenamedefine.getText()+".txt",
                        "write range A");
                showtxt.append("write range A\n");
                break;
            case R.id.range_B:
                wrtieFileOnInternalStorage(filenamedefine.getText()+".txt",
                        "write range B");
                showtxt.append("write range B\n");
                break;
            case R.id.location:
                wrtieFileOnInternalStorage(filenamedefine.getText()+".txt",
                        "write location"+write_location_index);
                ++write_location_index;
                showtxt.append("write location\n");
                break;
            }
    }
    public void wrtieFileOnInternalStorage(String sFileName, String sBody){
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        Log.i("CCC",path.toString());
        file = new File(path,sFileName);
        BufferedWriter buf;
        if(!file.exists()){
            try
            {
                file.createNewFile();
                file.setExecutable(true,false);
                buf = new BufferedWriter(new FileWriter(file, true));
                buf.append("ScanPeriod:"+ScanPeriod+"\tSleepTime:"+SleepTime);
                buf.newLine();
                buf.close();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try{
            buf = new BufferedWriter(new FileWriter(file, true));
            buf.append(sBody);
            scrollView.fullScroll(View.FOCUS_DOWN);
            buf.newLine();
            buf.close();
            Log.d(Tag, "success"+file.getAbsolutePath());
        }catch (Exception e){
            Log.d(Tag, "fail"+file.getAbsolutePath());
            e.printStackTrace();
        }
    }
    public String bytesToHex(byte[] bytes) {

        char[] hexArray = "0123456789ABCDEF".toCharArray();

        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    public double calculateAccuracy(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0;
        }
        double ratio = rssi * 1.0 / txPower;

        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
            return accuracy;
        }
    }
}
