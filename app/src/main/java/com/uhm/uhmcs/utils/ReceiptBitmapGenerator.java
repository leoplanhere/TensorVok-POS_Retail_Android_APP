package com.uhm.uhmcs.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.CheckoutBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ReceiptBitmapGenerator {

    /**
     * 生成结账小票的长 Bitmap
     */
    public static Bitmap generateReceiptBitmap(Context context, CheckoutBean bean, String orderSn) {
        ReceiptConfigUtils config = ReceiptConfigUtils.getInstance(context);

        // 1. 确定画布宽度 (58mm ≈ 384px, 80mm ≈ 576px)
        int paperWidthPx = (config.getPaperType() == 0) ? 384 : 576;

        // 2. 确定字体缩放倍率 (0=小0.7, 1=中1.0, 2=大1.4)
        float fontScale = 1.0f;
        switch (config.getFontSize()) {
            case 0: fontScale = 0.7f; break;
            case 1: fontScale = 1.0f; break;
            case 2: fontScale = 1.4f; break;
        }

        // 3. 加载视图
        View view = LayoutInflater.from(context).inflate(R.layout.item_receipt_template, null);

        // 绑定控件
        LinearLayout root = view.findViewById(R.id.ll_receipt_root);
        ImageView ivTopLogo = view.findViewById(R.id.iv_top_logo);
        TextView tvShopName = view.findViewById(R.id.tv_shop_name);
        TextView tvShopPhone = view.findViewById(R.id.tv_shop_phone);
        TextView tvMember = view.findViewById(R.id.tv_member_info);
        TextView tvCashier = view.findViewById(R.id.tv_cashier_info);
        TextView tvTime = view.findViewById(R.id.tv_time_info);
        LinearLayout llGoods = view.findViewById(R.id.ll_goods_container);
        TextView tvTotalLabel = view.findViewById(R.id.tv_total_amount_label);
        TextView tvTotalAmount = view.findViewById(R.id.tv_total_amount);
        TextView tvPayName = view.findViewById(R.id.tv_pay_name);
        TextView tvPayDetail = view.findViewById(R.id.tv_payment_detail);
        ImageView ivQrcode = view.findViewById(R.id.iv_qrcode);
        ImageView ivBarcode = view.findViewById(R.id.iv_barcode);
        TextView tvOrderSn = view.findViewById(R.id.tv_order_sn);
        TextView tvBottomText = view.findViewById(R.id.tv_bottom_text);
        ImageView ivBottomLogo = view.findViewById(R.id.iv_bottom_logo);

        // 【关键修复】手动创建 LayoutParams 防止 null 指针崩溃
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(paperWidthPx, ViewGroup.LayoutParams.WRAP_CONTENT);
        root.setLayoutParams(lp);

        // 递归应用字体缩放
        applyFontScale(root, fontScale);

        // 4. 根据配置填充数据与控制显隐

        // 顶部 Logo
        if (config.showTopLogo()) {
            ivTopLogo.setVisibility(View.VISIBLE);
            ivTopLogo.setImageResource(R.mipmap.pos_ui_logo_01);
        } else {
            ivTopLogo.setVisibility(View.GONE);
        }

        // 店铺名
        if (config.showShopName()) {
            tvShopName.setVisibility(View.VISIBLE);
            String shopName = "优道佳家福会员超市";
            try {
                if (UserUtils.getInstance().getShopDataBean() != null
                        && UserUtils.getInstance().getShopDataBean().getData() != null
                        && !UserUtils.getInstance().getShopDataBean().getData().isEmpty()) {
                    shopName = UserUtils.getInstance().getShopDataBean().getData().get(0).getName();
                }
            } catch (Exception e) {}
            tvShopName.setText(shopName);
        } else {
            tvShopName.setVisibility(View.GONE);
        }

        // 电话
        tvShopPhone.setVisibility(config.showPhone() ? View.VISIBLE : View.GONE);
        tvShopPhone.setText("全国客服热线: 17560635652");

        // 会员信息 (同步居中)
        if (config.showMember() && bean != null && !TextUtils.isEmpty(bean.getMember_name())) {
            tvMember.setVisibility(View.VISIBLE);
            tvMember.setGravity(Gravity.CENTER);
            tvMember.setText("会员名称: " + bean.getMember_name() + "\n会员手机: " + Utilis.maskPhone(bean.getMember_phone()));
        } else {
            tvMember.setVisibility(View.GONE);
        }

        // 收银员 & 时间 (同步居中)
        tvCashier.setVisibility(config.showCashier() ? View.VISIBLE : View.GONE);
        tvCashier.setGravity(Gravity.CENTER);
        String cashierName = "";
        try {
            if (UserUtils.getInstance().getLoginBase() != null) {
                cashierName = UserUtils.getInstance().getLoginBase().getData().getUserinfo().getNickname();
            }
        } catch (Exception e) {}
        tvCashier.setText("收银员: " + (TextUtils.isEmpty(cashierName) ? "001" : cashierName));

        tvTime.setVisibility(config.showCashier() ? View.VISIBLE : View.GONE);
        tvTime.setGravity(Gravity.CENTER);
        tvTime.setText("收银时间: " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));

        // 商品列表 (同步 14:6:6:6 比例与去零逻辑)
        try {
            llGoods.removeAllViews();
            ArrayList<CheckoutBean.GoodsJsonBean> goodsList = new Gson().fromJson(bean.getGoodsjson(),
                    new TypeToken<ArrayList<CheckoutBean.GoodsJsonBean>>(){}.getType());

            if (goodsList != null) {
                float finalSize = 26f * fontScale;
                for (CheckoutBean.GoodsJsonBean item : goodsList) {
                    LinearLayout row = new LinearLayout(context);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setPadding(0, 4, 0, 4);

                    // 数量去零逻辑
                    String qtyVal = "normal".equals(item.getOnline_type()) ? String.valueOf(item.getGoods_num()) : item.getGoods_weight();
                    try { qtyVal = new BigDecimal(qtyVal).stripTrailingZeros().toPlainString(); } catch (Exception e) {}
                    String qtyStr = "weight".equals(item.getOnline_type()) ? qtyVal + "g" : "x" + qtyVal;

                    row.addView(createPreviewText(context, item.getTitle(), finalSize, 14, Gravity.START));
                    row.addView(createPreviewText(context, qtyStr, finalSize, 6, Gravity.END));
                    row.addView(createPreviewText(context, item.getGoods_price(), finalSize, 6, Gravity.END));
                    row.addView(createPreviewText(context, item.getPay_price(), finalSize, 6, Gravity.END));

                    llGoods.addView(row);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        // 金额汇总区
        if (bean != null) {
            tvTotalLabel.setText(bean.getTotal_amount());
            tvTotalAmount.setText(bean.getTotal_amount());

            String pType = bean.getPay_type();
            String payTitle = "微信支付:";
            if (!TextUtils.isEmpty(pType)) {
                if (pType.contains("cash")) payTitle = "现金支付:";
                else if (pType.contains("alipay")) payTitle = "支付宝支付:";
                else if (pType.contains("member") || pType.contains("wallet")) payTitle = "会员卡支付:";
            }
            tvPayName.setText(payTitle);
            tvPayDetail.setText(bean.getTotal_amount());
        }

        // 底部条码
        if (config.showBarcode() && !TextUtils.isEmpty(orderSn)) {
            ivBarcode.setVisibility(View.VISIBLE);
            tvOrderSn.setVisibility(View.VISIBLE);
            tvOrderSn.setText(orderSn);
            ivBarcode.setImageBitmap(generateBarcode(orderSn, 400, 80));
        } else {
            ivBarcode.setVisibility(View.GONE);
            tvOrderSn.setVisibility(View.GONE);
        }

        // 二维码
        ivQrcode.setVisibility(config.showQrcode() ? View.VISIBLE : View.GONE);

        // 底部文字
        tvBottomText.setVisibility(config.showBottomText() ? View.VISIBLE : View.GONE);
        tvBottomText.setGravity(Gravity.CENTER);
        tvBottomText.setText("此单据二维码为开具增值税普通发票\n的唯一凭证，请妥善保管。\n请保留此单据，作为退、换货凭证。");

        // 底部 Logo
        if (config.showBottomLogo()) {
            ivBottomLogo.setVisibility(View.VISIBLE);
            // ⭐ 修复点：必须手动设置图片，否则预览也是空的
            ivBottomLogo.setImageResource(R.mipmap.pos_ui_logo_01);
        } else {
            ivBottomLogo.setVisibility(View.GONE);
        }

        // 5. 最终测量与绘制
        view.measure(View.MeasureSpec.makeMeasureSpec(paperWidthPx, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        view.draw(canvas);

        return bitmap;
    }

    private static TextView createPreviewText(Context ctx, String text, float size, int weight, int gravity) {
        TextView tv = new TextView(ctx);
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        tv.setTextColor(Color.BLACK);
        tv.setGravity(gravity);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weight));
        return tv;
    }

    private static void applyFontScale(View view, float scale) {
        if (view instanceof TextView) {
            TextView tv = (TextView) view;
            float originalSize = tv.getTextSize();
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, originalSize * scale);
        } else if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                applyFontScale(vg.getChildAt(i), scale);
            }
        }
    }

    private static Bitmap generateBarcode(String content, int w, int h) {
        try {
            return new BarcodeEncoder().createBitmap(new com.google.zxing.MultiFormatWriter().encode(content, BarcodeFormat.CODE_128, w, h));
        } catch (Exception e) { return null; }
    }
}