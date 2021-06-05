package com.lenovo.feizai.parking.customerfragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.base.BaseFragment;
import com.lenovo.feizai.parking.base.BaseModel;
import com.lenovo.feizai.parking.base.BaseObserver;
import com.lenovo.feizai.parking.entity.MessageEvent;
import com.lenovo.feizai.parking.entity.Password;
import com.lenovo.feizai.parking.net.ExceptionHandle;
import com.lenovo.feizai.parking.net.RetrofitClient;
import com.lenovo.feizai.parking.util.MD5Util;
import com.lenovo.feizai.parking.util.ToolUtil;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTouch;

/**
 * @author feizai
 * @date 2021/4/14 0014 上午 9:22:59
 * @annotation
 */
public class ChangePasswordFragment extends BaseFragment {

    @BindView(R.id.oldpassedit)
    EditText oldpassedit;
    @BindView(R.id.newpassedit)
    EditText newpassedit;
    @BindView(R.id.renewpassedit)
    EditText renewpassedit;

    private RetrofitClient client;


    public ChangePasswordFragment() {
        super(R.layout.fragment_change_password);
    }

    @Override
    protected void initView(View view) {
        client = RetrofitClient.getInstance(getContext());
    }

    @OnClick(R.id.imgback)
    public void back() {
        EventBus.getDefault().postSticky(new MessageEvent("personal"));
    }

    @OnTouch(R.id.oldsee)
    public void oldsee(View arg0, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP://松开事件发生后执行代码的区域
                oldpassedit.setTransformationMethod(PasswordTransformationMethod.getInstance());
                break;
            case MotionEvent.ACTION_DOWN://按住事件发生后执行代码的区域
                oldpassedit.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                break;
            default:
                break;
        }
        //切换后将EditText光标置于末尾
        CharSequence charSequence = oldpassedit.getText();
        if (charSequence instanceof Spannable) {
            Spannable spanText = (Spannable) charSequence;
            Selection.setSelection(spanText, charSequence.length());
        }
    }

    @OnTouch(R.id.newsee)
    public void newsee(View arg0, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP://松开事件发生后执行代码的区域
                newpassedit.setTransformationMethod(PasswordTransformationMethod.getInstance());
                break;
            case MotionEvent.ACTION_DOWN://按住事件发生后执行代码的区域
                newpassedit.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                break;
            default:
                break;
        }
        //切换后将EditText光标置于末尾
        CharSequence charSequence = newpassedit.getText();
        if (charSequence instanceof Spannable) {
            Spannable spanText = (Spannable) charSequence;
            Selection.setSelection(spanText, charSequence.length());
        }
    }

    @OnTouch(R.id.renewsee)
    public void renewsee(View arg0, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP://松开事件发生后执行代码的区域
                renewpassedit.setTransformationMethod(PasswordTransformationMethod.getInstance());
                break;
            case MotionEvent.ACTION_DOWN://按住事件发生后执行代码的区域
                renewpassedit.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                break;
            default:
                break;
        }
        //切换后将EditText光标置于末尾
        CharSequence charSequence = renewpassedit.getText();
        if (charSequence instanceof Spannable) {
            Spannable spanText = (Spannable) charSequence;
            Selection.setSelection(spanText, charSequence.length());
        }
    }

    @OnClick(R.id.oldclean)
    public void oldclean() {
        oldpassedit.setText("");
    }

    @OnClick(R.id.newclean)
    public void newclean() {
        newpassedit.setText("");
    }

    @OnClick(R.id.renewclean)
    public void renewclean() {
        renewpassedit.setText("");
    }

    @OnClick(R.id.enter)
    public void enter() {
        String oldpass = oldpassedit.getText().toString().trim();
        String newpass = newpassedit.getText().toString().trim();
        String renewpass = renewpassedit.getText().toString().trim();
        if (TextUtils.isEmpty(oldpass) || TextUtils.isEmpty(newpass) || TextUtils.isEmpty(renewpass)) {
            showToast("密码不能为空");
            return;
        }
        if (!TextUtils.equals(newpass, renewpass)) {
            showToast("两次输入的密码不一致");
            return;
        }
        if (newpass.length() < 6 || renewpass.length() < 6) {
            showToast("密码长度不能小于6位");
            return;
        }
        Password password = new Password();
        password.setUsername(ToolUtil.getUsername(getActivity()));
        password.setRole(ToolUtil.getRole(getActivity()));
        password.setOldpassword(MD5Util.string2MD5(oldpass));
        password.setNewpassword(MD5Util.string2MD5(renewpass));
        client.changePassword(password, new BaseObserver<BaseModel>(getContext()) {
            @Override
            protected void showDialog() {

            }

            @Override
            protected void hideDialog() {

            }

            @Override
            protected void successful(BaseModel baseModel) {
                showToast(baseModel.getMessage());
                SharedPreferences.Editor editor = getActivity().getSharedPreferences("userdata", Context.MODE_PRIVATE).edit();
                editor.remove("userpass");
                editor.apply();
                EventBus.getDefault().postSticky(new MessageEvent("personal"));
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
