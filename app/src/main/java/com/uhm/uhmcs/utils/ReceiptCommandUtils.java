package com.uhm.uhmcs.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.CheckoutBean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 小票打印指令工具类
 * 修复重点：彻底解耦支付明细判断，确保组合支付在下单和补打时均能正确显示
 */
public class ReceiptCommandUtils {

    private static final byte[] RESET = {0x1B, 0x40};
    private static final byte[] ALIGN_CENTER = {0x1B, 0x61, 0x01};
    private static final byte[] ALIGN_LEFT = {0x1B, 0x61, 0x00};
    private static final byte[] BOLD_ON = {0x1B, 0x45, 0x01};
    private static final byte[] BOLD_OFF = {0x1B, 0x45, 0x00};
    private static final byte[] CUT_PAPER = {0x1D, 0x56, 0x42, 0x00};

    public static byte[] getReceiptCommands(Context context, CheckoutBean bean, String orderSn,
                                            String cash, String wechat, String alipay, String wallet, String nets) {
        ReceiptConfigUtils config = ReceiptConfigUtils.getInstance(context);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final int TOTAL_WIDTH = (config.getPaperType() == 0) ? 32 : 48;

        try {
            buffer.write(RESET);

            /* ========== 1. 顶部 Logo ========== */
            if (config.showTopLogo()) {
                buffer.write(ALIGN_CENTER);
                try {
                    int targetWidth = (config.getPaperType() == 0) ? 200 : 320;
                    Bitmap logo = Glide.with(context).asBitmap().load(R.mipmap.pos_ui_logo_01).submit().get();
                    Bitmap bmp = processBitmapForPrinter(logo, targetWidth);
                    if (bmp != null) buffer.write(ImagePrinter.convertBitmapToEscPos(ImagePrinter.toMonochrome(bmp)));
                } catch (Exception ignored) {}
            }

            /* ========== 2. 店铺信息 ========== */
            buffer.write(ALIGN_CENTER);
            buffer.write(BOLD_ON);
            printText(buffer, getShopName() + "\n");
            buffer.write(BOLD_OFF);
            if (config.showPhone()) printText(buffer, "全国客服热线:17560635652\n");

            /* ========== 3. 基础信息 ========== */
            buffer.write(ALIGN_LEFT);
            if (config.showMember() && bean != null && !TextUtils.isEmpty(bean.getMember_name())) {
                printText(buffer, "会员名称:" + bean.getMember_name() + "\n");
                printText(buffer, "会员手机:" + maskPhone(bean.getMember_phone()) + "\n");
            }
            if (config.showCashier()) {
                String cashier = getCashierName(bean);
                String time = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
                printText(buffer, "收银员:" + cashier + "\n");
                printText(buffer, "收银时间:" + time + "\n");
            }
            printSeparator(buffer, TOTAL_WIDTH);

            /* ========== 4. 商品明细 ========== */
            buffer.write(BOLD_ON);
            printRowStrict(buffer, "商品名称", "数量", "单价", "金额", TOTAL_WIDTH);
            buffer.write(BOLD_OFF);
            printSeparator(buffer, TOTAL_WIDTH);

            if (bean != null && !TextUtils.isEmpty(bean.getGoodsjson())) {
                ArrayList<CheckoutBean.GoodsJsonBean> goodsList = new Gson().fromJson(bean.getGoodsjson(), new TypeToken<ArrayList<CheckoutBean.GoodsJsonBean>>(){}.getType());
                if (goodsList != null) {
                    for (CheckoutBean.GoodsJsonBean item : goodsList) {
                        String qty = "weight".equals(item.getOnline_type()) ? formatQty(item.getGoods_weight()) + "g" : "x" + item.getGoods_num();
                        printRowStrict(buffer, item.getTitle(), qty, item.getGoods_price(), item.getPay_price(), TOTAL_WIDTH);
                    }
                }
            }
            printSeparator(buffer, TOTAL_WIDTH);

            /* ========== 5. 金额汇总 ========== */
            if (bean != null) {
                BigDecimal totalAmount = new BigDecimal(TextUtils.isEmpty(bean.getTotal_amount()) ? "0.00" : bean.getTotal_amount());
                BigDecimal couponFee = new BigDecimal(TextUtils.isEmpty(bean.getCoupon_fee()) ? "0.00" : bean.getCoupon_fee());
                BigDecimal actualPaid = totalAmount.subtract(couponFee);
                if (actualPaid.compareTo(BigDecimal.ZERO) < 0) actualPaid = BigDecimal.ZERO;

                printTwoColumnRow(buffer, "购买数量:", bean.getAllNum() + "件", TOTAL_WIDTH);
                printTwoColumnRow(buffer, "应付总计:", "￥" + totalAmount.setScale(2, BigDecimal.ROUND_HALF_UP).toString(), TOTAL_WIDTH);

                if (isGreaterZero(bean.getDiscount_fee())) printTwoColumnRow(buffer, "优惠总计:", "-￥" + bean.getDiscount_fee(), TOTAL_WIDTH);
                if (couponFee.compareTo(BigDecimal.ZERO) > 0) printTwoColumnRow(buffer, "代金券:", "-￥" + couponFee.setScale(2, BigDecimal.ROUND_HALF_UP).toString(), TOTAL_WIDTH);

                buffer.write(BOLD_ON);
                printTwoColumnRow(buffer, "实付金额:", "￥" + actualPaid.setScale(2, BigDecimal.ROUND_HALF_UP).toString(), TOTAL_WIDTH);
                buffer.write(BOLD_OFF);

                /* ========== ⭐ 支付详情 (关键修复点：平铺式判断) ========== */
                printText(buffer, "支付详情:\n");

                // 每一项都是独立的，只要大于0就印出来，不再使用 if-else 互斥
                if (isGreaterZero(cash))    printTwoColumnRow(buffer, "  - 现金支付:", "￥" + cash.trim(), TOTAL_WIDTH);
                if (isGreaterZero(wechat))  printTwoColumnRow(buffer, "  - 微信支付:", "￥" + wechat.trim(), TOTAL_WIDTH);
                if (isGreaterZero(alipay))  printTwoColumnRow(buffer, "  - 支付宝支付:", "￥" + alipay.trim(), TOTAL_WIDTH);
                if (isGreaterZero(wallet))  printTwoColumnRow(buffer, "  - 会员卡余额:", "￥" + wallet.trim(), TOTAL_WIDTH);
                if (isGreaterZero(nets))    printTwoColumnRow(buffer, "  - NETS支付:", "￥" + nets.trim(), TOTAL_WIDTH);

                if (isGreaterZero(bean.getCash_change())) printTwoColumnRow(buffer, "找零:", "￥" + bean.getCash_change(), TOTAL_WIDTH);
            }

            /* ========== 6. 条码 & 二维码 ========== */
            if (config.showBarcode() && !TextUtils.isEmpty(orderSn) && orderSn.length() > 5 && !orderSn.equals("10000")) {
                buffer.write(new byte[]{0x0A, 0x0A});
                buffer.write(ALIGN_CENTER);
                printBarcode(buffer, orderSn);
                buffer.write(new byte[]{0x0A});
            }

            if (config.showQrcode()) {
                buffer.write(ALIGN_CENTER);
                printQRCode(buffer, "https://posvox.com");
                buffer.write(new byte[]{0x0A});
            }

            /* ========== 7. 底部文案 ========== */
            buffer.write(ALIGN_CENTER);
            printText(buffer, "此单据二维码为开具增值税普通发票\n");
            printText(buffer, "的唯一凭证，请妥善保管。\n");
            printText(buffer, "请保留此单据，作为退、换货凭证。");
            if (config.showBottomText()) printText(buffer, "\n\n谢谢惠顾，欢迎下次光临！");

            /* ========== 8. 走纸 & 切纸 ========== */
            buffer.write(new byte[]{0x1B, 0x64, 0x05});
            buffer.write(CUT_PAPER);

        } catch (Exception e) {
            try { buffer.write(CUT_PAPER); } catch (IOException ignored) {}
        }
        return buffer.toByteArray();
    }

    // ---------------- 助手方法 (带健壮性解析) ----------------
    private static boolean isGreaterZero(String val) {
        if (TextUtils.isEmpty(val)) return false;
        try {
            // 过滤掉可能存在的 COMBINED 等干扰字符，确保纯数字判断
            String cleanVal = val.replace("COMBINED:", "").trim();
            return new BigDecimal(cleanVal).compareTo(BigDecimal.ZERO) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static String maskPhone(String phone) {
        if (TextUtils.isEmpty(phone) || phone.length() < 11) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    private static String formatQty(String value) {
        if (TextUtils.isEmpty(value)) return "0";
        try { return new BigDecimal(value).stripTrailingZeros().toPlainString(); } catch (Exception e) { return value; }
    }

    private static void printRowStrict(ByteArrayOutputStream buffer, String name, String qty, String price, String total, int totalWidth) throws IOException {
        final int NAME_W = (totalWidth == 32) ? 14 : 24;
        final int COL_W = (totalWidth == 32) ? 6 : 8;
        List<String> nameLines = splitByGbkUnits(name, NAME_W);
        for (int i = 0; i < nameLines.size(); i++) {
            StringBuilder line = new StringBuilder(padRight(nameLines.get(i), NAME_W));
            if (i == nameLines.size() - 1) line.append(padLeft(qty, COL_W)).append(padLeft(price, COL_W)).append(padLeft(total, COL_W));
            line.append("\n");
            printText(buffer, line.toString());
        }
    }

    private static String padRight(String s, int w) {
        int len = getGbkWidth(s);
        StringBuilder sb = new StringBuilder(s);
        while (len < w) { sb.append(" "); len++; }
        return sb.toString();
    }

    private static String padLeft(String s, int w) {
        int len = getGbkWidth(s);
        StringBuilder sb = new StringBuilder();
        while (len < w) { sb.append(" "); len++; }
        sb.append(s);
        return sb.toString();
    }

    private static List<String> splitByGbkUnits(String s, int max) {
        List<String> list = new ArrayList<>();
        int len = 0, start = 0;
        for (int i = 0; i < s.length(); i++) {
            int u = (s.charAt(i) < 128) ? 1 : 2;
            if (len + u > max) { list.add(s.substring(start, i)); start = i; len = u; } else len += u;
        }
        list.add(s.substring(start));
        return list;
    }

    private static int getGbkWidth(String s) {
        int w = 0; if (s == null) return 0;
        for (char c : s.toCharArray()) w += (c < 128) ? 1 : 2;
        return w;
    }

    private static void printTwoColumnRow(ByteArrayOutputStream buffer, String l, String r, int w) throws IOException {
        int lw = getGbkWidth(l), rw = getGbkWidth(r);
        if (lw + rw <= w) printText(buffer, l + padLeft(r, w - lw) + "\n");
        else printText(buffer, l + "\n" + padLeft(r, w) + "\n");
    }

    private static void printSeparator(ByteArrayOutputStream buffer, int w) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < w; i++) sb.append("-");
        printText(buffer, sb.toString() + "\n");
    }

    private static void printText(ByteArrayOutputStream buffer, String s) throws IOException {
        if (s != null) buffer.write(s.getBytes("GBK"));
    }

    private static void printBarcode(ByteArrayOutputStream buffer, String content) throws IOException {
        buffer.write(new byte[]{0x1D, 0x68, (byte) 65});
        buffer.write(new byte[]{0x1D, 0x77, (byte) 1});
        buffer.write(new byte[]{0x1D, 0x48, (byte) 0x02});
        buffer.write(new byte[]{0x1D, 0x6B, 73, (byte) content.length()});
        buffer.write(content.getBytes());
        buffer.write(0x0A);
    }

    private static void printQRCode(ByteArrayOutputStream buffer, String content) throws IOException {
        byte[] bytes = content.getBytes();
        int length = bytes.length + 3;
        buffer.write(new byte[]{0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, 0x08});
        buffer.write(new byte[]{0x1D, 0x28, 0x6B, (byte) (length % 256), (byte) (length / 256), 0x31, 0x50, 0x30});
        buffer.write(bytes);
        buffer.write(new byte[]{0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30});
    }

    private static Bitmap processBitmapForPrinter(Bitmap src, int w) {
        if (src == null) return null;
        float r = (float) src.getHeight() / src.getWidth();
        int h = (int) (w * r);
        w = (w / 8) * 8;
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        c.drawColor(Color.WHITE);
        c.drawBitmap(src, null, new android.graphics.Rect(0, 0, w, h), new Paint());
        return bmp;
    }

    private static String getShopName() {
        try { return UserUtils.getInstance().getShopDataBean().getData().get(0).getName(); } catch (Exception e) { return "收银门店"; }
    }

    private static String getCashierName(CheckoutBean bean) {
        if (bean != null && !TextUtils.isEmpty(bean.getMachineNumber())) return bean.getMachineNumber();
        try { return UserUtils.getInstance().getLoginBase().getData().getUserinfo().getNickname(); } catch (Exception e) { return "管理员"; }
    }
}