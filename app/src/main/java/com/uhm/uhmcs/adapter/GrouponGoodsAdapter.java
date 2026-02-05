package com.uhm.uhmcs.adapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.GrouponGoodsBean;


import java.util.List;

public class GrouponGoodsAdapter extends BaseQuickAdapter <GrouponGoodsBean.GrouponGoodsModel, BaseViewHolder> {
    public boolean isLoading = false; // 标记是否正在加载更多
    public boolean hasMore = true; // 标记是否还有更多数据可以加载
    private Context context;
    public GrouponGoodsAdapter(Context context, int layoutResId) {
        super(layoutResId);
        this.context=context;
    }

    @Override
    protected void convert(BaseViewHolder helper, GrouponGoodsBean.GrouponGoodsModel item) {
        // 1. 获取标题控件并设置内容
        TextView titleTv = helper.getView(R.id.title);
        helper.setText(R.id.title, item.getTitle());
        titleTv.setTextSize(20); // 维持你原来的字号设置

        // 2. 图片逻辑（维持你原来的注释状态）
        /* ... 图片逻辑代码 ... */

        // 3. 【核心修改】动态设置价格和货币符号
        String symbol = com.uhm.uhmcs.utils.CurrencyUtils.getSymbol();
        String priceValue = item.getPrice(); // 假设这个值是 "23.00"

        if ("weight".equals(item.getOnline_type())) {
            // 称重商品逻辑：符号 + 价格 + 单位
            // 这里建议将 "元" 改为更通用的 "/"
            helper.setText(R.id.price, symbol + priceValue + "/500g");
        } else {
            // 计件商品逻辑：符号 + 价格
            helper.setText(R.id.price, symbol + priceValue);
        }
    }


    public void loadMoreData(List<GrouponGoodsBean.GrouponGoodsModel> newData) {
        if (!isLoading && hasMore) {
            isLoading = true; // 开始加载更多数据
            addData(newData); // 添加新数据到列表末尾
            notifyDataSetChanged(); // 通知适配器数据已改变
            isLoading = false; // 加载完成，重置状态
            if (newData.isEmpty()) { // 如果新数据为空，则表示没有更多数据了
                hasMore = false;
            }
        }
    }



}
