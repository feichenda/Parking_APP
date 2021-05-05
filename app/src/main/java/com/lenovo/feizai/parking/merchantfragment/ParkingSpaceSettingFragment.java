package com.lenovo.feizai.parking.merchantfragment;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.base.BaseFragment;
import com.lenovo.feizai.parking.base.BaseModel;
import com.lenovo.feizai.parking.base.BaseObserver;
import com.lenovo.feizai.parking.base.BaseRecyclerView;
import com.lenovo.feizai.parking.customeractivity.SubscribeActivity;
import com.lenovo.feizai.parking.entity.MessageEvent;
import com.lenovo.feizai.parking.entity.ParkingSpace;
import com.lenovo.feizai.parking.net.ExceptionHandle;
import com.lenovo.feizai.parking.net.RetrofitClient;
import com.lenovo.feizai.parking.util.DensityUtil;
import com.luck.picture.lib.decoration.GridSpacingItemDecoration;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.OnClick;

/**
 * @author feizai
 * @date 2021/4/4 0004 上午 11:14:03
 * @annotation
 */
public class ParkingSpaceSettingFragment extends BaseFragment {

    private BaseRecyclerView<ParkingSpace, BaseViewHolder> space_list;
    private RetrofitClient client;
    private String merchant;
    private Map<String, String> spaces;

    public ParkingSpaceSettingFragment() {
        super(R.layout.parking_space_setting);
    }

    @Override
    protected void initView(View view) {
        client = RetrofitClient.getInstance(getContext());
        Bundle bundle = getArguments();
        merchant = bundle.getString("name");
        spaces = new HashMap<>();

        space_list = new BaseRecyclerView<ParkingSpace, BaseViewHolder>(view, R.id.space_list) {
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
                        params.width = (DensityUtil.getSreenWidth(getContext()) - 65) / 10;
                        params.height = (DensityUtil.getSreenWidth(getContext()) - 65) / 10;
                        text.setLayoutParams(params);
                        baseViewHolder.setText(R.id.serial, parkingSpace.getSerialnumber() + "");
                        String parkingstate = parkingSpace.getParkingstate();
                        View view = baseViewHolder.getView(R.id.bg);
                        switch (parkingstate) {
                            case "未使用":
                                view.setSelected(false);
                                view.setBackgroundResource(R.drawable.manage_space_selector);
                                break;
                            case "已预约":
                                baseViewHolder.setBackgroundResource(R.id.bg, R.drawable.subscribed);
                                break;
                            case "已使用":
                                baseViewHolder.setBackgroundResource(R.id.bg, R.drawable.used);
                                break;
                            case "不可用":
                                view.setSelected(true);
                                view.setBackgroundResource(R.drawable.manage_space_selector);
                                break;
                        }
                    }
                }
                ParkingSpaceAdapter adapter = new ParkingSpaceAdapter(null);
                return adapter;
            }
        };

        space_list.setItemDecoration(new GridSpacingItemDecoration(10, 5, false));
        space_list.setLayoutManager(new GridLayoutManager(getContext(), 10));

        space_list.setOnItemClick(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                ParkingSpace space = (ParkingSpace) adapter.getItem(position);
                View bg = view.findViewById(R.id.bg);
                switch (space.getParkingstate()) {
                    case "未使用":
                        bg.setSelected(!bg.isSelected());
                        if (spaces.containsKey(space.getSerialnumber())) {
                            spaces.remove(space.getSerialnumber());
                        } else {
                            spaces.put(space.getSerialnumber(), "不可用");
                        }
                        break;
                    case "已预约":
                        showToast("该车位已被预约");
                        break;
                    case "已使用":
                        showToast("该车位已使用");
                        break;
                    case "不可用":
                        bg.setSelected(!bg.isSelected());
                        if (spaces.containsKey(space.getSerialnumber())) {
                            spaces.remove(space.getSerialnumber());
                        } else {
                            spaces.put(space.getSerialnumber(), "未使用");
                        }
                        break;
                }
            }
        });
    }

    private void initData() {
        client.searchParkingSpace(merchant, new BaseObserver<BaseModel<ParkingSpace>>(getContext()) {
            @Override
            protected void showDialog() {

            }

            @Override
            protected void hideDialog() {

            }

            @Override
            protected void successful(BaseModel<ParkingSpace> parkingSpaceBaseModel) {
                List<ParkingSpace> datas = parkingSpaceBaseModel.getDatas();
                space_list.replaceData(datas);
            }

            @Override
            protected void defeated(BaseModel<ParkingSpace> parkingSpaceBaseModel) {
                showToast(parkingSpaceBaseModel.getMessage());
            }

            @Override
            public void onError(ExceptionHandle.ResponeThrowable e) {
                Log.e("tag", e.getMessage());
            }
        });
    }

    @OnClick(R.id.back)
    public void back() {
        if (spaces.size() > 0) {
            MaterialDialog dialog = new MaterialDialog(getContext(), MaterialDialog.getDEFAULT_BEHAVIOR());
            dialog.title(null, "警告");
            dialog.message(null, "您的设置未保存，是否退出？", null);
            dialog.positiveButton(null, "退出", materialDialog -> {
                getActivity().getSupportFragmentManager().popBackStack();
                return null;
            });
            dialog.negativeButton(null, "取消", materialDialog -> {
                return null;
            });
            dialog.show();
        } else {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    @OnClick(R.id.save)
    public void save() {
        Log.e("tag", spaces.toString());
        if (spaces.size() > 0) {
            spaces.put("merchant", merchant);
            client.updateParkingSpaceByNameAndSerialnumber(spaces, new BaseObserver<BaseModel>(getContext()) {
                @Override
                protected void showDialog() {

                }

                @Override
                protected void hideDialog() {

                }

                @Override
                protected void successful(BaseModel baseModel) {
                    showToast(baseModel.getMessage());
                    getActivity().getSupportFragmentManager().popBackStack();
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

    @Override
    public void onStart() {
        super.onStart();
        initData();
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
                    if (spaces.size() > 0) {
                        MaterialDialog dialog = new MaterialDialog(getContext(), MaterialDialog.getDEFAULT_BEHAVIOR());
                        dialog.title(null, "警告");
                        dialog.message(null, "您的设置未保存，是否退出？", null);
                        dialog.positiveButton(null, "退出", materialDialog -> {
                            getActivity().getSupportFragmentManager().popBackStack();
                            return null;
                        });
                        dialog.negativeButton(null, "取消", materialDialog -> {
                            return null;
                        });
                        dialog.show();
                    } else {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                    return true;
                }
                return false;
            }
        });
    }
}
