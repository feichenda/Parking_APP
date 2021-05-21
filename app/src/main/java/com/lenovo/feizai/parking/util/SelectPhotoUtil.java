package com.lenovo.feizai.parking.util;

import android.app.Activity;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;

/**
 * @author feizai
 * @date 12/22/2020 022 5:10:08 PM
 */
public class SelectPhotoUtil {

    public static void shoot(Activity activity,OnResultCallbackListener<LocalMedia> onResultCallbackListener){
        PictureSelector.create(activity)
                .openCamera(PictureMimeType.ofImage())
                .loadImageEngine(GlideEngine.createGlideEngine())
                .compress(true)
                .minimumCompressSize(2048)//小于2048的不压缩
                .forResult(onResultCallbackListener);
    }

    public static void shootAvatar(Activity activity,OnResultCallbackListener<LocalMedia> onResultCallbackListener){
        PictureSelector.create(activity)
                .openCamera(PictureMimeType.ofImage())
                .loadImageEngine(GlideEngine.createGlideEngine())
                .compress(true)
                .minimumCompressSize(100)//小于100k的不压缩
                .enableCrop(true)//是否开启裁剪
                .withAspectRatio(1,1)
                .forResult(onResultCallbackListener);
    }

    public static void select(Activity activity,int maxNum,OnResultCallbackListener<LocalMedia> onResultCallbackListener){
        PictureSelector.create(activity)
                .openGallery(PictureMimeType.ofImage())
                .loadImageEngine(GlideEngine.createGlideEngine())
                .selectionMode(PictureConfig.MULTIPLE)//设置为多选模式
                .maxSelectNum(maxNum<=0?1:maxNum)//设置最大选择图片数
                .isWeChatStyle(true)//开启微信样式
                .isCamera(false)
                .isAutomaticTitleRecyclerTop(true)
                .compress(true)
                .minimumCompressSize(2048)
                .forResult(onResultCallbackListener);
    }

    public static void selectAvatar(Activity activity,int maxNum,OnResultCallbackListener<LocalMedia> onResultCallbackListener){
        PictureSelector.create(activity)
                .openGallery(PictureMimeType.ofImage())
                .loadImageEngine(GlideEngine.createGlideEngine())
                .selectionMode(PictureConfig.MULTIPLE)//设置为多选模式
                .maxSelectNum(maxNum<=0?1:maxNum)//设置最大选择图片数
                .isWeChatStyle(true)//开启微信样式
                .isCamera(false)
                .isAutomaticTitleRecyclerTop(true)
                .compress(true)
                .minimumCompressSize(100)
                .enableCrop(true)//是否开启裁剪
                .withAspectRatio(1,1)
                .forResult(onResultCallbackListener);
    }

}
