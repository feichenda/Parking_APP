package com.lenovo.feizai.parking.fragment;

import android.os.Bundle;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.base.BaseFragment;
import com.lenovo.feizai.parking.base.BaseModel;
import com.lenovo.feizai.parking.base.BaseObserver;
import com.lenovo.feizai.parking.entity.MessageEvent;
import com.lenovo.feizai.parking.net.ExceptionHandle;
import com.lenovo.feizai.parking.net.RetrofitClient;
import com.lenovo.feizai.parking.util.MD5Util;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTouch;

/**
 * @author feizai
 * @date 02/15/2021 015 10:18:05 AM
 */
public class FindPasswordFragment extends BaseFragment {

    @BindView(R.id.pass)
    EditText pass;
    @BindView(R.id.repass)
    EditText repass;
    String user;

    public FindPasswordFragment() {
        super(R.layout.fragment_find_password);
    }

    @Override
    protected void initView(View view) {
        Bundle bundle = getArguments();
        user=bundle.getString("user");
    }

    @OnTouch(R.id.see)
    public void see(View arg0, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP://松开事件发生后执行代码的区域
                pass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                break;
            case MotionEvent.ACTION_DOWN://按住事件发生后执行代码的区域
                pass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                break;
            default:
                break;
        }
        //切换后将EditText光标置于末尾
        CharSequence charSequence = pass.getText();
        if (charSequence instanceof Spannable) {
            Spannable spanText = (Spannable) charSequence;
            Selection.setSelection(spanText, charSequence.length());
        }
    }

    @OnTouch(R.id.resee)
    public void resee(View arg0, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP://松开事件发生后执行代码的区域
                repass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                break;
            case MotionEvent.ACTION_DOWN://按住事件发生后执行代码的区域
                repass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                break;
            default:
                break;
        }
        //切换后将EditText光标置于末尾
        CharSequence charSequence = pass.getText();
        if (charSequence instanceof Spannable) {
            Spannable spanText = (Spannable) charSequence;
            Selection.setSelection(spanText, charSequence.length());
        }
    }

    @OnClick(R.id.sure)
    public void sure(){
        String passStr=pass.getText().toString().trim();
        String repassStr=repass.getText().toString().trim();
        if (passStr.length() < 6 || repassStr.length() < 6) {
            showToast("密码长度不能小于6位");
            return;
        }
        if (TextUtils.isEmpty(passStr)){
            Toast.makeText(getContext(),"新密码不能为空",Toast.LENGTH_SHORT).show();
            return;
        }else {
            if (TextUtils.equals(passStr,repassStr)){
                RetrofitClient.getInstance(getContext())
                        .find(user, MD5Util.string2MD5(passStr), new BaseObserver<BaseModel>(getContext()) {
                            @Override
                            protected void showDialog() {

                            }

                            @Override
                            protected void hideDialog() {

                            }

                            @Override
                            protected void successful(BaseModel baseModel) {
                                showToast("修改成功");
                                EventBus.getDefault().postSticky(new MessageEvent("login"));
                            }

                            @Override
                            protected void defeated(BaseModel baseModel) {

                            }

                            @Override
                            public void onError(ExceptionHandle.ResponeThrowable e) {

                            }
                        });
            }else {
                Toast.makeText(getContext(),"您输入的两次密码不一致",Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }
}
