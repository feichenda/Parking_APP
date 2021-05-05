package com.lenovo.feizai.parking.merchantactivity;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.base.BaseActivity;
import com.lenovo.feizai.parking.entity.MessageEvent;
import com.lenovo.feizai.parking.merchantfragment.AddInfoSettingFragment;
import com.lenovo.feizai.parking.merchantfragment.ChangeInfoSettingFragment;
import com.lenovo.feizai.parking.merchantfragment.ChangeLinkFragment;
import com.lenovo.feizai.parking.merchantfragment.MerchantOrderFragment;
import com.lenovo.feizai.parking.merchantfragment.ParkingSettingFragment;
import com.lenovo.feizai.parking.merchantfragment.ParkingSpaceSettingFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author feizai
 * @date 2021/4/2 0002 下午 9:44:44
 * @annotation
 */
public class ParkingSettingActivity extends BaseActivity {

    private String merchant;

    public ParkingSettingActivity() {
        super(R.layout.activity_container);
    }

    @Override
    protected void initView() {
        Intent intent = getIntent();
        merchant = intent.getStringExtra("name");
        EventBus.getDefault().register(this);
        loadFragment("setting");
    }

    private void loadFragment(String flag){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Bundle bundle = new Bundle();
        switch (flag){
            case "setting":
                ParkingSettingFragment setting = new ParkingSettingFragment();
                bundle.putString("name",merchant);
                setting.setArguments(bundle);
                ft.replace(R.id.container, setting);
                break;
            case "space":
                ParkingSpaceSettingFragment spaceSetting = new ParkingSpaceSettingFragment();
                bundle.putString("name",merchant);
                spaceSetting.setArguments(bundle);
                ft.replace(R.id.container, spaceSetting);
                ft.addToBackStack(null);
                break;
            case "addinfo":
                AddInfoSettingFragment addInfoSettingFragment = new AddInfoSettingFragment();
                bundle.putString("name",merchant);
                addInfoSettingFragment.setArguments(bundle);
                ft.replace(R.id.container, addInfoSettingFragment);
                ft.addToBackStack(null);
                break;
            case "seeinfo":
                showToast("您已提交，请等待管理员审核");
                break;
            case "changeinfo":
                ChangeInfoSettingFragment changeInfoSettingFragment = new ChangeInfoSettingFragment();
                bundle.putString("name",merchant);
                changeInfoSettingFragment.setArguments(bundle);
                ft.replace(R.id.container, changeInfoSettingFragment);
                ft.addToBackStack(null);
                break;
            case "link":
                ChangeLinkFragment changeLinkFragment = new ChangeLinkFragment();
                bundle.putString("name",merchant);
                changeLinkFragment.setArguments(bundle);
                ft.replace(R.id.container, changeLinkFragment);
                ft.addToBackStack(null);
                break;
            case "order":
                MerchantOrderFragment merchantOrderFragment = new MerchantOrderFragment();
                bundle.putString("name",merchant);
                merchantOrderFragment.setArguments(bundle);
                ft.replace(R.id.container, merchantOrderFragment);
                ft.addToBackStack(null);
                break;
        }

        ft.commit();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void getData(MessageEvent event) {
        loadFragment(event.getGo());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
