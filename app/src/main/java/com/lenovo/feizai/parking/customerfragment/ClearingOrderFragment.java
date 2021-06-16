package com.lenovo.feizai.parking.customerfragment;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.base.BaseFragment;
import com.lenovo.feizai.parking.base.BaseModel;
import com.lenovo.feizai.parking.base.BaseObserver;
import com.lenovo.feizai.parking.entity.Order;
import com.lenovo.feizai.parking.net.ExceptionHandle;
import com.lenovo.feizai.parking.net.RetrofitClient;
import com.lenovo.feizai.parking.util.GsonUtil;
import com.lenovo.feizai.parking.util.ToolUtil;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author feizai
 * @date 2021/4/22 0022 下午 11:28:23
 */
public class ClearingOrderFragment extends BaseFragment {

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
    @BindView(R.id.QR_Code)
    ImageView QR_Code;
    @BindView(R.id.duration_text)
    TextView duration_text;

    private RetrofitClient client;

    public ClearingOrderFragment() {
        super(R.layout.fragment_order_info);
    }

    @Override
    protected void initView(View view) {
        QR_Code.setVisibility(View.GONE);
        Bundle bundle = getArguments();
        String info = bundle.getString("info");
        String orderjson = bundle.getString("order");
        Order order = GsonUtil.GsonToBean(orderjson, Order.class);
        order = GsonUtil.GsonToBean(orderjson, Order.class);
        duration_text.setText("停车时长");
        merchant.setText(order.getMerchantName());
        orderState.setText("订单已支付");
        orderNumber.setText(order.getOrderNumber());
        space.setText(order.getSpace());
        duration.setText(ToolUtil.getDetailDuration(ToolUtil.timeStampToString(order.getStartDate()),ToolUtil.timeStampToString(order.getEndDate())));
        startdate.setText(ToolUtil.timeStampToString(order.getStartDate()));
        enddate.setText(ToolUtil.timeStampToString(order.getEndDate()));

        client = RetrofitClient.getInstance(getContext());
        client.updataByScanQRCode(orderjson, info, new BaseObserver<BaseModel>(getContext()) {
            @Override
            protected void showDialog() {

            }

            @Override
            protected void hideDialog() {

            }

            @Override
            protected void successful(BaseModel baseModel) {
                showToast(baseModel.getMessage());
                MaterialDialog dialog = new MaterialDialog(getContext(), MaterialDialog.getDEFAULT_BEHAVIOR());
                dialog.title(null, "支付成功");
                dialog.message(null,"请于5分钟内把车辆驶离停车场，超时系统将自动重新计费！",dialogMessageSettings -> {
                    return null;
                });
                dialog.show();
            }

            @Override
            protected void defeated(BaseModel baseModel) {
                showToast(baseModel.getMessage());
            }

            @Override
            public void onError(ExceptionHandle.ResponeThrowable e) {
                Log.e("tag",e.getMessage());
                showToast(e.getMessage());
            }
        });
    }

    @OnClick(R.id.sure)
    public void sure() {
        getActivity().finish();
    }
}
