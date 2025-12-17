package com.uhm.uhmcs.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.CheckoutBean;
import com.uhm.uhmcs.bean.LastOrderBean;
import com.uhm.uhmcs.bean.PrintDataBean;
import com.uhm.uhmcs.bean.RelieveShiftPrintBean;
import com.uhm.uhmcs.view.MyPresentation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyPrinterHelper {
    private static final String TAG = "MyPrinterHelper";
    private final ExecutorService printExecutor = Executors.newSingleThreadExecutor();
    private ByteArrayOutputStream output;
    private UsbDeviceConnection usbConnection;
    private UsbEndpoint endpointOut;
    private static MyPrinterHelper instance;

    // 常量定义
    private static final String CHARSET_GBK = "GBK";
    // private static final byte[] CMD_INIT = {0x1B, 0x40}; // 屏蔽初始化指令，防止多余切纸
    private static final byte[] CMD_ALIGN_LEFT = {0x1B, 0x61, 0x00};
    private static final byte[] CMD_ALIGN_CENTER = {0x1B, 0x61, 0x01};
    private static final byte[] CMD_ALIGN_RIGHT = {0x1B, 0x61, 0x02};
    private static final byte[] CMD_CUT_PAPER = {0x1D, 0x56, 0x42, 0x30};
    private static final byte CMD_LINE_FEED = 0x0A;

    public static MyPrinterHelper getInstance() {
        if (null == instance)
            instance = new MyPrinterHelper();
        return instance;
    }

    /**
     * 连接设备
     */
    public void connectAndPrint(UsbDevice device, UsbDeviceConnection usbConnection) {
        Log.i(TAG, ">>>>>>>connectAndPrint>>>>>>");
        this.usbConnection = usbConnection;
        UsbInterface usbInterface = device.getInterface(0);
        this.usbConnection.claimInterface(usbInterface, true);

        for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
            UsbEndpoint ep = usbInterface.getEndpoint(i);
            if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                endpointOut = ep;
                break;
            }
        }
    }

    /**
     * 异步打印结账单
     */
    public void asyncPrintCheckout(Activity context, CheckoutBean bean, PrintDataBean printDataBean, String xinjin_pice, String weixin_pice, String zhifubao_pice, String order_sn) {
        MyPresentation.showHavePaidView();
        printExecutor.execute(() -> {
            try {
                output = new ByteArrayOutputStream();

                // ▼▼▼▼▼▼ 移除 CMD_INIT，防止先切纸 ▼▼▼▼▼▼
                // output.write(CMD_INIT);
                // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲

                output.write(CMD_ALIGN_CENTER);

                // 1. 打印头部信息 (Logo, 标题, 描述, 图片)
                printHeaderInfo(context, printDataBean);

                // 2. 打印店铺信息
                printShopInfo(false);

                // 3. 打印收银时间
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                String formattedTime = sdf.format(System.currentTimeMillis());
                printTextLine("收银时间:" + formattedTime);
                printDivider();

                // 4. 打印商品列表头
                printTableTitle();
                printDivider();

                // 5. 打印商品列表
                Gson gson = new Gson();
                ArrayList<CheckoutBean.GoodsJsonBean> goodsList = gson.fromJson(bean.getGoodsjson(), new TypeToken<ArrayList<CheckoutBean.GoodsJsonBean>>() {
                }.getType());

                for (int j = 0; j < goodsList.size(); j++) {
                    CheckoutBean.GoodsJsonBean goods = goodsList.get(j);
                    String displayName = (j + 1) + " " + goods.getTitle();
                    if (calculateDisplayWidth(displayName) > 14) {
                        List<String> strings = splitByGbkUnits(displayName, 14);
                        for (int i = 0; i < strings.size(); i++) {
                            if (i == strings.size() - 1) {
                                buildCheckoutLine(goods, strings.get(i));
                            } else {
                                printTextLine(strings.get(i));
                            }
                        }
                    } else {
                        buildCheckoutLine(goods, "");
                    }
                }
                printDivider();

                // 6. 打印统计金额信息
                printSummaryLine("购买商品数量:", bean.getAllNum() + "件", 13);
                printSummaryLine("应付总计:", bean.getTotal_amount() + "", 9);
                printSummaryLine("优惠总计:", bean.getDiscount_fee() + "", 9);

                BigDecimal shifujine_pice = new BigDecimal("0.00");
                if (!TextUtils.isEmpty(xinjin_pice)) shifujine_pice = shifujine_pice.add(new BigDecimal(xinjin_pice));
                if (!TextUtils.isEmpty(weixin_pice)) shifujine_pice = shifujine_pice.add(new BigDecimal(weixin_pice));
                if (!TextUtils.isEmpty(zhifubao_pice)) shifujine_pice = shifujine_pice.add(new BigDecimal(zhifubao_pice));

                printSummaryLine("实付金额:", shifujine_pice.toString(), 9);

                if (!TextUtils.isEmpty(xinjin_pice)) printPaymentLine("现金:", xinjin_pice);
                if (!TextUtils.isEmpty(weixin_pice)) printPaymentLine("微信:", weixin_pice);
                if (!TextUtils.isEmpty(zhifubao_pice)) printPaymentLine("支付宝:", zhifubao_pice);

                if (new BigDecimal(bean.getCash_change()).compareTo(BigDecimal.ZERO) > 0) {
                    printSummaryLine("找零:", bean.getCash_change(), 5);
                }

                // 7. 打印条形码和单号
                output.write(CMD_ALIGN_CENTER);
                output.write(CMD_LINE_FEED);
                if (!TextUtils.isEmpty(order_sn)) {
                    printBarcode(order_sn);
                    printTextLine(order_sn);
                }
                output.write(CMD_LINE_FEED);

                // 8. 打印页脚备注
                printFooter(printDataBean);

                // 9. 切纸并发送
                finalizePrint(context);

            } catch (Exception e) {
                Log.e(TAG, "打印失败", e);
                sendPrintStatus(context, false);
            }
        });
    }

    /**
     * 异步打印尾单 (重打)
     */
    public void asyncPrintLastOrder(Activity context, LastOrderBean bean, PrintDataBean printDataBean) {
        asyncPrintLastOrderInternal(context, bean, printDataBean);
    }

    public void asyncPrintLastOrderInternal(Activity context, LastOrderBean bean, PrintDataBean printDataBean) {
        printExecutor.execute(() -> {
            try {
                if (bean == null) {
                    sendPrintStatus(context, false);
                    return;
                }
                output = new ByteArrayOutputStream();

                // ▼▼▼▼▼▼ 移除 CMD_INIT ▼▼▼▼▼▼
                // output.write(CMD_INIT);
                // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲

                output.write(CMD_ALIGN_CENTER);

                printHeaderInfo(context, printDataBean);
                printShopInfo(true); // isReprint = true

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                String formattedTime = sdf.format(new Date(bean.getPaytime() * 1000L));
                printTextLine("收银时间:" + formattedTime);
                printDivider();

                printTableTitle();
                printDivider();

                ArrayList<LastOrderBean.GoodsJsonBean> goodsList = bean.getOrder_item();
                int allNum = 0;
                if (goodsList != null) {
                    for (int j = 0; j < goodsList.size(); j++) {
                        LastOrderBean.GoodsJsonBean goods = goodsList.get(j);
                        allNum += goods.getGoods_num();
                        String displayName = (j + 1) + " " + goods.getTitle();
                        if (calculateDisplayWidth(displayName) > 14) {
                            List<String> strings = splitByGbkUnits(displayName, 14);
                            for (int i = 0; i < strings.size(); i++) {
                                if (i == strings.size() - 1) {
                                    buildLastOrderLine(goods, strings.get(i));
                                } else {
                                    printTextLine(strings.get(i));
                                }
                            }
                        } else {
                            buildLastOrderLine(goods, "");
                        }
                    }
                }
                printDivider();

                printSummaryLine("购买商品数量:", allNum + "件", 13);
                printSummaryLine("应付总计:", bean.getTotal_amount() + "", 9);
                printSummaryLine("优惠总计:", bean.getDiscount_fee() + "", 9);

                BigDecimal shifujine_pice = new BigDecimal("0.00");
                String xianjin = "", weixin = "", zhifubao = "";

                // 处理支付方式逻辑 (合并 Payment 和 Paymentlog)
                List<LastOrderBean.PaymentlogBean> allPayments = new ArrayList<>();
                if (bean.getPayment() != null) allPayments.addAll(bean.getPayment());
                if (bean.getPaymentlog() != null) allPayments.addAll(bean.getPaymentlog());

                for (LastOrderBean.PaymentlogBean payment : allPayments) {
                    String money = payment.getReceivedmoney();
                    shifujine_pice = shifujine_pice.add(new BigDecimal(money));
                    if ("cash".equals(payment.getPay_type())) xianjin = money;
                    else if ("alipay".equals(payment.getPay_type())) zhifubao = money;
                    else if ("wechat".equals(payment.getPay_type())) weixin = money;
                }

                printSummaryLine("实付金额:", shifujine_pice.toString(), 9);
                if (!TextUtils.isEmpty(xianjin)) printPaymentLine("现金:", xianjin);
                if (!TextUtils.isEmpty(weixin)) printPaymentLine("微信:", weixin);
                if (!TextUtils.isEmpty(zhifubao)) printPaymentLine("支付宝:", zhifubao);

                BigDecimal totalFee = new BigDecimal(TextUtils.isEmpty(bean.getTotal_fee()) ? "0" : bean.getTotal_fee());
                if (shifujine_pice.subtract(totalFee).compareTo(BigDecimal.ZERO) > 0) {
                    printSummaryLine("找零:", shifujine_pice.subtract(totalFee).toString(), 5);
                }

                output.write(CMD_ALIGN_CENTER);
                output.write(CMD_LINE_FEED);
                if (!TextUtils.isEmpty(bean.getOrder_sn())) {
                    printBarcode(bean.getOrder_sn());
                    printTextLine(bean.getOrder_sn());
                }
                output.write(CMD_LINE_FEED);

                printFooter(printDataBean);
                finalizePrint(context);

            } catch (Exception e) {
                Log.e(TAG, "打印失败", e);
                sendPrintStatus(context, false);
            }
        });
    }

    /**
     * 开启钱箱
     */
    public void asyncOpenMoneyBox(Activity context) {
        printExecutor.execute(() -> {
            try {
                output = new ByteArrayOutputStream();
                // 钱箱不需要初始化指令，直接发脉冲
                output.write(new byte[]{0x1B, 0x70, 0x00, 0x60, 0x60});

                int transfer = usbConnection.bulkTransfer(
                        endpointOut,
                        output.toByteArray(),
                        output.toByteArray().length,
                        5000
                );
                if (transfer >= 0) {
                    sendPrintStatus(context, true);
                }
            } catch (Exception e) {
                Log.e(TAG, "钱箱打开失败", e);
                sendPrintStatus(context, false);
            }
        });
    }

    /**
     * 打印交班单
     */
    public void asyncPrintRelieveShift(Activity context, RelieveShiftPrintBean bean) {
        printExecutor.execute(() -> {
            try {
                output = new ByteArrayOutputStream();
                // output.write(CMD_INIT); // 移除初始化
                output.write(CMD_ALIGN_CENTER);
                printTextLine("交接单");
                printDivider();

                output.write(CMD_ALIGN_RIGHT);
                printTextLine("交班单号:" + bean.getShift_id());

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                String formattedTime = sdf.format(System.currentTimeMillis());
                printTextLine("交班时间:" + formattedTime);
                printTextLine("营业门店:" + UserUtils.getInstance().getShopDataBean().getData().get(0).getName());
                printTextLine("收银员:" + UserUtils.getInstance().getLoginBase().getData().getUserinfo().getUsername());
                printDivider();

                finalizePrint(context);
            } catch (Exception e) {
                Log.e(TAG, "打印失败", e);
                sendPrintStatus(context, false);
            }
        });
    }

    // ================= 私有辅助方法 =================

    private void printHeaderInfo(Activity context, PrintDataBean printDataBean) throws IOException {
        if (printDataBean != null) {
            if (!TextUtils.isEmpty(printDataBean.getLogo())) {
                printImage(context, printDataBean.getLogo(), 200, 200);
            }
            if (!TextUtils.isEmpty(printDataBean.getTitle())) {
                printTextLine(printDataBean.getTitle());
            }
            if (!TextUtils.isEmpty(printDataBean.getDescribe())) {
                printTextLine(printDataBean.getDescribe());
            }
            if (!TextUtils.isEmpty(printDataBean.getImage())) {
                printImage(context, printDataBean.getImage(), 200, 200);
            }
        }
    }

    private void printShopInfo(boolean isReprint) throws IOException {
        String shopName = UserUtils.getInstance().getShopDataBean().getData().get(0).getName();
        if (isReprint) shopName += " (补打)";
        printTextLine(shopName);
        printTextLine(UserUtils.getInstance().getShopDataBean().getData().get(0).getAddress());
        printTextLine("全国客服热线:" + UserUtils.getInstance().getShopDataBean().getData().get(0).getPhone());
        output.write(CMD_ALIGN_LEFT);
    }

    private void printTableTitle() throws IOException {
        String title = "商品名称";
        title += getSpaces(18 - 8);
        title += "数量";
        title += getSpaces(12 - 4);
        title += "单价";
        title += getSpaces(8 - 4);
        title += getSpaces(10 - 4);
        title += "金额";
        output.write(title.getBytes(CHARSET_GBK));
        output.write(CMD_LINE_FEED);
    }

    private void printFooter(PrintDataBean printDataBean) throws IOException {
        output.write(CMD_ALIGN_LEFT);
        printTextLine("此单据二维码为开具增值税普通发票");
        printTextLine("的唯一凭证，请妥善保管。");
        printTextLine("请保留此单据，作为退丶换货凭证。");
        output.write(CMD_ALIGN_CENTER);
        if (printDataBean != null && !TextUtils.isEmpty(printDataBean.getBottom_remarks())) {
            printTextLine(printDataBean.getBottom_remarks());
        }
    }

    private void printBarcode(String content) throws IOException {
        Bitmap bitmap = generateBarcode(content, BarcodeFormat.CODE_128, 360, 80);
        if (bitmap != null) {
            Bitmap processed = ImagePrinter.toMonochrome(bitmap);
            output.write(ImagePrinter.convertBitmapToEscPos(processed));
        }
    }

    private void printImage(Activity context, String url, int w, int h) {
        try {
            Bitmap bitmap = Glide.with(context)
                    .asBitmap()
                    .load(url)
                    .submit(w, h)
                    .get();
            Bitmap processed = ImagePrinter.toMonochrome(bitmap);
            output.write(ImagePrinter.convertBitmapToEscPos(processed));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printTextLine(String text) throws IOException {
        if (text != null) {
            output.write((text + "\n").getBytes(CHARSET_GBK));
        }
    }

    private void printDivider() throws IOException {
        printTextLine("------------------------------------------------");
    }

    private void printSummaryLine(String label, String value, int spaceAdjustment) throws IOException {
        String line = label;
        int padding = 48 - spaceAdjustment - calculateDisplayWidth(value);
        line += getSpaces(padding);
        line += value;
        printTextLine(line);
    }

    private void printPaymentLine(String label, String value) throws IOException {
        String line = label;
        int padding = 48 - calculateDisplayWidth(label) - calculateDisplayWidth(value);
        line += getSpaces(padding);
        line += value;
        printTextLine(line);
    }

    private String getSpaces(int length) {
        if (length <= 0) return "";
        return new String(new char[length]).replace('\0', ' ');
    }

    private void finalizePrint(Activity context) throws IOException {
        output.write(CMD_CUT_PAPER);
        int transfer = usbConnection.bulkTransfer(
                endpointOut,
                output.toByteArray(),
                output.toByteArray().length,
                5000
        );
        if (transfer >= 0) {
            sendPrintStatus(context, true);
        } else {
            throw new IOException("打印数据传输失败");
        }
    }

    private int calculateDisplayWidth(String str) {
        if (str == null) return 0; // 修复空指针
        int width = 0;
        for (char c : str.toCharArray()) {
            width += (c < 128) ? 1 : 2;
        }
        return width;
    }

    public void buildCheckoutLine(CheckoutBean.GoodsJsonBean goods, String nameOverride) throws IOException {
        String name = TextUtils.isEmpty(nameOverride) ? goods.getTitle() : nameOverride;
        if (name == null) name = "";
        String num = "x" + goods.getGoods_num();
        String price = goods.getGoods_price() == null ? "0.00" : goods.getGoods_price();
        String total = goods.getPay_price() == null ? "0.00" : goods.getPay_price();

        buildGoodsLine(name, num, price, total);
    }

    public void buildLastOrderLine(LastOrderBean.GoodsJsonBean goods, String nameOverride) throws IOException {
        String name = TextUtils.isEmpty(nameOverride) ? goods.getTitle() : nameOverride;
        if (name == null) name = "";
        String num = "x" + goods.getGoods_num();
        String price = goods.getGoods_price() == null ? "0.00" : goods.getGoods_price();
        String total = goods.getPay_price() == null ? "0.00" : goods.getPay_price();

        buildGoodsLine(name, num, price, total);
    }

    private void buildGoodsLine(String name, String num, String price, String total) throws IOException {
        String line = name;
        line += getSpaces(18 - calculateDisplayWidth(name));
        line += num;
        line += getSpaces(12 - calculateDisplayWidth(num));
        line += price;
        line += getSpaces(8 - calculateDisplayWidth(price));
        line += getSpaces(10 - calculateDisplayWidth(total));
        line += total;
        printTextLine(line);
    }

    public List<String> splitByGbkUnits(String input, int maxUnits) {
        List<String> result = new ArrayList<>();
        if (input == null || input.isEmpty()) return result;
        try {
            int currentUnits = 0;
            int startIndex = 0;
            for (int i = 0; i < input.length(); i++) {
                char c = input.charAt(i);
                int unit = getGbkCharUnit(c);
                if (currentUnits + unit > maxUnits) {
                    result.add(input.substring(startIndex, i));
                    startIndex = i;
                    currentUnits = unit;
                } else {
                    currentUnits += unit;
                }
            }
            if (startIndex < input.length()) {
                result.add(input.substring(startIndex));
            }
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "GBK split error", e);
        }
        return result;
    }

    private int getGbkCharUnit(char c) throws UnsupportedEncodingException {
        return String.valueOf(c).getBytes(CHARSET_GBK).length;
    }

    private Bitmap generateBarcode(String content, BarcodeFormat format, int width, int height) {
        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix matrix = writer.encode(content, format, width, height);
            BarcodeEncoder encoder = new BarcodeEncoder();
            return encoder.createBitmap(matrix);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void sendPrintStatus(Activity context, boolean success) {
        if (context != null) {
            context.runOnUiThread(() -> Toast.makeText(context, success ? "打印成功" : "打印失败", Toast.LENGTH_SHORT).show());
        }
    }
}