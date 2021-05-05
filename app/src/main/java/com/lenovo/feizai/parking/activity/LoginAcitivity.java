package com.lenovo.feizai.parking.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.base.BaseActivity;
import com.lenovo.feizai.parking.base.BaseModel;
import com.lenovo.feizai.parking.base.BaseObserver;
import com.lenovo.feizai.parking.customeractivity.CustomerMainActivity;
import com.lenovo.feizai.parking.entity.MessageEvent;
import com.lenovo.feizai.parking.entity.User;
import com.lenovo.feizai.parking.fragment.LoginFragment;
import com.lenovo.feizai.parking.fragment.NetworkErrorFragment;
import com.lenovo.feizai.parking.fragment.RegisterFragment;
import com.lenovo.feizai.parking.fragment.SelectRoleFragment;
import com.lenovo.feizai.parking.fragment.FindPasswordFragment;
import com.lenovo.feizai.parking.fragment.ForgetFragment;
import com.lenovo.feizai.parking.merchantactivity.MerchantMainActivity;
import com.lenovo.feizai.parking.net.ExceptionHandle;
import com.lenovo.feizai.parking.net.RetrofitClient;
import com.lenovo.feizai.parking.util.MD5Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import me.leefeng.promptlibrary.PromptDialog;

/**
 * @author feizai
 * @date 01/11/2021 011 8:35:00 PM
 */
public class LoginAcitivity extends BaseActivity {

    String role = null;

    public LoginAcitivity() {
        super(R.layout.activity_container);
    }

    @Override
    protected void initView() {
        RetrofitClient client = RetrofitClient.getInstance(this);
        EventBus.getDefault().register(this);
        SharedPreferences preferences = getSharedPreferences("userdata", Context.MODE_PRIVATE);
        String username = preferences.getString("username", "");
        String userpass = preferences.getString("userpass", "");
        role = preferences.getString("role", "");
        PromptDialog promptDialog = new PromptDialog(this);
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(userpass) || TextUtils.isEmpty(role)) {
            FragmentManager fragmentmanager = getSupportFragmentManager();
            FragmentTransaction ft = fragmentmanager.beginTransaction();
            ft.replace(R.id.container, new SelectRoleFragment());
            ft.commit();
        } else {
            User user = new User();
            user.setUsername(username);
            user.setPassword(userpass);
            user.setRole(role);
            switch (role){
                case "商家":
                    client.login(user, new BaseObserver<BaseModel>(LoginAcitivity.this) {
                        @Override
                        protected void showDialog() {
                            promptDialog.showLoading("正在加载");
                        }

                        @Override
                        protected void hideDialog() {

                        }

                        @Override
                        protected void successful(BaseModel baseModel) {
                            promptDialog.showSuccess("加载完成");
                            startActivity(MerchantMainActivity.class);
                            finish();
                        }

                        @Override
                        protected void defeated(BaseModel baseModel) {
                            promptDialog.showError("加载失败");
                            Toast.makeText(LoginAcitivity.this,baseModel.getMessage(),Toast.LENGTH_SHORT).show();
                            SharedPreferences.Editor editor = getSharedPreferences("userdata", Context.MODE_PRIVATE).edit();
                            editor.clear();
                            editor.apply();
                            FragmentManager fragmentmanager = getSupportFragmentManager();
                            FragmentTransaction ft = fragmentmanager.beginTransaction();
                            ft.replace(R.id.container, new SelectRoleFragment());
                            ft.commit();
                        }

                        @Override
                        public void onError(ExceptionHandle.ResponeThrowable e) {
                            promptDialog.dismiss();
                            Toast.makeText(LoginAcitivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                            FragmentManager fragmentmanager = getSupportFragmentManager();
                            FragmentTransaction ft = fragmentmanager.beginTransaction();
                            ft.add(R.id.container, new NetworkErrorFragment());
                            ft.commit();
                        }
                    });
                    break;
                case "用户":
                    client.login(user, new BaseObserver<BaseModel>(LoginAcitivity.this) {
                        @Override
                        protected void showDialog() {
                            promptDialog.showLoading("正在加载");
                        }

                        @Override
                        protected void hideDialog() {

                        }

                        @Override
                        protected void successful(BaseModel baseModel) {
                            promptDialog.showSuccess("加载完成");
                            startActivity(CustomerMainActivity.class);
                            finish();
                        }

                        @Override
                        protected void defeated(BaseModel baseModel) {
                            promptDialog.showError("加载失败");
                            Toast.makeText(LoginAcitivity.this,baseModel.getMessage(),Toast.LENGTH_SHORT).show();
                            SharedPreferences.Editor editor = getSharedPreferences("userdata", Context.MODE_PRIVATE).edit();
                            editor.clear();
                            editor.apply();
                            FragmentManager fragmentmanager = getSupportFragmentManager();
                            FragmentTransaction ft = fragmentmanager.beginTransaction();
                            ft.replace(R.id.container, new SelectRoleFragment());
                            ft.commit();
                        }

                        @Override
                        public void onError(ExceptionHandle.ResponeThrowable e) {
                            promptDialog.dismiss();
                            FragmentManager fragmentmanager = getSupportFragmentManager();
                            FragmentTransaction ft = fragmentmanager.beginTransaction();
                            ft.add(R.id.container, new NetworkErrorFragment());
                            ft.commit();
                        }
                    });
                    break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void getData(MessageEvent event) {
        Bundle role_bundle = new Bundle();
        role_bundle.putString("role", role);
        FragmentManager fragmentmanager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentmanager.beginTransaction();
        switch (event.getGo()) {
            case "login":
                LoginFragment login = new LoginFragment();
                login.setArguments(role_bundle);
                ft.replace(R.id.container, login);
                ft.addToBackStack(null);
                ft.commit();
                break;
            case "register":
                RegisterFragment register = new RegisterFragment();
                register.setArguments(role_bundle);
                ft.replace(R.id.container, register);
                ft.addToBackStack(null);
                ft.commit();
                break;
            case "forget":
                ForgetFragment forgetFragment = new ForgetFragment();
                forgetFragment.setArguments(role_bundle);
                ft.replace(R.id.container, forgetFragment);
                ft.addToBackStack(null);
                ft.commit();
                break;
            case "user":
                role = "用户";
                role_bundle.putString("role",role);
                login = new LoginFragment();
                login.setArguments(role_bundle);
                ft.replace(R.id.container, login);
                ft.addToBackStack(null);
                ft.commit();
                break;
            case "merchant":
                role = "商家";
                role_bundle.putString("role",role);
                login = new LoginFragment();
                login.setArguments(role_bundle);
                ft.replace(R.id.container, login);
                ft.addToBackStack(null);
                ft.commit();
                break;
            default:
                String message = event.getGo();
                Bundle bundle = new Bundle();
                bundle.putString("user", message);
                FindPasswordFragment fragment = new FindPasswordFragment();
                fragment.setArguments(bundle);
                ft.replace(R.id.container, fragment);
                ft.addToBackStack(null);
                ft.commit();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
