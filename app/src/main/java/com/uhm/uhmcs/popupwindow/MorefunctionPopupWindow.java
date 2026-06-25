package com.uhm.uhmcs.popupwindow;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.uhm.uhmcs.R;
import com.uhm.uhmcs.utils.UserUtils;
import com.uhm.uhmcs.view.MaxHeightScrollView;

public class MorefunctionPopupWindow {
    private PopupWindow popupWindow;
    private Context context;
    private PopupWindowOnClickListener.MorefunctionOnClickListener morefunctionOnClickListener;
    private Animation animation;

    public MorefunctionPopupWindow(Context context, PopupWindowOnClickListener.MorefunctionOnClickListener morefunctionOnClickListener) {
        this.context = context;
        this.morefunctionOnClickListener = morefunctionOnClickListener;
        initPopup();
    }

    private void initPopup() {
        animation = AnimationUtils.loadAnimation(context, R.anim.scale_click);
        View popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_more_function, null);
        popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true
        );
        popupWindow.setOutsideTouchable(true);

        setupCardSize(popupView);

        popupView.findViewById(R.id.guanbi_btn).setOnClickListener(v -> popupWindow.dismiss());

        popupView.findViewById(R.id.xiaopiaodiy_btn).setOnClickListener(v -> {
            morefunctionOnClickListener.onClick(13);
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

        TextView textView = popupView.findViewById(R.id.dazhekaiguan_btn);
        textView.setText(!UserUtils.getInstance().isDazhe()
                ? context.getString(R.string.discount_toggle, context.getString(R.string.off))
                : context.getString(R.string.discount_toggle, context.getString(R.string.on)));
        textView.setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(10);
            popupWindow.dismiss();
        });

        TextView shangpindianjiBtn = popupView.findViewById(R.id.shangpindianji_btn);
        shangpindianjiBtn.setText(!UserUtils.getInstance().isDianji()
                ? context.getString(R.string.shangpinkaiguan, context.getString(R.string.off))
                : context.getString(R.string.shangpinkaiguan, context.getString(R.string.on)));
        shangpindianjiBtn.setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(12);
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.gengxinjiancha_btn).setOnClickListener(v -> {
            v.startAnimation(animation);
            morefunctionOnClickListener.onClick(14);
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.all_view).setOnClickListener(v -> popupWindow.dismiss());
    }

    private void setupCardSize(View popupView) {
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int horizontalMargin = dpToPx(64);
        int cardWidth = Math.min(metrics.widthPixels - horizontalMargin, dpToPx(920));
        int maxScrollHeight = (int) (metrics.heightPixels * 0.82f);

        View card = popupView.findViewById(R.id.more_feature_card);
        ViewGroup.LayoutParams cardParams = card.getLayoutParams();
        cardParams.width = cardWidth;
        card.setLayoutParams(cardParams);

        MaxHeightScrollView scrollView = popupView.findViewById(R.id.more_feature_scroll);
        scrollView.setMaxHeight(maxScrollHeight);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

    public void show() {
        View rootView = ((Activity) context).getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
    }
}
