package com.lenovo.feizai.parking.merchantactivity;

import android.content.Context;
import android.content.Intent;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnItemLongClickListener;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.activity.MapActivity;
import com.lenovo.feizai.parking.adapter.CertificateAdapter;
import com.lenovo.feizai.parking.adapter.PhotoAdapter;
import com.lenovo.feizai.parking.base.BaseActivity;
import com.lenovo.feizai.parking.base.BaseModel;
import com.lenovo.feizai.parking.base.BaseObserver;
import com.lenovo.feizai.parking.base.BaseRecyclerView;
import com.lenovo.feizai.parking.dialog.OptionDialog;
import com.lenovo.feizai.parking.dialog.ShowPhotoDialog;
import com.lenovo.feizai.parking.entity.Location;
import com.lenovo.feizai.parking.entity.MerchantProperty;
import com.lenovo.feizai.parking.entity.MerchantState;
import com.lenovo.feizai.parking.entity.ParkingInfo;
import com.lenovo.feizai.parking.entity.ParkingNumber;
import com.lenovo.feizai.parking.entity.Rates;
import com.lenovo.feizai.parking.net.ExceptionHandle;
import com.lenovo.feizai.parking.net.RequestAPI;
import com.lenovo.feizai.parking.net.RetrofitClient;
import com.lenovo.feizai.parking.util.DensityUtil;
import com.lenovo.feizai.parking.util.FileUtil;
import com.lenovo.feizai.parking.util.GsonUtil;
import com.lenovo.feizai.parking.util.PriceInputFilter;
import com.lenovo.feizai.parking.util.SelectPhotoUtil;
import com.lenovo.feizai.parking.util.ToolUtil;
import com.luck.picture.lib.decoration.GridSpacingItemDecoration;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;
import com.orhanobut.logger.Logger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import me.leefeng.promptlibrary.PromptDialog;
import okhttp3.MultipartBody;

/**
 * @author feizai
 * @date 02/09/2021 009 2:15:40 PM
 */
public class SeeParkingInfoActivity extends BaseActivity {

    @BindView(R.id.audit_text)
    EditText audit_text;
    @BindView(R.id.image_number)
    TextView image_number;
    @BindView(R.id.certificate_number)
    TextView certificate_number;
    @BindView(R.id.address_edit)
    EditText address_edit;
    @BindView(R.id.name_edit)
    EditText name_edit;
    @BindView(R.id.number)
    EditText number;
    @BindView(R.id.one_price_edit)
    EditText one_price_edit;
    @BindView(R.id.orderone_price_edit)
    EditText orderone_price_edit;
    @BindView(R.id.qq_edit)
    EditText qq_edit;
    @BindView(R.id.linkman_edit)
    EditText linkman_edit;
    @BindView(R.id.phone_edit)
    EditText phone_edit;
    @BindView(R.id.sure)
    TextView sure;
    @BindView(R.id.title)
    TextView title;
    private String city;
    private String username;
    private String oldname;
    private LatLng mylatlng;
    private BaseRecyclerView<String, BaseViewHolder> photo_list;
    private BaseRecyclerView<String, BaseViewHolder> certificate_list;
    private RetrofitClient client;


    public SeeParkingInfoActivity() {
        super(R.layout.activity_change_parking);
    }

    @Override
    protected void initView() {
        Intent intent = getIntent();
        String merchantname = intent.getStringExtra("name");
        username = ToolUtil.getUsername(this);
        InputFilter[] filters = {new PriceInputFilter()};
        one_price_edit.setFilters(filters);
        orderone_price_edit.setFilters(filters);
        client = RetrofitClient.getInstance(this);

        title.setText("停车场信息");
        image_number.setVisibility(View.GONE);
        certificate_number.setVisibility(View.GONE);
        sure.setVisibility(View.GONE);
        name_edit.setEnabled(false);
        address_edit.setEnabled(false);
        number.setEnabled(false);
        one_price_edit.setEnabled(false);
        orderone_price_edit.setEnabled(false);
        linkman_edit.setEnabled(false);
        phone_edit.setEnabled(false);
        qq_edit.setEnabled(false);

        mylatlng = null;
        initRecyclerview();
        initDate(merchantname);
    }

    @OnClick(R.id.i)
    public void i() {
        MaterialDialog dialog = new MaterialDialog(SeeParkingInfoActivity.this, MaterialDialog.getDEFAULT_BEHAVIOR());
        dialog.title(null, "提示");
        dialog.message(null, "执照包括：营业执照，收费许可证等", null);
        dialog.positiveButton(null, "确认", materialDialog -> {
            return null;
        });
        dialog.show();
    }

    @OnClick(R.id.address_edit)
    public void address_edit() {
        if (mylatlng != null) {
            Intent intent = new Intent(SeeParkingInfoActivity.this, MapActivity.class);
            intent.putExtra("mylat", GsonUtil.GsonString(mylatlng));
            startActivityForResult(intent, 1);
        } else {
            startActivityForResult(MapActivity.class, 1);
        }
    }

    @OnClick(R.id.back)
    public void back() {
        finish();
    }

    private void initDate(String name) {
        client.selectParkingInfoByName(name, new BaseObserver<BaseModel<MerchantProperty>>(this) {
            @Override
            protected void showDialog() {

            }

            @Override
            protected void hideDialog() {

            }

            @Override
            protected void successful(BaseModel<MerchantProperty> merchantPropertyBaseModel) {
                MerchantProperty property = merchantPropertyBaseModel.getData();
                ParkingInfo parkingInfo = property.getParkingInfo();
                Location location = property.getLocation();
                MerchantState merchantState = property.getMerchantState();
                ParkingNumber parkingNumber = property.getParkingNumber();
                Rates rates = property.getRates();
                audit_text.setText(merchantState.getAuditstate());
                oldname = parkingInfo.getMerchantname();
                name_edit.setText(oldname);
                address_edit.setText(parkingInfo.getMerchantaddress());
                city = location.getCity();
                number.setText(String.valueOf(parkingNumber.getAllnumber()));
                one_price_edit.setText(String.format("%.2f", rates.getOnehour()));
                orderone_price_edit.setText(String.format("%.2f", rates.getOtherone()));
                qq_edit.setText(parkingInfo.getQQ());
                phone_edit.setText(parkingInfo.getPhone());
                linkman_edit.setText(parkingInfo.getLinkman());
                mylatlng = new LatLng(location.getLatitude(), location.getLongitude());
                List<String> licenses = new ArrayList<>();
                List<String> images = new ArrayList<>();
                String businesslicense = parkingInfo.getBusinesslicense();
                String[] license = businesslicense.split("&");
                for (String s : license) {
                    licenses.add(RequestAPI.baseImageURL+s);
                }
                String merchantimage = parkingInfo.getMerchantimage();
                String[] image = merchantimage.split("&");
                for (String s : image) {
                    images.add(RequestAPI.baseImageURL+s);
                }
                certificate_list.replaceData(licenses);
                photo_list.replaceData(images);
            }

            @Override
            protected void defeated(BaseModel<MerchantProperty> merchantPropertyBaseModel) {
                showToast(merchantPropertyBaseModel.getMessage());
                finish();
            }

            @Override
            public void onError(ExceptionHandle.ResponeThrowable e) {
                showToast(e.getMessage());
                Logger.e(e, e.getMessage());
                finish();
            }
        });
    }

    private void initRecyclerview() {
        class PhotoAdapter extends BaseQuickAdapter<String,BaseViewHolder>{

            private Context context;

            public PhotoAdapter(Context context, @Nullable List<String> data) {
                super(R.layout.photo_item, data);
                this.context = context;
            }

            @Override
            protected void convert(@NotNull BaseViewHolder baseViewHolder, String s) {
                ImageView photo_image = baseViewHolder.getView(R.id.photo_image);
                int width = (DensityUtil.getSreenWidth(context)-10-4)/3;
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) photo_image.getLayoutParams();
                lp.width=width;
                lp.height=width;
                photo_image.setLayoutParams(lp);
                Glide.with(context).load(s)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .placeholder(R.mipmap.image_space)
                        .error(R.mipmap.image_error)
                        .override(width,width)
                        .into(photo_image);
            }
        }

        certificate_list = new BaseRecyclerView<String, BaseViewHolder>(SeeParkingInfoActivity.this, R.id.certificate_list) {
            @Override
            public BaseQuickAdapter<String, BaseViewHolder> initAdapter() {
                PhotoAdapter photoAdapter = new PhotoAdapter(SeeParkingInfoActivity.this, null);
                return photoAdapter;
            }
        };
        certificate_list.setItemDecoration(new GridSpacingItemDecoration(3, 5, false));
        certificate_list.setLayoutManager(new GridLayoutManager(SeeParkingInfoActivity.this, 3));

        photo_list = new BaseRecyclerView<String, BaseViewHolder>(SeeParkingInfoActivity.this, R.id.photo_list) {
            @Override
            public BaseQuickAdapter<String, BaseViewHolder> initAdapter() {
                PhotoAdapter photoAdapter = new PhotoAdapter(SeeParkingInfoActivity.this, null);
                return photoAdapter;
            }
        };
        photo_list.setItemDecoration(new GridSpacingItemDecoration(3, 5, false));
        photo_list.setLayoutManager(new GridLayoutManager(SeeParkingInfoActivity.this, 3));

        certificate_list.setNestedScrollingEnabled(false);
        photo_list.setNestedScrollingEnabled(false);

        certificate_list.setOnItemClick(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                String data = (String) adapter.getItem(position);
                ShowPhotoDialog photoDialog = new ShowPhotoDialog(SeeParkingInfoActivity.this);
                photoDialog.setPhoto(data);
                photoDialog.show();
            }
        });
        photo_list.setOnItemClick(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                String data = (String) adapter.getItem(position);
                ShowPhotoDialog photoDialog = new ShowPhotoDialog(SeeParkingInfoActivity.this);
                photoDialog.setPhoto(data);
                photoDialog.show();
            }
        });
    }
}
