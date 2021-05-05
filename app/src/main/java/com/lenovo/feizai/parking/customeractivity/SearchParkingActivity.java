package com.lenovo.feizai.parking.customeractivity;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.base.BaseActivity;
import com.lenovo.feizai.parking.base.BaseModel;
import com.lenovo.feizai.parking.base.BaseObserver;
import com.lenovo.feizai.parking.base.BaseRecyclerView;
import com.lenovo.feizai.parking.entity.CollectionInfo;
import com.lenovo.feizai.parking.entity.Location;
import com.lenovo.feizai.parking.entity.ParkingInfo;
import com.lenovo.feizai.parking.entity.MerchantProperty;
import com.lenovo.feizai.parking.net.ExceptionHandle;
import com.lenovo.feizai.parking.net.RetrofitClient;
import com.lenovo.feizai.parking.util.ToolUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author feizai
 * @date 2021/3/31 0031 下午 2:45:54
 * @annotation
 */
public class SearchParkingActivity extends BaseActivity {

    @BindView(R.id.search_edit)
    EditText search_edit;
    BaseRecyclerView<MerchantProperty, BaseViewHolder> merchant_list;
    RetrofitClient client;
    private int code,id;

    public SearchParkingActivity() {
        super(R.layout.activity_search);
    }

    @Override
    protected void initView() {
        Intent intent = getIntent();
        code = intent.getIntExtra("code", -1);
        id = intent.getIntExtra("id", -1);
        client = RetrofitClient.getInstance(this);

        merchant_list = new BaseRecyclerView<MerchantProperty, BaseViewHolder>(this, R.id.search_result) {
            @Override
            public BaseQuickAdapter<MerchantProperty, BaseViewHolder> initAdapter() {
                class ParkingAdapter extends BaseQuickAdapter<MerchantProperty, BaseViewHolder> {

                    private Context context;

                    public ParkingAdapter(Context context, @Nullable List<MerchantProperty> data) {
                        super(R.layout.address_item, data);
                        this.context = context;
                    }

                    @Override
                    protected void convert(@NotNull BaseViewHolder baseViewHolder, MerchantProperty merchantProperty) {
                        ParkingInfo parkingInfo = merchantProperty.getParkingInfo();
                        baseViewHolder.setText(R.id.address_name, parkingInfo.getMerchantname());
                        baseViewHolder.setText(R.id.address_info, parkingInfo.getMerchantaddress());
                    }
                }
                return new ParkingAdapter(SearchParkingActivity.this, null);
            }
        };

        merchant_list.setOnItemClick(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                CollectionInfo info = new CollectionInfo();
                MerchantProperty merchantProperty = (MerchantProperty) adapter.getItem(position);
                ParkingInfo parkingInfo = merchantProperty.getParkingInfo();
                Location location = merchantProperty.getLocation();
                info.setUsername(ToolUtil.getUsername(SearchParkingActivity.this));
                info.setRemark(parkingInfo.getMerchantname());
                info.setAddress(parkingInfo.getMerchantaddress());
                info.setLongitude(location.getLongitude());
                info.setLatitude(location.getLatitude());
                if (id!=-1)
                    info.setId(id);
                setData(info);
            }
        });
    }

    @OnClick(R.id.search)
    public void search() {
        String name = search_edit.getText().toString().trim();
        searchParking(name);
    }

    @OnClick(R.id.clean)
    public void clean() {
        search_edit.setText("");
        merchant_list.cleanData();
    }

    @OnClick(R.id.back)
    public void back() {
        finish();
    }

    private void setData(CollectionInfo info){
        if (code==1) {
            client.addCollection(info, new BaseObserver<BaseModel>(SearchParkingActivity.this) {
                @Override
                protected void showDialog() {

                }

                @Override
                protected void hideDialog() {

                }

                @Override
                protected void successful(BaseModel baseModel) {
                    showToast(baseModel.getMessage());
                    setResult(RESULT_OK);
                    finish();
                }

                @Override
                protected void defeated(BaseModel baseModel) {
                    if (baseModel.getCode() == 201)
                        showToast(baseModel.getMessage());
                    else
                        showToast(baseModel.getMessage());
                }

                @Override
                public void onError(ExceptionHandle.ResponeThrowable e) {

                }
            });
        }
        if (code==2){
            client.updateCollection(info, new BaseObserver<BaseModel>(SearchParkingActivity.this) {
                @Override
                protected void showDialog() {

                }

                @Override
                protected void hideDialog() {

                }

                @Override
                protected void successful(BaseModel baseModel) {
                    showToast(baseModel.getMessage());
                    setResult(RESULT_OK);
                    finish();
                }

                @Override
                protected void defeated(BaseModel baseModel) {
                    if (baseModel.getCode() == 201)
                        showToast(baseModel.getMessage());
                    else
                        showToast(baseModel.getMessage());
                }

                @Override
                public void onError(ExceptionHandle.ResponeThrowable e) {

                }
            });
        }
    }

    private void searchParking(String name) {
        client.searchMerchant(name, new BaseObserver<BaseModel<MerchantProperty>>(this) {
            @Override
            protected void showDialog() {

            }

            @Override
            protected void hideDialog() {

            }

            @Override
            protected void successful(BaseModel<MerchantProperty> merchantPropertyBaseModel) {
                merchant_list.cleanData();
                merchant_list.replaceData(merchantPropertyBaseModel.getDatas());
            }

            @Override
            protected void defeated(BaseModel<MerchantProperty> merchantPropertyBaseModel) {
                merchant_list.cleanData();
                showToast(merchantPropertyBaseModel.getMessage());
            }

            @Override
            public void onError(ExceptionHandle.ResponeThrowable e) {

            }
        });
    }
}
