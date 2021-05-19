package com.lenovo.feizai.parking.merchantactivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.base.BaseActivity;
import com.lenovo.feizai.parking.base.BaseModel;
import com.lenovo.feizai.parking.base.BaseObserver;
import com.lenovo.feizai.parking.dialog.ShowPhotoDialog;
import com.lenovo.feizai.parking.entity.CheckInfo;
import com.lenovo.feizai.parking.net.ExceptionHandle;
import com.lenovo.feizai.parking.net.RetrofitClient;
import com.lenovo.feizai.parking.util.EncodingUtils;
import com.lenovo.feizai.parking.util.GsonUtil;
import com.lenovo.feizai.parking.util.ToolUtil;
import com.lenovo.feizai.parking.util.UniqueOrderGenerateUtil;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author feizai
 * @date 2021/4/18 0018 下午 2:30:37
 */
public class CheckOutActivity extends BaseActivity {

    @BindView(R.id.license)
    EditText license;
    @BindView(R.id.merchant)
    EditText merchant;
    @BindView(R.id.space)
    EditText space;
    @BindView(R.id.intime)
    EditText intime;
    @BindView(R.id.outtime)
    EditText outtime;
    @BindView(R.id.price)
    EditText price;
    @BindView(R.id.payCode)
    ImageView payCode;
    @BindView(R.id.money)
    Button money;

    private String merchantname;
    private String car;
    private RetrofitClient client;
    private CheckInfo info;

    public CheckOutActivity() {
        super(R.layout.activity_checkout);
    }

    @Override
    protected void initView() {
        Intent intent = getIntent();
        car = intent.getStringExtra("car");
        merchantname = intent.getStringExtra("merchant");
        info = GsonUtil.GsonToBean(intent.getStringExtra("info"),CheckInfo.class);
        client = RetrofitClient.getInstance(this);
        merchant.setText(merchantname);
        license.setText(car);
        space.setText(info.getSerialnumber());
        intime.setText(info.getIntime());
        outtime.setText(info.getOuttime());
        price.setText(info.getPrice()+"元");
        if (info.getState() == "已缴费") {
            money.setVisibility(View.GONE);
            payCode.setVisibility(View.GONE);
        }else {
            Bitmap qrCode = EncodingUtils.createQRCode(GsonUtil.GsonString(info), 500, 500, null);
            Glide.with(CheckOutActivity.this).load(qrCode).override(500, 500).into(payCode);
            payCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ShowPhotoDialog dialog = new ShowPhotoDialog(CheckOutActivity.this);
                    dialog.setPhoto(qrCode);
                    dialog.show();
                }
            });
        }
    }

    @OnClick(R.id.back)
    public void back() {
        finish();
    }

    @OnClick(R.id.sure)
    public void sure() {
        if (info.getState() == "已缴费") {

        } else {
            client.isPay(merchantname, car, new BaseObserver<BaseModel>(this) {
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
                    return;
                }

                @Override
                public void onError(ExceptionHandle.ResponeThrowable e) {
                    Log.e("tag", e.getMessage());
                    return;
                }
            });
        }
    }

    @OnClick(R.id.money)
    public void money() {//现金缴费
        info.setOrdernumber(new UniqueOrderGenerateUtil(0,0).getId());
        client.updatePayByMoney(info, new BaseObserver<BaseModel>(this) {
            @Override
            protected void showDialog() {

            }

            @Override
            protected void hideDialog() {

            }

            @Override
            protected void successful(BaseModel baseModel) {
                money.setEnabled(false);
                showToast(baseModel.getMessage());
                finish();
            }

            @Override
            protected void defeated(BaseModel baseModel) {
                showToast(baseModel.getMessage());
            }

            @Override
            public void onError(ExceptionHandle.ResponeThrowable e) {
                showToast(e.getMessage());
            }
        });
    }
}
