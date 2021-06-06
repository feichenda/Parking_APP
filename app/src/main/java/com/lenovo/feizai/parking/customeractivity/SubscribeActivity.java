package com.lenovo.feizai.parking.customeractivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.lenovo.feizai.parking.entity.ParkingSpace;
import com.lenovo.feizai.parking.entity.Order;
import com.lenovo.feizai.parking.merchantactivity.ManagementActivity;
import com.lenovo.feizai.parking.net.ExceptionHandle;
import com.lenovo.feizai.parking.net.RetrofitClient;
import com.lenovo.feizai.parking.util.DensityUtil;
import com.lenovo.feizai.parking.util.GsonUtil;
import com.lenovo.feizai.parking.util.ToolUtil;
import com.luck.picture.lib.decoration.GridSpacingItemDecoration;
import com.orhanobut.logger.Logger;

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
 * @date 2021/3/19 0019 下午 3:46:16
 * @annotation
 */
public class SubscribeActivity extends BaseActivity {

    @BindView(R.id.select_time)
    TextView select_time;
    @BindView(R.id.subscribe)
    TextView subscribeText;
    @BindView(R.id.merchantname)
    TextView merchantname;
    @BindView(R.id.select_car)
    TextView select_car;
//    @BindView(R.id.ing)
//    TextView ing;

    private String merchant;
    private String username;
    private List<String> car_license;
    private RetrofitClient retrofitClient;
    private int sSpace;
    private int duration;
    private String sCar;
    private Float sPay;
    private BaseRecyclerView<ParkingSpace, BaseViewHolder> subscribe_table;
    private List<String> orderList;
    private Boolean flag;
    private Order order;
    private Boolean isIng;

    public SubscribeActivity() {
        super(R.layout.activity_subscribe);
    }

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);
        Intent intent = getIntent();
        merchant = intent.getStringExtra("merchant");
        merchantname.setText("商家名:" + merchant);
        username = ToolUtil.getUsername(this);
        car_license = new ArrayList<>();
        retrofitClient = RetrofitClient.getInstance(this);
        sSpace = 0;
        orderList = new ArrayList<>();
        flag = false;
        isIng = false;

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
                        params.width = (DensityUtil.getSreenWidth(SubscribeActivity.this) - 65) / 10;
                        params.height = (DensityUtil.getSreenWidth(SubscribeActivity.this) - 65) / 10;
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
                if (sSpace == 0) {//选择车位，0表示目前没有选择的车位
                    ParkingSpace space = (ParkingSpace) adapter.getItem(position);
                    switch (space.getParkingstate()) {
                        case "未使用":
                            View bg = view.findViewById(R.id.bg);
                            bg.setSelected(!bg.isSelected());
                            sSpace = position + 1;
                            break;
                        case "已预约":
                            showToast("该车位已被预约");
                            break;
                        case "已使用":
                            showToast("该车位已使用");
                            break;
                        case "不可用":
                            showToast("该车位暂不可用");
                            break;
                    }
                } else {
                    if (sSpace > 0 && position + 1 == sSpace) {//再次点击刚刚选中的车位，取消选择
                        if (isIng) {
                            showToast("您不能更改原预约车位");
                            return;
                        }
                        View bg = view.findViewById(R.id.bg);
                        if (bg.isSelected()) {
                            sSpace = 0;
                            bg.setSelected(!bg.isSelected());
                        }
                    } else {
                        Toast.makeText(SubscribeActivity.this, "您已选择" + sSpace + "号车位", Toast.LENGTH_SHORT).show();
                    }
                }
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
        if (flag) {
            if (duration > 0) {
                if (sSpace != 0) {
                    if (!TextUtils.isEmpty(sCar)) {
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
                            Intent intent = new Intent(SubscribeActivity.this, SubscribePayActivity.class);
                            intent.putExtra("json", json);
                            intent.putExtra("price", sPay);
                            intent.putExtra("flag", flag);
                            startActivity(intent);
                            return null;
                        });
                        dialog.show();
                    } else {
                        showToast("请选择车辆");
                    }
                } else {
                    showToast("请选择车位");
                }
            } else {
                showToast("请选择预约时长");
            }
        } else {//第一次预约
            if (duration > 0) {
                if (sSpace != 0) {
                    if (!TextUtils.isEmpty(sCar)) {
                        StringBuilder sb = new StringBuilder();
                        sb = sb.append("商家名：" + merchant + "\n")
                                .append("车位号：" + sSpace + "\n")
                                .append("预约时长：" + duration + "分钟\n")
                                .append("车牌号：" + sCar + "\n")
                                .append("费用：￥" + String.format("%.2f", sPay) + "\n");
                        MaterialDialog dialog = new MaterialDialog(this, MaterialDialog.getDEFAULT_BEHAVIOR());
                        dialog.title(null, "提交预约");
                        dialog.message(null, sb.toString(), dialogMessageSettings -> {
                            return null;
                        });
                        dialog.positiveButton(null, "去支付", materialDialog -> {
                            Order order = new Order();
                            order.setMerchantName(merchant);
                            order.setCustomerName(username);
                            order.setCarLicense(sCar);
                            order.setDuration(duration);
                            order.setSpace(String.valueOf(sSpace));
                            order.setPrice(sPay);
                            String json = GsonUtil.GsonString(order);
                            Intent intent = new Intent(SubscribeActivity.this, SubscribePayActivity.class);
                            intent.putExtra("json", json);
                            intent.putExtra("price", sPay);
                            intent.putExtra("flag", flag);
                            startActivity(intent);
                            return null;
                        });
                        dialog.show();
                    } else {
                        showToast("请选择车辆");
                    }
                } else {
                    showToast("请选择车位");
                }
            } else {
                showToast("请选择预约时长");
            }
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

    //选择车辆
    @OnClick(R.id.select_car)
    public void selectcar() {
        if (car_license.size() > 0) {
            MaterialDialog dialog = new MaterialDialog(this, MaterialDialog.getDEFAULT_BEHAVIOR());
            dialog.title(null, "选择您的车辆");
            DialogSingleChoiceExtKt.listItemsSingleChoice(dialog, null, car_license, null, -1,
                    true, (materialDialog, index, text) -> {
                        sCar = TextUtils.isEmpty(text) ? "" : text.toString();
                        select_car.setText(TextUtils.isEmpty(text) ? "选择车辆" : text);
                        return null;
                    });
            dialog.positiveButton(null, "确认", materialDialog -> {
                return null;
            });
            dialog.negativeButton(null, "其他车辆", materialDialog -> {
                startActivityForResult(EnterCarLicenseActivity.class, 1);
                return null;
            });
            dialog.show();
        } else {
            if (TextUtils.isEmpty(sCar)) {
                showToast("您暂无可预约的车辆");
                MaterialDialog dialog = new MaterialDialog(this, MaterialDialog.getDEFAULT_BEHAVIOR());
                dialog.title(null, "提示");
                dialog.message(null, "是否输入车牌号以完成预约？", null);
                dialog.positiveButton(null, "去输入", materialDialog -> {
                    startActivityForResult(EnterCarLicenseActivity.class, 1);
                    return null;
                });
                dialog.negativeButton(null, "放弃预约", materialDialog -> {
                    finish();
                    return null;
                });
                dialog.show();
            } else {
                startActivityForResult(EnterCarLicenseActivity.class, 1);
            }
        }
    }

//    @OnClick(R.id.ing)
//    public void ing() {
//        flag = true;
//        class orderAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
//            private Context context;
//
//            public orderAdapter(Context context, @Nullable List<String> data) {
//                super(R.layout.order_item, data);
//            }
//
//            @Override
//            protected void convert(@NotNull BaseViewHolder baseViewHolder, String s) {
//                Order order = GsonUtil.GsonToBean(s, Order.class);
//                baseViewHolder.setText(R.id.merchant, "车牌号:" + order.getCarLicense());
//                baseViewHolder.setText(R.id.ordertime, "结束时间:" + order.getEndDate().toString());
//                baseViewHolder.setVisible(R.id.price, false);
//            }
//        }
//        List<String> cars = new ArrayList<>();
//        if (orderList.size() > 0) {
//            for (String s : orderList) {
//                cars.add(GsonUtil.GsonToBean(s, Order.class).getCarLicense());
//            }
//            MaterialDialog dialog = new MaterialDialog(this, MaterialDialog.getDEFAULT_BEHAVIOR());
//            dialog.title(null, "选择您要延长预约的车辆");
//            DialogSingleChoiceExtKt.listItemsSingleChoice(dialog, null, cars, null, -1, true, (materialDialog, which, text) -> {
//                String s = orderList.get(which);
//                order = GsonUtil.GsonToBean(s, Order.class);
//                sSpace = Integer.valueOf(order.getSpace());
//                subscribe_table.notifyDataSetChanged();
//                isIng = true;
//                sCar = TextUtils.isEmpty(text) ? "" : text.toString();
//                select_car.setText(TextUtils.isEmpty(text) ? "选择车辆" : text);
//                select_car.setEnabled(false);
//                return null;
//            });
//            dialog.positiveButton(null, "确认", materialDialog -> {
//                return null;
//            });
//            dialog.show();
//        }
//    }

    private void getCarInfo() {
        retrofitClient.selectFreeCarByUsername(username, new BaseObserver<BaseModel<String>>(this) {

            @Override
            protected void showDialog() {

            }

            @Override
            protected void hideDialog() {

            }

            @Override
            protected void successful(BaseModel<String> stringBaseModel) {
                car_license = stringBaseModel.getDatas();
            }

            @Override
            protected void defeated(BaseModel<String> stringBaseModel) {
                car_license = new ArrayList<>();
            }

            @Override
            public void onError(ExceptionHandle.ResponeThrowable e) {
                Log.e("tag", e.getMessage());
            }
        });
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
        getCarInfo();

//        retrofitClient.selectIngOrderByUser(username, new BaseObserver<BaseModel<String>>(this) {
//            @Override
//            protected void showDialog() {
//
//            }
//
//            @Override
//            protected void hideDialog() {
//
//            }
//
//            @Override
//            protected void successful(BaseModel<String> stringBaseModel) {
//                orderList = stringBaseModel.getDatas();
//                ing.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            protected void defeated(BaseModel<String> stringBaseModel) {
//                ing.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onError(ExceptionHandle.ResponeThrowable e) {
//
//            }
//        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    String car = data.getStringExtra("car");
                    RetrofitClient.getInstance(this)
                            .isSubscribing(merchant, car, new BaseObserver<BaseModel<Boolean>>(this) {
                                @Override
                                protected void showDialog() {

                                }

                                @Override
                                protected void hideDialog() {

                                }

                                @Override
                                protected void successful(BaseModel<Boolean> booleanBaseModel) {
                                    if (!booleanBaseModel.getData()) {
                                        showToast("该车辆已在预约中");
                                    }
                                }

                                @Override
                                protected void defeated(BaseModel<Boolean> booleanBaseModel) {
                                    if (booleanBaseModel.getData()) {
                                        select_car.setText(car);
                                        sCar = car;
                                    }
                                }

                                @Override
                                public void onError(ExceptionHandle.ResponeThrowable e) {
                                    showToast(e.getMessage());
                                    Logger.e(e, e.getMessage());
                                }
                            });
                }
                break;
        }
    }
}
