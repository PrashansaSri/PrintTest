package in.acural.printtest.fragment;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.jxit.jxitbluetoothprintersdk1_6.Jxit_esc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import in.acural.printtest.MainActivity;
import in.acural.printtest.R;


public class FragmentConfig extends Fragment {
    private static final int CONNECT_FAILD = 41;
    private static final int CONFIG_FAILD = 42;
    private static final int CONFIG_SUCCESS = 44;
    private static final int ADDRESS_ERROR = 47;
    private static final int REQUEST_ENABLE_BT = 48;

    private String deviceAddress ;
    private EditText mBtNameET,mPinET,mAddrET,mWifiNameET,mWifiPasswordET,mWifiPortET,mWifiMacET,
                     mWifiStaticIpET,mWifiGatewayET,mWifiSubnetMaskET,mDNSET;
    private Spinner mDeviceTypeSp,mWifiEncryptionSp;
    private CheckBox mWifiParameterCB,mWifiStaticIpCB,mWifiGatewayCB,mWifiSubnetMaskCB,mWifiDNSCB;
    private Button mConfigBtn,mReadBtn;
    private Jxit_esc mJxit_escConfig;
    private BluetoothAdapter mBluetoothAdapter;
    private List<String> listType,listEncryption;
    private RelativeLayout mWifiParamRelativeLayout,mWifiStaticIpRelativeLayout;

    private boolean isClicked = false;
    private ConfigThread mConfigThread;
    private String btName,btPin,btType,wifiEncryptionType;

    /**
     * onCreateView
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_config, container, false);
    }

    /**
     * onActivityCreated
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState)    {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    /**
     * init
     */
    private void init() {
        initView();

        initData();
    }

    /**
     * initView
     */
    private void initView(){
        deviceAddress = MainActivity.btDeviceAddress;

        mBtNameET = (EditText) getActivity().findViewById(R.id.et_bt_name);
        mPinET = (EditText) getActivity().findViewById(R.id.et_bt_pin);
        mAddrET = (EditText) getActivity().findViewById(R.id.et_bt_mac);
        mAddrET.setClickable(false);

        mWifiNameET = (EditText) getActivity().findViewById(R.id.et_wifi_name);
        mWifiPasswordET = (EditText) getActivity().findViewById(R.id.et_wifi_password);
        mWifiPortET = (EditText) getActivity().findViewById(R.id.et_wifi_port);
        mWifiMacET = (EditText) getActivity().findViewById(R.id.et_wifi_mac);

        mWifiStaticIpET = (EditText) getActivity().findViewById(R.id.et_wifi_static_ip);
        mWifiGatewayET = (EditText) getActivity().findViewById(R.id.et_wifi_gateway);
        mWifiSubnetMaskET = (EditText) getActivity().findViewById(R.id.et_wifi_subnet_mask);
        mDNSET = (EditText) getActivity().findViewById(R.id.et_wifi_DNS);

        mDeviceTypeSp = (Spinner) getActivity().findViewById(R.id.sp_device_identification) ;
        listType = new ArrayList<>();
        listType.add(
                "Printing device");
        listType.add("Bluetooth device");
        listType.add("Any device");
        ArrayAdapter<String> macAdapter=new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item,listType);
        macAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        mDeviceTypeSp.setAdapter(macAdapter);
        mDeviceTypeSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                btType = listType.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        mWifiEncryptionSp = (Spinner) getActivity().findViewById(R.id.sp_wifi_encryption) ;
        listEncryption = new ArrayList<>();

        listEncryption.add("WPA2");
        listEncryption.add("WEP");
        listEncryption.add("OPEN");
        ArrayAdapter<String> encryptionAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item,listEncryption);
        encryptionAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        mWifiEncryptionSp.setAdapter(encryptionAdapter);
        mWifiEncryptionSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                wifiEncryptionType = listEncryption.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        mWifiParamRelativeLayout = (RelativeLayout) getActivity().findViewById(R.id.rl_wifi_param) ;
        mWifiStaticIpRelativeLayout = (RelativeLayout) getActivity().findViewById(R.id.rl_static_ip) ;

        mWifiParameterCB = (CheckBox) getActivity().findViewById(R.id.cb_wifi_parameter);
        mWifiStaticIpCB = (CheckBox) getActivity().findViewById(R.id.cb_wifi_static_ip);
        mWifiGatewayCB = (CheckBox) getActivity().findViewById(R.id.cb_wifi_gateway);
        mWifiSubnetMaskCB = (CheckBox) getActivity().findViewById(R.id.cb_wifi_subnet_mask);
        mWifiDNSCB = (CheckBox) getActivity().findViewById(R.id.cb_wifi_DNS);

        mWifiParameterCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    mWifiParamRelativeLayout.setVisibility(View.VISIBLE);
                    mWifiStaticIpCB.setVisibility(View.VISIBLE);
                }else {
                    mWifiParamRelativeLayout.setVisibility(View.INVISIBLE);
                    mWifiStaticIpCB.setVisibility(View.INVISIBLE);
                    mWifiGatewayCB.setVisibility(View.INVISIBLE);
                    mWifiSubnetMaskCB.setVisibility(View.INVISIBLE);
                    mWifiDNSCB.setVisibility(View.INVISIBLE);
                    mWifiStaticIpET.setVisibility(View.INVISIBLE);
                    mWifiGatewayET.setVisibility(View.INVISIBLE);
                    mWifiSubnetMaskET.setVisibility(View.INVISIBLE);
                    mDNSET.setVisibility(View.INVISIBLE);
                }
            }
        });

        mWifiStaticIpCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    mWifiGatewayCB.setVisibility(View.VISIBLE);
                    mWifiSubnetMaskCB.setVisibility(View.VISIBLE);
                    mWifiDNSCB.setVisibility(View.VISIBLE);
                    mWifiStaticIpET.setVisibility(View.VISIBLE);
                }else {
                    mWifiGatewayCB.setVisibility(View.INVISIBLE);
                    mWifiSubnetMaskCB.setVisibility(View.INVISIBLE);
                    mWifiDNSCB.setVisibility(View.INVISIBLE);
                    mWifiStaticIpET.setVisibility(View.INVISIBLE);
                }
            }
        });

        mWifiGatewayCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    mWifiGatewayET.setVisibility(View.VISIBLE);
                }else {
                    mWifiGatewayET.setVisibility(View.INVISIBLE);
                }
            }
        });

        mWifiSubnetMaskCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    mWifiSubnetMaskET.setVisibility(View.VISIBLE);
                }else {
                    mWifiSubnetMaskET.setVisibility(View.INVISIBLE);
                }
            }
        });

        mWifiDNSCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    mDNSET.setVisibility(View.VISIBLE);
                }else {
                    mDNSET.setVisibility(View.INVISIBLE);
                }
            }
        });


        mConfigBtn = (Button) getActivity().findViewById(R.id.btn_config);
        mConfigBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( !isClicked){
                    isClicked = true;
                    btName = String.valueOf(mBtNameET.getText());
                    btPin = String.valueOf(mPinET.getText());
                    if(MainActivity.btDeviceAddress == null || !BluetoothAdapter.checkBluetoothAddress(MainActivity.btDeviceAddress)) {
                        Toast.makeText(getActivity(), "Please select a printer！", Toast.LENGTH_SHORT).show();
                        isClicked = false;
                    } else {
                        if (!mBluetoothAdapter.isEnabled()) {
                            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                        }else {
                            mConfigBtn.setEnabled(false);
                            mReadBtn.setEnabled(false);
                            mJxit_escConfig.close();
                            mConfigThread = new ConfigThread();
                            mConfigThread.start();
                        }
                    }
                }
            }
        });

        mReadBtn = (Button) getActivity().findViewById(R.id.btn_read);
        mReadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mAddrET.setText(deviceAddress);
            }
        });
    }

    /**
     * initData
     */
    private void initData() {
        mJxit_escConfig = Jxit_esc.getInstance();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }


    /**
     * onActivityResult
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            mConfigBtn.setEnabled(false);
            mReadBtn.setEnabled(false);
            mJxit_escConfig.close();
            mConfigThread = new ConfigThread();
            mConfigThread.start();
        }
        else Toast.makeText(getActivity(),
                "Please turn on Bluetooth！",Toast.LENGTH_SHORT).show();
    }

    /**
     * ReadThread
     */
    private class ConfigThread extends Thread {
        @Override
        public void run() {
            mJxit_escConfig.close();

            if (!BluetoothAdapter.checkBluetoothAddress(deviceAddress)) {
                mHandlerReadInfo.sendEmptyMessage(ADDRESS_ERROR);
            }else {
                if (!mJxit_escConfig.connectDevice(deviceAddress)) {
                    mHandlerReadInfo.sendEmptyMessage(CONNECT_FAILD);
                }else {
                    if (!config()) {
                        mHandlerReadInfo.sendEmptyMessage(CONFIG_FAILD);
                    }else{
                        mHandlerReadInfo.sendEmptyMessage(CONFIG_SUCCESS);
                    }
                }
            }
        }
        private boolean config(){
            byte type;
            if(btType.equals("Printing device")){
                type = 2;
            }else if(btType.equals(
                    "Bluetooth device")){
                type = 1;
            }else if(btType.equals("Any device")){
                type = 0;
            }else {
                type = 0;
            }
            if (!mJxit_escConfig.esc_write_bytes(new byte[]{0x10, 0x02, 0x0E, type})) {
                return false;
            }

            try {
                Thread.currentThread().sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int resultBtName = produce_config((byte) 11, btName.getBytes());
            Log.i("btName", String.valueOf(resultBtName));
            if (resultBtName == -1) {
                return false;
            } else if (resultBtName == -2) {
                return false;
            }
            Log.i("btName","OK");

            try {
                Thread.currentThread().sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int resultBtPin = produce_config((byte) 12, btPin.getBytes());
            Log.i("btPinStr", String.valueOf(resultBtPin));
            if (resultBtPin == -1) {
                return false;
            } else if (resultBtPin == -2) {
                return false;
            }
            Log.i("btPin","OK");

            try {
                Thread.currentThread().sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(!produce_config_cmd(1)){
                return false;
            }
            Log.i(
                    "Save Settings","OK");

            try {
                Thread.currentThread().sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(!produce_config_cmd(2)){
                return false;
            }
            Log.i("Print a self-test page","OK");

            try {
                Thread.currentThread().sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(!produce_config_cmd(3)){
                return false;
            }
            Log.i("Shut down","OK");

            return true;
        }
    }

    private boolean produce_config_write(byte ID, byte[] Value){
        int ValueLen = Value.length;
        byte[] Cmd = new byte[5+ValueLen];
        Cmd[0]= 0x10;
        Cmd[1]= 0x02;
        Cmd[2]= ID;
        Cmd[3]= 0x57;
        Cmd[4]= (byte) ValueLen;
        System.arraycopy(Value,0,Cmd,5,ValueLen);
        byte[] Rep = new byte[5];
        if(!mJxit_escConfig.esc_write_bytes(Cmd)) {
            return false;
        }
        if(!mJxit_escConfig.esc_read(Rep)) {
            return false;
        }
        if(!(Rep[0] == Cmd[0] && Rep[1] == Cmd[1] && Rep[2] == Cmd[2] && Rep[3] == Cmd[3] && Rep[4] == 0x00)){
            return false;
        }
        return true;
    }

    private boolean produce_config_read(byte ID, byte[] Value) {
        int ValueLen = Value.length;
        byte[] Cmd = new byte[4];
        Cmd[0] = 0x10;
        Cmd[1] = 0x02;
        Cmd[2] = ID;
        Cmd[3] = 0x52;
        mJxit_escConfig.esc_write_bytes(Cmd);
        byte[] Rep = new byte[5 + ValueLen];
        mJxit_escConfig.esc_read(Rep);
        byte[] Check = new byte[5 + ValueLen];
        System.arraycopy(Cmd, 0, Check, 0, 4);
        Check[4] = (byte) ValueLen;
        System.arraycopy(Value, 0, Check, 5, ValueLen);
        return Arrays.equals(Check, Rep);
    }

    private int produce_config(byte ID, byte[] Value) {
        if(!produce_config_write(ID, Value)){
            return -1;
        }else {
            if(!produce_config_read(ID, Value)){
                return -2;
            }
        }
        return 0;
    }

    private boolean produce_config_cmd(int Cmd) {
        byte[] cmd = new byte[12];
        byte[] check = {0x10, 0x03, 0x00};
        byte[] Rep = new byte[3];
        if (Cmd == 1) {
            cmd[0] = 0x10;
            cmd[1] = 0x03;
            cmd[2] = 0x08;
            cmd[3] = 0x00;
            cmd[4] = (byte) 0xFF;
            cmd[5] = (byte) 0xFD;
            cmd[6] = (byte) 0xCD;
            cmd[7] = 0x2A;
            cmd[8] = (byte) 0x8D;
            cmd[9] = (byte) 0xE1;
            cmd[10] = 0x23;
            cmd[11] = (byte) 0xF8;
        } else if (Cmd == 2) {
            cmd[0] = 0x10;
            cmd[1] = 0x03;
            cmd[2] = 0x08;
            cmd[3] = 0x00;
            cmd[4] = (byte)0xFF;
            cmd[5] = (byte)0xFD;
            cmd[6] = (byte)0x3D;
            cmd[7] = 0x2A;
            cmd[8] = (byte) 0x9D;
            cmd[9] = (byte) 0xE1;
            cmd[10] = (byte) 0xAD;
            cmd[11] = (byte) 0xF8;
        }
        if (Cmd == 3) {
            cmd[0] = 0x10;
            cmd[1] = 0x03;
            cmd[2] = 0x08;
            cmd[3] = 0x00;
            cmd[4] = (byte) 0xFF;
            cmd[5] = (byte) 0xFD;
            cmd[6] = (byte) 0x01;
            cmd[7] = 0x10;
            cmd[8] = (byte) 0x4C;
            cmd[9] = (byte) 0xF5;
            cmd[10] = (byte) 0x96;
            cmd[11] = (byte) 0xCF;
        }
        if(!mJxit_escConfig.esc_write_bytes(cmd)){
            return false;
        }
        if(!mJxit_escConfig.esc_read(Rep)){
            return false;
        }
        if(!Arrays.equals(Rep, check)){
            return false;
        }
        return true;
    }


    /**
     * Handler
     */
    private Handler mHandlerReadInfo = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CONNECT_FAILD:
                    Toast.makeText(getActivity(),"Connection failed！",Toast.LENGTH_SHORT).show();
                    mJxit_escConfig.close();
                    isClicked = false;
                    mConfigBtn.setEnabled(true);
                    mReadBtn.setEnabled(true);
                    break;
                case CONFIG_FAILD:
                    Toast.makeText(getActivity(),
                            "Setup failed！",Toast.LENGTH_SHORT).show();
                    mJxit_escConfig.close();
                    isClicked = false;
                    mConfigBtn.setEnabled(true);
                    mReadBtn.setEnabled(true);
                    break;
                case CONFIG_SUCCESS:
                    Toast.makeText(getActivity(),"Set successfully！",Toast.LENGTH_SHORT).show();
                    mJxit_escConfig.close();
                    isClicked = false;
                    mConfigBtn.setEnabled(true);
                    mReadBtn.setEnabled(true);
                    break;
                case ADDRESS_ERROR:
                    isClicked = false;
                    Toast.makeText(getActivity(),"Please select a printer！",Toast.LENGTH_SHORT).show();
                    mConfigBtn.setEnabled(true);
                    mReadBtn.setEnabled(true);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

}
