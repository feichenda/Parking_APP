package com.lenovo.feizai.parking.customeractivity;

import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
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
import com.lenovo.feizai.parking.net.RetrofitClient;
import com.lenovo.feizai.parking.util.GsonUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author feizai
 * @date 01/07/2021 007 10:36:05 PM
 */
public class SearchActivity extends BaseLocationActivity implements OnGetSuggestionResultListener {

    @BindView(R.id.search_edit)
    EditText search_edit;

    List<SuggestionResult.SuggestionInfo> datas;

    String city;

    RetrofitClient retrofitClient;

    SuggestionSearch suggestionSearch;

    BaseRecyclerView<SuggestionResult.SuggestionInfo, BaseViewHolder> recyclerView;

    public SearchActivity() {
        super(R.layout.activity_search);
    }

    @Override
    protected void initView() {
        requestLocation();

        retrofitClient = RetrofitClient.getInstance(SearchActivity.this);

        suggestionSearch = SuggestionSearch.newInstance();
        suggestionSearch.setOnGetSuggestionResultListener(this);



        recyclerView
                = new BaseRecyclerView<SuggestionResult.SuggestionInfo, BaseViewHolder>(SearchActivity.this,R.id.search_result) {
            @Override
            public BaseQuickAdapter<SuggestionResult.SuggestionInfo, BaseViewHolder> initAdapter() {
                AddressAdapter adapter = new AddressAdapter(SearchActivity.this, null);
                return adapter;
            }
        };

        search_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.e("test3", s.toString());
                if (s.toString().length() > 0 && (!TextUtils.isEmpty(city))) {
                    suggestionSearch.requestSuggestion(new SuggestionSearchOption().city(city).keyword(s.toString()));
                } else {
                    recyclerView.replaceData(new ArrayList<>());
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
                if (TextUtils.equals(info.getKey(),"未搜索到结果")){

                }else {
                    search_edit.setText(info.getKey());
                    LatLng latlng = info.getPt();
                    searchParking(latlng);
                }
            }
        });
    }

    @OnClick(R.id.back)
    public void back() {
        finish();
    }

    @OnClick(R.id.search)
    public void search() {
        List<SuggestionResult.SuggestionInfo> result = recyclerView.getData();
        if (result.size()>0){
            SuggestionResult.SuggestionInfo info = result.get(0);
            if (TextUtils.equals(info.getKey(),"未搜索到结果")){

            }else {
                search_edit.setText(info.getKey());
                LatLng latlng = info.getPt();
                searchParking(latlng);
            }
        }
    }

    @OnClick(R.id.clean)
    public void clean(){
        search_edit.setText("");
    }

    @Override
    protected void setMapOverlay(LatLng point) {

    }

    @Override
    protected void initMap() {

    }

    @Override
    protected void navigateTo(BDLocation location) {
        city = location.getCity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        }else {
            datas = new ArrayList<>();
            SuggestionResult.SuggestionInfo info = new SuggestionResult.SuggestionInfo();
            info.setKey("未搜索到结果");
            datas.add(info);
            recyclerView.replaceData(datas);
        }
    }

    private void searchParking(LatLng latLng){
        //去数据库找停车场
        Intent intent = new Intent();
        String result = GsonUtil.GsonString(latLng);
        intent.putExtra("result",result);
        setResult(RESULT_OK,intent);
        finish();
    }

}
