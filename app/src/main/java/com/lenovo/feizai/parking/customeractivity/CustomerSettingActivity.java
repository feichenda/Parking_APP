package com.lenovo.feizai.parking.customeractivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.MaterialDialog;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.activity.MapActivity;
import com.lenovo.feizai.parking.activity.WebActivity;
import com.lenovo.feizai.parking.base.BaseActivity;
import com.lenovo.feizai.parking.base.BaseModel;
import com.lenovo.feizai.parking.base.BaseObserver;
import com.lenovo.feizai.parking.entity.Customer;
import com.lenovo.feizai.parking.net.ExceptionHandle;
import com.lenovo.feizai.parking.net.RetrofitClient;
import com.lenovo.feizai.parking.util.GsonUtil;
import com.lenovo.feizai.parking.util.ToolUtil;
import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author feizai
 * @date 2021/5/8 0008 上午 10:54:36
 */
public class CustomerSettingActivity extends BaseActivity {

    @BindView(R.id.home_addr)
    TextView home;
    @BindView(R.id.company_addr)
    TextView company;

    private RetrofitClient client;
    private LatLng home_Lat, company_Lat;
    private Boolean isHomeNull, isCompanyNull;

    public CustomerSettingActivity() {
        super(R.layout.activity_customer_setting);
    }

    @Override
    protected void initView() {
        client = RetrofitClient.getInstance(this);
        home_Lat = null;
        company_Lat = null;
        isHomeNull = true;
        isCompanyNull = true;
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

    @OnClick(R.id.home)
    public void home() {
        if (isHomeNull) {
            startActivityForResult(MapActivity.class, 1);
        } else {
            Intent intent = new Intent(CustomerSettingActivity.this, MapActivity.class);
            intent.putExtra("mylat", GsonUtil.GsonString(home_Lat));
            startActivityForResult(intent, 1);
        }
    }

    @OnClick(R.id.company)
    public void company() {
        if (isCompanyNull) {
            startActivityForResult(MapActivity.class, 2);
        } else {
            Intent intent = new Intent(CustomerSettingActivity.this, MapActivity.class);
            intent.putExtra("mylat", GsonUtil.GsonString(company_Lat));
            startActivityForResult(intent, 2);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    String latlng_str = data.getStringExtra("latlng");
                    String result_str = data.getStringExtra("result");
                    home_Lat = GsonUtil.GsonToBean(latlng_str, LatLng.class);
                    ReverseGeoCodeResult result = GsonUtil.GsonToBean(result_str, ReverseGeoCodeResult.class);
                    Customer customer = new Customer();
                    customer.setUsername(ToolUtil.getUsername(this));
                    customer.setHome(result.getAddress());
                    customer.setHome_latitude(home_Lat.latitude);
                    customer.setHome_longitude(home_Lat.longitude);
                    Log.e("tag", customer.toString());
                    client.updatehomeaddressinfo(customer, new BaseObserver<BaseModel>(this) {
                        @Override
                        protected void showDialog() {

                        }

                        @Override
                        protected void hideDialog() {

                        }

                        @Override
                        protected void successful(BaseModel baseModel) {
                            home.setText(result.getAddress());
                            showToast(baseModel.getMessage());
                            isHomeNull = false;
                        }

                        @Override
                        protected void defeated(BaseModel baseModel) {
                            showToast(baseModel.getMessage());
                        }

                        @Override
                        public void onError(ExceptionHandle.ResponeThrowable e) {
                            Logger.e(e, e.getMessage());
                            showToast(e.getMessage());
                        }
                    });
                }
                break;
            case 2:
                if (resultCode == RESULT_OK) {
                    String latlng_str = data.getStringExtra("latlng");
                    String result_str = data.getStringExtra("result");
                    company_Lat = GsonUtil.GsonToBean(latlng_str, LatLng.class);
                    ReverseGeoCodeResult result = GsonUtil.GsonToBean(result_str, ReverseGeoCodeResult.class);
                    Customer customer = new Customer();
                    customer.setUsername(ToolUtil.getUsername(this));
                    customer.setCompany(result.getAddress());
                    customer.setCompany_latitude(company_Lat.latitude);
                    customer.setCompany_longitude(company_Lat.longitude);
                    Log.e("tag", customer.toString());
                    client.updateCompanyAddressInfo(customer, new BaseObserver<BaseModel>(this) {
                        @Override
                        protected void showDialog() {

                        }

                        @Override
                        protected void hideDialog() {

                        }

                        @Override
                        protected void successful(BaseModel baseModel) {
                            company.setText(result.getAddress());
                            showToast(baseModel.getMessage());
                            isCompanyNull = false;
                        }

                        @Override
                        protected void defeated(BaseModel baseModel) {
                            showToast(baseModel.getMessage());
                        }

                        @Override
                        public void onError(ExceptionHandle.ResponeThrowable e) {
                            Logger.e(e, e.getMessage());
                            showToast(e.getMessage());
                        }
                    });
                }
                break;
        }
//        isHomeNull = true;
//        isCompanyNull = true;
//        getData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getData();
    }

    private void getData() {
        client.selectCustomerByUsername(ToolUtil.getUsername(this), new BaseObserver<BaseModel<Customer>>(this) {
            @Override
            protected void showDialog() {

            }

            @Override
            protected void hideDialog() {

            }

            @Override
            protected void successful(BaseModel<Customer> customerBaseModel) {
                Customer customer = customerBaseModel.getData();
                if (TextUtils.isEmpty(customer.getHome())) {
                    home.setText("去设置");
                    isHomeNull = true;
                }else {
                    home.setText(customer.getHome());
                    home_Lat = new LatLng(customer.getHome_latitude(), customer.getHome_longitude());
                    isHomeNull = false;
                }
                if (TextUtils.isEmpty(customer.getCompany())) {
                    company.setText("去设置");
                    isCompanyNull = true;
                } else {
                    company.setText(customer.getCompany());
                    company_Lat = new LatLng(customer.getCompany_latitude(), customer.getCompany_longitude());
                    isCompanyNull = false;
                }
            }

            @Override
            protected void defeated(BaseModel<Customer> customerBaseModel) {
                showToast(customerBaseModel.getMessage());
            }

            @Override
            public void onError(ExceptionHandle.ResponeThrowable e) {
                Logger.e(e, e.getMessage());
                showToast(e.getMessage());
            }
        });
    }
}
