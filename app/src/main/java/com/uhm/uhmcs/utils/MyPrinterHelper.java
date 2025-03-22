package com.uhm.uhmcs.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uhm.uhmcs.bean.CheckoutBean;
import com.uhm.uhmcs.bean.LastOrderBean;
import com.uhm.uhmcs.bean.LoginBase;
import com.uhm.uhmcs.bean.PrintDataBean;
import com.uhm.uhmcs.bean.ShopDataBean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyPrinterHelper {
    private  final ExecutorService printExecutor = Executors.newSingleThreadExecutor();
    private  ByteArrayOutputStream output;


    private  final int VENDOR_ID = 1046;  // 替换为你的打印机厂商ID（如芯烨为 1155）
    private  final int PRODUCT_ID = 20497; // 替换为你的打印机产品ID
    private  final String ACTION_USB_PERMISSION = "com.example.USB_PERMISSION";
    private UsbManager usbManager;
    private UsbDeviceConnection usbConnection;
    private UsbEndpoint endpointOut;
    private UsbDevice printer;

    private Context context;

    private static MyPrinterHelper instance;

    /**
     * 获取单件实例
     *
     * @return
     */
    public static MyPrinterHelper getInstance() {
        if (null == instance)
            instance = new MyPrinterHelper();
        return instance;
    }
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    public void inti(Context context) {
       this.context=context;
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        context.registerReceiver(usbReceiver, filter);
        checkConnectedDevices(); // 初始化检测已连接设备

    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                Log.i("ttt",">>>>sss>>>>>>>>>");
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (device != null) {
                        try {
                            printer=device;
                            connectAndPrint(device); // 连接并打印
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }else {
                    Log.i("ttt",">>>>>>>>>>>>>");
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                checkConnectedDevices();
            }
        }
    };
    // 检测已连接的打印机
    private void checkConnectedDevices() {
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        for (UsbDevice device : deviceList.values()) {
            Log.i("ttt",">>>>qqqqq>>>>>>>>>"+device.getVendorId()+"asdadad"+device.getProductId());
            if (device.getVendorId() == VENDOR_ID && device.getProductId() == PRODUCT_ID) { // 替换为实际 VID/PID
                requestUsbPermission(device);
                break;
            }
        }
    }

    // 请求 USB 权限
    private void requestUsbPermission(UsbDevice device) {
        Log.i("ttt",">>>>ssssssssaaaaaaaaaa>>>>>>>>>");
        PendingIntent permissionIntent = PendingIntent.getBroadcast(
                context, 0, new Intent(ACTION_USB_PERMISSION), android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S ? PendingIntent.FLAG_MUTABLE : 0
        );
        usbManager.requestPermission(device, permissionIntent);


    }

    // 连接设备并发送打印指令
    private void connectAndPrint(UsbDevice device) throws IOException {
        usbConnection = usbManager.openDevice(device);
        UsbInterface usbInterface = device.getInterface(0);
        usbConnection.claimInterface(usbInterface, true);

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
    public  void asyncPrintCheckout(Activity context, CheckoutBean bean, PrintDataBean printDataBean) {
        printExecutor.execute(() -> {
            try {
                Gson gson=new Gson();
                CheckoutBean checkoutBean;
                ArrayList<CheckoutBean.GoodsJsonBean> goodsJsonBeanArrayList;
                checkoutBean=bean;
                // 关键：通过 TypeToken 保留泛型信息
                goodsJsonBeanArrayList = gson.fromJson(bean.getGoodsjson(),new TypeToken<ArrayList<CheckoutBean.GoodsJsonBean>>(){}.getType());
                output = new ByteArrayOutputStream();
                // ESC POS初始化
                output.write(new byte[] { 0x1B, 0x40 });
                // 设置居中对齐
                output.write(new byte[]{0x1B, 0x61, 0x01});
//                String LogimageUrl="https://ww3.sinaimg.cn/mw690/008emNaGgy1heup6a9g6jj30j60j6my8.jpg";
                if (!TextUtils.isEmpty(printDataBean.getLogo())){
                    Bitmap Logbitmap=  Glide.with(context)
                            .asBitmap()
                            .load(printDataBean.getLogo())
                            .submit(200, 200) // 控制内存使用
                            .get();
                    Bitmap processed = ImagePrinter.toMonochrome(Logbitmap);
                    output.write(ImagePrinter.convertBitmapToEscPos(processed));
                }
                if (!TextUtils.isEmpty(printDataBean.getTitle())){
                    output.write((printDataBean.getTitle()+"\n").getBytes(Charset.forName("GBK")));
                }
                if (!TextUtils.isEmpty(printDataBean.getDescribe())){
                    output.write((printDataBean.getDescribe()+"\n").getBytes(Charset.forName("GBK")));
                }
                if (!TextUtils.isEmpty(printDataBean.getImage())){
//                    String biaoqingimageUrl="https://ww3.sinaimg.cn/mw690/008emNaGgy1heup6a9g6jj30j60j6my8.jpg";

                    Bitmap biaoqingbitmap=  Glide.with(context)
                            .asBitmap()
                            .load(printDataBean.getImage())
                            .submit(200, 200) // 控制内存使用
                            .get();
                    Bitmap biaoqingprocessed = ImagePrinter.toMonochrome(biaoqingbitmap);
                    output.write(ImagePrinter.convertBitmapToEscPos(biaoqingprocessed));
                }



                output.write((UserUtils.getInstance().getShopDataBean().getData().get(0).getName()+"\n").getBytes(Charset.forName("GBK")));
                output.write((UserUtils.getInstance().getShopDataBean().getData().get(0).getAddress()+"\n").getBytes(Charset.forName("GBK")));
                output.write(("全国客服热线:"+UserUtils.getInstance().getShopDataBean().getData().get(0).getPhone()+"\n").getBytes(Charset.forName("GBK")));
                // 左对齐
                output.write(new byte[]{0x1B, 0x61, 0x00});
                // 定义日期格式模板
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                // 获取当前时间（基于系统时区）
                String formattedTime = sdf.format(System.currentTimeMillis());
                output.write(("收银时间:"+formattedTime+"\n").getBytes(Charset.forName("GBK")));
                output.write("------------------------------------------------\n".getBytes(Charset.forName("GBK")));
                String title="商品名称";
                title+=(new String(new char[18-8]).replace('\0', ' '));
                title+="数量";
                title+=(new String(new char[12-4]).replace('\0', ' '));
                title+=("单价");
                title+=(new String(new char[8-4]).replace('\0', ' '));
                title+=(new String(new char[10-4]).replace('\0', ' '));
                title+="金额";
                output.write(title.getBytes("GBK"));
                output.write(0x0A); // 换行
                output.write("------------------------------------------------\n".getBytes(Charset.forName("GBK")));
                for (int j=0;j<goodsJsonBeanArrayList.size();j++){
                    CheckoutBean.GoodsJsonBean goodsJsonBean=goodsJsonBeanArrayList.get(j);
                    if (calculateDisplayWidth((j+1)+" "+goodsJsonBean.getTitle())>14){
                        List<String>  strings=splitByGbkUnits((j+1)+" "+goodsJsonBean.getTitle(),14);
                        for (int i=0;i<strings.size();i++){
                            if (i==strings.size()-1){
                                buildCheckoutLine(goodsJsonBean,strings.get(i));
                            }else {
                                output.write((strings.get(i)+"\n").getBytes(Charset.forName("GBK")));
                            }

                        }
                    }else {
                        buildCheckoutLine(goodsJsonBean,"");
                    }
                }
                output.write("------------------------------------------------\n".getBytes(Charset.forName("GBK")));
                String goumaishangpinshulian="购买商品数量:";
                goumaishangpinshulian+=(new String(new char[48-13-calculateDisplayWidth(goodsJsonBeanArrayList.size()+"件")]).replace('\0', ' '));
                goumaishangpinshulian+=(goodsJsonBeanArrayList.size()+"件");
                output.write(goumaishangpinshulian.getBytes("GBK"));
                output.write(0x0A); // 换行

                String yingfuzongji="应付总计:";
                yingfuzongji+=(new String(new char[48-9-calculateDisplayWidth(checkoutBean.getTotal_amount()+"")]).replace('\0', ' '));
                yingfuzongji+=(checkoutBean.getTotal_amount()+"");
                output.write(yingfuzongji.getBytes("GBK"));
                output.write(0x0A); // 换行

                String youhuizongji="优惠总计:";
                youhuizongji+=(new String(new char[48-9-calculateDisplayWidth(checkoutBean.getDiscount_fee()+"")]).replace('\0', ' '));
                youhuizongji+=(checkoutBean.getDiscount_fee()+"");
                output.write(youhuizongji.getBytes("GBK"));
                output.write(0x0A); // 换行

                String shifujine="实付金额:";
                shifujine+=(new String(new char[48-9-calculateDisplayWidth(checkoutBean.getTotal_fee()+"")]).replace('\0', ' '));
                shifujine+=(checkoutBean.getTotal_fee()+"");
                output.write(shifujine.getBytes("GBK"));
                output.write(0x0A); // 换行
                String xianjin="";
                if (checkoutBean.getPay_type().equals("cash")){
                    xianjin="现金:";
                }

                xianjin+=(new String(new char[48-calculateDisplayWidth(xianjin)-calculateDisplayWidth(checkoutBean.getPay_fee()+"")]).replace('\0', ' '));
                xianjin+=(checkoutBean.getPay_fee()+"");
                output.write(xianjin.getBytes("GBK"));
                output.write(0x0A); // 换行

                String zhaolin="找零:";
                zhaolin+=(new String(new char[48-5-calculateDisplayWidth(checkoutBean.getCash_change())]).replace('\0', ' '));
                zhaolin+=(checkoutBean.getCash_change());
                output.write(zhaolin.getBytes("GBK"));
                output.write(0x0A); // 换行
//                // 设置居中对齐
//                output.write(new byte[]{0x1B, 0x61, 0x01});
//                String erweimaimageUrl="https://img1.baidu.com/it/u=1999126764,447372295&fm=253&fmt=auto&app=138&f=GIF?w=500&h=500";
//
//                Bitmap erweimabitmap=  Glide.with(context)
//                        .asBitmap()
//                        .load(erweimaimageUrl)
//                        .submit(160, 160) // 控制内存使用
//                        .get();
//                Bitmap erweimaprocessed = ImagePrinter.toMonochrome(erweimabitmap);
//                output.write(ImagePrinter.convertBitmapToEscPos(erweimaprocessed));
//                // 左对齐
//                output.write(new byte[]{0x1B, 0x61, 0x00});
                output.write("此单据二维码为开具增值税普通发票\n".getBytes(Charset.forName("GBK")));
                output.write("的唯一凭证，请妥善保管。\n".getBytes(Charset.forName("GBK")));
                output.write("请保留此单据，作为退丶换货凭证。\n".getBytes(Charset.forName("GBK")));
                // 设置居中对齐
                output.write(new byte[]{0x1B, 0x61, 0x01});
                if (!TextUtils.isEmpty(printDataBean.getBottom_remarks())){
                    output.write((printDataBean.getBottom_remarks()+"\n").getBytes(Charset.forName("GBK")));
                }

                // 走纸和切纸
                output.write(new byte[] { 0x1D, 0x56, 0x42, 0x30 });
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
            } catch (Exception e) {
                Log.e("PrintError", "打印失败", e);
                sendPrintStatus(context, false);
            }
        });
    }


    public  void asyncPrintLastOrder(Activity context, LastOrderBean bean, PrintDataBean printDataBean) {
        printExecutor.execute(() -> {
            try {
                Gson gson=new Gson();
                LastOrderBean lastOrderBean;
                ArrayList<LastOrderBean.GoodsJsonBean> goodsJsonBeans;
                lastOrderBean=bean;
                // 关键：通过 TypeToken 保留泛型信息
                goodsJsonBeans = lastOrderBean.getOrder_item();
                output = new ByteArrayOutputStream();
                // ESC POS初始化
                output.write(new byte[] { 0x1B, 0x40 });
                // 设置居中对齐
                output.write(new byte[]{0x1B, 0x61, 0x01});
//                String LogimageUrl="https://ww3.sinaimg.cn/mw690/008emNaGgy1heup6a9g6jj30j60j6my8.jpg";
                if (!TextUtils.isEmpty(printDataBean.getLogo())){
                    Bitmap Logbitmap=  Glide.with(context)
                            .asBitmap()
                            .load(printDataBean.getLogo())
                            .submit(200, 200) // 控制内存使用
                            .get();
                    Bitmap processed = ImagePrinter.toMonochrome(Logbitmap);
                    output.write(ImagePrinter.convertBitmapToEscPos(processed));
                }
                if (!TextUtils.isEmpty(printDataBean.getTitle())){
                    output.write((printDataBean.getTitle()+"\n").getBytes(Charset.forName("GBK")));
                }
                if (!TextUtils.isEmpty(printDataBean.getDescribe())){
                    output.write((printDataBean.getDescribe()+"\n").getBytes(Charset.forName("GBK")));
                }
                if (!TextUtils.isEmpty(printDataBean.getImage())){
//                    String biaoqingimageUrl="https://ww3.sinaimg.cn/mw690/008emNaGgy1heup6a9g6jj30j60j6my8.jpg";

                    Bitmap biaoqingbitmap=  Glide.with(context)
                            .asBitmap()
                            .load(printDataBean.getImage())
                            .submit(200, 200) // 控制内存使用
                            .get();
                    Bitmap biaoqingprocessed = ImagePrinter.toMonochrome(biaoqingbitmap);
                    output.write(ImagePrinter.convertBitmapToEscPos(biaoqingprocessed));
                }



                output.write((UserUtils.getInstance().getShopDataBean().getData().get(0).getName()+"\n").getBytes(Charset.forName("GBK")));
                output.write((UserUtils.getInstance().getShopDataBean().getData().get(0).getAddress()+"\n").getBytes(Charset.forName("GBK")));
                output.write(("全国客服热线:"+UserUtils.getInstance().getShopDataBean().getData().get(0).getPhone()+"\n").getBytes(Charset.forName("GBK")));
                // 左对齐
                output.write(new byte[]{0x1B, 0x61, 0x00});
                // 定义日期格式模板
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                // 获取当前时间（基于系统时区）
                String formattedTime = sdf.format(System.currentTimeMillis());
                output.write(("收银时间:"+formattedTime+"\n").getBytes(Charset.forName("GBK")));
                output.write("------------------------------------------------\n".getBytes(Charset.forName("GBK")));
                String title="商品名称";
                title+=(new String(new char[18-8]).replace('\0', ' '));
                title+="数量";
                title+=(new String(new char[12-4]).replace('\0', ' '));
                title+=("单价");
                title+=(new String(new char[8-4]).replace('\0', ' '));
                title+=(new String(new char[10-4]).replace('\0', ' '));
                title+="金额";
                output.write(title.getBytes("GBK"));
                output.write(0x0A); // 换行
                output.write("------------------------------------------------\n".getBytes(Charset.forName("GBK")));
                for (int j=0;j<goodsJsonBeans.size();j++){
                    LastOrderBean.GoodsJsonBean goodsJsonBean=goodsJsonBeans.get(j);
                    if (calculateDisplayWidth((j+1)+" "+goodsJsonBean.getTitle())>14){
                        List<String>  strings=splitByGbkUnits((j+1)+" "+goodsJsonBean.getTitle(),14);
                        for (int i=0;i<strings.size();i++){
                            if (i==strings.size()-1){
                                buildLastOrderLine(goodsJsonBean,strings.get(i));
                            }else {
                                output.write((strings.get(i)+"\n").getBytes(Charset.forName("GBK")));
                            }

                        }
                    }else {
                        buildLastOrderLine(goodsJsonBean,"");
                    }
                }
                output.write("------------------------------------------------\n".getBytes(Charset.forName("GBK")));
                String goumaishangpinshulian="购买商品数量:";
                goumaishangpinshulian+=(new String(new char[48-13-calculateDisplayWidth(goodsJsonBeans.size()+"件")]).replace('\0', ' '));
                goumaishangpinshulian+=(goodsJsonBeans.size()+"件");
                output.write(goumaishangpinshulian.getBytes("GBK"));
                output.write(0x0A); // 换行

                String yingfuzongji="应付总计:";
                yingfuzongji+=(new String(new char[48-9-calculateDisplayWidth(lastOrderBean.getTotal_amount()+"")]).replace('\0', ' '));
                yingfuzongji+=(lastOrderBean.getTotal_amount()+"");
                output.write(yingfuzongji.getBytes("GBK"));
                output.write(0x0A); // 换行

                String youhuizongji="优惠总计:";
                youhuizongji+=(new String(new char[48-9-calculateDisplayWidth(lastOrderBean.getDiscount_fee()+"")]).replace('\0', ' '));
                youhuizongji+=(lastOrderBean.getDiscount_fee()+"");
                output.write(youhuizongji.getBytes("GBK"));
                output.write(0x0A); // 换行

                String shifujine="实付金额:";
                shifujine+=(new String(new char[48-9-calculateDisplayWidth(lastOrderBean.getTotal_fee()+"")]).replace('\0', ' '));
                shifujine+=(lastOrderBean.getTotal_fee()+"");
                output.write(shifujine.getBytes("GBK"));
                output.write(0x0A); // 换行
                String xianjin="";
                if (lastOrderBean.getPay_type().equals("cash")){
                    xianjin="现金:";
                }

                xianjin+=(new String(new char[48-calculateDisplayWidth(xianjin)-calculateDisplayWidth(lastOrderBean.getPay_fee()+"")]).replace('\0', ' '));
                xianjin+=(lastOrderBean.getPay_fee()+"");
                output.write(xianjin.getBytes("GBK"));
                output.write(0x0A); // 换行

                String zhaolin="找零:";
                zhaolin+=(new String(new char[48-5-calculateDisplayWidth((lastOrderBean.getPay_fee()-lastOrderBean.getTotal_fee())+"")]).replace('\0', ' '));
                zhaolin+=((lastOrderBean.getPay_fee()-lastOrderBean.getTotal_fee())+"");
                output.write(zhaolin.getBytes("GBK"));
                output.write(0x0A); // 换行
//                // 设置居中对齐
//                output.write(new byte[]{0x1B, 0x61, 0x01});
//                String erweimaimageUrl="https://img1.baidu.com/it/u=1999126764,447372295&fm=253&fmt=auto&app=138&f=GIF?w=500&h=500";
//
//                Bitmap erweimabitmap=  Glide.with(context)
//                        .asBitmap()
//                        .load(erweimaimageUrl)
//                        .submit(160, 160) // 控制内存使用
//                        .get();
//                Bitmap erweimaprocessed = ImagePrinter.toMonochrome(erweimabitmap);
//                output.write(ImagePrinter.convertBitmapToEscPos(erweimaprocessed));
//                // 左对齐
//                output.write(new byte[]{0x1B, 0x61, 0x00});
                output.write("此单据二维码为开具增值税普通发票\n".getBytes(Charset.forName("GBK")));
                output.write("的唯一凭证，请妥善保管。\n".getBytes(Charset.forName("GBK")));
                output.write("请保留此单据，作为退丶换货凭证。\n".getBytes(Charset.forName("GBK")));
                // 设置居中对齐
                output.write(new byte[]{0x1B, 0x61, 0x01});
                if (!TextUtils.isEmpty(printDataBean.getBottom_remarks())){
                    output.write((printDataBean.getBottom_remarks()+"\n").getBytes(Charset.forName("GBK")));
                }

                // 走纸和切纸
                output.write(new byte[] { 0x1D, 0x56, 0x42, 0x30 });
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
            } catch (Exception e) {
                Log.e("PrintError", "打印失败", e);
                sendPrintStatus(context, false);
            }
        });
    }

    // 计算字符串的显示宽度（中文算2，英文算1）
    private  int calculateDisplayWidth(String str) {
        int width = 0;
        for (char c : str.toCharArray()) {
            width += (c < 128) ? 1 : 2;
        }
        return width;
    }
    public  void buildCheckoutLine(CheckoutBean.GoodsJsonBean goodsJsonBean, String name) throws IOException {
        int nameWidth;
        int numWidth,priceWidth,allPriceWidth;
        String line="";
        if (TextUtils.isEmpty(name)){
            nameWidth = calculateDisplayWidth(goodsJsonBean.getTitle());
            // 生成填充空格并构造完整行
            line+=(goodsJsonBean.getTitle());
        }else {
            nameWidth = calculateDisplayWidth(name);
            // 生成填充空格并构造完整行
            line+=(name);
        }
        numWidth = calculateDisplayWidth("x"+goodsJsonBean.getGoods_num());
        priceWidth = calculateDisplayWidth(goodsJsonBean.getGoods_price());
        allPriceWidth = calculateDisplayWidth(goodsJsonBean.getPay_price());

        Log.i("ttt",">>>>>"+nameWidth+">>>"+numWidth+">>>"+priceWidth+">>>"+allPriceWidth+">>>");
        line+=(new String(new char[18-nameWidth]).replace('\0', ' '));
        line+=("x"+goodsJsonBean.getGoods_num());
        line+=(new String(new char[12-numWidth]).replace('\0', ' '));
        line+=(goodsJsonBean.getGoods_price());
        line+=(new String(new char[8-priceWidth]).replace('\0', ' '));
        line+=(new String(new char[10-allPriceWidth]).replace('\0', ' '));
        line+=(goodsJsonBean.getPay_price());
        output.write(line.getBytes("GBK"));
        output.write(0x0A); // 换行

    }
    public  void buildLastOrderLine(LastOrderBean.GoodsJsonBean goodsJsonBean, String name) throws IOException {
        int nameWidth;
        int numWidth,priceWidth,allPriceWidth;
        String line="";
        if (TextUtils.isEmpty(name)){
            nameWidth = calculateDisplayWidth(goodsJsonBean.getTitle());
            // 生成填充空格并构造完整行
            line+=(goodsJsonBean.getTitle());
        }else {
            nameWidth = calculateDisplayWidth(name);
            // 生成填充空格并构造完整行
            line+=(name);
        }
        numWidth = calculateDisplayWidth("x"+goodsJsonBean.getGoods_num());
        priceWidth = calculateDisplayWidth(goodsJsonBean.getGoods_price());
        allPriceWidth = calculateDisplayWidth(goodsJsonBean.getPay_price());

        Log.i("ttt",">>>>>"+nameWidth+">>>"+numWidth+">>>"+priceWidth+">>>"+allPriceWidth+">>>");
        line+=(new String(new char[18-nameWidth]).replace('\0', ' '));
        line+=("x"+goodsJsonBean.getGoods_num());
        line+=(new String(new char[12-numWidth]).replace('\0', ' '));
        line+=(goodsJsonBean.getGoods_price());
        line+=(new String(new char[8-priceWidth]).replace('\0', ' '));
        line+=(new String(new char[10-allPriceWidth]).replace('\0', ' '));
        line+=(goodsJsonBean.getPay_price());
        output.write(line.getBytes("GBK"));
        output.write(0x0A); // 换行

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

    /**
     * 状态回调方法（主线程执行）
     */
    private  void sendPrintStatus(Activity context, boolean success) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (context instanceof Activity) {
                    Toast.makeText(context,
                            success ? "打印成功" : "打印失败",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
