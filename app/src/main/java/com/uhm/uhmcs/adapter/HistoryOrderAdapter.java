package com.uhm.uhmcs.adapter;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.LastOrderBean;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HistoryOrderAdapter extends BaseQuickAdapter<LastOrderBean, BaseViewHolder> {

    // ★★★ 修改点：格式从 "yyyy-MM-dd" 改为 "yyyy-MM-dd HH:mm:ss" 以显示时分秒 ★★★
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public HistoryOrderAdapter() {
        super(R.layout.item_history_order);
    }

    @Override
    protected void convert(BaseViewHolder helper, LastOrderBean item) {
        // 1. 绑定基础文本
        helper.setText(R.id.shouyinyuan_tv, item.getCash_user_sn());
        helper.setText(R.id.dingdanbianhao_tv, item.getOrder_sn());

        // 2. 日期格式化
        try {
            // 注意：服务端返回的 paytime 通常是秒级时间戳，需要乘 1000 转毫秒
            long time = item.getPaytime() * 1000L;
            helper.setText(R.id.dingdanshijian_tv, SDF.format(new Date(time)));
        } catch (Exception e) {
            helper.setText(R.id.dingdanshijian_tv, "");
        }

        helper.setText(R.id.youhuijine_tv, String.valueOf(item.getDiscount_fee()));
        helper.setText(R.id.zonge_tv, String.valueOf(item.getTotal_amount()));

        // 3. 处理退款状态逻辑
        boolean isRefunded = item.isRefundedCache;

        if (!isRefunded) {
            if (item.getOrder_status() == 4) {
                isRefunded = true;
            } else if (item.getPaymentlog() != null) {
                for (LastOrderBean.PaymentlogBean log : item.getPaymentlog()) {
                    if (log.getOrder_status() == 4) {
                        isRefunded = true;
                        break;
                    }
                }
            }
        }

        // 渲染支付金额
        if (isRefunded) {
            String priceText = String.valueOf(item.getTotal_fee());
            String tagText = " 【已退款】";
            SpannableString spannable = new SpannableString(priceText + tagText);

            spannable.setSpan(new ForegroundColorSpan(Color.RED),
                    priceText.length(), spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                    priceText.length(), spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            helper.setText(R.id.zhifujine_tv, spannable);
        } else {
            helper.setText(R.id.zhifujine_tv, String.valueOf(item.getTotal_fee()));
        }

        // 4. 绑定点击事件
        helper.addOnClickListener(R.id.zhifuxinxi_btn);
        helper.addOnClickListener(R.id.gouwuxinxi_tv);
        helper.addOnClickListener(R.id.daying_tv);
    }
}