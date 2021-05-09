package com.lenovo.feizai.parking.customeractivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.google.android.material.navigation.NavigationView;
import com.lenovo.feizai.parking.ALiPayactivity.ScannerPayActivity;
import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.activity.LoginAcitivity;
import com.lenovo.feizai.parking.base.BaseLocationActivity;
import com.lenovo.feizai.parking.base.BaseModel;
import com.lenovo.feizai.parking.base.BaseObserver;
import com.lenovo.feizai.parking.base.BaseRecyclerView;
import com.lenovo.feizai.parking.camera.CaptureActivity;
import com.lenovo.feizai.parking.customview.ScrollViewGroup;
import com.lenovo.feizai.parking.dialog.ShowPhotoDialog;
import com.lenovo.feizai.parking.entity.CheckInfo;
import com.lenovo.feizai.parking.entity.Customer;
import com.lenovo.feizai.parking.entity.Location;
import com.lenovo.feizai.parking.activity.MapActivity;
import com.lenovo.feizai.parking.entity.MerchantProperty;
import com.lenovo.feizai.parking.net.ExceptionHandle;
import com.lenovo.feizai.parking.net.RequestAPI;
import com.lenovo.feizai.parking.net.RetrofitClient;
import com.lenovo.feizai.parking.util.DensityUtil;
import com.lenovo.feizai.parking.util.GsonUtil;
import com.lenovo.feizai.parking.util.ToolUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

import static com.lenovo.feizai.parking.camera.CaptureActivity.REQUEST_QR_CODE;

/**
 * @author feizai
 * @date 12/24/2020 024 11:56:20 PM
 * @annotation 用户主界面
 */
public class CustomerMainActivity extends BaseLocationActivity{

    @BindView(R.id.bmapView)
    MapView mapView;
    @BindView(R.id.mybar)
    Toolbar mybar;
    @BindView(R.id.drawerlayout)
    DrawerLayout layout;
    @BindView(R.id.navigationview)
    NavigationView navigationview;
    @BindView(R.id.myhome)
    ImageButton myhome;
    @BindView(R.id.search_edit)
    EditText search_edit;
    @BindView(R.id.home_collection_state)
    TextView home_collection_state;
    @BindView(R.id.company_collection_state)
    TextView company_collection_state;
    @BindView(R.id.search_view_group)
    ScrollViewGroup serach_view_group;

    private BaiduMap baiduMap;
    private LatLng nowLatlng;//当前经纬度
    private ActionBar actionbar;
    private BaseRecyclerView<Location, BaseViewHolder> recyclerview;
    private List<Location> datas;
    private String[] permissions = {
            Manifest.permission.CAMERA,
    };
    private String username;
    private boolean isHome, isCompany, isFirst, isLocal;
    private Customer myCustomer;
    private RetrofitClient client;
    private ImageView profile_image;
    private TextView phone_text;
    private TextView name_text;

    public CustomerMainActivity() {
        super(R.layout.activity_customer_main);
    }

    @Override
    protected void initView() {
        SharedPreferences preferences = getSharedPreferences("userdata", Context.MODE_PRIVATE);
        username = preferences.getString("username", "");
        client = RetrofitClient.getInstance(CustomerMainActivity.this);

        baiduMap = mapView.getMap();
        mapView.showZoomControls(false);
        nowLatlng = null;

        requestLocation();

        isFirst = true;
        isLocal = true;
        isHome = true;
        isCompany = true;

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setSupportActionBar(mybar);

        actionbar = getSupportActionBar();

        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.mipmap.menu);
            actionbar.setTitle("正在定位···");
        }

        ScrollViewGroup.LayoutParams mapViewlp = mapView.getLayoutParams();
        mapViewlp.height = DensityUtil.getSreenHeight(this) - DensityUtil.px2dip(CustomerMainActivity.this,serach_view_group.getTargetInitBottom()) * 2;

        initNavigationview();

        //地图点击事件
        baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                setMapOverlay(latLng);//显示我点击的点
                search(latLng);
            }

            @Override
            public void onMapPoiClick(MapPoi mapPoi) {
                setMapOverlay(mapPoi.getPosition());//显示我点击的点
                search(mapPoi.getPosition());
            }
        });

        baiduMap.setOnMapTouchListener(new BaiduMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                isLocal = false;
            }
        });

        init();

        search_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLocal = false;
                startActivityForResult(SearchActivity.class, 1);
            }
        });

    }

    private void initNavigationview() {
        navigationview.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.collection: {
                        Intent intent = new Intent(CustomerMainActivity.this, CollectionActivity.class);
                        if (nowLatlng != null) {
                            intent.putExtra("nowLat", GsonUtil.GsonString(nowLatlng));
                        }
                        startActivity(intent);
                    }
                    break;
                    case R.id.form: {
                        startActivity(CustomerOrderActivity.class);
                    }
                    break;
                    case R.id.setting: {
                        startActivity(CustomerSettingActivity.class);
                    }
                    break;
                    case R.id.logout: {
                        SharedPreferences preferences = getSharedPreferences("userdata", Context.MODE_PRIVATE);
                        preferences.edit().clear().commit();
                        startActivity(LoginAcitivity.class);
                        finish();
                    }
                    break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    //地图上自定义标点
    private void setMapOverlay(LatLng point, int Res) {
        baiduMap.clear();
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(Res);
        OverlayOptions option = new MarkerOptions().position(point).icon(bitmap);
        baiduMap.addOverlay(option);
    }

    //搜索停车场
    private void search(LatLng position) {
        Location location = new Location();
        location.setLatitude(position.latitude);
        location.setLongitude(position.longitude);
        /*client.searchParking(location, new BaseObserver<BaseModel<Location>>(CustomerMainActivity.this) {
            @Override
            protected void showDialog() {

            }

            @Override
            protected void hideDialog() {

            }

            @Override
            protected void successful(BaseModel<Location> locationBaseModel) {
                List<Location> datas = locationBaseModel.getDatas();
                for (Location parking : datas) {
                    LatLng lng = new LatLng(parking.getLatitude(), parking.getLongitude());
                    BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.p);
                    OverlayOptions option = new MarkerOptions().position(lng).icon(bitmap);
                    baiduMap.addOverlay(option);
                }
                recyclerview.replaceData(datas);
            }

            @Override
            protected void defeated(BaseModel<Location> locationBaseModel) {
                Toast.makeText(CustomerMainActivity.this, "你所选地点附近暂无停车场", Toast.LENGTH_SHORT).show();
                recyclerview.cleanData();
            }

            @Override
            public void onError(ExceptionHandle.ResponeThrowable e) {

            }
        });*/

        client.searchParking(location, new BaseObserver<BaseModel<MerchantProperty>>(CustomerMainActivity.this) {
            @Override
            protected void showDialog() {

            }

            @Override
            protected void hideDialog() {

            }

            @Override
            protected void successful(BaseModel<MerchantProperty> merchantPropertyBaseModel) {
                List<MerchantProperty> datas = merchantPropertyBaseModel.getDatas();
                List<Location> locations = new ArrayList<>();
                for (MerchantProperty data : datas) {
                    Location parking = data.getLocation();
                    LatLng lng = new LatLng(parking.getLatitude(), parking.getLongitude());
                    BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.p);
                    OverlayOptions option = new MarkerOptions().position(lng).icon(bitmap);
                    baiduMap.addOverlay(option);
                    locations.add(parking);
                }
                recyclerview.replaceData(locations);
            }

            @Override
            protected void defeated(BaseModel<MerchantProperty> merchantPropertyBaseModel) {
                showToast("你所选地点附近暂无停车场");
                recyclerview.cleanData();
            }

            @Override
            public void onError(ExceptionHandle.ResponeThrowable e) {

            }
        });
    }

    @OnClick(R.id.scanner)
    public void scanner() {
        requestPermission(2, permissions, new Runnable() {
            @Override
            public void run() {
                startActivityForResult(CaptureActivity.class, REQUEST_QR_CODE);
            }
        }, new Runnable() {
            @Override
            public void run() {
                showToast("您已禁止应用获取相机权限");
            }
        }, new Runnable() {
            @Override
            public void run() {
                MaterialDialog dialog = new MaterialDialog(CustomerMainActivity.this, MaterialDialog.getDEFAULT_BEHAVIOR());
                dialog.title(null, "警告");
                dialog.icon(null, getResources().getDrawable(R.drawable.ic_error));
                dialog.message(null, "跳转到设置以获取相机权限", dialogMessageSettings -> {
                    return null;
                });
                dialog.positiveButton(null, "确认", materialDialog -> {
                    startActivity(getAppDetailSettingIntent());
                    return null;
                });
                dialog.negativeButton(null, "取消", materialDialog -> {

                    return null;
                });
                dialog.show();
            }
        });
    }

    @OnClick(R.id.ing)
    public void ing() {
        startActivity(CustomerIngActivity.class);
    }

    @OnClick(R.id.myhome)
    public void home() {
        if (nowLatlng != null) {
            baiduMap.clear();
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(nowLatlng);//设置经纬度
            baiduMap.animateMapStatus(update);//地图移动到设定位置
            update = MapStatusUpdateFactory.zoomTo(15.0f);
            baiduMap.animateMapStatus(update);
            search(nowLatlng);
        } else {
            showToast("未获取到当前位置");
        }
    }

    @OnClick(R.id.gohome)
    public void gohome() {
        if (isHome) {
            startActivityForResult(MapActivity.class, 2);
        } else {
            isLocal = false;
            Location location = new Location();
            location.setLongitude(myCustomer.getHome_longitude());
            location.setLatitude(myCustomer.getHome_latitude());
            LatLng home = new LatLng(myCustomer.getHome_latitude(), myCustomer.getHome_longitude());
            setMapOverlay(home, R.mipmap.my_position);
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(home);//设置经纬度
            baiduMap.animateMapStatus(update);//地图移动到设定位置
            /*client.searchParking(location, new BaseObserver<BaseModel<Location>>(CustomerMainActivity.this) {
                @Override
                protected void showDialog() {

                }

                @Override
                protected void hideDialog() {

                }

                @Override
                protected void successful(BaseModel<Location> locationBaseModel) {
                    List<Location> parkings = locationBaseModel.getDatas();
                    for (Location parking : parkings) {
                        LatLng lng = new LatLng(parking.getLatitude(), parking.getLongitude());
                        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.p);
                        OverlayOptions option = new MarkerOptions().position(lng).icon(bitmap);
                        baiduMap.addOverlay(option);
                    }
                    MapStatus.Builder builder = new MapStatus.Builder();
                    builder.zoom(15.0f);
                    baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                    datas = parkings;
                    recyclerview.replaceData(datas);
                    Log.e("tag", parkings.toString());
                }

                @Override
                protected void defeated(BaseModel<Location> locationBaseModel) {
                    recyclerview.cleanData();
                    showToast("您附近暂无停车场");
                }

                @Override
                public void onError(ExceptionHandle.ResponeThrowable e) {

                }
            });*/
            client.searchParking(location, new BaseObserver<BaseModel<MerchantProperty>>(CustomerMainActivity.this) {
                @Override
                protected void showDialog() {

                }

                @Override
                protected void hideDialog() {

                }

                @Override
                protected void successful(BaseModel<MerchantProperty> merchantPropertyBaseModel) {
                    List<MerchantProperty> parkings = merchantPropertyBaseModel.getDatas();
                    datas = new ArrayList<>();
                    for (MerchantProperty data : parkings) {
                        Location parking = data.getLocation();
                        LatLng lng = new LatLng(parking.getLatitude(), parking.getLongitude());
                        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.p);
                        OverlayOptions option = new MarkerOptions().position(lng).icon(bitmap);
                        baiduMap.addOverlay(option);
                        datas.add(parking);
                    }
                    MapStatus.Builder builder = new MapStatus.Builder();
                    builder.zoom(15.0f);
                    baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                    recyclerview.replaceData(datas);
                    Log.e("tag", parkings.toString());
                }

                @Override
                protected void defeated(BaseModel<MerchantProperty> merchantPropertyBaseModel) {
                    recyclerview.cleanData();
                    showToast("您附近暂无停车场");
                }

                @Override
                public void onError(ExceptionHandle.ResponeThrowable e) {

                }
            });
        }
    }

    @OnClick(R.id.gocompany)
    public void gocompany() {
        if (isCompany) {
            startActivityForResult(MapActivity.class, 3);
        } else {
            isLocal = false;
            Location location = new Location();
            location.setLongitude(myCustomer.getCompany_longitude());
            location.setLatitude(myCustomer.getCompany_latitude());
            LatLng company = new LatLng(myCustomer.getCompany_latitude(), myCustomer.getCompany_longitude());
            setMapOverlay(company, R.mipmap.my_position);
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(company);//设置经纬度
            baiduMap.animateMapStatus(update);//地图移动到设定位置
            client.searchParking(location, new BaseObserver<BaseModel<MerchantProperty>>(CustomerMainActivity.this) {
                @Override
                protected void showDialog() {

                }

                @Override
                protected void hideDialog() {

                }

                @Override
                protected void successful(BaseModel<MerchantProperty> merchantPropertyBaseModel) {
                    List<MerchantProperty> parkings = merchantPropertyBaseModel.getDatas();
                    datas = new ArrayList<>();
                    for (MerchantProperty data : parkings) {
                        Location parking = data.getLocation();
                        LatLng lng = new LatLng(parking.getLatitude(), parking.getLongitude());
                        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.p);
                        OverlayOptions option = new MarkerOptions().position(lng).icon(bitmap);
                        baiduMap.addOverlay(option);
                        datas.add(parking);
                    }
                    MapStatus.Builder builder = new MapStatus.Builder();
                    builder.zoom(15.0f);
                    baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                    recyclerview.replaceData(datas);
                    Log.e("tag", parkings.toString());
                }

                @Override
                protected void defeated(BaseModel<MerchantProperty> merchantPropertyBaseModel) {
                    recyclerview.cleanData();
                    showToast("您附近暂无停车场");
                }

                @Override
                public void onError(ExceptionHandle.ResponeThrowable e) {

                }
            });
        }
    }

    private void init() {
        client.selectCustomerByUsername(username, new BaseObserver<BaseModel<Customer>>(this) {

            @Override
            protected void showDialog() {

            }

            @Override
            protected void hideDialog() {

            }

            @Override
            protected void successful(BaseModel<Customer> customerBaseModel) {
                myCustomer = customerBaseModel.getData();
                isHome = TextUtils.isEmpty(myCustomer.getHome());
                isCompany = TextUtils.isEmpty(myCustomer.getCompany());

                home_collection_state.setText(isHome ? "去设置" : "");
                company_collection_state.setText(isCompany ? "去设置" : "");

                View headerView = navigationview.getHeaderView(0);
                View head = headerView.findViewById(R.id.head);
                phone_text = headerView.findViewById(R.id.phone);
                name_text = headerView.findViewById(R.id.username);
                profile_image = headerView.findViewById(R.id.profile_image);
                StringBuilder sb = new StringBuilder(myCustomer.getPhone());
                sb.replace(3, 7, "****");
                phone_text.setText(sb.toString());
                name_text.setText(myCustomer.getUsername());
                Glide.with(CustomerMainActivity.this)
                        .load(RequestAPI.baseImageURL + myCustomer.getAvatar())
                        .override(64, 64)
                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(profile_image);

                SharedPreferences.Editor editor = getSharedPreferences("userdata", Context.MODE_PRIVATE).edit();
                editor.putString("avatar", myCustomer.getAvatar());
                editor.putString("phone", myCustomer.getPhone());
                editor.apply();

                profile_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ShowPhotoDialog dialog = new ShowPhotoDialog(CustomerMainActivity.this);
                        dialog.setCirclePhoto(RequestAPI.baseImageURL + myCustomer.getAvatar());
                        dialog.show();
                    }
                });

                headerView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivityForResult(CustomerPersonalDataActivity.class, 4);
                    }
                });

            }

            @Override
            protected void defeated(BaseModel<Customer> customerBaseModel) {
                showToast(customerBaseModel.getMessage());
            }

            @Override
            public void onError(ExceptionHandle.ResponeThrowable e) {
            }
        });

        recyclerview = new BaseRecyclerView<Location, BaseViewHolder>(CustomerMainActivity.this, R.id.search_result) {
            @Override
            public BaseQuickAdapter<Location, BaseViewHolder> initAdapter() {

                class ResultAdapter extends BaseQuickAdapter<Location, BaseViewHolder> {

                    public ResultAdapter(@org.jetbrains.annotations.Nullable List<Location> data) {
                        super(R.layout.text_item, data);
                    }

                    @Override
                    protected void convert(@NotNull BaseViewHolder baseViewHolder, Location location) {
                        baseViewHolder.setText(R.id.item_name, location.getMerchantname());
                        if (nowLatlng != null)
                            baseViewHolder.setText(R.id.item_distance, "约"+ToolUtil.getDistance(location, nowLatlng) + "公里");
                        else
                            baseViewHolder.setText(R.id.item_distance, "");
                    }
                }
                ;

                ResultAdapter resultAdapter = new ResultAdapter(datas);

                return resultAdapter;
            }
        };

        recyclerview.setOnItemClick(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                if (nowLatlng != null) {
                    Location destination = (Location) adapter.getItem(position);
                    Intent intent = new Intent(CustomerMainActivity.this, ParkingDetailedInfoActivity.class);
                    intent.putExtra("location", GsonUtil.GsonString(destination));
                    intent.putExtra("nowLatlng", GsonUtil.GsonString(nowLatlng));
                    startActivity(intent);
                } else {
                    Location destination = (Location) adapter.getItem(position);
                    Intent intent = new Intent(CustomerMainActivity.this, ParkingDetailedInfoActivity.class);
                    intent.putExtra("location", GsonUtil.GsonString(destination));
                    startActivity(intent);
                }
            }
        });

        serach_view_group.setOnScrollListener(new ScrollViewGroup.IScrollListener() {
            @Override
            public void onTargetToTopDistance(int distance) {
                if (distance > (DensityUtil.getSreenHeight(CustomerMainActivity.this) / 2)) {
                    myhome.setVisibility(View.VISIBLE);
                } else {
                    myhome.setVisibility(View.GONE);
                }
                Log.e("LOG_TAG", "target top :" + distance);
            }

            @Override
            public void onHeaderToTopDistance(int distance) {
                Log.e("LOG_TAG", "header top :" + distance);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                layout.openDrawer(GravityCompat.START);
                break;
        }
        return true;
    }

    @Override
    protected void setMapOverlay(LatLng point) {
        baiduMap.clear();
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.water_drop);
        OverlayOptions option = new MarkerOptions().position(point).icon(bitmap);
        baiduMap.addOverlay(option);
    }

    @Override
    protected void initMap() {
        //设置地图模式
        baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        //开启位置
        baiduMap.setMyLocationEnabled(true);
        //开启路况
        baiduMap.setTrafficEnabled(true);
        //初始化放大级别
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.zoom(15.0f);
        baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        //实例化UiSettings类对象
        UiSettings mUiSettings = baiduMap.getUiSettings();
        //通过设置enable为true或false 选择是否显示指南针
        mUiSettings.setCompassEnabled(true);

        mapView.showScaleControl(false);
    }

    //获取到GPS信息后自动回调该方法，在这里将设置地图
    @Override
    protected void navigateTo(BDLocation location) {
        nowLatlng = new LatLng(location.getLatitude(), location.getLongitude());//获取经纬度
        MapStatusUpdate update = null;
        if (isFirst) {
            update = MapStatusUpdateFactory.newLatLng(nowLatlng);//设置经纬度
            baiduMap.animateMapStatus(update);//地图移动到设定位置
            isFirst = false;
            actionbar.setTitle(location.getCity());
            Location mylocation = new Location();
            mylocation.setMerchantname("我的位置");
            mylocation.setLongitude(nowLatlng.longitude);
            mylocation.setLatitude(nowLatlng.latitude);
            client.searchParking(mylocation, new BaseObserver<BaseModel<MerchantProperty>>(CustomerMainActivity.this) {
                @Override
                protected void showDialog() {

                }

                @Override
                protected void hideDialog() {

                }

                @Override
                protected void successful(BaseModel<MerchantProperty> merchantPropertyBaseModel) {
                    List<MerchantProperty> properties = merchantPropertyBaseModel.getDatas();
                    List<Location> locations = new ArrayList<>();
                    for (MerchantProperty data : properties) {
                        Location parking = data.getLocation();
                        LatLng lng = new LatLng(parking.getLatitude(), parking.getLongitude());
                        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.p);
                        OverlayOptions option = new MarkerOptions().position(lng).icon(bitmap);
                        baiduMap.addOverlay(option);
                        locations.add(parking);
                    }
                    recyclerview.replaceData(locations);
                }

                @Override
                protected void defeated(BaseModel<MerchantProperty> merchantPropertyBaseModel) {
                    showToast("附近暂无停车场");
                }

                @Override
                public void onError(ExceptionHandle.ResponeThrowable e) {

                }
            });
        }

        if (isLocal) {
            update = MapStatusUpdateFactory.newLatLng(nowLatlng);
            baiduMap.animateMapStatus(update);
        }

        MyLocationData.Builder builder = new MyLocationData.Builder();
        builder.accuracy(-1.0f);
        builder.direction(getmCurrentX());
        builder.latitude(location.getLatitude());
        builder.longitude(location.getLongitude());
        MyLocationData data = builder.build();
        baiduMap.setMyLocationData(data);

        MyLocationConfiguration configuration = new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL,
                true,
                null
        );
        baiduMap.setMyLocationConfiguration(configuration);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    //获取应用详情页面intent（如果找不到要跳转的界面，也可以先把用户引导到系统设置页面）
    private Intent getAppDetailSettingIntent() {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
        }
        return localIntent;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    //去数据库找停车场，更新到列表
                    LatLng latLng = GsonUtil.GsonToBean(data.getStringExtra("result"), LatLng.class);
                    Location location = new Location();
                    location.setLatitude(latLng.latitude);
                    location.setLongitude(latLng.longitude);
                    client.searchParking(location, new BaseObserver<BaseModel<MerchantProperty>>(CustomerMainActivity.this) {
                        @Override
                        protected void showDialog() {

                        }

                        @Override
                        protected void hideDialog() {

                        }

                        @Override
                        protected void successful(BaseModel<MerchantProperty> merchantPropertyBaseModel) {
                            List<MerchantProperty> parkings = merchantPropertyBaseModel.getDatas();
                            baiduMap.clear();
                            datas = new ArrayList<>();
                            for (MerchantProperty data : parkings) {
                                Location parking = data.getLocation();
                                LatLng lng = new LatLng(parking.getLatitude(), parking.getLongitude());
                                MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(latLng);//设置经纬度
                                baiduMap.animateMapStatus(update);//地图移动到设定位置
                                BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.p);
                                OverlayOptions option = new MarkerOptions().position(lng).icon(bitmap);
                                baiduMap.addOverlay(option);
                                datas.add(parking);
                            }
                            MapStatus.Builder builder = new MapStatus.Builder();
                            builder.zoom(15.0f);
                            baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                            recyclerview.replaceData(datas);
                            Log.e("tag", parkings.toString());
                        }

                        @Override
                        protected void defeated(BaseModel<MerchantProperty> merchantPropertyBaseModel) {
                            showToast("您所选地点附近暂无停车场");
                            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(latLng);//设置经纬度
                            baiduMap.animateMapStatus(update);//地图移动到设定位置
                            recyclerview.cleanData();
                        }

                        @Override
                        public void onError(ExceptionHandle.ResponeThrowable e) {
                            Log.e("error", e.toString());
                        }
                    });

                }
                break;
            case 2:
                if (resultCode == RESULT_OK) {
                    String latlng_str = data.getStringExtra("latlng");
                    String result_str = data.getStringExtra("result");
                    LatLng latLng = GsonUtil.GsonToBean(latlng_str, LatLng.class);
                    ReverseGeoCodeResult result = GsonUtil.GsonToBean(result_str, ReverseGeoCodeResult.class);
                    Customer customer = new Customer();
                    customer.setUsername(username);
                    customer.setHome(result.getAddress());
                    customer.setHome_latitude(latLng.latitude);
                    customer.setHome_longitude(latLng.longitude);
                    Log.e("tag", customer.toString());
                    client.updatehomeaddressinfo(customer, new BaseObserver<BaseModel>(CustomerMainActivity.this) {
                        @Override
                        protected void showDialog() {

                        }

                        @Override
                        protected void hideDialog() {

                        }

                        @Override
                        protected void successful(BaseModel baseModel) {
                            Toast.makeText(CustomerMainActivity.this, baseModel.getMessage(), Toast.LENGTH_SHORT).show();
                            home_collection_state.setText("");
                            isHome = false;
                        }

                        @Override
                        protected void defeated(BaseModel baseModel) {
                            Toast.makeText(CustomerMainActivity.this, baseModel.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(ExceptionHandle.ResponeThrowable e) {

                        }
                    });
                }
                break;
            case 3:
                if (resultCode == RESULT_OK) {
                    String latlng_str = data.getStringExtra("latlng");
                    String result_str = data.getStringExtra("result");
                    LatLng latLng = GsonUtil.GsonToBean(latlng_str, LatLng.class);
                    ReverseGeoCodeResult result = GsonUtil.GsonToBean(result_str, ReverseGeoCodeResult.class);
                    Customer customer = new Customer();
                    customer.setUsername(username);
                    customer.setCompany(result.getAddress());
                    customer.setCompany_latitude(latLng.latitude);
                    customer.setCompany_longitude(latLng.longitude);
                    Log.e("tag", customer.toString());
                    client.updateCompanyAddressInfo(customer, new BaseObserver<BaseModel>(CustomerMainActivity.this) {
                        @Override
                        protected void showDialog() {

                        }

                        @Override
                        protected void hideDialog() {

                        }

                        @Override
                        protected void successful(BaseModel baseModel) {
                            Toast.makeText(CustomerMainActivity.this, baseModel.getMessage(), Toast.LENGTH_SHORT).show();
                            company_collection_state.setText("");
                            isCompany = false;
                        }

                        @Override
                        protected void defeated(BaseModel baseModel) {
                            Toast.makeText(CustomerMainActivity.this, baseModel.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(ExceptionHandle.ResponeThrowable e) {

                        }
                    });
                }
                break;
            case 4:
                if (resultCode == RESULT_OK) {
                    Glide.with(CustomerMainActivity.this)
                            .load(RequestAPI.baseImageURL + data.getStringExtra("avatar"))
                            .override(64, 64)
                            .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(profile_image);
                    StringBuilder sb = new StringBuilder(data.getStringExtra("phone"));
                    sb.replace(3, 7, "****");
                    phone_text.setText(sb.toString());
                    name_text.setText(data.getStringExtra("user"));
                }
                break;
            case REQUEST_QR_CODE:
                if (resultCode == RESULT_OK) {
                    String result = data.getStringExtra("result");
                    boolean flag = GsonUtil.isJson(result);
                    if (flag) {//是JSON字符串，出口扫码
                        try {
                            GsonUtil.GsonToBean(result, CheckInfo.class);
                            Intent intent = new Intent(CustomerMainActivity.this, ScannerPayActivity.class);
                            intent.putExtra("json", result);
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                            MaterialDialog dialog = new MaterialDialog(CustomerMainActivity.this, MaterialDialog.getDEFAULT_BEHAVIOR());
                            dialog.title(null, "错误");
                            dialog.icon(null, getResources().getDrawable(R.drawable.ic_error));
                            dialog.message(null, "二维码识别错误", null);
                            dialog.show();
                        }
                    } else {//不是JSON字符串，非出口扫码
                        Intent intent = new Intent(CustomerMainActivity.this, CarLicenseActivity.class);
                        intent.putExtra("merchant", result);
                        startActivity(intent);
                    }
                }
                break;
        }
    }

}

