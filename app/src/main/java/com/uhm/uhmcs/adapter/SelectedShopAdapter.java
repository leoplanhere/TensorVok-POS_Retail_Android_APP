package com.uhm.uhmcs.adapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.CategoryListBean;
import com.uhm.uhmcs.bean.GrouponGoodsBean;
import com.uhm.uhmcs.utils.UserUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SelectedShopAdapter extends BaseQuickAdapter<GrouponGoodsBean.GrouponGoodsModel, BaseViewHolder> {
    private Context context;
    public SelectedShopAdapter(Context context, int layoutResId) {
        super(layoutResId);
        this.context=context;
    }


    @Override
    protected void convert(BaseViewHolder helper, GrouponGoodsBean.GrouponGoodsModel item) {
        // 1. 基础文字赋值
        helper.setText(R.id.xuhao, (helper.getLayoutPosition() + 1) + "");
        helper.setText(R.id.pinming, item.getTitle());
        helper.setText(R.id.shuliang, item.getShuliang() + "");
        helper.setText(R.id.heji, item.getHeji() + "");
        helper.setText(R.id.guige, "暂无规格");

        // 2. 获取 UI 控件
        TextView tvXuhao = helper.getView(R.id.xuhao);
        TextView tvPinming = helper.getView(R.id.pinming);
        TextView tvShuliang = helper.getView(R.id.shuliang);
        TextView tvHeji = helper.getView(R.id.heji);
        TextView tvZhekou = helper.getView(R.id.zhekou_view);

        // 3. 【核心分支逻辑】
        if (item.isIs_zengsong()) {
            // ======= 赠品模式 =======
            int red = Color.RED;
            // (1) 文字变红
            tvXuhao.setTextColor(red);
            tvPinming.setTextColor(red);
            tvShuliang.setTextColor(red);
            tvHeji.setTextColor(red);

            // (2) 隐藏加减号 (使用 INVISIBLE 保持数量文字在中间不偏移)
            helper.getView(R.id.shuliang_jia).setVisibility(android.view.View.INVISIBLE);
            helper.getView(R.id.shuliang_jian).setVisibility(android.view.View.INVISIBLE);

            // (3) 隐藏折扣信息和赠送按钮
            tvZhekou.setVisibility(android.view.View.GONE);
            helper.setGone(R.id.zengsong, false); // 强制隐藏

        } else {
            // ======= 普通商品模式 =======
            int black = Color.BLACK;
            tvXuhao.setTextColor(Color.parseColor("#333333"));
            tvPinming.setTextColor(black);
            tvShuliang.setTextColor(black);
            tvHeji.setTextColor(black);

            // (1) 显示加减号
            helper.getView(R.id.shuliang_jia).setVisibility(android.view.View.VISIBLE);
            helper.getView(R.id.shuliang_jian).setVisibility(android.view.View.VISIBLE);

            // (2) 恢复你原本的“赠送按钮”显示逻辑
            helper.setGone(R.id.zengsong, UserUtils.getInstance().isDazhe());

            // (3) 恢复你原本的“折扣文字”显示逻辑
            if (TextUtils.isEmpty(item.getDiscount()) || item.getDiscount().equals("100")) {
                tvZhekou.setVisibility(android.view.View.GONE);
            } else {
                tvZhekou.setVisibility(android.view.View.VISIBLE);
                tvZhekou.setText(context.getString(R.string.fold) + item.getDiscount() + "%");
            }
        }

        // 4. 选中背景逻辑（保持不变）
        if (item.isSelected()) {
            helper.setBackgroundColor(R.id.all_view, Color.parseColor("#757fa4"));
        } else {
            helper.getView(R.id.all_view).setBackgroundColor(Color.TRANSPARENT);
        }

        // 5. 注册点击事件
        helper.addOnClickListener(R.id.shuliang_jia);
        helper.addOnClickListener(R.id.shuliang_jian);
        helper.addOnClickListener(R.id.zengsong);
    }
}
