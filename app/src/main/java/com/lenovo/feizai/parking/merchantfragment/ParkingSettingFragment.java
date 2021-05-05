package com.lenovo.feizai.parking.merchantfragment;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Switch;

import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.base.BaseFragment;
import com.lenovo.feizai.parking.base.BaseModel;
import com.lenovo.feizai.parking.base.BaseObserver;
import com.lenovo.feizai.parking.entity.MerchantChange;
import com.lenovo.feizai.parking.entity.MerchantState;
import com.lenovo.feizai.parking.entity.MessageEvent;
import com.lenovo.feizai.parking.net.ExceptionHandle;
import com.lenovo.feizai.parking.net.RetrofitClient;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author feizai
 * @date 2021/4/6 0006 下午 4:15:07
 * @annotation
 */
public class ParkingSettingFragment extends BaseFragment {

    @BindView(R.id.subscribe_switch)
    Switch mSwitch;

    private String merchant;
    private RetrofitClient client;
    private String auditstate;

    public ParkingSettingFragment() {
        super(R.layout.activity_parking_setting);
    }

    @Override
    protected void initView(View view) {
        Bundle bundle = getArguments();
        merchant = bundle.getString("name");
        client = RetrofitClient.getInstance(getContext());
    }

    @OnClick(R.id.subscribe_switch)
    public void subscribe_switch() {
        boolean checked = mSwitch.isChecked();
        if (auditstate.equals("已通过")) {
            if (checked) {
                updateParkingState("营业中");
            } else {
                updateParkingState("未营业");
            }
        } else {
            mSwitch.setChecked(false);
            showToast("由于您未审核通过，暂不可以开店!");
        }
    }

    @OnClick(R.id.change_parking_info)
    public void changeinfo(){
        if (auditstate.equals("已通过")) {
            client.isMerchantChange(merchant, new BaseObserver<BaseModel<MerchantChange>>(getContext()) {
                @Override
                protected void showDialog() {

                }

                @Override
                protected void hideDialog() {

                }

                @Override
                protected void successful(BaseModel<MerchantChange> merchantChangeBaseModel) {
                    MerchantChange change = merchantChangeBaseModel.getData();
                    switch (change.getAuditstate()) {
                        case "未通过":
                            EventBus.getDefault().postSticky(new MessageEvent("changeinfo"));
                            break;
                        case "未审核":
                            EventBus.getDefault().postSticky(new MessageEvent("seeinfo"));
                            break;
                    }
                }

                @Override
                protected void defeated(BaseModel<MerchantChange> merchantChangeBaseModel) {
                    EventBus.getDefault().postSticky(new MessageEvent("addinfo"));
                }

                @Override
                public void onError(ExceptionHandle.ResponeThrowable e) {
                    Log.e("tag", e.getMessage());
                }
            });
        }
        else
            showToast("由于您未审核通过，暂不可以管理停车场!");
    }

    @OnClick(R.id.change_number)
    public void changenumber() {
        if (auditstate.equals("已通过"))
            EventBus.getDefault().postSticky(new MessageEvent("space"));
        else
            showToast("由于您未审核通过，暂不可以管理停车场!");
    }

    @OnClick(R.id.change_link_info)
    public void change_link_info() {
        EventBus.getDefault().postSticky(new MessageEvent("link"));
    }

    private void updateParkingState(String state) {
        client.updateParkingState(merchant, state, new BaseObserver<BaseModel>(getContext()) {
            @Override
            protected void showDialog() {

            }

            @Override
            protected void hideDialog() {

            }

            @Override
            protected void successful(BaseModel baseModel) {
                switch (state) {
                    case "营业中":
                        showToast("已开店，快去迎接您的第一单生意！");
                        break;
                    case "未营业":
                        showToast("已关店，辛苦一天了快去休息吧！");
                        break;
                }
            }

            @Override
            protected void defeated(BaseModel baseModel) {
                showToast("开店失败");
                mSwitch.setChecked(false);
            }

            @Override
            public void onError(ExceptionHandle.ResponeThrowable e) {
                mSwitch.setChecked(false);
            }
        });
    }

    @OnClick(R.id.back)
    public void back() {
        getActivity().finish();
    }

    @OnClick(R.id.findorder)
    public void findorder() {
        EventBus.getDefault().postSticky(new MessageEvent("order"));
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

    @Override
    public void onStart() {
        super.onStart();
        client.findMerchantState(merchant, new BaseObserver<BaseModel<MerchantState>>(getContext()) {
            @Override
            protected void showDialog() {

            }

            @Override
            protected void hideDialog() {

            }

            @Override
            protected void successful(BaseModel<MerchantState> merchantStateBaseModel) {
                MerchantState state = merchantStateBaseModel.getData();
                auditstate = state.getAuditstate();
                if (state.getOperatingstate().equals("营业中")) {
                    mSwitch.setChecked(true);
                } else {
                    mSwitch.setChecked(false);
                }
            }

            @Override
            protected void defeated(BaseModel<MerchantState> merchantStateBaseModel) {

            }

            @Override
            public void onError(ExceptionHandle.ResponeThrowable e) {

            }
        });
    }
}
