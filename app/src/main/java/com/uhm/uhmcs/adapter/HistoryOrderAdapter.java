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

        // ========================== 核心修改：处理支付信息按钮文字 ==========================

        if (item.getRefund_type() == 2) {
            // A. 已退款优先级最高：显示红色“已退款”
            helper.setText(R.id.zhifuxinxi_btn, "已退款");
            helper.setTextColor(R.id.zhifuxinxi_btn, Color.RED);
        } else {
            // B. 未退款状态，进一步判断是否为组合支付
            String pType = item.getPay_type();
            boolean isCombined = false;

            // 逻辑 1：通过 pay_type 字符串中的分隔符判断 (cash,alipay 或 cash alipay)
            if (!TextUtils.isEmpty(pType) && (pType.contains(",") || pType.contains(" "))) {
                isCombined = true;
            }
            // 逻辑 2：通过 paymentlog 列表长度判断
            else if (item.getPaymentlog() != null && item.getPaymentlog().size() > 1) {
                isCombined = true;
            }

            if (isCombined) {
                // 如果是组合支付
                helper.setText(R.id.zhifuxinxi_btn, "组合支付信息");
                // 建议使用一个稍微不同的颜色（如蓝色或深绿色）来提醒收银员这是组合单
                helper.setTextColor(R.id.zhifuxinxi_btn, Color.parseColor("#007AFF"));
            } else {
                // 普通单一支付
                helper.setText(R.id.zhifuxinxi_btn, "支付信息");
                helper.setTextColor(R.id.zhifuxinxi_btn, Color.BLACK);
            }
        }
        // =========================================================================
    }
}