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
//        helper.setText(R.id.shop_type,item.getName());
//        if (item.isSelected()){
//            helper.setBackgroundRes(R.id.item_shop_type_view,R.drawable.shop_bg);
//            helper.setTextColor(R.id.shop_type,context.getResources().getColor(R.color.white));
//        }else {
//            helper.setTextColor(R.id.shop_type,context.getResources().getColor(R.color.black));
//            helper.setBackgroundRes(R.id.item_shop_type_view,R.drawable.menu_bg);
//        }
        helper.setGone(R.id.zengsong, UserUtils.getInstance().isDazhe());
        helper.setText(R.id.xuhao,(helper.getLayoutPosition()+1)+"");
        helper.setText(R.id.pinming,item.getTitle());
        helper.setText(R.id.guige,"暂无规格");

        helper.setText(R.id.heji,item.getHeji()+"");
        helper.setText(R.id.shuliang,item.getShuliang()+"");
        helper.setText(R.id.zengsong,item.isIs_zengsong()?context.getString(R.string.cancel_give_away):context.getString(R.string.give_away));

        helper.addOnClickListener(R.id.shuliang_jia);
        helper.addOnClickListener(R.id.shuliang_jian);
        helper.addOnClickListener(R.id.zengsong);

        TextView zhekou_view=helper.getView(R.id.zhekou_view);
        if (TextUtils.isEmpty(item.getDiscount())||item.getDiscount().equals("100")){
            zhekou_view.setVisibility(GONE);
        }else {
            zhekou_view.setVisibility(VISIBLE);
            zhekou_view.setText(context.getString(R.string.fold)+item.getDiscount()+"%");
        }

        if (item.isSelected()){

            helper.setBackgroundColor(R.id.all_view,Color.parseColor("#757fa4"));
        }else {
            helper.getView(R.id.all_view).setBackgroundColor(Color.TRANSPARENT);
        }


    }
}
