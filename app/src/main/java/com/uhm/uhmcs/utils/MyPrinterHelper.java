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
    /**
     * 结账打印 - 切换为 DIY 模板引擎版
     */
    public void asyncPrintCheckout(Activity context, CheckoutBean bean, PrintDataBean printDataBean, String xinjin_pice, String weixin_pice, String zhifubao_pice, String order_sn) {
        MyPresentation.showHavePaidView();
        printExecutor.execute(() -> {
            try {
                // ⭐ 核心修改：不再自己写 output.write，而是让 CommandUtils 根据 DIY 配置生成指令
                byte[] commands = ReceiptCommandUtils.getReceiptCommands(
                        context,
                        bean,
                        order_sn,
                        xinjin_pice,
                        weixin_pice,
                        zhifubao_pice,
                        "0.00", // 兼容会员卡
                        "0.00"  // 兼容NETS
                );

                if (commands != null) {
                    executePrint(context, commands);
                }
            } catch (Exception e) {
                Log.e(TAG, "打印失败", e);
                sendPrintStatus(context, false);
            }
        });
    }

    /**
     * 必须补全这个方法，否则 DIY 弹窗的“测试打印”会崩溃
     */
    public void printCommand(Activity context, byte[] commands) {
        printExecutor.execute(() -> {
            try {
                executePrint(context, commands);
            } catch (Exception e) {
                sendPrintStatus(context, false);
            }
        });
    }

    /**
     * 统一执行 bulkTransfer
     */
    private void executePrint(Activity context, byte[] data) throws IOException {
        if (usbConnection == null || endpointOut == null) return;
        int transfer = usbConnection.bulkTransfer(endpointOut, data, data.length, 5000);
        if (transfer >= 0) {
            sendPrintStatus(context, true);
        } else {
            throw new IOException("USB 传输失败");
        }
    }


    /**
     * 异步打印尾单 (重打)
     */

    /**
     * 异步打印尾单 (重打上一单或历史单)
     * 统一走 DIY 模板引擎
     */
    public void asyncPrintLastOrder(Activity context, LastOrderBean lastOrder, PrintDataBean printDataBean) {
        if (lastOrder == null) return;

        printExecutor.execute(() -> {
            try {
                // 1. 将 LastOrderBean 转换为 CheckoutBean 格式
                CheckoutBean cb = new CheckoutBean();
                // ★ 修改：优先取 lastOrder 里的 cash_user_sn（因为你历史订单显示正常是靠这个字段）
                cb.setCashierName(lastOrder.getCash_user_sn());
                cb.setMachineNumber("001"); // 补打时的机器号也固定为数字
                cb.setOrder_sn(lastOrder.getOrder_sn());
                cb.setMember_name(lastOrder.getMember_name());
                cb.setMember_phone(lastOrder.getMember_phone());
                cb.setTotal_amount(lastOrder.getTotal_amount());
                cb.setPay_fee(lastOrder.getPay_fee());
                cb.setDiscount_fee(lastOrder.getDiscount_fee());
                cb.setCoupon_fee(lastOrder.getCoupon_fee());
                cb.setCash_change(lastOrder.getCash_change());
                cb.setMachineNumber(lastOrder.getMachineNumber());
                cb.setPay_type(lastOrder.getPay_type());

                // 2. 转换商品明细
                ArrayList<CheckoutBean.GoodsJsonBean> newList = new ArrayList<>();
                int totalCount = 0;
                if (lastOrder.getOrder_item() != null) {
                    for (LastOrderBean.GoodsJsonBean oldItem : lastOrder.getOrder_item()) {
                        CheckoutBean.GoodsJsonBean newItem = new CheckoutBean.GoodsJsonBean();
                        newItem.setTitle(oldItem.getTitle());
                        newItem.setGoods_num(oldItem.getGoods_num());
                        newItem.setGoods_price(oldItem.getGoods_price());
                        newItem.setPay_price(oldItem.getPay_price());

                        // ★★★ 核心修复点 1：安全处理 online_type ★★★
                        // 由于 LastOrderBean 可能没有 online_type 字段，我们根据重量是否存在来判定类型
                        String weightStr = oldItem.getGoods_weight();
                        if (!TextUtils.isEmpty(weightStr) && !weightStr.equals("0") && !weightStr.equals("0.00")) {
                            newItem.setOnline_type("weight");

                            // ★★★ 核心修复点 2：处理重量转换 (String -> int) ★★★
                            try {
                                // 先转 double 处理可能存在的 "500.0" 这种格式，再转 int
                                double weightVal = Double.parseDouble(weightStr);
                                newItem.setGoods_weight((int) weightVal);
                            } catch (Exception e) {
                                newItem.setGoods_weight(0);
                            }
                        } else {
                            newItem.setOnline_type("normal");
                            newItem.setGoods_weight(0);
                        }

                        newList.add(newItem);
                        totalCount += oldItem.getGoods_num();
                    }
                }
                cb.setAllNum(totalCount);
                cb.setGoodsjson(new Gson().toJson(newList));

                // 3. 获取支付方式详情 (现金、微信、支付宝等)
                String cash = "0.00", wechat = "0.00", alipay = "0.00";
                List<LastOrderBean.PaymentlogBean> logs = new ArrayList<>();
                if (lastOrder.getPayment() != null) logs.addAll(lastOrder.getPayment());
                if (lastOrder.getPaymentlog() != null) logs.addAll(lastOrder.getPaymentlog());

                for (LastOrderBean.PaymentlogBean log : logs) {
                    String type = log.getPay_type();
                    String money = log.getReceivedmoney();
                    if (TextUtils.isEmpty(money)) continue;

                    if ("cash".equals(type)) cash = money;
                    else if ("wechat".equals(type)) wechat = money;
                    else if ("alipay".equals(type)) alipay = money;
                }

                // 4. 调用 DIY 打印指令引擎
                byte[] commands = ReceiptCommandUtils.getReceiptCommands(
                        context,
                        cb,
                        lastOrder.getOrder_sn() + " (补打)",
                        cash, wechat, alipay, "0.00", "0.00"
                );

                if (commands != null) {
                    executePrint(context, commands);
                }

            } catch (Exception e) {
                Log.e("PrintHelper", "重打失败: " + e.getMessage(), e);
                sendPrintStatus(context, false);
            }
        });
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