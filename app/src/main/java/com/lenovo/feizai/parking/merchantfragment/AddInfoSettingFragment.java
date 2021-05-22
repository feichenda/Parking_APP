package com.lenovo.feizai.parking.merchantfragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
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
import com.lenovo.feizai.parking.util.PriceInputFilter;
import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.activity.MapActivity;
import com.lenovo.feizai.parking.adapter.PhotoAdapter;
import com.lenovo.feizai.parking.base.BaseFragment;
import com.lenovo.feizai.parking.base.BaseModel;
import com.lenovo.feizai.parking.base.BaseObserver;
import com.lenovo.feizai.parking.base.BaseRecyclerView;
import com.lenovo.feizai.parking.dialog.OptionDialog;
import com.lenovo.feizai.parking.dialog.ShowPhotoDialog;
import com.lenovo.feizai.parking.entity.MerchantChange;
import com.lenovo.feizai.parking.entity.MerchantProperty;
import com.lenovo.feizai.parking.entity.MessageEvent;
import com.lenovo.feizai.parking.net.ExceptionHandle;
import com.lenovo.feizai.parking.net.RetrofitClient;
import com.lenovo.feizai.parking.util.FileUtil;
import com.lenovo.feizai.parking.util.GsonUtil;
import com.lenovo.feizai.parking.util.SelectPhotoUtil;
import com.lenovo.feizai.parking.util.ToolUtil;
import com.luck.picture.lib.decoration.GridSpacingItemDecoration;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import me.leefeng.promptlibrary.PromptDialog;
import okhttp3.MultipartBody;

import static android.app.Activity.RESULT_OK;

/**
 * @author feizai
 * @date 2021/4/8 0008 下午 2:03:33
 * @annotation
 */
public class AddInfoSettingFragment extends BaseFragment {

    @BindView(R.id.name_edit)
    EditText name_edit;
    @BindView(R.id.address_edit)
    EditText address_edit;
    @BindView(R.id.one_price_edit)
    EditText one_price_edit;
    @BindView(R.id.orderone_price_edit)
    EditText orderone_price_edit;
    @BindView(R.id.certificate_number)
    TextView certificate_number;

    private RetrofitClient client;
    private String merchantname;
    private BaseRecyclerView<String, BaseViewHolder> license_list;
    private List<String> photo;
    private Double latitude;
    private Double longitude;
    private String city;
    private LatLng mylatlng;

    public AddInfoSettingFragment() {
        super(R.layout.fragment_infosetting);
    }

    @Override
    protected void initView(View view) {
        client = RetrofitClient.getInstance(getContext());
        Bundle bundle = getArguments();
        merchantname = bundle.getString("name");
        photo = new ArrayList<>();
        photo.add("take");
        InputFilter[] filters = {new PriceInputFilter()};
        one_price_edit.setFilters(filters);
        orderone_price_edit.setFilters(filters);

        client.searchMerchant(merchantname, new BaseObserver<BaseModel<MerchantProperty>>(getContext()) {
            @Override
            protected void showDialog() {

            }

            @Override
            protected void hideDialog() {

            }

            @Override
            protected void successful(BaseModel<MerchantProperty> merchantPropertyBaseModel) {
                List<MerchantProperty> datas = merchantPropertyBaseModel.getDatas();
                MerchantProperty merchantProperty = datas.get(0);
                name_edit.setText(merchantProperty.getParkingInfo().getMerchantname());
                address_edit.setText(merchantProperty.getParkingInfo().getMerchantaddress());
                latitude = merchantProperty.getLocation().getLatitude();
                longitude = merchantProperty.getLocation().getLongitude();
                city = merchantProperty.getLocation().getCity();
                one_price_edit.setText(String.format("%.2f",merchantProperty.getRates().getOnehour()));
                orderone_price_edit.setText(String.format("%.2f",merchantProperty.getRates().getOtherone()));
            }

            @Override
            protected void defeated(BaseModel<MerchantProperty> merchantPropertyBaseModel) {
                showToast(merchantPropertyBaseModel.getMessage());
            }

            @Override
            public void onError(ExceptionHandle.ResponeThrowable e) {
                showToast(e.getMessage());
            }
        });

        license_list = new BaseRecyclerView<String, BaseViewHolder>(view, R.id.license_list) {
            @Override
            public BaseQuickAdapter<String, BaseViewHolder> initAdapter() {
                PhotoAdapter photoAdapter = new PhotoAdapter(getActivity(), photo);
                return photoAdapter;
            }
        };

        license_list.setItemDecoration(new GridSpacingItemDecoration(3, 5, false));
        license_list.setLayoutManager(new GridLayoutManager(getContext(), 3));
        license_list.setNestedScrollingEnabled(false);

        license_list.setOnItemClick(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                String data = (String) adapter.getItem(position);
                if (TextUtils.equals(data, "take")) {
                    OptionDialog optionDialog = new OptionDialog(getContext());
                    optionDialog.setShowTitle(true);
                    optionDialog.setTitleTxt("选择图片");
                    optionDialog.setFirstOptionTxt("拍摄");
                    optionDialog.setSecondOptionTxt("从相册选择");
                    optionDialog.show();
                    optionDialog.setOnBtnClickLister(new OptionDialog.OnBtnClickLister() {
                        @Override
                        public void first() {
                            SelectPhotoUtil.shoot(getActivity(), new OnResultCallbackListener<LocalMedia>() {
                                @Override
                                public void onResult(List<LocalMedia> result) {
                                    List<String> old = license_list.getData();
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
                                    license_list.replaceData(list);
                                }

                                @Override
                                public void onCancel() {

                                }
                            });
                        }

                        @Override
                        public void second() {
                            List<String> old = license_list.getData();
                            List<String> list = new ArrayList<>();
                            SelectPhotoUtil.select(getActivity(), 4 - old.size(), new OnResultCallbackListener<LocalMedia>() {
                                @Override
                                public void onResult(List<LocalMedia> result) {

                                    for (int i = 0; i < old.size(); i++) {
                                        if (!old.get(i).equals("take"))
                                            list.add(old.get(i));
                                    }
                                    for (int i = 0; i < result.size(); i++) {
                                        list.add(result.get(i).getPath());
                                    }
                                    if (list.size() < 3) {
                                        certificate_number.setText(list.size() + "/3");
                                        list.add("take");
                                    } else {
                                        certificate_number.setText(list.size() + "/3");
                                    }
                                    license_list.replaceData(list);
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
                    ShowPhotoDialog photoDialog = new ShowPhotoDialog(getContext());
                    photoDialog.setPhoto(data);
                    photoDialog.show();
                }
            }
        });

        license_list.setOnItemLongClick(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                String data = (String) adapter.getItem(position);
                if (TextUtils.equals(data, "take")) {

                } else {
                    MaterialDialog dialog = new MaterialDialog(getContext(), MaterialDialog.getDEFAULT_BEHAVIOR());
                    dialog.title(null, "删除");
                    dialog.message(null, "确认删除该照片", dialogMessageSettings -> {
                        return null;
                    });
                    dialog.positiveButton(null, "确认", materialDialog -> {
                        license_list.removeData(position);
                        String temp = (String) license_list.getItem(license_list.getItemCount() - 1);
                        if (TextUtils.equals(temp, "take")) {
                            certificate_number.setText((license_list.getItemCount() - 1) + "/3");
                        } else {
                            certificate_number.setText(license_list.getItemCount() + "/3");
                            license_list.addData("take");
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

    @OnClick(R.id.back)
    public void back() {
        EventBus.getDefault().postSticky(new MessageEvent("setting"));
    }

    @OnClick({R.id.save})
    public void save() {
        String newmerchantname = name_edit.getText().toString().trim();
        String address = address_edit.getText().toString().trim();
        String smallone = one_price_edit.getText().toString().trim();
        String small = orderone_price_edit.getText().toString().trim();
        PromptDialog dialog = new PromptDialog(getActivity());

        MerchantChange change = new MerchantChange();
        change.setUsername(ToolUtil.getUsername(getActivity()));
        change.setOldmerchantname(merchantname);

        if (TextUtils.isEmpty(newmerchantname)) {
            showToast("用户名不能为空");
        } else {
            if (TextUtils.isEmpty(address)) {
                showToast("地址不能为空");
            } else {
                if (TextUtils.isEmpty(small) || TextUtils.isEmpty(smallone)) {
                    showToast("价格不能为空");
                } else {
                    change.setNewmerchantname(newmerchantname);
                    change.setMerchantaddress(address);
                    change.setLatitude(latitude);
                    change.setLongitude(longitude);
                    change.setCity(city);
                    change.setOnehour(Float.valueOf(smallone));
                    change.setOtherone(Float.valueOf(small));
                    change.setAuditstate("未审核");
                    List<String> data = license_list.getData();
                    if (data.size() == 1) {
                        showToast("您必须上传最少一张执照");
                        return;
                    }
                    MultipartBody.Part[] parts = new MultipartBody.Part[3];
                    for (int i = 0; i < data.size(); i++) {
                        if (!data.get(i).equals("take")) {
                            MultipartBody.Part part = FileUtil.getFile(data.get(i), "image");
                            parts[i] = part;
                        }
                    }
                    String json = GsonUtil.GsonString(change);
                    client.changeParkingInfo(json, parts, new BaseObserver<BaseModel>(getContext()) {
                        @Override
                        protected void showDialog() {
                            dialog.showLoading("正在上传");
                        }

                        @Override
                        protected void hideDialog() {
                            dialog.dismiss();
                        }

                        @Override
                        protected void successful(BaseModel baseModel) {
                            dialog.showSuccess(baseModel.getMessage());
                            showToast(baseModel.getMessage());
                            getActivity().getSupportFragmentManager().popBackStack();
                        }

                        @Override
                        protected void defeated(BaseModel baseModel) {
                            showToast(baseModel.getMessage());
                            dialog.showError(baseModel.getMessage());
                        }

                        @Override
                        public void onError(ExceptionHandle.ResponeThrowable e) {
                            Logger.e(e, e.getMessage());
                        }
                    });
                }
            }
        }
    }

    @OnClick(R.id.address_edit)
    public void selectaddress() {
        LatLng latLng = new LatLng(latitude, longitude);
        Intent intent = new Intent(getActivity(), MapActivity.class);
        intent.putExtra("mylat", GsonUtil.GsonString(latLng));
        startActivityForResult(intent,1);
    }

    @OnClick(R.id.i)
    public void i() {
        MaterialDialog dialog = new MaterialDialog(getContext(), MaterialDialog.getDEFAULT_BEHAVIOR());
        dialog.title(null, "提示");
        dialog.message(null, "执照包括：营业执照，收费许可证等", null);
        dialog.positiveButton(null, "确认", materialDialog -> {
            return null;
        });
        dialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
}
