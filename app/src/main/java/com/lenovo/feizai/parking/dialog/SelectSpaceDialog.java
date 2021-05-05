package com.lenovo.feizai.parking.dialog;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.base.BaseModel;
import com.lenovo.feizai.parking.base.BaseObserver;
import com.lenovo.feizai.parking.base.BaseRecyclerView;
import com.lenovo.feizai.parking.entity.ParkingSpace;
import com.lenovo.feizai.parking.net.ExceptionHandle;
import com.lenovo.feizai.parking.net.RetrofitClient;
import com.luck.picture.lib.decoration.GridSpacingItemDecoration;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Logger;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author feizai
 * @date 2021/4/21 0021 下午 2:59:42
 */
public class SelectSpaceDialog {

    private BaseRecyclerView space_list;
    private RetrofitClient client;
    private int sSpace;
    private OnDialogClickListener mlistener;
    private Context context;
    private MaterialDialog dialog;

    public SelectSpaceDialog(Context context,String merchant,int space) {
        this.context = context;
        this.sSpace = space;
        View view = onCreateView(merchant);
        if (dialog == null) {
            dialog = new MaterialDialog(context, MaterialDialog.getDEFAULT_BEHAVIOR());
            dialog.addContentView(view,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    public View onCreateView(String merchant) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_space, null, false);
        initView(view);
        client = RetrofitClient.getInstance(context);
        client.searchParkingSpace(merchant, new BaseObserver<BaseModel<ParkingSpace>>(context) {
            @Override
            protected void showDialog() {

            }

            @Override
            protected void hideDialog() {

            }

            @Override
            protected void successful(BaseModel<ParkingSpace> parkingSpaceBaseModel) {
                List<ParkingSpace> parkingSpaces = parkingSpaceBaseModel.getDatas();
                space_list.replaceData(parkingSpaces);
            }

            @Override
            protected void defeated(BaseModel<ParkingSpace> parkingSpaceBaseModel) {

            }

            @Override
            public void onError(ExceptionHandle.ResponeThrowable e) {

            }
        });
        return view;
    }

    private void initView(View view) {
        space_list = new BaseRecyclerView<ParkingSpace, BaseViewHolder>(view, R.id.space_list) {
            @Override
            public BaseQuickAdapter<ParkingSpace, BaseViewHolder> initAdapter() {
                class ParkingSpaceAdapter extends BaseQuickAdapter<ParkingSpace, BaseViewHolder> {

                    public ParkingSpaceAdapter(@org.jetbrains.annotations.Nullable List<ParkingSpace> data) {
                        super(R.layout.table_item, data);
                    }

                    @Override
                    protected void convert(@NotNull BaseViewHolder baseViewHolder, ParkingSpace parkingSpace) {
                        baseViewHolder.setText(R.id.serial, parkingSpace.getSerialnumber() + "");
                        baseViewHolder.setTextColor(R.id.serial, Color.BLACK);
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

        space_list.setItemDecoration(new GridSpacingItemDecoration(5, 10, false));
        space_list.setLayoutManager(new GridLayoutManager(context, 5));
        space_list.setOnItemClick(new OnItemClickListener() {
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
                        View bg = view.findViewById(R.id.bg);
                        if (bg.isSelected()) {
                            sSpace = 0;
                            bg.setSelected(!bg.isSelected());
                        }
                    } else {
                        showToast("您已选择" + sSpace + "号车位");
                    }
                }
            }
        });

        view.findViewById(R.id.esc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        view.findViewById(R.id.sure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sSpace > 0) {
                    mlistener.onDialogSureClick(sSpace);
                    dialog.dismiss();
                } else {
                    showToast("您未选择车位");
                }
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

    public interface OnDialogClickListener {
        void onDialogSureClick(int poistion);
    }

    public void setOnDialogListener(OnDialogClickListener dialogClickListener){
        this.mlistener = dialogClickListener;
    }


    private void showToast(String content) {
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }
}
