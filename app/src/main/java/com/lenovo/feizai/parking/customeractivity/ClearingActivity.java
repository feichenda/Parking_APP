package com.lenovo.feizai.parking.customeractivity;

import android.content.Intent;
import android.widget.EditText;

import com.lenovo.feizai.parking.ALiPayactivity.ScanPayActivity;
import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.base.BaseActivity;
import com.lenovo.feizai.parking.entity.CheckInfo;
import com.lenovo.feizai.parking.entity.MessageEvent;
import com.lenovo.feizai.parking.util.GsonUtil;
import com.lenovo.feizai.parking.util.ToolUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
    }

    @OnClick(R.id.money)
    public void pay() {
        Intent intent = new Intent(ClearingActivity.this, ScanPayActivity.class);
        intent.putExtra("json",GsonUtil.GsonString(checkInfo));
        startActivity(intent);
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
    }
}
