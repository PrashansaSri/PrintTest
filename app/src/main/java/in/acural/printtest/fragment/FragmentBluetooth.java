package in.acural.printtest.fragment;

import static androidx.appcompat.app.AppCompatActivity.RESULT_OK;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import in.acural.libzxing.CaptureActivity;
import in.acural.printtest.R;


public class FragmentBluetooth extends Fragment {
    private static final int REQUEST_CAMERA_BT=50;
    private static final int REQUEST_ENABLE_BT=51;
    private static final int REQUEST_QR=52;

    private Switch mBluetoothSwitch;
    private ListView mPairedListView, mNewListView;
    private Button mSearchBtn, mScanBtn;

    private BluetoothAdapter mBluetoothAdapter;

    private PairedBlueToothDeviceAdapter mPairedBlueToothDeviceAdapter;
    private NewBlueToothDeviceAdapter mNewBlueToothDeviceAdapter;
    private List<BluetoothDevice> mPairedBlueList=new ArrayList<>();
    private List<BluetoothDevice> mNewBlueList=new ArrayList<>();

    private int btRequest;


    private FragmentInteractionBluetooth listernerBluetooth;

    public interface FragmentInteractionBluetooth {
        void processBluetooth(String str);
    }

    /**
     * onAttach
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof FragmentInteractionBluetooth) {
            listernerBluetooth=(FragmentInteractionBluetooth) activity;
        } else {
            throw new IllegalArgumentException("activity must implements FragmentInteraction");
        }
    }

    /**
     * onCreateView
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bluetooth, container, false);
    }

    /**
     * onActivityCreated
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initView();

        initBtAdapter();

        registerBtReceiver();
    }

    /**
     * initView
     */
    private void initView() {
        mBluetoothSwitch=(Switch) getActivity().findViewById(R.id.switch_bluetooth);
        mBluetoothSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (!mBluetoothAdapter.isEnabled()) {
                        btRequest=0;
                        Intent enableIntent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                    } else {
                        getPairedDevices();
                    }
                } else {
                    if (mBluetoothAdapter.isEnabled()) {
                        mBluetoothAdapter.disable();
                        mSearchBtn.setText("Search device");

                        mPairedBlueList.clear();
                        mPairedBlueToothDeviceAdapter.notifyDataSetChanged();

                        mNewBlueList.clear();
                        mNewBlueToothDeviceAdapter.notifyDataSetChanged();

                        listernerBluetooth.processBluetooth("@Turn off Bluetooth");
                    }
                }
            }
        });

        mSearchBtn = (Button) getActivity().findViewById(R.id.search_btn);
        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mBluetoothAdapter.isEnabled()) {
                    mNewBlueList.clear();
                    mNewBlueToothDeviceAdapter.notifyDataSetChanged();
                    if (mBluetoothAdapter.isDiscovering()) {
                        mBluetoothAdapter.cancelDiscovery();
                    }
                    mSearchBtn.setText("searching...");
                    mBluetoothAdapter.startDiscovery();
                }else {
                    btRequest = 1;
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
            }
        });

        mScanBtn = (Button) getActivity().findViewById(R.id.scan_btn);
        mScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mBluetoothAdapter.isEnabled()) {
                    btRequest = 2;
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }else {
                    if (Build.VERSION.SDK_INT>22) {
                        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_BT);
                        } else {
                            startActivityForResult(new Intent(getActivity(), CaptureActivity.class), REQUEST_QR);
                        }
                    }else {
                        startActivityForResult(new Intent(getActivity(), CaptureActivity.class), REQUEST_QR);
                    }
                }
            }
        });

        mPairedListView = (ListView) getActivity().findViewById(R.id.paired_lv);
        mPairedBlueToothDeviceAdapter = new PairedBlueToothDeviceAdapter(mPairedBlueList,getActivity(),mPairedListener);
        mPairedListView.setAdapter(mPairedBlueToothDeviceAdapter);
        mPairedListView.setOnItemClickListener(mPairedDeviceClickListener);

        mNewListView = (ListView) getActivity().findViewById(R.id.new_lv);
        mNewBlueToothDeviceAdapter = new NewBlueToothDeviceAdapter(mNewBlueList,getActivity());
        mNewListView.setAdapter(mNewBlueToothDeviceAdapter);
        mNewListView.setOnItemClickListener(mNewDeviceClickListener);

    }

    /**
     * initBtAdapter
     */
    private void initBtAdapter() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getActivity(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
            getActivity().finish();
        }else {
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothSwitch.setChecked(false);
            }else if(mBluetoothAdapter.isEnabled()) {
                mBluetoothSwitch.setChecked(true);
            }
        }
    }

    /**
     * registerBtReceiver
     */
    private void registerBtReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        getActivity().registerReceiver(mBluetoothDeviceReceiver, filter);
    }

    /**
     * getPaireddevices
     */
    private void getPairedDevices(){
        mPairedBlueList.clear();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mPairedBlueList.add(device);
                mPairedBlueToothDeviceAdapter.notifyDataSetChanged();
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

    /**
     * doNext
     */
    private void doNext(int requestCode, int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_BT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(new Intent(getActivity(), CaptureActivity.class),REQUEST_QR);
            }else {
                listernerBluetooth.processBluetooth(
                        "@No camera permissions");
            }
        }
    }

    /**
     * onActivityResult
     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    if(btRequest == 0){
                        mBluetoothSwitch.setChecked(true);
                        getPairedDevices();
                    }else if(btRequest == 1){
                        mBluetoothSwitch.setChecked(true);
                        getPairedDevices();

                        mNewBlueList.clear();
                        mNewBlueToothDeviceAdapter.notifyDataSetChanged();
                        if (mBluetoothAdapter.isDiscovering()) {
                            mBluetoothAdapter.cancelDiscovery();
                        }
                        mSearchBtn.setText(
                                "searching...");
                        mBluetoothAdapter.startDiscovery();
                    }else if(btRequest == 2){
                        mBluetoothSwitch.setChecked(true);
                        getPairedDevices();
                        mSearchBtn.setText(
                                "Search device");
                        if (Build.VERSION.SDK_INT>22) {
                            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_BT);
                            } else {
                                startActivityForResult(new Intent(getActivity(), CaptureActivity.class), REQUEST_QR);
                            }
                        }else {
                            startActivityForResult(new Intent(getActivity(), CaptureActivity.class), REQUEST_QR);
                        }
                    }
                }else {
                    mBluetoothSwitch.setChecked(false);
                }
                break;
            case REQUEST_QR:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    if (bundle == null) {
                        break;
                    }else {
                        String result = bundle.getString("result");
                        if(result != null && result.length() == 15 ) {
                            result = result.toUpperCase();
                            String address1 = result.substring(3,5);
                            String address2 = result.substring(5,7);
                            String address3 = result.substring(7,9);
                            String address4 = result.substring(9,11);
                            String address5 = result.substring(11,13);
                            String address6 = result.substring(13);
                            String scanAddress = address1+":"+address2+":"+address3+":"+address4+":"+address5+":"+address6;
                            if(BluetoothAdapter.checkBluetoothAddress(scanAddress)){
                                BluetoothDevice scanDevice = mBluetoothAdapter.getRemoteDevice(scanAddress);

                                if(!isBondedDevices(scanDevice)){
                                    Method createBondMethod = null;
                                    try {
                                        createBondMethod =BluetoothDevice.class.getMethod("createBond");
                                    } catch (NoSuchMethodException e) {
                                        e.printStackTrace();
                                    }
                                    try {
                                        if(createBondMethod != null) {
                                            createBondMethod.invoke(scanDevice);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }else {
                                    Toast.makeText(getActivity(),scanDevice.getName()+"Paired ok",Toast.LENGTH_SHORT).show();
                                    listernerBluetooth.processBluetooth(scanAddress);
                                }
                            }else {
                                listernerBluetooth.processBluetooth(
                                        "@MAC address error");
                            }
                        }else {
                            listernerBluetooth.processBluetooth("@Wrong MAC address");
                            Toast.makeText(getActivity(), "Check website for help", Toast.LENGTH_SHORT).show();
                            Intent intentObj = new Intent(Intent.ACTION_VIEW);
                            intentObj.setData(Uri.parse("http://www.acural.in/"));
                            startActivity(intentObj);
                        }
                    }
                    break;
                }
                break;
        }
    }

    private boolean isBondedDevices(BluetoothDevice device){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice bondedDevice : pairedDevices) {
                if(bondedDevice.equals(device)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * BroadcastReceiver
     */
    public BroadcastReceiver mBluetoothDeviceReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                        if (mNewBlueList.isEmpty()) {
                            mNewBlueList.add(device);
                            mNewBlueToothDeviceAdapter.notifyDataSetChanged();
                        } else if (!mNewBlueList.isEmpty() && mNewBlueList.size() != 0) {
                            L:
                            {
                                for (int i = 0; i < mNewBlueList.size(); i++) {
                                    if (device.equals(mNewBlueList.get(i))) {
                                        break L;
                                    }
                                }
                                if (device.getName() != null && device.getName().length() > 0) {
                                    mNewBlueList.add(device);
                                    mNewBlueToothDeviceAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    mScanBtn.setText("Search device");
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    BluetoothDevice createBondDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (createBondDevice.getBondState() == (BluetoothDevice.BOND_BONDED)) {
                        String str = createBondDevice.getName() + createBondDevice.getAddress();
                        Toast.makeText(getActivity(),createBondDevice.getName()+
                                "Pairing success",Toast.LENGTH_SHORT).show();
                        listernerBluetooth.processBluetooth(str);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private AdapterView.OnItemClickListener mPairedDeviceClickListener = new AdapterView.OnItemClickListener()  {


        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            if(mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
            String str = mPairedBlueList.get(arg2).getName()+mPairedBlueList.get(arg2).getAddress();
            Toast.makeText(getActivity(),mPairedBlueList.get(arg2).getName()+"Paired",Toast.LENGTH_SHORT).show();
            listernerBluetooth.processBluetooth(str);
        }
    };

    private AdapterView.OnItemClickListener mNewDeviceClickListener = new AdapterView.OnItemClickListener()  {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            if(mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
            mSearchBtn.setText("Search device");

            Method createBondMethod = null;
            try {
                createBondMethod =BluetoothDevice.class.getMethod("createBond");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            try {
                if(createBondMethod != null) {
                    createBondMethod.invoke(mNewBlueList.get(arg2));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * PairedBlueToothDeviceAdapter
     */
    public static class PairedBlueToothDeviceAdapter extends BaseAdapter {
        private List<BluetoothDevice> mBluelist;
        private LayoutInflater layoutInflater;
        private MyClickListener mListener;

        public PairedBlueToothDeviceAdapter(List<BluetoothDevice> list, Context context, MyClickListener listener) {
            mBluelist = list;
            layoutInflater = LayoutInflater.from(context);
            mListener = listener;
        }

        @Override
        public int getCount() {
            return mBluelist.size();
        }

        @Override
        public Object getItem(int i) {
            return mBluelist.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            final int position = i;
            ViewHolder viewHolder;
            if (view == null) {
                viewHolder = new ViewHolder();
                view = layoutInflater.inflate(R.layout.bluetooth_pairedlistview_item, null);
                viewHolder.device = (TextView) view.findViewById(R.id.lvitem_textview);
                viewHolder.imageFirst = (ImageView) view.findViewById(R.id.lvitem_image_first);
                viewHolder.imageEnd = (ImageView) view.findViewById(R.id.lvitem_image_end);
                viewHolder.imageEnd.setOnClickListener(mListener);
                viewHolder.imageEnd.setTag(position);
                view.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice blueDevice = mBluelist.get(i);
            final String deviceName = blueDevice.getName();
            if (deviceName != null && deviceName.length() > 0) {
                int deviceType = blueDevice.getBluetoothClass().getMajorDeviceClass();
                switch (deviceType){
                    case BluetoothClass.Device.Major.AUDIO_VIDEO:
                        viewHolder.imageFirst.setImageResource(R.drawable.headphone_mic_24);
                        break;
                    case BluetoothClass.Device.Major.COMPUTER:
                        viewHolder.imageFirst.setImageResource(R.drawable.computer_24);
                        break;
                    case BluetoothClass.Device.Major.HEALTH:
                        viewHolder.imageFirst.setImageResource(R.drawable.medical_bag_24);
                        break;
                    case BluetoothClass.Device.Major.IMAGING:
                        viewHolder.imageFirst.setImageResource(R.drawable.printer22);
                        break;
                    case BluetoothClass.Device.Major.MISC:
                        viewHolder.imageFirst.setImageResource(R.drawable.bluetooth_24);
                        break;
                    case BluetoothClass.Device.Major.NETWORKING:
                        viewHolder.imageFirst.setImageResource(R.drawable.bluetooth_24);
                        break;
                    case BluetoothClass.Device.Major.PERIPHERAL:
                        viewHolder.imageFirst.setImageResource(R.drawable.bluetooth_24);
                        break;
                    case BluetoothClass.Device.Major.PHONE:
                        viewHolder.imageFirst.setImageResource(R.drawable.phone_24);
                        break;
                    case BluetoothClass.Device.Major.TOY:
                        viewHolder.imageFirst.setImageResource(R.drawable.bluetooth_24);
                        break;
                    case BluetoothClass.Device.Major.UNCATEGORIZED:
                        viewHolder.imageFirst.setImageResource(R.drawable.bluetooth_24);
                        break;
                    case BluetoothClass.Device.Major.WEARABLE:
                        viewHolder.imageFirst.setImageResource(R.drawable.headphone_mic_24);
                        break;
                    default:
                        break;
                }
                viewHolder.device.setText(blueDevice.getName());
                viewHolder.imageEnd.setImageResource(R.drawable.info_24);
            }
            return view;

        }

        /**
         * 用于回调的抽象类
         *
         */
        static abstract class MyClickListener implements View.OnClickListener {
            /**
             * 基类的onClick方法
             */
            @Override
            public void onClick(View v) {
                myOnClick((Integer) v.getTag(), v);
            }
            public abstract void myOnClick(int position, View v);
         }

        class ViewHolder {
            /**
             * 避免重复的findviewbyId
             */
            TextView device;
            ImageView imageFirst,imageEnd;
        }
    }

    /**
     * NewBlueToothDeviceAdapter
     */
    public class NewBlueToothDeviceAdapter extends BaseAdapter {
        private List<BluetoothDevice> mBluelist;
        private LayoutInflater layoutInflater;

        NewBlueToothDeviceAdapter(List<BluetoothDevice> list, Context context) {
            mBluelist = list;
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mBluelist.size();
        }

        @Override
        public Object getItem(int i) {
            return mBluelist.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                viewHolder = new ViewHolder();
                view = layoutInflater.inflate(R.layout.bluetooth_newlistview_item, null);
                viewHolder.device = (TextView) view.findViewById(R.id.lvitem_textview);
                viewHolder.imageFirst = (ImageView) view.findViewById(R.id.lvitem_image_first);
                viewHolder.imageEnd = (ImageView) view.findViewById(R.id.lvitem_image_end);
                view.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice blueDevice = mBluelist.get(i);
            final String deviceName = blueDevice.getName();
            if (deviceName != null && deviceName.length() > 0) {
                int deviceType = blueDevice.getBluetoothClass().getMajorDeviceClass();
                switch (deviceType){
                    case BluetoothClass.Device.Major.AUDIO_VIDEO:
                        viewHolder.imageFirst.setImageResource(R.drawable.headphone_mic_24);
                        break;
                    case BluetoothClass.Device.Major.COMPUTER:
                        viewHolder.imageFirst.setImageResource(R.drawable.computer_24);
                        break;
                    case BluetoothClass.Device.Major.HEALTH:
                        viewHolder.imageFirst.setImageResource(R.drawable.medical_bag_24);
                        break;
                    case BluetoothClass.Device.Major.IMAGING:
                        viewHolder.imageFirst.setImageResource(R.drawable.printer22);
                        break;
                    case BluetoothClass.Device.Major.MISC:
                        viewHolder.imageFirst.setImageResource(R.drawable.bluetooth_24);
                        break;
                    case BluetoothClass.Device.Major.NETWORKING:
                        viewHolder.imageFirst.setImageResource(R.drawable.bluetooth_24);
                        break;
                    case BluetoothClass.Device.Major.PERIPHERAL:
                        viewHolder.imageFirst.setImageResource(R.drawable.bluetooth_24);
                        break;
                    case BluetoothClass.Device.Major.PHONE:
                        viewHolder.imageFirst.setImageResource(R.drawable.phone_24);
                        break;
                    case BluetoothClass.Device.Major.TOY:
                        viewHolder.imageFirst.setImageResource(R.drawable.bluetooth_24);
                        break;
                    case BluetoothClass.Device.Major.UNCATEGORIZED:
                        viewHolder.imageFirst.setImageResource(R.drawable.bluetooth_24);
                        break;
                    case BluetoothClass.Device.Major.WEARABLE:
                        viewHolder.imageFirst.setImageResource(R.drawable.headphone_mic_24);
                        break;
                    default:
                        break;
                }
                viewHolder.device.setText(blueDevice.getName());
                viewHolder.imageEnd.setImageResource(R.drawable.arrow_carrot_right_24);
            }
            return view;

        }


        class ViewHolder {
            /**
             * 避免重复的findviewbyId
             */
            TextView device;
            ImageView imageFirst,imageEnd;
        }
    }

    /**
     * mPairedListener
     */
    private PairedBlueToothDeviceAdapter.MyClickListener mPairedListener = new PairedBlueToothDeviceAdapter.MyClickListener() {
        @Override
        public void myOnClick(final int position, View v) {
            AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
            adb.setTitle(
                    "Bluetooth settings");
            adb.setMessage("Cancel"+mPairedBlueList.get(position).getName()+
                    "Pairing？");
            adb.setPositiveButton("determine", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    BluetoothDevice rmdevice = mPairedBlueList.get(position);
                    try {
                        Method m = rmdevice.getClass().getMethod("removeBond", (Class[]) null);
                        m.invoke(rmdevice, (Object[]) null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mPairedBlueList.remove(position);
                    mPairedBlueToothDeviceAdapter.notifyDataSetChanged();
                }
            });
            adb.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            adb.show();
        }
    };

    /**
     * onDetach
     */
    @Override
    public void onDetach() {
        super.onDetach();
        if(listernerBluetooth != null) {
            listernerBluetooth = null;
        }
    }

    /**
     * onDestroy
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mBluetoothDeviceReceiver != null) {
            getActivity().unregisterReceiver(mBluetoothDeviceReceiver);
            mBluetoothDeviceReceiver = null;
        }
       // listernerBluetooth.processBluetooth("@Exit the Bluetooth interface");
    }
}
