package com.lenovo.feizai.parking.merchantactivity;

import android.content.Intent;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.base.BaseActivity;
import com.lenovo.feizai.parking.net.RetrofitClient;
import com.lenovo.feizai.parking.util.ToolUtil;
import com.pxy.LicensePlateView;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author feizai
 * @date 2021/4/18 0018 下午 2:04:41
 */
public class CheckCarLicenseActivity extends BaseActivity {

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

    public final static int REQUEST_IN_CAR_LICENSE = 2000;
    public final static int REQUEST_OUT_CAR_LICENSE = 3000;

    public CheckCarLicenseActivity() {
        super(R.layout.layout_car_license);
    }

    @Override
    protected void initView() {
        title.setText("输入车牌号码");

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

        Intent intent = new Intent();
        intent.putExtra("car", license);
        setResult(RESULT_OK, intent);
        finish();

    }
}
