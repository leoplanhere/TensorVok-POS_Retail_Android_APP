package com.uhm.uhmcs.popupwindow;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.uhm.uhmcs.R;
import com.uhm.uhmcs.view.CustomInputTextView;

public class GoodsWarehousingPopupWindow {
    private PopupWindow popupWindow;
    private Context context;
    private CustomInputTextView shangpintiaoma_tv,kuchunshuliang_tv;
    private TextView shangpin_tv;




    public GoodsWarehousingPopupWindow(Context context) {
        this.context = context;
        initPopup();
    }



    private Runnable shangpintiaomaRunnable = new Runnable() {
        @Override
        public void run() {
            shangpintiaoma_tv.requestFocus();
        }
    };
    private Runnable kuchunshuliangRunnable = new Runnable() {
        @Override
        public void run() {
            kuchunshuliang_tv.requestFocus();
        }
    };
    private void initPopup() {
        View popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_goods_warehousing, null);
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
        shangpintiaoma_tv=popupView.findViewById(R.id.shangpintiaoma_tv);
        kuchunshuliang_tv=popupView.findViewById(R.id.kuchunshuliang_tv);
        shangpin_tv=popupView.findViewById(R.id.shangpin_tv);
        // 自动获取焦点
        shangpintiaoma_tv.postDelayed(shangpintiaomaRunnable,100);
        shangpintiaoma_tv.setOnInputCompleteListener(text -> {


        });
        // 自动获取焦点
//        kuchunshuliang_tv.postDelayed(() -> kuchunshuliang_tv.requestFocus(), 100);
        kuchunshuliang_tv.setOnInputCompleteListener(text -> {

        });


    }

    public void show() {
        View rootView = ((Activity) context).getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);

    }
}
