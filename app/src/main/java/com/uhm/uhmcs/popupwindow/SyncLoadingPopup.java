package com.uhm.uhmcs.popupwindow;

import android.content.Context;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.lxj.xpopup.core.CenterPopupView;
import com.uhm.uhmcs.R;

public class SyncLoadingPopup extends CenterPopupView {

    private TextView tv_title;
    private TextView tv_progress_detail;

    public SyncLoadingPopup(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.popup_sync_loading; // 关联刚才写的 XML
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        tv_title = findViewById(R.id.tv_title);
        tv_progress_detail = findViewById(R.id.tv_progress_detail);
    }

    // 更新进度的方法
    public void updateProgress(int percent, int current, int total) {
        if (tv_title != null && tv_progress_detail != null) {
            tv_title.setText("正在同步数据... " + percent + "%");
            tv_progress_detail.setText("(" + current + " / " + total + ")");
        }
    }

    // 设置简单标题
    public void setTitle(String title) {
        if (tv_title != null) {
            tv_title.setText(title);
            tv_progress_detail.setText("");
        }
    }
}