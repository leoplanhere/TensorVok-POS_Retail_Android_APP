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
                        cmd.append("TEXT " + 125 + "," + 70+ ",\"FONT001\",0,1,1,\"").append(grouponGoodsModel.getTitle()).append("\"\r\n");
                        cmd.append("TEXT " + 80 + "," + 120+ ",\"FONT001\",0,1,1,\"").append(grouponGoodsModel.getSn()).append("\"\r\n");
                        cmd.append("TEXT " + 80 + "," + 165+ ",\"FONT001\",0,1,1,\"").append(grouponGoodsModel.getGoods_sn()).append("\"\r\n");

                        if (!TextUtils.isEmpty(grouponGoodsModel.getGoods_sku_text())){
                            String input =grouponGoodsModel.getGoods_sku_text();
                            String result = input.substring(1, input.length() - 1);
                            cmd.append("TEXT " + 250 + "," + 165+ ",\"FONT001\",0,1,1,\"").append(result).append("\"\r\n");
                        }



                        cmd.append("TEXT " + 120 + "," + 215+ ",\"FONT001\",0,1,1,\"").append(grouponGoodsModel.getCompany()).append("\"\r\n");
                        cmd.append("TEXT " + 80 + "," + 260+ ",\"FONT001\",0,1,1,\"").append("合格品").append("\"\r\n");
                        cmd.append("TEXT " + 270 + "," + 215+ ",\"FONT001\",0,1,1,\"").append("物价员").append("\"\r\n");
//                        cmd.append("TEXT " + 250 + "," + 260+ ",\"FONT001\",0,1,1,\"").append(grouponGoodsModel.getc).append("\"\r\n");
                        cmd.append("TEXT " + 495 + "," + 130+ ",\"FONT001\",0,2,2,\"").append(grouponGoodsModel.getPrice()+"元").append("\"\r\n");
                        cmd.append("TEXT " + 495 + "," + 165+ ",\"FONT001\",0,1,1,\"").append(grouponGoodsModel.getReward_points()+"积分").append("\"\r\n");
                        cmd.append("TEXT " + 495 + "," + 215+ ",\"FONT001\",0,1,1,\"").append(grouponGoodsModel.getDeduction_golive()+"元").append("\"\r\n");
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
}


