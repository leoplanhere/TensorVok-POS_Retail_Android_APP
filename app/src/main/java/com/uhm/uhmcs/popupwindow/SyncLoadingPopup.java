package com.uhm.uhmcs.popupwindow;

import android.content.Context;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.lxj.xpopup.core.CenterPopupView;
import com.uhm.uhmcs.R;

/**
 * 自定义同步进度弹窗
 */
public class SyncLoadingPopup extends CenterPopupView {
    private ProgressBar progressBar;
    private TextView tvPercent;
    private TextView tvCount;

    public SyncLoadingPopup(@NonNull Context context) {
        super(context);
    }

    // 返回你的弹窗布局文件（需确保 layout 下有这个 xml）
    @Override
    protected int getImplLayoutId() {
        return R.layout.popup_sync_loading;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        progressBar = findViewById(R.id.sync_progress_bar);
        tvPercent = findViewById(R.id.tv_sync_percent);
        tvCount = findViewById(R.id.tv_sync_count);
    }

    // 更新进度的方法
    public void updateProgress(int percent, int current, int total) {
        if (progressBar != null) {
            progressBar.setProgress(percent);
            tvPercent.setText(percent + "%");
            tvCount.setText("正在同步: " + current + " / " + total);
        }
    }
}