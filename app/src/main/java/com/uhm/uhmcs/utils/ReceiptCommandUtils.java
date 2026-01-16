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
 * 小票打印指令工具类 - 极致紧凑省纸版
 */
public class ReceiptCommandUtils {

    private static final byte[] RESET = {0x1B, 0x40};
    private static final byte[] LINE_SPACING_20 = {0x1B, 0x33, 20}; // 设置紧凑行间距 (n=20)
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
        String symbol = CurrencyUtils.getPrinterSymbol();

        try {
            // 1. 初始化并立即设置紧凑行距
            buffer.write(RESET);
            buffer.write(LINE_SPACING_20); // 关键：缩减全局垂直间距

            // 2. 补打标识 (置顶紧凑显示)
            if (orderSn != null && orderSn.contains("补打")) {
                buffer.write(ALIGN_CENTER);
                buffer.write(BOLD_ON);
                printText(buffer, "-- 重打小票 --\n");
                buffer.write(BOLD_OFF);
            }

            /* ========== 3. 顶部 Logo (紧凑) ========== */
            if (config.showTopLogo()) {
                buffer.write(ALIGN_CENTER);
                try {
                    int targetWidth = (config.getPaperType() == 0) ? 200 : 320;
                    Bitmap logo = Glide.with(context).asBitmap().load(R.mipmap.plogo).submit().get();
                    Bitmap bmp = processBitmapForPrinter(logo, targetWidth);
                    if (bmp != null) buffer.write(ImagePrinter.convertBitmapToEscPos(ImagePrinter.toMonochrome(bmp)));
                } catch (Exception ignored) {}
            }

            /* ========== 4. 店铺信息 ========== */
            buffer.write(ALIGN_CENTER);
            buffer.write(BOLD_ON);
            printText(buffer, getShopName() + "\n");
            buffer.write(BOLD_OFF);

            if (config.showPhone()) {
                printText(buffer, "客服热线:" + config.getHotline() + "\n");
            }

            /* ========== 5. 基础信息 ========== */
            buffer.write(ALIGN_LEFT);
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

            setFontSize(buffer, 0, 0);
            printSeparator(buffer, TOTAL_WIDTH);

            /* ========== 6. 商品明细 ========== */
            buffer.write(BOLD_ON);
            printRowStrict(buffer, "商品名称", "数量", "单价", "金额", TOTAL_WIDTH);
            buffer.write(BOLD_OFF);
            printSeparator(buffer, TOTAL_WIDTH);

            applyUserFontSize(buffer, config.getFontSize());
            if (bean != null && !TextUtils.isEmpty(bean.getGoodsjson())) {
                ArrayList<CheckoutBean.GoodsJsonBean> goodsList = new Gson().fromJson(bean.getGoodsjson(), new TypeToken<ArrayList<CheckoutBean.GoodsJsonBean>>(){}.getType());
                if (goodsList != null) {
                    for (CheckoutBean.GoodsJsonBean item : goodsList) {
                        String qty = "weight".equals(item.getOnline_type()) ? formatQty(String.valueOf(item.getGoods_weight())) + "g" : "x" + item.getGoods_num();
                        printRowStrict(buffer, item.getTitle(), qty, item.getGoods_price(), item.getPay_price(), TOTAL_WIDTH);
                    }
                }
            }
            setFontSize(buffer, 0, 0);
            printSeparator(buffer, TOTAL_WIDTH);

            /* ========== 7. 金额汇总 ========== */
            if (bean != null) {
                BigDecimal totalAmount = new BigDecimal(TextUtils.isEmpty(bean.getTotal_amount()) ? "0.00" : bean.getTotal_amount());
                BigDecimal couponFee = new BigDecimal(TextUtils.isEmpty(bean.getCoupon_fee()) ? "0.00" : bean.getCoupon_fee());
                BigDecimal actualPaid = totalAmount.subtract(couponFee);
                if (actualPaid.compareTo(BigDecimal.ZERO) < 0) actualPaid = BigDecimal.ZERO;

                printTwoColumnRow(buffer, "购买数量:", bean.getAllNum() + "件", TOTAL_WIDTH);
                printTwoColumnRow(buffer, "应付总计:", symbol + totalAmount.setScale(2, BigDecimal.ROUND_HALF_UP).toString(), TOTAL_WIDTH);

                if (isGreaterZero(bean.getDiscount_fee())) printTwoColumnRow(buffer, "优惠总计:", "-" + symbol + bean.getDiscount_fee(), TOTAL_WIDTH);
                if (couponFee.compareTo(BigDecimal.ZERO) > 0) printTwoColumnRow(buffer, "代金券:", "-" + symbol + couponFee.setScale(2, BigDecimal.ROUND_HALF_UP).toString(), TOTAL_WIDTH);

                buffer.write(BOLD_ON);
                printTwoColumnRow(buffer, "实付金额:", symbol + actualPaid.setScale(2, BigDecimal.ROUND_HALF_UP).toString(), TOTAL_WIDTH);
                buffer.write(BOLD_OFF);

                printText(buffer, "支付详情:\n");
                if (isGreaterZero(cash))    printTwoColumnRow(buffer, "  - 现金支付:", symbol + cash.trim(), TOTAL_WIDTH);
                if (isGreaterZero(wechat))  printTwoColumnRow(buffer, "  - 微信支付:", symbol + wechat.trim(), TOTAL_WIDTH);
                if (isGreaterZero(alipay))  printTwoColumnRow(buffer, "  - 支付宝支付:", symbol + alipay.trim(), TOTAL_WIDTH);
                if (isGreaterZero(wallet))  printTwoColumnRow(buffer, "  - 会员卡余额:", symbol + wallet.trim(), TOTAL_WIDTH);
                if (isGreaterZero(nets))    printTwoColumnRow(buffer, "  - NETS支付:", symbol + nets.trim(), TOTAL_WIDTH);
                if (isGreaterZero(bean.getCash_change())) printTwoColumnRow(buffer, "找零:", symbol + bean.getCash_change(), TOTAL_WIDTH);
            }

            /* ========== 8. 条码 & 二维码 (零间距) ========== */
            buffer.write(ALIGN_CENTER);
            if (config.showBarcode() && !TextUtils.isEmpty(orderSn) && orderSn.length() > 5) {
                String cleanSn = orderSn.replaceAll("[^a-zA-Z0-9-]", "");
                if (!TextUtils.isEmpty(cleanSn)) {
                    printBarcode(buffer, cleanSn);
                    printText(buffer, cleanSn + "\n");
                }
            }

            if (config.showQrcode()) {
                printQRCode(buffer, config.getQrUrl());
            }

            /* ========== 9. 底部欢迎语 (紧凑) ========== */
            if (config.showBottomText()) {
                printText(buffer, config.getFooterNote() + "\n");
            }

            /* ========== 10. 底部 Logo & 切纸 (极致压缩间距) ========== */
            if (config.showBottomLogo()) {
                buffer.write(ALIGN_CENTER);
                try {
                    int targetWidth = (config.getPaperType() == 0) ? 200 : 320;
                    Bitmap logo = Glide.with(context).asBitmap().load(R.mipmap.plogo).submit().get();
                    Bitmap bmp = processBitmapForPrinter(logo, targetWidth);
                    if (bmp != null) buffer.write(ImagePrinter.convertBitmapToEscPos(ImagePrinter.toMonochrome(bmp)));
                } catch (Exception ignored) {}
                buffer.write(new byte[]{0x1B, 0x64, 0x02}); // 打印完 Logo 后仅走 2 行纸 (最小安全距离)
            } else {
                buffer.write(new byte[]{0x1B, 0x64, 0x01}); // 不印 Logo 仅走 1 行纸
            }

            buffer.write(CUT_PAPER);

        } catch (Exception e) {
            try { buffer.write(CUT_PAPER); } catch (IOException ignored) {}
        }
        return buffer.toByteArray();
    }

    // ---------------- 字体/换行 ----------------
    private static void setFontSize(ByteArrayOutputStream buffer, int w, int h) {
        try { buffer.write(new byte[]{0x1D, 0x21, (byte) ((w << 4) | h)}); } catch (IOException ignored) {}
    }

    private static void applyUserFontSize(ByteArrayOutputStream buffer, int size) {
        setFontSize(buffer, 0, size == 2 ? 1 : 0);
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
        buffer.write(new byte[]{0x1D, 0x68, (byte) 55}); // 高度降低到 55 让单行更紧凑
        buffer.write(new byte[]{0x1D, 0x77, (byte) 1});
        buffer.write(new byte[]{0x1D, 0x48, (byte) 0x00});
        buffer.write(new byte[]{0x1D, 0x6B, 73, (byte) content.length()});
        buffer.write(content.getBytes());
    }

    private static void printQRCode(ByteArrayOutputStream buffer, String content) throws IOException {
        byte[] bytes = content.getBytes();
        int length = bytes.length + 3;
        buffer.write(new byte[]{0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, 0x05}); // 模块大小降到 05
        buffer.write(new byte[]{0x1D, 0x28, 0x6B, (byte) (length % 256), (byte) (length / 256), 0x31, 0x50, 0x30});
        buffer.write(bytes);
        buffer.write(new byte[]{0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30});
    }

    // ---------------- 辅助处理 (略) ----------------
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

    private static boolean isGreaterZero(String val) {
        if (TextUtils.isEmpty(val)) return false;
        try { return new BigDecimal(val.replace("COMBINED:", "").trim()).compareTo(BigDecimal.ZERO) > 0; } catch (Exception e) { return false; }
    }

    private static Bitmap processBitmapForPrinter(Bitmap src, int w) {
        if (src == null) return null;
        float r = (float) src.getHeight() / src.getWidth();
        int h = (int) (w * r); w = (w / 8) * 8;
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp); c.drawColor(Color.WHITE);
        c.drawBitmap(src, null, new android.graphics.Rect(0, 0, w, h), new Paint());
        return bmp;
    }

    private static String getShopName() {
        try { return UserUtils.getInstance().getShopDataBean().getData().get(0).getName(); } catch (Exception e) { return "收银门店"; }
    }

    private static String getCashierName(CheckoutBean bean) {
        // 1. 优先读取专门的显示字段 cashierName
        if (bean != null && !TextUtils.isEmpty(bean.getCashierName())) {
            return bean.getCashierName();
        }

        // 2. 备选：如果 machineNumber 不是 001，说明可能存的是名字
        if (bean != null && !TextUtils.isEmpty(bean.getMachineNumber()) && !bean.getMachineNumber().equals("001")) {
            return bean.getMachineNumber();
        }

        // 3. 兜底：取当前登录的昵称
        try {
            String nickname = UserUtils.getInstance().getLoginBase().getData().getUserinfo().getNickname();
            if (!TextUtils.isEmpty(nickname)) return nickname;
        } catch (Exception ignored) {}

        return "管理员";
    }


}