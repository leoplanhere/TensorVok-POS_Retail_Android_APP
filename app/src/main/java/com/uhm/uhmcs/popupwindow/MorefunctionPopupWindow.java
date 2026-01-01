package com.uhm.uhmcs.popupwindow;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.activity.MainActivity;
import com.uhm.uhmcs.utils.UserUtils;
import com.uhm.uhmcs.view.CustomInputTextView;

public class MorefunctionPopupWindow {
    private PopupWindow popupWindow;
    private Context context;
    private PopupWindowOnClickListener.MorefunctionOnClickListener morefunctionOnClickListener;



    public MorefunctionPopupWindow(Context context, PopupWindowOnClickListener.MorefunctionOnClickListener morefunctionOnClickListener) {
        this.context = context;
        this.morefunctionOnClickListener = morefunctionOnClickListener;
        initPopup();
    }


    private Animation animation;
    @SuppressLint({"StringFormatInvalid", "UseCompatLoadingForDrawables"})
    private void initPopup() {
        animation = AnimationUtils.loadAnimation(context, R.anim.scale_click);
        @SuppressLint("InflateParams") View popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_more_function, null);
        popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true
        );
//        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupView.setBackgroundColor(context.getColor(R.color.black60));
        popupWindow.setOutsideTouchable(true);
        // 计算居中位置
        popupView.post(() -> {
            DisplayMetrics metrics = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int x = (metrics.widthPixels - popupView.getWidth()) / 2;
            int y = (metrics.heightPixels - popupView.getHeight()) / 2;
            popupWindow.update(x, y, -1, -1); // 更新位置
        });

        popupView.findViewById(R.id.guanbi_btn).setOnClickListener(v -> {
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.tongbushuju_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(1);
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.bianqiandaying_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(2);
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.shangpinruku_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(3);
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.lishizhangdan_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(4);
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.tuichudenglv_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(5);
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.qianxiangshezhi_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(6);
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.jiaojieban_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(7);
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.zhangdandayingji_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(8);
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.bianqiandayingji_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(9);
            popupWindow.dismiss();
        });

        // --- 新增：POS 设置按钮绑定 ---
        View posSettingBtn = popupView.findViewById(R.id.pos_setting_btn);
        if (posSettingBtn != null) {
            posSettingBtn.setOnClickListener(v -> {
                v.startAnimation(animation);
                morefunctionOnClickListener.onClick(14); // 定义 14 为 POS 设置
                popupWindow.dismiss();
            });
        }

        TextView textView=popupView.findViewById(R.id.dazhekaiguan_tv);
        textView.setText(!UserUtils.getInstance().isDazhe()?context.getString(R.string.discount_toggle,context.getString(R.string.off)):context.getString(R.string.discount_toggle,context.getString(R.string.on)));

        ImageView imageView=popupView.findViewById(R.id.dazhekaiguan_iv);
        imageView.setImageDrawable(context.getDrawable(!UserUtils.getInstance().isDazhe()?R.drawable.kaiguan_iv:R.drawable.kaiguan_iv1));
        popupView.findViewById(R.id.dazhekaiguan_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(10);
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.huiyuanchongzhi_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(11);
            popupWindow.dismiss();
        });
//        popupView.findViewById(R.id.zhifushengzhi_btn).setOnClickListener(v -> {
//        popupView.findViewById(R.id.zhifushengzhi_btn).setOnClickListener(v -> {
//            v.startAnimation(animation);
//            morefunctionOnClickListener.onClick(11);
//            popupWindow.dismiss();
//        });

        TextView shangpindianji_btn=popupView.findViewById(R.id.shangpindianji_btn);
        shangpindianji_btn.setText(!UserUtils.getInstance().isDianji()?context.getString(R.string.shangpinkaiguan,context.getString(R.string.off)):context.getString(R.string.shangpinkaiguan,context.getString(R.string.on)));
        shangpindianji_btn.setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(12);
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.yuyangshezhi_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(13);
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.all_view).setOnClickListener(v -> {

            popupWindow.dismiss();
        });

    }

    public void show() {
        View rootView = ((Activity) context).getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);

    }
}