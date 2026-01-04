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
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 打印机辅助类（全功能稳定版 - 动态宽度与门店名适配）
 * 1. 结账与补打：统一走 ReceiptCommandUtils，应用 DIY 样式。
 * 2. 交接班：自动适配纸张宽度，动态获取门店名称。
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
        return "收银门店"; // 默认保底名称
    }

    /**
     * 异步打印结账 - 已重定向至 DIY 模板
     */
    public void asyncPrintCheckout(Activity context, CheckoutBean bean, PrintDataBean printDataBean, String xinjin_pice, String weixin_pice, String zhifubao_pice, String huiyuanka_pice, String order_sn) {
        MyPresentation.showHavePaidView();
        printExecutor.execute(() -> {
            try {
                // 直接调用辛苦调好的指令生成器
                byte[] commands = ReceiptCommandUtils.getReceiptCommands(context, bean, order_sn);
                executePrint(context, commands);
            } catch (Exception e) {
                Log.e("PrintError", "结账打印异常", e);
                sendPrintStatus(context, false);
            }
        });
    }

    /**
     * 异步打印历史订单/尾单 - 已重定向至 DIY 模板
     */
    public void asyncPrintLastOrder(Activity context, LastOrderBean bean, PrintDataBean printDataBean) {
        printExecutor.execute(() -> {
            try {
                // 1. 数据转换：将历史订单转为结账 Bean
                CheckoutBean checkoutBean = new CheckoutBean();
                checkoutBean.setTotal_amount(bean.getTotal_amount());

                // 会员信息对齐
                checkoutBean.setMember_name(bean.getConsignee());
                checkoutBean.setMember_phone(bean.getPhone());

                // 商品与金额对齐
                checkoutBean.setGoodsjson(new Gson().toJson(bean.getOrder_item()));
                checkoutBean.setDiscount_fee(bean.getDiscount_fee());
                checkoutBean.setCoupon_fee(bean.getCoupon_fee());
                checkoutBean.setPay_type(bean.getPay_type());
                checkoutBean.setCash_change(bean.getCash_change());

                // ⭐【核心修复：动态化收银员】⭐
                // 逻辑：如果历史订单 bean 里的 cash_user_sn 有值，就用历史的；
                // 如果为空（比如旧数据），则获取当前登录用户的 Nickname。
                String realCashier = "";
                if (bean != null && !TextUtils.isEmpty(bean.getCash_user_sn())) {
                    realCashier = bean.getCash_user_sn();
                } else {
                    try {
                        realCashier = UserUtils.getInstance().getLoginBase().getData().getUserinfo().getNickname();
                    } catch (Exception e) {
                        realCashier = "管理员"; // 最后的保底
                    }
                }
                checkoutBean.setMachineNumber(realCashier);

                // 计算总数量
                int allNum = 0;
                if (bean.getOrder_item() != null) {
                    for (LastOrderBean.GoodsJsonBean item : bean.getOrder_item()) {
                        allNum += item.getGoods_num();
                    }
                }
                checkoutBean.setAllNum(allNum);

                // 2. 调用指令工具打印
                // 注意：这里传入 bean.getOrder_sn() 确保打印的是该订单的条码
                byte[] commands = ReceiptCommandUtils.getReceiptCommands(context, checkoutBean, bean.getOrder_sn());
                executePrint(context, commands);

            } catch (Exception e) {
                Log.e("PrintError", "尾单打印异常", e);
                sendPrintStatus(context, false);
            }
        });
    }

    /**
     * 充值打印 (同步修改门店名)
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
     * 【完整恢复并优化】交接班打印
     * 自动适配 58mm/80mm 纸张规格，动态门店名称
     */
    public void asyncPrintRelieveShift(Activity context, RelieveShiftPrintBean relieveShiftPrintBean) {
        printExecutor.execute(() -> {
            try {
                // 动态获取当前设置的纸张总宽度 (32字节或48字节)
                int totalWidth = (ReceiptConfigUtils.getInstance(context).getPaperType() == 0) ? 32 : 48;

                ByteArrayOutputStream output = new ByteArrayOutputStream();
                output.write(new byte[]{0x1B, 0x40});
                // 居中标题
                output.write(new byte[]{0x1B, 0x61, 0x01});
                output.write(new byte[]{0x1B, 0x45, 0x01}); // 加粗
                output.write("交接单\n".getBytes("GBK"));
                output.write(new byte[]{0x1B, 0x45, 0x00});
                printSeparator(output, totalWidth); // 动态分割线

                // 左对齐基本信息
                output.write(new byte[]{0x1B, 0x61, 0x00});
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                RelieveShiftPrintBean.DataBean data = relieveShiftPrintBean.getData();

                output.write(("上班时间:" + sdf.format(new Date(data.getLogintime() * 1000L)) + "\n").getBytes("GBK"));
                output.write(("交班时间:" + sdf.format(new Date(data.getEndtime() * 1000L)) + "\n").getBytes("GBK"));
                output.write(("营业门店:" + getShopName() + "\n").getBytes("GBK"));
                output.write(("收银员:" + data.getNickname() + "\n").getBytes("GBK"));
                printSeparator(output, totalWidth);

                // 收款汇总
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

                // 退款汇总
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

                // 实际营收
                output.write(new byte[]{0x1B, 0x45, 0x01});
                output.write(formatTwoColumn("实际总营收:", totalReceived.subtract(totalRefund).toString(), totalWidth).getBytes("GBK"));
                output.write(new byte[]{0x1B, 0x45, 0x00});

                output.write("\n\n\n".getBytes("GBK"));
                output.write(new byte[]{0x1D, 0x56, 0x42, 0x00}); // 切纸

                executePrint(context, output.toByteArray());
            } catch (Exception e) {
                Log.e("PrintError", "交接班打印失败", e);
                sendPrintStatus(context, false);
            }
        });
    }

    /**
     * 辅助：打印动态宽度的分割线
     */
    private void printSeparator(ByteArrayOutputStream os, int width) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < width; i++) sb.append("-");
        os.write((sb.toString() + "\n").getBytes("GBK"));
    }

    private String getPayTypeLabel(String type) {
        switch (type) {
            case "cash": return "现金收入:";
            case "alipay": return "支付宝收入:";
            case "wechat": return "微信收入:";
            case "wallet":
            case "member": return "会员卡收入:";
            default: return "其他收入:";
        }
    }

    /**
     * 核心对齐逻辑：根据纸张宽度自动计算中间空格，实现左右分布
     */
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

    /**
     * 底层执行：发送字节数组到打印机
     */
    private void executePrint(Activity context, byte[] data) throws IOException {
        if (usbConnection == null || endpointOut == null) {
            Log.e("Printer", "USB 未连接");
            sendPrintStatus(context, false);
            return;
        }
        int transfer = usbConnection.bulkTransfer(endpointOut, data, data.length, 5000);
        sendPrintStatus(context, transfer >= 0);
    }

    /**
     * 发送 ESC/POS 指令 (DIY 测试打印专用)
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
     * 开启钱箱
     */
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