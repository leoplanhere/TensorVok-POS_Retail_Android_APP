package com.uhm.uhmcs.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.GrouponGoodsBean;
import com.uhm.uhmcs.bean.LastOrderBean;

import java.math.BigDecimal;

public class OrderShopAdapter extends BaseQuickAdapter<LastOrderBean.GoodsJsonBean, BaseViewHolder> {

    public OrderShopAdapter() {
        super(R.layout.item_order_shop);
    }


    @Override
    protected void convert(BaseViewHolder helper, LastOrderBean.GoodsJsonBean item) {
        helper.setText(R.id.shop_name,item.getTitle());
        helper.setText(R.id.shop_pice,item.getGoods_price());
        helper.setText(R.id.shop_num,item.getGoods_num()+"");
        helper.setText(R.id.shop_discounted_price,item.getDiscounted_price());
        helper.setText(R.id.shop_pay_price,item.getPay_price());
        helper.setText(R.id.shop_all_price,new BigDecimal(item.getGoods_price()).multiply(new BigDecimal(item.getGoods_num()+"")).toString());


    }
}
