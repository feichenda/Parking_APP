package com.lenovo.feizai.parking.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.lenovo.feizai.parking.R;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;


/**
 * @author feizai
 * @date 12/14/2020 014 3:39:38 PM
 */
public class ShowPhotoDialog implements PhotoViewAttacher.OnPhotoTapListener {
    private String photoUrl;
    private Context context;
    private PhotoView photoView;
    private Dialog dialog;

    public ShowPhotoDialog(Context context) {
        this.context=context;
        initDialog();
    }

    private void initDialog(){
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_img, null);
        photoView = view.findViewById(R.id.dialog_img);
        dialog = new Dialog(context, R.style.photodialogstyle);
        dialog.setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        Window window = dialog.getWindow();
        // 设置显示动画
        window.setWindowAnimations(R.style.photo_dialog_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();

        wl.gravity = Gravity.CENTER;
        wl.width = WindowManager.LayoutParams.MATCH_PARENT;
        wl.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //显示状态栏

        // 设置显示位置
        dialog.onWindowAttributesChanged(wl);
        // 设置点击外围解散
        dialog.setCanceledOnTouchOutside(true);

        photoView.setOnPhotoTapListener(this);
    }

    public void setPhoto(String url){
        photoUrl=url;
        Glide.with(context).load(photoUrl).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).dontAnimate().into(photoView);
    }

    public void setPhoto(Bitmap bitmap){
        Glide.with(context).load(bitmap).into(photoView);
    }

    public void setPhoto(int resource) {
        Glide.with(context).load(resource).into(photoView);
    }

    public void setCirclePhoto(String url){
        photoUrl=url;
        Glide.with(context)
                .load(photoUrl)
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(photoView);
    }

    public boolean isShowing(){
        return dialog.isShowing();
    }

    public void show(){
        if (!isShowing()){
            dialog.show();
        }
    }

    public void setRotation(int rotation){
        photoView.setRotation(rotation);
    }

    @Override
    public void onPhotoTap(View view, float x, float y) {
        dialog.dismiss();
    }

    @Override
    public void onOutsidePhotoTap() {
        dialog.dismiss();
    }
}
