package com.lenovo.feizai.parking.merchantfragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.base.BaseFragment;
import com.lenovo.feizai.parking.base.BaseModel;
import com.lenovo.feizai.parking.base.BaseObserver;
import com.lenovo.feizai.parking.entity.ParkingInfo;
import com.lenovo.feizai.parking.net.ExceptionHandle;
import com.lenovo.feizai.parking.net.RetrofitClient;
import com.lenovo.feizai.parking.util.ToolUtil;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author feizai
 * @date 2021/4/16 0016 下午 7:39:54
 * @annotation
 */
public class ChangeLinkFragment extends BaseFragment {

    @BindView(R.id.phone_edit)
    EditText phone_edit;
    @BindView(R.id.name_edit)
    EditText name_edit;
    @BindView(R.id.qq_edit)
    EditText qq_edit;

    private RetrofitClient client;
    private String merchant;

    public ChangeLinkFragment() {
        super(R.layout.fragment_change_link);
    }

    @Override
    protected void initView(View view) {
        Bundle bundle = getArguments();
        merchant = bundle.getString("name");
        client = RetrofitClient.getInstance(getContext());
        client.selectParkingInfo(merchant, new BaseObserver<BaseModel<ParkingInfo>>(getContext()) {
            @Override
            protected void showDialog() {

            }

            @Override
            protected void hideDialog() {

            }

            @Override
            protected void successful(BaseModel<ParkingInfo> parkingInfoBaseModel) {
                ParkingInfo data = parkingInfoBaseModel.getData();
                phone_edit.setText(data.getPhone());
                name_edit.setText(data.getLinkman());
                qq_edit.setText(data.getQQ());
            }

            @Override
            protected void defeated(BaseModel<ParkingInfo> parkingInfoBaseModel) {
                showToast(parkingInfoBaseModel.getMessage());
            }

            @Override
            public void onError(ExceptionHandle.ResponeThrowable e) {
                Log.e("tag", e.getMessage());
            }
        });
    }

    @OnClick(R.id.back)
    public void back() {
        getActivity().getSupportFragmentManager().popBackStack();
    }

    @OnClick(R.id.submit)
    public void submit() {
        String phone = phone_edit.getText().toString().trim();
        String name = name_edit.getText().toString().trim();
        String qq = qq_edit.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            showToast("手机号不能为空");
            return;
        }
        if (TextUtils.isEmpty(name)) {
            showToast("联系人不能为空");
            return;
        }
        if (TextUtils.isEmpty(qq)) {
            showToast("QQ号不能为空");
            return;
        }
        if (!ToolUtil.checkMobileNumber(phone)) {
            showToast("请输入正确的电话号码");
            return;
        }

        client.updateParkingInfoLink(merchant, phone, name, qq, new BaseObserver<BaseModel>(getContext()) {
            @Override
            protected void showDialog() {

            }

            @Override
            protected void hideDialog() {

            }

            @Override
            protected void successful(BaseModel baseModel) {
                showToast(baseModel.getMessage());
                getActivity().getSupportFragmentManager().popBackStack();
            }

            @Override
            protected void defeated(BaseModel baseModel) {
                showToast(baseModel.getMessage());
            }

            @Override
            public void onError(ExceptionHandle.ResponeThrowable e) {
                Log.e("tag", e.getMessage());
            }
        });
    }
}
