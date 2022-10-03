package in.acural.printtest.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * 项目名称：MakeScanCodePrintJar
 * 类描述：bitmap转换工具
 * 创建人：任俊杰
 * 创建时间：2018-01-11 上午 10:52
 * 修改人：任俊杰
 * 修改时间：2018-01-11 上午 10:52
 * 修改备注：
 */

public class BitmapTools {
    /**
     * view转Bitmap
     *
     * @param view 视图
     * @return bitmap
     */
    public static Bitmap view2Bitmap(final View view) {
        if (view == null) return null;
        Bitmap ret = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(ret);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            bgDrawable.draw(canvas);
        } else {
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return ret;
    }

    /**
     * 获取单色位图
     */
    public static Bitmap getSinglePic(Bitmap inputBMP) {
        int[] pix = new int[inputBMP.getWidth() * inputBMP.getHeight()];
        int[] colorTemp = new int[inputBMP.getWidth() * inputBMP.getHeight()];
        inputBMP.getPixels(pix, 0, inputBMP.getWidth(), 0, 0,
                inputBMP.getWidth(), inputBMP.getHeight());
        Bitmap returnBMP = Bitmap.createBitmap(inputBMP.getWidth(),
                inputBMP.getHeight(), Bitmap.Config.RGB_565);
        int lightNumber = 127;// 曝光度，這個顔色是中間值，如果大於中間值，那就是黑色，否則白色，数值越小，曝光度越高
//		for (int j = 0; j < colorTemp.length; j++) {
//			// 将颜色数组中的RGB值取反，255减去当前颜色值就获得当前颜色的反色
//			// 網上的，但是我要進行曝光處理，使他變成單色圖
//			colorTemp[j] = Color.rgb(Color.red(pix[j]) > lightNumber ? 255 : 0,
//					Color.green(pix[j]) > lightNumber ? 255 : 0,
//					Color.blue(pix[j]) > lightNumber ? 255 : 0);
//		}
        for (int j = 0; j < colorTemp.length; j++) {
            colorTemp[j] = Color.rgb(Color.red(pix[j]), Color.green(pix[j]),
                    Color.blue(pix[j]));
        }
        for (int i = 0; i < colorTemp.length; i++) {
            // 這裏需要思考一下，上一步有可能得到：純紅，純黃，純藍，黑色，白色這樣5種顔色，前三種是應該變成白色還是黑色呢？
            // 發現這是一個很複雜的問題，涉及到不同區域閒顔色的對比，如果是黑色包圍紅色，那紅色就應該是白色，反之變成黑色。。。
            // 似乎衹能具體問題具體分析，這裏就先把黃色設成白色，藍色=白色，紅色=黑色
            int r = Color.red(pix[i]);
            int g = Color.green(pix[i]);
            int b = Color.blue(pix[i]);
            // 有兩種顔色以上的混合，那就是變成黑色但目前这种方法，对于黑白的曝光效果更出色，
            // 原理是设置一个曝光值，然后三种颜色相加大于3倍的曝光值，才是黑色，否则白色
            if (r + g + b > 3 * lightNumber) {
                colorTemp[i] = Color.rgb(255, 255, 255);
            } else {
                colorTemp[i] = Color.rgb(0, 0, 0);
            }
        }
        returnBMP.setPixels(colorTemp, 0, inputBMP.getWidth(), 0, 0,
                inputBMP.getWidth(), inputBMP.getHeight());
        return returnBMP;
    }
}
