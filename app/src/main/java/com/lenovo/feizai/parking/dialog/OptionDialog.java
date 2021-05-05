package com.lenovo.feizai.parking.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.lenovo.feizai.parking.R;

/**
 * @author feizai
 * @date 12/22/2020 022 3:57:18 PM
 */
public class OptionDialog implements View.OnClickListener {

    private Context context;
    private Dialog dialog;
    private OnBtnClickLister btnClickLister;
    TextView title;
    Button firstOption;
    Button secondOption;
    Button cancleOption;
    View firstview;

    public OptionDialog(Context context) {
        this.context=context;
        initDialog();
    }

    private void initDialog() {
        View view= LayoutInflater.from(context).inflate(R.layout.option_dialog,null,false);
        findBtn(view);
        dialog=new Dialog(context, R.style.optiondialogstyle);
        dialog.setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        Window window = dialog.getWindow();
        // 设置显示动画
        window.setWindowAnimations(R.style.option_dialog_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.x = 0;
        wl.y = ((Activity) context).getWindowManager().getDefaultDisplay()
                .getHeight();
        // 以下这两句是为了保证按钮可以水平满屏
        wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //显示状态栏

        // 设置显示位置
        dialog.onWindowAttributesChanged(wl);
        // 设置点击外围解散
        dialog.setCanceledOnTouchOutside(true);
    }

    private void findBtn(View view) {
        title=view.findViewById(R.id.dialogtitle);
        firstview=view.findViewById(R.id.firstview);
        firstOption=view.findViewById(R.id.firstoption);
        secondOption=view.findViewById(R.id.secondtoption);
        cancleOption=view.findViewById(R.id.cancle);
        firstOption.setOnClickListener(this);
        secondOption.setOnClickListener(this);
        cancleOption.setOnClickListener(this);
    }

    public void setTitleTxt(String titleTxt){
        title.setText(titleTxt);
    }
    public void setFirstOptionTxt(String firstOptionTxt){
        firstOption.setText(firstOptionTxt);
    }
    public void setSecondOptionTxt(String secondOptionTxt){
        secondOption.setText(secondOptionTxt);
    }

    public void setTitleColor(int color){
        title.setTextColor(color);
    }
    public void setFirstOptionColor(int color){
        firstOption.setTextColor(color);
    }
    public void setSecondOptionColor(int color){
        secondOption.setTextColor(color);
    }
    public void setCancleOptionColor(int color){
        cancleOption.setTextColor(color);
    }

    public void setTitleSize(float size){
        title.setTextSize(size);
    }
    public void setFirstOptionSize(float size){
        firstOption.setTextSize(size);
    }
    public void setsecondOptionSize(float size){
        secondOption.setTextSize(size);
    }
    public void setCancleOptionSize(float size){
        cancleOption.setTextSize(size);
    }

    public void setShowTitle(boolean value){
        title.setVisibility(value?View.VISIBLE:View.GONE);
        firstview.setVisibility(value?View.VISIBLE:View.GONE);
        if (!value)
            firstOption.setBackgroundResource(R.drawable.white_top_radius);
    }

    //设置监听
    public void setOnBtnClickLister(OnBtnClickLister onBtnClickLister) {
        btnClickLister = onBtnClickLister;
    }

    public boolean isShowing(){
        return dialog.isShowing();
    }

    public void show() {
        if (!isShowing()) {
            dialog.show();
        }
    }

    public void dismiss() {
        dialog.dismiss();
    }

    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.firstoption){
            if (btnClickLister != null) {
                btnClickLister.first();
            }
        }
        if (v.getId()==R.id.secondtoption){
            if (btnClickLister != null) {
                btnClickLister.second();
            }
        }
        if (v.getId()==R.id.cancle){
            if (btnClickLister != null) {
                btnClickLister.cancle();
            }
        }
//        switch (v.getId()) {
//            case R.id.firstoption:
//                if (btnClickLister != null) {
//                    btnClickLister.first();
//                }
//                break;
//            case R.id.secondtoption:
//                if (btnClickLister != null) {
//                    btnClickLister.second();
//                }
//
//                break;
//            case R.id.cancle:
//                if (btnClickLister != null) {
//                    btnClickLister.cancle();
//                }
//                break;
//        }
        dismiss();
    }

    public interface OnBtnClickLister {
        void first(); // 第一项

        void second();// 第二项

        void cancle();// 取消
    }
}

