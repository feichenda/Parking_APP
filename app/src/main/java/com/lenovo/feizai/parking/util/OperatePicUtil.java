package com.lenovo.feizai.parking.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * @author feizai
 * @date 2021/4/18 0018 上午 12:07:27
 */
public class OperatePicUtil {
    private static OperatePicUtil instance = new OperatePicUtil();
    private String path;

    private OperatePicUtil() {
    }

    public static OperatePicUtil getInstance() {
        return instance;
    }

    public void savePicByBm(Context context, String url,Observer<String> observer) {
        Glide.with(context).asBitmap().load(url).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                Observable.create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(@io.reactivex.rxjava3.annotations.NonNull ObservableEmitter<String> emitter) throws Throwable {
                        File destFile = createSaveFile(context, false, System.currentTimeMillis() + ".jpg", "parking/images");
                        saveBitmap2SelfDirectroy(context,resource,destFile);
                        emitter.onNext(destFile.getPath());
                        emitter.onComplete();
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(observer);
            }
        });
    }

    private void saveBitmap2SelfDirectroy(Context context, Bitmap bitmap, File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        refreshSystemPic(context, file);
    }

    private void refreshSystemPic(Context context, File destFile) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            insertPicInAndroidQ(context, destFile);
        } else {
            ContentValues value = new ContentValues();
            value.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            value.put(MediaStore.Images.Media.DATA, destFile.getAbsolutePath());
            context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, value);
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private void insertPicInAndroidQ(Context context, File insertFile) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DESCRIPTION, insertFile.getName());
        values.put(MediaStore.Images.Media.DISPLAY_NAME, insertFile.getName());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.TITLE, "Image.jpg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/");

        Uri external = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver resolver = context.getContentResolver();
        Uri insertUri = resolver.insert(external, values);
        BufferedInputStream inputStream = null;
        OutputStream os = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(insertFile));
            if (insertUri != null) {
                os = resolver.openOutputStream(insertUri);
            }
            if (os != null) {
                byte[] buffer = new byte[1024 * 4];
                int len = inputStream.read(buffer);
                while (len != -1) {
                    os.write(buffer, 0, len);
                }
                os.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File createSaveFile(Context context, Boolean isUseExternalFilesDir, String fileName, String folderName) {
        String filePath = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            filePath = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        }else {
            if (isUseExternalFilesDir) {
                filePath = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
            } else {
                filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            }
        }
        if (isUseExternalFilesDir) {
            return new File(filePath, fileName);
        } else {
            File file = new File(filePath, folderName);
            if (!file.exists()) {
                file.mkdirs();
            }
            return new File(file, fileName);
        }
    }
}
