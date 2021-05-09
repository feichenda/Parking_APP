package com.lenovo.feizai.parking.customeractivity;

import android.content.Intent;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.base.BaseActivity;
import com.lenovo.feizai.parking.base.BaseModel;
import com.lenovo.feizai.parking.base.BaseObserver;
import com.lenovo.feizai.parking.entity.CarInfo;
import com.lenovo.feizai.parking.entity.CheckInfo;
import com.lenovo.feizai.parking.net.ExceptionHandle;
import com.lenovo.feizai.parking.net.RetrofitClient;
import com.lenovo.feizai.parking.util.GsonUtil;
import com.lenovo.feizai.parking.util.ToolUtil;
import com.pxy.LicensePlateView;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author feizai
 * @date 2021/5/9 0009 上午 9:11:17
 */
public class EnterCarLicenseActivity extends BaseActivity {

    @BindView(R.id.activity_lpv)
    LicensePlateView plateView;
    @BindView(R.id.main_rl_container)
    RelativeLayout container;
    @BindView(R.id.isNewCar)
    CheckBox isNewCar;
    @BindView(R.id.title)
    TextView title;

    private Boolean check;
    private String license;
    private RetrofitClient client;

    public EnterCarLicenseActivity() {
        super(R.layout.layout_car_license);
    }

    @Override
    protected void initView() {
        title.setText("输入车牌号码");
        client = RetrofitClient.getInstance(this);

        check = false;
        license = new String();

        plateView.setKeyboardContainerLayout(container);

        isNewCar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    plateView.showLastView();
                } else {
                    plateView.hideLastView();
                }
            }
        });

        plateView.setInputListener(new LicensePlateView.InputListener() {
            @Override
            public void inputComplete(String s) {
                check = ToolUtil.checkCarLicense(s);
                if (check) {
                    license = s;
                } else {
                    showToast("您输入的车牌有误，请重新输入!");
                }
            }

            @Override
            public void deleteContent() {

            }
        });
    }

    @OnClick(R.id.back)
    public void back() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @OnClick(R.id.sure)
    public void sure() {
        if (!check) {
            showToast("您输入的车牌有误，请重新输入!");
            return;
        }

        if (license.isEmpty()) {
            showToast("请输入车牌号");
            return;
        }

        MaterialDialog dialog = new MaterialDialog(this, MaterialDialog.getDEFAULT_BEHAVIOR());
        dialog.title(null, "提示");
        dialog.message(null, "是否添加到我收藏的车辆列表中",null);
        dialog.positiveButton(null, "添加", materialDialog ->{
            CarInfo info = new CarInfo();
            info.setUsername(ToolUtil.getUsername(EnterCarLicenseActivity.this));
            info.setCar_license(license);
            client.addCarInfo(info, new BaseObserver<BaseModel>(EnterCarLicenseActivity.this) {
                @Override
                protected void showDialog() {

                }

                @Override
                protected void hideDialog() {

                }

                @Override
                protected void successful(BaseModel baseModel) {
                    showToast(baseModel.getMessage());
                    Intent intent = new Intent();
                    intent.putExtra("car", license);
                    setResult(RESULT_OK,intent);
                    finish();
                }

                @Override
                protected void defeated(BaseModel baseModel) {
                    showToast(baseModel.getMessage());
                    Intent intent = new Intent();
                    intent.putExtra("car", license);
                    setResult(RESULT_OK,intent);
                    finish();
                }

                @Override
                public void onError(ExceptionHandle.ResponeThrowable e) {
                    showToast("添加失败，请重试!");
                }
            });
            return null;
        });
        dialog.negativeButton(null, "放弃", materialDialog ->{
            Intent intent = new Intent();
            intent.putExtra("car", license);
            setResult(RESULT_OK,intent);
            finish();
            return null;
        });
        dialog.show();
    }
}
