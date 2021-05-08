package com.lenovo.feizai.parking.merchantactivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.activity.LoginAcitivity;
import com.lenovo.feizai.parking.customeractivity.CustomerSettingActivity;
import com.lenovo.feizai.parking.base.BaseActivity;
import com.lenovo.feizai.parking.base.BaseModel;
import com.lenovo.feizai.parking.base.BaseObserver;
import com.lenovo.feizai.parking.base.BaseRecyclerView;
import com.lenovo.feizai.parking.dialog.ShowPhotoDialog;
import com.lenovo.feizai.parking.entity.Location;
import com.lenovo.feizai.parking.entity.Merchant;
import com.lenovo.feizai.parking.entity.ParkingInfo;
import com.lenovo.feizai.parking.entity.MerchantProperty;
import com.lenovo.feizai.parking.entity.MerchantState;
import com.lenovo.feizai.parking.entity.User;
import com.lenovo.feizai.parking.net.ExceptionHandle;
import com.lenovo.feizai.parking.net.RequestAPI;
import com.lenovo.feizai.parking.net.RetrofitClient;
import com.lenovo.feizai.parking.util.DensityUtil;
import com.lenovo.feizai.parking.util.ToolUtil;
import com.youth.banner.Banner;
import com.youth.banner.adapter.BannerImageAdapter;
import com.youth.banner.holder.BannerImageHolder;
import com.youth.banner.indicator.CircleIndicator;
import com.youth.banner.listener.OnBannerListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;

/**
 * @author feizai
 * @date 2021/4/1 0001 上午 9:26:01
 * @annotation
 */
public class MerchantMainActivity extends BaseActivity {

    private Banner advertising;

    private BaseRecyclerView<MerchantProperty, BaseViewHolder> parking_list;
    private String username;
    private RetrofitClient client;
    private ImageView avatar_img;
    private TextView name_text;
    private TextView phone_text;
    private SlidingMenu menu;

    public MerchantMainActivity() {
        super(R.layout.activity_merchant_main);
    }

    @Override
    protected void initView() {
        username = ToolUtil.getUsername(this);
        client = RetrofitClient.getInstance(this);

        menu = initMenu();


        parking_list = new BaseRecyclerView<MerchantProperty, BaseViewHolder>(this, R.id.parking_list) {
            @Override
            public BaseQuickAdapter<MerchantProperty, BaseViewHolder> initAdapter() {
                class ParkingAdapter extends BaseQuickAdapter<MerchantProperty, BaseViewHolder> {
                    private Context context;

                    public ParkingAdapter(Context context, @org.jetbrains.annotations.Nullable List<MerchantProperty> data) {
                        super(R.layout.parking_item, data);
                        this.context = context;
                    }

                    @Override
                    protected void convert(@NotNull BaseViewHolder baseViewHolder, MerchantProperty merchantProperty) {
                        ImageView imageView = baseViewHolder.getView(R.id.image);
                        ParkingInfo parkingInfo = merchantProperty.getParkingInfo();
                        Location location = merchantProperty.getLocation();
                        MerchantState state = merchantProperty.getMerchantState();
                        String merchantimage = parkingInfo.getMerchantimage();
                        if (!TextUtils.isEmpty(merchantimage)) {
                            String[] split = merchantimage.split("&");
                            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) imageView.getLayoutParams();
                            double width = (DensityUtil.getSreenWidth(MerchantMainActivity.this) - 80) / (3.5*1.0);
                            double height = width * 1080 / (1920 * 1.0);
                            lp.width = (int) width;
                            lp.height = (int) (width *0.7);
                            imageView.setLayoutParams(lp);
                            RequestOptions myOptions = new RequestOptions().transform(new MultiTransformation<>(new CenterCrop(),new RoundedCorners(10)));

                            Glide.with(context)
                                    .load(RequestAPI.baseImageURL + split[0])
                                    .error(R.mipmap.image_error)
                                    .placeholder(R.mipmap.image_space)
                                    .apply(myOptions)
                                    .into(imageView);
                        }
                        baseViewHolder.setText(R.id.name, parkingInfo.getMerchantname());
                        baseViewHolder.setText(R.id.auditstatus, state.getAuditstate());
                        baseViewHolder.setText(R.id.state, state.getOperatingstate());
                    }
                }
                View view = LayoutInflater.from(MerchantMainActivity.this).inflate(R.layout.head_layout, null, false);
                advertising = view.findViewById(R.id.advertising);
                initBanner();
                ParkingAdapter adapter = new ParkingAdapter(MerchantMainActivity.this, null);
                adapter.addHeaderView(view);
                return adapter;
            }
        };

//        parking_list.setItemDecoration(new HorizontalDividerItemDecoration.Builder(this).size(1).color(Color.GRAY).build());
        parking_list.removeItemDecoration();

        parking_list.setOnItemClick(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                MerchantProperty merchantProperty = (MerchantProperty) adapter.getItem(position);
                switch (merchantProperty.getMerchantState().getAuditstate()) {
                    case "已通过":
                        Intent intent = new Intent(MerchantMainActivity.this, ManagementActivity.class);
                        intent.putExtra("name", merchantProperty.getParkingInfo().getMerchantname());
                        startActivity(intent);
                        break;
                    case "未通过":
                        break;
                    case "未审核":
                        showToast("您已提交，请等待管理员审核");
                        break;
                }
            }
        });

    }

    private void initBanner() {
        List<Integer> images = new ArrayList<>();
        images.add(R.mipmap.ad1);
        images.add(R.mipmap.ad2);
        images.add(R.mipmap.ad3);
        advertising.addBannerLifecycleObserver(MerchantMainActivity.this)//添加生命周期观察者
                .setIndicator(new CircleIndicator(MerchantMainActivity.this))//设置指示器
                .setAdapter(new BannerImageAdapter<Integer>(images) {
                    @Override
                    public void onBindView(BannerImageHolder holder, Integer data, int position, int size) {
                        ImageView imageView = holder.imageView;
                        int sreenWidth = DensityUtil.getSreenWidth(MerchantMainActivity.this);
                        double height = (289 * 1.0 / 640) * sreenWidth;
                        Glide.with(MerchantMainActivity.this)
                                .load(data)
                                .override(sreenWidth, (int) height)
                                .into(imageView);
                    }
                });
        advertising.setOnBannerListener(new OnBannerListener() {
            @Override
            public void OnBannerClick(Object data, int position) {
                ShowPhotoDialog dialog = new ShowPhotoDialog(MerchantMainActivity.this);
                Integer image = (Integer) data;
                dialog.setPhoto(image);
                dialog.show();
            }
        });
    }

    @OnClick(R.id.add)
    public void add() {
        startActivity(AddParkingActivity.class);
    }

    @OnClick(R.id.account)
    public void account() {
//        startActivity(MerchantPersonalMenuActivity.class);
//        overridePendingTransition(R.anim.anim_enter_from_left,R.anim.anim_exit_from_left);
        menu.showMenu();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        User user = new User();
        user.setUsername(username);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getMerchantInfo();
        client.selectMerchantBaseInfo(ToolUtil.getUsername(this), new BaseObserver<BaseModel<Merchant>>(this) {
            @Override
            protected void showDialog() {

            }

            @Override
            protected void hideDialog() {

            }

            @Override
            protected void successful(BaseModel<Merchant> merchantBaseModel) {
                Merchant merchant = merchantBaseModel.getData();
                SharedPreferences.Editor editor = getSharedPreferences("userdata", Context.MODE_PRIVATE).edit();
                editor.putString("avatar", merchant.getAvatar());
                editor.putString("phone", merchant.getPhone());
                editor.apply();

                Glide.with(MerchantMainActivity.this)
                        .load(RequestAPI.baseImageURL + ToolUtil.getUsernameAvatar(MerchantMainActivity.this))
                        .override(64, 64)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                        .into(avatar_img);
                StringBuilder sb = new StringBuilder(merchant.getPhone());
                sb.replace(3, 7, "****");
                phone_text.setText(sb.toString());
                name_text.setText(merchant.getUsername());
            }

            @Override
            protected void defeated(BaseModel<Merchant> merchantBaseModel) {
                showToast(merchantBaseModel.getMessage());
            }

            @Override
            public void onError(ExceptionHandle.ResponeThrowable e) {
                Log.e("tag", e.getMessage());
            }
        });
    }

    private void getMerchantInfo() {
        client.selectMerchantByUsername(ToolUtil.getUsername(this), new BaseObserver<BaseModel<MerchantProperty>>(this) {
            @Override
            protected void showDialog() {

            }

            @Override
            protected void hideDialog() {

            }

            @Override
            protected void successful(BaseModel<MerchantProperty> merchantPropertyBaseModel) {
                List<MerchantProperty> datas = merchantPropertyBaseModel.getDatas();
                parking_list.replaceData(datas);
            }

            @Override
            protected void defeated(BaseModel<MerchantProperty> merchantPropertyBaseModel) {
                showToast("您暂时没有可管理的停车场！");
            }

            @Override
            public void onError(ExceptionHandle.ResponeThrowable e) {

            }
        });
    }

    private SlidingMenu initMenu() {
        // configure the SlidingMenu
        SlidingMenu menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.LEFT);
        // 设置触摸屏幕的模式
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setShadowDrawable(R.drawable.shadow);

        // 设置滑动菜单视图的宽度
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        // 设置渐入渐出效果的值
        menu.setFadeDegree(0.35f);
        /**
         * SLIDING_WINDOW will include the Title/ActionBar in the content
         * section of the SlidingMenu, while SLIDING_CONTENT does not.
         */
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        //为侧滑菜单设置布局
        menu.setMenu(R.layout.activity_merchant_personal_menu);
        View menuView = menu.getMenu();

        avatar_img = menuView.findViewById(R.id.profile_image);
        name_text = menuView.findViewById(R.id.username);
        phone_text = menuView.findViewById(R.id.phone);
        RelativeLayout setting = menuView.findViewById(R.id.setting);
        RelativeLayout out = menuView.findViewById(R.id.out);
        View head = menuView.findViewById(R.id.fragment);

        avatar_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowPhotoDialog dialog = new ShowPhotoDialog(MerchantMainActivity.this);
                dialog.setCirclePhoto(RequestAPI.baseImageURL + ToolUtil.getUsernameAvatar(MerchantMainActivity.this));
                dialog.show();
            }
        });

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(CustomerSettingActivity.class);
            }
        });

        head.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(MerchantPersonalDataActivity.class, 1);
            }
        });

        out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = getSharedPreferences("userdata", Context.MODE_PRIVATE);
                preferences.edit().clear().commit();
                startActivity(LoginAcitivity.class);
                finish();
            }
        });

        menu.setTouchModeBehind(0);

        return menu;
    }

    @Override
    public void onBackPressed() {
        if (menu.isMenuShowing()) {
            menu.toggle();
        } else {
            finish();
        }
    }
}
