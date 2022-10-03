package in.acural.printtest.util;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;


/**
 * 以下为封装好的指令，使用outputstream的write方法往打印机输入指令便可以打印。
 * Created by Rupendra on 2018/8/18.
 */

public class PrintUtils {

    private static OutputStream outputStream = null;

    public static OutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * 设置打印机OutputStream
     *
     * @param outputStream 打印机输出流
     */
    public static void setOutputStream(OutputStream outputStream) {
        PrintUtils.outputStream = outputStream;
    }

    /**
     * 打印文字
     *
     * @param text 要打印的文字
     */
    public static void printText(String text) {
        try {
            byte[] data = text.getBytes("gbk");
            outputStream.write(data, 0, data.length);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 打印一维条码
     *
     * @param text 要打印的条码内容,
     * @param barcodeType 条码类型(0-8,10-12,65-73),HRI字符打印位置(0-3),HRIFont HRI字符字体(0-1),height 条形码高度（0-255）,width 条形码宽度（2-6）
     */
    public static void printBarcode1d(String text, int barcodeType, int HRILocation, int HRIFont, int height, int width) {
        try {
            outputStream.write(new byte[]{0x1d,0x48,(byte) HRILocation});
            Log.i("printBarcode1d",""+(byte) HRILocation);
            outputStream.write(new byte[]{0x1d,0x66,(byte) HRIFont});
            Log.i("printBarcode1d",""+(byte) HRIFont);
            outputStream.write(new byte[]{0x1d,0x68,(byte) height});
            Log.i("printBarcode1d",""+(byte) height);
            outputStream.write(new byte[]{0x1d,0x77,(byte) width});
            Log.i("printBarcode1d",""+(byte) width);
            if((0<=barcodeType && barcodeType<=8) || (10<=barcodeType && barcodeType<=12)){
                outputStream.write(new byte[]{0x1d,0x6b,(byte) barcodeType});
                Log.i("printBarcode1d",""+(byte) barcodeType);
                byte[] data = text.getBytes();
                outputStream.write(data);
                outputStream.write(new byte[]{0x00});
            }else if(65<=barcodeType && barcodeType<=73){
                outputStream.write(new byte[]{0x1d,0x6b,(byte) barcodeType,(byte) text.length()});
                byte[] data = text.getBytes();
                outputStream.write(data);
            }
            outputStream.write(new byte[]{0x1b,0x64,0x02});
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置打印格式
     *
     * @param command 格式指令
     */
    public static void selectCommand(byte[] command) {
        try {
            outputStream.write(command);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 复位打印机
     */
    public static final byte[] RESET = {0x1b, 0x40};

    /**
     * 左对齐
     */
    public static final byte[] ALIGN_LEFT = {0x1b, 0x61, 0x00};

    /**
     * 中间对齐
     */
    public static final byte[] ALIGN_CENTER = {0x1b, 0x61, 0x01};

    /**
     * 右对齐
     */
    public static final byte[] ALIGN_RIGHT = {0x1b, 0x61, 0x02};

    /**
     * 选择加粗模式
     */
    public static final byte[] BOLD = {0x1b, 0x45, 0x01};

    /**
     * 取消加粗模式
     */
    public static final byte[] BOLD_CANCEL = {0x1b, 0x45, 0x00};

    /**
     * 宽高加倍
     */
    public static final byte[] DOUBLE_HEIGHT_WIDTH = {0x1d, 0x21, 0x11};

    /**
     * 3倍宽高
     */
    public static final byte[] TRIPLE_HEIGHT_WIDTH = {0x1d, 0x21, 0x22};

    /**
     * 4倍宽高
     */
    public static final byte[] FOURFOLD_HEIGHT_WIDTH = {0x1d, 0x21, 0x33};

    /**
     * 宽加倍
     */
    public static final byte[] DOUBLE_WIDTH = {0x1d, 0x21, 0x10};

    /**
     * 高加倍
     */
    public static final byte[] DOUBLE_HEIGHT = {0x1d, 0x21, 0x01};

    /**
     * 字体不放大
     */
    public static final byte[] NORMAL = {0x1d, 0x21, 0x00};

    /**
     * 设置默认行间距
     */
    public static final byte[] LINE_SPACING_DEFAULT = {0x1b, 0x32};

    /**
     * 选择下划线模式
     */
    public static final byte[] UNDERLINE = {0x1b, 0x2D, 0x01};

    /**
     * 取消下划线模式
     */
    public static final byte[] UNDERLINE_CANCEL = {0x1b, 0x2D, 0x00};

    /**
     * 打印并进纸一行
     */
    public static final byte[] PRINT_FORMFEED= {0x0a};

    /**
     * 跳到下一水平制表符位置
     */
    public static final byte[] NEXT_LEVEL_TAB = {0x09};

    /**
     * 选择字符字体A
     */
    public static final byte[] CHARACTER_FONT_A = {0x1b,0x4d,0x00};

    /**
     * 选择字符字体B
     */
    public static final byte[] CHARACTER_FONT_B = {0x1b,0x4d,0x01};

    /**
     * 打开90°顺时针旋转模式
     */
    public static final byte[] ROTATE_90 = {0x1b, 0x56, 0x01};

    /**
     * 打开180°顺时针旋转模式
     */
    public static final byte[] ROTATE_180 = {0x1b, 0x56, 0x02};

    /**
     * 打开270°顺时针旋转模式
     */
    public static final byte[] ROTATE_270 = {0x1b, 0x56, 0x03};

    /**
     * 取消90°顺时针旋转模式
     */
    public static final byte[] ROTATE_CANCEL = {0x1b, 0x56, 0x00};

    /**
     * 打开颠倒打印模式
     */
    public static final byte[] REVERSE = {0x1b, 0x7B, 0x01};

    /**
     * 关闭颠倒打印模式
     */
    public static final byte[] REVERSE_CANCEL = {0x1b, 0x7B, 0x00};

    /**
     * 打开白/黑颠倒打印模式
     */
    public static final byte[] WHITE_BLACK_REVERSE = {0x1d, 0x42, 0x01};

    /**
     * 关闭白/黑颠倒打印模式
     */
    public static final byte[] WHITE_BLACK_REVERSE_CANCEL = {0x1d, 0x42, 0x00};





    /**
     * 打印纸一行最大的字节
     */
    private static final int LINE_BYTE_SIZE = 32;

    private static final int LEFT_LENGTH = 20;

    private static final int RIGHT_LENGTH = 12;

    /**
     * 左侧汉字最多显示几个文字
     */
    private static final int LEFT_TEXT_MAX_LENGTH = 8;

    /**
     * 小票打印菜品的名称，上限调到8个字
     */
    public static final int MEAL_NAME_MAX_LENGTH = 8;

    /**
     * 打印两列
     *
     * @param leftText  左侧文字
     * @param rightText 右侧文字
     * @return
     */
    public static String printTwoData(String leftText, String rightText) {
        StringBuilder sb = new StringBuilder();
        int leftTextLength = getBytesLength(leftText);
        int rightTextLength = getBytesLength(rightText);
        sb.append(leftText);

        int marginBetweenMiddleAndRight = LINE_BYTE_SIZE - leftTextLength - rightTextLength;

        for (int i = 0; i < marginBetweenMiddleAndRight; i++) {
            sb.append(" ");
        }
        sb.append(rightText);
        return sb.toString();
    }

    /**
     * 打印三列
     *
     * @param leftText   左侧文字
     * @param middleText 中间文字
     * @param rightText  右侧文字
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
     * 获取数据长度
     *
     * @param msg
     * @return
     */
    private static int getBytesLength(String msg) {
        return msg.getBytes(Charset.forName("GB2312")).length;
    }

    /**
     * 格式化菜品名称，最多显示MEAL_NAME_MAX_LENGTH个数
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


}
