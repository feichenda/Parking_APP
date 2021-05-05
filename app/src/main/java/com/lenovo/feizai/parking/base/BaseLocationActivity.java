package com.lenovo.feizai.parking.base;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBaiduNaviManager;
import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.listener.MyOrientationListener;
import com.lenovo.feizai.parking.util.StatusBarUtil;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 基本位置activity，默认需要获取位置和读写权限，不给则结束该activity
 *
 * @author feizai
 * @date 12/25/2020 025 12:04:54 AM
 */
public abstract class BaseLocationActivity extends AppCompatActivity {

    //布局文件ID
    int resource;
    //用户同意权限权限申请
    public Map<Integer, Runnable> allowablePermissionRunnables = new HashMap<>();
    //用户拒绝权限申请
    public Map<Integer, Runnable> disallowablePermissionRunnables = new HashMap<>();
    //用户彻底禁止权限申请
    public Map<Integer, Runnable> completebanPermissionRunnables = new HashMap<>();

    LocationClient locationClient;

    MyOrientationListener myOrientationListener;

    //方向
    float mCurrentX;

    String[] permissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    public BaseLocationActivity(int resLayout) {
        resource = resLayout;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationClient = new LocationClient(getApplicationContext());
        locationClient.registerLocationListener(new MyLocationListener());
        myOrientationListener = new MyOrientationListener(getApplicationContext());
        SDKInitializer.initialize(getApplicationContext());
        setContentView(resource);
        StatusBarUtil.setStatusBarMode(this,true, R.color.color_ffffff);
        Logger.addLogAdapter(new AndroidLogAdapter());
        ButterKnife.bind(this);
        requestPermission(1, permissions, new Runnable() {
            @Override
            public void run() {
                initView();
                initMap();
            }
        }, new Runnable() {
            @Override
            public void run() {
                MaterialDialog dialog = new MaterialDialog(BaseLocationActivity.this, MaterialDialog.getDEFAULT_BEHAVIOR());
                dialog.title(null, "警告");
                dialog.icon(null, getResources().getDrawable(R.drawable.ic_error));
                dialog.message(null, "您已禁止获取位置和读写权限，将无法使用本APP,是否去设置开启权限", dialogMessageSettings -> {
                    return null;
                });
                dialog.positiveButton(null, "确认", materialDialog -> {
                    startActivity(getAppDetailSettingIntent());
                    return null;
                });
                dialog.negativeButton(null, "取消", materialDialog -> {
                    finish();
                    return null;
                });
                dialog.show();
            }
        }, null);

    }

    //活动初始化自定义操作
    protected abstract void initView();

    //在地图上标点
    protected abstract void setMapOverlay(LatLng point);

    //地图初始化,不需要用地图时可以不写
    protected abstract void initMap();

    //实时定位函数回调，在这里可以实时获取到最新的位置数据
    protected abstract void navigateTo(BDLocation location);

    //开启位置及方向请求
    public void requestLocation() {
        initLocation();
        locationClient.start();
        //方向监听
        myOrientationListener.start();
        myOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                mCurrentX = x;
            }
        });
    }

    //初始化位置监听客户端的设置
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(1000);//设置扫描时间间隔，单位ms
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//设置高精度定位模式
        option.setIsNeedAddress(true);//是否需要地址信息
        option.setOpenGps(true);//是否开启GPS
        option.setCoorType("bd09ll");//设置经纬度格式
        option.setEnableSimulateGps(true);//是否过滤GPS仿真数据
        locationClient.setLocOption(option);
    }

    /**
     * 活动跳转，无返回值
     *
     * @param activityclass 将要跳转的活动的class
     */
    protected void startActivity(Class activityclass) {
        startActivity(new Intent(this, activityclass));
    }

    /**
     * 活动跳转，有返回值
     *
     * @param activityclass 将要跳转的活动的class
     * @param requsetCode   请求跳转的id 唯一标识符即可
     */
    protected void startActivityForResult(Class activityclass, int requsetCode) {
        startActivityForResult(new Intent(this, activityclass), requsetCode);
    }

    /**
     * 请求权限
     *
     * @param id                   请求授权的id 唯一标识即可
     * @param permissions          请求的权限组
     * @param allowableRunnable    同意授权后的操作，不能为空
     * @param disallowableRunnable 禁止权限后的操作，可以为空，为空默认操作弹Toast
     * @param completebanRunable   彻底禁止权限后的操作，可以为空，为空默认操作询问是否到设置里打开权限，确认这跳转，取消则结束activity
     */
    public void requestPermission(int id, String[] permissions, Runnable allowableRunnable, Runnable disallowableRunnable, Runnable completebanRunable) {
        if (allowableRunnable == null) {
            throw new IllegalArgumentException("allowableRunnable == null");
        }

        allowablePermissionRunnables.put(id, allowableRunnable);
        if (disallowableRunnable != null) {
            disallowablePermissionRunnables.put(id, disallowableRunnable);
        }
        if (completebanRunable != null) {
            completebanPermissionRunnables.put(id, completebanRunable);
        }
        //版本判断
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //检查是否拥有权限
            int count = 0;
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                    count++;
                }
            }
            if (count == 0) {
                allowableRunnable.run();
            } else {
                ActivityCompat.requestPermissions(BaseLocationActivity.this, permissions, id);
            }
        } else {
            allowableRunnable.run();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < grantResults.length; i++) {
            boolean isTip = ActivityCompat.shouldShowRequestPermissionRationale(BaseLocationActivity.this, permissions[i]);
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                if (isTip) {//表明用户没有彻底禁止弹出权限请求
                    Runnable disallowRun = disallowablePermissionRunnables.get(requestCode);
                    if (disallowRun != null) {
                        disallowRun.run();
                    } else {
                        showToast("您以禁止获取该权限");
                    }
                } else {//表明用户已经彻底禁止弹出权限请求
                    //这里一般会提示用户进入权限设置界面
                    Runnable completebanRun = completebanPermissionRunnables.get(requestCode);
                    if (completebanRun != null) {
                        completebanRun.run();
                    } else {
                        MaterialDialog dialog = new MaterialDialog(this, MaterialDialog.getDEFAULT_BEHAVIOR());
                        dialog.title(null, "警告");
                        dialog.icon(null, getResources().getDrawable(R.drawable.ic_error));
                        dialog.message(null, "跳转到设置以获取权限", dialogMessageSettings -> {
                            return null;
                        });
                        dialog.positiveButton(null, "确认", materialDialog -> {
                            startActivity(getAppDetailSettingIntent());
                            return null;
                        });
                        dialog.negativeButton(null, "取消", materialDialog -> {
                            finish();
                            return null;
                        });
                        dialog.show();
                    }
                }
                return;
            } else {
                Runnable allowRun = allowablePermissionRunnables.get(requestCode);
                if (allowRun != null) {
                    allowRun.run();
                } else {
                    throw new NullPointerException("allowRun == null");
                }
            }
        }
    }

    /*获取应用详情页面intent（如果找不到要跳转的界面，也可以先把用户引导到系统设置页面）*/
    private Intent getAppDetailSettingIntent() {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
        }
        return localIntent;
    }

    private class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null) {
                return;
            }
            if (location.getLocType() == BDLocation.TypeGpsLocation || location.getLocType() == BDLocation.TypeNetWorkLocation) {
                navigateTo(location);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationClient.stop();
        myOrientationListener.stop();
    }

    public float getmCurrentX() {
        return mCurrentX;
    }

    protected void showToast(String content) {
        Toast.makeText(BaseLocationActivity.this, content, Toast.LENGTH_SHORT).show();
    }

}
