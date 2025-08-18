package com.uhm.uhmcs.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.CategoryListBean;
import com.uhm.uhmcs.bean.LastOrderBean;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HistoryOrderAdapter extends BaseQuickAdapter<LastOrderBean, BaseViewHolder> {


    public HistoryOrderAdapter() {
        super(R.layout.item_history_order);
    }


    @Override
    protected void convert(BaseViewHolder helper, LastOrderBean item) {
        helper.setText(R.id.shouyinyuan_tv, item.getCash_user_sn());
        helper.setText(R.id.dingdanbianhao_tv, item.getOrder_sn());
        long time=item.getPaytime()* 1000L;
        Date date = new Date(time);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = sdf.format(date);
        helper.setText(R.id.dingdanshijian_tv, formattedDate);
        helper.setText(R.id.youhuijine_tv, item.getDiscount_fee()+"");
        helper.setText(R.id.zhifujine_tv, item.getPay_fee()+"");
        helper.setText(R.id.zonge_tv, item.getTotal_amount()+"");
        helper.setText(R.id.youhuiquan_tv, item.getCoupon_fee()+"");


        helper.addOnClickListener(R.id.daying_tv);
        helper.addOnClickListener(R.id.zhifuxinxi_btn);
        if (item.getXf_type()==2){
            helper.setText(R.id.gouwuxinxi_tv, "会员充值");
            helper.setText(R.id.huiyuanshoujihao_tv, TextUtils.isEmpty(item.getNumber())? "":item.getNumber());
        }else {
            helper.setText(R.id.huiyuanshoujihao_tv, TextUtils.isEmpty(item.getPhone())? "":item.getPhone());
            helper.setText(R.id.gouwuxinxi_tv, "查看");
            helper.addOnClickListener(R.id.gouwuxinxi_tv);
        }
//        if (item.getRefund_type()==2){
//            helper.setText(R.id.zhifuxinxi_btn, "已退款");
//        } else {
//
//            helper.setText(R.id.zhifuxinxi_btn, "退款");
//        }
    }
}
