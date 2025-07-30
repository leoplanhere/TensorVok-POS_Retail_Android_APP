package com.uhm.uhmcs.adapter;

import android.graphics.Color;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.CategoryListBean;
import com.uhm.uhmcs.bean.GrouponGoodsBean;

public class ShopAdapter extends BaseQuickAdapter<GrouponGoodsBean.GrouponGoodsModel, BaseViewHolder> {

    public ShopAdapter() {
        super(R.layout.item_shop);
    }
    public boolean hasMore = true; // 标记是否还有更多数据可以加载

    @Override
    protected void convert(BaseViewHolder helper, GrouponGoodsBean.GrouponGoodsModel item) {
        helper.setText(R.id.shop_name,item.getTitle());
        helper.setText(R.id.shop_name1,item.getSubtitle());
        helper.setText(R.id.xuhao_tv,(helper.getLayoutPosition()+1)+"");
        helper.setText(R.id.bianma_tv,item.getSn());
        helper.setText(R.id.fubiaoti_tv,item.getSubtitle());
        helper.setText(R.id.shoujia_tv,item.getPrice());
        helper.setText(R.id.yuanjia_tv,item.getOriginal_price());
        helper.setText(R.id.chengbenjia_tv,item.getCost_price());
        if (item.isSelected()){
            helper.setBackgroundRes(R.id.is_selected,R.mipmap.checkbox_2);
        }else {
            helper.setBackgroundRes(R.id.is_selected,R.mipmap.checkbox_1);
        }
    }
}
