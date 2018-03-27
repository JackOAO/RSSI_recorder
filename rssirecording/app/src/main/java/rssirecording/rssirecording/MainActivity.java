package rssirecording.rssirecording;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import java.util.Collection;
import java.util.Iterator;


public class MainActivity extends AppCompatActivity implements BeaconConsumer {
//    turn on bluetooth
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    protected final String Tag = "BeaconSearch";
//    Thread for handling Lbeacon ID while in a navigation tour
    Thread threadForHandleLbeaconID;
//    flash screen
    private static Handler mHandler;
//    time when receive beacon
    private DateFormat df = new SimpleDateFormat("h:mm:ss.SSS");
//    UI text
    private TextView showtxt;
    private ScrollView scrollView;
    private String researchdata;
    private int i = 0;
//    write out data
    private File file;
    private EditText filenamedefine;
    private int write_location_index;
    String currentLBeaconID = "EmptyString";
//    button
    private Button startB;
    private Button stopB;
    private Button locationB;
//    Beacon manager for ranging Lbeaon signal
    private BeaconManager beaconManager;
    private Region region;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        init UI objects
        showtxt = (TextView)findViewById(R.id.textView1);
        scrollView = (ScrollView) findViewById(R.id.scrollview1);
        filenamedefine = (EditText) findViewById(R.id.editText);
        startB = (Button) findViewById(R.id.start);
        stopB = (Button) findViewById(R.id.stop);
        locationB = (Button) findViewById(R.id.location);
        startB.setOnClickListener(ClickIntHere);
        stopB.setOnClickListener(ClickIntHere);
        locationB.setOnClickListener(ClickIntHere);
//        handler
        mHandler = new Handler(); //UI text flash

//        Beacon manager setup
        beaconManager =  BeaconManager.getInstanceForApplication(this);
//        beaconManager.bind(this);
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

        beaconManager.setForegroundScanPeriod(1000);
        beaconManager.setForegroundBetweenScanPeriod(2000);


        //create a thread to do the matching of navigation path and current location
        threadForHandleLbeaconID = new Thread();
        threadForHandleLbeaconID.start();

        region = new Region("justGiveMeEverything", null, null, null);

        bluetoothManager = (BluetoothManager)
                getSystemService(Context.BLUETOOTH_SERVICE);
        /*
//        暫時註解以前硬體驗證
        mBluetoothAdapter = bluetoothManager.getAdapter();
//        檢查手機硬體是否為BLE裝置
        if (!getPackageManager().hasSystemFeature
                (PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "硬體不支援", Toast.LENGTH_SHORT).show();
            finish();
        }
//         檢查手機是否開啟藍芽裝置
        if (mBluetoothAdapter == null)
            Toast.makeText(this, "Your device doesnt support Bluetooth",
                    Toast.LENGTH_LONG).show();
        else if (!mBluetoothAdapter.isEnabled()) {
            Intent BtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(BtIntent, 0);
            Toast.makeText(this, "Turning on Bluetooth", Toast.LENGTH_LONG).show();
        }
        */
        ActivityCompat.requestPermissions(
                this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
        beaconManager.bind(this);
    }
    private View.OnClickListener ClickIntHere = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == startB.getId()){
                filenamedefine.setClickable(false);
                showtxt.setText("");
                beaconManager.bind(MainActivity.this);
                Log.i("AAA","start click");
//                scanLeDevice(true);
            }
            else if(v.getId() == stopB.getId()){
                filenamedefine.setClickable(true);
//                scanLeDevice(false);
                beaconManager.unbind(MainActivity.this);
            }
            else if(v.getId() == locationB.getId()){
                ++write_location_index;
                wrtieFileOnInternalStorage(filenamedefine.getText()+".txt",
                        "write location"+write_location_index);
                showtxt.setText("write location");
            }

        }
    };
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
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId",
                    null, null, null));
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
                    showtxt.setText(showtxt.getText()+researchdata+"\n");
                    i++;
                    if(i>30) {
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
                    beacon.getId3().toString()
            };
            Log.i("AAA",
                    "beacon1:"+beacondata[0]+
                    "\tbeacon2:"+beacondata[1]+
                    "\tbeacon3:"+beacondata[2]);
//            Log.i("AAA","Recieved ID: "+CConvX.concat(CConvY)+" Length: "+CConvX.concat(CConvY).length());
    }
//    output file
    public void wrtieFileOnInternalStorage(String sFileName, String sBody){
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        file = new File(path,sFileName);
        if(!file.exists()){
            try
            {
                file.createNewFile();
                file.setExecutable(true,false);
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try{
            BufferedWriter buf = new BufferedWriter(new FileWriter(file, true));
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
