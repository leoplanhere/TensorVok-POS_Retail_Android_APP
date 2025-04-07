package com.uhm.uhmcs.adapter;

import android.content.Context;
import android.graphics.Color;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.CategoryListBean;

public class ShopTypeAdapter1 extends BaseQuickAdapter<CategoryListBean.CategoryListModel, BaseViewHolder> {

    public ShopTypeAdapter1() {
        super(R.layout.item_shop_type1);
    }
    private int index=0;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
        notifyDataSetChanged();
    }

    @Override
    protected void convert(BaseViewHolder helper, CategoryListBean.CategoryListModel item) {
        helper.setText(R.id.shop_type,item.getName());
        if (helper.getLayoutPosition()==index){
            helper.setBackgroundColor(R.id.item_shop_type_view, Color.parseColor("#65755a"));
        }else {
            helper.getView(R.id.item_shop_type_view).setBackgroundColor(Color.TRANSPARENT);
        }
    }
}
