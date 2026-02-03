package com.uhm.uhmcs.popupwindow;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.uhm.uhmcs.R;
import com.uhm.uhmcs.view.CustomInputTextView;

public class PrintNumPopupWindow {
    private PopupWindow popupWindow;
    private Context context;
    private PopupWindowOnClickListener.DiscountOnClickListener discountOnClickListener;
    private View popupView;
    private CustomInputTextView pint_num;

    public PrintNumPopupWindow(Context context, PopupWindowOnClickListener.DiscountOnClickListener discountOnClickListener) {
        this.context = context;
        this.discountOnClickListener = discountOnClickListener;
        initPopup();
    }

    private void initPopup() {
        // 1. 确保正确加载布局
        popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_print_num, null);

        if (popupView == null) {
            Toast.makeText(context, "加载打印数量布局失败", Toast.LENGTH_SHORT).show();
            return;
        }

        popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true
        );

        popupView.setBackgroundColor(context.getColor(R.color.black60));
        popupWindow.setOutsideTouchable(true);

        // 居中适配
        popupView.post(() -> {
            DisplayMetrics metrics = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int x = (metrics.widthPixels - popupView.getWidth()) / 2;
            int y = (metrics.heightPixels - popupView.getHeight()) / 2;
            popupWindow.update(x, y, -1, -1);
        });

        // 2. 绑定控件（注意：必须在 inflate 之后）
        pint_num = popupView.findViewById(R.id.print_num);
        View btnMinus = popupView.findViewById(R.id.btn_minus);
        View btnPlus = popupView.findViewById(R.id.btn_plus);
        View btnPrint = popupView.findViewById(R.id.print_btn);
        View btnClose = popupView.findViewById(R.id.guanbi_btn);

        // --- 加减逻辑 ---
        if (btnMinus != null) {
            btnMinus.setOnClickListener(v -> {
                int current = getCurrentNum();
                if (current > 1) {
                    pint_num.setText(String.valueOf(current - 1));
                }
            });
        }

        if (btnPlus != null) {
            btnPlus.setOnClickListener(v -> {
                int current = getCurrentNum();
                pint_num.setText(String.valueOf(current + 1));
            });
        }

        // --- 打印按钮 ---
        if (btnPrint != null) {
            btnPrint.setOnClickListener(v -> {
                String num = pint_num.getText().toString();
                if (!isInteger(num)) {
                    Toast.makeText(context, "请输入有效数字", Toast.LENGTH_SHORT).show();
                    return;
                }
                discountOnClickListener.onClick(num);
                popupWindow.dismiss();
            });
        }

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> popupWindow.dismiss());
        }

        pint_num.postDelayed(() -> pint_num.requestFocus(), 100);
    }

    private int getCurrentNum() {
        try {
            String text = pint_num.getText().toString();
            return Integer.parseInt(text);
        } catch (Exception e) {
            return 1;
        }
    }

    public void show() {
        if (context instanceof Activity && !((Activity) context).isFinishing()) {
            View rootView = ((Activity) context).getWindow().getDecorView();
            popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
        }
    }

    public static boolean isInteger(String str) {
        if (str == null || str.isEmpty()) return false;
        return str.matches("^\\d+$") && Integer.parseInt(str) > 0;
    }
}