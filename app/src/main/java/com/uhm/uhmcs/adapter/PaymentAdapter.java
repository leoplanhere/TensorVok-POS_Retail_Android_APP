package com.uhm.uhmcs.adapter;

import android.content.Context;
import android.icu.math.BigDecimal;
import android.text.TextUtils;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.LastOrderBean;

public class PaymentAdapter extends BaseQuickAdapter<LastOrderBean.PaymentlogBean, BaseViewHolder> {
    Context context;

    public PaymentAdapter(Context context) {
        super(R.layout.item_payment);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, LastOrderBean.PaymentlogBean item) {
        String payType = (item.getPay_type() != null) ? item.getPay_type().toLowerCase() : "";
        String lexing = "未知支付";

        // 1. ⭐ 映射支付类型名称（增加 NETS 识别）
        if (payType.equals("cash")) {
            lexing = context.getString(R.string.cash);
        } else if (payType.equals("alipay")) {
            lexing = context.getString(R.string.alipay);
        } else if (payType.equals("wechat")) {
            lexing = context.getString(R.string.wechat_pay);
        } else if (payType.equals("wallet")) {
            lexing = "会员卡支付";
        } else if (payType.contains("nets")) {
            // ⭐ 识别 NETS 支付
            lexing = "NETS支付";
        }

        // 2. 设置基础文字信息
        helper.setText(R.id.zhifuleixing_tv, lexing);
        helper.setText(R.id.liushuihao_tv, TextUtils.isEmpty(item.getTransaction_id()) ? "" : item.getTransaction_id());

        // 3. ⭐ 处理退款按钮逻辑（NETS 不允许退款）
        if (payType.contains("nets")) {
            // 如果是 NETS，直接隐藏退款按钮
            helper.setGone(R.id.tuikuan_btn, true);
        } else {
            // 其他支付方式正常处理
            helper.setVisible(R.id.tuikuan_btn, true);
            if (item.getOrder_status() == 4) {
                // 已退款状态
                helper.setText(R.id.tuikuan_btn, context.getString(R.string.Refunded));
                // 移除点击监听，防止重复触发
                helper.getView(R.id.tuikuan_btn).setOnClickListener(null);
            } else {
                // 正常可退款状态
                helper.addOnClickListener(R.id.tuikuan_btn);
                helper.setText(R.id.tuikuan_btn, context.getString(R.string.refund));
            }
        }

        // 4. 处理金额计算与显示
        if (!TextUtils.isEmpty(item.getChangemoney())) {
            try {
                // 实收 = 收到的钱 - 找零
                BigDecimal received = new BigDecimal(item.getReceivedmoney());
                BigDecimal change = new BigDecimal(item.getChangemoney());
                helper.setText(R.id.jiage_tv, received.subtract(change).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            } catch (Exception e) {
                helper.setText(R.id.jiage_tv, item.getReceivedmoney());
            }
        } else {
            helper.setText(R.id.jiage_tv, item.getReceivedmoney());
        }
    }
}