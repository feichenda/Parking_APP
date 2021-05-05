package com.lenovo.feizai.parking.base;

import android.app.Activity;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


/**
 * @author feizai
 * @date 12/22/2020 022 3:11:11 PM
 */
public abstract class BaseRefreshRecyclerView extends BaseRecyclerView {

    SwipeRefreshLayout swipeRefreshLayout;

    public BaseRefreshRecyclerView(Activity activity, int recyclerViewId, int refreshId) {
        super(activity, recyclerViewId);
        swipeRefreshLayout = activity.findViewById(refreshId);
    }

    public BaseRefreshRecyclerView(Fragment fragment, int recyclerViewId, int refreshId) {
        super(fragment, recyclerViewId);
        swipeRefreshLayout = fragment.getView().findViewById(refreshId);
    }

    public BaseRefreshRecyclerView(View view, int recyclerViewId, int refreshId) {
        super(view, recyclerViewId);
        swipeRefreshLayout = view.findViewById(refreshId);
    }

    public void addRefreshLisenter(SwipeRefreshLayout.OnRefreshListener onRefreshListener) {
        swipeRefreshLayout.setOnRefreshListener(onRefreshListener);
    }

    public void setRefreshEnable(boolean enable) {
        swipeRefreshLayout.setEnabled(enable);
    }

    public void refreshEnd() {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public void refresh() {
        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }
    }
}
