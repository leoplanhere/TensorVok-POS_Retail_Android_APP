package com.uhm.uhmcs.adapter;

import android.graphics.Color;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.GrouponGoodsBean;
import com.uhm.uhmcs.bean.RegistrationShopBean;

public class GuaDanShopAdapter extends BaseQuickAdapter<GrouponGoodsBean.GrouponGoodsModel, BaseViewHolder> {

    public GuaDanShopAdapter() {
        super(R.layout.item_guadan_shop);

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
        helper.setText(R.id.shop_name,(item.getTitle()));
        helper.setText(R.id.shop_num,item.getShuliang()+"");
        helper.setText(R.id.shop_pice,item.getPrice());
        helper.setText(R.id.shop_heji,item.getHeji().toString());



    }
}
