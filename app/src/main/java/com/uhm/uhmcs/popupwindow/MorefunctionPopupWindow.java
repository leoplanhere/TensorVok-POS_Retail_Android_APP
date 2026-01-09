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

        // 1. 关闭按钮
        popupView.findViewById(R.id.guanbi_btn).setOnClickListener(v -> {
            popupWindow.dismiss();
        });

        // 2. 同步数据 (Case 1)
        popupView.findViewById(R.id.tongbushuju_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(1);
            popupWindow.dismiss();
        });

        // 3. 标签打印 (Case 2)
        popupView.findViewById(R.id.bianqiandaying_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(2);
            popupWindow.dismiss();
        });

        // 4. 商品入库 (Case 3)
        popupView.findViewById(R.id.shangpinruku_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(3);
            popupWindow.dismiss();
        });

        // 5. 历史账单 (Case 4)
        popupView.findViewById(R.id.lishizhangdan_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(4);
            popupWindow.dismiss();
        });

        // 6. 退出登录 (Case 5)
        popupView.findViewById(R.id.tuichudenglv_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(5);
            popupWindow.dismiss();
        });

        // 7. 钱箱设置 (Case 6)
        View btnMoneyBox = popupView.findViewById(R.id.qianxiangshezhi_btn);
        if (btnMoneyBox != null) {
            btnMoneyBox.setOnClickListener(v -> {
                v.startAnimation(animation);
                morefunctionOnClickListener.onClick(6);
                popupWindow.dismiss();
            });
        }

        // 8. 交接班 (Case 7)
        popupView.findViewById(R.id.jiaojieban_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(7);
            popupWindow.dismiss();
        });

        // 9. 账单打印机设置 (Case 8)
        popupView.findViewById(R.id.zhangdandayingji_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(8);
            popupWindow.dismiss();
        });

        // 10. 标签打印机设置 (Case 9)
        popupView.findViewById(R.id.bianqiandayingji_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(9);
            popupWindow.dismiss();
        });

        // 11. 打折开关 (Case 10)
        TextView dazheTv = popupView.findViewById(R.id.dazhekaiguan_tv);
        dazheTv.setText(!UserUtils.getInstance().isDazhe() ? context.getString(R.string.discount_toggle, context.getString(R.string.off)) : context.getString(R.string.discount_toggle, context.getString(R.string.on)));

        ImageView dazheIv = popupView.findViewById(R.id.dazhekaiguan_iv);
        dazheIv.setImageDrawable(context.getDrawable(!UserUtils.getInstance().isDazhe() ? R.drawable.kaiguan_iv : R.drawable.kaiguan_iv1));

        popupView.findViewById(R.id.dazhekaiguan_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(10);
            popupWindow.dismiss();
        });

        // 12. 会员充值 (Case 11)
        View btnMemberRecharge = popupView.findViewById(R.id.huiyuanchongzhi_btn);
        if (btnMemberRecharge != null) {
            btnMemberRecharge.setOnClickListener(v -> {
                v.startAnimation(animation);
                morefunctionOnClickListener.onClick(11);
                popupWindow.dismiss();
            });
        }

        // 13. 商品点击开关 (Case 12)
        TextView shangpindianji_btn = popupView.findViewById(R.id.shangpindianji_btn);
        if (shangpindianji_btn != null) {
            shangpindianji_btn.setText(!UserUtils.getInstance().isDianji() ? context.getString(R.string.shangpinkaiguan, context.getString(R.string.off)) : context.getString(R.string.shangpinkaiguan, context.getString(R.string.on)));
            shangpindianji_btn.setOnClickListener(v -> {
                v.startAnimation(animation);
                morefunctionOnClickListener.onClick(12);
                popupWindow.dismiss();
            });
        }

        // 14. 语言设置 (Case 13)
        popupView.findViewById(R.id.yuyangshezhi_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(13);
            popupWindow.dismiss();
        });

        // =========================================================================
        // 【新增】 15. 货币设置 (Case 16)
        // =========================================================================
        View btnCurrencySetting = popupView.findViewById(R.id.huobishezhi_btn);
        if (btnCurrencySetting != null) {
            btnCurrencySetting.setOnClickListener(v -> {
                v.startAnimation(animation);
                morefunctionOnClickListener.onClick(16); // 对应 MainActivity 的 Case 16
                popupWindow.dismiss();
            });
        }

        // 16. POS 通信设置 (Case 14)
        View posSettingBtn = popupView.findViewById(R.id.pos_setting_btn);
        if (posSettingBtn != null) {
            posSettingBtn.setOnClickListener(v -> {
                v.startAnimation(animation);
                morefunctionOnClickListener.onClick(14);
                popupWindow.dismiss();
            });
        }

        // 17. 小票样式 DIY 设置 (Case 15)
        View btnReceiptDiy = popupView.findViewById(R.id.xiaopiaoyangshi_btn);
        if (btnReceiptDiy != null) {
            btnReceiptDiy.setOnClickListener(v -> {
                v.startAnimation(animation);
                morefunctionOnClickListener.onClick(15);
                popupWindow.dismiss();
            });
        }

        // 18. 点击背景阴影关闭
        popupView.findViewById(R.id.all_view).setOnClickListener(v -> {
            popupWindow.dismiss();
        });
    }

    public void show() {
        View rootView = ((Activity) context).getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
    }
}