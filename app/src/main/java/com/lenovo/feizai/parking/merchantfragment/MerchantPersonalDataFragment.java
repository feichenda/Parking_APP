package com.lenovo.feizai.parking.merchantfragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.input.DialogInputExtKt;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.base.BaseFragment;
import com.lenovo.feizai.parking.base.BaseModel;
import com.lenovo.feizai.parking.base.BaseObserver;
import com.lenovo.feizai.parking.dialog.OptionDialog;
import com.lenovo.feizai.parking.entity.Customer;
import com.lenovo.feizai.parking.entity.Merchant;
import com.lenovo.feizai.parking.entity.MessageEvent;
import com.lenovo.feizai.parking.net.ExceptionHandle;
import com.lenovo.feizai.parking.net.RequestAPI;
import com.lenovo.feizai.parking.net.RetrofitClient;
import com.lenovo.feizai.parking.util.FileUtil;
import com.lenovo.feizai.parking.util.SelectPhotoUtil;
import com.lenovo.feizai.parking.util.ToolUtil;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.MultipartBody;

import static android.app.Activity.RESULT_OK;

/**
 * @author feizai
 * @date 2021/4/14 0014 上午 9:18:17
 * @annotation
 */
public class MerchantPersonalDataFragment extends BaseFragment {

    @BindView(R.id.head_img)
    ImageView avatar_img;
    @BindView(R.id.nickname)
    TextView nickname;
    @BindView(R.id.phonenumber)
    TextView phonenumber;
    @BindView(R.id.qqnumber)
    TextView qqnumber;

    private RetrofitClient client;
    private String newPath;
    private Merchant merchant;

    public MerchantPersonalDataFragment() {
        super(R.layout.fragment_personal_data);
    }

    @Override
    protected void initView(View view) {
        client = RetrofitClient.getInstance(getContext());
    }

    @OnClick(R.id.imgback)
    public void back() {
        Intent intent = new Intent();
        intent.putExtra("phone", phonenumber.getText().toString().trim());
        intent.putExtra("user", nickname.getText().toString().trim());
        intent.putExtra("avatar", ToolUtil.getUsernameAvatar(getActivity()));
        getActivity().setResult(RESULT_OK, intent);
        getActivity().finish();
    }

    @OnClick(R.id.avatar)
    public void changeavatat() {
        OptionDialog dialog = new OptionDialog(getContext());
        dialog.setTitleTxt("更换头像");
        dialog.setFirstOptionTxt("拍一张");
        dialog.setSecondOptionTxt("选一张");
        dialog.setOnBtnClickLister(new OptionDialog.OnBtnClickLister() {
            @Override
            public void first() {
                SelectPhotoUtil.shootAvatar(getActivity(), new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(List<LocalMedia> result) {
                        if (result.get(0).isCut()) {
                            newPath = result.get(0).getCutPath();
                        } else {
                            if (result.get(0).isCompressed()) {
                                newPath = result.get(0).getCompressPath();
                            } else {
                                newPath = result.get(0).getPath();
                            }
                        }
                        //上传
                        MultipartBody.Part file = FileUtil.getFile(newPath, "file");
                        client.upadteMerchantAvatar(ToolUtil.getUsername(getActivity()), file, new BaseObserver<BaseModel<String>>(getContext()) {
                            @Override
                            protected void showDialog() {

                            }

                            @Override
                            protected void hideDialog() {

                            }

                            @Override
                            protected void successful(BaseModel<String> stringBaseModel) {
                                showToast("头像上传成功");
                                Glide.with(getContext())
                                        .load(newPath)
                                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                                        .skipMemoryCache(true)
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .into(avatar_img);
                                SharedPreferences.Editor editor = getActivity().getSharedPreferences("userdata", Context.MODE_PRIVATE).edit();
                                editor.putString("avatar", stringBaseModel.getData());
                                editor.apply();
                            }

                            @Override
                            protected void defeated(BaseModel<String> stringBaseModel) {
                                showToast("头像上传失败");
                            }

                            @Override
                            public void onError(ExceptionHandle.ResponeThrowable e) {
                                Log.e("tag", e.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onCancel() {

                    }
                });
            }

            @Override
            public void second() {
                SelectPhotoUtil.selectAvatar(getActivity(), 1, new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(List<LocalMedia> result) {
                        if (result.get(0).isCut()) {
                            newPath = result.get(0).getCutPath();
                        } else {
                            if (result.get(0).isCompressed()) {
                                newPath = result.get(0).getCompressPath();
                            } else {
                                newPath = result.get(0).getPath();
                            }
                        }
                        MultipartBody.Part file = FileUtil.getFile(newPath, "file");
                        client.upadteMerchantAvatar(ToolUtil.getUsername(getActivity()), file, new BaseObserver<BaseModel<String>>(getContext()) {
                            @Override
                            protected void showDialog() {

                            }

                            @Override
                            protected void hideDialog() {

                            }

                            @Override
                            protected void successful(BaseModel<String> stringBaseModel) {
                                showToast("头像上传成功");
                                Glide.with(getContext())
                                        .load(newPath)
                                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                                        .skipMemoryCache(true)
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .into(avatar_img);
                                SharedPreferences.Editor editor = getActivity().getSharedPreferences("userdata", Context.MODE_PRIVATE).edit();
                                editor.putString("avatar", stringBaseModel.getData());
                                editor.apply();
                            }

                            @Override
                            protected void defeated(BaseModel<String> stringBaseModel) {
                                showToast("头像上传失败");
                            }

                            @Override
                            public void onError(ExceptionHandle.ResponeThrowable e) {
                                Log.e("tag", e.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onCancel() {

                    }
                });
            }

            @Override
            public void cancle() {

            }
        });
        dialog.show();
    }

    @OnClick(R.id.username)
    public void changeusername() {
        MaterialDialog dialog = new MaterialDialog(getContext(), MaterialDialog.getDEFAULT_BEHAVIOR());
        dialog.title(null, "修改用户名");
        DialogInputExtKt.input(dialog, null, null, nickname.getText().toString().trim(), null, InputType.TYPE_CLASS_TEXT, 8, true, false, (materialDialog, text) -> {
            if (text.toString().equals(nickname.getText().toString().trim())) {
                return null;
            }
            client.updateUsername(ToolUtil.getUsername(getActivity()), text.toString(), ToolUtil.getRole(getActivity()), new BaseObserver<BaseModel<String>>(getContext()) {
                @Override
                protected void showDialog() {

                }

                @Override
                protected void hideDialog() {

                }

                @Override
                protected void successful(BaseModel<String> stringBaseModel) {
                    showToast("修改成功");
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("userdata", Context.MODE_PRIVATE).edit();
                    editor.putString("username", text.toString());
                    editor.putString("avatar", stringBaseModel.getData());
                    editor.apply();
                    nickname.setText(text);
                }

                @Override
                protected void defeated(BaseModel<String> stringBaseModel) {
                    showToast(stringBaseModel.getMessage());
                }

                @Override
                public void onError(ExceptionHandle.ResponeThrowable e) {
                    Log.e("tag", e.getMessage());
                }
            });
            return null;
        });
        dialog.show();
    }

    @OnClick(R.id.phone)
    public void changephone() {
        MaterialDialog dialog = new MaterialDialog(getContext(), MaterialDialog.getDEFAULT_BEHAVIOR());
        dialog.title(null, "修改手机号");
        DialogInputExtKt.input(dialog, null, null, null, null, InputType.TYPE_CLASS_PHONE, 11, true, false, (materialDialog, text) -> {
            if (ToolUtil.checkMobileNumber(text.toString())) {
                client.updateMerchantPhone(ToolUtil.getUsername(getActivity()), text.toString(), new BaseObserver<BaseModel<String>>(getContext()) {
                    @Override
                    protected void showDialog() {

                    }

                    @Override
                    protected void hideDialog() {

                    }

                    @Override
                    protected void successful(BaseModel<String> stringBaseModel) {
                        phonenumber.setText(stringBaseModel.getData());
                        showToast(stringBaseModel.getMessage());
                    }

                    @Override
                    protected void defeated(BaseModel<String> stringBaseModel) {
                        showToast(stringBaseModel.getMessage());
                    }

                    @Override
                    public void onError(ExceptionHandle.ResponeThrowable e) {
                        Log.e("tag", e.getMessage());
                    }
                });
            } else {
                showToast("您输入的手机号有误");
            }
            return null;
        });
        dialog.show();
    }

    @OnClick(R.id.qq)
    public void changeqq() {
        MaterialDialog dialog = new MaterialDialog(getContext(), MaterialDialog.getDEFAULT_BEHAVIOR());
        dialog.title(null, "修改QQ号");
        DialogInputExtKt.input(dialog, null, null, null, null, InputType.TYPE_CLASS_PHONE, 10, true, false, (materialDialog, text) -> {
            client.updateMerchantQQ(ToolUtil.getUsername(getActivity()), text.toString(), new BaseObserver<BaseModel<String>>(getContext()) {
                @Override
                protected void showDialog() {

                }

                @Override
                protected void hideDialog() {

                }

                @Override
                protected void successful(BaseModel<String> stringBaseModel) {
                    qqnumber.setText(stringBaseModel.getData());
                    showToast(stringBaseModel.getMessage());
                }

                @Override
                protected void defeated(BaseModel<String> stringBaseModel) {
                    showToast(stringBaseModel.getMessage());
                }

                @Override
                public void onError(ExceptionHandle.ResponeThrowable e) {
                    Log.e("tag", e.getMessage());
                }
            });
            return null;
        });
        dialog.show();
    }

    @OnClick(R.id.changepassword)
    public void changepassword() {
        EventBus.getDefault().postSticky(new MessageEvent("password"));
    }

    @Override
    public void onStart() {
        super.onStart();
        client.selectMerchantBaseInfo(ToolUtil.getUsername(getActivity()), new BaseObserver<BaseModel<Merchant>>(getContext()) {
            @Override
            protected void showDialog() {

            }

            @Override
            protected void hideDialog() {

            }

            @Override
            protected void successful(BaseModel<Merchant> merchantBaseModel) {
                merchant = merchantBaseModel.getData();
                Glide.with(getContext())
                        .load(RequestAPI.baseImageURL + merchant.getAvatar())
                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(avatar_img);
                nickname.setText(merchant.getUsername());
                phonenumber.setText(merchant.getPhone());
                qqnumber.setText(merchant.getQQ());
            }

            @Override
            protected void defeated(BaseModel<Merchant> merchantBaseModel) {

            }

            @Override
            public void onError(ExceptionHandle.ResponeThrowable e) {

            }
        });
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
}
