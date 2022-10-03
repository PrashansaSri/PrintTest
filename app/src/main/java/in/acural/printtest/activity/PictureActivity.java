package in.acural.printtest.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.jxit.jxitbluetoothprintersdk1_6.Jxit_esc;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import in.acural.printtest.R;


public class PictureActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int PHOTO_REQUEST_CAREMA = 441;
    private static final int PHOTO_REQUEST_GALLERY = 442;
    private static final int PHOTO_REQUEST_CUT = 443;
    private static final int CAMERA_OK = 444;
    private static final int PRINT_OK = 445;
    private static final int PRINT_PICTURE_FAILD = 446;
    private static final int CONNECT_DEVICE_FAILD = 447;

    private Toolbar toolbar;
    private ImageView iv_image;
    private Button mDownloadBtn,mPrintBtn;
    private Spinner mSizeSp;
    private List<String> mSizeList;
    private Bitmap tmpBitmap;
    private Bitmap srcBitmap;

    private String address;
    private static final String PHOTO_FILE_NAME = "temp_photo.jpg";
    private File tempFile;

    private Jxit_esc mJxit_esc;

    /**
     * Handler
     */
    private Handler mPictureHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CONNECT_DEVICE_FAILD:
                    Toast.makeText(PictureActivity.this,"Connect Failed！",Toast.LENGTH_SHORT).show();
                    if (mJxit_esc.isConnected()) {
                        mJxit_esc.close();
                    }
                    mPrintBtn.setEnabled(true);
                    break;
                case PRINT_PICTURE_FAILD:
                    Toast.makeText(PictureActivity.this,"Send Failed！",Toast.LENGTH_SHORT).show();
                    if (mJxit_esc.isConnected()) {
                        mJxit_esc.close();
                    }
                    mPrintBtn.setEnabled(true);
                    break;
                case PRINT_OK:
                    Toast.makeText(PictureActivity.this,"Print Successed！",Toast.LENGTH_SHORT).show();
                    if (mJxit_esc.isConnected()) {
                        mJxit_esc.close();
                    }
                    mPrintBtn.setEnabled(true);
                    break;
                default:
                    break;
            }
        }
    };


    /**
     * onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        initView();

        initData();
    }

    /**
     * initView
     */
    private void initView(){
        initToolbar();

        iv_image = (ImageView) findViewById(R.id.iV);
        setDefaultBitmap();

        mDownloadBtn = (Button) findViewById(R.id.download_btn);
        mPrintBtn = (Button) findViewById(R.id.print_btn);
        mDownloadBtn.setOnClickListener(this);
        mPrintBtn.setOnClickListener(this);

        mSizeSp = (Spinner) findViewById(R.id.size_spinner) ;
        mSizeList = new ArrayList<>();
        mSizeList.add("2inch 384Dot");
        mSizeList.add("3inch 576Dots");


        ArrayAdapter<String> macAdapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,mSizeList);
        macAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        mSizeSp.setAdapter(macAdapter);
        mSizeSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i("mSizeList", mSizeList.get(i));
                switch (mSizeList.get(i)){
                    case "2inch 384Dot":
                        tmpBitmap = setImgSize(srcBitmap,380);
                        tmpBitmap =convertToBlackWhite(tmpBitmap);
                        iv_image.setImageBitmap(tmpBitmap);

                        break;
                    case "3inch 576Dots":
                        tmpBitmap = setImgSize(srcBitmap,560);
                        tmpBitmap =convertToBlackWhite(tmpBitmap);
                        iv_image.setImageBitmap(tmpBitmap);
//                        iv_image.setDrawingCacheEnabled(true);
//                        tmpBitmap = setImgSize(iv_image.getDrawingCache(),576,288);
//                        Log.i("3寸机型576点宽","宽："+tmpBitmap.getWidth()+"高："+tmpBitmap.getHeight());
//                        iv_image.setDrawingCacheEnabled(false);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setDefaultBitmap() {
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(getAssets().open("acrural_n1121.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        srcBitmap = BitmapFactory.decodeStream(bis);
        if(srcBitmap.getWidth() > 380){
            tmpBitmap = setImgSize(srcBitmap,380);
        }
        iv_image.setImageBitmap(tmpBitmap);
    }

    /**
     * initToolbar
     */
    private void initToolbar(){
        toolbar = (Toolbar)findViewById(R.id.picture_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    /**
     * initData
     */
    private void initData() {
        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();
        address = bundle.getString("btDeviceAddress");

        mJxit_esc = Jxit_esc.getInstance();
    }

    /**
     * onClick
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.print_btn:
                Log.i("onClick","print_btn");
                mPrintBtn.setEnabled(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(mJxit_esc.isConnected()) {
                            mJxit_esc.close();
                        }
                        if (!mJxit_esc.connectDevice(address)) {
                            mPictureHandler.sendEmptyMessage(CONNECT_DEVICE_FAILD);
                        } else {
                            if (!mJxit_esc.esc_bitmap_mode(1, tmpBitmap)) {
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                mPictureHandler.sendEmptyMessage(PRINT_PICTURE_FAILD);
                            }else {
                                if (!mJxit_esc.esc_print_formfeed_row(5)) {
                                    try {
                                        Thread.sleep(5000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    mPictureHandler.sendEmptyMessage(PRINT_PICTURE_FAILD);
                                }else {
                                    try {
                                        Thread.sleep(5000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    mPictureHandler.sendEmptyMessage(PRINT_OK);
                                }
                            }
                        }
                    }
                }).start();
//                if (Build.VERSION.SDK_INT>22){
//                    if (ContextCompat.checkSelfPermission(PictureActivity.this,android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
//                        ActivityCompat.requestPermissions(PictureActivity.this,new String[]{android.Manifest.permission.CAMERA},CAMERA_OK);
//                    }else {
//                        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
//                        if (hasSdcard()) {
//                            tempFile = new File(Environment.getExternalStorageDirectory(),PHOTO_FILE_NAME);
//                            Uri uri = Uri.fromFile(tempFile);
//                            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//                        }
//                        startActivityForResult(intent, PHOTO_REQUEST_CAREMA);
//                    }
//                }else {
//                    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
//                    if (hasSdcard()) {
//                        tempFile = new File(Environment.getExternalStorageDirectory(),PHOTO_FILE_NAME);
//                        Uri uri = Uri.fromFile(tempFile);
//                        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//                    }
//                    startActivityForResult(intent, PHOTO_REQUEST_CAREMA);
//                }
                break;
            case R.id.download_btn:
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
                break;
            default:
                break;
        }
    }

    /**
     * onRequestPermissionsResult
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case CAMERA_OK:
                if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                    if (hasSdcard()) {
                        tempFile = new File(Environment.getExternalStorageDirectory(),PHOTO_FILE_NAME);
                        Uri uri = Uri.fromFile(tempFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    }
                    startActivityForResult(intent, PHOTO_REQUEST_CAREMA);
                }else {
                    Toast.makeText(PictureActivity.this,
                            "Please turn on camera permissions manually",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    /**
     * hasSdcard
     */
    private boolean hasSdcard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * onActivityResult
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHOTO_REQUEST_GALLERY && resultCode == RESULT_OK) {
            ContentResolver resolver = getContentResolver();
            try {
                Uri originalUri = data.getData();
                srcBitmap = MediaStore.Images.Media.getBitmap(resolver, originalUri);
                if(srcBitmap.getWidth() > 380){
                    tmpBitmap = setImgSize(srcBitmap,380);
                }
                tmpBitmap =convertToBlackWhite(tmpBitmap);

                //显得到bitmap图片
                iv_image.setImageBitmap(tmpBitmap);
                mSizeSp.setSelection(0,true);

//
//                        null);
//
//                int column_index = cursor
//                        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//
//                cursor.moveToFirst();
//
//                String path = cursor.getString(column_index);
//                imgPath.setText(path);
            } catch (IOException e) {
                Log.e("getImg", e.toString());

            }
//            if (data != null) {
//                Uri uri = data.getData();
//                Bitmap bitmap = null;
//                try {
//                    bitmap = getBitmapFormUri(this,uri);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    return;
//                }
//                tmpBitmap = testSketch.testGaussBlur(bitmap, 10, 10 / 3);
//                iv_image.setImageBitmap(tmpBitmap);
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mJxit_esc = Jxit_esc.getInstance();
//                        if (!mJxit_esc.connectDevice(address)) {
//                        } else {
//                            if (!mJxit_esc.esc_bitmap_mode(0, tmpBitmap))
//                            else {
//                                if (!mJxit_esc.esc_print_text("\n\n"))
//                            }
//                        }
//                        mJxit_esc.close();
//                    }
//                }).start();
//            }
//        } else if (requestCode == PHOTO_REQUEST_CAREMA && resultCode == RESULT_OK) {
//            if (hasSdcard()) {
//                crop(Uri.fromFile(tempFile));
//            } else {
//            }
//        } else if (requestCode == PHOTO_REQUEST_CUT && resultCode == RESULT_OK) {
//            if (data != null) {
//                Bitmap bitmap = data.getParcelableExtra("data");
//                final Bitmap mBitmap = testSketch.testGaussBlur(bitmap, 5, 5 / 3);
//                iv_image.setImageBitmap(mBitmap);
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mJxit_esc = Jxit_esc.getInstance();
//                        if(!mJxit_esc.connectDevice(address)) {
//                        }
//                        else {
//                            if (!mJxit_esc.esc_bitmap_mode(0, mBitmap))
//                            else {
//                                if (!mJxit_esc.esc_print_text("\n\n"))
//                            }
//                        }
//                        mJxit_esc.close();
//                    }
//                }).start();
//            }
            try {
//                tempFile.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * getBitmapFormUri
     */
    public static Bitmap getBitmapFormUri(AppCompatActivity ac, Uri uri) throws IOException {
        InputStream input = ac.getContentResolver().openInputStream(uri);
        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ALPHA_8;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        if (input != null) {
            input.close();
        }
        int originalWidth = onlyBoundsOptions.outWidth;
        int originalHeight = onlyBoundsOptions.outHeight;
        if ((originalWidth == -1) || (originalHeight == -1))
            return null;
        float hh = 320f;
        float ww = 320f;
        int be = 1;
        if (originalWidth > originalHeight && originalWidth > ww) {
            be = (int) (originalWidth / ww);
        } else if (originalWidth < originalHeight && originalHeight > hh) {
            be = (int) (originalHeight / hh);
        }
        if (be <= 0)
            be = 1;
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = be;
        bitmapOptions.inDither = true;
        bitmapOptions.inPreferredConfig = Bitmap.Config.ALPHA_8;
        input = ac.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();

        return compressImage(bitmap);
    }

    /**
     * compressImage
     */
    public static Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {
            baos.reset();
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            options -= 10;
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }

    /**
     * crop
     */
    private void crop(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1.5);
        intent.putExtra("outputX", 180);
        intent.putExtra("outputY", 320);
        intent.putExtra("scale", true);
        intent.putExtra("circleCrop",false);
        intent.putExtra("outputFormat", "JPEG");
        intent.putExtra("noFaceDetection", true);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }

    /**
     *
     *
     * @param bmp
     * @return
     */
    public static Bitmap convertToBlackWhite(Bitmap bmp) {
        int width = bmp.getWidth(); //
        int height = bmp.getHeight(); //
        int[] pixels = new int[width * height]; //
        int SumGray = 0;
        int mGray = 0;
        int eGray = 0;
        int nGray = 0;

        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];

                //
                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);

                //
                grey = (int) (red * 0.3 + green * 0.59 + blue * 0.11);
                //grey = alpha | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + j] = grey;
                SumGray += grey;
                nGray++;
            }
        }
        mGray = SumGray / nGray;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];
                if(grey > mGray)
                {
                    pixels[width * i + j] = 0xFFFFFFFF;
                    eGray = grey - 255;
                }
                else
                {
                    pixels[width * i + j] = 0x00000000;
                    eGray = grey - 0;
                }
                if (j<width-1 &&i<height-1) {
                    pixels[width*i+j+1] += 3*eGray/8;//
                    pixels[width*(i+1)+j] += 3*eGray/8;//
                    pixels[width*(i+1)+j+1] += eGray/4;//
                }
                else if (j==width-1 && i<height-1) //
                {
                    pixels[width*(i+1)+j]+=3*eGray/8;//
                }
                else if (j<width-1 && i==height-1) //
                {
                    pixels[width*(i)+j+1]+=eGray/4;
                }
            }
        }

        Bitmap newBmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        newBmp.setPixels(pixels, 0, width, 0, 0, width, height);

//        return resizeBmp;
        return newBmp;
    }

    public Bitmap setImgSize(Bitmap bm, int newWidth){
        int width = bm.getWidth();
        int height = bm.getHeight();
        Log.i("width", String.valueOf(width));
        Log.i("height", String.valueOf(height));

        double a= newWidth;
        double b= width;
        double c= height;
        int newHeight = (int) ((a/b)*c);
        Log.i("newWidth", String.valueOf(newWidth));
        Log.i("newHeight", String.valueOf(newHeight));
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newbm;
    }

}
