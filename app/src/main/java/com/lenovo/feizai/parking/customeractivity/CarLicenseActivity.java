package com.lenovo.feizai.parking.customeractivity;

import android.content.Intent;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.base.BaseActivity;
import com.lenovo.feizai.parking.base.BaseModel;
import com.lenovo.feizai.parking.base.BaseObserver;
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
 * @date 2021/4/22 0022 上午 11:15:49
 */
public class CarLicenseActivity extends BaseActivity {

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
    private String merchant;
    private RetrofitClient client;

    public CarLicenseActivity() {
        super(R.layout.layout_car_license);
    }

    @Override
    protected void initView() {
        Intent intent = getIntent();
        merchant = intent.getStringExtra("merchant");
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

        client.clearing(merchant, license, new BaseObserver<BaseModel<CheckInfo>>(this) {
            @Override
            protected void showDialog() {

            }

            @Override
            protected void hideDialog() {

            }

            @Override
            protected void successful(BaseModel<CheckInfo> checkInfoBaseModel) {
                Intent intent = new Intent(CarLicenseActivity.this, ClearingActivity.class);
                intent.putExtra("json", GsonUtil.GsonString(checkInfoBaseModel.getData()));
                startActivity(intent);
                finish();
            }

            @Override
            protected void defeated(BaseModel<CheckInfo> checkInfoBaseModel) {
                showToast(checkInfoBaseModel.getMessage());
            }

            @Override
            public void onError(ExceptionHandle.ResponeThrowable e) {
                Log.e("tag", e.getMessage());
            }
        });
    }
}
