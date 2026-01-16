package com.uhm.uhmcs.utils;

import android.annotation.SuppressLint;
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

import com.uhm.uhmcs.bean.GrouponGoodsBean;
import com.uhm.uhmcs.popupwindow.DeleteShopPopupWindow;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyLabeksPrinterHelper {
    private final ExecutorService printExecutor = Executors.newSingleThreadExecutor();
    private ByteArrayOutputStream output;


    private UsbDeviceConnection usbConnection;
    private UsbEndpoint endpointOut;


    private static MyLabeksPrinterHelper instance;

    /**
     * 获取单件实例
     *
     * @return
     */
    public static MyLabeksPrinterHelper getInstance() {
        if (null == instance)
            instance = new MyLabeksPrinterHelper();
        return instance;
    }


    // 连接设备并发送打印指令
    public void connectAndPrint(UsbDevice device, UsbDeviceConnection usbConnection) throws IOException {
        Log.i("ttt", ">>>>>>>MyLabeksPrinterHelper>>>>>>");
        this.usbConnection = usbConnection;
        UsbInterface usbInterface = device.getInterface(0);
        this.usbConnection.claimInterface(usbInterface, true);

        // 获取输出端点
        for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
            UsbEndpoint ep = usbInterface.getEndpoint(i);
            if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                endpointOut = ep;

                break;
            }
        }
    }

    /**
     * 异步打印结账
     */
    @SuppressLint("DefaultLocale")
    public void asyncPrintCheckout(Activity context, ArrayList<GrouponGoodsBean.GrouponGoodsModel> grouponGoodsModelArrayList, int num) {
        printExecutor.execute(() -> {
            try {

                StringBuilder cmd = new StringBuilder();

                // 标签初始化
                cmd.append("SIZE 100 mm,40 mm \n");   // 标签尺寸
                cmd.append("BELINE 1 mm,1 mm \n");      // 标签间隙
                cmd.append("SET RESPONSE NO \n");

                cmd.append("DIRECTION 1 \n");
                cmd.append("SENSOR BLACKMARK \n");


//        // 添加图片（需转换为单色位图）
//        Bitmap bitmap = convertToMonochrome(BitmapFactory.decodeResource(getResources(), R.drawable.logo));
//        cmd.append("BITMAP ")
//                .append(400).append(",").append(100).append(",")
//                .append(bitmap.getWidth() / 8).append(",")
//                .append(bitmap.getHeight()).append(",0,");
                // 此处需添加位图数据（简化示例，实际需遍历像素生成二进制数据）

                for (GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel : grouponGoodsModelArrayList) {
                    for (int j=0;j<num;j++){
                        // 清空缓冲区
                        cmd.append("CLS \n");                // 清空缓冲区
                        cmd.append("TEXT " + 125 + "," + 65+ ",\"FONT001\",0,1,1,\"").append(grouponGoodsModel.getSubtitle()).append("\"\r\n");
                        cmd.append("TEXT " + 80 + "," + 120+ ",\"FONT001\",0,1,1,\"").append(grouponGoodsModel.getSn()).append("\"\r\n");
                        cmd.append("TEXT " + 80 + "," + 165+ ",\"FONT001\",0,1,1,\"").append(grouponGoodsModel.getGoods_sn()).append("\"\r\n");

                        if (!TextUtils.isEmpty(grouponGoodsModel.getSpecs_title())){
//                            String input =grouponGoodsModel.getGoods_sku_text();
//                            String result = input.substring(1, input.length() - 1);
                            cmd.append("TEXT " + 250 + "," + 165+ ",\"FONT001\",0,1,1,\"").append(grouponGoodsModel.getSpecs_title()).append("\"\r\n");
                        }



                        cmd.append("TEXT " + 120 + "," + 215+ ",\"FONT001\",0,1,1,\"").append((TextUtils.isEmpty(grouponGoodsModel.getUnit())?"1":grouponGoodsModel.getUnit())).append("\"\r\n");
                        cmd.append("TEXT " + 80 + "," + 260+ ",\"FONT001\",0,1,1,\"").append("合格品").append("\"\r\n");
                        cmd.append("TEXT " + 270 + "," + 215+ ",\"FONT001\",0,1,1,\"").append("物价员").append("\"\r\n");
//                        cmd.append("TEXT " + 250 + "," + 260+ ",\"FONT001\",0,1,1,\"").append(grouponGoodsModel.getc).append("\"\r\n");
                        cmd.append("TEXT " + 495 + "," + 70+ ",\"FONT001\",0,2,2,\"").append(grouponGoodsModel.getPrice()+"元").append("\"\r\n");
                        cmd.append("TEXT " + 495 + "," + 165+ ",\"FONT001\",0,1,1,\"").append(grouponGoodsModel.getReward_points()).append("\"\r\n");
                        cmd.append("TEXT " + 495 + "," + 215+ ",\"FONT001\",0,1,1,\"").append(grouponGoodsModel.getDeduction_golive()).append("\"\r\n");
                        cmd.append("PRINT 1\r\n");
                    }

                }
                // 执行打印
//                cmd.append("PRINT "+grouponGoodsModelArrayList.size()+"\r\n"); // 打印 n 份
                // 3. 发送指令
                byte[] commandBytes = cmd.toString().getBytes("GBK");
                int transferResult = usbConnection.bulkTransfer(endpointOut, commandBytes, commandBytes.length, 5000);


            } catch (Exception e) {
                Log.e("PrintError", "打印失败", e);
                sendPrintStatus(context, false);
            }
        });
    }

    /**
     * 将字符串按GBK编码的字符单位分割
     * @param input      输入字符串
     * @param maxUnits   每块最大单位数（例如14）
     * @return 分割后的字符串列表
     */
    public  List<String> splitByGbkUnits(String input, int maxUnits) {
        List<String> result = new ArrayList<>();
        if (input == null || input.isEmpty()) return result;

        try {
            int currentUnits = 0;    // 当前块累计单位
            int startIndex = 0;      // 当前块的起始位置

            for (int i = 0; i < input.length(); i++) {
                char c = input.charAt(i);
                int unit = getGbkCharUnit(c);

                if (currentUnits + unit > maxUnits) {
                    // 超出限制，分割字符串
                    result.add(input.substring(startIndex, i));
                    startIndex = i;
                    currentUnits = unit;  // 新块从当前字符开始
                } else {
                    currentUnits += unit;
                }
            }

            // 添加最后一个块
            if (startIndex < input.length()) {
                result.add(input.substring(startIndex));
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("GBK encoding not supported", e);
        }

        return result;
    }

    /**
     * 获取字符的GBK单位（中文2，英文1）
     */
    private  int getGbkCharUnit(char c) throws UnsupportedEncodingException {
        byte[] bytes = String.valueOf(c).getBytes("GBK");
        return bytes.length; // 中文字符返回2，英文字符返回1
    }

    // 构建 TSC 指令
    private String createTscCommand(GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel) {
        StringBuilder cmd = new StringBuilder();

        // 标签初始化
        cmd.append("SIZE 80 mm,30 mm\r\n");   // 标签尺寸
        cmd.append("GAP 2 mm,0 mm\r\n");      // 标签间隙
        cmd.append("CLS\r\n");                // 清空缓冲区

        // 添加文本
        cmd.append("TEXT " + 10 + "," + 40 + ",\"TSS24.BF2\",0,1,1,\"" + grouponGoodsModel.getTitle() + "\"\r\n");
        cmd.append("TEXT " + 10 + "," + 120 + ",\"TSS24.BF2\",0,2,2,\"" +"￥"+ grouponGoodsModel.getPrice() + "\"\r\n");

//        // 添加图片（需转换为单色位图）
//        Bitmap bitmap = convertToMonochrome(BitmapFactory.decodeResource(getResources(), R.drawable.logo));
//        cmd.append("BITMAP ")
//                .append(400).append(",").append(100).append(",")
//                .append(bitmap.getWidth() / 8).append(",")
//                .append(bitmap.getHeight()).append(",0,");
        // 此处需添加位图数据（简化示例，实际需遍历像素生成二进制数据）

        return cmd.toString();
    }
    // 位图转单色（简化实现）
    private Bitmap convertToMonochrome(Bitmap src) {
        Bitmap result = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        // 实际需实现二值化算法（例如阈值处理）
        return result;
    }


    /**
     * 状态回调方法（主线程执行）
     */
    private void sendPrintStatus(Activity context, boolean success) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (context instanceof Activity) {
                    Toast.makeText(context,
                            success ? "打印成功" : "打印失败",
                            Toast.LENGTH_SHORT).show();
                    new DeleteShopPopupWindow(context,success ? "打印成功" : "打印失败",true).show();
                }
            }
        });
    }

    /**
     * DIY 动态标签打印逻辑 (基于TSPL)
     */
    /**
     * DIY 动态标签打印逻辑 (基于TSPL) - 修复版
     */
    /**
     * DIY 动态标签打印逻辑 (基于TSPL) - 针对40mm窄纸优化版
     */
    public void asyncPrintLabelDIY(Activity context, List<GrouponGoodsBean.GrouponGoodsModel> goodsList, int count) {
        printExecutor.execute(() -> {
            try {
                // 1. 读取配置
                int labelW = UserUtils.getInstance().getLabelWidth(context); // 40
                int labelH = UserUtils.getInstance().getLabelHeight(context); // 30
                boolean showShop = UserUtils.getInstance().getLabelConfig(context, "shop_name", true);
                boolean showName = UserUtils.getInstance().getLabelConfig(context, "product_name", true);
                boolean showPrice = UserUtils.getInstance().getLabelConfig(context, "price", true);
                boolean showBarcode = UserUtils.getInstance().getLabelConfig(context, "barcode", true);

                if (usbConnection == null || endpointOut == null) {
                    sendPrintStatus(context, false);
                    return;
                }

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                // 2. 初始化指令
                // 关键修复：设置原点参考 REFERENCE 0,0
                String sizeCmd = "SIZE " + labelW + " mm," + labelH + " mm\n";
                buffer.write(sizeCmd.getBytes());
                buffer.write("GAP 2 mm,0 mm\n".getBytes());
                buffer.write("DIRECTION 1\n".getBytes());
                buffer.write("REFERENCE 0,0\n".getBytes());
                buffer.write("CLS\n".getBytes());

                // 计算中心点 X 坐标 (203 DPI: 1mm ≈ 8 dots)
                // 40mm 宽 = 320 dots
                int centerX = (labelW * 8) / 2;

                for (GrouponGoodsBean.GrouponGoodsModel goods : goodsList) {
                    for (int i = 0; i < count; i++) {
                        buffer.write("CLS\n".getBytes());

                        // 动态 Y 坐标，初始稍微靠下一点，防止被撕纸刀挡住
                        int currentY = 10;

                        // A. 打印店名
                        if (showShop) {
                            String shopName = com.uhm.uhmcs.utils.UserUtils.getInstance().getLoginBase().getData().getUserinfo().getNickname();
                            if (TextUtils.isEmpty(shopName)) shopName = "优海猫测试用";

                            // 字体参数: x,y,"字体",旋转,X放大,Y放大,对齐(2=居中),内容
                            String cmd = "TEXT " + centerX + "," + currentY + ",\"TSS24.BF2\",0,1,1,2,\"" + shopName + "\"\n";
                            buffer.write(cmd.getBytes("GBK"));
                            currentY += 30; // 窄纸行间距小一点
                        }

                        // B. 打印商品名称
                        if (showName) {
                            String name = goods.getTitle();
                            // 【修复】40mm纸很窄，TSS24字体一行只能打约12个英文字符或6个汉字
                            // 如果不做截断，文字会自动换行或被切掉
                            if (name.length() > 8) name = name.substring(0, 7) + "..";

                            String cmd = "TEXT " + centerX + "," + currentY + ",\"TSS24.BF2\",0,1,1,2,\"" + name + "\"\n";
                            buffer.write(cmd.getBytes("GBK"));
                            currentY += 30;
                        }

                        // C. 打印价格
                        if (showPrice) {
                            String priceStr = "￥" + goods.getPrice();
                            // 【优化】40mm纸，价格字体放大(2,2)可能太大，占满一行，容易偏
                            // 改为宽放大2倍，高放大2倍 (TSS24本身高24，放大后48)
                            String cmd = "TEXT " + centerX + "," + currentY + ",\"TSS24.BF2\",0,2,2,2,\"" + priceStr + "\"\n";
                            buffer.write(cmd.getBytes("GBK"));
                            currentY += 60;
                        }

                        // D. 打印条码 (最关键的修复)
                        if (showBarcode) {
                            String code = goods.getSn();
                            if (TextUtils.isEmpty(code)) code = goods.getGoods_sn();
                            if (TextUtils.isEmpty(code)) code = "123456";

                            // 如果空间不够，固定到底部
                            int barcodeY = Math.max(currentY, (labelH * 8) - 50);

                            // 【核心修复】
                            // 1. x坐标：不要用centerX-90，直接固定靠左 (x=10)，因为条码很长
                            // 2. 窄条宽/宽条宽：改为 1,2 (之前是2,2，太宽了，40mm纸打不下)
                            // BARCODE x,y,"类型",高度,是否显文字,旋转,窄条,宽条,内容
                            String cmd = "BARCODE 10," + barcodeY + ",\"128\",40,1,0,1,2,\"" + code + "\"\n";
                            buffer.write(cmd.getBytes());
                        }

                        buffer.write("PRINT 1,1\n".getBytes());
                    }
                }

                int transfer = usbConnection.bulkTransfer(endpointOut, buffer.toByteArray(), buffer.size(), 5000);
                if (transfer >= 0) {
                    sendPrintStatus(context, true);
                } else {
                    sendPrintStatus(context, false);
                }

            } catch (Exception e) {
                Log.e("PrintError", "DIY打印失败", e);
                sendPrintStatus(context, false);
            }
        });
    }


    /**
     * 【修复版】直接打印 Bitmap 图片 (所见即所得)
     * @param mmW 用户在UI设置的宽度 (mm)
     * @param mmH 用户在UI设置的高度 (mm)
     */
    public void printBitmapLabel(Activity context, Bitmap bitmap, int mmW, int mmH, int count) {
        printExecutor.execute(() -> {
            try {
                if (usbConnection == null || endpointOut == null) {
                    sendPrintStatus(context, false);
                    return;
                }

                int width = bitmap.getWidth();
                int height = bitmap.getHeight();

                // TSPL 指令要求宽度字节数必须是整数：每行字节数 = (像素宽 + 7) / 8
                int rowBytes = (width + 7) / 8;
                byte[] data = new byte[rowBytes * height];

                java.util.Arrays.fill(data, (byte) 0xFF); // 初始化为全白（TSPL中0为黑，1为白）

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int pixel = bitmap.getPixel(x, y);
                        int r = (pixel >> 16) & 0xFF;
                        int g = (pixel >> 8) & 0xFF;
                        int b = pixel & 0xFF;
                        int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);

                        // 阈值处理：这里的灰度判断决定了打印的清晰度
                        if (gray < 200) {
                            // 将该点设为黑色 (Bit 置 0)
                            data[y * rowBytes + x / 8] &= ~(128 >> (x % 8));
                        }
                    }
                }

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                // --- 核心修复点 ---
                // 1. 使用动态传入的 mm 尺寸
                buffer.write(("SIZE " + mmW + " mm," + mmH + " mm\n").getBytes());
                buffer.write("GAP 2 mm,0 mm\n".getBytes());
                buffer.write("DIRECTION 1\n".getBytes());
                buffer.write("REFERENCE 0,0\n".getBytes()); // 强制原点归零，防止偏移
                buffer.write("CLS\n".getBytes());

                // 2. 发送图片指令 (从 0,0 坐标开始绘制)
                String cmd = "BITMAP 0,0," + rowBytes + "," + height + ",0,";
                buffer.write(cmd.getBytes());
                buffer.write(data);
                buffer.write("\n".getBytes());

                // 3. 打印
                buffer.write(("PRINT " + count + ",1\n").getBytes());

                int transfer = usbConnection.bulkTransfer(endpointOut, buffer.toByteArray(), buffer.size(), 10000);
                sendPrintStatus(context, transfer >= 0);

            } catch (Exception e) {
                Log.e("PrintError", "图片打印失败", e);
                sendPrintStatus(context, false);
            }
        });
    }





    /**
     * 批量打印 Bitmap 列表 (用于商品列表打印)
     * @param bitmaps 图片列表
     * @param copies 每张图打印几份
     */
    public void asyncPrintBatchBitmaps(Activity context, List<Bitmap> bitmaps, int copies) {
        printExecutor.execute(() -> {
            try {
                if (usbConnection == null || endpointOut == null) {
                    sendPrintStatus(context, false);
                    return;
                }

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                // 1. 初始化指令 (只需发一次尺寸，因为所有标签尺寸一样)
                // 注意：这里我们假设所有图片尺寸一致，取第一张图的尺寸
                if (bitmaps.isEmpty()) return;

                int bmpW = bitmaps.get(0).getWidth();
                int bmpH = bitmaps.get(0).getHeight();
                // 这里的尺寸指令其实不太重要，因为 BITMAP 指令会覆盖，但为了走纸正确，还是发一下
                // 注意：这里没办法反推 mm，所以我们取 UserUtils 里的配置
                int labelW = com.uhm.uhmcs.utils.UserUtils.getInstance().getLabelWidth(context);
                int labelH = com.uhm.uhmcs.utils.UserUtils.getInstance().getLabelHeight(context);

                buffer.write(("SIZE " + labelW + " mm," + labelH + " mm\n").getBytes());
                buffer.write("GAP 2 mm,0 mm\n".getBytes());
                buffer.write("DIRECTION 1\n".getBytes());
                buffer.write("REFERENCE 0,0\n".getBytes());
                buffer.write("CLS\n".getBytes());

                // 2. 遍历图片生成指令
                for (Bitmap bitmap : bitmaps) {
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    int rowBytes = (width + 7) / 8;
                    byte[] data = new byte[rowBytes * height];
                    java.util.Arrays.fill(data, (byte) 0xFF); // 初始化全白

                    // 二值化处理循环
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            int pixel = bitmap.getPixel(x, y);
                            int r = (pixel >> 16) & 0xFF;
                            int g = (pixel >> 8) & 0xFF;
                            int b = pixel & 0xFF;
                            int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);

                            // 【核心修改】阈值从 128 改为 200
                            // 之前：只有很黑的才打印 (gray < 128)
                            // 现在：只要不是纯白，都打印成黑 (gray < 200)
                            // 作用：这会让文字和条码线条变粗，大大提高可扫描性
                            if (gray < 200) {
                                data[y * rowBytes + x / 8] &= ~(128 >> (x % 8));
                            }
                        }
                    }

                    // 每张标签的指令
                    buffer.write("CLS\n".getBytes());
                    String cmd = "BITMAP 0,0," + rowBytes + "," + height + ",0,";
                    buffer.write(cmd.getBytes());
                    buffer.write(data);
                    buffer.write("\n".getBytes());
                    buffer.write(("PRINT " + copies + ",1\n").getBytes());
                }

                // 3. 发送所有数据
                // 如果数据量太大(比如超过100张)，可能需要分包发送，这里暂时一次性发
                int transfer = usbConnection.bulkTransfer(endpointOut, buffer.toByteArray(), buffer.size(), 10000); // 超时给长点
                if (transfer >= 0) {
                    sendPrintStatus(context, true);
                } else {
                    sendPrintStatus(context, false);
                }

            } catch (Exception e) {
                Log.e("PrintError", "批量打印失败", e);
                sendPrintStatus(context, false);
            }
        });
    }


}


