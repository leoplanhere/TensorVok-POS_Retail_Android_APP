package com.uhm.uhmcs.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.LastOrderBean;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HistoryOrderAdapter extends BaseQuickAdapter<LastOrderBean, BaseViewHolder> {

    public HistoryOrderAdapter() {
        super(R.layout.item_history_order);
    }

    @Override
    protected void convert(BaseViewHolder helper, LastOrderBean item) {
        // 1. 基础信息绑定
        helper.setText(R.id.shouyinyuan_tv, item.getCash_user_sn());
        helper.setText(R.id.dingdanbianhao_tv, item.getOrder_sn());

        // 2. 时间转换
        long time = item.getPaytime() * 1000L;
        Date date = new Date(time);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        helper.setText(R.id.dingdanshijian_tv, sdf.format(date));

        // 3. 金额信息
        helper.setText(R.id.youhuijine_tv, item.getDiscount_fee() + "");
        helper.setText(R.id.zhifujine_tv, item.getPay_fee() + "");
        helper.setText(R.id.zonge_tv, item.getTotal_amount() + "");
        helper.setText(R.id.youhuiquan_tv, item.getCoupon_fee() + "");

        // 4. 注册点击事件
        helper.addOnClickListener(R.id.daying_tv);
        helper.addOnClickListener(R.id.zhifuxinxi_btn);

        // 5. 业务类型判断（充值 vs 普通订单）
        if (item.getXf_type() == 2) {
            helper.setText(R.id.gouwuxinxi_tv, "会员充值");
            helper.setText(R.id.huiyuanshoujihao_tv, TextUtils.isEmpty(item.getNumber()) ? "" : item.getNumber());
        } else {
            helper.setText(R.id.huiyuanshoujihao_tv, TextUtils.isEmpty(item.getPhone()) ? "" : item.getPhone());
            helper.setText(R.id.gouwuxinxi_tv, "查看");
            helper.addOnClickListener(R.id.gouwuxinxi_tv);
        }

        // ========================== 核心修改：处理退款状态 ==========================
        if (item.getRefund_type() == 2) {
            // 退款成功状态：显示红色“已退款”
            helper.setText(R.id.zhifuxinxi_btn, "已退款");
            helper.setTextColor(R.id.zhifuxinxi_btn, Color.RED);
        } else {
            // 正常状态：显示“支付信息”并恢复默认颜色（通常是黑色或你在XML定义的颜色）
            helper.setText(R.id.zhifuxinxi_btn, "支付信息");
            helper.setTextColor(R.id.zhifuxinxi_btn, Color.BLACK);
        }
        // =========================================================================
    }
}