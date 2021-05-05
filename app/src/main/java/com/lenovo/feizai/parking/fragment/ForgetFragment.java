package com.lenovo.feizai.parking.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.base.BaseFragment;
import com.lenovo.feizai.parking.base.BaseModel;
import com.lenovo.feizai.parking.base.BaseObserver;
import com.lenovo.feizai.parking.entity.MessageEvent;
import com.lenovo.feizai.parking.net.ExceptionHandle;
import com.lenovo.feizai.parking.net.RetrofitClient;
import com.lenovo.feizai.parking.util.GetYanUtil;
import com.lenovo.feizai.parking.util.ToolUtil;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author feizai
 * @date 01/12/2021 012 8:05:31 PM
 */
public class ForgetFragment extends BaseFragment {

    @BindView(R.id.user)
    EditText user;
    @BindView(R.id.yanedit)
    EditText yanedit;
    @BindView(R.id.phone)
    EditText phone;
    @BindView(R.id.yan)
    TextView yan;

    MaterialDialog registerDialog;
    private String role;

    public ForgetFragment() {
        super(R.layout.fragment_forget);
    }

    @Override
    protected void initView(View view) {
        Bundle bundle = getArguments();
        role = bundle.getString("role");
    }

    @OnClick(R.id.find)
    public void register() {
        String userStr=user.getText().toString().trim();
        String phoneStr=phone.getText().toString().trim();
        String yanStr=yanedit.getText().toString().trim();
        String yanzheng=yan.getText().toString().trim();
        if (TextUtils.isEmpty(userStr)){
            Toast.makeText(getContext(),"用户名不能为空",Toast.LENGTH_SHORT).show();
            return;
        }else {
            if (TextUtils.isEmpty(phoneStr)){
                Toast.makeText(getContext(),"手机号不能为空",Toast.LENGTH_SHORT).show();
                return;
            }else {
                if (TextUtils.isEmpty(yanStr)){
                    Toast.makeText(getContext(),"请输入验证码",Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    if (TextUtils.equals(yanStr,yanzheng)){
                        if (!ToolUtil.checkMobileNumber(phoneStr)) {
                            showToast("您输入的手机号有误");
                            return;
                        }
                        RetrofitClient.getInstance(getContext())
                                .forget(userStr, phoneStr, role, new BaseObserver<BaseModel>(getContext()) {
                            @Override
                            protected void showDialog() {

                            }

                            @Override
                            protected void hideDialog() {

                            }

                            @Override
                            protected void successful(BaseModel baseModel) {
                                EventBus.getDefault().postSticky(new MessageEvent(userStr));
                            }

                            @Override
                            protected void defeated(BaseModel baseModel) {
                                showToast(baseModel.getMessage());
                            }

                            @Override
                            public void onError(ExceptionHandle.ResponeThrowable e) {
                                Logger.e(e,e.getMessage());
                            }
                        });
                    }else {
                        Toast.makeText(getContext(),"您输入的验证码有误",Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        }
    }

    @OnClick(R.id.yan)
    public void getYan(){
        boolean flag=yanedit.isSelected();
        if(flag){
            yan.setSelected(!flag);
            yan.setText(GetYanUtil.getyan());

        }else{
            yan.setSelected(!flag);
            yan.setText(GetYanUtil.getyan());
        }
    }
}
