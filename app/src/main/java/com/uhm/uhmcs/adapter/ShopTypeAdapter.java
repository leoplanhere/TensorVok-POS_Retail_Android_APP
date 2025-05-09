package com.uhm.uhmcs.adapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;


import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.CategoryListBean;

import java.util.List;

public class ShopTypeAdapter extends BaseQuickAdapter<CategoryListBean.CategoryListModel, BaseViewHolder> {
    private Context context;
    public ShopTypeAdapter(Context context, int layoutResId) {
        super(layoutResId);
        this.context=context;
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
            helper.setBackgroundRes(R.id.item_shop_type_view,R.drawable.shop_bg);
            helper.setTextColor(R.id.shop_type,context.getColor(R.color.white));
        }else {
            helper.setTextColor(R.id.shop_type,context.getColor(R.color.black));
            helper.setBackgroundRes(R.id.item_shop_type_view,R.drawable.menu_bg);
        }
    }
}
