package com.lenovo.feizai.parking.merchantactivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.base.BaseActivity;
import com.lenovo.feizai.parking.base.BaseModel;
import com.lenovo.feizai.parking.base.BaseObserver;
import com.lenovo.feizai.parking.dialog.SelectSpaceDialog;
import com.lenovo.feizai.parking.entity.CheckInfo;
import com.lenovo.feizai.parking.entity.Order;
import com.lenovo.feizai.parking.net.ExceptionHandle;
import com.lenovo.feizai.parking.net.RetrofitClient;
import com.lenovo.feizai.parking.util.GsonUtil;
import com.lenovo.feizai.parking.util.ToolUtil;
import com.orhanobut.logger.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author feizai
 * @date 2021/4/1 0001 下午 9:57:22
 * @annotation
 */
public class CheckInActivity extends BaseActivity {

    @BindView(R.id.license)
    EditText license;
    @BindView(R.id.merchant)
    EditText merchant;
    @BindView(R.id.space)
    EditText space;
    @BindView(R.id.intime)
    EditText intime;

    private RetrofitClient client;
    private Order order;
    private int sSpace;


    public CheckInActivity() {
        super(R.layout.activity_checkin);
    }

    @Override
    protected void initView() {
        Intent intent = getIntent();
        String orderjson = intent.getStringExtra("order");
        String merchantname = intent.getStringExtra("merchant");
        String car = intent.getStringExtra("car");
        if (!orderjson.isEmpty()) {
            order = GsonUtil.GsonToBean(orderjson, Order.class);
            license.setText(order.getCarLicense());
        }
        if (!car.isEmpty()) {
            license.setText(car);
        }
        merchant.setText(merchantname);
        intime.setText(ToolUtil.getDate());
        client = RetrofitClient.getInstance(this);
        sSpace = 0;
    }

    @OnClick(R.id.back)
    public void back() {
        finish();
    }

    @OnClick(R.id.sure)
    public void sure() {
        if (sSpace == 0) {
            showToast("您未选择车位");
            return;
        }
        CheckInfo checkInfo = new CheckInfo();
        checkInfo.setCarlicense(license.getText().toString().trim());
        checkInfo.setMerchant(merchant.getText().toString().trim());
        checkInfo.setSerialnumber(String.valueOf(sSpace));
        checkInfo.setIntime(intime.getText().toString().trim());

        client.addCheckInfo(checkInfo, new BaseObserver<BaseModel>(this) {
            @Override
            protected void showDialog() {

            }

            @Override
            protected void hideDialog() {

            }

            @Override
            protected void successful(BaseModel baseModel) {
                showToast(baseModel.getMessage());
                finish();
            }

            @Override
            protected void defeated(BaseModel baseModel) {
                showToast(baseModel.getMessage());
            }

            @Override
            public void onError(ExceptionHandle.ResponeThrowable e) {
                Logger.e(e,e.getMessage());
            }
        });
    }

    @OnClick(R.id.space)
    public void selecSpace() {
        SelectSpaceDialog dialog = new SelectSpaceDialog(this,merchant.getText().toString().trim(),sSpace);
        dialog.setOnDialogListener(new SelectSpaceDialog.OnDialogClickListener() {
            @Override
            public void onDialogSureClick(int poistion) {
                sSpace = poistion;
                space.setText(poistion + "");
                space.setTextColor(Color.GRAY);
            }
        });
        dialog.show();
    }
}
