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
public class LengthenOrderInfoFragment extends BaseFragment {

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


    private Order order;
    private Bitmap qrCode;
    private RetrofitClient client;
    private String orderjson;
    private MultipartBody.Part part;

    public LengthenOrderInfoFragment() {
        super(R.layout.fragment_order_info);
    }

    @Override
    protected void initView(View view) {
        Bundle bundle = getArguments();
        orderjson = bundle.getString("order", "");
        client = RetrofitClient.getInstance(getContext());
        if (!TextUtils.isEmpty(orderjson)) {
            order = GsonUtil.GsonToBean(orderjson, Order.class);
            merchant.setText(order.getMerchantName());
            orderState.setText("订单已支付");
            orderNumber.setText(order.getOrderNumber());
            space.setText(order.getSpace());
            duration.setText(order.getDuration()+"分钟");
            startdate.setText(ToolUtil.timeStampToString(order.getStartDate()));
            Calendar calendar=Calendar.getInstance();
            calendar.setTime(order.getStartDate());
            calendar.set(Calendar.MINUTE,calendar.get(Calendar.MINUTE)+order.getDuration());//让日期加1
            order.setState("已支付");
            order.setOrderType("预定");
            order.setEndDate(new Timestamp(calendar.getTime().getTime()));
            enddate.setText(ToolUtil.timeStampToString(order.getEndDate()));
            String json = GsonUtil.GsonString(order);
            part = creatQRCode(json);
            client.updateSubsrcibeOrder(json, part, new BaseObserver<BaseModel>(getContext()) {
                @Override
                protected void showDialog() {

                }

                @Override
                protected void hideDialog() {

                }

                @Override
                protected void successful(BaseModel baseModel) {
                    Log.i("tag", baseModel.getMessage());
                    EventBus.getDefault().postSticky(new MessageEvent("finish"));
                }

                @Override
                protected void defeated(BaseModel baseModel) {

                }

                @Override
                public void onError(ExceptionHandle.ResponeThrowable e) {

                }
            });
        }
    }

    private MultipartBody.Part creatQRCode(String content) {
        qrCode = EncodingUtils.createQRCode(content, 500, 500, null);
        Glide.with(getContext()).load(qrCode).override(500, 500).into(QR_Code);
        File file = ToolUtil.savePhotoToSDCard(qrCode, "/sdcard/DCIM/Parking/");
        order.setQrCode(file.getPath());
        RequestBody body = RequestBody.create(MediaType.parse("multipare/form-data"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("uploadFile", file.getName(), body);
        return part;
    }

    @OnClick(R.id.QR_Code)
    public void show() {
        ShowPhotoDialog dialog = new ShowPhotoDialog(getContext());
        dialog.setPhoto(qrCode);
        dialog.show();
    }

    @OnClick(R.id.sure)
    public void sure() {
        EventBus.getDefault().postSticky(new MessageEvent("finish"));
        getActivity().finish();
    }

    @OnClick(R.id.back)
    public void back() {
        EventBus.getDefault().postSticky(new MessageEvent("finish"));
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
                    EventBus.getDefault().postSticky(new MessageEvent("finish"));
                    getActivity().finish();
                    return true;
                }
                return false;
            }
        });
    }

}
