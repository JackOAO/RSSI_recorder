package rssirecording.rssirecording;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.Notification;
import android.os.Environment;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import lecho.lib.hellocharts.view.ColumnChartView;


public class MainActivity extends AppCompatActivity {
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    protected final String Tag = "BeaconSearch";
    private Handler mHandler;
    private static final long SCAN_PERIOD = 9999999;
    private DateFormat df = new SimpleDateFormat("h:mm:ss");
    private TextView showtxt;
    private ScrollView scrollView;
    private String researchdata;
    private File file;
    private EditText filenamedefine;
    private Button startB;
    private Button stopB;
    private Button locationB;
//    private ColumnChartView mchart;
    private int i = 0;
    private int write_location_index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showtxt = (TextView)findViewById(R.id.textView1);
        scrollView = (ScrollView) findViewById(R.id.scrollview1);
        mHandler = new Handler();
        bluetoothManager = (BluetoothManager)
                getSystemService(Context.BLUETOOTH_SERVICE);
        filenamedefine = (EditText) findViewById(R.id.editText);
        startB = (Button) findViewById(R.id.start);
        stopB = (Button) findViewById(R.id.stop);
        locationB = (Button) findViewById(R.id.location);
        startB.setOnClickListener(ClickIntHere);
        stopB.setOnClickListener(ClickIntHere);
        locationB.setOnClickListener(ClickIntHere);
        mBluetoothAdapter = bluetoothManager.getAdapter();
//        mchart = (ColumnChartView) findViewById(R.id.chart);
        // 檢查手機硬體是否為BLE裝置
        if (!getPackageManager().hasSystemFeature
                (PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "硬體不支援", Toast.LENGTH_SHORT).show();
            finish();
        }
        // 檢查手機是否開啟藍芽裝置
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "請開啟藍芽裝置", Toast.LENGTH_SHORT).show();
            Intent enableBluetooth = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, REQUEST_ENABLE_BT);

        } else {
//            scanLeDevice(true);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
    }
    private View.OnClickListener ClickIntHere = new View.OnClickListener() {
        @Override
        //按下Button事件時會進入這個 function
        public void onClick(View v) {
            if(v.getId() == startB.getId()){
                //判斷如果是 Button_1 按下時會進入這
                //TextView_1 顯示 Click Button_1
                Log.i("myinfotage","clicking");
                filenamedefine.setClickable(false);

                showtxt.setText("");
                scanLeDevice(true);
            }
            else if(v.getId() == stopB.getId()){
                //判斷如果是 Button_2 按下時會進入這
                //TextView_1 顯示 Click Button_2
                filenamedefine.setClickable(true);
                scanLeDevice(false);
            }
            else if(v.getId() == locationB.getId()){
                ++write_location_index;
                wrtieFileOnInternalStorage(filenamedefine.getText()+".txt",
                        "write location"+write_location_index);
                showtxt.setText("write location");
            }
        }
    };
    // 掃描藍芽裝置
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    Log.i("myinfotage","stopLescan");
                }
            }, SCAN_PERIOD);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            Log.i("myinfotage","startLescan in enable");
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            Log.i("myinfotage","stopLescan");
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
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
//                public FileOutputStream outputStream;

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi,
                                     final byte[] scanRecord) {
                    int startByte = 2;
                    boolean patternFound = false;

                    // 尋找ibeacon
                    // 先依序尋找第2到第8陣列的元素
                    while (startByte <= 5) {
                        // Identifies an iBeacon
                        if (((int) scanRecord[startByte + 2] & 0xff) == 0x02 &&
                                // Identifies correct data length
                                ((int) scanRecord[startByte + 3] & 0xff) == 0x15) {
                            patternFound = true;
                            break;
                        }
                        startByte++;

                    }
                    // 如果找到了的话
                    if (patternFound) {
//                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        // 轉換16進制
                        byte[] uuidBytes = new byte[16];
                        // 來源、起始位置
                        System.arraycopy(scanRecord, startByte + 4, uuidBytes, 0, 16);
                        String hexString = bytesToHex(uuidBytes);

                        // UUID
                        String uuid = hexString.substring(0, 8) + "-"
                                + hexString.substring(8, 12) + "-"
                                + hexString.substring(12, 16) + "-"
                                + hexString.substring(16, 20) + "-"
                                + hexString.substring(20, 32);
                        String mac = device.getAddress();

                        // Major
                        int major = (scanRecord[startByte + 20] & 0xff) * 0x100
                                + (scanRecord[startByte + 21] & 0xff);
                        // Minor
                        int minor = (scanRecord[startByte + 22] & 0xff) * 0x100
                                + (scanRecord[startByte + 23] & 0xff);
                        // txPower
                        int txPower = (scanRecord[startByte + 24]);
                        double distance = calculateAccuracy(txPower, rssi);
                        String date = df.format(Calendar.getInstance().getTime());
                        researchdata = uuid.toString() + "\t" + date + "\t" + rssi;
                        Message msg = new Message();
                        msg.what = 1;
                        mHandler2.sendMessage(msg);
                        Log.d(Tag, "Mac：" + mac
                                + " \nUUID：" + uuid + "\nMajor：" + major + "\nMinor："
                                + minor + "\nTxPower：" + txPower + "\nrssi：" + rssi);
//                        Log.d(Tag, "distance：" + calculateAccuracy(txPower, rssi));
//                        wrtieFileOnInternalStorage("//storage//emulated//0/Download//rssidata.txt",researchdata);
                        wrtieFileOnInternalStorage(filenamedefine.getText()+".txt",researchdata);

                    }
                }
            };

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

}
