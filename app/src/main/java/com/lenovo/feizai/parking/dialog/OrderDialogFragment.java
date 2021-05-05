package com.lenovo.feizai.parking.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.entity.Order;
import com.lenovo.feizai.parking.net.RequestAPI;
import com.lenovo.feizai.parking.util.GsonUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author feizai
 * @date 2021/3/28 0028 下午 10:16:58
 * @annotation
 */
public class OrderDialogFragment extends DialogFragment {

    private Order order;
    @BindView(R.id.QR_Code)
    ImageView QR_Code;
    @BindView(R.id.merchant)
    TextView merchant;
    @BindView(R.id.state)
    TextView orderState;
    @BindView(R.id.end)
    TextView end;
    @BindView(R.id.orderNumber)
    TextView orderNumber;
    @BindView(R.id.spaceNumber)
    TextView space;
    @BindView(R.id.subsceibeTime)
    TextView subsceibeTime;
    @BindView(R.id.date)
    TextView date;
    @BindView(R.id.priceNumber)
    TextView price;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.orderdialogstyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_order, container, false);
        ButterKnife.bind(this, view);
        Bundle bundle = getArguments();
        String json = bundle.getString("json", "");
        if (!json.equals("")) {
            order = GsonUtil.GsonToBean(json, Order.class);
            Glide.with(getContext()).load(RequestAPI.baseImageURL + order.getQrCode()).into(QR_Code);
            merchant.setText("商家名：" + order.getMerchantName());
            orderState.setText("订单" + order.getState());
            orderNumber.setText("订单号：" + order.getOrderNumber());
            space.setText(order.getSpace());
            subsceibeTime.setText("预约时长：" + order.getDuration() + "分钟");
            date.setText("下单时间：" + order.getStartDate().toString());
            price.setText(order.getPrice()+"");
            end.setText("结束时间：" + order.getEndDate().toString());
        }
        return view;
    }

    @OnClick(R.id.sure)
    public void sure() {
        this.dismiss();
    }
}
