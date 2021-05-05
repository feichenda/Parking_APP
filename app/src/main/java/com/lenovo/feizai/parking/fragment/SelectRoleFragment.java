package com.lenovo.feizai.parking.fragment;

import android.view.KeyEvent;
import android.view.View;

import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.base.BaseFragment;
import com.lenovo.feizai.parking.entity.MessageEvent;

import org.greenrobot.eventbus.EventBus;

import butterknife.OnClick;

/**
 * @author feizai
 * @date 2021/3/17 0017 下午 1:28:22
 * @annotation
 */
public class SelectRoleFragment extends BaseFragment {

    public SelectRoleFragment() {
        super(R.layout.fragment_select_role);
    }

    @Override
    protected void initView(View view) {

    }

    @OnClick(R.id.user_btn)
    public void user(){
        EventBus.getDefault().postSticky(new MessageEvent("user"));
    }

    @OnClick(R.id.merchant_btn)
    public void merchant(){
        EventBus.getDefault().postSticky(new MessageEvent("merchant"));
    }

    @Override
    public void onResume() {
        super.onResume();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_BACK) {
                    getActivity().finish();
                    return true;
                }
                return false;
            }
        });
    }
}
