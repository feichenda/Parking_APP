package com.lenovo.feizai.parking.customeractivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.base.BaseActivity;
import com.lenovo.feizai.parking.base.BaseModel;
import com.lenovo.feizai.parking.base.BaseObserver;
import com.lenovo.feizai.parking.base.BaseRefreshRecyclerView;
import com.lenovo.feizai.parking.dialog.OptionDialog;
import com.lenovo.feizai.parking.dialog.OrderDialog;
import com.lenovo.feizai.parking.entity.Order;
import com.lenovo.feizai.parking.net.ExceptionHandle;
import com.lenovo.feizai.parking.net.RetrofitClient;
import com.lenovo.feizai.parking.util.GsonUtil;
import com.lenovo.feizai.parking.util.ToolUtil;
import com.orhanobut.logger.Logger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import butterknife.OnClick;
import me.leefeng.promptlibrary.PromptButton;
import me.leefeng.promptlibrary.PromptButtonListener;
import me.leefeng.promptlibrary.PromptDialog;

/**
 * @author feizai
 * @date 2021/4/27 0027 下午 3:00:47
 */
public class CustomerIngActivity extends BaseActivity {

    private RetrofitClient client;
    private String username;
    private BaseRefreshRecyclerView ing_list;

    public CustomerIngActivity() {
        super(R.layout.activity_ing_list);
    }

    @Override
    protected void initView() {
        client = RetrofitClient.getInstance(this);
        username = ToolUtil.getUsername(this);
        initRecycle();
    }

    private void initRecycle() {
        ing_list = new BaseRefreshRecyclerView(this,R.id.ing_list,R.id.ing_refresh) {
            @Override
            public BaseQuickAdapter initAdapter() {
                class IngAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

                    private Context context;

                    public IngAdapter(Context context, @Nullable List<String> data) {
                        super(R.layout.order_item, data);
                    }

                    @Override
                    protected void convert(@NotNull BaseViewHolder baseViewHolder, String s) {
                        Order order = GsonUtil.GsonToBean(s, Order.class);
                        baseViewHolder.setText(R.id.merchant, order.getMerchantName());
                        baseViewHolder.setText(R.id.ordertime, ToolUtil.timeStampToString(order.getStartDate()));
                        baseViewHolder.setText(R.id.price, String.valueOf(order.getCarLicense()));
                    }
                }
                return new IngAdapter(CustomerIngActivity.this,null);
            }
        };

        ing_list.addRefreshLisenter(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ing_list.cleanData();
                ing_list.removeAllHeadView();
                getIngData();
            }
        });

        ing_list.setOnItemClick(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                String orderjson = (String) adapter.getItem(position);
                Order order = GsonUtil.GsonToBean(orderjson, Order.class);
                PromptDialog dialog = new PromptDialog(CustomerIngActivity.this);
                PromptButton esc = new PromptButton("取消", new PromptButtonListener() {
                    @Override
                    public void onClick(PromptButton button) {
                        dialog.dismiss();
                    }
                });
                PromptButton see = new PromptButton("查看订单", new PromptButtonListener() {
                    @Override
                    public void onClick(PromptButton button) {
                        OrderDialog orderDialog = new OrderDialog(CustomerIngActivity.this,order);
                        orderDialog.show();
                        dialog.dismiss();
                    }
                });
                PromptButton change = new PromptButton("延长预约", new PromptButtonListener() {
                    @Override
                    public void onClick(PromptButton button) {
                        Intent intent = new Intent(CustomerIngActivity.this,ChangeSubscribeActivity.class);
                        intent.putExtra("json", orderjson);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                });
                PromptButton cancle = new PromptButton("取消订单", new PromptButtonListener() {
                    @Override
                    public void onClick(PromptButton button) {
                        PromptDialog promptDialog = new PromptDialog(CustomerIngActivity.this);
                        promptDialog.showWarnAlert("确认取消订单",new PromptButton("确认", new PromptButtonListener() {
                            @Override
                            public void onClick(PromptButton button) {
                                client.cancelOrderByNumber(orderjson, new BaseObserver<BaseModel>(CustomerIngActivity.this) {
                                    @Override
                                    protected void showDialog() {

                                    }

                                    @Override
                                    protected void hideDialog() {

                                    }

                                    @Override
                                    protected void successful(BaseModel baseModel) {
                                        showToast(baseModel.getMessage());
                                        ing_list.removeAllHeadView();
                                        ing_list.cleanData();
                                        getIngData();
                                    }

                                    @Override
                                    protected void defeated(BaseModel baseModel) {
                                        showToast(baseModel.getMessage());
                                    }

                                    @Override
                                    public void onError(ExceptionHandle.ResponeThrowable e) {
                                        showToast("error");
                                        Logger.e(e,e.getMessage());
                                    }
                                });
                            }
                        }),new PromptButton("取消", new PromptButtonListener() {
                            @Override
                            public void onClick(PromptButton button) {
                                promptDialog.dismiss();
                            }
                        }));
                        dialog.dismiss();
                    }
                });
                dialog.showAlertSheet(null,true,esc,cancle,change,see);
            }
        });
    }

    @OnClick(R.id.back)
    public void back() {
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getIngData();
    }

    private void getIngData() {
        client.selectIngOrderByUser(username, new BaseObserver<BaseModel<String>>(this) {
            @Override
            protected void showDialog() {

            }

            @Override
            protected void hideDialog() {

            }

            @Override
            protected void successful(BaseModel<String> stringBaseModel) {
                List<String> datas = stringBaseModel.getDatas();
                ing_list.replaceData(datas);
                ing_list.removeAllHeadView();
                View view = LayoutInflater.from(CustomerIngActivity.this).inflate(R.layout.head_nulldata, null, false);
                TextView title = view.findViewById(R.id.title);
                title.setText("正在进行中的订单");
                title.setTextSize(20);
                title.setTextColor(Color.BLACK);
                ing_list.addHeadView(view);
                ing_list.refreshEnd();
            }

            @Override
            protected void defeated(BaseModel<String> stringBaseModel) {
                ing_list.cleanData();
                ing_list.removeAllHeadView();
                View view = LayoutInflater.from(CustomerIngActivity.this).inflate(R.layout.head_nulldata, null, false);
                TextView title = view.findViewById(R.id.title);
                title.setText("很抱歉，您暂时没有预定中的订单");
                title.setTextSize(20);
                title.setTextColor(Color.BLACK);
                ing_list.addHeadView(view);
                ing_list.refreshEnd();
            }

            @Override
            public void onError(ExceptionHandle.ResponeThrowable e) {
                ing_list.cleanData();
                ing_list.removeAllHeadView();
                View view = LayoutInflater.from(CustomerIngActivity.this).inflate(R.layout.head_nulldata, null, false);
                TextView title = view.findViewById(R.id.title);
                title.setText("很抱歉，页面丢失了");
                title.setTextSize(20);
                title.setTextColor(Color.BLACK);
                ing_list.addHeadView(view);
                ing_list.refreshEnd();
                showToast("error");
                Logger.e(e,e.getMessage());
            }
        });
    }
}
