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

import com.afollestad.materialdialogs.MaterialDialog;
import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.base.BaseFragment;
import com.lenovo.feizai.parking.base.BaseModel;
import com.lenovo.feizai.parking.base.BaseObserver;
import com.lenovo.feizai.parking.entity.Customer;
import com.lenovo.feizai.parking.entity.MessageEvent;
import com.lenovo.feizai.parking.entity.User;
import com.lenovo.feizai.parking.net.ExceptionHandle;
import com.lenovo.feizai.parking.net.RetrofitClient;
import com.lenovo.feizai.parking.util.MD5Util;
import com.lenovo.feizai.parking.util.ToolUtil;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTouch;
import me.leefeng.promptlibrary.PromptDialog;

/**
 * @author feizai
 * @date 01/12/2021 012 8:05:31 PM
 */
public class RegisterFragment extends BaseFragment {

    @BindView(R.id.user)
    EditText user;
    @BindView(R.id.pass)
    EditText pass;
    @BindView(R.id.repass)
    EditText repass;
    @BindView(R.id.phone)
    EditText phone;

    MaterialDialog registerDialog;

    String role;

    public RegisterFragment() {
        super(R.layout.fragment_register);
    }

    @Override
    protected void initView(View view) {
        Bundle bundle = getArguments();
        role = bundle.getString("role");
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

    @OnClick(R.id.register)
    public void register() {
        PromptDialog promptDialog = new PromptDialog(getActivity());
        String username = user.getText().toString().trim();
        String password = pass.getText().toString().trim();
        String repassword = repass.getText().toString().trim();
        String phonenumber = phone.getText().toString().trim();
        if (password.length() < 6 || repassword.length() < 6) {
            showToast("密码长度不能小于6位");
            return;
        }
        if (TextUtils.isEmpty(phonenumber)){
            showToast("手机号不能为空");
        }else {
            if (ToolUtil.checkMobileNumber(phonenumber)){
                if (TextUtils.isEmpty(username)) {
                    showToast("请输入用户名");
                } else {
                    if (TextUtils.isEmpty(password)) {
                        showToast("请输入密码");
                    } else {
                        if (TextUtils.isEmpty(repassword)) {
                            showToast("请再次输入密码");
                        } else {
                            if (TextUtils.equals(password, repassword)) {
                                User user = new User();
                                user.setUsername(username);
                                user.setPassword(MD5Util.string2MD5(password));
                                user.setRole(role);
                                Customer customer = new Customer();
                                customer.setUsername(username);
                                customer.setPhone(phonenumber);
                                RetrofitClient.getInstance(getContext())
                                        .register(user,phonenumber, new BaseObserver<BaseModel>(getContext()) {
                                            @Override
                                            protected void showDialog() {
                                                promptDialog.showLoading("正在注册");
                                            }

                                            @Override
                                            protected void hideDialog() {

                                            }

                                            @Override
                                            protected void successful(BaseModel baseModel) {
                                                showToast("注册成功!");
                                                promptDialog.showSuccess("注册成功");
                                                EventBus.getDefault().postSticky(new MessageEvent("login"));
                                            }

                                            @Override
                                            protected void defeated(BaseModel baseModel) {
                                                showToast(baseModel.getMessage());
                                                promptDialog.showError("注册失败");
                                            }

                                            @Override
                                            public void onError(ExceptionHandle.ResponeThrowable e) {
                                                promptDialog.dismiss();
                                                showToast(e.getMessage());
                                            }
                                        });

                            } else {
                                showToast("两次输入的密码不相同");
                            }
                        }
                    }
                }
            }else {
                showToast("你输入的手机号码有误");
            }
        }

    }
}
