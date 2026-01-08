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
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.CheckoutBean;
import com.uhm.uhmcs.bean.LastOrderBean;
import com.uhm.uhmcs.bean.PrintDataBean;
import com.uhm.uhmcs.bean.RelieveShiftPrintBean;
import com.uhm.uhmcs.view.MyPresentation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 打印机辅助类（最终修正全功能版）
 * 1. 结账与补打：对齐 9 参数逻辑，支持组合支付与 NETS 明细。
 * 2. 指令生成：调用 ReceiptCommandUtils 解决条码逻辑。
 * 3. 补打修复：精准解析 PaymentlogBean 列表，修复组合支付显示。
 */
public class MyPrinterHelper {
    private final ExecutorService printExecutor = Executors.newSingleThreadExecutor();
    private UsbDeviceConnection usbConnection;
    private UsbEndpoint endpointOut;

    private static MyPrinterHelper instance;

    public static MyPrinterHelper getInstance() {
        if (null == instance)
            instance = new MyPrinterHelper();
        return instance;
    }

    // 连接设备并发送打印指令
    public void connectAndPrint(UsbDevice device, UsbDeviceConnection usbConnection) {
        Log.i("ttt", ">>>>>>>connectAndPrint>>>>>>");
        this.usbConnection = usbConnection;
        if (device.getInterfaceCount() > 0) {
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
    }

    /**
     * 获取动态门店名称
     */
    private String getShopName() {
        try {
            if (UserUtils.getInstance().getShopDataBean() != null
                    && UserUtils.getInstance().getShopDataBean().getData() != null
                    && !UserUtils.getInstance().getShopDataBean().getData().isEmpty()) {
                return UserUtils.getInstance().getShopDataBean().getData().get(0).getName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "收银门店";
    }

    /**
     * ⭐ 结账打印 - 完整 9 参数版
     */
    public void asyncPrintCheckout(Activity context, CheckoutBean bean, PrintDataBean printDataBean,
                                   String xinjin_pice, String weixin_pice, String zhifubao_pice,
                                   String huiyuanka_pice, String nets_pice, String order_sn) {

        MyPresentation.showHavePaidView();

        printExecutor.execute(() -> {
            try {
                byte[] commands = ReceiptCommandUtils.getReceiptCommands(
                        context,
                        bean,
                        order_sn,
                        xinjin_pice,
                        weixin_pice,
                        zhifubao_pice,
                        huiyuanka_pice,
                        nets_pice
                );

                executePrint(context, commands);

            } catch (Exception e) {
                Log.e("PrintError", "结账打印异常", e);
                sendPrintStatus(context, false);
            }
        });
    }

    /**
     * ⭐ 补打尾单（历史订单）- 完美修复组合支付版
     */
    // 找到 asyncPrintLastOrder 方法，直接完整替换
    public void asyncPrintLastOrder(Activity context, LastOrderBean bean, PrintDataBean printDataBean) {
        printExecutor.execute(() -> {
            try {
                // 1. 基础转换
                CheckoutBean checkoutBean = new CheckoutBean();
                checkoutBean.setTotal_amount(bean.getTotal_amount());
                checkoutBean.setMember_name(bean.getConsignee());
                checkoutBean.setMember_phone(bean.getPhone());
                checkoutBean.setGoodsjson(new Gson().toJson(bean.getOrder_item()));
                checkoutBean.setDiscount_fee(bean.getDiscount_fee());
                checkoutBean.setCoupon_fee(bean.getCoupon_fee());
                checkoutBean.setPay_type(bean.getPay_type());
                checkoutBean.setCash_change(bean.getCash_change());
                checkoutBean.setMachineNumber(!TextUtils.isEmpty(bean.getCash_user_sn()) ? bean.getCash_user_sn() : "管理员");

                int allNum = 0;
                if (bean.getOrder_item() != null) {
                    for (LastOrderBean.GoodsJsonBean item : bean.getOrder_item()) {
                        allNum += item.getGoods_num();
                    }
                }
                checkoutBean.setAllNum(allNum);

                // 2. ⭐ 支付解析 (主界面按钮专用加强版)
                String cash = "0.00", wechat = "0.00", alipay = "0.00", wallet = "0.00", nets = "0.00";

                List<LastOrderBean.PaymentlogBean> logList = bean.getPaymentlog();
                if (logList == null || logList.isEmpty()) logList = bean.getPayment();

                // 情况 A: 列表有数据 (历史订单页面通常走这里)
                if (logList != null && !logList.isEmpty()) {
                    for (LastOrderBean.PaymentlogBean log : logList) {
                        String type = (log.getPay_type() != null) ? log.getPay_type().toLowerCase() : "";
                        String money = log.getReceivedmoney();
                        if (type.contains("cash")) cash = money;
                        else if (type.contains("wechat") || type.contains("weixin")) wechat = money;
                        else if (type.contains("alipay") || type.contains("zhifubao")) alipay = money;
                        else if (type.contains("member") || type.contains("wallet")) wallet = money;
                        else if (type.contains("nets")) nets = money;
                    }
                }
                // 情况 B: ⭐ 列表为空 (主界面点击打印尾单通常走这里)
                else {
                    String pType = (bean.getPay_type() != null) ? bean.getPay_type().toLowerCase() : "";
                    BigDecimal total = new BigDecimal(TextUtils.isEmpty(bean.getTotal_amount()) ? "0" : bean.getTotal_amount());
                    BigDecimal coupon = new BigDecimal(TextUtils.isEmpty(bean.getCoupon_fee()) ? "0" : bean.getCoupon_fee());
                    String actualAmount = total.subtract(coupon).setScale(2, BigDecimal.ROUND_HALF_UP).toString();

                    // 强制检测是否是组合支付 (即便没有明细列表)
                    // 兼容 逗号、空格、竖线 各种分隔符
                    if (pType.contains(",") || pType.contains(" ") || pType.contains("|")) {
                        // 如果检测到多个支付方式，但没明细，我们标记为“组合支付”
                        nets = "COMBINED:" + actualAmount;
                    } else {
                        // 单一支付方式解析
                        if (pType.contains("cash")) cash = actualAmount;
                        else if (pType.contains("wechat") || pType.contains("weixin")) wechat = actualAmount;
                        else if (pType.contains("alipay") || pType.contains("zhifubao")) alipay = actualAmount;
                        else if (pType.contains("member") || pType.contains("wallet")) wallet = actualAmount;
                        else if (pType.contains("nets")) nets = actualAmount;
                    }
                }

                // 3. 调用指令生成
                byte[] commands = ReceiptCommandUtils.getReceiptCommands(
                        context, checkoutBean, bean.getOrder_sn(),
                        cash, wechat, alipay, wallet, nets
                );
                executePrint(context, commands);

            } catch (Exception e) {
                Log.e("PrintDebug", "主界面补印失败", e);
            }
        });
    }

    // 辅助方法
    private boolean isZero(String val) {
        if (TextUtils.isEmpty(val)) return true;
        try { return new BigDecimal(val).compareTo(BigDecimal.ZERO) == 0; } catch (Exception e) { return true; }
    }

    /**
     * 充值打印
     */
    public void asyncPrintCheckout(Activity context, String pice, String pay_type) {
        printExecutor.execute(() -> {
            try {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                output.write(new byte[]{0x1B, 0x40});
                output.write(new byte[]{0x1B, 0x61, 0x01});
                output.write((getShopName() + " - 充值单\n\n").getBytes("GBK"));
                output.write(("充值金额: " + pice + "\n").getBytes("GBK"));
                output.write(("支付类型: " + pay_type + "\n\n").getBytes("GBK"));
                output.write(new byte[]{0x1D, 0x56, 0x42, 0x00});
                executePrint(context, output.toByteArray());
            } catch (Exception e) {
                sendPrintStatus(context, false);
            }
        });
    }

    /**
     * 交接班打印
     */
    public void asyncPrintRelieveShift(Activity context, RelieveShiftPrintBean relieveShiftPrintBean) {
        printExecutor.execute(() -> {
            try {
                int totalWidth = (ReceiptConfigUtils.getInstance(context).getPaperType() == 0) ? 32 : 48;

                ByteArrayOutputStream output = new ByteArrayOutputStream();
                output.write(new byte[]{0x1B, 0x40});
                output.write(new byte[]{0x1B, 0x61, 0x01});
                output.write(new byte[]{0x1B, 0x45, 0x01});
                output.write("交接单\n".getBytes("GBK"));
                output.write(new byte[]{0x1B, 0x45, 0x00});
                printSeparator(output, totalWidth);

                output.write(new byte[]{0x1B, 0x61, 0x00});
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                RelieveShiftPrintBean.DataBean data = relieveShiftPrintBean.getData();

                output.write(("上班时间:" + sdf.format(new Date(data.getLogintime() * 1000L)) + "\n").getBytes("GBK"));
                output.write(("交班时间:" + sdf.format(new Date(data.getEndtime() * 1000L)) + "\n").getBytes("GBK"));
                output.write(("营业门店:" + getShopName() + "\n").getBytes("GBK"));
                output.write(("收银员:" + data.getNickname() + "\n").getBytes("GBK"));
                printSeparator(output, totalWidth);

                output.write("收款汇总:\n".getBytes("GBK"));
                BigDecimal totalReceived = BigDecimal.ZERO;
                if (data.getTotal() != null) {
                    for (RelieveShiftPrintBean.DataBean.TotalBean item : data.getTotal()) {
                        String label = getPayTypeLabel(item.getPay_type());
                        output.write(formatTwoColumn(label, item.getTotal(), totalWidth).getBytes("GBK"));
                        totalReceived = totalReceived.add(new BigDecimal(item.getTotal()));
                    }
                }
                output.write(formatTwoColumn("总收款:", totalReceived.toString(), totalWidth).getBytes("GBK"));
                printSeparator(output, totalWidth);

                output.write("退款汇总:\n".getBytes("GBK"));
                BigDecimal totalRefund = BigDecimal.ZERO;
                if (data.getRefund() != null) {
                    for (RelieveShiftPrintBean.DataBean.RefundBean item : data.getRefund()) {
                        String label = getPayTypeLabel(item.getPay_type()) + "(退款):";
                        output.write(formatTwoColumn(label, item.getTotal(), totalWidth).getBytes("GBK"));
                        totalRefund = totalRefund.add(new BigDecimal(item.getTotal()));
                    }
                }
                output.write(formatTwoColumn("总退款:", totalRefund.toString(), totalWidth).getBytes("GBK"));
                printSeparator(output, totalWidth);

                output.write(new byte[]{0x1B, 0x45, 0x01});
                output.write(formatTwoColumn("实际总营收:", totalReceived.subtract(totalRefund).toString(), totalWidth).getBytes("GBK"));
                output.write(new byte[]{0x1B, 0x45, 0x00});

                output.write("\n\n\n".getBytes("GBK"));
                output.write(new byte[]{0x1D, 0x56, 0x42, 0x00});

                executePrint(context, output.toByteArray());
            } catch (Exception e) {
                Log.e("PrintError", "交接班打印失败", e);
                sendPrintStatus(context, false);
            }
        });
    }

    private void printSeparator(ByteArrayOutputStream os, int width) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < width; i++) sb.append("-");
        os.write((sb.toString() + "\n").getBytes("GBK"));
    }

    private String getPayTypeLabel(String type) {
        if (type == null) return "其他收入:";
        switch (type) {
            case "cash": return "现金收入:";
            case "alipay": return "支付宝收入:";
            case "wechat": return "微信收入:";
            case "wallet":
            case "member": return "会员卡收入:";
            case "netsp":
            case "netsqr":
            case "netscc":
            case "nets": return "NETS收入:";
            default: return "其他收入:";
        }
    }

    private String formatTwoColumn(String left, String right, int width) {
        int leftLen = getGbkWidth(left);
        int rightLen = getGbkWidth(right);
        int spaces = width - leftLen - rightLen;
        StringBuilder sb = new StringBuilder(left);
        for (int i = 0; i < Math.max(1, spaces); i++) sb.append(" ");
        sb.append(right).append("\n");
        return sb.toString();
    }

    private int getGbkWidth(String s) {
        if (s == null) return 0;
        int w = 0;
        for (char c : s.toCharArray()) w += (c < 128) ? 1 : 2;
        return w;
    }

    private void executePrint(Activity context, byte[] data) throws IOException {
        if (usbConnection == null || endpointOut == null) {
            Log.e("Printer", "USB 未连接");
            return;
        }
        usbConnection.bulkTransfer(endpointOut, data, data.length, 5000);
    }

    public void printCommand(Activity context, byte[] commands) {
        printExecutor.execute(() -> {
            try {
                executePrint(context, commands);
            } catch (Exception e) {
                sendPrintStatus(context, false);
            }
        });
    }

    public void asyncOpenMoneyBox(Activity context) {
        printExecutor.execute(() -> {
            try {
                byte[] openBox = new byte[]{0x1B, 0x40, 0x1B, 0x70, 0x00, 0x60, 0x60};
                executePrint(context, openBox);
            } catch (Exception e) {
                sendPrintStatus(context, false);
            }
        });
    }

    private void sendPrintStatus(Activity context, boolean success) {
        context.runOnUiThread(() -> Toast.makeText(context, success ? "打印完成" : "打印失败", Toast.LENGTH_SHORT).show());
    }

    public void printLongBitmap(Activity context, Bitmap bitmap) {
        printExecutor.execute(() -> {
            try {
                if (usbConnection == null || endpointOut == null) return;
                int height = bitmap.getHeight();
                int width = bitmap.getWidth();
                int chunkHeight = 200;
                int y = 0;
                while (y < height) {
                    int h = Math.min(chunkHeight, height - y);
                    Bitmap chunk = Bitmap.createBitmap(bitmap, 0, y, width, h);
                    byte[] imgCmd = ImagePrinter.convertBitmapToEscPos(ImagePrinter.toMonochrome(chunk));
                    usbConnection.bulkTransfer(endpointOut, imgCmd, imgCmd.length, 5000);
                    y += h;
                    Thread.sleep(50);
                }
                byte[] cut = new byte[]{0x1D, 0x56, 0x42, 0x30};
                usbConnection.bulkTransfer(endpointOut, cut, cut.length, 2000);
            } catch (Exception e) {
                Log.e("Printer", "图片打印失败", e);
            }
        });
    }
}