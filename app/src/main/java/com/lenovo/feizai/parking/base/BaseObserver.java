package com.lenovo.feizai.parking.base;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.lenovo.feizai.parking.net.ExceptionHandle;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * @author feizai
 * @date 12/31/2020 031 11:45:51 AM
 */
public abstract class BaseObserver<T> implements Observer<T> {

    private Context mContext;

    public BaseObserver(Context context){
        mContext=context;
    }

    @Override
    public void onSubscribe(Disposable d) {
        Log.v("Message","请求开始");
        showDialog();
    }

    @Override
    public void onNext(T t) {
        hideDialog();
        Log.v("Message","请求到数据");
        int code = 0;
//        String msg = "";
        if (t instanceof BaseModel) {
//            msg = ((BaseModel) t).getMessage();
            code = ((BaseModel) t).getCode();
        }
        switch (code) {
            case 200:
                successful(t);
                break;
            default:
                defeated(t);
                break;
        }
    }

    @Override
    public void onError(Throwable e) {
        Log.v("Message","请求出错Error:"+e.getMessage());
        ExceptionHandle.ResponeThrowable error;
        if (e instanceof ExceptionHandle.ResponeThrowable) {
            error = (ExceptionHandle.ResponeThrowable) e;
        } else {
            error = new ExceptionHandle.ResponeThrowable(e, ExceptionHandle.ERROR.UNKNOWN);
        }
        showErrorMessage(error);
    }

    @Override
    public void onComplete() {
        Log.v("Message","请求完成");
        hideDialog();
    }

    protected abstract void showDialog();

    protected abstract void hideDialog();

    protected abstract void successful(T t);

    protected abstract void defeated(T t);

    public abstract void onError(ExceptionHandle.ResponeThrowable e);

    public void showErrorMessage(ExceptionHandle.ResponeThrowable e) {
        Toast.makeText(mContext,e.getMessage(),Toast.LENGTH_SHORT).show();
        hideDialog();
        onError(e);
    }
}

