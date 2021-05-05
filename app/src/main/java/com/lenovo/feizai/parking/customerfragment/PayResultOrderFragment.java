package com.lenovo.feizai.parking.customerfragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.base.BaseFragment;
import com.lenovo.feizai.parking.base.BaseModel;
import com.lenovo.feizai.parking.base.BaseObserver;
import com.lenovo.feizai.parking.dialog.ShowPhotoDialog;
import com.lenovo.feizai.parking.entity.CheckInfo;
import com.lenovo.feizai.parking.entity.MessageEvent;
import com.lenovo.feizai.parking.entity.Order;
import com.lenovo.feizai.parking.net.ExceptionHandle;
import com.lenovo.feizai.parking.net.RetrofitClient;
import com.lenovo.feizai.parking.util.EncodingUtils;
import com.lenovo.feizai.parking.util.GsonUtil;
import com.lenovo.feizai.parking.util.ToolUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.sql.Timestamp;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * @author feizai
 * @date 2021/3/26 0026 上午 11:12:06
 * @annotation
 */
public class PayResultOrderFragment extends BaseFragment {

    @BindView(R.id.QR_Code)
    ImageView QR_Code;
    @BindView(R.id.merchant)
    TextView merchant;
    @BindView(R.id.orderState)
    TextView orderState;
    @BindView(R.id.orderNumber)
    TextView orderNumber;
    @BindView(R.id.space)
    TextView space;
    @BindView(R.id.duration)
    TextView duration;
    @BindView(R.id.startdate)
    TextView startdate;
    @BindView(R.id.enddate)
    TextView enddate;

    private CheckInfo checkInfo;
    private RetrofitClient client;
    private String json;
    private Order order;

    public PayResultOrderFragment() {
        super(R.layout.fragment_order_info);
    }

    @Override
    protected void initView(View view) {
        Bundle bundle = getArguments();
        json = bundle.getString("info", "");
        order = new Order();
        QR_Code.setVisibility(View.GONE);
        client = RetrofitClient.getInstance(getContext());
        if (!TextUtils.isEmpty(json)) {
            checkInfo = GsonUtil.GsonToBean(json, CheckInfo.class);
            int durationtime = ToolUtil.getDuration(checkInfo.getIntime(), checkInfo.getOuttime());

            checkInfo.setState("已出场");

            merchant.setText(checkInfo.getMerchant());
            orderState.setText("订单已支付");
            orderNumber.setText(checkInfo.getOrdernumber());
            space.setText(checkInfo.getSerialnumber());
            duration.setText(ToolUtil.getDetailDuration(checkInfo.getIntime(),checkInfo.getOuttime()));
            startdate.setText(checkInfo.getIntime());
            enddate.setText(checkInfo.getOuttime());

            order.setCustomerName(ToolUtil.getUsername(getActivity()));
            order.setOrderNumber(checkInfo.getOrdernumber());
            order.setMerchantName(checkInfo.getMerchant());
            order.setSpace(checkInfo.getSerialnumber());
            order.setCarLicense(checkInfo.getCarlicense());
            order.setPrice(checkInfo.getPrice());
            order.setDuration(durationtime);
            order.setStartDate(Timestamp.valueOf(checkInfo.getIntime()));
            order.setEndDate(Timestamp.valueOf(checkInfo.getOuttime()));
            order.setState("已完成");
            order.setOrderType("缴费");

            //去更改数据库
            client.updateByNowCode(GsonUtil.GsonString(order), GsonUtil.GsonString(checkInfo), new BaseObserver<BaseModel>(getContext()) {
                @Override
                protected void showDialog() {

                }

                @Override
                protected void hideDialog() {

                }

                @Override
                protected void successful(BaseModel baseModel) {
                    showToast("支付成功");
                }

                @Override
                protected void defeated(BaseModel baseModel) {
                    showToast(baseModel.getMessage());
                }

                @Override
                public void onError(ExceptionHandle.ResponeThrowable e) {
                    Log.e("tag", e.getMessage());
                }
            });
        }
    }

    @OnClick(R.id.sure)
    public void sure() {
        getActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_BACK) {
                    return true;
                }
                return false;
            }
        });
    }

}
