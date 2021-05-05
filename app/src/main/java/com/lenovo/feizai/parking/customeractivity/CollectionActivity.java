package com.lenovo.feizai.parking.customeractivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.base.BaseActivity;
import com.lenovo.feizai.parking.base.BaseModel;
import com.lenovo.feizai.parking.base.BaseObserver;
import com.lenovo.feizai.parking.base.BaseRecyclerView;
import com.lenovo.feizai.parking.dialog.OptionDialog;
import com.lenovo.feizai.parking.entity.CarInfo;
import com.lenovo.feizai.parking.entity.CollectionInfo;
import com.lenovo.feizai.parking.entity.Location;
import com.lenovo.feizai.parking.net.ExceptionHandle;
import com.lenovo.feizai.parking.net.RetrofitClient;
import com.lenovo.feizai.parking.util.GsonUtil;
import com.lenovo.feizai.parking.util.ToolUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;

/**
 * @author feizai
 * @date 2021/3/28 0028 下午 7:13:45
 * @annotation
 */
public class CollectionActivity extends BaseActivity {

    private BaseRecyclerView<CollectionInfo, BaseViewHolder> address_list;
    private BaseRecyclerView<CarInfo, BaseViewHolder> car_list;
    private List<CollectionInfo> address;
    private List<CarInfo> cars;
    private RetrofitClient client;
    private String nowLat;

    public CollectionActivity() {
        super(R.layout.activity_collection);
    }

    @Override
    protected void initView() {
        Intent intent = getIntent();
        nowLat = intent.getStringExtra("nowLat");
        client = RetrofitClient.getInstance(this);
        address = new ArrayList<>();
        cars = new ArrayList<>();
        initAddress();
        initCar();
        CollectionInfo collection = new CollectionInfo();
        collection.setRemark("添加");
        address.add(collection);
        address_list.replaceData(address);
        CarInfo carInfo = new CarInfo();
        carInfo.setCar_license("添加");
        cars.add(carInfo);
        car_list.replaceData(cars);
    }

    private void initCar() {
        car_list = new BaseRecyclerView<CarInfo, BaseViewHolder>(CollectionActivity.this, R.id.collection_car_list) {
            @Override
            public BaseQuickAdapter<CarInfo, BaseViewHolder> initAdapter() {
                class CarInfoAdapter extends BaseQuickAdapter<CarInfo, BaseViewHolder> {

                    Context context;

                    public CarInfoAdapter(Context context, @Nullable List<CarInfo> data) {
                        super(R.layout.collection_item, data);
                        this.context = context;
                    }

                    @Override
                    protected void convert(@NotNull BaseViewHolder baseViewHolder, CarInfo carInfo) {
                        ImageView image = baseViewHolder.getView(R.id.image);
                        if (carInfo.getCar_license() == "添加") {
                            Glide.with(context).load(R.mipmap.add).override(50, 50).into(image);
                            baseViewHolder.setText(R.id.name, "添加车辆");
                            baseViewHolder.setTextColor(R.id.name, Color.GRAY);
                            baseViewHolder.setVisible(R.id.more, false);
                        } else {
                            Glide.with(context).load(R.mipmap.car).override(50, 50).into(image);
                            baseViewHolder.setText(R.id.name, carInfo.getCar_license());
                            baseViewHolder.setVisible(R.id.more, true);
                        }
                    }
                }
                return new CarInfoAdapter(CollectionActivity.this, null);
            }
        };

        car_list.setOnItemClick(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                CarInfo carInfo = (CarInfo) adapter.getItem(position);
                if (carInfo.getCar_license() == "添加") {
                    Intent intent = new Intent(CollectionActivity.this, AddCarLicenseActivity.class);
                    intent.putExtra("code", 1);
                    intent.putExtra("title", "添加车牌号");
                    startActivityForResult(intent, 3);
                } else {
                    OptionDialog dialog = new OptionDialog(CollectionActivity.this);
                    dialog.setShowTitle(false);
                    dialog.setFirstOptionTxt("修改");
                    dialog.setSecondOptionTxt("删除");
                    dialog.setSecondOptionColor(Color.RED);
                    dialog.setOnBtnClickLister(new OptionDialog.OnBtnClickLister() {
                        @Override
                        public void first() {
                            Intent intent = new Intent(CollectionActivity.this, AddCarLicenseActivity.class);
                            intent.putExtra("code", 2);
                            intent.putExtra("id", carInfo.getId());
                            intent.putExtra("title", "修改车牌号");
                            startActivityForResult(intent, 4);
                        }

                        @Override
                        public void second() {
                            MaterialDialog dialog = new MaterialDialog(CollectionActivity.this, MaterialDialog.getDEFAULT_BEHAVIOR());
                            dialog.title(null, "提示");
                            dialog.message(null, "确认删除该车辆信息", dialogMessageSettings -> {
                                return null;
                            });
                            dialog.positiveButton(null, "确认", materialDialog -> {
                                client.deleteCar(carInfo, new BaseObserver<BaseModel>(CollectionActivity.this) {
                                    @Override
                                    protected void showDialog() {

                                    }

                                    @Override
                                    protected void hideDialog() {

                                    }

                                    @Override
                                    protected void successful(BaseModel baseModel) {
                                        showToast(baseModel.getMessage());
                                        car_list.removeData(carInfo);
                                    }

                                    @Override
                                    protected void defeated(BaseModel baseModel) {
                                        showToast(baseModel.getMessage());
                                    }

                                    @Override
                                    public void onError(ExceptionHandle.ResponeThrowable e) {

                                    }
                                });
                                return null;
                            });
                            dialog.negativeButton(null, "取消", materialDialog -> {
                                dialog.dismiss();
                                return null;
                            });
                            dialog.show();
                        }

                        @Override
                        public void cancle() {

                        }
                    });
                    dialog.show();
                }
            }
        });
    }

    private void initAddress() {
        address_list = new BaseRecyclerView<CollectionInfo, BaseViewHolder>(CollectionActivity.this, R.id.collection_address_list) {
            @Override
            public BaseQuickAdapter<CollectionInfo, BaseViewHolder> initAdapter() {
                class CollectionAddressAdapter extends BaseQuickAdapter<CollectionInfo, BaseViewHolder> {

                    Context context;

                    public CollectionAddressAdapter(Context context, @Nullable List<CollectionInfo> data) {
                        super(R.layout.collection_item, data);
                        this.context = context;
                    }

                    @Override
                    protected void convert(@NotNull BaseViewHolder baseViewHolder, CollectionInfo collection) {
                        ImageView image = baseViewHolder.getView(R.id.image);
                        if (collection.getRemark() == "添加") {
                            Glide.with(context).load(R.mipmap.add).override(50, 50).into(image);
                            baseViewHolder.setText(R.id.name, "添加地址");
                            baseViewHolder.setTextColor(R.id.name, Color.GRAY);
                            baseViewHolder.setVisible(R.id.more, false);
                        } else {
                            Glide.with(context).load(R.mipmap.address2).override(50, 50).into(image);
                            baseViewHolder.setText(R.id.name, collection.getRemark());
                            baseViewHolder.setVisible(R.id.more, true);
                        }
                    }
                }
                CollectionAddressAdapter adapter = new CollectionAddressAdapter(CollectionActivity.this, null);
                adapter.addChildClickViewIds(R.id.more);
                return adapter;
            }
        };

        address_list.setOnItemClick(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                CollectionInfo collection = (CollectionInfo) adapter.getItem(position);
                if (collection.getRemark() == "添加") {
                    Intent intent = new Intent(CollectionActivity.this, SearchParkingActivity.class);
                    intent.putExtra("code", 1);
                    startActivityForResult(intent, 1);
                } else {
                    Intent intent = new Intent(CollectionActivity.this, ParkingDetailedInfoActivity.class);
                    Location location = new Location();
                    location.setMerchantname(collection.getRemark());
                    location.setLatitude(collection.getLatitude());
                    location.setLongitude(collection.getLongitude());
                    intent.putExtra("location", GsonUtil.GsonString(location));
                    if (!nowLat.isEmpty()) {
                        intent.putExtra("nowLatlng", nowLat);
                    }
                    startActivity(intent);
                }
            }
        });

        address_list.setOnItemChildClick(new OnItemChildClickListener() {
            @Override
            public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                CollectionInfo collectionInfo = (CollectionInfo) adapter.getItem(position);
                if (view.getId() == R.id.more) {
                    OptionDialog dialog = new OptionDialog(CollectionActivity.this);
                    dialog.setShowTitle(false);
                    dialog.setFirstOptionTxt("修改地址");
                    dialog.setSecondOptionTxt("删除");
                    dialog.setSecondOptionColor(Color.RED);
                    dialog.setOnBtnClickLister(new OptionDialog.OnBtnClickLister() {
                        @Override
                        public void first() {
                            Intent intent = new Intent(CollectionActivity.this, SearchParkingActivity.class);
                            intent.putExtra("code", 2);
                            intent.putExtra("id", collectionInfo.getId());
                            startActivityForResult(intent, 2);
                        }

                        @Override
                        public void second() {
                            MaterialDialog dialog = new MaterialDialog(CollectionActivity.this, MaterialDialog.getDEFAULT_BEHAVIOR());
                            dialog.title(null, "提示");
                            dialog.message(null, "确认取消收藏该停车场", dialogMessageSettings -> {
                                return null;
                            });
                            dialog.positiveButton(null, "确认", materialDialog -> {
                                client.deleteCollection(collectionInfo, new BaseObserver<BaseModel>(CollectionActivity.this) {
                                    @Override
                                    protected void showDialog() {

                                    }

                                    @Override
                                    protected void hideDialog() {

                                    }

                                    @Override
                                    protected void successful(BaseModel baseModel) {
                                        showToast(baseModel.getMessage());
                                        address_list.removeData(collectionInfo);
                                    }

                                    @Override
                                    protected void defeated(BaseModel baseModel) {
                                        showToast(baseModel.getMessage());
                                    }

                                    @Override
                                    public void onError(ExceptionHandle.ResponeThrowable e) {
//                                                    showToast(e.getMessage());
                                    }
                                });
                                return null;
                            });
                            dialog.negativeButton(null, "取消", materialDialog -> {
                                dialog.dismiss();
                                return null;
                            });
                            dialog.show();
                        }

                        @Override
                        public void cancle() {

                        }
                    });
                    dialog.show();
                }
            }
        });
    }

    private void getData(String username) {
        client.selectCollectionByCustomer(username, new BaseObserver<BaseModel<CollectionInfo>>(CollectionActivity.this) {
            @Override
            protected void showDialog() {

            }

            @Override
            protected void hideDialog() {

            }

            @Override
            protected void successful(BaseModel<CollectionInfo> collectionInfoBaseModel) {
                List<CollectionInfo> datas = collectionInfoBaseModel.getDatas();
                CollectionInfo collection = new CollectionInfo();
                collection.setRemark("添加");
                datas.add(collection);
                address_list.replaceData(datas);
            }

            @Override
            protected void defeated(BaseModel<CollectionInfo> collectionInfoBaseModel) {

            }

            @Override
            public void onError(ExceptionHandle.ResponeThrowable e) {

            }
        });
        client.selectCarByUsername(username, new BaseObserver<BaseModel<CarInfo>>(CollectionActivity.this) {
            @Override
            protected void showDialog() {

            }

            @Override
            protected void hideDialog() {

            }

            @Override
            protected void successful(BaseModel<CarInfo> carInfoBaseModel) {
                List<CarInfo> datas = carInfoBaseModel.getDatas();
                CarInfo carInfo = new CarInfo();
                carInfo.setCar_license("添加");
                datas.add(carInfo);
                car_list.replaceData(datas);
            }

            @Override
            protected void defeated(BaseModel<CarInfo> carInfoBaseModel) {

            }

            @Override
            public void onError(ExceptionHandle.ResponeThrowable e) {

            }
        });
    }

    @OnClick(R.id.back)
    public void back() {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    address_list.cleanData();
                    getData(ToolUtil.getUsername(CollectionActivity.this));
                }
                break;
            case 2:
                if (resultCode == RESULT_OK) {
                    address_list.cleanData();
                    getData(ToolUtil.getUsername(CollectionActivity.this));
                }
                break;
            case 3:
                if (resultCode == RESULT_OK) {
                    car_list.cleanData();
                    getData(ToolUtil.getUsername(CollectionActivity.this));
                }
                break;
            case 4:
                if (resultCode == RESULT_OK) {
                    car_list.cleanData();
                    getData(ToolUtil.getUsername(CollectionActivity.this));
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        getData(ToolUtil.getUsername(this));
    }
}
