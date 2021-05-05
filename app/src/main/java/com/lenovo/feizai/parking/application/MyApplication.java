package com.lenovo.feizai.parking.application;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBaiduNaviManager;
import com.baidu.navisdk.adapter.struct.BNTTsInitConfig;
import com.lenovo.feizai.parking.util.BNDemoUtils;

import java.io.File;

/**
 * @author feizai
 * @date 12/21/2020 021 9:41:59 PM
 */
public class MyApplication extends Application {
    static MyApplication application;
    public int appCount;

    @Override
    public void onCreate() {
        super.onCreate();
        application=this;
        SDKInitializer.initialize(getApplicationContext());
        //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL);
        initNavi();
    }

    private void initNavi() {
        // 针对单次有效的地图设置项 - DemoNaviSettingActivity
        BNDemoUtils.setBoolean(this, BNDemoUtils.KEY_gb_routeSort, true);
        BNDemoUtils.setBoolean(this, BNDemoUtils.KEY_gb_routeSearch, true);
        BNDemoUtils.setBoolean(this, BNDemoUtils.KEY_gb_moreSettings, true);

        if (BaiduNaviManagerFactory.getBaiduNaviManager().isInited()) {
            return;
        }

        BaiduNaviManagerFactory.getBaiduNaviManager().init(this,
                getExternalFilesDir(null).getPath(),
                "Parking", new IBaiduNaviManager.INaviInitListener() {

                    @Override
                    public void onAuthResult(int status, String msg) {
                        String result;
                        if (0 == status) {
                            result = "key校验成功!";
                        } else {
                            result = "key校验失败, " + msg;
                        }
                        Log.e("TAG", result);
                    }

                    @Override
                    public void initStart() {
                        Log.e("TAG", "initStart");
                    }

                    @Override
                    public void initSuccess() {
                        Log.e("TAG", "initSuccess");
                        BaiduNaviManagerFactory.getBaiduNaviManager().enableOutLog(true);
                        String cuid = BaiduNaviManagerFactory.getBaiduNaviManager().getCUID();
                        Log.e("TAG", "cuid = " + cuid);
                        // 初始化tts
                        initTTS();
                        sendBroadcast(new Intent("com.navi.ready"));
                    }

                    @Override
                    public void initFailed(int errCode) {
                        Log.e("TAG", "initFailed-" + errCode);
                    }
                });
    }

    private void initTTS() {
        // 使用内置TTS
        BNTTsInitConfig config = new BNTTsInitConfig.Builder()
                .context(getApplicationContext())
                .sdcardRootPath(getSdcardDir())
                .appFolderName("Parking")
                .appId(BNDemoUtils.getTTSAppID())
                .appKey(BNDemoUtils.getTTSAppKey())
                .secretKey(BNDemoUtils.getTTSsecretKey())
                .build();
        BaiduNaviManagerFactory.getTTSManager().initTTS(config);
    }

    private String getSdcardDir() {
        if (Build.VERSION.SDK_INT >= 29) {
            // 如果外部储存可用 ,获得外部存储路径
            File file = getExternalFilesDir(null);
            if (file != null && file.exists()) {
                return file.getPath();
            } else {
                return getFilesDir().getPath();
            }
        } else {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        }
    }

    /**
     * 获取app缓存路径
     * 先获取外部存储 外部存储不可用用内部存储  没有使用sd卡权限的使用内部缓存
     *
     * @return
     */
    public String getCachePath() {
        String cachePath;
        int checkCallPhonePermission = 1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //检查是否拥有权限
            checkCallPhonePermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkCallPhonePermission == PackageManager.PERMISSION_GRANTED) {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
                //外部存储可用
                cachePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "cmodichina" + File.separator + "medicaliot";
                //设置磁盘缓存目录（和创建的缓存目录相同）
                File file = new File(cachePath);
                if (!file.exists()) {
                    file.mkdirs();
                }
            } else {
                //外部存储不可用
                cachePath = getApplication().getCacheDir().getPath();
            }
        } else {
            cachePath = getCacheDirectory(getApplicationContext(), null);
        }
        return cachePath;
    }

    /**
     * 获取app图片缓存路径
     *
     * @return
     */
    public String getImageCachePath(Context context, String type) {
        String path = getCacheDirectory(context, type) + File.separator + "img";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return path;
    }


    public static MyApplication getApplication() {
        return application;
    }

    /**
     * 获取应用专属缓存目录
     * android 4.4及以上系统不需要申请SD卡读写权限
     * 因此也不用考虑6.0系统动态申请SD卡读写权限问题，切随应用被卸载后自动清空 不会污染用户存储空间
     *
     * @param context 上下文
     * @param type    文件夹类型 可以为空，为空则返回API得到的一级目录
     * @return 缓存文件夹 如果没有SD卡或SD卡有问题则返回内存缓存目录，否则优先返回SD卡缓存目录
     */
    public static String getCacheDirectory(Context context, String type) {
        File appCacheDir = getExternalCacheDirectory(context, type);
        if (appCacheDir == null) {
            appCacheDir = getInternalCacheDirectory(context, type);
        }

        if (appCacheDir == null) {
            Log.e("getCacheDirectory", "getCacheDirectory fail ,the reason is mobile phone unknown exception !");
        } else {
            if (!appCacheDir.exists() && !appCacheDir.mkdirs()) {
                Log.e("getCacheDirectory", "getCacheDirectory fail ,the reason is make directory fail !");
            }
        }
        return appCacheDir.getAbsolutePath();
    }

    public String getDownLoadCacheDirectory() {
        return getCachePath() + File.separator + "download" + File.separator;
    }

    /**
     * 获取SD卡缓存目录
     *
     * @param context 上下文
     * @param type    文件夹类型 如果为空则返回 /storage/emulated/0/Android/data/app_package_name/cache
     *                否则返回对应类型的文件夹如Environment.DIRECTORY_PICTURES 对应的文件夹为 .../data/app_package_name/files/Pictures
     *                {@link Environment#DIRECTORY_MUSIC},
     *                {@link Environment#DIRECTORY_PODCASTS},
     *                {@link Environment#DIRECTORY_RINGTONES},
     *                {@link Environment#DIRECTORY_ALARMS},
     *                {@link Environment#DIRECTORY_NOTIFICATIONS},
     *                {@link Environment#DIRECTORY_PICTURES}, or
     *                {@link Environment#DIRECTORY_MOVIES}.or 自定义文件夹名称
     * @return 缓存目录文件夹 或 null（无SD卡或SD卡挂载失败）
     */
    public static File getExternalCacheDirectory(Context context, String type) {
        File appCacheDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            if (TextUtils.isEmpty(type)) {
                appCacheDir = context.getExternalCacheDir();
            } else {
                appCacheDir = context.getExternalFilesDir(type);
            }

            if (appCacheDir == null) {// 有些手机需要通过自定义目录
                appCacheDir = new File(Environment.getExternalStorageDirectory(), "Android/data/" + context.getPackageName() + "/cache/" + type);
            }

            if (appCacheDir == null) {
                Log.e("getExternalDirectory", "getExternalDirectory fail ,the reason is sdCard unknown exception !");
            } else {
                if (!appCacheDir.exists() && !appCacheDir.mkdirs()) {
                    Log.e("getExternalDirectory", "getExternalDirectory fail ,the reason is make directory fail !");
                }
            }
        } else {
            Log.e("getExternalDirectory", "getExternalDirectory fail ,the reason is sdCard nonexistence or sdCard mount fail !");
        }
        return appCacheDir;
    }

    /**
     * 获取内存缓存目录
     *
     * @param type 子目录，可以为空，为空直接返回一级目录
     * @return 缓存目录文件夹 或 null（创建目录文件失败）
     * 注：该方法获取的目录是能供当前应用自己使用，外部应用没有读写权限，如 系统相机应用
     */
    public static File getInternalCacheDirectory(Context context, String type) {
        File appCacheDir = null;
        if (TextUtils.isEmpty(type)) {
            appCacheDir = context.getCacheDir();// /data/data/app_package_name/cache
        } else {
            appCacheDir = new File(context.getFilesDir(), type);// /data/data/app_package_name/files/type
        }

        if (!appCacheDir.exists() && !appCacheDir.mkdirs()) {
            Log.e("getInternalDirectory", "getInternalDirectory fail ,the reason is make directory fail !");
        }
        return appCacheDir;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

}

