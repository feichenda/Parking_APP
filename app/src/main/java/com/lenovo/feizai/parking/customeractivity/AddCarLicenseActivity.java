package com.lenovo.feizai.parking.customeractivity;

import android.content.Intent;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.base.BaseActivity;
import com.lenovo.feizai.parking.base.BaseModel;
import com.lenovo.feizai.parking.base.BaseObserver;
import com.lenovo.feizai.parking.entity.CarInfo;
import com.lenovo.feizai.parking.net.ExceptionHandle;
import com.lenovo.feizai.parking.net.RetrofitClient;
import com.lenovo.feizai.parking.util.ToolUtil;
import com.pxy.LicensePlateView;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author feizai
 * @date 2021/3/31 0031 下午 7:33:36
 * @annotation
 */
public class AddCarLicenseActivity extends BaseActivity {

    @BindView(R.id.activity_lpv)
    LicensePlateView plateView;
    @BindView(R.id.main_rl_container)
    RelativeLayout container;
    @BindView(R.id.isNewCar)
    CheckBox isNewCar;
    @BindView(R.id.title)
    TextView title;

    private boolean check;
    private String license;
    private int code;
    private RetrofitClient client;
    private int id;

    public AddCarLicenseActivity() {
        super(R.layout.layout_car_license);
    }

    @Override
    protected void initView() {
        Intent intent = getIntent();
        code = intent.getIntExtra("code", -1);
        id = intent.getIntExtra("id", -1);
        title.setText(intent.getStringExtra("title"));
        check = false;
        plateView.setKeyboardContainerLayout(container);
        client = RetrofitClient.getInstance(this);

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
        finish();
    }

    @OnClick(R.id.sure)
    public void sure() {
        if (check) {
            if (TextUtils.isEmpty(license)){
                showToast("请填写车牌号");
                return;
            }
            CarInfo info = new CarInfo();
            info.setUsername(ToolUtil.getUsername(AddCarLicenseActivity.this));
            info.setCar_license(license);
            if (id != -1)
                info.setId(id);
            if (code == 1) {
                client.addCarInfo(info, new BaseObserver<BaseModel>(this) {
                    @Override
                    protected void showDialog() {

                    }

                    @Override
                    protected void hideDialog() {

                    }

                    @Override
                    protected void successful(BaseModel baseModel) {
                        showToast(baseModel.getMessage());
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    protected void defeated(BaseModel baseModel) {
                        showToast(baseModel.getMessage());
                    }

                    @Override
                    public void onError(ExceptionHandle.ResponeThrowable e) {
                        showToast("添加失败，请重试!");
                    }
                });
            }
            if (code == 2) {
                client.updateCarInfo(info, new BaseObserver<BaseModel>(this) {
                    @Override
                    protected void showDialog() {

                    }

                    @Override
                    protected void hideDialog() {

                    }

                    @Override
                    protected void successful(BaseModel baseModel) {
                        showToast(baseModel.getMessage());
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    protected void defeated(BaseModel baseModel) {
                        showToast(baseModel.getMessage());
                    }

                    @Override
                    public void onError(ExceptionHandle.ResponeThrowable e) {
                        showToast("更新失败，请重试!");
                    }
                });
            }
        }
    }
}
