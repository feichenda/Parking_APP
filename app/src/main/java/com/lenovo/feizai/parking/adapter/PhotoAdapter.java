package com.lenovo.feizai.parking.adapter;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.util.DensityUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author feizai
 * @date 02/09/2021 009 2:31:30 PM
 */
public class PhotoAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

    Context context;

    public PhotoAdapter(Context context, @Nullable List<String> data) {
        super(R.layout.photo_item, data);
        this.context = context;
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, String s) {
        ImageView photo_image = baseViewHolder.getView(R.id.photo_image);
        int width = (DensityUtil.getSreenWidth(context)-10-4)/3;
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) photo_image.getLayoutParams();
        lp.width=width;
        lp.height=width;
        photo_image.setLayoutParams(lp);
        Log.e("down", s);
        if (s.equals("take"))
            Glide.with(context).load(R.mipmap.take_photo).override(width,width).into(photo_image);
        else
            Glide.with(context).load(s).override(width,width).into(photo_image);
    }
}
