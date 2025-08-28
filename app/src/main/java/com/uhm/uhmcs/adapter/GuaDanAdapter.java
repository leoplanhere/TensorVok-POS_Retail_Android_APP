package com.uhm.uhmcs.adapter;

import android.content.Context;
import android.graphics.Color;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.GrouponGoodsBean;
import com.uhm.uhmcs.bean.RegistrationShopBean;

public class GuaDanAdapter extends BaseQuickAdapter<RegistrationShopBean, BaseViewHolder> {

    public GuaDanAdapter() {
        super(R.layout.item_guadan);

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
    protected void convert(BaseViewHolder helper, RegistrationShopBean item) {
//        helper.setText(R.id.shop_type,item.getName());
//        if (item.isSelected()){
//            helper.setBackgroundRes(R.id.item_shop_type_view,R.drawable.shop_bg);
//            helper.setTextColor(R.id.shop_type,context.getResources().getColor(R.color.white));
//        }else {
//            helper.setTextColor(R.id.shop_type,context.getResources().getColor(R.color.black));
//            helper.setBackgroundRes(R.id.item_shop_type_view,R.drawable.menu_bg);
//        }
        helper.setText(R.id.xuhao,(helper.getLayoutPosition()+1)+"");
        helper.setText(R.id.guadan_time,item.getTime());
        helper.setText(R.id.guadan_jine,item.getTotal_price().toString());

        if (helper.getLayoutPosition()==index){
            helper.setBackgroundRes(R.id.all_view,R.drawable.blue_line2);
            helper.setGone(R.id.line_view,false);
        }else {
            helper.getView(R.id.all_view).setBackgroundColor(Color.TRANSPARENT);
            helper.setVisible(R.id.line_view,true);
        }


    }
}
