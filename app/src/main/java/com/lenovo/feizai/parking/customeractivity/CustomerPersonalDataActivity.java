package com.lenovo.feizai.parking.customeractivity;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.base.BaseActivity;
import com.lenovo.feizai.parking.customerfragment.ChangePasswordFragment;
import com.lenovo.feizai.parking.customerfragment.UserPersonalDataFragment;
import com.lenovo.feizai.parking.entity.MessageEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author feizai
 * @date 2021/4/13 0013 下午 5:33:43
 * @annotation
 */
public class CustomerPersonalDataActivity extends BaseActivity {

    public CustomerPersonalDataActivity() {
        super(R.layout.activity_container);
    }

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);
        loadFragment("personal");
    }

    private void loadFragment(String flag){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        switch (flag){
            case "personal":
                UserPersonalDataFragment personal = new UserPersonalDataFragment();
                ft.replace(R.id.container, personal);
                break;
            case "password":
                ChangePasswordFragment password = new ChangePasswordFragment();
                ft.replace(R.id.container, password);
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
