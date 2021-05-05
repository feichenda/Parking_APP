package com.lenovo.feizai.parking.util;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;

/**
 * @author feizai
 * @date 12/31/2020 031 12:00:43 PM
 */
public class DensityUtil {
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int getSreenWidth(Context context) {
//        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        return wm.getDefaultDisplay().getWidth();
        DisplayMetrics dm = context.getApplicationContext()
                .getResources().getDisplayMetrics();
        return dm.widthPixels;
    }
    public static int getSreenHeight(Context context) {
//        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        return wm.getDefaultDisplay().getHeight();
        DisplayMetrics dm = context.getApplicationContext()
                .getResources().getDisplayMetrics();
        return dm.heightPixels;
    }




    /**
     * 获取屏幕宽度和高度，单位为px
     * @param context
     * @return
     */
    public static Point getScreenMetrics(Context context){
        DisplayMetrics dm =context.getResources().getDisplayMetrics();
        int w_screen = dm.widthPixels;
        int h_screen = dm.heightPixels;
        return new Point(w_screen, h_screen);

    }

    /**
     * 获取屏幕长宽比
     * @param context
     * @return
     */
    public static float getScreenRate(Context context){
        Point P = getScreenMetrics(context);
        float H = P.y;
        float W = P.x;
        return (W/H);
    }

    /**

     * sp转px
     *
     * @param context
     * @return

     */

    public static int sp2px(Context context, float spVal)
    {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, context.getResources().getDisplayMetrics());

    }

    /**
     * 获取获取系统状态栏高度。
     *
     * @param appContext APP的上下文
     * @return 状态栏高度
     */
    public static int getStatusBarHeight(Context appContext) {
        int result = 0;
        int resourceId = appContext.getResources().getIdentifier
                ("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = appContext.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


}

