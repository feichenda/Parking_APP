package com.lenovo.feizai.parking.customeractivity;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.list.DialogSingleChoiceExtKt;
import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.lenovo.feizai.parking.ALiPayactivity.SubscribePayActivity;
import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.base.BaseActivity;
import com.lenovo.feizai.parking.base.BaseModel;
import com.lenovo.feizai.parking.base.BaseObserver;
import com.lenovo.feizai.parking.base.BaseRecyclerView;
import com.lenovo.feizai.parking.entity.MessageEvent;
import com.lenovo.feizai.parking.entity.Order;
import com.lenovo.feizai.parking.entity.ParkingSpace;
import com.lenovo.feizai.parking.net.ExceptionHandle;
import com.lenovo.feizai.parking.net.RetrofitClient;
import com.lenovo.feizai.parking.util.DensityUtil;
import com.lenovo.feizai.parking.util.GsonUtil;
import com.lenovo.feizai.parking.util.ToolUtil;
import com.luck.picture.lib.decoration.GridSpacingItemDecoration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author feizai
 * @date 2021/4/27 0027 下午 3:58:07
 */
public class ChangeSubscribeActivity extends BaseActivity {

    @BindView(R.id.select_time)
    TextView select_time;
    @BindView(R.id.subscribe)
    TextView subscribeText;
    @BindView(R.id.merchantname)
    TextView merchantname;
    @BindView(R.id.select_car)
    TextView select_car;
    @BindView(R.id.ing)
    TextView ing;

    private RetrofitClient retrofitClient;
    private int sSpace;
    private int duration;
    private String sCar;
    private Float sPay;
    private BaseRecyclerView<ParkingSpace, BaseViewHolder> subscribe_table;
    private Order order;
    private String merchant;
    private String username;

    public ChangeSubscribeActivity() {
        super(R.layout.activity_subscribe);
    }

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);
        Intent intent = getIntent();
        String json = intent.getStringExtra("json");
        order = GsonUtil.GsonToBean(json, Order.class);
        merchant = order.getMerchantName();
        merchantname.setText("商家名:" + merchant);
        username = ToolUtil.getUsername(this);
        retrofitClient = RetrofitClient.getInstance(this);
        sSpace = Integer.valueOf(order.getSpace());
        sCar = order.getCarLicense();
        select_car.setText(sCar);
        ing.setVisibility(View.GONE);

        subscribe_table = new BaseRecyclerView<ParkingSpace, BaseViewHolder>(this, R.id.subscribe_table) {
            @Override
            public BaseQuickAdapter<ParkingSpace, BaseViewHolder> initAdapter() {
                class ParkingSpaceAdapter extends BaseQuickAdapter<ParkingSpace, BaseViewHolder> {

                    public ParkingSpaceAdapter(@Nullable List<ParkingSpace> data) {
                        super(R.layout.table_item, data);
                    }

                    @Override
                    protected void convert(@NotNull BaseViewHolder baseViewHolder, ParkingSpace parkingSpace) {
                        TextView text = baseViewHolder.getView(R.id.serial);
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) text.getLayoutParams();
                        params.width = (DensityUtil.getSreenWidth(ChangeSubscribeActivity.this) - 65) / 10;
                        params.height = (DensityUtil.getSreenWidth(ChangeSubscribeActivity.this) - 65) / 10;
                        text.setLayoutParams(params);
                        baseViewHolder.setText(R.id.serial, parkingSpace.getSerialnumber() + "");
                        String parkingstate = parkingSpace.getParkingstate();
                        switch (parkingstate) {
                            case "未使用":
                                View view = baseViewHolder.getView(R.id.bg);
                                if (sSpace > 0 && String.valueOf(sSpace).equals(parkingSpace.getSerialnumber())) {
                                    view.setSelected(true);
                                } else {
                                    view.setSelected(false);
                                }
                                view.setBackgroundResource(R.drawable.subscribe_space_selector);
                                break;
                            case "已预约":
                                baseViewHolder.setBackgroundResource(R.id.bg, R.drawable.subscribed);
                                break;
                            case "已使用":
                                baseViewHolder.setBackgroundResource(R.id.bg, R.drawable.used);
                                break;
                            case "不可用":
                                baseViewHolder.setBackgroundResource(R.id.bg, R.drawable.unused);
                                break;
                        }
                    }
                }
                ParkingSpaceAdapter adapter = new ParkingSpaceAdapter(null);
                return adapter;
            }
        };

        subscribe_table.setItemDecoration(new GridSpacingItemDecoration(10, 5, false));
        subscribe_table.setLayoutManager(new GridLayoutManager(this, 10));
        subscribe_table.setOnItemClick(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                showToast("您不能修改预约的停车位");
            }
        });
    }

    @OnClick(R.id.back)
    public void back() {
        finish();
    }

    //提交预约
    @OnClick(R.id.subscribe)
    public void subscribe() {
        if (duration > 0) {
            StringBuilder sb = new StringBuilder();
            sb = sb.append("商家名：" + merchant + "\n")
                    .append("车位号：" + sSpace + "\n")
                    .append("延长预约时长：" + duration + "分钟\n")
                    .append("车牌号：" + sCar + "\n")
                    .append("费用：￥" + sPay + "\n");
            MaterialDialog dialog = new MaterialDialog(this, MaterialDialog.getDEFAULT_BEHAVIOR());
            dialog.title(null, "提交预约");
            dialog.message(null, sb.toString(), dialogMessageSettings -> {
                return null;
            });
            dialog.positiveButton(null, "去支付", materialDialog -> {
                order.setDuration(order.getDuration() + duration);
                order.setPrice(order.getPrice() + sPay);
                String json = GsonUtil.GsonString(order);
                Intent intent = new Intent(ChangeSubscribeActivity.this, SubscribePayActivity.class);
                intent.putExtra("json", json);
                intent.putExtra("price", sPay);
                intent.putExtra("flag", true);
                startActivity(intent);
                return null;
            });
            dialog.show();
        } else {
            showToast("请选择预约时长");
        }
    }

    //选择时间
    @OnClick(R.id.select_time)
    public void selecttime() {
        List<Integer> options1Items = new ArrayList<>();
        for (int i = 4; i < 30; i = i + 5) {
            options1Items.add(i + 1);
        }
        OptionsPickerView<Integer> optionsPicker = new OptionsPickerBuilder(this, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                duration = options1Items.size() > 0 ? options1Items.get(options1) : 0;
                select_time.setText("已选择：" + duration + "分钟");
                if (duration > 0 && duration <= 15) {
                    sPay = 0.50f;
                    subscribeText.setText("￥0.50    预约");
                } else {
                    sPay = 1.00f;
                    subscribeText.setText("￥1.00    预约");
                }
            }
        })
                .setTitleText("选择预约时长(分钟)")
                .setOutSideCancelable(true)
                .isDialog(true)
                .build();
        optionsPicker.setPicker(options1Items);
        optionsPicker.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void finish(MessageEvent event) {
        if (event.getGo() == "finish") {
            event.setGo("");
            finish();
        } else {
            if (event.getGo() == "failed") {
                showToast("预约失败，请重试!");
                event.setGo("");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        retrofitClient.searchParkingSpace(merchant, new BaseObserver<BaseModel<ParkingSpace>>(this) {
            @Override
            protected void showDialog() {

            }

            @Override
            protected void hideDialog() {

            }

            @Override
            protected void successful(BaseModel<ParkingSpace> parkingSpaceBaseModel) {
                List<ParkingSpace> parkingSpaces = parkingSpaceBaseModel.getDatas();
                subscribe_table.replaceData(parkingSpaces);
            }

            @Override
            protected void defeated(BaseModel<ParkingSpace> parkingSpaceBaseModel) {

            }

            @Override
            public void onError(ExceptionHandle.ResponeThrowable e) {

            }
        });
    }
}
