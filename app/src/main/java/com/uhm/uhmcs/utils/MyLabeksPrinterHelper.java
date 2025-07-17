package com.uhm.uhmcs.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.icu.math.BigDecimal;
import android.util.Log;
import android.widget.Toast;

import com.uhm.uhmcs.bean.GrouponGoodsBean;
import com.uhm.uhmcs.popupwindow.DeleteShopPopupWindow;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.RoundingMode;
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
                cmd.append("SIZE 40 mm,30 mm\r\n");   // 标签尺寸
                cmd.append("GAP 2 mm,0 mm\r\n");      // 标签间隙


//        // 添加图片（需转换为单色位图）
//        Bitmap bitmap = convertToMonochrome(BitmapFactory.decodeResource(getResources(), R.drawable.logo));
//        cmd.append("BITMAP ")
//                .append(400).append(",").append(100).append(",")
//                .append(bitmap.getWidth() / 8).append(",")
//                .append(bitmap.getHeight()).append(",0,");
                // 此处需添加位图数据（简化示例，实际需遍历像素生成二进制数据）

//                for (GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel : grouponGoodsModelArrayList) {
//                    for (int j=0;j<num;j++){
//                        cmd.append("CLS\r\n");                // 清空缓冲区
//                        List<String> lines = splitByGbkUnits(grouponGoodsModel.getTitle(), 24);
//                        int currentY = 0;
//                        for (int i = 0; i < lines.size(); i++) {
//                            currentY = 10+ (i * 30);
//
//                            cmd.append("TEXT " + 20 + ",").append(currentY).append(",\"TSS24.BF2\",0,1,1,\"").append(lines.get(i)).append("\"\r\n");
//                        }
//                        // 添加文本
//
//                        cmd.append("TEXT " + 20 + "," + (currentY+30) + ",\"TSS24.BF2\",0,2,2,\"" + "￥").append(grouponGoodsModel.getPrice()).append("\"\r\n");
//                        cmd.append("BARCODE "+20+","+(currentY+90)+",\"128\",80,1,0,2,2,\"").append(grouponGoodsModel.getSn()).append("\"\r\n");
//                        cmd.append("PRINT 1\r\n");
//                    }
//
//
//                }
                for (GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel : grouponGoodsModelArrayList) {
                    for (int j=0;j<num;j++){
                        cmd.append("CLS\r\n");                // 清空缓冲区
                        List<String> lines = splitByGbkUnitsWithRule(grouponGoodsModel.getTitle());
                        int currentY = 0;
                        for (int i = 0; i < lines.size(); i++) {
                            currentY = 6+ (i * 30);

                            cmd.append("TEXT " + 20 + ",").append(currentY).append(",\"TSS24.BF2\",0,1,1,\"").append(lines.get(i)).append("\"\r\n");
                        }
                        // 添加文本

                        cmd.append("TEXT " + 150 + "," + 10 + ",\"TSS24.BF2\",0,2,2,\"" + "￥").append(new BigDecimal(grouponGoodsModel.getPrice()).setScale(1, BigDecimal.ROUND_DOWN).toString()).append("\"\r\n");
                        cmd.append("BARCODE "+20+","+(currentY+30)+",\"128\",80,1,0,2,2,\"").append(grouponGoodsModel.getSn()).append("\"\r\n");
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
     * 差异化截取：前两列10单位，第三列起24单位
     * @param input 输入字符串
     * @return 分块后的字符串列表
     */
    public List<String> splitByGbkUnitsWithRule(String input) {
        List<String> result = new ArrayList<>();
        if (input == null || input.isEmpty()) return result;

        try {
            int[] maxUnits = {12, 12, 24}; // 各列单位限制
            int currentCol = 0;            // 当前列索引
            int currentUnits = 0;          // 当前列累计单位
            int startIndex = 0;            // 当前块起始位置

            for (int i = 0; i < input.length(); i++) {
                char c = input.charAt(i);
                int unit = getGbkCharUnit(c);

                // 列切换逻辑：前两列完成后进入第三列
                if (currentCol < 2 && currentUnits + unit > maxUnits[currentCol]) {
                    result.add(input.substring(startIndex, i));
                    startIndex = i;
                    currentCol++;
                    currentUnits = unit;
                }
                // 第三列超限处理
                else if (currentCol >= 2 && currentUnits + unit > maxUnits[2]) {
                    result.add(input.substring(startIndex, i));
                    startIndex = i;
                    currentUnits = unit;
                } else {
                    currentUnits += unit;
                }
            }

            // 添加剩余内容
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
}


