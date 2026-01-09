package com.uhm.uhmcs.popupwindow;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lxj.xpopup.core.CenterPopupView;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.utils.CurrencyUtils; // 必须导入工具类
import com.uhm.uhmcs.utils.UserUtils;

import java.util.List;

/**
 * 货币选择弹窗 - 2026 优化同步版
 * 逻辑：自动同步 CurrencyUtils.getCurrencyList() 中的配置
 */
public class CurrencySelectPopupWindow extends CenterPopupView {

    private OnCurrencySelectedListener listener;

    public interface OnCurrencySelectedListener {
        void onSelected(String currencyCode);
    }

    public CurrencySelectPopupWindow(@NonNull Context context, OnCurrencySelectedListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.popupwindow_currency_select;
    }

    @Override
    protected void onCreate() {
        super.onCreate();

        // 1. 绑定标题
        TextView tvTitle = findViewById(R.id.tv_title);
        if (tvTitle != null) {
            tvTitle.setText(getContext().getString(R.string.currency_setting));
        }

        // 2. 绑定并初始化 RecyclerView (解决报错：找不到符号 recyclerView)
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 3. 直接从 Utils 获取统一列表 (对齐方法名：getCurrencyList)
        List<CurrencyUtils.CurrencyBean> list = CurrencyUtils.getCurrencyList();

        // 4. 设置适配器
        CurrencyAdapter adapter = new CurrencyAdapter(list);
        recyclerView.setAdapter(adapter);
    }

    // --- 适配器内部类 ---
    private class CurrencyAdapter extends RecyclerView.Adapter<CurrencyAdapter.ViewHolder> {
        private List<CurrencyUtils.CurrencyBean> mData; // 使用 Utils 中的 Bean
        private String currentType;

        public CurrencyAdapter(List<CurrencyUtils.CurrencyBean> data) {
            this.mData = data;
            this.currentType = UserUtils.getInstance().getCurrencyType();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // 使用系统标准的简单列表布局
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CurrencyUtils.CurrencyBean bean = mData.get(position);
            TextView textView = (TextView) holder.itemView;

            // 显示格式：人民币 (CNY)  ￥
            textView.setText(bean.name + "  " + bean.symbol);
            textView.setPadding(50, 40, 50, 40);
            textView.setTextSize(18);

            // 高亮当前选中的货币
            if (bean.code.equalsIgnoreCase(currentType)) {
                textView.setTextColor(Color.parseColor("#FF3B82F6")); // 品牌蓝
                textView.setBackgroundColor(Color.parseColor("#F0F7FF"));
            } else {
                textView.setTextColor(Color.BLACK);
                textView.setBackgroundColor(Color.WHITE);
            }

            holder.itemView.setOnClickListener(v -> {
                // 1. 保存选中的货币代码 (如 "THB")
                UserUtils.getInstance().setCurrencyType(getContext(), bean.code);
                // 2. 回调通知 MainActivity 执行 recreate()
                if (listener != null) listener.onSelected(bean.code);
                // 3. 关闭弹窗
                dismiss();
            });
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }

}