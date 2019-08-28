package rssirecording.rssirecording;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.TimerTask;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Node;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.TreeMap;

import static java.lang.Math.pow;
import static java.lang.Thread.sleep;


public class MainActivity extends AppCompatActivity implements BeaconConsumer {
//    turn on bluetooth
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    protected final String Tag = "BeaconSearch";
    private HashMap<String,Integer> map = new HashMap<String,Integer>();
    private HashMap<String,Double> rssimap = new HashMap<String,Double>();
    private HashMap<String,String> Namemap = new HashMap<String,String>();
    private ArrayList<Double> list = new ArrayList<Double>();
    private double Standard_Dif;
    int max = 0;
//    flash screen
    private static Handler mHandler;
//    time when receive beacon
    private DateFormat df = new SimpleDateFormat("h:mm:ss.SSS");
//    UI text
    private TextView showtxt,showlocation;
    private ScrollView scrollView;
    private String researchdata,get_location="";
    private Switch ana_switch;
    private Button stop;
    private int i = 0;
    private String UUID;
    private String Name;
    private long startT;
    private long endT;
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
    private Boolean testcolorchangemsg = true,temp_msg = true;
    private Queue<List<String>> data_queue = new LinkedList<>();
    private ana_singal as = new ana_singal();
    private UUIDtoID trtoid = new UUIDtoID();
    private boolean ana_switch_tmp;
    private static JSONArray jarray = new JSONArray();
    private static final String n_value = "n";
    private static final String id = "id";
    private static final String R0 = "R0";
    private static final String name = "name";
    private static final String parameter = "parameter";
    private static final String install_hight = "install_hight";
    private static final String a_value = "a";
    private static final String b_value = "b";
    private static final String c_value = "c";
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
        ana_switch = (Switch) findViewById(R.id.switch1);
        stop = (Button) findViewById(R.id.stop);
        mHandler = new Handler(); //UI text flash

        try {
            getcountthresholdSetup();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.unbind(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-15,i:16-19,i:20-23,p:24-24"));

        //setBeaconLayout("m:2-3=0215,i:4-19,i:20-23,i:24-27,p:28-28"));
        // Detect the Eddystone main identifier (UID) frame:
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19"));

        // Detect the Eddystone telemetry (TLM) frame:
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("x,s:0-1=feaa,m:2-2=20,d:3-3,d:4-5,d:6-7,d:8-11,d:12-15"));

        // Detect the Eddystone URL frame:
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-20"));

        //beaconManager.setForegroundScanPeriod(ONE_SECOND);
        //beaconManager.setForegroundBetweenScanPeriod(2*ONE_SECOND);


        beaconManager.setForegroundScanPeriod(50);
        beaconManager.setForegroundBetweenScanPeriod(0);

        beaconManager.removeAllMonitorNotifiers();
        //beaconManager.removeAllRangeNotifiers();

        // Get the details for all the beacons we encounter.
        region = new Region("justGiveMeEverything", null, null, null);
        bluetoothManager = (BluetoothManager)
                getSystemService(Context.BLUETOOTH_SERVICE);
        //ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 1001);

        Notification.Builder builder = new Notification.Builder(this);
        //builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentTitle("Scanning for Beacons");
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(pendingIntent);
        beaconManager.enableForegroundServiceScanning(builder.build(), 456);
        beaconManager.setEnableScheduledScanJobs(false);
        beaconManager.setBackgroundBetweenScanPeriod(0);
        beaconManager.setBackgroundScanPeriod(1100);
        beaconManager.bind(MainActivity.this);

        /*
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
        beaconManager.unbind(this);
//        beaconManager.bind(this);
*/
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.removeAllMonitorNotifiers();
        beaconManager.removeAllRangeNotifiers();
        beaconManager.unbind(this);
    }
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
                    //showtxt.append(researchdata+"\n");
                    showlocation.setText("Now at :"+trtoid.trUUID(get_location));
                    Log.i("Beacon UUID", trtoid.trUUID(get_location).toString());
                    if (ana_switch.isChecked()) {
                        wrtieFileOnInternalStorage(filenamedefine.getText() + "_loc.txt",
                                "write location" + trtoid.trUUID(get_location));
                        if (testcolorchangemsg != temp_msg) {
                            temp_msg = testcolorchangemsg;
                            if (showlocation.getCurrentTextColor() == Color.BLUE)
                                showlocation.setTextColor(Color.RED);
                            else showlocation.setTextColor(Color.BLUE);
                        }
                    }
                    scrollView.fullScroll(View.FOCUS_DOWN);

                    /*
                    i++;
                    if(i>100) {
                        showtxt.setText("");
                        i=0;
                    }
                    */

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
        endT = System.currentTimeMillis();
        String date = df.format(Calendar.getInstance().getTime());
        researchdata = beacondata[1]+" "+beacondata[2]+"\t"+date+"\t"+beacondata[3];
        Log.i("researchdata",beacondata[1]+" "+beacondata[2]+"\t"+date+"\t"+beacondata[3]);
        /***************************************************/


        UUID = beacondata[1]+beacondata[2];
        Name = UUID;
        UUID = trtoid.ptr(UUID);
        String rssi = beacondata[3];

        if(map.containsKey(UUID)){
            map.put(UUID,map.get(UUID)+1);
            if(map.get(UUID) > max) max = map.get(UUID);
            Log.i("max",max+"");
            //Log.i("max value",map.get(max).toString());
        }else{
            map.put(UUID,1);
        }

        if(rssimap.containsKey(UUID)){
            rssimap.put(UUID,rssimap.get(UUID)+Double.parseDouble(rssi));
        }else{
            rssimap.put(UUID,Double.parseDouble(rssi));
        }

        if(Namemap.containsKey(UUID)){
            Namemap.put(UUID,Name);
        }else{
            Namemap.put(UUID,Name);
        }

       // Log.i("map UUID",map.get(UUID).toString());





        /***************************************************/
        wrtieFileOnInternalStorage(filenamedefine.getText()+".txt",researchdata);
        List<String> data_list = Arrays.asList(beacondata[1].concat(beacondata[2]),beacondata[3]);
        if (ana_switch.isChecked()) {
            data_queue.offer(data_list);
//        if (data_queue.size() > 10){
//            data_queue.poll();
//        }
//        get_location = as.ana_singal_1(data_queue);
            if (data_queue.size() == 10) {
//            get_location = as.ana_singal_1(data_queue);
                get_location = as.ana_singal_2(data_queue, 5);
                testcolorchangemsg = !testcolorchangemsg;
                data_queue.clear();
            }
        }
        Message msg = new Message();
        msg.what = 1;
        mHandler2.sendMessage(msg);
//        Log.i("AAA","beacon:"+researchdata);
        Log.e("endTime",endT+"");
        Log.e("startTime",startT+"");
        if ((endT-startT)>10000){
            startT = System.currentTimeMillis();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    stop.performClick();
                }
            });

        }
    }
    public void Clickevent(View view){
        switch (view.getId()){
            case R.id.start:
                startT = System.currentTimeMillis();

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
                //Log.i("AAA","start click");
                break;
            case R.id.stop:


                filenamedefine.setClickable(true);
                ScanPeriodUI.setClickable(true);
                SleepTimeUI.setClickable(true);
                beaconManager.unbind(MainActivity.this);

                showtxt.setText("");
                showtxt.append("-----------------------------------------RSSI----------------------------------------- "+'\n');
                RSSIrecord("--------------------------------RSSI---------------------------------- ");
                for (Object key : rssimap.keySet()) {
                    Log.e("*RSSI / count",Double.valueOf(rssimap.get(key))+"");
                    Log.e("RSSI / *count",Double.valueOf(map.get(key))+"");

                    double rssi = Math.abs(Double.valueOf(rssimap.get(key)) / Double.valueOf(map.get(key)));
                    list.add(rssi);
                    Log.e("ListData",list+"");
                }
                Collections.sort(list);
                Log.e("ListData Sort",list+"");
                Log.e("keysize()",list.size()+"");
                for(int i=0; i<list.size(); i++){
                    for (Object key : rssimap.keySet()) {
                      //  Log.e("(2)*RSSI / count",Double.valueOf(rssimap.get(key))+"");
                        //Log.e("(2)RSSI / *count",Double.valueOf(map.get(key))+"");
                        Log.i("test ", "key =" + key + "rssimap" + Double.valueOf(rssimap.get(key)) + "map = " + Double.valueOf(map.get(key)));
                        Log.e("rssimap / count  = ", String.valueOf(Math.abs(Double.valueOf(rssimap.get(key)))+"   /   "+ Double.valueOf(map.get(key))) +" = " +Math.abs(Double.valueOf(rssimap.get(key)) / Double.valueOf(map.get(key))));
                        Log.e("list.get(i)", String.valueOf(list.get(i)));
                        if(Math.abs(Double.valueOf(rssimap.get(key)) / Double.valueOf(map.get(key))) == list.get(i)){
                            double s;
                            s = countThreshold(Namemap.get(key));
                            showtxt.append("UUID =  " + key + " RSSI  = " + String.format("%.2f ", Double.valueOf(rssimap.get(key)) / Double.valueOf(map.get(key))) + " Threshold = " + s + '\n');
                            RSSIrecord("UUID =  " + key + " RSSI  = " + String.format("%.2f ", Double.valueOf(rssimap.get(key)) / Double.valueOf(map.get(key))));
                        }
                    }
                }
                showtxt.append(" "+'\n');
                showtxt.append("--------------------------------Paclage Count---------------------------------- "+'\n');
                RSSIrecord("--------------------------------Paclage Count---------------------------------- ");
                for(int i=max; i >= 0; i--) {
                    for(Object key: map.keySet()){
                        if (map.get(key) == i) {
                            showtxt.append("UUID =  " + key + " count  = " + map.get(key).toString()+'\n');
                            RSSIrecord("UUID =  " + key + " count  = " + map.get(key).toString());
                            Log.i("UUID", key+"");
                            Log.i("count",map.get(key)+"");
                        }
                    }
                }
                showtxt.append(" "+'\n');
                showtxt.append(" "+'\n');
                list.clear();
                rssimap.clear();
                map.clear();
                Namemap.clear();
                max = 0;
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

    private Double countStandard_Deviation(ArrayList<Double> list) {
        double count=0,num=0,avg=0,standard=0,base=0;
        for (double i:list){
            count += i;
            num ++;
        }
        avg = count/num;
        for (double i:list){
            base = (float) Math.pow(i-avg,2);
            standard = standard + base;
        }
        standard = (float) Math.sqrt((standard/num));


        return standard;
    }

    public void wrtieFileOnInternalStorage(String sFileName, String sBody){
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
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

    public void RSSIrecord(String text)
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss - ");
        Date date = new Date(System.currentTimeMillis());
        simpleDateFormat.format(date);
        File logFile = new File("sdcard/RSSIrecord.txt");
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            Writer buf = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile,true),"UTF-8"));
            buf.append( simpleDateFormat.format(date).toString());
            buf.append(text + "\n");
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public void getcountthresholdSetup() throws JSONException {
        List<String> Idlist = new ArrayList<>();
        jarray = ReadJsonFile(this);

        for (int i = 0; i < jarray.length(); i++) {
            JSONObject tmp_jobject = jarray.getJSONObject(i);
            Idlist.add(tmp_jobject.getString(this.id));
        }

        Log.i("json", "jsonArray = " + jarray);


    }

    public double countThreshold(String id){
        double threshold = 0.0;
        double a, b, c,range;
        a = get_a(id);
        b = get_b(id);
        c = get_c(id);
        range = get_parameter(id);
        threshold = (a*pow(3+range,2) + b* (3 + range) + c);
        return threshold;
    }

    public double get_a(String s){
        for (int i=0; i < jarray.length(); i ++){
            try {
                JSONObject tmp_jobject = jarray.getJSONObject(i);
                if(tmp_jobject.getString(this.id).equals(s)){
                    return tmp_jobject.getDouble(this.a_value);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }
    public double get_b(String s){
        for (int i=0; i < jarray.length(); i ++){
            try {
                JSONObject tmp_jobject = jarray.getJSONObject(i);
                if(tmp_jobject.getString(this.id).equals(s)){
                    return tmp_jobject.getDouble(this.b_value);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }
    public double get_c(String s){
        for (int i=0; i < jarray.length(); i ++){
            try {
                JSONObject tmp_jobject = jarray.getJSONObject(i);
                if(tmp_jobject.getString(this.id).equals(s)){
                    return tmp_jobject.getDouble(this.c_value);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }
    public double get_parameter(String s){
        for (int i=0; i < jarray.length(); i ++){
            try {
                JSONObject tmp_jobject = jarray.getJSONObject(i);
                if(tmp_jobject.getString(this.id).equals(s)){
                    return tmp_jobject.getDouble(this.parameter);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }
    public JSONArray ReadJsonFile(Context context) {
        JSONArray jarray = null;
        AssetManager assetManager = context.getAssets();
        try {
            InputStream is = assetManager.open("DeviceParamation.json");
            int tmp_size = is.available();
            byte[] buffer = new byte[tmp_size];
            is.read(buffer);
            is.close();
            String jsonText = new String(buffer, "UTF-8");
            jarray = new JSONArray(jsonText);
            Log.i("JSON","load json success");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jarray;
    }


}
