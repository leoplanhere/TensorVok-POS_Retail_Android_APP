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
 * ESC/POS 小票打印工具（商用稳定全功能版）
 * 优化：
 * 1. 修正支付详情逻辑：现金/微信/支付宝金额 = 实付金额 - 代金券。
 * 2. 修复条形码与下方数字粘连问题。
 * 3. 优化底部节纸逻辑。
 */
public class ReceiptCommandUtils {

    private static final byte[] RESET = {0x1B, 0x40};
    private static final byte[] ALIGN_CENTER = {0x1B, 0x61, 0x01};
    private static final byte[] ALIGN_LEFT = {0x1B, 0x61, 0x00};
    private static final byte[] BOLD_ON = {0x1B, 0x45, 0x01};
    private static final byte[] BOLD_OFF = {0x1B, 0x45, 0x00};
    private static final byte[] CUT_PAPER = {0x1D, 0x56, 0x42, 0x00};

    public static byte[] getReceiptCommands(Context context, CheckoutBean bean, String orderSn) {
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
                    Bitmap logo = Glide.with(context).asBitmap()
                            .load(R.mipmap.pos_ui_logo_01).submit().get();
                    Bitmap bmp = processBitmapForPrinter(logo, targetWidth);
                    if (bmp != null) {
                        buffer.write(ImagePrinter.convertBitmapToEscPos(ImagePrinter.toMonochrome(bmp)));
                    }
                } catch (Exception ignored) {}
            }

            /* ========== 2. 店铺信息 & 热线 ========== */
            buffer.write(ALIGN_CENTER);
            buffer.write(BOLD_ON);
            printText(buffer, getShopName() + "\n");
            buffer.write(BOLD_OFF);

            if (config.showPhone()) {
                printText(buffer, "全国客服热线:17560635652\n");
            }

            /* ========== 3. 会员 / 收银员信息 ========== */
            applyUserFontSize(buffer, config.getFontSize());

            if (config.showMember() && bean != null && !TextUtils.isEmpty(bean.getMember_name())) {
                printText(buffer, "会员名称:" + bean.getMember_name() + "\n");
                printText(buffer, "会员手机:" + Utilis.maskPhone(bean.getMember_phone()) + "\n");
            }

            if (config.showCashier()) {
                String cashier = getCashierName(bean);
                String time = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
                printText(buffer, "收银员:" + cashier + "\n");
                printText(buffer, "收银时间:" + time + "\n");
            }

            buffer.write(ALIGN_CENTER);
            printSeparator(buffer, TOTAL_WIDTH);

            /* ========== 4. 商品明细 ========== */
            buffer.write(ALIGN_LEFT);
            buffer.write(BOLD_ON);
            setFontSize(buffer, 0, 0);
            printRowStrict(buffer, "商品名称", "数量", "单价", "金额", TOTAL_WIDTH);
            buffer.write(BOLD_OFF);

            printSeparator(buffer, TOTAL_WIDTH);
            applyUserFontSize(buffer, config.getFontSize());

            if (bean != null) {
                ArrayList<CheckoutBean.GoodsJsonBean> goodsList =
                        new Gson().fromJson(bean.getGoodsjson(),
                                new TypeToken<ArrayList<CheckoutBean.GoodsJsonBean>>(){}.getType());

                if (goodsList != null) {
                    for (CheckoutBean.GoodsJsonBean item : goodsList) {
                        String qty = "weight".equals(item.getOnline_type())
                                ? formatQty(item.getGoods_weight()) + "g"
                                : "x" + formatQty(String.valueOf(item.getGoods_num()));

                        printRowStrict(buffer, item.getTitle(), qty, item.getGoods_price(), item.getPay_price(), TOTAL_WIDTH);
                    }
                }
            }

            printSeparator(buffer, TOTAL_WIDTH);

            /* ========== 5. 金额汇总 ========== */
            setFontSize(buffer, 0, 0);
            buffer.write(BOLD_OFF);

            if (bean != null) {
                // 原始身价
                BigDecimal totalAmount = new BigDecimal(bean.getTotal_amount());
                // 代金券抵扣额
                BigDecimal couponFee = new BigDecimal(TextUtils.isEmpty(bean.getCoupon_fee()) ? "0.00" : bean.getCoupon_fee());

                // 核心计算：最终支付软件划走的钱 = 实付金额 - 代金券
                BigDecimal payValue = totalAmount.subtract(couponFee);
                if (payValue.compareTo(BigDecimal.ZERO) < 0) payValue = BigDecimal.ZERO;

                printTwoColumnRow(buffer, "购买数量:", bean.getAllNum() + "件", TOTAL_WIDTH);
                printTwoColumnRow(buffer, "应付总计:", totalAmount.setScale(2, BigDecimal.ROUND_HALF_UP).toString(), TOTAL_WIDTH);

                if (!TextUtils.isEmpty(bean.getDiscount_fee()) && new BigDecimal(bean.getDiscount_fee()).compareTo(BigDecimal.ZERO) > 0) {
                    printTwoColumnRow(buffer, "优惠总计:", "-" + bean.getDiscount_fee(), TOTAL_WIDTH);
                }

                if (couponFee.compareTo(BigDecimal.ZERO) > 0) {
                    printTwoColumnRow(buffer, "代金券:", "-" + couponFee.setScale(2, BigDecimal.ROUND_HALF_UP).toString(), TOTAL_WIDTH);
                }

                buffer.write(BOLD_ON);
                // 这里按你说的，依然显示总的实付金额
                printTwoColumnRow(buffer, "实付金额:", totalAmount.setScale(2, BigDecimal.ROUND_HALF_UP).toString(), TOTAL_WIDTH);
                buffer.write(BOLD_OFF);

                /* ========== 6. 支付详情 (修正此处) ========== */
                String pType = bean.getPay_type();
                if (!TextUtils.isEmpty(pType)) {
                    String payName = "支付方式:";
                    if (pType.contains("cash")) payName = "现金支付:";
                    else if (pType.contains("alipay")) payName = "支付宝支付:";
                    else if (pType.contains("wechat")) payName = "微信支付:";
                    else if (pType.contains("member") || pType.contains("wallet")) payName = "会员卡支付:";

                    // 【修正点】这里显示的是 扣除代金券后的 实际付给店里的钱 (100 - 20 = 80)
                    printTwoColumnRow(buffer, payName, payValue.setScale(2, BigDecimal.ROUND_HALF_UP).toString(), TOTAL_WIDTH);
                }

                if (!TextUtils.isEmpty(bean.getCash_change())
                        && new BigDecimal(bean.getCash_change()).compareTo(BigDecimal.ZERO) > 0) {
                    printTwoColumnRow(buffer, "找零:", bean.getCash_change(), TOTAL_WIDTH);
                }
            }

            printText(buffer, "\n");

            /* ========== 7. 条码 & 二维码 ========== */
            if (config.showBarcode() && !TextUtils.isEmpty(orderSn)) {
                buffer.write(ALIGN_CENTER);
                printBarcode(buffer, orderSn);
                printText(buffer, orderSn + "\n");
            }

            if (config.showQrcode()) {
                buffer.write(ALIGN_CENTER);
                printQRCode(buffer, "https://posvox.com");
                printText(buffer, "\n");
            }

            /* ========== 8. 底部免责声明 ========== */
            buffer.write(ALIGN_CENTER);
            printText(buffer, "此单据二维码为开具增值税普通发票\n");
            printText(buffer, "的唯一凭证，请妥善保管。\n");
            printText(buffer, "请保留此单据，作为退、换货凭证。");

            if (config.showBottomText()) {
                printText(buffer, "\n\n谢谢惠顾，欢迎下次光临！");
            }

            /* ========== 9. 底部 Logo & 节纸优化 ========== */
            if (config.showBottomLogo()) {
                printText(buffer, "\n");
                try {
                    int targetWidth = (config.getPaperType() == 0) ? 200 : 320;
                    Bitmap bottomLogo = Glide.with(context).asBitmap()
                            .load(R.mipmap.pos_ui_logo_01).submit().get();
                    Bitmap bBmp = processBitmapForPrinter(bottomLogo, targetWidth);
                    if (bBmp != null) {
                        buffer.write(ImagePrinter.convertBitmapToEscPos(ImagePrinter.toMonochrome(bBmp)));
                    }
                } catch (Exception ignored) {}
                buffer.write(new byte[]{0x1B, 0x64, 0x05});
            } else {
                buffer.write(new byte[]{0x1B, 0x64, 0x03});
            }

            buffer.write(CUT_PAPER);

        } catch (Exception e) { e.printStackTrace(); }
        return buffer.toByteArray();
    }

    private static String formatQty(String value) {
        if (TextUtils.isEmpty(value)) return "0";
        try {
            return new BigDecimal(value).stripTrailingZeros().toPlainString();
        } catch (Exception e) { return value; }
    }

    private static void printRowStrict(ByteArrayOutputStream buffer, String name, String qty, String price, String total, int totalWidth) throws IOException {
        final int NAME_W = (totalWidth == 32) ? 14 : 24;
        final int QTY_W = (totalWidth == 32) ? 6 : 8;
        final int PRICE_W = (totalWidth == 32) ? 6 : 8;
        final int TOTAL_W = (totalWidth == 32) ? 6 : 8;

        List<String> nameLines = splitByGbkUnits(name, NAME_W);
        if (nameLines.isEmpty()) nameLines.add("");

        for (int i = 0; i < nameLines.size(); i++) {
            StringBuilder line = new StringBuilder();
            line.append(padRight(nameLines.get(i), NAME_W));
            if (i == nameLines.size() - 1) {
                line.append(padLeft(qty, QTY_W));
                line.append(padLeft(price, PRICE_W));
                line.append(padLeft(total, TOTAL_W));
            }
            line.append("\n");
            printText(buffer, line.toString());
        }
    }

    private static String padRight(String s, int w) {
        int len = getStringWidth(s);
        StringBuilder sb = new StringBuilder(s);
        while (len < w) { sb.append(" "); len++; }
        return sb.toString();
    }

    private static String padLeft(String s, int w) {
        int len = getStringWidth(s);
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
            if (len + u > max) {
                list.add(s.substring(start, i));
                start = i;
                len = u;
            } else {
                len += u;
            }
        }
        list.add(s.substring(start));
        return list;
    }

    private static int getStringWidth(String s) {
        int w = 0;
        if (s == null) return 0;
        for (char c : s.toCharArray()) w += (c < 128) ? 1 : 2;
        return w;
    }

    private static void printTwoColumnRow(ByteArrayOutputStream buffer, String l, String r, int w) throws IOException {
        int lw = getStringWidth(l), rw = getStringWidth(r);
        if (lw + rw <= w) {
            printText(buffer, l + padLeft(r, w - lw) + "\n");
        } else {
            printText(buffer, l + "\n");
            printText(buffer, padLeft(r, w) + "\n");
        }
    }

    private static void printSeparator(ByteArrayOutputStream buffer, int w) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < w; i++) sb.append("-");
        sb.append("\n");
        printText(buffer, sb.toString());
    }

    private static void printText(ByteArrayOutputStream buffer, String s) throws IOException {
        if (s == null) return;
        buffer.write(s.getBytes("GBK"));
    }

    private static void setFontSize(ByteArrayOutputStream buffer, int w, int h) {
        try { buffer.write(new byte[]{0x1D, 0x21, (byte) ((w << 4) | h)}); } catch (IOException ignored) {}
    }

    private static void applyUserFontSize(ByteArrayOutputStream buffer, int size) {
        setFontSize(buffer, 0, size == 2 ? 1 : 0);
    }

    private static void printBarcode(ByteArrayOutputStream buffer, String c) throws IOException {
        buffer.write(new byte[]{0x1D, 0x68, (byte) 75});
        buffer.write(new byte[]{0x1D, 0x77, (byte) 2});
        buffer.write(new byte[]{0x1D, 0x6B, 73, (byte) c.length()});
        buffer.write(c.getBytes());
        buffer.write(0x0A);
    }

    private static void printQRCode(ByteArrayOutputStream buffer, String content) throws IOException {
        byte[] bytes = content.getBytes();
        int length = bytes.length + 3;
        buffer.write(new byte[]{0x1D, 0x28, 0x6B, 0x04, 0x00, 0x31, 0x41, 0x32, 0x00});
        buffer.write(new byte[]{0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, 0x08});
        buffer.write(new byte[]{0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x45, 0x30});
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
        try { return UserUtils.getInstance().getShopDataBean().getData().get(0).getName(); }
        catch (Exception e) { return "店铺名称未设置"; }
    }

    private static String getCashierName(CheckoutBean bean) {
        if (bean != null && !TextUtils.isEmpty(bean.getMachineNumber())) return bean.getMachineNumber();
        try { return UserUtils.getInstance().getLoginBase().getData().getUserinfo().getNickname(); }
        catch (Exception e) { return "001"; }
    }
}