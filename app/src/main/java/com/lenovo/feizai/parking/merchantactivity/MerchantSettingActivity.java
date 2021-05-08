package com.lenovo.feizai.parking.merchantactivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import com.afollestad.materialdialogs.MaterialDialog;
import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.activity.WebActivity;
import com.lenovo.feizai.parking.base.BaseActivity;
import butterknife.OnClick;

/**
 * @author feizai
 * @date 2021/5/8 0008 上午 10:54:36
 */
public class MerchantSettingActivity extends BaseActivity {

    public MerchantSettingActivity() {
        super(R.layout.activity_merchant_setting);
    }

    @Override
    protected void initView() {

    }

    @OnClick(R.id.back)
    public void back() {
        finish();
    }

    @OnClick(R.id.about)
    public void about() {
        MaterialDialog dialog = new MaterialDialog(this, MaterialDialog.getDEFAULT_BEHAVIOR());
        try {
            String version = getPackageManager().getPackageInfo(getPackageName(), getPackageManager().GET_CONFIGURATIONS).versionName;
            dialog.icon(null, getDrawable(R.drawable.ic_about));
            dialog.title(null, "关于");
            dialog.message(null, "版本号:" + version, null);
            dialog.positiveButton(null, "确定", materialDialog -> {
                return null;
            });
            dialog.show();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            dialog.icon(null, getDrawable(R.drawable.ic_error));
            dialog.title(null, "错误");
            dialog.message(null, "版本号获取失败", null);
            dialog.positiveButton(null, "确定", materialDialog -> {
                return null;
            });
            dialog.show();
        }
    }

    @OnClick(R.id.authority)
    public void authority() {
        startActivity(getAppDetailSettingIntent());
    }

    @OnClick(R.id.privacy)
    public void privacy() {
        startActivity(WebActivity.class);
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
}
