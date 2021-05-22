package com.lenovo.feizai.parking.merchantactivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
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
import com.lenovo.feizai.parking.net.RetrofitClient;
import com.lenovo.feizai.parking.util.FileUtil;
import com.lenovo.feizai.parking.util.GsonUtil;
import com.lenovo.feizai.parking.util.PriceInputFilter;
import com.lenovo.feizai.parking.util.SelectPhotoUtil;
import com.lenovo.feizai.parking.util.ToolUtil;
import com.luck.picture.lib.decoration.GridSpacingItemDecoration;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;
import com.orhanobut.logger.Logger;

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
public class AddChangeParkingActivity extends BaseActivity {

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
    private String city;
    private String username;
    private String oldname;
    private LatLng mylatlng;
    private Double latitude;
    private Double longitude;
    private BaseRecyclerView<String, BaseViewHolder> photo_list;
    private BaseRecyclerView<String, BaseViewHolder> certificate_list;
    private RetrofitClient client;


    public AddChangeParkingActivity() {
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

        mylatlng = null;
        initRecyclerview();
        initDate(merchantname);
    }

    @OnClick(R.id.i)
    public void i() {
        MaterialDialog dialog = new MaterialDialog(AddChangeParkingActivity.this, MaterialDialog.getDEFAULT_BEHAVIOR());
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
            Intent intent = new Intent(AddChangeParkingActivity.this, MapActivity.class);
            intent.putExtra("mylat", GsonUtil.GsonString(mylatlng));
            startActivityForResult(intent, 1);
        } else {
            startActivityForResult(MapActivity.class, 1);
        }
    }

    @OnClick(R.id.sure)
    public void sure() {
        String parking_name = name_edit.getText().toString().trim();
        String parking_address = address_edit.getText().toString().trim();
        String parking_number = number.getText().toString().trim();
        String parking_one_price_edit = one_price_edit.getText().toString().trim();
        String parking_orderone_price_edit = orderone_price_edit.getText().toString().trim();
        String parking_qq_edit = qq_edit.getText().toString().trim();
        String parking_linkman_edit = linkman_edit.getText().toString().trim();
        String parking_phone_edit = phone_edit.getText().toString().trim();

        if (TextUtils.isEmpty(parking_name)) {
            showToast("商家名不能为空");
            return;
        }
        if (TextUtils.isEmpty(parking_address)) {
            showToast("地址不能为空");
            return;
        }
        if (TextUtils.isEmpty(parking_number)) {
            showToast("车位数量不能为空");
            return;
        }
        if (TextUtils.isEmpty(parking_one_price_edit) || TextUtils.isEmpty(parking_orderone_price_edit)) {
            showToast("单价不能为空");
            return;
        }
        if (TextUtils.isEmpty(parking_qq_edit)) {
            showToast("联系QQ不能为空");
            return;
        }
        if (TextUtils.isEmpty(parking_linkman_edit)) {
            showToast("联系人不能为空");
            return;
        }
        if (TextUtils.isEmpty(parking_phone_edit)) {
            showToast("联系电话不能为空");
            return;
        }else {
            if (!ToolUtil.checkMobileNumber(parking_phone_edit)) {
                showToast("请输入正确的电话号码");
                return;
            }
        }

        ParkingInfo parkingInfo = new ParkingInfo();
        Location location = new Location();
        Rates rates = new Rates();
        ParkingNumber parkingNumber = new ParkingNumber();
        parkingInfo.setUsername(username);
        parkingInfo.setMerchantname(parking_name);
        parkingInfo.setMerchantaddress(parking_address);
        parkingInfo.setMerchantimage(null);
        parkingInfo.setBusinesslicense(null);
        parkingInfo.setPhone(parking_phone_edit);
        parkingInfo.setLinkman(parking_linkman_edit);
        parkingInfo.setQQ(parking_qq_edit);
        location.setMerchantname(parking_name);
        location.setCity(city);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        rates.setMerchantname(parking_name);
        rates.setOnehour(Float.valueOf(parking_one_price_edit));
        rates.setOtherone(Float.valueOf(parking_orderone_price_edit));
        parkingNumber.setMerchantname(parking_name);
        parkingNumber.setAllnumber(Integer.valueOf(parking_number));


        List<String> cl = certificate_list.getData();
        List<String> pl = photo_list.getData();
        if (cl.size() == 1 || pl.size() == 1) {
            showToast("您必须上传最少一张执照和停车场图片");
            return;
        }
        MultipartBody.Part[] license = new MultipartBody.Part[3];
        MultipartBody.Part[] image = new MultipartBody.Part[3];
        for (int i = 0; i < cl.size(); i++) {
            if (!cl.get(i).equals("take")) {
                MultipartBody.Part file = FileUtil.getFile(cl.get(i), "license");
                license[i] = file;
            }
        }
        for (int i = 0; i < pl.size(); i++) {
            if (!pl.get(i).equals("take")) {
                MultipartBody.Part file = FileUtil.getFile(pl.get(i), "image");
                image[i] = file;
            }
        }

        PromptDialog dialog = new PromptDialog(this);
        client.addchengmerchantinfo(oldname, GsonUtil.GsonString(parkingInfo), GsonUtil.GsonString(location), GsonUtil.GsonString(rates), GsonUtil.GsonString(parkingNumber), license, image, new BaseObserver<BaseModel>(this) {
            @Override
            protected void showDialog() {
                dialog.showLoading("正在上传");
            }

            @Override
            protected void hideDialog() {

            }

            @Override
            protected void successful(BaseModel baseModel) {
                dialog.showSuccess(baseModel.getMessage());
                showToast(baseModel.getMessage());
                finish();
            }

            @Override
            protected void defeated(BaseModel baseModel) {
                dialog.showError(baseModel.getMessage());
                showToast(baseModel.getMessage());
            }

            @Override
            public void onError(ExceptionHandle.ResponeThrowable e) {
                dialog.dismiss();
                showToast(e.getMessage());
                Logger.e(e,e.getMessage());
            }
        });
    }

    @OnClick(R.id.back)
    public void back() {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    mylatlng = GsonUtil.GsonToBean(data.getStringExtra("latlng"), LatLng.class);
                    latitude = mylatlng.latitude;
                    longitude = mylatlng.longitude;
                    address_edit.setText(GsonUtil.GsonToBean(data.getStringExtra("result"), ReverseGeoCodeResult.class).getAddress());
                    city = GsonUtil.GsonToBean(data.getStringExtra("result"), ReverseGeoCodeResult.class).getAddressDetail().city;
                }
                break;
        }
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
                audit_text.setText(merchantState.getAuditstate()+merchantState.getRemark());
                oldname = parkingInfo.getMerchantname();
                name_edit.setText(oldname);
                address_edit.setText(parkingInfo.getMerchantaddress());
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                city = location.getCity();
                number.setText(String.valueOf(parkingNumber.getAllnumber()));
                one_price_edit.setText(String.format("%.2f",rates.getOnehour()));
                orderone_price_edit.setText(String.format("%.2f",rates.getOtherone()));
                qq_edit.setText(parkingInfo.getQQ());
                phone_edit.setText(parkingInfo.getPhone());
                linkman_edit.setText(parkingInfo.getLinkman());
                mylatlng = new LatLng(latitude, longitude);
            }

            @Override
            protected void defeated(BaseModel<MerchantProperty> merchantPropertyBaseModel) {
                showToast(merchantPropertyBaseModel.getMessage());
                finish();
            }

            @Override
            public void onError(ExceptionHandle.ResponeThrowable e) {
                showToast(e.getMessage());
                Logger.e(e,e.getMessage());
                finish();
            }
        });
    }

    private void initRecyclerview() {
        List<String> photo = new ArrayList<>();
        List<String> certificate = new ArrayList<>();
        photo.add("take");
        certificate.add("take");
        certificate_list = new BaseRecyclerView<String, BaseViewHolder>(AddChangeParkingActivity.this, R.id.certificate_list) {
            @Override
            public BaseQuickAdapter<String, BaseViewHolder> initAdapter() {
                CertificateAdapter certificateAdapter = new CertificateAdapter(AddChangeParkingActivity.this, certificate);
                return certificateAdapter;
            }
        };
        certificate_list.setItemDecoration(new GridSpacingItemDecoration(3, 5, false));
        certificate_list.setLayoutManager(new GridLayoutManager(AddChangeParkingActivity.this, 3));

        photo_list = new BaseRecyclerView<String, BaseViewHolder>(AddChangeParkingActivity.this, R.id.photo_list) {
            @Override
            public BaseQuickAdapter<String, BaseViewHolder> initAdapter() {
                PhotoAdapter photoAdapter = new PhotoAdapter(AddChangeParkingActivity.this, photo);
                return photoAdapter;
            }
        };
        photo_list.setItemDecoration(new GridSpacingItemDecoration(3, 5, false));
        photo_list.setLayoutManager(new GridLayoutManager(AddChangeParkingActivity.this, 3));

        certificate_list.setNestedScrollingEnabled(false);
        photo_list.setNestedScrollingEnabled(false);

        certificate_list.setOnItemClick(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                String data = (String) adapter.getItem(position);
                if (TextUtils.equals(data, "take")) {
                    OptionDialog optionDialog = new OptionDialog(AddChangeParkingActivity.this);
                    optionDialog.setShowTitle(true);
                    optionDialog.setTitleTxt("选择图片");
                    optionDialog.setFirstOptionTxt("拍摄");
                    optionDialog.setSecondOptionTxt("从相册选择");
                    optionDialog.show();
                    optionDialog.setOnBtnClickLister(new OptionDialog.OnBtnClickLister() {
                        @Override
                        public void first() {
                            SelectPhotoUtil.shoot(AddChangeParkingActivity.this, new OnResultCallbackListener<LocalMedia>() {
                                @Override
                                public void onResult(List<LocalMedia> result) {
                                    List<String> old = certificate_list.getData();
                                    List<String> list = new ArrayList<>();

                                    for (int i = 0; i < old.size(); i++) {
                                        if (!old.get(i).equals("take"))
                                            list.add(old.get(i));
                                    }
                                    if (result.get(0).isCut()) {
                                        list.add(result.get(0).getCutPath());
                                    } else {
                                        if (result.get(0).isCompressed()) {
                                            list.add(result.get(0).getCompressPath());
                                        } else {
                                            list.add(result.get(0).getPath());
                                        }
                                    }
                                    if (list.size() < 3) {
                                        certificate_number.setText(list.size() + "/3");
                                        list.add("take");
                                    } else {
                                        certificate_number.setText(list.size() + "/3");
                                    }
                                    certificate_list.replaceData(list);
                                }

                                @Override
                                public void onCancel() {

                                }
                            });
                        }

                        @Override
                        public void second() {
                            List<String> old = certificate_list.getData();
                            List<String> list = new ArrayList<>();
                            SelectPhotoUtil.select(AddChangeParkingActivity.this, 4 - old.size(), new OnResultCallbackListener<LocalMedia>() {
                                @Override
                                public void onResult(List<LocalMedia> result) {

                                    for (int i = 0; i < old.size(); i++) {
                                        if (!old.get(i).equals("take"))
                                            list.add(old.get(i));
                                    }
                                    for (int i = 0; i < result.size(); i++) {
                                        if (result.get(i).isCut()) {
                                            list.add(result.get(i).getCutPath());
                                        } else {
                                            if (result.get(i).isCompressed()) {
                                                list.add(result.get(i).getCompressPath());
                                            } else {
                                                list.add(result.get(i).getPath());
                                            }
                                        }
                                    }
                                    if (list.size() < 3) {
                                        certificate_number.setText(list.size() + "/3");
                                        list.add("take");
                                    } else {
                                        certificate_number.setText(list.size() + "/3");
                                    }
                                    certificate_list.replaceData(list);
                                }

                                @Override
                                public void onCancel() {

                                }
                            });
                        }

                        @Override
                        public void cancle() {

                        }
                    });
                } else {
                    ShowPhotoDialog photoDialog = new ShowPhotoDialog(AddChangeParkingActivity.this);
                    photoDialog.setPhoto(data);
                    photoDialog.show();
                }
            }
        });
        photo_list.setOnItemClick(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                String data = (String) adapter.getItem(position);
                if (TextUtils.equals(data, "take")) {
                    OptionDialog optionDialog = new OptionDialog(AddChangeParkingActivity.this);
                    optionDialog.setShowTitle(true);
                    optionDialog.setTitleTxt("选择图片");
                    optionDialog.setFirstOptionTxt("拍摄");
                    optionDialog.setSecondOptionTxt("从相册选择");
                    optionDialog.show();
                    optionDialog.setOnBtnClickLister(new OptionDialog.OnBtnClickLister() {
                        @Override
                        public void first() {
                            SelectPhotoUtil.shoot(AddChangeParkingActivity.this, new OnResultCallbackListener<LocalMedia>() {
                                @Override
                                public void onResult(List<LocalMedia> result) {
                                    List<String> old = photo_list.getData();
                                    List<String> list = new ArrayList<>();

                                    for (int i = 0; i < old.size(); i++) {
                                        if (!old.get(i).equals("take"))
                                            list.add(old.get(i));
                                    }
                                    if (result.get(0).isCut()) {
                                        list.add(result.get(0).getCutPath());
                                    } else {
                                        if (result.get(0).isCompressed()) {
                                            list.add(result.get(0).getCompressPath());
                                        } else {
                                            list.add(result.get(0).getPath());
                                        }
                                    }
                                    if (list.size() < 3) {
                                        image_number.setText(list.size() + "/3");
                                        list.add("take");
                                    } else {
                                        image_number.setText(list.size() + "/3");
                                    }
                                    photo_list.replaceData(list);
                                }

                                @Override
                                public void onCancel() {

                                }
                            });
                        }

                        @Override
                        public void second() {
                            List<String> old = photo_list.getData();
                            List<String> list = new ArrayList<>();
                            SelectPhotoUtil.select(AddChangeParkingActivity.this, 4 - old.size(), new OnResultCallbackListener<LocalMedia>() {
                                @Override
                                public void onResult(List<LocalMedia> result) {


                                    for (int i = 0; i < old.size(); i++) {
                                        if (!old.get(i).equals("take"))
                                            list.add(old.get(i));
                                    }
                                    for (int i = 0; i < result.size(); i++) {
                                        if (result.get(i).isCut()) {
                                            list.add(result.get(i).getCutPath());
                                        } else {
                                            if (result.get(i).isCompressed()) {
                                                list.add(result.get(i).getCompressPath());
                                            } else {
                                                list.add(result.get(i).getPath());
                                            }
                                        }
                                    }
                                    if (list.size() < 3) {
                                        image_number.setText(list.size() + "/3");
                                        list.add("take");
                                    } else {
                                        image_number.setText(list.size() + "/3");
                                    }
                                    photo_list.replaceData(list);
                                }

                                @Override
                                public void onCancel() {

                                }
                            });
                        }

                        @Override
                        public void cancle() {

                        }
                    });
                } else {
                    ShowPhotoDialog photoDialog = new ShowPhotoDialog(AddChangeParkingActivity.this);
                    photoDialog.setPhoto(data);
                    photoDialog.show();
                }
            }
        });
        photo_list.setOnItemLongClick(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                String data = (String) adapter.getItem(position);
                if (TextUtils.equals(data, "take")) {

                } else {
                    MaterialDialog dialog = new MaterialDialog(AddChangeParkingActivity.this, MaterialDialog.getDEFAULT_BEHAVIOR());
                    dialog.title(null, "删除");
                    dialog.message(null, "确认删除该照片", dialogMessageSettings -> {
                        return null;
                    });
                    dialog.positiveButton(null, "确认", materialDialog -> {
                        photo_list.removeData(position);
                        String temp = (String) photo_list.getItem(photo_list.getItemCount() - 1);
                        if (TextUtils.equals(temp, "take")) {
                            image_number.setText((photo_list.getItemCount() - 1) + "/3");
                        } else {
                            image_number.setText(photo_list.getItemCount() + "/3");
                            photo_list.addData("take");
                        }
                        return null;
                    });
                    dialog.negativeButton(null, "取消", materialDialog -> {
                        return null;
                    });
                    dialog.show();
                }
                return true;
            }
        });
        certificate_list.setOnItemLongClick(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                String data = (String) adapter.getItem(position);
                if (TextUtils.equals(data, "take")) {

                } else {
                    MaterialDialog dialog = new MaterialDialog(AddChangeParkingActivity.this, MaterialDialog.getDEFAULT_BEHAVIOR());
                    dialog.title(null, "删除");
                    dialog.message(null, "确认删除该照片", dialogMessageSettings -> {
                        return null;
                    });
                    dialog.positiveButton(null, "确认", materialDialog -> {
                        certificate_list.removeData(position);
                        String temp = (String) certificate_list.getItem(certificate_list.getItemCount() - 1);
                        if (TextUtils.equals(temp, "take")) {
                            certificate_number.setText((certificate_list.getItemCount() - 1) + "/3");
                        } else {
                            certificate_number.setText(certificate_list.getItemCount() + "/3");
                            certificate_list.addData("take");
                        }
                        return null;
                    });
                    dialog.negativeButton(null, "取消", materialDialog -> {
                        return null;
                    });
                    dialog.show();
                }
                return true;
            }
        });
    }
}
