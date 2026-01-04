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
        // 注意：这里确保 layout 文件名是你刚才修改过包含 xiaopiaoyangshi_btn 的那个 xml
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

        // 1. 同步数据
        popupView.findViewById(R.id.tongbushuju_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(1);
            popupWindow.dismiss();
        });

        // 2. 标签打印
        popupView.findViewById(R.id.bianqiandaying_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(2);
            popupWindow.dismiss();
        });

        // 3. 商品入库
        popupView.findViewById(R.id.shangpinruku_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(3);
            popupWindow.dismiss();
        });

        // 4. 历史账单
        popupView.findViewById(R.id.lishizhangdan_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(4);
            popupWindow.dismiss();
        });

        // 5. 退出登录
        popupView.findViewById(R.id.tuichudenglv_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(5);
            popupWindow.dismiss();
        });

        // 6. 钱箱设置
        popupView.findViewById(R.id.qianxiangshezhi_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(6);
            popupWindow.dismiss();
        });

        // 7. 交接班
        popupView.findViewById(R.id.jiaojieban_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(7);
            popupWindow.dismiss();
        });

        // 8. 账单打印机设置
        popupView.findViewById(R.id.zhangdandayingji_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(8);
            popupWindow.dismiss();
        });

        // 9. 标签打印机设置
        popupView.findViewById(R.id.bianqiandayingji_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(9);
            popupWindow.dismiss();
        });

        // 10. 打折开关
        TextView textView = popupView.findViewById(R.id.dazhekaiguan_tv);
        textView.setText(!UserUtils.getInstance().isDazhe() ? context.getString(R.string.discount_toggle, context.getString(R.string.off)) : context.getString(R.string.discount_toggle, context.getString(R.string.on)));

        ImageView imageView = popupView.findViewById(R.id.dazhekaiguan_iv);
        imageView.setImageDrawable(context.getDrawable(!UserUtils.getInstance().isDazhe() ? R.drawable.kaiguan_iv : R.drawable.kaiguan_iv1));
        popupView.findViewById(R.id.dazhekaiguan_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(10);
            popupWindow.dismiss();
        });

        // 11. 会员充值
        popupView.findViewById(R.id.huiyuanchongzhi_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(11);
            popupWindow.dismiss();
        });

        // 12. 商品点击开关
        TextView shangpindianji_btn = popupView.findViewById(R.id.shangpindianji_btn);
        shangpindianji_btn.setText(!UserUtils.getInstance().isDianji() ? context.getString(R.string.shangpinkaiguan, context.getString(R.string.off)) : context.getString(R.string.shangpinkaiguan, context.getString(R.string.on)));
        shangpindianji_btn.setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(12);
            popupWindow.dismiss();
        });

        // 13. 语言设置
        popupView.findViewById(R.id.yuyangshezhi_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(13);
            popupWindow.dismiss();
        });

        // =========================================================================
        // 【新增】 14. 小票样式设置
        // =========================================================================
        View btnReceiptDiy = popupView.findViewById(R.id.xiaopiaoyangshi_btn);
        if (btnReceiptDiy != null) {
            btnReceiptDiy.setOnClickListener(v -> {
                v.startAnimation(animation);
                morefunctionOnClickListener.onClick(14); // 对应 MainActivity 的 case 14
                popupWindow.dismiss();
            });
        }

        // 点击空白处关闭
        popupView.findViewById(R.id.all_view).setOnClickListener(v -> {
            popupWindow.dismiss();
        });
    }

    public void show() {
        View rootView = ((Activity) context).getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
    }
}