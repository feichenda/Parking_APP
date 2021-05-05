package com.lenovo.feizai.parking.base;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.util.StatusBarUtil;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {

    //布局文件ID
    int resource;
    //用户同意权限权限申请
    public Map<Integer, Runnable> allowablePermissionRunnables = new HashMap<>();
    //用户拒绝权限申请
    public Map<Integer, Runnable> disallowablePermissionRunnables = new HashMap<>();
    //用户彻底禁止权限申请
    public Map<Integer, Runnable> completebanPermissionRunnables = new HashMap<>();

    public BaseActivity(int resLayout) {
        resource = resLayout;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(resource);
        ButterKnife.bind(this);
        StatusBarUtil.setStatusBarMode(this,true, R.color.color_ffffff);
        Logger.addLogAdapter(new AndroidLogAdapter());
        initView();
    }

    //活动初始化自定义操作
    protected abstract void initView();

    /**
     * 活动跳转，无返回值
     * @param activityclass 将要跳转的活动的class
     */
    protected void startActivity(Class activityclass) {
        startActivity(new Intent(this, activityclass));
    }

    /**
     * 活动跳转，有返回值
     * @param activityclass 将要跳转的活动的class
     * @param requsetCode   请求跳转的id 唯一标识符即可
     */
    protected void startActivityForResult(Class activityclass, int requsetCode) {
        startActivityForResult(new Intent(this, activityclass), requsetCode);
    }

    /**
     * 请求权限
     * @param id                   请求授权的id 唯一标识即可
     * @param permissions           请求的权限组
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
                ActivityCompat.requestPermissions(BaseActivity.this, permissions, id);
            }
        } else {
            allowableRunnable.run();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < grantResults.length; i++) {
            boolean isTip = ActivityCompat.shouldShowRequestPermissionRationale(BaseActivity.this, permissions[i]);
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                if (isTip) {//表明用户没有彻底禁止弹出权限请求
                    Runnable disallowRun = disallowablePermissionRunnables.get(requestCode);
                    if (disallowRun != null) {
                        disallowRun.run();
                    }else{
                        Toast.makeText(this,"您以禁止获取该权限",Toast.LENGTH_SHORT).show();
                    }
                } else {//表明用户已经彻底禁止弹出权限请求
                    //这里一般会提示用户进入权限设置界面
                    Runnable completebanRun = completebanPermissionRunnables.get(requestCode);
                    if (completebanRun != null) {
                        completebanRun.run();
                    }else{
                        MaterialDialog dialog = new MaterialDialog(this, MaterialDialog.getDEFAULT_BEHAVIOR());
                        dialog.title(null, "警告");
                        dialog.icon(null, getResources().getDrawable(R.drawable.ic_error));
                        dialog.message(null, "跳转到设置以获取权限",dialogMessageSettings -> {return null;});
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
                }else {
                    throw  new NullPointerException("allowRun == null");
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

    protected void showToast(String content) {
        Toast.makeText(BaseActivity.this,content,Toast.LENGTH_SHORT).show();
    }
}
