package com.uhm.uhmcs.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.GrouponGoodsBean;

public class ShopAdapter1 extends BaseQuickAdapter<GrouponGoodsBean.GrouponGoodsModel, BaseViewHolder> {

    public ShopAdapter1() {
        super(R.layout.item_shop1);
    }


    @Override
    protected void convert(BaseViewHolder helper, GrouponGoodsBean.GrouponGoodsModel item) {
        helper.setText(R.id.shop_name,item.getTitle());
        helper.setText(R.id.shop_num,"x"+item.getShuliang());
        helper.setText(R.id.shop_all_price,"￥"+item.getHeji().toString());

    }
}
