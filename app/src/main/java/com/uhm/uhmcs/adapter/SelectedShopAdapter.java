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
import com.uhm.uhmcs.utils.CurrencyUtils; // 引入货币工具类
import com.uhm.uhmcs.utils.UserUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SelectedShopAdapter extends BaseQuickAdapter<GrouponGoodsBean.GrouponGoodsModel, BaseViewHolder> {
    private Context context;

    public SelectedShopAdapter(Context context, int layoutResId) {
        super(layoutResId);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, GrouponGoodsBean.GrouponGoodsModel item) {
        // 控制赠送按钮的显隐（打折权限控制）
        helper.setGone(R.id.zengsong, UserUtils.getInstance().isDazhe());
        helper.setGone(R.id.zengsong_view, UserUtils.getInstance().isDazhe());

        // 设置序号和品名
        helper.setText(R.id.xuhao, (helper.getLayoutPosition() + 1) + "");
        helper.setText(R.id.pinming, item.getTitle());

        // ================= 货币符号动态化修改点 =================
        // 使用 CurrencyUtils.format 自动根据当前设置添加货币符号 (如: ￥10.00, $10.00, ฿10.00)
        helper.setText(R.id.heji, CurrencyUtils.format(item.getHeji()));
        // ======================================================

        // 处理赠送/取消赠送状态
        if (item.isIs_zengsong()) {
            helper.setTextColor(R.id.zengsong, Color.parseColor("#FFEF4444"));
            helper.setBackgroundRes(R.id.zengsong, R.drawable.red_line1);
        } else {
            helper.setTextColor(R.id.zengsong, Color.parseColor("#FF3B82F6"));
            helper.setBackgroundRes(R.id.zengsong, R.drawable.blue_line1);
        }

        helper.setText(R.id.zengsong, item.isIs_zengsong() ?
                context.getString(R.string.cancel_give_away) :
                context.getString(R.string.give_away));

        // 商品计量逻辑：计件 or 称重
        if (!"weight".equals(item.getOnline_type())) {
            helper.addOnClickListener(R.id.shuliang_jia);
            helper.addOnClickListener(R.id.shuliang_jian);
            helper.setGone(R.id.shuliang_jia, true);
            helper.setGone(R.id.shuliang_jian, true);
            helper.setText(R.id.shuliang, String.valueOf(item.getShuliang()));
        } else {
            // 称重商品：显示克重，隐藏加减按钮
            helper.setText(R.id.shuliang, item.getGoods_weight() + "g");
            helper.setGone(R.id.shuliang_jia, false);
            helper.setGone(R.id.shuliang_jian, false);
        }

        helper.addOnClickListener(R.id.zengsong);

        // 显示折扣信息
        TextView zhekou_view = helper.getView(R.id.zhekou_view);
        zhekou_view.setText(context.getString(R.string.fold) + item.getDiscount() + "%");

        // 设置选中项的高亮背景
        if (item.isSelected()) {
            helper.setBackgroundRes(R.id.all_view, R.drawable.blue_line2);
        } else {
            helper.getView(R.id.all_view).setBackgroundColor(Color.TRANSPARENT);
        }
    }
}