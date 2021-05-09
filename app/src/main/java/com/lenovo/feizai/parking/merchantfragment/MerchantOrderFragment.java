package com.lenovo.feizai.parking.merchantfragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.bigkoo.pickerview.view.TimePickerView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnLoadMoreListener;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.base.BaseFragment;
import com.lenovo.feizai.parking.base.BaseModel;
import com.lenovo.feizai.parking.base.BaseObserver;
import com.lenovo.feizai.parking.base.BaseRefreshRecyclerView;
import com.lenovo.feizai.parking.customeractivity.CustomerOrderActivity;
import com.lenovo.feizai.parking.dialog.OrderDialog;
import com.lenovo.feizai.parking.dialog.OrderDialogFragment;
import com.lenovo.feizai.parking.entity.Order;
import com.lenovo.feizai.parking.net.ExceptionHandle;
import com.lenovo.feizai.parking.net.RetrofitClient;
import com.lenovo.feizai.parking.util.GsonUtil;
import com.lenovo.feizai.parking.util.ToolUtil;
import com.orhanobut.logger.Logger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author feizai
 * @date 2021/4/24 0024 上午 11:28:04
 */
public class MerchantOrderFragment extends BaseFragment {

    @BindView(R.id.typetext)
    TextView typeView;
    @BindView(R.id.datetext)
    TextView dateView;

    BaseRefreshRecyclerView order_list;
    RetrofitClient client;
    private String username;
    private String merchantname;
    private int index;
    private List<String> list;
    private String sType;
    private int year, month;
    private Date now;

    public MerchantOrderFragment() {
        super(R.layout.activity_find_order);
    }

    @Override
    protected void initView(View view) {
        Bundle bundle = getArguments();
        merchantname = bundle.getString("name");
        client = RetrofitClient.getInstance(getContext());
        username = ToolUtil.getUsername(getActivity());
        index = 1;
        list = new ArrayList<>();
        sType = "预定";
        now = new Date();
        year = Integer.valueOf(String.format("%tY", now));
        month = Integer.valueOf(String.format("%tm", now));
        dateView.setText(year + "年" + month + "月");

        order_list = new BaseRefreshRecyclerView(view, R.id.order_list, R.id.order_refresh) {
            @Override
            public BaseQuickAdapter initAdapter() {
                class OrderAdapter extends BaseQuickAdapter<String, BaseViewHolder> implements LoadMoreModule {

                    public OrderAdapter(@Nullable List<String> data) {
                        super(R.layout.order_item, data);
                    }

                    @Override
                    protected void convert(@NotNull BaseViewHolder baseViewHolder, String s) {
                        Order order = GsonUtil.GsonToBean(s, Order.class);
                        baseViewHolder.setText(R.id.merchant, order.getMerchantName());
                        baseViewHolder.setText(R.id.ordertime, order.getStartDate().toString());
                        baseViewHolder.setText(R.id.price, String.valueOf(order.getPrice()));
                    }
                }
                return new OrderAdapter(null);
            }
        };

        order_list.addRefreshLisenter(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (sType.equals("预定")) {
                    index = 1;
                    order_list.cleanData();
                    list = new ArrayList<>();
                    getSubscribeData(merchantname, year, month, sType);

                } else {
                    index = 1;
                    order_list.cleanData();
                    list = new ArrayList<>();
                    getSubscribeData(merchantname, year, month, sType);
                }
            }
        });

        order_list.setOnItemClick(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
//                OrderDialogFragment fragment = new OrderDialogFragment();
//                String order = (String) adapter.getItem(position);
//                Bundle bundle = new Bundle();
//                bundle.putString("json", order);
//                fragment.setArguments(bundle);
//                fragment.show(getActivity().getSupportFragmentManager(), "dialog");
                OrderDialog dialog = new OrderDialog(getContext(), GsonUtil.GsonToBean((String) adapter.getItem(position),Order.class));
                dialog.show();
            }
        });

        order_list.enableLoadMore(true);
        order_list.loadAutoMore(false);
        order_list.loadMore(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (sType.equals("预定"))
                    getSubscribeData(merchantname, year, month, sType);
                else
                    getSubscribeData(merchantname, year, month, sType);
            }
        });

        getSubscribeData(merchantname, year, month, sType);
    }

    @OnClick(R.id.back)
    public void back() {
        getActivity().getSupportFragmentManager().popBackStack();
    }

    @OnClick(R.id.date)
    public void date() {
        Date nowdate = new Date();
        String yearTxt = String.format("%tY", nowdate);
        String mouthTxt = String.format("%tm", nowdate);
        String dateTxt = String.format("%td", nowdate);
        Calendar startDate = Calendar.getInstance();
        startDate.set(2018, 0, 1);
        Calendar endDate = Calendar.getInstance();
        endDate.set(Integer.valueOf(yearTxt), Integer.valueOf(mouthTxt) - 1, Integer.valueOf(dateTxt));
        TimePickerView timePickerView = new TimePickerBuilder(getContext(), new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                year = Integer.valueOf(String.format("%tY", date));
                month = Integer.valueOf(String.format("%tm", date));
                showToast(year + "年" + month + "月");
                dateView.setText(year + "年" + month + "月");
                index = 1;
                list = new ArrayList<>();
                getSubscribeData(merchantname, year, month, sType);
                index++;
            }
        })
                .setTitleText("选择日期")
                .setDate(endDate)
                .setRangDate(startDate,endDate)
                .isCenterLabel(true)
                .setType(new boolean[]{true, true, false, false, false, false}).build();
        timePickerView.show();
    }

    @OnClick(R.id.type)
    public void type() {
        List<String> options1Items = new ArrayList<>();
        options1Items.add("预定");
        options1Items.add("缴费");
        OptionsPickerView<String> optionsPicker = new OptionsPickerBuilder(getContext(), new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                sType = options1Items.size() > 0 ? options1Items.get(options1) : "";
                typeView.setText(sType);
                if (sType.equals("预定")) {
                    index = 1;
                    list = new ArrayList<>();
                    getSubscribeData(merchantname, year, month, sType);
                    index++;
                } else {
                    index = 1;
                    list = new ArrayList<>();
                    getSubscribeData(merchantname, year, month, sType);
                    index++;
                }
            }
        })
                .setTitleText("选择类型")
                .setOutSideCancelable(true)
                .build();
        optionsPicker.setPicker(options1Items);
        optionsPicker.show();
    }

    private void getSubscribeData(String name, int year, int month, String type) {
        client.merchantFindOrder(name, year, month, type, index, new BaseObserver<BaseModel<String>>(getContext()) {
            @Override
            protected void showDialog() {

            }

            @Override
            protected void hideDialog() {

            }

            @Override
            protected void successful(BaseModel<String> stringBaseModel) {
                order_list.refreshEnd();
                switch (stringBaseModel.getMessage()) {
                    case "yes":
                        List<String> result = stringBaseModel.getDatas();
                        for (String s : result) {
                            list.add(s);
                            order_list.replaceData(list);
                        }
                        order_list.loadComplete();
                        break;
                    case "no":
                        showToast("没有更多数据了");
                        order_list.loadEnd();
                        break;
                }
                index++;
            }

            @Override
            protected void defeated(BaseModel<String> stringBaseModel) {
                order_list.cleanData();
                order_list.refreshEnd();
                order_list.loadFail();
                showToast("查询失败");
                index = 1;
            }

            @Override
            public void onError(ExceptionHandle.ResponeThrowable e) {
                order_list.cleanData();
                order_list.refreshEnd();
                Logger.e(e,e.getMessage());
            }
        });
    }
}
