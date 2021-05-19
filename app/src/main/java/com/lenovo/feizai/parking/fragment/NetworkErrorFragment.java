package com.lenovo.feizai.parking.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.activity.LoginAcitivity;
import com.lenovo.feizai.parking.base.BaseFragment;
import com.lenovo.feizai.parking.base.BaseModel;
import com.lenovo.feizai.parking.base.BaseObserver;
import com.lenovo.feizai.parking.customeractivity.CustomerMainActivity;
import com.lenovo.feizai.parking.entity.User;
import com.lenovo.feizai.parking.merchantactivity.MerchantMainActivity;
import com.lenovo.feizai.parking.net.ExceptionHandle;
import com.lenovo.feizai.parking.net.RetrofitClient;

import butterknife.OnClick;
import me.leefeng.promptlibrary.PromptDialog;

/**
 * @author feizai
 * @date 2021/3/25 0025 上午 11:11:53
 * @annotation
 */
public class NetworkErrorFragment extends BaseFragment {

    private RetrofitClient client;

    public NetworkErrorFragment() {
        super(R.layout.fragment_network_error);
    }

    @Override
    protected void initView(View view) {
        SharedPreferences preferences = getActivity().getSharedPreferences("userdata", Context.MODE_PRIVATE);
        String username = preferences.getString("username", "");
        String userpass = preferences.getString("userpass", "");
        String role = preferences.getString("role", "");
        User user = new User();
        user.setUsername(username);
        user.setPassword(userpass);
        user.setRole(role);
        client = RetrofitClient.getInstance(getContext());
        view.findViewById(R.id.retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retry(user);
            }
        });
    }

    public void retry(User user) {
        PromptDialog promptDialog = new PromptDialog(getActivity());
        switch (user.getRole()){
            case "商家":
                client.login(user, new BaseObserver<BaseModel>(getContext()) {
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
                        getActivity().finish();
                    }

                    @Override
                    protected void defeated(BaseModel baseModel) {
                        promptDialog.showError("加载失败");
                        Toast.makeText(getContext(),baseModel.getMessage(),Toast.LENGTH_SHORT).show();
                        SharedPreferences.Editor editor = getActivity().getSharedPreferences("userdata", Context.MODE_PRIVATE).edit();
                        editor.clear();
                        editor.apply();
                        FragmentManager fragmentmanager = getActivity().getSupportFragmentManager();
                        FragmentTransaction ft = fragmentmanager.beginTransaction();
                        ft.replace(R.id.container, new SelectRoleFragment());
                        ft.commit();
                    }

                    @Override
                    public void onError(ExceptionHandle.ResponeThrowable e) {
                        promptDialog.dismiss();
                        Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                        FragmentManager fragmentmanager = getActivity().getSupportFragmentManager();
                        FragmentTransaction ft = fragmentmanager.beginTransaction();
                        ft.add(R.id.container, new NetworkErrorFragment());
                        ft.commit();
                    }
                });
                break;
            case "用户":
                client.login(user, new BaseObserver<BaseModel>(getContext()) {
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
                        getActivity().finish();
                    }

                    @Override
                    protected void defeated(BaseModel baseModel) {
                        promptDialog.showError("加载失败");
                        Toast.makeText(getContext(),baseModel.getMessage(),Toast.LENGTH_SHORT).show();
                        SharedPreferences.Editor editor = getActivity().getSharedPreferences("userdata", Context.MODE_PRIVATE).edit();
                        editor.clear();
                        editor.apply();
                        FragmentManager fragmentmanager = getActivity().getSupportFragmentManager();
                        FragmentTransaction ft = fragmentmanager.beginTransaction();
                        ft.replace(R.id.container, new SelectRoleFragment());
                        ft.commit();
                    }

                    @Override
                    public void onError(ExceptionHandle.ResponeThrowable e) {
                        promptDialog.dismiss();
                        FragmentManager fragmentmanager = getActivity().getSupportFragmentManager();
                        FragmentTransaction ft = fragmentmanager.beginTransaction();
                        ft.add(R.id.container, new NetworkErrorFragment());
                        ft.commit();
                    }
                });
                break;
        }
    }
}
