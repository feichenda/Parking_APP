package com.lenovo.feizai.parking.base;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemChildLongClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnItemLongClickListener;
import com.chad.library.adapter.base.listener.OnLoadMoreListener;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.List;

/**
 * 如需使用加载更多，请勿直接new BaseQuickAdapter<T,BaseViewHolder>，请继承并实现LoadMoreModule接口
 *
 * @author feizai
 * @date 12/22/2020 022 11:58:42 AM
 */
public abstract class BaseRecyclerView<T, K extends BaseViewHolder> extends RecyclerView.OnScrollListener {
    BaseQuickAdapter<T, K> adapter;
    RecyclerView recyclerView;
    Context mContext;
    RecyclerView.ItemDecoration itemDecoration;
    RecyclerView.LayoutManager layoutManager;

    public BaseRecyclerView(Activity activity, int recyclerViewId) {
        mContext = activity;
        recyclerView = activity.findViewById(recyclerViewId);
        init();
        adapter = initAdapter();
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    public BaseRecyclerView(Fragment fragment, int recyclerViewId) {
        mContext = fragment.getContext();
        recyclerView = fragment.getView().findViewById(recyclerViewId);
        init();
        adapter = initAdapter();
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    public BaseRecyclerView(View view, int recyclerViewId) {
        mContext = view.getContext();
        recyclerView = view.findViewById(recyclerViewId);
        init();
        adapter = initAdapter();
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void init() {
        itemDecoration = new HorizontalDividerItemDecoration.Builder(mContext)
                .color(Color.BLACK)
                .size(2)
                .build();
        layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
    }

    public abstract BaseQuickAdapter<T, K> initAdapter();

    public void setOnItemClick(OnItemClickListener onItemClickListener) {
        adapter.setOnItemClickListener(onItemClickListener);
    }

    public void setOnItemChildClick(OnItemChildClickListener onItemChildClickListener) {
        adapter.setOnItemChildClickListener(onItemChildClickListener);
    }

    public void setOnItemLongClick(OnItemLongClickListener onItemLongClickListener) {
        adapter.setOnItemLongClickListener(onItemLongClickListener);
    }

    public void setOnItemChildLongClick(OnItemChildLongClickListener onItemChildLongClickListener) {
        adapter.setOnItemChildLongClickListener(onItemChildLongClickListener);
    }

    public void setItemDecoration(RecyclerView.ItemDecoration itemDecoration) {
        recyclerView.removeItemDecoration(this.itemDecoration);
        this.itemDecoration = itemDecoration;
        recyclerView.addItemDecoration(itemDecoration);
    }

    public void removeItemDecoration() {
        recyclerView.removeItemDecoration(this.itemDecoration);
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        this.layoutManager = layoutManager;
        recyclerView.setLayoutManager(layoutManager);
    }

    public void replaceData(List<T> data) {
        cleanData();
        adapter.replaceData(data);
        adapter.notifyDataSetChanged();
    }

    public void addHeadView(View view) {
        adapter.addHeaderView(view);
    }

    public void removeData(T data) {
        adapter.remove(data);
    }

    public void removeData(int position) {
        adapter.remove(position);
    }

    public T getItem(int position) {
        return adapter.getItem(position);
    }

    public int getItemCount() {
        return adapter.getItemCount();
    }

    public void addDatas(List<T> datas) {
        adapter.addData(datas);
    }

    public void addData(T data) {
        adapter.addData(data);
    }

    public void addData(int position, T data) {
        adapter.addData(position, data);
    }

    public void notifyDataSetChanged() {
        adapter.notifyDataSetChanged();
    }

    public void cleanData() {
        List<T> datas = getData();
        if (datas != null) {
            datas.clear();
            adapter.notifyDataSetChanged();
        }
    }

    public void removeAllHeadView() {
        adapter.removeAllHeaderView();
    }

    public void setNestedScrollingEnabled(Boolean boolen) {
        recyclerView.setNestedScrollingEnabled(boolen);
    }

    public List<T> getData() {
        return adapter.getData();
    }

    public void loadEnd() {
        adapter.getLoadMoreModule().loadMoreEnd();
    }

    public void loadComplete() {
        adapter.getLoadMoreModule().loadMoreComplete();
    }

    public void loadFail() {
        adapter.getLoadMoreModule().loadMoreFail();
    }

    public void enableLoadMore(boolean value) {
        adapter.getLoadMoreModule().setEnableLoadMore(value);
    }

    public void setVisibility(int visibility) {
        recyclerView.setVisibility(visibility);
    }

    public void loadMore(OnLoadMoreListener onLoadMoreListener) {
        adapter.getLoadMoreModule().setOnLoadMoreListener(onLoadMoreListener);
    }

    public void loadAutoMore(boolean value) {
        adapter.getLoadMoreModule().setAutoLoadMore(value);
    }

    /**
     * Callback method to be invoked when RecyclerView's scroll state changes.
     *
     * @param recyclerView The RecyclerView whose scroll state has changed.
     * @param newState
     */
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
    }

    /**
     * Callback method to be invoked when the RecyclerView has been scrolled. This will be
     * called after the scroll has completed.
     * <p>
     * This callback will also be called if visible text_item range changes after a layout
     * calculation. In that case, dx and dy will be 0.
     *
     * @param recyclerView The RecyclerView which scrolled.
     * @param dx           The amount of horizontal scroll.
     * @param dy           The amount of vertical scroll.
     */
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
    }
}
