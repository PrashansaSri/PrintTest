package in.acural.printtest;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.jxit.jxitbluetoothprintersdk1_6.Jxit_cpcl;
import com.jxit.jxitbluetoothprintersdk1_6.Jxit_esc;
import com.jxit.jxitbluetoothprintersdk1_6.PrintPP_CPCL;

import java.io.BufferedInputStream;
import java.io.IOException;

import in.acural.printtest.activity.PictureActivity;
import in.acural.printtest.activity.SettingActivity;
import in.acural.printtest.activity.UsbPrinterActivity;
import in.acural.printtest.activity.Welcome2Activity;
import in.acural.printtest.fragment.FragmentBill;
import in.acural.printtest.fragment.FragmentBluetooth;
import in.acural.printtest.fragment.FragmentConfig;
import in.acural.printtest.fragment.FragmentEsc;
import in.acural.printtest.fragment.FragmentLabel;
import in.acural.printtest.util.PrintUtils;

import static java.lang.Math.sin;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, FragmentBluetooth.FragmentInteractionBluetooth,
        FragmentEsc.FragmentInteractionEsc, FragmentBill.FragmentInteractionBill, FragmentLabel.FragmentInteractionLabel {
    private static final int REQUEST_CAMERA=0;
    private static final int CONNECTED=1;
    private static final int CONNECT_FAILD=2;
    private static final int DISCONNECT=3;
    private static final int PRINT_FINISHED=4;
    private static final int PRINT_FAILD=5;

    private boolean FirstOpen=false;

    private Toolbar toolBarMain;
    private TextView tvTitle;
    private ImageButton btnRight;
    private FrameLayout mFLEsc, mFLBill, mFLLebel, mFLReadInfo;
    private ImageView mIVEsc, mIVBill, mIVLebel, mIVReadInfo;

    private FragmentEsc mFragmentEsc;
    private FragmentBluetooth mFragmentBluetooth;
    private FragmentLabel mFragmentLabel;

    private String deviceName;
    public static String btDeviceAddress=null;
    private boolean isFragmentBluetooth=false;

    private BluetoothAdapter mBtAdapter;
    private Jxit_esc mJxit_esc;
    private PrintPP_CPCL mCPCL;
    private Jxit_cpcl mJxit_cpcl;

    /**
     * onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (isFirstOpen()) goGuide();
        init();
    }

    private void init() {
        initView();

        clickEscBtn();
        mBtAdapter=BluetoothAdapter.getDefaultAdapter();
        mJxit_esc=Jxit_esc.getInstance();
        mCPCL=PrintPP_CPCL.getInstance();
        mJxit_cpcl=Jxit_cpcl.getInstance();

        IntentFilter filter=new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        this.registerReceiver(mActivityReceiver, filter);
    }

    /**
     * onResume
     */
    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT > 22) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
            }
        } else {
            if (mBtAdapter.isEnabled()) {
                mBtAdapter.enable();
            }
        }
    }

    /**
     * onRequestPermissionsResult
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        doNext(requestCode, grantResults);
    }

    private void doNext(int requestCode, int[] grantResults) {
        if (requestCode == REQUEST_CAMERA) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,
                        "Please open in app management“camera”access permission！", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * isFirstOpen
     */
    private boolean isFirstOpen() {
        SharedPreferences share=getSharedPreferences("First", MODE_PRIVATE);
        String s=share.getString("FirstEntrance", null);
        return !(FirstOpen || !TextUtils.isEmpty(s));
    }

    /**
     * goGuide
     */
    private void goGuide() {
        Intent intent=new Intent(this, Welcome2Activity.class);
        startActivity(intent);
        SharedPreferences sharedPreference=getSharedPreferences("First", MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreference.edit();
        editor.putString("FirstEntrance", "FirstEntrance");
        editor.apply();
    }

    /**
     * initView
     */
    private void initView() {
        initToolbar();
        initBottomView();
        setmLongclick_ll();
    }

    /**
     * initBottomView
     */
    private void initBottomView() {
        mFLEsc=(FrameLayout) findViewById(R.id.fl_esc);
        mFLBill=(FrameLayout) findViewById(R.id.fl_bill);
        mFLLebel=(FrameLayout) findViewById(R.id.fl_label);
        mFLReadInfo=(FrameLayout) findViewById(R.id.fl_read_info);
        mFLEsc.setOnClickListener(this);
        mFLBill.setOnClickListener(this);
        mFLLebel.setOnClickListener(this);
        mFLReadInfo.setOnClickListener(this);

        mIVEsc=(ImageView) findViewById(R.id.image_esc);
        mIVBill=(ImageView) findViewById(R.id.image_bill);
        mIVLebel=(ImageView) findViewById(R.id.image_label);
        mIVReadInfo=(ImageView) findViewById(R.id.image_read_info);
    }

    /**
     * initToolbar
     */
    private void initToolbar() {
        toolBarMain=(Toolbar) findViewById(R.id.tb_main);
        tvTitle=(TextView) toolBarMain.findViewById(R.id.tv_tb_title);
        btnRight=(ImageButton) toolBarMain.findViewById(R.id.btn_tb_right);
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFragmentBluetooth) {
                    isFragmentBluetooth=true;
                    clickBluetoothBtn();
                }
            }
        });

        setSupportActionBar(toolBarMain);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolBarMain.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, UsbPrinterActivity.class);
                startActivity(intent);

//                toolBarMain.setNavigationIcon(R.drawable.usb1_32);
//                AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
//                adb.setTitle("提示");
//                adb.setMessage("暂时没有可用的USB打印机，请插入USB打印机设备。");
//                adb.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        toolBarMain.setNavigationIcon(R.drawable.bluetooth_24);
//                    }
//                });
//                adb.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        toolBarMain.setNavigationIcon(R.drawable.bluetooth_24);
//                    }
//                });
//                adb.show();
            }
        });
    }

    /**
     * setmLongclick_ll
     */
    private void setmLongclick_ll() {
        RelativeLayout mLongclick_ll=(RelativeLayout) findViewById(R.id.rll_main);
        mLongclick_ll.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intent=new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
                return false;
            }
        });
    }

    /**
     * onClick
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fl_esc:
                clickEscBtn();
                break;
            case R.id.fl_bill:
                clickBillBtn();
                break;
            case R.id.fl_label:
                clickLabelBtn();
                break;
            case R.id.fl_read_info:
                clickReadInfoBtn();
                break;
            default:
                break;
        }
    }

    /**
     * clickDemmoBtn
     */
    private void clickEscBtn() {
        isFragmentBluetooth=false;
        toolBarMain.setNavigationIcon(R.drawable.usb1_32);
        tvTitle.setText(R.string.esc_title);
        btnRight.setVisibility(Button.VISIBLE);

        mFragmentEsc=new FragmentEsc();
        FragmentTransaction fragmentTransaction=MainActivity.this.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_content, mFragmentEsc);
        fragmentTransaction.commit();

        mFLEsc.setSelected(true);
        mIVEsc.setSelected(true);
        mFLBill.setSelected(false);
        mIVBill.setSelected(false);
        mFLLebel.setSelected(false);
        mIVLebel.setSelected(false);
        mFLReadInfo.setSelected(false);
        mIVReadInfo.setSelected(false);
    }

    /**
     * clickProduceBtn
     */
    private void clickBillBtn() {
        isFragmentBluetooth=false;
        toolBarMain.setNavigationIcon(R.drawable.usb1_32);
        tvTitle.setText(R.string.bill_title);
        btnRight.setVisibility(Button.VISIBLE);

        FragmentBill mFragmentBill=new FragmentBill();
        FragmentTransaction fragmentTransaction=MainActivity.this.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_content, mFragmentBill);
        fragmentTransaction.commit();

        mFLEsc.setSelected(false);
        mIVEsc.setSelected(false);
        mFLBill.setSelected(true);
        mIVBill.setSelected(true);
        mFLLebel.setSelected(false);
        mIVLebel.setSelected(false);
        mFLReadInfo.setSelected(false);
        mIVReadInfo.setSelected(false);
    }

    /**
     * clickTestBtn
     */
    private void clickLabelBtn() {
        toolBarMain.setNavigationIcon(R.drawable.usb1_32);
        tvTitle.setText(R.string.label_title);
        btnRight.setVisibility(Button.VISIBLE);

        mFragmentLabel=new FragmentLabel();
        FragmentTransaction fragmentTransaction=MainActivity.this.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_content, mFragmentLabel);
        fragmentTransaction.commit();

        mFLEsc.setSelected(false);
        mIVEsc.setSelected(false);
        mFLBill.setSelected(false);
        mIVBill.setSelected(false);
        mFLLebel.setSelected(true);
        mIVLebel.setSelected(true);
        mFLReadInfo.setSelected(false);
        mIVReadInfo.setSelected(false);
    }

    /**
     * clickReadInfoBtn
     */
    private void clickReadInfoBtn() {
        toolBarMain.setNavigationIcon(R.drawable.usb1_32);
        tvTitle.setText(R.string.read_title);
        btnRight.setVisibility(Button.VISIBLE);

        FragmentConfig mFragmentReadInfo=new FragmentConfig();
        FragmentTransaction fragmentTransaction=MainActivity.this.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_content, mFragmentReadInfo);
        fragmentTransaction.commit();

        mFLEsc.setSelected(false);
        mIVEsc.setSelected(false);
        mFLBill.setSelected(false);
        mIVBill.setSelected(false);
        mFLLebel.setSelected(false);
        mIVLebel.setSelected(false);
        mFLReadInfo.setSelected(true);
        mIVReadInfo.setSelected(true);
    }

    /**
     * clickBluetoothBtn
     */
    private void clickBluetoothBtn() {
        tvTitle.setText(R.string.bluetooth_title);
        mFragmentBluetooth=new FragmentBluetooth();

        FragmentTransaction fragmentTransaction=MainActivity.this.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.frame_content, mFragmentBluetooth, "mFragmentBluetooth");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        mFLEsc.setSelected(false);
        mIVEsc.setSelected(false);
        mFLBill.setSelected(false);
        mIVBill.setSelected(false);
        mFLLebel.setSelected(false);
        mIVLebel.setSelected(false);
        mFLReadInfo.setSelected(false);
        mIVReadInfo.setSelected(false);
    }

    /**
     * BroadcastReceiver
     */
    private BroadcastReceiver mActivityReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            switch (action) {
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    mHandler.sendEmptyMessage(DISCONNECT);
                    break;
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    mHandler.sendEmptyMessage(CONNECTED);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * Handler
     */
    private Handler mHandler=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CONNECTED:
                    btnRight.setImageResource(R.drawable.printer20);
                    break;
                case DISCONNECT:
                    if (btDeviceAddress == null) {
                        btnRight.setImageResource(R.drawable.icon_plus_32);
                    }
                    break;
                case CONNECT_FAILD:
                    Toast.makeText(MainActivity.this, deviceName + "Connect Failed", Toast.LENGTH_SHORT).show();
                    try {
                        if (mJxit_esc.isConnected()) {
                            mJxit_esc.close();
                        }
                        if (mCPCL.isConnected()) {
                            mCPCL.disconnect();
                        }
                        if (mJxit_cpcl.isConnected()) {
                            mJxit_cpcl.disconnect();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mFragmentEsc.mListView.setEnabled(true);
                    mFragmentLabel.mListView.setEnabled(true);
                    break;
                case PRINT_FAILD:
                    Toast.makeText(MainActivity.this, deviceName + "Send Failed", Toast.LENGTH_SHORT).show();
                    try {
                        if (mJxit_esc.isConnected()) {
                            mJxit_esc.close();
                        }
                        if (mCPCL.isConnected()) {
                            mCPCL.disconnect();
                        }
                        if (mJxit_cpcl.isConnected()) {
                            mJxit_cpcl.disconnect();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mFragmentEsc.mListView.setEnabled(true);
                    mFragmentLabel.mListView.setEnabled(true);
                    break;
                case PRINT_FINISHED:
                    try {
                        if (mJxit_esc.isConnected()) {
                            mJxit_esc.close();
                        }
                        if (mCPCL.isConnected()) {
                            mCPCL.disconnect();
                        }
                        if (mJxit_cpcl.isConnected()) {
                            mJxit_cpcl.disconnect();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        mFragmentEsc.mListView.setEnabled(true);
                        mFragmentLabel.mListView.setEnabled(true);
                    } catch (Exception e) {

                    }

                    break;
                default:
                    break;
            }
        }
    };

    /**
     *processBluetooth
     */
    @Override
    public void processBluetooth(String str) {
        switch (str) {
            case "@Close Bluetooth":
                btnRight.setBackgroundResource(R.drawable.bluetooth_24);
                break;
            case "@Exit Bluetooth interface":
                isFragmentBluetooth=false;
                break;
            case "@No camera permissions":
                Toast.makeText(this, "Please open in app management“camera”access permission！", Toast.LENGTH_SHORT).show();
                break;
            case "Wrong MAC address":
                Toast.makeText(this,
                        "Get Bluetooth MAC address error！", Toast.LENGTH_SHORT).show();
                break;
            default:
                btnRight.setBackgroundResource(R.drawable.printer20);

                deviceName=str.substring(0, str.length() - 17);
                btDeviceAddress=str.substring(str.length() - 17);

                FragmentTransaction fragmentTransaction=MainActivity.this.getSupportFragmentManager().beginTransaction();
                mFragmentBluetooth.onDestroy();
                fragmentTransaction.remove(mFragmentBluetooth);
                fragmentTransaction.commit();

                isFragmentBluetooth=false;
                break;
        }
    }

    @Override
    public void processEsc(String str) {
        switch (str) {
            case "@ESC Test":
                if (btDeviceAddress == null) {
                    Toast.makeText(this, "Choose Printer！", Toast.LENGTH_SHORT).show();
                } else {
                    connectPrintEscTest();
                }
                break;
            case "@Print Picture":
                if (btDeviceAddress == null) {
                    Toast.makeText(MainActivity.this, "Choose Printer", Toast.LENGTH_SHORT).show();
                    clickBluetoothBtn();
                } else {
                    try {
                        if (mFragmentBluetooth.mBluetoothDeviceReceiver != null) {
                            unregisterReceiver(mFragmentBluetooth.mBluetoothDeviceReceiver);
                            mFragmentBluetooth.mBluetoothDeviceReceiver=null;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Intent intent=new Intent();
                    intent.setClass(MainActivity.this, PictureActivity.class);
                    intent.putExtra("btDeviceAddress", btDeviceAddress);
                    startActivity(intent);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void processBill(String str) {
        switch (str) {
            case "@Recipt":
                if (btDeviceAddress == null) {
                    Toast.makeText(this, "Choose Printer", Toast.LENGTH_SHORT).show();
                } else {
                    connectPrintCateringBills();
                }
                break;
            case "@Check Details":
                if (btDeviceAddress == null) {
                    Toast.makeText(this, "Choose Printer", Toast.LENGTH_SHORT).show();
                } else {
                    connectPrintPatrolResult();
                }
                break;
            case "@Package List":
                if (btDeviceAddress == null) {
                    Toast.makeText(this, "Choose Printer", Toast.LENGTH_SHORT).show();
                } else {
                    connectPrintGoodsList();
                }
                break;
            default:
                break;
        }
    }


    @Override
    public void processLabel(String str) {
        switch (str) {
            case "@label":
                // Toast.makeText(this, "lebal", Toast.LENGTH_SHORT).show();
                Log.i("processLabel", "Label");
                if (btDeviceAddress == null) {
                    Toast.makeText(this, "Choose Printer", Toast.LENGTH_SHORT).show();
                } else {
                    connectPrintLabel();
                }
                break;
            case "@Waybill":
                Log.i("processLabel", "Waybill");
                if (btDeviceAddress == null) {
                    // Toast.makeText(this,"hhhh",Toast.LENGTH_SHORT).show();
                    Toast.makeText(this, "Choose Printer", Toast.LENGTH_SHORT).show();
                } else {
                    connectPrintLabel2();
                }
                break;
            case "@new cpcl":
                Log.i("processLabel", "new cpcl");
                if (btDeviceAddress == null) {
                    Toast.makeText(this, "Choose Printer", Toast.LENGTH_SHORT).show();
                } else {
                    connectPrintNewLabel();
                }
                break;
            default:
                break;
        }
    }

    private void connectPrintEscTest() {
        if (BluetoothAdapter.checkBluetoothAddress(btDeviceAddress) && btDeviceAddress != null) {
            mFragmentEsc.mListView.setEnabled(false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (mJxit_esc.isConnected()) {
                        mJxit_esc.close();
                    }
                    if (!mJxit_esc.connectDevice(btDeviceAddress)) {
                        mHandler.sendEmptyMessage(CONNECT_FAILD);
                        return;
                    }
                    if (!EscTest()) {
                        mHandler.sendEmptyMessage(PRINT_FAILD);
                    }
                    mHandler.sendEmptyMessageDelayed(PRINT_FINISHED, 10000);
                }
            }).start();
        } else Toast.makeText(MainActivity.this, "Choose Printer", Toast.LENGTH_SHORT).show();
    }


    private boolean EscTest() {
        if (!printPicture()) {
            return false;
        }
        if (!printText()) {
            return false;
        }

//        if(!printText1()){
//            return false;
//        }
//        if(!printText2()){
//            return false;
//        }
//        if(!printTable()){
//            return false;
//        }
        if (!printBarcode1d()) {
            return false;
        }
        if (!printBarcode2d()) {
            return false;
        }
        if (!printCurve()) {
            return false;
        }

//        if(!printCateringBills()){
//            return false;
//        }
//        if(!printControlCommand()){
//            return false;
//        }
        return true;
    }

    private void connectPrintLabel() {
        if (BluetoothAdapter.checkBluetoothAddress(btDeviceAddress) && btDeviceAddress != null) {
            mFragmentLabel.mListView.setEnabled(false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (mCPCL.isConnected()) {
                        mCPCL.disconnect();
                    }
                    if (!mCPCL.connect(btDeviceAddress)) {
                        Log.i("connectint", "connect faild!");
                        mHandler.sendEmptyMessage(CONNECT_FAILD);
                        return;
                    }
                    if (!printLable()) {
                        Log.i("connectint", "connect success!");
                        mHandler.sendEmptyMessage(PRINT_FAILD);
                    }
                    mHandler.sendEmptyMessageDelayed(PRINT_FINISHED, 10000);
                }
            }).start();
        } else Toast.makeText(MainActivity.this, "Choose Printer", Toast.LENGTH_SHORT).show();
    }

    private void connectPrintLabel2() {
        if (BluetoothAdapter.checkBluetoothAddress(btDeviceAddress) && btDeviceAddress != null) {
            mFragmentLabel.mListView.setEnabled(false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (mCPCL.isConnected()) {
                        mCPCL.disconnect();
                    }
                    if (!mCPCL.connect(btDeviceAddress)) {
                        Log.i("connectint", "connect faild!");
                        mHandler.sendEmptyMessage(CONNECT_FAILD);
                        return;
                    }
                    if (!printLable2()) {
                        Log.i("connectint", "connect success!");
                        mHandler.sendEmptyMessage(PRINT_FAILD);
                    }
                    mHandler.sendEmptyMessageDelayed(PRINT_FINISHED, 10000);
                }
            }).start();
        } else Toast.makeText(MainActivity.this, "Choose Printer", Toast.LENGTH_SHORT).show();
    }

    private void connectPrintNewLabel() {
        if (BluetoothAdapter.checkBluetoothAddress(btDeviceAddress) && btDeviceAddress != null) {
            mFragmentLabel.mListView.setEnabled(false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (mJxit_cpcl.isConnected()) {
                        mJxit_cpcl.disconnect();
                    }
                    if (!mJxit_cpcl.connect(btDeviceAddress)) {
                        Log.i("connectint", "connect faild!");
                        mHandler.sendEmptyMessage(CONNECT_FAILD);
                        return;
                    }
                    printNewLable();
                    mHandler.sendEmptyMessageDelayed(PRINT_FINISHED, 10000);
                }
            }).start();
        } else Toast.makeText(MainActivity.this, "Choose Printer", Toast.LENGTH_SHORT).show();
    }

    /**
     *connectPrintCateringBills
     */
    private void connectPrintCateringBills() {
        if (BluetoothAdapter.checkBluetoothAddress(btDeviceAddress) && btDeviceAddress != null) {
            mFragmentEsc.mListView.setEnabled(false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (mJxit_esc.isConnected()) {
                        mJxit_esc.close();
                    }
                    if (!mJxit_esc.connectDevice(btDeviceAddress)) {
                        mHandler.sendEmptyMessage(CONNECT_FAILD);
                        return;
                    }
                    if (!printCateringBills()) {
                        mHandler.sendEmptyMessage(PRINT_FAILD);
                    }
                    mHandler.sendEmptyMessageDelayed(PRINT_FINISHED, 2000);
                }
            }).start();
        } else Toast.makeText(MainActivity.this, "Choose Printer", Toast.LENGTH_SHORT).show();
    }

    /**
     *connectPrintPatrolResult
     */
    private void connectPrintPatrolResult() {
        if (BluetoothAdapter.checkBluetoothAddress(btDeviceAddress) && btDeviceAddress != null) {
            mFragmentEsc.mListView.setEnabled(false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (mJxit_esc.isConnected()) {
                        mJxit_esc.close();
                    }
                    if (!mJxit_esc.connectDevice(btDeviceAddress)) {
                        mHandler.sendEmptyMessage(CONNECT_FAILD);
                        return;
                    }
                    if (!printPatrolResult()) {
                        mHandler.sendEmptyMessage(PRINT_FAILD);
                    }
                    mHandler.sendEmptyMessageDelayed(PRINT_FINISHED, 4500);
                }
            }).start();
        } else Toast.makeText(MainActivity.this, "Choose Printer", Toast.LENGTH_SHORT).show();
    }

    /**
     *connectPrintGoodsList
     */
    private void connectPrintGoodsList() {
        if (BluetoothAdapter.checkBluetoothAddress(btDeviceAddress) && btDeviceAddress != null) {
            mFragmentEsc.mListView.setEnabled(false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (mJxit_esc.isConnected()) {
                        mJxit_esc.close();
                    }
                    if (!mJxit_esc.connectDevice(btDeviceAddress)) {
                        mHandler.sendEmptyMessage(CONNECT_FAILD);
                        return;
                    }
                    if (!printGoodsList()) {
                        mHandler.sendEmptyMessage(PRINT_FAILD);
                        return;
                    }
                    mHandler.sendEmptyMessageDelayed(PRINT_FINISHED, 3500);
                }
            }).start();
        } else Toast.makeText(MainActivity.this, "Choose Printer", Toast.LENGTH_SHORT).show();
    }


    /**
     * printText
     */
    private boolean printText() {
        if (!mJxit_esc.esc_print_text("\n Text alignment\n")) return false;
        if (!mJxit_esc.esc_align(0)) return false;
        if (!mJxit_esc.esc_print_text("Align left\n")) return false;
        if (!mJxit_esc.esc_reset()) return false;
        if (!mJxit_esc.esc_align(1)) return false;
        if (!mJxit_esc.esc_print_text("Centered\n")) return false;
        if (!mJxit_esc.esc_reset()) return false;
        if (!mJxit_esc.esc_align(2)) return false;
        if (!mJxit_esc.esc_print_text("Align right\n")) return false;
        if (!mJxit_esc.esc_reset()) return false;
        if (!mJxit_esc.esc_bold(true)) return false;
        if (!mJxit_esc.esc_print_text("Bold\n")) return false;
        if (!mJxit_esc.esc_reset()) return false;
        if (!mJxit_esc.esc_underline(2)) return false;
        if (!mJxit_esc.esc_print_text("Underline\n")) return false;
        if (!mJxit_esc.esc_reset()) return false;
        if (!mJxit_esc.esc_print_text("Different heights\nsingle\n")) return false;
        if (!mJxit_esc.esc_character_size(22)) return false;
        if (!mJxit_esc.esc_print_text("Double\n")) return false;
        if (!mJxit_esc.esc_character_size(33)) return false;
        if (!mJxit_esc.esc_print_text("Triple\n")) return false;
        if (!mJxit_esc.esc_character_size(44)) return false;
        if (!mJxit_esc.esc_print_text("Fourfold\n")) return false;
        if (!mJxit_esc.esc_reset()) return false;
        if (!mJxit_esc.esc_print_text(
                "Zoom in（Englarge None)\n")) return false;
        if (!mJxit_esc.esc_character_size(22)) return false;
        if (!mJxit_esc.esc_print_text("Zoom in\n（Englarge 2X)\n")) return false;
//        if(!mJxit_esc.esc_character_size(33)) return false;
//        if(!mJxit_esc.esc_print_text("Zoom in\n(Englarge 3X)\n")) return false;
//        if(!mJxit_esc.esc_character_size(44)) return false;
//        if(!mJxit_esc.esc_print_text("Zoom in\n（Englarge 4X)\n")) return false;
        if (!mJxit_esc.esc_reset()) return false;
        if (!mJxit_esc.esc_font(0)) return false;
        if (!mJxit_esc.esc_print_text("Font A\n")) return false;
        if (!mJxit_esc.esc_reset()) return false;
        if (!mJxit_esc.esc_font(1)) return false;
        if (!mJxit_esc.esc_print_text(
                "Font B\n")) return false;
        if (!mJxit_esc.esc_reset()) return false;
        if (!mJxit_esc.esc_rotate(1)) return false;
        if (!mJxit_esc.esc_print_text("Rotate clockwise 90°\n")) return false;
        if (!mJxit_esc.esc_reset()) return false;
        if (!mJxit_esc.esc_rotate(2)) return false;
        if (!mJxit_esc.esc_print_text("Rotate clockwise 180°\n")) return false;
        if (!mJxit_esc.esc_reset()) return false;
        if (!mJxit_esc.esc_rotate(3)) return false;
        if (!mJxit_esc.esc_print_text("Rotate clockwise 270°\n")) return false;
        if (!mJxit_esc.esc_reset()) return false;
        if (!mJxit_esc.esc_black_white_reverse(true)) return false;
        if (!mJxit_esc.esc_print_text(
                "   Black and white reverse  \n")) return false;
        if (!mJxit_esc.esc_reset()) return false;
        return true;
    }

    /**
     * printPicture
     */
    ////// ru
    private boolean printText1() {
        if (!mJxit_esc.esc_align(1)) return false;
        if (!mJxit_esc.esc_print_text("Acural Solutions Pvt. Ltd\n")) return false;
        if (!mJxit_esc.esc_align(1)) return false;
        if (!mJxit_esc.esc_print_text("Delhi\n")) return false;
        return true;

    }

    private boolean printText2() {
        if (!mJxit_esc.esc_print_text(" \n")) return false;
        if (!mJxit_esc.esc_reset()) return false;
        if (!mJxit_esc.esc_print_text(" \n")) return false;
        if (!mJxit_esc.esc_reset()) return false;

        return true;
    }


    private boolean printPicture() {
        BufferedInputStream bis=null;
        try {
            bis=new BufferedInputStream(getAssets().open("aa6.bmp"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bitmap=BitmapFactory.decodeStream(bis);
        if (!mJxit_esc.esc_reset()) return false;
        if (!mJxit_esc.esc_print_formfeed()) return false;
        if (!mJxit_esc.esc_bitmap_mode(1, bitmap)) return false;
        if (!mJxit_esc.esc_reset()) return false;
        if (!mJxit_esc.esc_print_formfeed()) return false;
        return true;
    }

    /**
     * printBarcode1d
     */
    private boolean printBarcode1d() {
        if (!mJxit_esc.esc_reset()) return false;
        if (!mJxit_esc.esc_print_formfeed()) return false;
        if (!mJxit_esc.esc_barcode_1d(0, 1, 3, 80, 8, "123456789012")) return false;
        if (!mJxit_esc.esc_print_formfeed()) return false;
        if (!mJxit_esc.esc_reset()) return false;
        return true;
    }

    /**
     * printBarcode2d
     */
    private boolean printBarcode2d() {
        if (!mJxit_esc.esc_reset()) return false;
        if (!mJxit_esc.esc_print_formfeed()) return false;
        if (!mJxit_esc.esc_print_barcode_2d(8, "http://www.acural.in/")) return false;
        if (!mJxit_esc.esc_print_formfeed()) return false;
        if (!mJxit_esc.esc_reset()) return false;
        return true;
    }

    /**
     * printCurve
     */
    private boolean printCurve() {
        mJxit_esc.esc_reset();
        byte curveBytes[]=new byte[7];
        byte y1, y1s=0;
        byte[] sinBytes=new byte[4];

        curveBytes[0]=0x1D;
        curveBytes[1]=0x27;
        curveBytes[2]=1;
        curveBytes[3]=30;
        curveBytes[4]=0;
        curveBytes[5]=(byte) 230;
        curveBytes[6]=0;
        if (!mJxit_esc.esc_write_bytes(curveBytes)) {
            return false;
        }

        curveBytes[0]=0x1D;
        curveBytes[1]=0x27;
        curveBytes[2]=2;
        curveBytes[3]=(byte) 130;
        curveBytes[4]=0;
        curveBytes[5]=(byte) 130;
        curveBytes[6]=0;

        for (int i=1; i <= 512; i++) {
            y1=(byte) (sin(i * 3.1415926 / 128) * 100 + 130);
            if (i == 1) {
                y1s=y1;
            }
            if (!mJxit_esc.esc_write_bytes(curveBytes)) {
                return false;
            }

            sinBytes[0]=y1s;
            sinBytes[1]=0;
            sinBytes[2]=y1;
            sinBytes[3]=0;

            if (!mJxit_esc.esc_write_bytes(sinBytes)) {
                return false;
            }
            y1s=y1;

        }
        return mJxit_esc.esc_write_bytes(new byte[]{0x0D, 0x0A, 0x0D, 0x0A});
    }

    /**
     * printTable
     */
    private boolean printTable() {
        mJxit_esc.esc_reset();
        String s1="┏━━┳━━━┳━━━┳━━━┓\n";
        String s2="┃No. ┃Name  ┃Sex   ┃Ages  ┃\n┣━━╋━━━╋━━━╋━━━┫\n";
        String s3="┃ 1  ┃anuj  ┃   M  ┃  18  ┃\n┣━━╋━━━╋━━━╋━━━┫\n";
        String s4="┃ 2  ┃ruby  ┃  FM  ┃  17  ┃\n┣━━╋━━━╋━━━╋━━━┫\n";
        String s5="┃ 3  ┃Ajit  ┃   M  ┃  16  ┃\n┗━━┻━━━┻━━━┻━━━┛\n\n\n\n";
        return mJxit_esc.esc_write_bytes(new byte[]{0x1B, 0x33, 0x00, 0x0D, 0x0A}) && mJxit_esc.esc_print_text(s1)
                && mJxit_esc.esc_print_text(s2) && mJxit_esc.esc_print_text(s3) && mJxit_esc.esc_print_text(s4) && mJxit_esc.esc_print_text(s5) && mJxit_esc.esc_reset();
    }


    /**
     * printControlCommand
     */
    private boolean printControlCommand() {
        try {
            mJxit_esc.esc_reset();
            mJxit_esc.esc_print_text(
                    "Control command effect display：\n");
            mJxit_esc.esc_print_text("Print and enter effect demo：");
            mJxit_esc.esc_write_bytes(new byte[]{0x0D});
            mJxit_esc.esc_print_text(
                    "Print and walk a line of effects demonstration：");
            mJxit_esc.esc_write_bytes(new byte[]{0x0A});
            mJxit_esc.esc_print_text(
                    "Print and feed 100 longitudinal moving unit effect demonstration：");
            mJxit_esc.esc_write_bytes(new byte[]{0x1B, 0x4A, 0x64});
            mJxit_esc.esc_print_text(
                    "Print and walk 10 lines of high-performance effects：");
            mJxit_esc.esc_write_bytes(new byte[]{0x1B, 0x64, 0x0A});
            mJxit_esc.esc_print_text(
                    "Horizontal tab effect demonstration：\n");
            mJxit_esc.esc_write_bytes(new byte[]{0x09});
            mJxit_esc.esc_print_text("1");
            mJxit_esc.esc_write_bytes(new byte[]{0x09});
            mJxit_esc.esc_print_text("2");
            mJxit_esc.esc_write_bytes(new byte[]{0x09});
            mJxit_esc.esc_print_text("3");
            mJxit_esc.esc_write_bytes(new byte[]{0x09});
            mJxit_esc.esc_print_text("4\n");
            mJxit_esc.esc_write_bytes(new byte[]{0x1B, 0x24, 0x00, 0x00});
            mJxit_esc.esc_print_text("Absolute position 0、0 effect demo：\n");
            mJxit_esc.esc_write_bytes(new byte[]{0x1B, 0x24, 0x32, 0x32});
            mJxit_esc.esc_print_text("\n" +
                    "Absolute position 50、50 effect demo：\n");
            mJxit_esc.esc_write_bytes(new byte[]{0x1B, 0x24, (byte) 0x96, (byte) 0x96});
            mJxit_esc.esc_print_text("Absolute position 150、150 effect demo：\n");
            mJxit_esc.esc_reset();
            mJxit_esc.esc_write_bytes(new byte[]{0x1B, 0x32});
            mJxit_esc.esc_print_text("Set default line height effect demo：\n");
            mJxit_esc.esc_write_bytes(new byte[]{0x1B, 0x33, 0x00});
            mJxit_esc.esc_print_text("Set the line height to 0 effect demo：\n");
            mJxit_esc.esc_write_bytes(new byte[]{0x1B, 0x33, 0x32});
            mJxit_esc.esc_print_text(
                    "Set line height to 50 effect demo：\n");
            mJxit_esc.esc_write_bytes(new byte[]{0x1B, 0x33, (byte) 0x96});
            mJxit_esc.esc_print_text("Set line height to 150 effect demo：\n");
            mJxit_esc.esc_reset();
            mJxit_esc.esc_write_bytes(new byte[]{0x1B, 0x20, 0x00});
            mJxit_esc.esc_print_text(
                    "Set the right margin to 0 effect demo：\n");
            mJxit_esc.esc_write_bytes(new byte[]{0x1B, 0x20, 0x32});
            mJxit_esc.esc_print_text(
                    "Set the right margin to 50 effect demo：\n");
            mJxit_esc.esc_write_bytes(new byte[]{0x1B, 0x20, 0x64});
            mJxit_esc.esc_print_text(
                    "Set the right margin to 100 effect demo：\n");
            mJxit_esc.esc_write_bytes(new byte[]{0x1B, 0x20, (byte) 0x96});
            mJxit_esc.esc_print_text(
                    "Set the right margin to 150 effect demo：\n");
            mJxit_esc.esc_reset();
            mJxit_esc.esc_write_bytes(new byte[]{0x1D, 0x4C, 0x32, 0x00});
            mJxit_esc.esc_print_text(
                    "Set the left margin to 50、0 effect demo：\n");
            mJxit_esc.esc_write_bytes(new byte[]{0x1D, 0x4C, 0x32, 0x01});
            mJxit_esc.esc_print_text(
                    "Set the left margin to 50、1 effect demo：\n");
            mJxit_esc.esc_write_bytes(new byte[]{0x1D, 0x4C, 0x64, 0x01});
            mJxit_esc.esc_print_text(
                    "Set the left margin to 100、1 effect demo：\n");
            mJxit_esc.esc_write_bytes(new byte[]{0x1D, 0x4C, 0x00, 0x00});
            mJxit_esc.esc_print_text("Set the left margin to 0 effect demo：\n\n\n\n");
            mJxit_esc.esc_reset();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * printCateringBills
     */
    private boolean printCateringBills() {
        try {
            mJxit_esc.esc_reset();
            mJxit_esc.esc_default_line_height();
//            mJxit_esc.esc_align(1);
            mJxit_esc.esc_print_text("    Acural Solutions Pvt. Ltd\n");
            mJxit_esc.esc_print_text("              Delhi\n");
            mJxit_esc.esc_character_size(22);
            mJxit_esc.esc_print_text("    Table : 1\n\n");
            mJxit_esc.esc_reset();
            mJxit_esc.esc_print_text(PrintUtils.printTwoData("Book No.", "201901031515\n"));
            mJxit_esc.esc_print_text(PrintUtils.printTwoData("Book At:", "2019-01-03 10:46\n"));
            mJxit_esc.esc_print_text(PrintUtils.printTwoData("Take At:", "2017-01-03 11:46\n"));
            mJxit_esc.esc_print_text(PrintUtils.printTwoData("No.of people :2", "Cashier:Ajit\n"));

            mJxit_esc.esc_print_text("--------------------------------\n");
            mJxit_esc.esc_bold(true);
            mJxit_esc.esc_print_text(PrintUtils.printThreeData("Item", "Num ", "Price\n"));
            mJxit_esc.esc_print_text("--------------------------------\n");
            mJxit_esc.esc_bold(false);
            mJxit_esc.esc_print_text(PrintUtils.printThreeData("I1", "1", "0.00\n"));
            mJxit_esc.esc_print_text(PrintUtils.printThreeData("Item", "1", "6.00\n"));
            mJxit_esc.esc_print_text(PrintUtils.printThreeData("Item00", "1", "26.00\n"));
            mJxit_esc.esc_print_text(PrintUtils.printThreeData("Item0000", "1", "226.00\n"));
            mJxit_esc.esc_print_text(PrintUtils.printThreeData("Item000000", "1", "2226.00\n"));
            mJxit_esc.esc_print_text(PrintUtils.printThreeData("Item000000", "888", "98886.00\n"));

            mJxit_esc.esc_print_text("--------------------------------\n");
            mJxit_esc.esc_print_text(PrintUtils.printTwoData("Total", "53.50\n"));
            mJxit_esc.esc_print_text(PrintUtils.printTwoData("Int  ", "3.50\n"));
            mJxit_esc.esc_print_text("--------------------------------\n");
            mJxit_esc.esc_print_text(PrintUtils.printTwoData("Rece", "50.00\n"));
            mJxit_esc.esc_print_text("--------------------------------\n");

            mJxit_esc.esc_reset();
            mJxit_esc.esc_print_text("PS：Not Spicy\n\n\n\n");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * printPatrolResult
     */
    private boolean printPatrolResult() {
        try {
            mJxit_esc.esc_write_bytes(new byte[]{0x1B, 0x40, 0x1B, 0x61, 0x01, 0x1C, 0x21, 0x08});
            mJxit_esc.esc_print_text("---------------------------------------------------\nAcural Solutions Pvt. Ltd.\n");
            mJxit_esc.esc_write_bytes(new byte[]{0x1B, 0x40, 0x1B, 0x61, 0x01, 0x1C, 0x57, 0x01});
            //  mJxit_esc.esc_print_text("Order a correction notice\n");
            mJxit_esc.esc_write_bytes(new byte[]{0x1C, 0x57, 0x00});
            //  mJxit_esc.esc_print_text("Acural solutions pvt. ltd[2019]  No. 14\n");
            mJxit_esc.esc_write_bytes(new byte[]{0x1B, 0x40});
            mJxit_esc.esc_print_text("Acural is an emerging IT services provider and was started on a fundamental belief to put together great experiences in designing & developing the required solutions as per the needs of the customers.\n" +
                    "We strive to carve the process of development in a simple & lucid manner. We offer ready to use solutions, software application development & web design solutions that help small and medium-scale businesses to perform and grow.\n " +
                    "Our expertise in providing the tailored solution specific requirements would help your organization in proposing the exact mix of hardware & software based solution. We strive to assist your endeavor to gain market with competitive advantage from cost-efficient and effective solutions. We have been serving businesses representing diverse industry verticals with quality solutions and services." +
                    "\n\n");
            if (!mJxit_esc.esc_bold(true)) return false;
            if (!mJxit_esc.esc_print_text("Why Choose Us\n")) return false;
            //mJxit_esc.esc_print_text("Why Choose Us\n\n");
            mJxit_esc.esc_print_text("COMPETITIVE & AFFORDABLE PRICING\n\n" +
                    "CUSTOMER CENTRIC AFTER SALES\n SUPPORT\n\n" +
                    "ADOPTION OF LATEST TECHNOLOGIES\n\n" +
                    "HIGHLY SKILLED AND EXPERIENCED \n TEAM TO DELIVER PROJECTS\n" +
                    "\n\n");
            String pictureStr="0D 0A 1B 45 00 1B 61 02 0D 0A 1B 45 00 1B 4A 46 1D 76 30 00 16 00 C9 00 00 00 00 00 00 00 00 00 00 01 FF FF FC 00 " +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF F8 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0F FF FF FF " +
                    "FF FF 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF FF FF F8 00 00 00 00 00 00 00 00 00 00 00 00 00 00 07 FF FF " +
                    "FF FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 3F FF FF FF 07 FF FF FF E0 00 00 00 00 00 00 00 00 00 00 00 00 00 FF " +
                    "FF F8 00 00 00 FF FF F8 00 00 00 00 00 00 00 00 00 00 00 00 07 FF FF 00 00 00 00 07 FF FF 00 00 00 00 00 00 00 00 00 00 00 00 " +
                    "1F FF F0 00 00 00 00 00 7F FF C0 00 00 00 00 00 00 00 00 00 00 00 7F FF 80 00 00 00 00 00 0F FF F0 00 00 00 00 00 00 00 00 00 " +
                    "00 01 FF FC 00 00 00 00 00 20 01 FF FC 00 00 00 00 00 00 00 00 00 00 03 FF E0 00 30 00 00 02 30 00 3F FE 00 00 00 00 00 00 00 " +
                    "00 00 00 0F FF 80 00 38 00 00 03 39 80 0F FF 80 00 00 00 00 00 00 00 00 00 3F FE 01 00 FC 00 00 07 F9 C0 03 FF E0 00 00 00 00 " +
                    "00 00 00 00 00 7F F8 01 EF F8 00 00 07 FB C0 00 FF F0 00 00 00 00 00 00 00 00 01 FF E0 01 FF F8 00 00 07 73 C0 00 3F FC 00 00 " +
                    "00 00 00 00 00 00 03 FF 80 01 EE 70 00 00 07 77 80 00 0F FE 00 00 00 00 00 00 00 00 0F FE 00 01 E0 70 00 00 0F F7 98 00 03 FF " +
                    "80 00 00 00 00 00 00 00 1F F8 00 00 E0 F0 00 00 0E EF F8 00 00 FF C0 00 00 00 00 00 00 00 3F F0 00 00 E0 E0 00 00 0E EF FC 00 " +
                    "00 7F E0 00 00 00 00 00 00 00 FF C0 00 00 FD E0 80 00 0E FF FC 00 00 1F F8 00 00 00 00 00 00 01 FF 80 00 00 FF E1 C0 00 1F FF " +
                    "8C 00 00 0F FC 00 00 00 00 00 00 03 FF 00 00 00 7F C3 E0 00 1D FF 80 00 00 07 FE 00 00 00 00 00 00 07 FC 00 00 03 FF FF E0 00 " +
                    "1D F9 C0 00 00 01 FF 00 00 00 00 00 00 0F F8 00 00 03 FB FF C0 00 1D F1 C0 00 00 00 FF 80 00 00 00 00 00 1F F0 00 00 01 FF FF " +
                    "E0 00 3F E1 C0 00 00 00 7F C0 00 00 00 00 00 3F E0 00 00 00 3B FF E0 00 33 E1 C0 00 00 00 3F E0 00 00 00 00 00 7F C0 00 00 00 " +
                    "39 7F E0 00 3B 81 C0 00 00 06 1F F0 00 00 00 00 00 FF 80 38 00 00 3C 7F E0 00 3F 81 C0 00 00 0E 0F F8 00 00 00 00 01 FE 00 3F " +
                    "00 00 3C FF F0 00 3F F1 80 00 04 1E 03 FC 00 00 00 00 03 FC 00 3F 00 00 1D FF F0 00 7F FF 80 00 06 3F 01 FE 00 00 00 00 07 F8 " +
                    "00 3E 00 00 1F FF 70 00 77 FF 80 00 07 7E 00 FF 00 00 00 00 0F F8 7E 78 00 00 1F FE 70 00 77 7F 80 00 07 FF 00 FF 80 00 00 00 " +
                    "0F F0 7E F0 00 00 1F EE 70 00 77 77 00 00 0F FF 80 7F 80 00 00 00 1F E0 3F E7 80 00 0F EE 70 00 F7 77 00 00 7F FF C0 3F C0 00 " +
                    "00 00 3F C0 0F C7 80 00 1E FE 70 00 EF F7 00 00 FF FF E0 1F E0 00 00 00 7F 80 07 C7 C0 00 3D DE 78 07 EE FF 00 00 FF FF F0 0F " +
                    "F0 00 00 00 7F 00 0F EF E0 00 7D 9F F8 03 FE EE 00 00 0F DF FE 07 F0 00 00 00 FE 00 1F FF F0 00 79 BB F0 03 FF EF 80 00 7F EF " +
                    "3E 03 F8 00 00 01 FE 00 3C FC F8 00 38 79 F0 00 1F FF 80 00 7F E7 FC 03 FC 00 00 01 FC 00 78 FC 7C 00 00 70 60 00 01 FF 80 00 " +
                    "FD EF FC 01 FC 00 00 03 F8 00 F1 FE 3E 00 00 E0 00 00 00 1F 80 01 FF FF F0 00 FE 00 00 07 F0 01 EF DF 1F 00 00 C0 00 00 00 01 " +
                    "C0 03 FF FF C0 00 7F 00 00 07 F0 00 4F 9F 8F 80 00 00 00 00 00 00 00 07 FF FE 00 00 7F 00 00 0F E0 00 0F CF C7 80 00 00 00 00 " +
                    "00 00 00 0F FF 1E 00 00 3F 80 00 0F C0 00 07 E7 C3 C0 00 00 00 00 00 00 00 1F FF 9E 00 00 1F 80 00 1F C0 00 03 E3 EF C0 00 00 " +
                    "00 00 00 00 00 3F 7F FE 00 00 1F C0 00 3F 80 00 01 F1 FF C0 00 00 00 00 00 00 00 7F BF FE 00 00 0F E0 00 3F 80 00 00 F8 FF C0 " +
                    "00 00 00 00 00 00 00 FB DF FE 00 00 0F E0 00 7F 00 00 00 7C 7C 00 00 00 00 00 00 00 00 FD EF FE 00 00 07 F0 00 7F 00 00 00 3E " +
                    "3E 00 00 00 00 00 00 00 00 7E FF 80 00 00 07 F0 00 FE 00 00 00 1F 1F 00 00 00 00 00 00 00 00 0F FF 00 00 00 03 F8 00 FE 00 00 " +
                    "00 0F 8F 80 00 00 00 00 00 00 00 07 FE 00 00 00 03 F8 01 FC 00 00 00 07 C7 00 00 00 00 70 00 00 00 03 FC 00 00 00 01 FC 01 FC " +
                    "00 00 00 03 C3 00 00 00 00 70 00 00 00 00 F8 00 00 00 01 FC 01 F8 00 00 00 01 C0 00 00 00 00 70 00 00 00 00 F8 00 00 00 00 FC " +
                    "03 F8 00 00 00 00 80 00 00 00 00 70 00 00 00 00 F0 00 00 00 00 FE 03 F0 00 00 00 00 00 00 00 00 00 78 00 00 00 00 20 00 00 00 " +
                    "00 7E 07 F0 03 00 00 00 00 00 00 00 00 F8 00 00 00 00 00 00 00 18 00 7F 07 E0 3F 00 00 00 00 00 00 00 00 F8 00 00 00 00 00 00 " +
                    "00 1F FE 3F 07 E0 1E 00 00 00 00 00 00 00 00 FC 00 00 00 00 00 00 03 FF FC 3F 0F E0 1E 00 00 00 00 00 00 00 01 FC 00 00 00 00 " +
                    "00 00 03 FB FC 3F 0F C0 0E 70 00 00 00 00 00 00 01 FE 00 00 00 00 00 00 03 F9 F8 1F 0F C0 0C FC 00 00 00 00 00 00 03 FE 00 00 " +
                    "00 00 00 00 01 F7 E0 1F 1F C0 1C FF 80 00 00 00 00 00 03 FE 00 00 00 00 00 00 07 FF F8 1F 1F 81 DC 7F F0 00 00 00 00 00 03 FF " +
                    "00 00 00 00 00 00 3F FF F8 0F 1F 87 F8 EF FE 00 00 00 00 00 07 FF 00 00 00 00 00 01 FF DB F0 0F 1F 8F FE E1 FF 00 00 00 00 00 " +
                    "07 FF 80 00 00 00 00 0F FF DF FF 8F 3F 0F FF E0 3F 80 00 00 00 00 0F FF 80 00 00 00 00 7F FC DF FF 07 3F 00 3F FC 07 80 00 00 " +
                    "00 00 0F FF 80 00 00 00 00 FF DC EF 3F 07 3F 00 73 FF 8F C0 00 00 00 00 0F FF 80 00 00 00 00 FD CC EE 7E 07 3F 00 71 FF FE 00 " +
                    "00 00 00 00 0F FF 80 00 00 00 00 79 CE 6E FC 07 7E 00 63 8F FE 00 00 00 00 00 0F FF C0 00 00 00 00 1C EE FF FF 03 7E 00 E3 81 " +
                    "FF C0 00 00 00 00 1F FF C0 00 00 00 00 1C E7 FF EF 03 7E 00 EF E0 3F E0 00 00 00 00 1F FF C0 00 00 00 00 1C EF FF 8F 03 7E 00 " +
                    "EF FC 07 E0 00 00 00 00 1F FF E0 00 00 00 00 0E 7F FF CE 03 7E 01 E7 FF 80 E0 00 00 00 00 3F FF E0 00 00 00 00 0F FE 1F C0 03 " +
                    "7C 00 00 7F F0 00 00 00 00 00 3F FF F0 00 00 00 00 0F F8 07 80 01 7C 00 00 0F FC 00 00 00 00 00 3F FF F0 00 00 00 00 1F F0 03 " +
                    "00 01 FC 00 00 01 FC 00 00 00 00 00 7F FF F0 00 00 00 00 0F 00 00 00 01 FC 00 00 00 3C 00 07 FF FF FF FF FF FF FF FF FF 00 00 " +
                    "00 00 00 01 FC 00 00 00 04 00 07 FF FF FF FF FF FF FF FF FF 00 00 00 00 00 01 FC 00 00 00 00 00 01 FF FF FF FF FF FF FF FF FC " +
                    "00 00 00 00 00 01 FC 00 00 00 00 00 00 7F FF FF FF FF FF FF FF F0 00 00 00 00 00 01 FC 00 00 00 00 00 00 3F FF FF FF FF FF FF " +
                    "FF E0 00 00 00 00 00 01 FC 00 00 00 00 00 00 1F FF FF FF FF FF FF FF E0 00 00 00 00 00 01 FC 00 00 00 00 00 00 0F FF FF FF FF " +
                    "FF FF FF 80 00 00 00 00 00 01 FC 00 00 00 00 00 00 03 FF FF FF FF FF FF FF 00 00 00 00 00 00 01 F8 00 00 00 00 00 00 01 FF FF " +
                    "FF FF FF FF FC 00 00 00 00 00 00 00 F8 00 00 00 00 00 00 00 7F FF FF FF FF FF F8 00 00 00 00 00 00 00 F8 00 00 00 00 00 00 00 " +
                    "3F FF FF FF FF FF E0 00 00 00 00 00 00 00 F8 00 00 00 00 00 00 00 1F FF FF FF FF FF C0 00 00 00 78 00 00 00 F8 00 00 00 00 00 " +
                    "00 00 07 FF FF FF FF FF 00 00 00 00 78 00 00 00 FC 00 00 01 FE 00 00 00 03 FF FF FF FF FE 00 00 00 00 38 07 00 01 FC 00 00 7F " +
                    "FF 00 00 00 01 FF FF FF FF FC 00 00 00 00 3E 06 01 01 FC 00 0F FF FF 00 00 00 00 7F FF FF FF F8 00 00 00 03 9F FE 07 01 FC 05 " +
                    "FF FF 9E 00 00 00 00 3F FF FF FF F0 00 00 00 03 1F FF C7 01 FC 0F FF F0 1C 00 00 00 00 1F FF FF FF C0 00 00 00 03 0F FF FF 01 " +
                    "FC 1F FE 00 1C 00 00 00 00 1F FF FF FF C0 00 00 00 07 0E 1F FE 01 FC 1F C0 00 00 00 00 00 00 1F FF FF FF C0 00 00 00 07 1E 0F " +
                    "FE 01 FC 07 00 00 00 00 00 00 00 1F FF FF FF E0 00 00 00 07 1E FF 0F 01 FC 07 00 00 00 00 00 00 00 3F FF FF FF E0 00 00 00 06 " +
                    "1C FF CF 81 7C 07 00 00 00 00 00 00 00 3F FF FF FF F0 00 00 00 07 D8 FF FF 01 7C 03 00 00 00 00 00 00 00 7F FF FF FF F0 00 00 " +
                    "00 0F FE 7F FF 81 7E 03 80 00 00 00 00 00 00 7F FF FF FF F0 00 00 00 0F FF F1 FF 83 7E 03 80 00 01 80 00 00 00 7F FF FF FF F8 " +
                    "00 00 00 0E 7F FF C7 03 7E 03 00 00 3F C0 00 00 00 FF FF FF FF F8 00 00 00 0C 3B FF FE 03 7E 03 E0 07 FF E0 00 00 00 FF FF FF " +
                    "FF FC 00 00 00 0E 38 FF FE 03 7E 07 E0 FF FF C0 00 00 01 FF FF FF FF FC 00 00 00 0F 3D F3 FE 03 3F 0F FF FF F8 00 00 00 01 FF " +
                    "FF CF FF FC 00 00 00 1F 7D FF 8E 07 3F 0F BF FF 00 00 00 00 01 FF FF 07 FF FC 00 00 00 18 21 FF FE 07 3F 1E 7F E0 00 00 00 00 " +
                    "01 FF FC 01 FF FC 00 00 00 00 00 1F FF 07 3F 0C 7C 00 00 00 00 00 01 FF F8 00 FF FE 00 00 00 00 00 00 FF 07 1F 80 00 00 00 00 " +
                    "00 00 03 FF F0 00 3F FE 00 00 00 00 00 00 0C 0F 1F 80 00 00 00 00 00 00 03 FF C0 00 1F FE 00 00 01 00 00 00 00 0F 1F 80 00 00 " +
                    "00 00 00 00 03 FF 80 00 07 FF 00 00 03 80 00 00 00 0F 1F C0 00 00 00 3E 00 00 07 FF 00 00 03 FF 00 00 01 C0 00 00 00 1F 0F C0 " +
                    "00 00 00 3E 00 00 07 FC 00 00 01 FF 80 00 01 C0 00 00 00 1F 0F C0 00 00 00 1F 00 00 0F F8 00 00 00 FF 80 00 01 E0 00 00 00 1F " +
                    "0F E0 00 00 00 0F 00 00 0F E0 00 00 00 3F 80 00 00 E0 00 00 00 3F 07 E0 00 00 00 0F 00 00 0F C0 00 00 00 1F C0 00 00 F0 00 00 " +
                    "00 3F 07 E0 00 00 00 07 00 00 1F 80 00 00 00 0F C0 00 00 78 00 00 00 3F 07 F0 00 00 00 07 00 00 1F 00 00 00 00 03 C0 00 03 3C " +
                    "00 00 00 7F 03 F0 00 00 01 FF 00 00 1C 00 00 00 00 00 C0 00 03 BE 00 00 00 7E 03 F8 00 00 01 FF F0 00 10 00 00 00 00 00 40 00 " +
                    "03 DF 00 00 00 FE 01 F8 00 00 0F FF FC 00 00 00 00 00 00 00 00 00 03 FF 80 00 00 FC 01 FC 00 00 1F FF FE 00 00 00 00 00 00 00 " +
                    "00 07 E3 FF C0 00 01 FC 01 FC 00 00 3F 7E 1F 80 00 00 00 00 00 00 00 07 FF FF E0 00 01 FC 00 FE 00 00 7F BF C3 80 00 00 00 00 " +
                    "00 00 00 07 DF 7F F0 00 03 F8 00 FE 00 00 FF FF FB C0 00 00 00 00 00 00 00 07 9F 3F F8 00 03 F8 00 7F 00 01 FF FF FF C0 00 00 " +
                    "00 00 00 00 00 07 DF F9 FE 00 07 F0 00 7F 00 37 FF FC 3F 80 00 00 00 00 00 00 00 03 EF F3 FF 00 07 F0 00 3F 80 BF FF FC FE 00 " +
                    "00 00 00 00 00 00 00 01 F3 E7 9F 80 0F E0 00 3F 80 FF FB F9 F8 00 00 00 00 00 00 00 00 00 FD EF 1F C0 0F E0 00 1F C1 FF FF F7 " +
                    "E0 00 00 00 00 00 00 00 00 00 7F FE 3F F0 1F C0 00 0F C1 FF FF EF C0 00 00 00 00 00 00 00 00 00 3F 3C 79 F8 1F 80 00 0F E1 FF " +
                    "FF FF 00 00 00 00 00 00 00 00 00 00 1F B8 F1 F8 3F 80 00 07 F1 BF FF 7E 00 00 00 00 00 00 00 00 00 00 07 F1 E3 C0 7F 00 00 07 " +
                    "F0 1F FE FC 00 00 00 00 00 00 00 00 00 00 03 F5 C7 80 7F 00 00 03 F8 0F FD F8 00 00 00 00 00 00 00 00 00 00 01 EF 8F 00 FE 00 " +
                    "00 01 FC 0F FB E0 00 00 00 00 00 00 00 00 00 00 01 FF 9E 01 FC 00 00 01 FE 07 FF C0 00 00 00 00 00 00 00 00 00 00 00 0F 9C 03 " +
                    "FC 00 00 00 FE 03 FF 80 00 00 00 00 00 00 00 00 00 00 00 07 F8 03 F8 00 00 00 7F 01 FF 00 00 00 00 00 00 00 00 00 00 00 00 03 " +
                    "F8 07 F0 00 00 00 7F 80 FE 00 00 00 00 00 00 00 00 00 00 00 00 01 F0 0F F0 00 00 00 3F C0 78 00 00 00 00 00 00 00 00 00 00 00 " +
                    "00 00 F0 1F E0 00 00 00 1F E0 70 00 00 00 00 00 00 00 00 00 00 00 00 00 30 3F C0 00 00 00 0F F0 60 00 00 00 00 00 00 00 00 00 " +
                    "00 00 00 00 00 7F 80 00 00 00 0F F8 00 07 80 00 00 00 00 00 00 00 00 00 07 00 00 FF 80 00 00 00 07 F8 00 07 C0 00 00 00 00 00 " +
                    "00 00 00 00 1F 80 00 FF 00 00 00 00 03 FC 00 06 C0 00 00 00 00 00 00 00 00 00 1F C0 01 FE 00 00 00 00 01 FE 00 07 C8 00 00 00 " +
                    "00 00 00 00 00 00 19 C0 03 FC 00 00 00 00 00 FF 80 3F CE 00 00 00 00 00 00 00 00 03 9D C0 0F F8 00 00 00 00 00 7F C0 3B 1F 80 " +
                    "00 00 00 00 00 00 00 07 9E E0 1F F0 00 00 00 00 00 3F E0 3F 3B 80 00 00 00 00 00 00 00 0F EF C0 3F E0 00 00 00 00 00 1F F0 1F " +
                    "3E 80 00 00 00 00 00 00 00 0F FF C0 7F C0 00 00 00 00 00 0F F8 00 66 3C 00 00 00 00 00 00 03 EF 73 00 FF 80 00 00 00 00 00 07 " +
                    "FC 00 67 7E 00 00 00 00 00 00 07 E7 30 01 FF 00 00 00 00 00 00 03 FF 00 6E 77 00 00 00 00 00 00 77 67 F0 07 FE 00 00 00 00 00 " +
                    "00 01 FF 80 3E E7 7C 00 00 00 00 03 F7 F3 E0 0F FC 00 00 00 00 00 00 00 FF C0 1C E7 EE 30 00 00 00 C3 F3 F8 00 1F F8 00 00 00 " +
                    "00 00 00 00 3F F0 00 EE 6E F8 00 00 01 E3 63 B8 00 7F E0 00 00 00 00 00 00 00 1F F8 00 FE 3C FC F8 78 E1 E3 63 F8 00 FF C0 00 " +
                    "00 00 00 00 00 00 0F FE 00 7C FD CC F8 F9 E0 E0 63 F0 03 FF 80 00 00 00 00 00 00 00 03 FF 80 01 E9 DD DD D8 60 60 60 00 0F FE " +
                    "00 00 00 00 00 00 00 00 01 FF E0 01 FD DD DD F8 70 60 70 00 3F FC 00 00 00 00 00 00 00 00 00 7F F8 00 79 DD 9D FC 70 70 70 00 " +
                    "FF F0 00 00 00 00 00 00 00 00 00 3F FE 00 00 F9 DD DC 70 F8 00 03 FF E0 00 00 00 00 00 00 00 00 00 0F FF 80 00 71 F9 DC 78 F0 " +
                    "00 0F FF 80 00 00 00 00 00 00 00 00 00 03 FF E0 00 00 F9 F8 F8 00 00 3F FE 00 00 00 00 00 00 00 00 00 00 01 FF FC 00 00 00 70 " +
                    "80 00 01 FF FC 00 00 00 00 00 00 00 00 00 00 00 7F FF 80 00 00 00 00 00 0F FF F0 00 00 00 00 00 00 00 00 00 00 00 1F FF F0 00 " +
                    "00 00 00 00 7F FF C0 00 00 00 00 00 00 00 00 00 00 00 07 FF FF 00 00 00 00 07 FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 FF " +
                    "FF F8 00 00 00 FF FF F8 00 00 00 00 00 00 00 00 00 00 00 00 00 3F FF FF FF 07 FF FF FF E0 00 00 00 00 00 00 00 00 00 00 00 00 " +
                    "00 07 FF FF FF FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF FF FF F8 00 00 00 00 00 00 00 00 00 00 00 " +
                    "00 00 00 00 0F FF FF FF FF FF 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF F8 00 00 00 00 00 00 00 00 00 00 " +
                    "00 00 00 00 00 00 00 01 FF FF FC 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 " +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 " +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 " +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 " +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 " +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 " +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 " +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 " +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 " +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 " +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0D 0A";
            String[] data=pictureStr.split(" ");
            byte[] pictureBytes=new byte[data.length];
            for (int i=0; i < data.length; i++) {
                pictureBytes[i]=(byte) Integer.parseInt(data[i], 16);
            }
//            mJxit_esc.esc_write_bytes(pictureBytes);
            mJxit_esc.esc_print_formfeed();
            mJxit_esc.esc_write_bytes(new byte[]{0x1B, 0x4A, 0x20});
            mJxit_esc.esc_reset();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * printGoodsList
     */
    private boolean printGoodsList() {
        if (!mJxit_esc.esc_align(1)) return false;
        if (!mJxit_esc.esc_print_text("Acural Solutions Pvt. Ltd\n")) return false;
        if (!mJxit_esc.esc_align(1)) return false;
        if (!mJxit_esc.esc_print_text("Delhi\n")) return false;
        String s1="┏━━━━━━━┳━━┳━━━┓\n";
        String s2="┃ product name ┃unit┃ price┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s3="┃sugar         ┃ 1  ┃32.00 ┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s4="┃Frosted       ┃ 2  ┃11.00 ┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s5="┃refill        ┃ 2  ┃4.50  ┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s6="┃fluid/glue    ┃ 5  ┃14.00 ┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s7="┃Copy          ┃ 3  ┃22    ┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s8="┃tape/glue     ┃ 2  ┃11.20 ┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s9="┃paper         ┃ box┃32.00 ┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s10="┃stapler       ┃ 1  ┃16.6  ┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s11="┃ruler         ┃ 2  ┃3.00  ┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s12="┃Staples       ┃ box┃9.80  ┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s13="┃glue          ┃ 1  ┃9.60  ┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s14="┃frame         ┃ 1  ┃19.00 ┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s15="┃tier          ┃ 1  ┃36.00 ┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s16="┃holder        ┃ 1  ┃8.00  ┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s17="┃cabinets      ┃ 1  ┃122   ┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s18="┃Knife         ┃ 1  ┃17.5  ┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s19="┃total         ┃--  ┃00.00 ┃\n┗━━━━━━━┻━━┻━━━┛\n\n\n\n";
        mJxit_esc.esc_reset();
        return mJxit_esc.esc_write_bytes(new byte[]{0x1B, 0x33, 0x00, 0x0D, 0x0A}) && mJxit_esc.esc_print_text(s1) && mJxit_esc.esc_print_text(s2)
                && mJxit_esc.esc_print_text(s3) && mJxit_esc.esc_print_text(s4) && mJxit_esc.esc_print_text(s5) && mJxit_esc.esc_print_text(s6)
                && mJxit_esc.esc_print_text(s7) && mJxit_esc.esc_print_text(s8) && mJxit_esc.esc_print_text(s9) && mJxit_esc.esc_print_text(s10)
                && mJxit_esc.esc_print_text(s11) && mJxit_esc.esc_print_text(s12) && mJxit_esc.esc_print_text(s13) && mJxit_esc.esc_print_text(s14)
                && mJxit_esc.esc_print_text(s15) && mJxit_esc.esc_print_text(s16) && mJxit_esc.esc_print_text(s17) && mJxit_esc.esc_print_text(s18)
                && mJxit_esc.esc_print_text(s19) && mJxit_esc.esc_reset();
    }

    private boolean printLable() {
        String text="";
        if (!mCPCL.pageSetup(576, 1650)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 0, 120, 576, 120, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 0, 280, 576, 280, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 0, 360, 576, 360, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 0, 425, 576, 425, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 0, 550, 576, 550, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 0, 660, 576, 660, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 285, 360, 285, 425, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 55, 425, 55, 800, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 285, 650, 285, 800, false)) {
            return false;
        }

        text="530737925026";
        if (!mCPCL.drawBarCode(100, 130, text, 128, 0, 4, 80)) {
            return false;
        }
        if (!mCPCL.drawText(150, 210, text, 4, 0, 0, false, false)) {
            return false;
        }

        text="Collection of money: ₹0.01";
        if (!mCPCL.drawText(270, 80, text, 3, 0, 1, true, false)) {
            return false;
        }

        text="Agra to Delhi";
        if (!mCPCL.drawText(100, 290, text, 4, 0, 0, false, false)) {
            return false;
        }
        text="delhi";
        if (!mCPCL.drawText(90, 380, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="2019-01-5";
        if (!mCPCL.drawText(300, 380, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="Receive";
        if (!mCPCL.drawText(25, 430, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="Matter";
        if (!mCPCL.drawText(25, 455, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="letter";
        if (!mCPCL.drawText(25, 480, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="interest";
        if (!mCPCL.drawText(25, 505, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="send";
        if (!mCPCL.drawText(25, 560, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="Matter";
        if (!mCPCL.drawText(25, 585, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="letter";
        if (!mCPCL.drawText(25, 610, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="interest";
        if (!mCPCL.drawText(25, 635, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="clothes";
        if (!mCPCL.drawText(25, 690, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="Business";
        if (!mCPCL.drawText(25, 715, text, 2, 0, 0, false, false)) {
            return false;
        }

        text="Recipient：aaaa";
        if (!mCPCL.drawText(55, 450, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="Mobile phone/phone：00000000";
        if (!mCPCL.drawText(300, 450, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="address:Chander vihar";
        if (!mCPCL.drawText(55, 475, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="IP Ext.";
        if (!mCPCL.drawText(120, 500, text, 2, 0, 0, false, false)) {
            return false;
        }

        text="sender：Acural";
        if (!mCPCL.drawText(60, 580, text, 1, 0, 0, false, false)) {
            return false;
        }
        text="Mobile/phone：18721011668";
        if (!mCPCL.drawText(300, 580, text, 1, 0, 0, false, false)) {
            return false;
        }
        text="address：Chander vihar";
        if (!mCPCL.drawText(60, 605, text, 1, 0, 0, false, false)) {
            return false;
        }

        text="Content name:0123456789";
        if (!mCPCL.drawText(60, 665, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="Chargeable Weight:2.60(kg)";
        if (!mCPCL.drawText(60, 690, text, 2, 0, 0, false, false)) {
            return false;
        }
        text=
                "Declared value:₹60.00";
        if (!mCPCL.drawText(60, 715, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="Collection amount:₹0.01";
        if (!mCPCL.drawText(60, 740, text, 2, 0, 0, false, false)) {
            return false;
        }
        text=
                "Payment amount:₹0.00";
        if (!mCPCL.drawText(60, 765, text, 2, 0, 0, false, false)) {
            return false;
        }

        text="Signer/Submission time";
        if (!mCPCL.drawText(290, 665, text, 2, 0, 1, false, false)) {
            return false;
        }
        text=
                "Your signature means that you have accepted this package.,And confirmed";
        if (!mCPCL.drawText(290, 695, text, 1, 0, 0, false, false)) {
            return false;
        }
        text=
                "Product information is correct,Well packed,No scratches,Breakage, etc.";
        if (!mCPCL.drawText(290, 713, text, 1, 0, 0, false, false)) {
            return false;
        }
        text="Surface quality problem.";
        if (!mCPCL.drawText(290, 731, text, 1, 0, 0, false, false)) {
            return false;
        }
        text="month    day";
        if (!mCPCL.drawText(450, 775, text, 2, 0, 1, false, false)) {
            return false;
        }


        if (!mCPCL.drawLine(2, 0, 900, 576, 900, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 0, 955, 576, 955, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 0, 1075, 576, 1075, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 0, 1130, 576, 1130, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 0, 1285, 576, 1285, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 0, 1405, 576, 1405, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 0, 1465, 576, 1465, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 0, 1520, 576, 1520, false)) {
            return false;
        }

        if (!mCPCL.drawLine(2, 285, 900, 285, 1075, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 115, 1075, 115, 1190, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 230, 1075, 230, 1190, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 345, 1075, 345, 1190, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 460, 1075, 460, 1190, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 285, 1190, 285, 1405, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 115, 1405, 115, 1520, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 230, 1405, 230, 1520, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 345, 1405, 345, 1520, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 460, 1405, 460, 1520, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 285, 1520, 285, 1620, false)) {
            return false;
        }

        text="530737925026";
        if (!mCPCL.drawBarCode(30, 1200, text, 128, 0, 2, 55)) {
            return false;
        }
        if (!mCPCL.drawText(50, 1260, text, 2, 0, 0, false, false)) {
            return false;
        }

        text="Waybill number:930737925029";
        if (!mCPCL.drawText(0, 920, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="order number:DD8016450402";
        if (!mCPCL.drawText(290, 920, text, 2, 0, 0, false, false)) {
            return false;
        }

        text="Recipient information";
        if (!mCPCL.drawText(0, 960, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="Jiri 18122226666";
        if (!mCPCL.drawText(0, 985, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="Delhi region";
        if (!mCPCL.drawText(0, 1010, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="Chander vihar";
        if (!mCPCL.drawText(0, 1035, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="Sender information";
        if (!mCPCL.drawText(290, 960, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="BCD 18721011668";
        if (!mCPCL.drawText(290, 985, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="Acural Delhi";
        if (!mCPCL.drawText(290, 1010, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="No.";
        if (!mCPCL.drawText(290, 1035, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="Content name";
        if (!mCPCL.drawText(0, 1080, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="Chargeable Weight";
        if (!mCPCL.drawText(120, 1080, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="Declared value";
        if (!mCPCL.drawText(235, 1080, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="Collection amount";
        if (!mCPCL.drawText(350, 1080, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="Payment amount";
        if (!mCPCL.drawText(465, 1080, text, 2, 0, 0, false, false)) {
            return false;
        }

        text="0123456789";
        if (!mCPCL.drawText(0, 1135, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="2.60kg";
        if (!mCPCL.drawText(130, 1135, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="₹60.00";
        if (!mCPCL.drawText(235, 1135, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="₹0.01";
        if (!mCPCL.drawText(350, 1135, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="₹0.00";
        if (!mCPCL.drawText(465, 1135, text, 2, 0, 0, false, false)) {
            return false;
        }

        text="Recipient information";
        if (!mCPCL.drawText(0, 1290, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="Sdd 18122226666";
        if (!mCPCL.drawText(0, 1315, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="Delhi region";
        if (!mCPCL.drawText(0, 1340, text, 2, 0, 0, false, false)) {
            return false;
        }
        text=" Delhi";
        if (!mCPCL.drawText(0, 1365, text, 2, 0, 0, false, false)) {
            return false;
        }

        text=
                "Sender information";
        if (!mCPCL.drawText(290, 1290, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="bcds 18721011668";
        if (!mCPCL.drawText(290, 1315, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="Delhi";
        if (!mCPCL.drawText(290, 1340, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="No.";
        if (!mCPCL.drawText(290, 1365, text, 2, 0, 0, false, false)) {
            return false;
        }

        text=
                "Content name";
        if (!mCPCL.drawText(0, 1410, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="Chargeable Weight";
        if (!mCPCL.drawText(120, 1410, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="Declared value";
        if (!mCPCL.drawText(235, 1410, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="Collection amount";
        if (!mCPCL.drawText(350, 1410, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="Payment amount";
        if (!mCPCL.drawText(465, 1410, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="0123456789";
        if (!mCPCL.drawText(0, 1470, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="2.60kg";
        if (!mCPCL.drawText(130, 1470, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="₹60.00";
        if (!mCPCL.drawText(235, 1470, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="₹0.01";
        if (!mCPCL.drawText(350, 1470, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="₹0.00";
        if (!mCPCL.drawText(465, 1470, text, 2, 0, 0, false, false)) {
            return false;
        }

        text="Print Time";
        if (!mCPCL.drawText(0, 1530, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="2017-11-07 20:40";
        if (!mCPCL.drawText(0, 1560, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="Courier signature/Signature time";
        if (!mCPCL.drawText(290, 1525, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="month    day";
        if (!mCPCL.drawText(450, 1590, text, 2, 0, 1, false, false)) {
            return false;
        }

        if (!mCPCL.print(0, 1)) {
            return false;
        }
        return true;
    }

    private boolean printLable2() {
        String text="";
        if (!mCPCL.pageSetup(576, 500)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 0, 0, 510, 0, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 20, 70, 510, 70, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 20, 120, 415, 120, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 20, 200, 300, 200, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 20, 270, 415, 270, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 20, 310, 415, 310, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 20, 380, 475, 380, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 0, 435, 510, 435, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 300, 160, 415, 160, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 300, 220, 415, 220, false)) {
            return false;
        }

        if (!mCPCL.drawLine(2, 0, 0, 0, 435, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 20, 70, 20, 435, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 300, 70, 300, 270, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 415, 70, 415, 380, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 475, 70, 475, 435, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 510, 0, 510, 435, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 110, 380, 110, 435, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 190, 380, 190, 435, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 300, 380, 300, 435, false)) {
            return false;
        }
        if (!mCPCL.drawLine(2, 380, 380, 380, 435, false)) {
            return false;
        }

        text="569999315";
        if (!mCPCL.drawBarCode(425, 375, text, 128, 1, 3, 40)) {
            return false;
        }
        if (!mCPCL.drawText(100, 80, text, 2, 0, 0, false, false)) {
            return false;
        }

        text=
                "Returned item（Check back）";
        if (!mCPCL.drawText(70, 20, text, 3, 0, 1, false, false)) {
            return false;
        }
//        text = "零担";
//        if(!mCPCL.drawText(30,80,text,2,0,1,true,false)){return false;}
        text=" Express Test Distribution";
        if (!mCPCL.drawText(25, 150, text, 3, 0, 1, false, false)) {
            return false;
        }
        text="c";
        if (!mCPCL.drawText(25, 220, text, 3, 0, 1, false, false)) {
            return false;
        }

        Bitmap bitmap=Bitmap.createBitmap(70, 50, Bitmap.Config.RGB_565);
        Canvas canvas=new Canvas(bitmap);
        canvas.drawColor(Color.BLACK);
        Paint paint=new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
//        paint.setStrokeWidth(1);
        paint.setTextSize(24);
//        Typeface font = Typeface.createFromAsset(getAssets(), "song.TTF");
//        paint.setTypeface(font);
        canvas.drawText(
                "Lent", 10, 30, paint);
        if (!mCPCL.drawGraphic(20, 70, 70, 50, bitmap)) {
            return false;
        }

        text="1/2";
        if (!mCPCL.drawText(340, 80, text, 2, 0, 0, false, false)) {
            return false;
        }
        text=
                "Rack 2";
        if (!mCPCL.drawText(340, 130, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="Delivery";
        if (!mCPCL.drawText(310, 170, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="3000.0kg/ 15m3";
        if (!mCPCL.drawText(320, 225, 80, 35, text, 1, 0, 0, false, false)) {
            return false;
        }
        text=
                "Express secondary network branch division";
        if (!mCPCL.drawText(40, 280, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="Acural Delhi";
        if (!mCPCL.drawText(40, 330, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="lko";
        if (!mCPCL.drawText(40, 390, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="knp";
        if (!mCPCL.drawText(125, 390, text, 2, 0, 0, false, false)) {
            return false;
        }
        text="Acural";
        if (!mCPCL.drawText(220, 390, text, 2, 0, 0, false, false)) {
            return false;
        }

        if (!mCPCL.print(0, 1)) {
            return false;
        }
        return true;
    }


    private void printNewLable() {
        String text="";
        mJxit_cpcl.pageSetup(576, 2300);
        mJxit_cpcl.barcode1D(10, 10, 128, "1234567890", 1, 80);
        mJxit_cpcl.QrCode(10, 200, "1234567890", 1, 1, 10);
        BufferedInputStream bis=null;
        try {
            bis=new BufferedInputStream(getAssets().open("aa6.bmp"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bitmap=BitmapFactory.decodeStream(bis);
        Log.i("bitmapWidth", String.valueOf(bitmap.getWidth()));
        Log.i("bitmapHeight", String.valueOf(bitmap.getHeight()));
        mJxit_cpcl.image(10, 400, bitmap);
        mJxit_cpcl.line(10, 600, 550, 650, 1);
        mJxit_cpcl.box(10, 650, 550, 700, 1);
        mJxit_cpcl.text(10, 710, "16*16test", 0, 0, false, false, 0, 0);
        mJxit_cpcl.text(10, 730, "24*24test", 1, 0, false, false, 0, 0);
        mJxit_cpcl.text(10, 760, "32*32test", 2, 0, false, false, 0, 0);
        mJxit_cpcl.text(10, 800, "48*48test", 3, 0, false, false, 0, 0);
        mJxit_cpcl.text(10, 850, "64*64test", 4, 0, false, false, 0, 0);
        mJxit_cpcl.text(10, 920, "72*72test", 5, 0, false, false, 0, 0);
        mJxit_cpcl.text(10, 1000, "96*96test", 6, 0, false, false, 0, 0);
        mJxit_cpcl.text(10, 1100, "32*16test", 7, 0, false, false, 0, 0);
        mJxit_cpcl.text(10, 1140, "48*24 test", 8, 0, false, false, 0, 0);
        mJxit_cpcl.text(10, 1190, "48*32 test", 9, 0, false, false, 0, 0);
        mJxit_cpcl.text(10, 1240, "64*32 test", 10, 0, false, false, 0, 0);
        mJxit_cpcl.text(10, 1310, "72*48 test", 11, 0, false, false, 0, 0);
        mJxit_cpcl.text(10, 1390, "96*48 test", 12, 0, false, false, 0, 0);
        mJxit_cpcl.text(10, 1490, "96*72 test", 13, 0, false, false, 0, 0);
        mJxit_cpcl.text(10, 1590, "16*32 test", 14, 0, false, false, 0, 0);
        mJxit_cpcl.text(10, 1630, "24*48 test", 15, 0, false, false, 0, 0);
        mJxit_cpcl.text(10, 1680, "32*48", 16, 0, false, false, 0, 0);
        mJxit_cpcl.text(10, 1730, "32*64 test", 17, 0, false, false, 0, 0);
        mJxit_cpcl.text(10, 1800, "48*72 test", 18, 0, false, false, 0, 0);
        mJxit_cpcl.text(10, 1880, "48*96 test", 19, 0, false, false, 0, 0);
        mJxit_cpcl.text(10, 1980, "72*96 test", 20, 0, false, false, 0, 0);
        mJxit_cpcl.pagePrint();
    }

    /**
     * onDestroy
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mJxit_esc.isConnected()) {
            mJxit_esc.close();
        }

        if (mActivityReceiver != null) {
            unregisterReceiver(mActivityReceiver);
            mActivityReceiver=null;
        }

        if (mBtAdapter.isEnabled()) {
            mBtAdapter.disable();
        }
        }
    }


