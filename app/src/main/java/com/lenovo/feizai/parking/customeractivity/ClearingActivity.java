package com.lenovo.feizai.parking.customeractivity;

import android.content.Intent;
import android.util.Log;
import android.widget.EditText;

import com.lenovo.feizai.parking.ALiPayactivity.ScanPayActivity;
import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.base.BaseActivity;
import com.lenovo.feizai.parking.base.BaseModel;
import com.lenovo.feizai.parking.base.BaseObserver;
import com.lenovo.feizai.parking.entity.CheckInfo;
import com.lenovo.feizai.parking.entity.MessageEvent;
import com.lenovo.feizai.parking.net.ExceptionHandle;
import com.lenovo.feizai.parking.net.RetrofitClient;
import com.lenovo.feizai.parking.util.GsonUtil;
import com.lenovo.feizai.parking.util.ToolUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author feizai
 * @date 2021/4/22 0022 下午 10:39:58
 */
public class ClearingActivity extends BaseActivity {

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
    @BindView(R.id.duration)
    EditText duration;
    @BindView(R.id.price)
    EditText price;
    private CheckInfo checkInfo;
    private RetrofitClient client;
    private Timer timer;


    public ClearingActivity() {
        super(R.layout.activity_check_out_order);
    }

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);
        Intent intent = getIntent();
        String json = intent.getStringExtra("json");
        checkInfo = GsonUtil.GsonToBean(json, CheckInfo.class);
        license.setText(checkInfo.getCarlicense());
        merchant.setText(checkInfo.getMerchant());
        space.setText(checkInfo.getSerialnumber());
        intime.setText(checkInfo.getIntime());
        outtime.setText(checkInfo.getOuttime());
        duration.setText(ToolUtil.getDetailDuration(checkInfo.getIntime(),checkInfo.getOuttime()));
        price.setText(checkInfo.getPrice()+"元");
        client = RetrofitClient.getInstance(this);
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                getData();
            }
        };
        timer.schedule(task,new Date(),1000*60);
    }

    @OnClick(R.id.money)
    public void pay() {
        Intent intent = new Intent(ClearingActivity.this, ScanPayActivity.class);
        intent.putExtra("json",GsonUtil.GsonString(checkInfo));
        startActivity(intent);
    }

    @OnClick(R.id.refresh)
    public void refresh() {
        getData();
    }

    @Subscribe(threadMode = ThreadMode.POSTING,sticky = true)
    public void onFinish(MessageEvent event) {
        if (event.getGo().equals("finish")) {
            finish();
            event.setGo("");
        }
    }

    @OnClick(R.id.back)
    public void back() {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        timer.cancel();
    }

    private void getData() {
        String merchatname = merchant.getText().toString();
        String carlicense = license.getText().toString();
        client.clearing(merchatname, carlicense, new BaseObserver<BaseModel<CheckInfo>>(this) {
            @Override
            protected void showDialog() {

            }

            @Override
            protected void hideDialog() {

            }

            @Override
            protected void successful(BaseModel<CheckInfo> checkInfoBaseModel) {
                CheckInfo info = checkInfoBaseModel.getData();
                license.setText(info.getCarlicense());
                merchant.setText(info.getMerchant());
                space.setText(info.getSerialnumber());
                intime.setText(info.getIntime());
                outtime.setText(info.getOuttime());
                duration.setText(ToolUtil.getDetailDuration(info.getIntime(),info.getOuttime()));
                price.setText(info.getPrice()+"元");

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
