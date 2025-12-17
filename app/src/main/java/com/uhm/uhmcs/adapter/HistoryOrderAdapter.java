package com.uhm.uhmcs.adapter;

import android.annotation.SuppressLint;
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

    // 优化：全局复用 SimpleDateFormat，避免列表滑动时频繁创建对象导致卡顿
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public HistoryOrderAdapter() {
        super(R.layout.item_history_order);
    }

    @Override
    protected void convert(BaseViewHolder helper, LastOrderBean item) {
        // 1. 绑定基础文本
        helper.setText(R.id.shouyinyuan_tv, item.getCash_user_sn());
        helper.setText(R.id.dingdanbianhao_tv, item.getOrder_sn());

        // 2. 日期格式化 (使用静态 SDF 优化性能)
        try {
            long time = item.getPaytime() * 1000L;
            helper.setText(R.id.dingdanshijian_tv, SDF.format(new Date(time)));
        } catch (Exception e) {
            helper.setText(R.id.dingdanshijian_tv, "");
        }

        helper.setText(R.id.youhuijine_tv, String.valueOf(item.getDiscount_fee()));
        helper.setText(R.id.zonge_tv, String.valueOf(item.getTotal_amount()));

        // ★★★ 3. 核心修复：显示【已退款】红色字样 ★★★

        // 逻辑说明：
        // 优先读取我们在 MainActivity 算好的缓存字段 isRefundedCache (性能最快)
        // 如果缓存没命中（比如没经过 MainActivity 预处理），再做一个兜底检查 (双重保险)
        boolean isRefunded = item.isRefundedCache;

        if (!isRefunded) {
            // 兜底检查：万一缓存没算对，再查一次状态和流水
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
            // 使用 SpannableString 实现部分文字变色，比 Html.fromHtml 性能更好
            String priceText = String.valueOf(item.getTotal_fee());
            String tagText = " 【已退款】";
            SpannableString spannable = new SpannableString(priceText + tagText);

            // 将“ 【已退款】”设置为红色
            spannable.setSpan(new ForegroundColorSpan(Color.RED),
                    priceText.length(), spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            // 将“ 【已退款】”设置为加粗
            spannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                    priceText.length(), spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            helper.setText(R.id.zhifujine_tv, spannable);
        } else {
            // 正常订单，只显示金额
            helper.setText(R.id.zhifujine_tv, String.valueOf(item.getTotal_fee()));
        }

        // 4. 绑定点击事件
        helper.addOnClickListener(R.id.zhifuxinxi_btn);
        helper.addOnClickListener(R.id.gouwuxinxi_tv);
        helper.addOnClickListener(R.id.daying_tv);
    }
}