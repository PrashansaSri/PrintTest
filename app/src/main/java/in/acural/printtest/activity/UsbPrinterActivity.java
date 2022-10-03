package in.acural.printtest.activity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jxit.usbprintersdk.JxitUSBPrinter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;

import in.acural.printtest.R;

import static java.lang.Math.sin;
/*
//create by Rupendra Srivastava at 17/1/2019
*/

public class UsbPrinterActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final int PRINT_OK = 1;
    private static final int PRINT_ERROR = 2;
    private Toolbar mUsbTb;
    private TextView mTbTitleTV;

    private ImageButton mTbIBtn;
    public ListView mListView;
    private TextView mLogTV;

    private String[] titles={
            "Print text",
            "Print picture","Print one-dimensional barcode",
            "Print form","control commands",
            "Print a restaurant bill",
            "Print inspection results",
            "Printed goods list","Print lottery ticket"," "};
    int [] resIdsf={R.drawable.text_24,R.drawable.photo_24,R.drawable.barcode_24,
            R.drawable.barcode_2d_24,R.drawable.curve_24,R.drawable.table_24,
            R.drawable.settings_24,R.drawable.billing_24,R.drawable.lottery24};
    int [] resIdse={R.drawable.bullet_grey_1,R.drawable.bullet_grey_1,R.drawable.bullet_grey_1,
            R.drawable.bullet_grey_1,R.drawable.bullet_grey_1,R.drawable.bullet_grey_1,
            R.drawable.bullet_grey_1,R.drawable.bullet_grey_1,R.drawable.bullet_grey_1};

    private boolean isBackCliecked = false;


    private PendingIntent mPendingIntent;
    private JxitUSBPrinter mJxitUSBPrinter;

    private UsbManager usbManager = null;
    private UsbDevice myUsbDevice = null;
//    private UsbInterface usbInterface;
//    private UsbEndpoint epBulkOut;
//    private UsbEndpoint epBulkIn;
//    private UsbEndpoint epControl;
//    private UsbEndpoint epIntEndpointOut;
//    private UsbEndpoint epIntEndpointIn;
//    public UsbDeviceConnection myDeviceConnection;

    /**
     * Handler
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PRINT_OK:
                    mListView.setEnabled(true);
                    showToastMsg(
                            "Print successfully！");
                    break;
                case PRINT_ERROR:
                    mListView.setEnabled(true);
                    showToastMsg(
                            "Printing abnormal！");
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_printer);

        initViews();

        initUsbMagager();

        registReceiver();
    }

    private void initViews() {
        initToolbar();
        initListView();
        initTextView();
    }

    private void initUsbMagager() {
        Intent intent = new Intent();
        mPendingIntent = (PendingIntent) intent.getParcelableExtra(Intent.EXTRA_INTENT);
        mJxitUSBPrinter = new JxitUSBPrinter(this.getApplicationContext());
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

    }

    private void registReceiver() {
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);
    }


    /**
     * initToolbar
     */
    private void initToolbar(){
        mUsbTb = (Toolbar)findViewById(R.id.usb_tb);
        mTbTitleTV = (TextView) mUsbTb.findViewById(R.id.title_tb_tv);
        mTbIBtn = (ImageButton) mUsbTb.findViewById(R.id.right_tb_imgbtn);
        mTbIBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUsbDeviceInfo();
            }
        });

        setSupportActionBar(mUsbTb);
        if(getSupportActionBar() == null) {
            showToastMsg("ActionBar is not supported");
            return;
        }
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mUsbTb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

            }
        });
    }

    private void initListView() {
        mListView = (ListView) findViewById(R.id.demo_lv);
        mListView.setAdapter(new ListViewAdapter(resIdsf,titles,resIdse));
        mListView.setOnItemClickListener(mCmdClickListener);
    }

    private void initTextView() {
        mLogTV = (TextView) findViewById(R.id.log_tv);
        mLogTV.setMovementMethod(new ScrollingMovementMethod());
    }

    private void getUsbDeviceInfo() {
        mLogTV.setText("");

        enumeraterDevices();


}

    /**
     */
    public void enumeraterDevices() {
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        StringBuilder sb = new StringBuilder();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            sb.append(devicesString(device));
            myUsbDevice = device;
        }
        mLogTV.append(sb.toString());
    }

    /**
     * usb
     * device
     */
    public String devicesString(UsbDevice device) {
        StringBuilder builder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            builder = new StringBuilder("UsbDevice\nName=" + device.getDeviceName() +
                    "\nVendorId=" + device.getVendorId() + "\nProductId=" + device.getProductId() +
                    "\nmClass=" + device.getClass() + "\nmSubclass=" + device.getDeviceSubclass() +
                    "\nmProtocol=" + device.getDeviceProtocol() + "\nmManufacturerName="+device.getManufacturerName() +
                    "\nmSerialNumber=" + device.getSerialNumber() + "\n\n");
        }else {
            builder = new StringBuilder("UsbDevice\nName=" + device.getDeviceName() +
                    "\nVendorId=" + device.getVendorId() + "\nProductId=" + device.getProductId() +
                    "\nmClass=" + device.getClass() + "\nmSubclass=" + device.getDeviceSubclass() +
                    "\nmProtocol=" + device.getDeviceProtocol() + "\nmManufacturerName=" + "\nmSerialNumber=" +
                    "\n\n");
        }
        return builder.toString();
    }

//    /**
//     *
//     */
//    private boolean getDeviceInterface() {
//        if (myUsbDevice == null) {return false;}
//        usbInterface = myUsbDevice.getInterface(0);
//        return true;
//    }
//
//    /**
//     *
//     */
//    private boolean assignEndpoint() {
//        if (usbInterface == null) {return false;}
//        for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
//            UsbEndpoint ep = usbInterface.getEndpoint(i);
//            switch (ep.getType()) {
//                case UsbConstants.USB_ENDPOINT_XFER_BULK://
//                    if (UsbConstants.USB_DIR_OUT == ep.getDirection()) {//
//                        epBulkOut = ep;
//                    } else {
//                        epBulkIn = ep;
//                    }
//                    break;
//                case UsbConstants.USB_ENDPOINT_XFER_CONTROL://
//                    epControl = ep;
//                    break;
//                case UsbConstants.USB_ENDPOINT_XFER_INT://
//                    if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {//
//                        epIntEndpointOut = ep;
//                    }
//                    if (ep.getDirection() == UsbConstants.USB_DIR_IN) {
//                        epIntEndpointIn = ep;
//                    }
//                    break;
//                default:
//                    break;
//            }
//        }
//        return true;
//    }
//
//    /**
//     *
//     */
//    public boolean openDevice() {
//        if (usbInterface == null) {return false;}
//        //

//            return myDeviceConnection.bulkTransfer(epBulkOut, resultBytes, len,3000) >= 0;
//        }
//    }
//
//    public boolean sendMessageToPoint(String text) {
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        byte[] buffer = null;
//        try {
//            buffer = text.getBytes("GBK");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        return myDeviceConnection.bulkTransfer(epBulkOut, buffer, buffer != null ? buffer.length : 0, 3000) >= 0;
//    }
//
//    private byte[] receiveMessageFromPoint() {
//        int inMax = epBulkIn.getMaxPacketSize();
//        byte[] buffer = new byte[inMax];
//        if (myDeviceConnection.bulkTransfer(epBulkIn, buffer, buffer.length, 3000) >= 0) {
//            mLogTV.append(Arrays.toString(buffer));
//        } else {}
//        return buffer;
//    }

    /**
     * printText
     */
    private boolean printText(){
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(
                "Print text effect display：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_align(0)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(
                "Left alignment effect demonstration abc123：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        if(!mJxitUSBPrinter.esc_align(1)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("Center alignment effect demonstration abc123：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        if(!mJxitUSBPrinter.esc_align(2)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("Right alignment effect demonstration abc123：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        if(!mJxitUSBPrinter.esc_bold(true)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("Bold effect demonstration abc123：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        if(!mJxitUSBPrinter.esc_underline(2)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("Underline effect demonstration abc123：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(
                "Different high-performance demonstrations: 1 times")) {return false;}
        if(!mJxitUSBPrinter.esc_character_size(22)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("2 times")) {return false;}
        if(!mJxitUSBPrinter.esc_character_size(33)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("3 times")) {return false;}
        if(!mJxitUSBPrinter.esc_character_size(44)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("4 times\n")) {return false;}
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("Zoom in 1x effect demo abc123：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        if(!mJxitUSBPrinter.esc_character_size(22)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("Zoom in 2x effect demo abc123：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        if(!mJxitUSBPrinter.esc_character_size(33)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(
                "Zoom in 3 times effect demo abc123：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        if(!mJxitUSBPrinter.esc_character_size(44)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("Zoom in 4x effect demo abc123：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        if(!mJxitUSBPrinter.esc_font(0)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(
                "Font A effect demo abc123：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        if(!mJxitUSBPrinter.esc_font(1)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(
                "Font B effect demo abc123：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        if(!mJxitUSBPrinter.esc_font(2)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("Font C effect demo abc123：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        if(!mJxitUSBPrinter.esc_font(3)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(
                "Font D effect demo abc123：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        if(!mJxitUSBPrinter.esc_rotate(1)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("Rotate 90° clockwise to demonstrate abc123:\n")) {return false;}
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        if(!mJxitUSBPrinter.esc_rotate(2)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(
                "Rotate 180° clockwise to demonstrate abc123:\n")) {return false;}
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        if(!mJxitUSBPrinter.esc_rotate(3)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("Rotate 270° clockwise to demonstrate abc123：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        if(!mJxitUSBPrinter.esc_black_white_reverse(true)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("Black and white reverse effect demonstration abc123：\n\n\n\n\n")) {return false;}
//        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        Log.i("printText",1008611 + " true");
        return mJxitUSBPrinter.esc_select_cutting_mode(10);
    }

//    private boolean reset() {
//        byte[] buffer = {0x1B,0x40};
//        return sendMessageToPoint(buffer);
//    }
//
//    private boolean print_text(String text) {return sendMessageToPoint(text);}
//
//    private boolean align(int i) {
//        byte[] buffer = new byte[3];
//        buffer[0] = 0x1B;
//        buffer[1] = 0x61;
//        if(i == 1) {
//            buffer[2] = 0x01;
//        }else if(i == 2) {
//            buffer[2] = 0x02;
//        }else {
//            buffer[2] = 0x00;
//        }
//        return sendMessageToPoint(buffer);
//    }
//
//    private boolean bold(boolean b) {
//        byte[] buffer = new byte[3];
//        buffer[0] = 0x1B;
//        buffer[1] = 0x45;
//        if(b) {
//            buffer[2] = 0x01;
//        }else {
//            buffer[2] = 0x00;
//        }
//        return sendMessageToPoint(buffer);
//    }
//
//    private boolean underline(int i) {
//        byte[] buffer = new byte[3];
//        buffer[0] = 0x1B;
//        buffer[1] = 0x2D;
//        if(i == 1) {
//            buffer[2] = 0x01;
//        }else if(i == 2) {
//            buffer[2] = 0x02;
//        }else {
//            buffer[2] = 0x00;
//        }
//        return sendMessageToPoint(buffer);
//    }
//
//    private boolean character_size(int i) {
//        byte[] buffer = new byte[3];
//        buffer[0] = 0x1D;
//        buffer[1] = 0x21;
//        if(i == 2){
//            buffer[2] = 0x01;
//        }else if(i == 3) {
//            buffer[2] = 0x02;
//        }else if(i == 4) {
//            buffer[2] = 0x03;
//        }else if(i == 20) {
//            buffer[2] = 0x10;
//        }else if(i == 30) {
//            buffer[2] = 0x20;
//        }else if(i == 40) {
//            buffer[2] = 0x30;
//        }else if(i == 22) {
//            buffer[2] = 0x11;
//        }else if(i == 33) {
//            buffer[2] = 0x22;
//        }else if(i == 44) {
//            buffer[2] = 0x33;
//        }else {
//            buffer[2] = 0x00;
//        }
//        return sendMessageToPoint(buffer);
//    }
//
//    private boolean font(int i) {
//        byte[] buffer = new byte[3];
//        buffer[0] = 0x1B;
//        buffer[1] = 0x4D;
//        if(i == 0){
//            buffer[2] = 0x00;
//        }else if(i == 1) {
//            buffer[2] = 0x01;
//        }else if(i == 3) {
//            buffer[2] = 0x03;
//        }else {
//            buffer[2] = 0x02;
//        }
//        return sendMessageToPoint(buffer);
//    }
//
//    private boolean rotate(int i) {
//        byte[] buffer = new byte[3];
//        buffer[0] = 0x1B;
//        buffer[1] = 0x56;
//        if(i == 1){
//            buffer[2] = 0x01;
//        }else if(i == 2) {
//            buffer[2] = 0x02;
//        }else if(i == 3) {
//            buffer[2] = 0x03;
//        }else {
//            buffer[2] = 0x00;
//        }
//        return sendMessageToPoint(buffer);
//    }
//
//    private boolean black_white_reverse(boolean b) {
//        byte[] buffer = new byte[3];
//        buffer[0] = 0x1D;
//        buffer[1] = 0x42;
//        if(b){
//            buffer[2] = 0x01;
//        }else {
//            buffer[2] = 0x00;
//        }
//        return sendMessageToPoint(buffer);
//    }
//
    /**
     * printPicture
     */
    private boolean printPicture(){
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        if(!mJxitUSBPrinter.esc_print_formfeed_row(1)) {return false;}
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(getAssets().open("aa6.bmp"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(bis);
//        if(!mJxitUSBPrinter.esc_line_height(24)) {return false;}
        if(!mJxitUSBPrinter.esc_print_grating_bitmap(0,bitmap)) {return false;}
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        if(!mJxitUSBPrinter.esc_print_formfeed_row(4)) {return false;}
        return mJxitUSBPrinter.esc_select_cutting_mode();
    }


//
//    /**
//     * printBitmap
//     */
//    private boolean printBitmap(Bitmap bitmap) {
//        bitmap = Bitmap.createBitmap(bitmap);
//        int width = bitmap.getWidth();
//        int height = bitmap.getHeight();
//        int heightbytes = (height - 1) / 8 + 1;
//        int bufsize = width * heightbytes;
//        byte[] maparray = new byte[bufsize];
//        int[] pixels = new int[width * height];
//        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
//        /***/
//        for (int j = 0; j < height; j++) {
//            for (int i = 0; i < width; i++) {
//                int pixel = pixels[width * j + i];
//                if (pixel == Color.BLACK) {
//                    maparray[i + (j / 8) * width] |= (byte) (0x80 >> (j % 8));
//                }
//            }
//        }
//        byte[] Cmd = new byte[5];
//        byte[] pictureTop = new byte[]{0x1B,0x33,0x00};
//        if(!sendMessageToPoint(pictureTop)) {return false;}
//        /***/
//        for (int i = 0; i < heightbytes; i++) {
//            Cmd[0] = 0x1B;
//            Cmd[1] = 0x2A;
//            Cmd[2] = 0x00;
//            Cmd[3] = (byte) (width % 256);
//            Cmd[4] = (byte) (width / 256);
//            if(!sendMessageToPoint(Cmd)){return false;}
//            if(!sendMessageToPoint(maparray, i * width, width)){return false;}
//            if(!sendMessageToPoint(new byte[]{0x0D,0x0A})){return false;}
//        }
//        return true;
//    }
//
    /**
     * printBarcode1d
     */
    private boolean printBarcode1d(){
        if(!mJxitUSBPrinter.esc_print_enter()) {return false;}
        if(!mJxitUSBPrinter.esc_print_formfeed()) {return false;}
        if(!mJxitUSBPrinter.esc_barcode_1d(2,1,3,80,0,"123456789012")) {return false;}
        if(!mJxitUSBPrinter.esc_print_enter()) {return false;}
        if(!mJxitUSBPrinter.esc_print_formfeed()) {return false;}
        if(!mJxitUSBPrinter.esc_print_formfeed_row(2)) {return false;}
        return mJxitUSBPrinter.esc_select_cutting_mode(50);
    }
//
//    public boolean barcode_1d(int HRI_position,int HRI_font,int width,int height,int type,String content){
//        byte[] esc_barcode_1d_HRI_position = {0x1D,0x48,0x00};
//        if(HRI_position == 1 || HRI_position ==49) esc_barcode_1d_HRI_position[2] = 0x01;
//        if(HRI_position == 2 || HRI_position ==50) esc_barcode_1d_HRI_position[2] = 0x02;
//        else esc_barcode_1d_HRI_position[2] = 0x00;
//        if(!sendMessageToPoint(esc_barcode_1d_HRI_position, 0, esc_barcode_1d_HRI_position.length)) {return false;}
//
//        byte[] esc_barcode_1d_HRI_font = {0x1D,0x66,0x00};
//        if(HRI_font == 1 || HRI_font ==49) esc_barcode_1d_HRI_font[2] = 0x01;
//        else esc_barcode_1d_HRI_font[2] = 0x00;
//        if(!sendMessageToPoint(esc_barcode_1d_HRI_font, 0, esc_barcode_1d_HRI_font.length)) {return false;}
//
//        byte[] esc_barcode_1d_width = {0x1D,0x77,0x00};
//        if(width == 2) esc_barcode_1d_width[2] = 0x02;
//        if(width == 3) esc_barcode_1d_width[2] = 0x03;
//        else esc_barcode_1d_width[2] = 0x01;
//        if(!sendMessageToPoint(esc_barcode_1d_width, 0, esc_barcode_1d_width.length)) {return false;}
//
//        byte[] esc_barcode_1d_height = {0x1D,0x68, (byte) 0xA2};
//        if(height <= 0 || height > 255) esc_barcode_1d_height[2] = (byte) 0xA2;
//        else esc_barcode_1d_height[2] = (byte) height;
//        if(!sendMessageToPoint(esc_barcode_1d_height, 0, esc_barcode_1d_height.length)) {return false;}
//
//        if(type == 0 || type == 65) type = 0;
//        else if(type == 1 || type == 66) type = 1;
//        else if(type == 2 || type == 67) type = 2;
//        else if(type == 3 || type == 68) type = 3;
//        else if(type == 4 || type == 69) type = 4;
//        else if(type == 5 || type == 70) type = 5;
//        else if(type == 6 || type == 71) type = 6;
//        else if(type == 7 || type == 72) type = 7;
//        else if(type == 8 || type == 73) type = 8;
//        else type = 8 ;
//        byte[] esc_barcode_1d_type = {0x1D,0x6B, (byte) type};
//        if(!sendMessageToPoint(esc_barcode_1d_type, 0, esc_barcode_1d_type.length)) {return false;}
//
//        byte[] esc_barcode_1d_content_end = new byte[]{0x00};
//        if (!sendMessageToPoint(content.getBytes(), 0, content.length())) {return false;}
//        return sendMessageToPoint(esc_barcode_1d_content_end, 0, esc_barcode_1d_content_end.length);
//    }
//
    /**
     * printBarcode2d
     */
    private boolean printBarcode2d(){
        if(!mJxitUSBPrinter.esc_print_enter()) {return false;}
        if(!mJxitUSBPrinter.esc_print_formfeed()) {return false;}
        if(!mJxitUSBPrinter.esc_write_bytes(new byte[]{0x1D,0x77,0x04})) {return false;}
        if(!mJxitUSBPrinter.esc_write_bytes(new byte[]{0x1D,0x68,0x64})) {return false;}
        return mJxitUSBPrinter.esc_print_barcode_2d(1,"123456789012");
    }

    /**
     * printCurve
     */
    private boolean printCurve() {
        if(!mJxitUSBPrinter.esc_write_bytes(new byte[]{0x1B,0x40,0x0D,0x0A})) {return false;}
        byte curveBytes[] = new byte[7];
        byte y1,y1s = 0;
        byte[] sinBytes = new byte[4];
        curveBytes[0] = 0x1D;
        curveBytes[1] = 0x27;
        curveBytes[2] = 1;
        curveBytes[3] = 30;
        curveBytes[4] = 0;
        curveBytes[5] = (byte) 230;
        curveBytes[6] = 0;
        if(!mJxitUSBPrinter.esc_write_bytes(curveBytes)) {return false;}
        curveBytes[0] = 0x1D;
        curveBytes[1] = 0x27;
        curveBytes[2] = 2;
        curveBytes[3] = (byte) 130;
        curveBytes[4] = 0;
        curveBytes[5] = (byte) 130;
        curveBytes[6] = 0;

        for(int i=1; i <= 512; i++){
            y1 = (byte) (sin(i*3.1415926/128)*100+130);
            if(i==1) { y1s = y1;}
            if(!mJxitUSBPrinter.esc_write_bytes(curveBytes)) {return false;}

            sinBytes[0] = y1s;
            sinBytes[1] = 0;
            sinBytes[2] = y1;
            sinBytes[3] = 0;
            if(!mJxitUSBPrinter.esc_write_bytes(sinBytes)) {return false;}
            y1s = y1;
        }
        return mJxitUSBPrinter.esc_write_bytes(new byte[]{0x0D, 0x0A, 0x1B, 0x4A, 0x40});
    }

    /**
     * printTable
     */
    private boolean printTable(){

        String s1 = "┏━━┳━━━┳━━━┳━━━┓\n";
        String s2 = "┃No. ┃Name  ┃Sex   ┃Ages  ┃\n┣━━╋━━━╋━━━╋━━━┫\n";
        String s3 = "┃ 1  ┃anuj  ┃   M  ┃  18  ┃\n┣━━╋━━━╋━━━╋━━━┫\n";
        String s4 = "┃ 2  ┃ruby  ┃  FM  ┃  17  ┃\n┣━━╋━━━╋━━━╋━━━┫\n";
        String s5 = "┃ 3  ┃Ajit  ┃   M  ┃  16  ┃\n┗━━┻━━━┻━━━┻━━━┛\n\n\n\n";
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        if(!mJxitUSBPrinter.esc_line_height(0)) {return false;}
        if(!mJxitUSBPrinter.esc_print_enter()) {return false;}
        if(!mJxitUSBPrinter.esc_print_formfeed()) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(s1)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(s2)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(s3)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(s4)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(s5)) {return false;}
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        return mJxitUSBPrinter.esc_select_cutting_mode(50);

    }

    /**
     * printControlCommand
     */
    private boolean printControlCommand(){
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        if(!mJxitUSBPrinter.esc_print_enter()) {return false;}
        if(!mJxitUSBPrinter.esc_print_formfeed()) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("Control command effect display：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(
                "Print and enter effect demo：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_print_enter()) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("Print and walk a line of effects demonstration：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_print_formfeed()) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("Print and feed 100 longitudinal moving unit effect demonstration：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_print_formfeed(100)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("\n" +
                "Print and walk 10 lines of high-performance effects：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_print_formfeed_row(10)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("Horizontal tab effect demonstration：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_next_horizontal_tab()) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("1")) {return false;}
        if(!mJxitUSBPrinter.esc_next_horizontal_tab()) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("2")) {return false;}
        if(!mJxitUSBPrinter.esc_next_horizontal_tab()) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("3")) {return false;}
        if(!mJxitUSBPrinter.esc_next_horizontal_tab()) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("4\n")) {return false;}
        if(!mJxitUSBPrinter.esc_absolute_print_position(0,0)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("Absolute position 0、0 effect demo：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_absolute_print_position(50,50)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("\n" +
                "Absolute position 50、50 effect demo：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_absolute_print_position(150,150)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("\n" +
                "Absolute position 150、150 effect demo：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        if(!mJxitUSBPrinter.esc_default_line_height()) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("Set default line height effect demo：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_line_height(0)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(
                "Set the line height to 0 effect demo：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_line_height(50)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("Set line height to 50 effect demo：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_line_height(150)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(
                "Set line height to 150 effect demo：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        if(!mJxitUSBPrinter.esc_right_spacing(0)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("Set the right margin to 0 effect demo：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_right_spacing(50)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("Set the right margin to 50 effect demo：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_right_spacing(100)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(
                "Set the right margin to 100 effect demo：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_right_spacing(150)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(
                "Set the right margin to 150 effect demo：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        if(!mJxitUSBPrinter.esc_left_margin(50,0)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(
                "Set the left margin to 50、 \n" +
                        "0 effect demo ：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_left_margin(50,1)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("Set the left margin to 50、1 effect demo：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_left_margin(100,1)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("Set the left margin to 100、1 effect demo：\n")) {return false;}
        if(!mJxitUSBPrinter.esc_left_margin(0,0)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("Set the left margin to 0 effect demo：\n\n\n\n\n")) {return false;}
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        return mJxitUSBPrinter.esc_select_cutting_mode();
    }
    /**
     * printCateringBills
     */
    private boolean printCateringBills(){
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        if(!mJxitUSBPrinter.esc_default_line_height()) {return false;}
//        if(!mJxitUSBPrinter.esc_align(1)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("Red Star Restaurant\n\n")) {return false;}
        if(!mJxitUSBPrinter.esc_character_size(22)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(
                "Table number：Table 1\n\n")) {return false;}
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        if(!mJxitUSBPrinter.esc_align(0)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(printTwoData("Order number", "201704161515\n"))) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(printTwoData(
                "Order time", "2017-04-16 10:46\n"))) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(printTwoData("Serving time", "2017-04-16 11:46\n"))) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(printTwoData("Number of people：2 people", "Cashier：rahul San\n"))) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("--------------------------------\n")) {return false;}
        if(!mJxitUSBPrinter.esc_bold(true)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(printThreeData("project", "Quantity", "Amount\n"))) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("--------------------------------\n")) {return false;}
        if(!mJxitUSBPrinter.esc_bold(false)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(printThreeData("surface", "1", "0.00\n"))) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(printThreeData("rice", "1", "16.00\n"))) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(printThreeData("Teppanyaki", "1", "26.00\n"))) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(printThreeData("Braised squid", "1", "226.00\n"))) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(printThreeData("Braised beef noodles", "1", "2226.00\n"))) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(printThreeData("Braised Beef Noodle, Braised Beef Noodle, Braised Beef Noodle", "888", "98886.00\n"))) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("--------------------------------\n")) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(printTwoData("total", "63.50\n"))) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(printTwoData("\n" +
                "Wipe zero", "3.50\n"))) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("--------------------------------\n")) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(printTwoData("Receivable", "50.00\n"))) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("--------------------------------\n")) {return false;}
        if(!mJxitUSBPrinter.esc_align(0)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("Remarks：Do not be spicy、Do not parsley！\n\n\n\n\n")) {return false;}
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        return mJxitUSBPrinter.esc_select_cutting_mode();
    }

//    private boolean default_line_height() {
//        byte[] buffer = new byte[3];
//        buffer[0] = 0x1B;
//        buffer[1] = 0x32;
//        return sendMessageToPoint(buffer);
//    }

    /**
     * printPatrolResult
     */
    private boolean printPatrolResult(){
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        if(!mJxitUSBPrinter.esc_align(1)) {return false;}
        if(!mJxitUSBPrinter.esc_print_mode(8)) {return false;}
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
     if(!mJxitUSBPrinter.esc_print_text("---------------------------------------------------\nAcural Solutions Pvt. Ltd.\n")) {return false;}
        if(!mJxitUSBPrinter.esc_align(1)) {return false;}
        if(!mJxitUSBPrinter.esc_chinese_character_twice_height_width(true)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(
                "Order a correction notice\n")) {return false;}
        if(!mJxitUSBPrinter.esc_chinese_character_twice_height_width(false)) {return false;}
//        if(!mJxitUSBPrinter.esc_print_text(
//                "Acural Solution pvt Ltd[2012]  No. 14\n（Industrial and commercial department retention）\n")) {return false;}
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        if(!mJxitUSBPrinter.esc_print_text("Acural is an emerging IT services provider and was started on a fundamental belief to put together great experiences in designing & developing the required solutions as per the needs of the customers.\n" +


                "We strive to carve the process of development in a simple & lucid manner. We offer ready to use solutions, software application development & web design solutions that help small and medium-scale businesses to perform and grow.\n " +

                "Our expertise in providing the tailored solution specific requirements would help your organization in proposing the exact mix of hardware & software based solution. We strive to assist your endeavor to gain market with competitive advantage from cost-efficient and effective solutions. We have been serving businesses representing diverse industry verticals with quality solutions and services." +

                "\n"))
        {return false;}

        String pictureStr = "0D 0A 1B 45 00 1B 61 02 0D 0A 1B 45 00 1B 4A 46 1D 76 30 00 16 00 C9 00 00 00 00 00 00 00 00 00 00 01 FF FF FC 00 " +
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
        String[] data = pictureStr.split(" ");
        byte[] pictureBytes = new byte[data.length];
        for (int i = 0; i < data.length; i++){
            pictureBytes[i] = (byte) Integer.parseInt(data[i],16);
        }
        if(!mJxitUSBPrinter.esc_write_bytes(pictureBytes)) {return false;}
        if(!mJxitUSBPrinter.esc_print_formfeed_row(4)) {return false;}
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        return mJxitUSBPrinter.esc_select_cutting_mode();
    }

    /**
     * printGoodsList
     */
    private boolean printGoodsList() {
        String s1 = "┏━━━━━━━┳━━┳━━━┓\n";
        String s2 = "┃ product name ┃unit┃ price┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s3 = "┃sugar         ┃ 1  ┃32.00 ┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s4 = "┃Frosted       ┃ 2  ┃11.00 ┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s5 = "┃refill        ┃ 2  ┃4.50  ┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s6 = "┃fluid/glue    ┃ 5  ┃14.00 ┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s7 = "┃Copy          ┃ 3  ┃22    ┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s8 = "┃tape/glue     ┃ 2  ┃11.20 ┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s9 = "┃paper         ┃ box┃32.00 ┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s10= "┃stapler       ┃ 1  ┃16.6  ┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s11= "┃ruler         ┃ 2  ┃3.00  ┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s12= "┃Staples       ┃ box┃9.80  ┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s13= "┃glue          ┃ 1  ┃9.60  ┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s14= "┃frame         ┃ 1  ┃19.00 ┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s15= "┃tier          ┃ 1  ┃36.00 ┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s16= "┃holder        ┃ 1  ┃8.00  ┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s17= "┃cabinets      ┃ 1  ┃122   ┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s18= "┃Knife         ┃ 1  ┃17.5  ┃\n┣━━━━━━━╋━━╋━━━┫\n";
        String s19= "┃total         ┃--  ┃00.00 ┃\n┗━━━━━━━┻━━┻━━━┛\n\n\n\n";


        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        if(!mJxitUSBPrinter.esc_line_height(0)) {return false;}
        if(!mJxitUSBPrinter.esc_print_enter()) {return false;}
        if(!mJxitUSBPrinter.esc_print_formfeed()) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(s1)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(s2)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(s3)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(s4)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(s5)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(s6)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(s7)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(s8)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(s9)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(s10)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(s11)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(s12)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(s13)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(s14)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(s15)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(s16)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(s17)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(s18)) {return false;}
        if(!mJxitUSBPrinter.esc_print_text(s19)) {return false;}
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        return mJxitUSBPrinter.esc_select_cutting_mode(30);
    }


    /**
     * printLottery
     */
    private boolean printLottery(){
       String pictureStr = "1d 0c 20 cd e6 b7 a8 a3 ba c6 df c0 d6 b2 ca 2d b5 a5 ca bd 20 20 20 20 20 20 20 20 20 20 bb fa ba c5 a3 ba 32 32 30 31 30 30 31 30 0d 0a 20 33 39 " +
                "44 39 2d 38 32 45 36 2d 37 32 35 33 2d 46 41 35 43 2d 45 36 31 39 2f 35 38 31 30 32 33 39 34 2f 43 34 38 39 38 0d 0a 20 20 20 20 20 20 20 20 20 20 20 20 20 20 " +
                "20 20 20 b2 e2 20 20 ca d4 20 20 c6 b1 20 1b 33 32 1b 21 10 1b 45 01 1b 20 00 0d 0a 20 41 2e 1b 24 2e 00 30 32 1b 24 5e 00 30 35 1b 24 8e 00 31 32 1b 24 be " +
                "00 31 35 1b 24 ee 00 32 30 1b 24 1e 01 32 35 1b 24 4e 01 32 38 1b 24 7e 01 28 31 29 0d 0a 20 42 2e 1b 24 2e 00 30 35 1b 24 5e 00 31 35 1b 24 8e 00 32 30 1b " +
                "24 be 00 32 35 1b 24 ee 00 32 36 1b 24 1e 01 32 38 1b 24 4e 01 33 30 1b 24 7e 01 28 31 29 0d 0a 20 43 2e 1b 24 2e 00 30 33 1b 24 5e 00 30 35 1b 24 8e 00 30 " +
                "36 1b 24 be 00 30 37 1b 24 ee 00 31 35 1b 24 1e 01 31 37 1b 24 4e 01 32 35 1b 24 7e 01 28 31 29 0d 0a 20 44 2e 1b 24 2e 00 30 35 1b 24 5e 00 31 30 1b 24 8e " +
                "00 31 35 1b 24 be 00 31 39 1b 24 ee 00 32 31 1b 24 1e 01 32 35 1b 24 4e 01 32 36 1b 24 7e 01 28 31 29 0d 0a 20 45 2e 1b 24 2e 00 30 35 1b 24 5e 00 31 32 1b " +
                "24 8e 00 31 33 1b 24 be 00 31 35 1b 24 ee 00 32 32 1b 24 1e 01 32 35 1b 24 4e 01 32 38 1b 24 7e 01 28 31 29 1b 21 00 1b 45 00 1b 33 00 0d 0a 20 bf aa bd b1 " +
                "c6 da a3 ba 32 30 31 36 31 37 34 20 31 36 2d 30 36 2d 32 32 20 20 20 20 20 20 20 20 20 20 20 20 20 20 a3 a4 1b 21 30 1b 45 01 a3 ba 31 30 1b 21 00 1b 45 00 " +
                "0d 0a 20 cf fa ca db c6 da a3 ba 32 30 31 36 31 37 34 2d 31 20 20 20 20 20 20 20 20 20 20 20 31 36 2d 30 36 2d 32 32 20 31 30 a3 ba 30 39 a3 ba 32 32 0d 0a " +
                "20 b5 d8 d6 b7 a3 ba C9 EE DB DA CA D0 BB A5 B2 CA CD A8 BF C6 BC BC D3 D0 CF DE B9 AB CB BE 0d 0a 1b 33 00 1b 74 01 1d 21 00 1c 2e 1b 24 00 00 83 1b 24 20 " +
                "00 83 1b 24 40 00 83 1b 24 60 00 83 1b 24 80 00 83 1b 24 a0 00 83 1b 24 00 01 83 1b 24 20 01 83 1b 24 40 01 83 1b 24 60 01 83 1b 24 80 01 83 1b 24 a0 01 83 " +
                "1b 24 c0 01 83 1b 24 00 02 83 1b 24 20 02 83 0a 1b 24 00 00 83 1b 24 20 00 83 1b 24 60 00 83 1b 24 80 00 83 1b 24 a0 00 83 1b 24 c0 00 83 1b 24 e0 00 83 1b " +
                "24 00 01 83 1b 24 40 01 83 1b 24 60 01 83 1b 24 a0 01 83 1b 24 c0 01 83 1b 24 e0 01 83 1b 24 00 02 83 1b 24 20 02 83 0a 1b 24 00 00 83 1b 24 40 00 83 1b 24 " +
                "60 00 83 1b 24 80 00 83 1b 24 a0 00 83 1b 24 c0 00 83 1b 24 e0 00 83 1b 24 20 01 83 1b 24 a0 01 83 1b 24 c0 01 83 1b 24 e0 01 83 1b 24 00 02 83 1b 24 20 02 " +
                "83 0a 1b 24 00 00 83 1b 24 20 00 83 1b 24 40 00 83 1b 24 60 00 83 1b 24 80 00 83 1b 24 a0 00 83 1b 24 c0 00 83 1b 24 e0 00 83 1b 24 00 01 83 1b 24 20 01 83 " +
                "1b 24 40 01 83 1b 24 60 01 83 1b 24 80 01 83 1b 24 a0 01 83 1b 24 c0 01 83 1b 24 e0 01 83 1b 24 00 02 83 1b 24 20 02 83 0a 1b 24 00 00 83 1b 24 20 00 83 1b " +
                "24 40 00 83 1b 24 60 00 83 1b 24 80 00 83 1b 24 a0 00 83 1b 24 c0 00 83 1b 24 e0 00 83 1b 24 00 01 83 1b 24 20 01 83 1b 24 c0 01 83 1b 24 e0 01 83 1b 24 00 " +
                "02 83 1b 24 20 02 83 0a 0a 1c 26 1d 21 00 1b 74 00 1b 33 1e 0a 0a";

        String[] data = pictureStr.split(" ");
        byte[] lotteryBytes = new byte[data.length];
        for (int i = 0; i < data.length; i++){
            lotteryBytes[i] = (byte) Integer.parseInt(data[i],16);
        }
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        if(!mJxitUSBPrinter.esc_write_bytes(lotteryBytes)) {return false;}
        if(!mJxitUSBPrinter.esc_reset()) {return false;}
        return mJxitUSBPrinter.esc_select_cutting_mode(30);
    }


    /**
     *
     */
    private static final int LINE_BYTE_SIZE = 32;

    private static final int LEFT_LENGTH = 20;

    private static final int RIGHT_LENGTH = 12;

    /**
     */
    private static final int LEFT_TEXT_MAX_LENGTH = 8;

    /**
     */
    public static final int MEAL_NAME_MAX_LENGTH = 8;

    /**
     *
     * @param leftText
     * @param rightText
     * @return
     */
    public static String printTwoData(String leftText, String rightText) {
        StringBuilder sb = new StringBuilder();
        int leftTextLength = getBytesLength(leftText);
        int rightTextLength = getBytesLength(rightText);
        sb.append(leftText);

        //
        int marginBetweenMiddleAndRight = LINE_BYTE_SIZE - leftTextLength - rightTextLength;

        for (int i = 0; i < marginBetweenMiddleAndRight; i++) {
            sb.append(" ");
        }
        sb.append(rightText);
        return sb.toString();
    }

    /**
     *
     *
     * @param leftText
     * @param middleText
     * @param rightText
     * @return
     */
    public static String printThreeData(String leftText, String middleText, String rightText) {
        StringBuilder sb = new StringBuilder();
        if (leftText.length() > LEFT_TEXT_MAX_LENGTH) {
            leftText = leftText.substring(0, LEFT_TEXT_MAX_LENGTH) + "..";
        }
        int leftTextLength = getBytesLength(leftText);
        int middleTextLength = getBytesLength(middleText);
        int rightTextLength = getBytesLength(rightText);

        sb.append(leftText);
        int marginBetweenLeftAndMiddle = LEFT_LENGTH - leftTextLength - middleTextLength / 2;

        for (int i = 0; i < marginBetweenLeftAndMiddle; i++) {
            sb.append(" ");
        }
        sb.append(middleText);

        int marginBetweenMiddleAndRight = RIGHT_LENGTH - middleTextLength / 2 - rightTextLength;

        for (int i = 0; i < marginBetweenMiddleAndRight; i++) {
            sb.append(" ");
        }

        sb.delete(sb.length() - 1, sb.length()).append(rightText);
        return sb.toString();
    }

    /**
     *
     * @param msg
     * @return
     */
    private static int getBytesLength(String msg) {
        return msg.getBytes(Charset.forName("GB2312")).length;
    }

    /**
     *
     * @param name
     * @return
     */
    public static String formatMealName(String name) {
        if (TextUtils.isEmpty(name)) {
            return name;
        }
        if (name.length() > MEAL_NAME_MAX_LENGTH) {
            return name.substring(0, 8) + "..";
        }
        return name;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.right_tb_imgbtn:
                break;
            default:
                break;
        }
    }

    /**
     * ListViewAdapter
     */
    public class ListViewAdapter extends BaseAdapter {
        View[] itemViews;
        ListViewAdapter(int[] itemImageResf, String[] itemTexts, int[] itemImageRese){
            itemViews = new View[itemImageResf.length];
            for (int i=0; i<itemViews.length; ++i){
                itemViews[i] = makeItemView(itemImageResf[i], itemTexts[i], itemImageRese[i]);
            }
        }
        public int getCount()  {return itemViews.length;}
        public View getItem(int position)  {return itemViews[position];}
        public long getItemId(int position) {return position;}
        private View makeItemView(int resIdf, String strText, int resIde) {
            final String tit = strText;
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View itemView = inflater.inflate(R.layout.listview_item, null);
            ImageView imagef = (ImageView)itemView.findViewById(R.id.lvitem_image_first);
            imagef.setImageResource(resIdf);
            TextView title = (TextView)itemView.findViewById(R.id.lvitem_textview);
            title.setText(strText);
            final ImageView imagee = (ImageView)itemView.findViewById(R.id.lvitem_image_end);
            imagee.setImageResource(resIde);
            imagee.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (tit){
                        case
                                "Print text":
//                            Intent intentT = new Intent(UsbPrinterActivity.this,TextActivity.class);
//                            startActivity(intentT);
                            break;
                        case "Print picture":
//                            Intent intentP = new Intent(UsbPrinterActivity.this,PictureActivity.class);
//                            startActivity(intentP);
                            break;
                        case "Print one-dimensional barcode":
//                            Intent intentB1 = new Intent(UsbPrinterActivity.this,Barcode1dActivity.class);
//                            startActivity(intentB1);
                            break;
                        case
                                "Print 2D barcode":
//                            Intent intentB2 = new Intent(UsbPrinterActivity.this,Barcode2dActivity.class);
//                            startActivity(intentB2);
                            break;
                        default:
                            break;
                    }

                }
            });
            return itemView;
        }

        public View getView(final int position, View convertView, final ViewGroup parent) {
            if (convertView == null) {
                return itemViews[position];
            }
            return convertView;
        }
    }

    /**
     * mCmdClickListener
     */
    private AdapterView.OnItemClickListener mCmdClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            if(myUsbDevice != null){
                if(mJxitUSBPrinter.openDevice(myUsbDevice,mPendingIntent)){
                    Log.i("cmd",titles[arg2]);
                    printCmd(titles[arg2]);
                }else {
                    mHandler.sendEmptyMessage(PRINT_ERROR);
                }
            }else {
                showToastMsg(
                        "Please click on the USB button in the upper right corner to find the connected device！");
            }

        }
    };

    private void printCmd(final String cmd) {
        mListView.setEnabled(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                doPrint(cmd);
            }
        }).start();

    }

    private void doPrint(String cmd) {
        switch (cmd){
            case "Print text":
                if(printText()){
                    mHandler.sendEmptyMessageDelayed(PRINT_OK,1000);
                }else {
                    mHandler.sendEmptyMessage(PRINT_ERROR);
                }
            break;
            case "Print picture":
                if(printPicture()){
                    mHandler.sendEmptyMessageDelayed(PRINT_OK,1000);
                }else {
                    mHandler.sendEmptyMessage(PRINT_ERROR);
                }
                break;
            case
                    "Print one-dimensional barcode":
                if(printBarcode1d()){
                    mHandler.sendEmptyMessageDelayed(PRINT_OK,1000);
                }else {
                    mHandler.sendEmptyMessage(PRINT_ERROR);
                }
                break;
            case
                    "Print form":
                if(printTable()){
                    mHandler.sendEmptyMessageDelayed(PRINT_OK,2000);
                }else {
                    mHandler.sendEmptyMessage(PRINT_ERROR);
                }
                break;
            case
                    "control commands":
                if(printControlCommand()){
                    mHandler.sendEmptyMessageDelayed(PRINT_OK,1000);
                }else {
                    mHandler.sendEmptyMessage(PRINT_ERROR);
                }
                break;
            case
                    "Print a restaurant bill":
                if(printCateringBills()){
                    mHandler.sendEmptyMessageDelayed(PRINT_OK,1000);
                }else {
                    mHandler.sendEmptyMessage(PRINT_ERROR);
                }
                break;
            case "Print inspection results":
                if(printPatrolResult()){
                    mHandler.sendEmptyMessageDelayed(PRINT_OK,1000);
                }else {
                    mHandler.sendEmptyMessage(PRINT_ERROR);
                }
                break;
            case
                    "Printed goods list":
                if(printGoodsList()){
                    mHandler.sendEmptyMessageDelayed(PRINT_OK,1000);
                }else {
                    mHandler.sendEmptyMessage(PRINT_ERROR);
                }
                break;
            case
                    "Print lottery ticket":
                if(printLottery()){
                    mHandler.sendEmptyMessageDelayed(PRINT_OK,1000);
                }else {
                    mHandler.sendEmptyMessage(PRINT_ERROR);
                }
                break;
            case " ":
//                if(printCurve()){
//                    mHandler.sendEmptyMessage(PRINT_OK);
//                }else {
//                    mHandler.sendEmptyMessage(PRINT_ERROR);
//                }
                break;
            default:
                break;
        }
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            if (isBackCliecked) {
//                this.finish();
//            } else {
//                isBackCliecked = true;
//                t.setGravity(Gravity.CENTER, 0, 0);
//                t.show();
//            }
//        }
//        return true;
//    }

    private void showToastMsg(String msg) {
        Toast.makeText(UsbPrinterActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                showToastMsg(
                        "USB device has been unplugged！");
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                mJxitUSBPrinter.close();
//                if (device != null) {
//                    myDeviceConnection.close();
//                    myDeviceConnection = null;
//                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mUsbReceiver != null){
            unregisterReceiver(mUsbReceiver);
            mUsbReceiver = null;
        }
        mJxitUSBPrinter.close();
    }

}
