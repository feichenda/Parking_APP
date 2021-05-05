package com.lenovo.feizai.parking.util;

import android.text.TextUtils;

import java.io.File;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * @author feizai
 * @date 2021/4/10 0010 下午 2:10:22
 * @annotation
 */
public class FileUtil {
    public static MultipartBody.Part getFile(String path, String mark) {
        File file = new File(path);
        RequestBody body = RequestBody.create(MediaType.parse("multipare/form-data"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData(mark, file.getName(), body);
        return part;
    }
}
