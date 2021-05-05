package com.lenovo.feizai.parking.activity;

import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.adapter.AddressAdapter;
import com.lenovo.feizai.parking.base.BaseLocationActivity;
import com.lenovo.feizai.parking.base.BaseRecyclerView;
import com.lenovo.feizai.parking.util.GsonUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author feizai
 * @date 01/01/2021 001 9:03:35 PM
 */
public class MapActivity extends BaseLocationActivity implements BaiduMap.OnMapStatusChangeListener, OnGetGeoCoderResultListener, OnGetSuggestionResultListener {
    @BindView(R.id.mapview)
    MapView mapView;
    @BindView(R.id.marker)
    ImageView marker;
    @BindView(R.id.search_edit)
    EditText search_edit;

    List<SuggestionResult.SuggestionInfo> datas;
    BaiduMap baiduMap;
    LatLng nowlatlng, selectlatlng;
    BDLocation location;
    boolean isFirst, isLocal;
    GeoCoder mCoder;
    ReverseGeoCodeResult selectReverseGeoCodeResult;
    private String city;
    private BaseRecyclerView<SuggestionResult.SuggestionInfo, BaseViewHolder> recyclerView;
    private SuggestionSearch suggestionSearch;
    private LatLng myLat;
    private String mylat;

    public MapActivity() {
        super(R.layout.map_layout);
    }

    @Override
    protected void initView() {
        Intent intent = getIntent();
        mylat = intent.getStringExtra("mylat");
        if (mylat == null) {
            myLat = null;
        } else {
            myLat = GsonUtil.GsonToBean(mylat, LatLng.class);
        }
        isFirst = true;
        isLocal = true;
        baiduMap = mapView.getMap();
        mapView.showZoomControls(false);
        baiduMap.setOnMapStatusChangeListener(this);
        requestLocation();
        mCoder = GeoCoder.newInstance();
        mCoder.setOnGetGeoCodeResultListener(this);
        suggestionSearch = SuggestionSearch.newInstance();
        suggestionSearch.setOnGetSuggestionResultListener(this);
        recyclerView
                = new BaseRecyclerView<SuggestionResult.SuggestionInfo, BaseViewHolder>(MapActivity.this, R.id.result_list) {
            @Override
            public BaseQuickAdapter<SuggestionResult.SuggestionInfo, BaseViewHolder> initAdapter() {
                AddressAdapter adapter = new AddressAdapter(MapActivity.this, null);
                return adapter;
            }
        };

        search_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                recyclerView.setVisibility(View.VISIBLE);
                recyclerView.cleanData();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 0 && (!TextUtils.isEmpty(city))) {
                    suggestionSearch.requestSuggestion(new SuggestionSearchOption().city(city).keyword(s.toString()));
                } else {
                    recyclerView.cleanData();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        recyclerView.setOnItemClick(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                SuggestionResult.SuggestionInfo info = (SuggestionResult.SuggestionInfo) adapter.getItem(position);
                selectlatlng = info.getPt();
                search_edit.setText(info.getKey());
                MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(selectlatlng);//设置经纬度
                baiduMap.animateMapStatus(update);//地图移动到设定位置
                mCoder.reverseGeoCode(new ReverseGeoCodeOption().location(selectlatlng).newVersion(1));
                recyclerView.cleanData();
                recyclerView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void setMapOverlay(LatLng point) {

    }

    @Override
    protected void initMap() {
        //设置地图模式
        baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        //开启位置
        baiduMap.setMyLocationEnabled(true);
        //初始化放大级别
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.zoom(17.0f);
        baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        if (myLat != null) {
            baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(myLat));
            isFirst = false;
        }
    }

    @Override
    protected void navigateTo(BDLocation location) {
        MapStatusUpdate update;
        this.location = location;
        nowlatlng = new LatLng(location.getLatitude(), location.getLongitude());//获取经纬度
        if (isFirst) {
            update = MapStatusUpdateFactory.newLatLng(nowlatlng);//设置经纬度
            baiduMap.animateMapStatus(update);//地图移动到设定位置
            isFirst = false;
        }
        city = location.getCity();
        Log.e("tag", city);
    }

    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus) {

    }

    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

    }

    @Override
    public void onMapStatusChange(MapStatus mapStatus) {

    }

    @Override
    public void onMapStatusChangeFinish(MapStatus mapStatus) {
        TranslateAnimation anim = new TranslateAnimation(0, 0, 0, -50);
        anim.setDuration(500);
        anim.setRepeatMode(Animation.RESTART);
        anim.setRepeatCount(1);
        marker.setAnimation(anim);
        if (isLocal) {
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(nowlatlng);
            baiduMap.animateMapStatus(update);
            isLocal = false;
        }
        if (mapStatus == null)
            return;
        selectlatlng = mapStatus.target;
        mCoder.reverseGeoCode(new ReverseGeoCodeOption().location(selectlatlng).newVersion(1));
    }

    @OnClick(R.id.clean)
    public void clean() {
        search_edit.setText("");
    }

    @OnClick(R.id.myhome)
    public void home() {
        if (nowlatlng != null) {
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(nowlatlng);//设置经纬度
            baiduMap.animateMapStatus(update);//地图移动到设定位置
            update = MapStatusUpdateFactory.zoomTo(17.0f);
            baiduMap.animateMapStatus(update);
            selectlatlng = nowlatlng;
            mCoder.reverseGeoCode(new ReverseGeoCodeOption().location(selectlatlng).newVersion(1));
            TranslateAnimation anim = new TranslateAnimation(0, 0, 0, -50);
            anim.setDuration(500);
            anim.setRepeatMode(android.view.animation.Animation.RESTART);
            anim.setRepeatCount(1);
            marker.setAnimation(anim);
        }else {
            showToast("未获取到当前位置");
        }
    }

    @OnClick(R.id.sure)
    public void sure() {
        if (selectlatlng == null)
            finish();
        if (selectReverseGeoCodeResult == null)
            finish();
        Intent intent = new Intent();
        String select = GsonUtil.GsonString(selectlatlng);
        String result = GsonUtil.GsonString(selectReverseGeoCodeResult);
        intent.putExtra("latlng", select);
        intent.putExtra("result", result);
        setResult(RESULT_OK, intent);
        finish();
    }

    @OnClick(R.id.back)
    public void back() {
        finish();
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

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
        if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
            //没有找到检索结果
            return;
        } else {
            selectReverseGeoCodeResult = reverseGeoCodeResult;
        }
    }

    @Override
    public void onGetSuggestionResult(SuggestionResult suggestionResult) {
        if (suggestionResult.error == SearchResult.ERRORNO.NO_ERROR) {
            List<SuggestionResult.SuggestionInfo> result = suggestionResult.getAllSuggestions();
            datas = new ArrayList<>();
            for (SuggestionResult.SuggestionInfo suggestionInfo : result) {
                datas.add(suggestionInfo);
            }
            recyclerView.replaceData(datas);
        } else {
            datas = new ArrayList<>();
            SuggestionResult.SuggestionInfo info = new SuggestionResult.SuggestionInfo();
            info.setKey("未搜索到结果");
            datas.add(info);
            recyclerView.replaceData(datas);
        }
    }

}
