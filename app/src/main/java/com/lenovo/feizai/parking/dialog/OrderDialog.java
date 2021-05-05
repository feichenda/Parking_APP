package com.lenovo.feizai.parking.dialog;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.ModalDialog;
import com.afollestad.materialdialogs.customview.DialogCustomViewExtKt;
import com.bumptech.glide.Glide;
import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.entity.Order;
import com.lenovo.feizai.parking.net.RequestAPI;
import com.lenovo.feizai.parking.util.ToolUtil;

import butterknife.BindView;

/**
 * @author feizai
 * @date 2021/4/26 0026 上午 11:52:53
 */
public class OrderDialog {
    private Context context;
    private Order order;
    private MaterialDialog dialog;

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

    public OrderDialog(Context context, Order order) {
        this.context = context;
        this.order = order;
        View view = onCreatView();
        if (dialog == null) {
            dialog = new MaterialDialog(context, MaterialDialog.getDEFAULT_BEHAVIOR());
            DialogCustomViewExtKt.customView(dialog, null, view, false, true, false, true);
        }
    }

    private View onCreatView() {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_order, null, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        QR_Code = view.findViewById(R.id.QR_Code);
        merchant = view.findViewById(R.id.merchant);
        orderState = view.findViewById(R.id.state);
        end = view.findViewById(R.id.end);
        orderNumber = view.findViewById(R.id.orderNumber);
        space = view.findViewById(R.id.spaceNumber);
        subsceibeTime = view.findViewById(R.id.subsceibeTime);
        date = view.findViewById(R.id.date);
        price = view.findViewById(R.id.priceNumber);

        Glide.with(context).load(RequestAPI.baseImageURL + order.getQrCode()).override(500,500).into(QR_Code);
        merchant.setText("商家名：" + order.getMerchantName());
        orderState.setText("订单" + order.getState());
        orderNumber.setText("订单号：" + order.getOrderNumber());
        space.setText(order.getSpace());
        subsceibeTime.setText("使用时长：" + ToolUtil.getDetailDuration(ToolUtil.timeStampToString(order.getStartDate()),ToolUtil.timeStampToString(order.getEndDate())));
        date.setText("下单时间：" + ToolUtil.timeStampToString(order.getStartDate()));
        price.setText("￥"+String.format("%.2f", order.getPrice()));
        end.setText("结束时间：" + ToolUtil.timeStampToString(order.getEndDate()));

        switch (order.getState()) {
            case "进行中":
                orderState.setTextColor(Color.GREEN);
                break;
            case "已超时":
                orderState.setTextColor(Color.RED);
                break;
            case "已完成":
                orderState.setTextColor(Color.GRAY);
                break;
            case "已取消":
                orderState.setTextColor(Color.BLACK);
                break;
        }

        view.findViewById(R.id.sure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void show() {
        if (dialog != null) {
            dialog.show();
        }
    }

    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
