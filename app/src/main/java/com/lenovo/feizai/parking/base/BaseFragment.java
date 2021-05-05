package com.lenovo.feizai.parking.base;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.lenovo.feizai.parking.R;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;

/**
 * @author feizai
 * @date 12/21/2020 021 9:44:14 PM
 */
public abstract class BaseFragment extends Fragment {

    int resLayout;
    //用户同意权限权限申请
    public Map<Integer, Runnable> allowablePermissionRunnables = new HashMap<>();
    //用户拒绝权限申请
    public Map<Integer, Runnable> disallowablePermissionRunnables = new HashMap<>();
    //用户彻底禁止权限申请
    public Map<Integer, Runnable> completebanPermissionRunnables = new HashMap<>();

    public BaseFragment(int resLayout){
        this.resLayout=resLayout;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(resLayout,container,false);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        view.setLayoutParams(lp);
        ButterKnife.bind(this,view);
        Logger.addLogAdapter(new AndroidLogAdapter());
        initView(view);
        return view;
    }

    protected abstract void initView(View view);

    /**
     * 请求权限
     * @param id                   请求授权的id 唯一标识即可
     * @param permissions           请求的权限组
     * @param allowableRunnable    同意授权后的操作，不能为空
     * @param disallowableRunnable 禁止权限后的操作，不能为空
     * @param completebanRunable   彻底禁止权限后的操作，可以为空
     */
    public void requestPermission(int id, String[] permissions, Runnable allowableRunnable, Runnable disallowableRunnable, Runnable completebanRunable) {
        if (allowableRunnable == null) {
            throw new IllegalArgumentException("allowableRunnable == null");
        }

        allowablePermissionRunnables.put(id, allowableRunnable);
//        disallowablePermissionRunnables.put(id, disallowableRunnable);
//        completebanPermissionRunnables.put(id, completebanRunable);
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
                if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                    count++;
                }
            }
            if (count == 0) {
                allowableRunnable.run();
            } else {
                ActivityCompat.requestPermissions(getActivity(), permissions, id);
            }
        } else {
            allowableRunnable.run();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < grantResults.length; i++) {
            boolean isTip = ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permissions[i]);
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                if (isTip) {//表明用户没有彻底禁止弹出权限请求
                    Runnable disallowRun = disallowablePermissionRunnables.get(requestCode);
                    if (disallowRun != null) {
                        disallowRun.run();
                    }else{
                        Toast.makeText(getContext(),"您以禁止获取该权限",Toast.LENGTH_SHORT).show();
                    }
                } else {//表明用户已经彻底禁止弹出权限请求
                    //这里一般会提示用户进入权限设置界面
                    Runnable completebanRun = completebanPermissionRunnables.get(requestCode);
                    if (completebanRun != null) {
                        completebanRun.run();
                    }else{
                        MaterialDialog dialog = new MaterialDialog(getContext(), MaterialDialog.getDEFAULT_BEHAVIOR());
                        dialog.title(null, "警告");
                        dialog.icon(null, getResources().getDrawable(R.drawable.ic_error));
                        dialog.message(null, "跳转到设置以获取权限",dialogMessageSettings -> {return null;});
                        dialog.positiveButton(null, "确认", materialDialog -> {
                            startActivity(getAppDetailSettingIntent());
                            return null;
                        });
                        dialog.negativeButton(null, "取消", materialDialog -> {
                            getActivity().finish();
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

    /**
     * 活动跳转，无返回值
     * @param activityclass 将要跳转的活动的class
     */
    protected void startActivity(Class activityclass) {
        startActivity(new Intent(getActivity(), activityclass));
    }

    /**
     * 活动跳转，有返回值
     * @param activityclass 将要跳转的活动的class
     * @param requsetCode   请求跳转的id 唯一标识符即可
     */
    protected void startActivityForResult(Class activityclass, int requsetCode) {
        startActivityForResult(new Intent(getActivity(), activityclass), requsetCode);
    }

    /*获取应用详情页面intent（如果找不到要跳转的界面，也可以先把用户引导到系统设置页面）*/
    private Intent getAppDetailSettingIntent() {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", getActivity().getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", getActivity().getPackageName());
        }
        return localIntent;
    }

    protected void showToast(String content) {
        Toast.makeText(getContext(),content,Toast.LENGTH_SHORT).show();
    }
}
