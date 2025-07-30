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
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyPrinterHelper {
    private  final ExecutorService printExecutor = Executors.newSingleThreadExecutor();
    private  ByteArrayOutputStream output;



    private UsbDeviceConnection usbConnection;
    private UsbEndpoint endpointOut;



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


    // 连接设备并发送打印指令
    public void connectAndPrint(UsbDevice device, UsbDeviceConnection usbConnection) throws IOException {
        Log.i("ttt",">>>>>>>connectAndPrint>>>>>>");
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
    public  void asyncPrintCheckout(Activity context, CheckoutBean bean, PrintDataBean printDataBean, String xinjin_pice, String weixin_pice, String zhifubao_pice,String order_sn) {
        MyPresentation.showHavePaidView();
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

                if (printDataBean!=null&&!TextUtils.isEmpty(printDataBean.getLogo())){
                    Bitmap Logbitmap=  Glide.with(context)
                            .asBitmap()
                            .load(printDataBean.getLogo())
                            .submit(200, 200) // 控制内存使用
                            .get();
                    Bitmap processed = ImagePrinter.toMonochrome(Logbitmap);
                    output.write(ImagePrinter.convertBitmapToEscPos(processed));
                }
                if (printDataBean!=null&&!TextUtils.isEmpty(printDataBean.getTitle())){
                    output.write((printDataBean.getTitle()+"\n").getBytes(Charset.forName("GBK")));
                }
                if (printDataBean!=null&&!TextUtils.isEmpty(printDataBean.getDescribe())){
                    output.write((printDataBean.getDescribe()+"\n").getBytes(Charset.forName("GBK")));
                }
                if (printDataBean!=null&&!TextUtils.isEmpty(printDataBean.getImage())){
//                    String biaoqingimageUrl="https://ww3.sinaimg.cn/mw690/008emNaGgy1heup6a9g6jj30j60j6my8.jpg";

                    Bitmap biaoqingbitmap=  Glide.with(context)
                            .asBitmap()
                            .load(printDataBean.getImage())
                            .submit(200, 200) // 控制内存使用
                            .get();
                    Bitmap biaoqingprocessed = ImagePrinter.toMonochrome(biaoqingbitmap);
                    output.write(ImagePrinter.convertBitmapToEscPos(biaoqingprocessed));
                }

                output.write(("优道佳家福会员超市"+"\n").getBytes(Charset.forName("GBK")));
                //                output.write(("潍坊市经济开发区玄武街文化路西北角1611号"+"\n").getBytes(Charset.forName("GBK")));
                output.write(("全国客服热线:"+"17560635652"+"\n").getBytes(Charset.forName("GBK")));


//                output.write("潮享三句半\n".getBytes(Charset.forName("GBK")));
//                output.write("上海市静安区大悦城s516\n".getBytes(Charset.forName("GBK")));
//                output.write("全国客服热线:021-37631329\n".getBytes(Charset.forName("GBK")));
                // 左对齐
                output.write(new byte[]{0x1B, 0x61, 0x00});

                if (!TextUtils.isEmpty(checkoutBean.getMember_name())){
                    String huiyuanmingcheng="会员名称:";
                    huiyuanmingcheng+=checkoutBean.getMember_name();
                    huiyuanmingcheng+=(new String(new char[48-18-calculateDisplayWidth(checkoutBean.getMember_name()+Utilis.maskPhone(checkoutBean.getMember_phone()))]).replace('\0', ' '));
                    String huiyuanshouji="会员手机:";
                    huiyuanmingcheng+=(huiyuanshouji+Utilis.maskPhone(checkoutBean.getMember_phone()));
                    output.write(huiyuanmingcheng.getBytes("GBK"));
                    output.write(0x0A); // 换行
                }


                // 定义日期格式模板
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                // 获取当前时间（基于系统时区）
                String formattedTime = sdf.format(System.currentTimeMillis());
//                output.write(("收银时间:"+formattedTime+"\n").getBytes(Charset.forName("GBK")));
                String shouyinshijian="收银时间:"+formattedTime;
                String shouyinyuan="收银员:"+UserUtils.getInstance().getLoginBase().getData().getUserinfo().getNickname();
                String shouyinData=shouyinyuan;
                shouyinData+=(new String(new char[48-calculateDisplayWidth(shouyinyuan+shouyinshijian)]).replace('\0', ' '));
                shouyinData+=shouyinshijian;
                output.write(shouyinData.getBytes("GBK"));
                output.write(0x0A); // 换行
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
                goumaishangpinshulian+=(new String(new char[48-13-calculateDisplayWidth(checkoutBean.getAllNum()+"件")]).replace('\0', ' '));
                goumaishangpinshulian+=(checkoutBean.getAllNum()+"件");
                Log.i("ttt","购买商品数量"+checkoutBean.getAllNum());
                output.write(goumaishangpinshulian.getBytes("GBK"));
                output.write(0x0A); // 换行

                String yingfuzongji="应付总计:";
                yingfuzongji+=(new String(new char[48-9-calculateDisplayWidth(checkoutBean.getTotal_amount()+"")]).replace('\0', ' '));
                yingfuzongji+=(checkoutBean.getTotal_amount()+"");
                output.write(yingfuzongji.getBytes("GBK"));
                output.write(0x0A); // 换行

                String youhuizongji="优惠总计:";
                youhuizongji+=(new String(new char[48-9-calculateDisplayWidth("-"+checkoutBean.getDiscount_fee()+"")]).replace('\0', ' '));
                youhuizongji+=("-"+checkoutBean.getDiscount_fee()+"");
                output.write(youhuizongji.getBytes("GBK"));
                output.write(0x0A); // 换行

                String daijinjuan="代金券抵扣:";
                daijinjuan+=(new String(new char[48-11-calculateDisplayWidth("-"+(TextUtils.isEmpty(checkoutBean.getCoupon_fee())?"0.00":checkoutBean.getCoupon_fee())+"")]).replace('\0', ' '));
                daijinjuan+=("-"+(TextUtils.isEmpty(checkoutBean.getCoupon_fee())?"0.00":checkoutBean.getCoupon_fee())+"");
                output.write(daijinjuan.getBytes("GBK"));
                output.write(0x0A); // 换行
                BigDecimal shifujine_pice=new BigDecimal("0.00");
                if (!TextUtils.isEmpty(xinjin_pice)){
                    shifujine_pice=shifujine_pice.add(new BigDecimal(xinjin_pice));
                }
                if (!TextUtils.isEmpty(weixin_pice)){
                    shifujine_pice=shifujine_pice.add(new BigDecimal(weixin_pice));
                }
                if (!TextUtils.isEmpty(zhifubao_pice)){
                    shifujine_pice=shifujine_pice.add(new BigDecimal(zhifubao_pice));
                }
                String shifujine="实付金额:";
                shifujine+=(new String(new char[48-9-calculateDisplayWidth(shifujine_pice.toString()+"")]).replace('\0', ' '));
                shifujine+=(shifujine_pice.toString()+"");
                output.write(shifujine.getBytes("GBK"));
                output.write(0x0A); // 换行
                String xianjin="现金:";
                String zhifubao="支付宝:";
                String weixin="微信:";


                if (!TextUtils.isEmpty(xinjin_pice)){
                    xianjin+=(new String(new char[48-calculateDisplayWidth(xianjin)-calculateDisplayWidth(xinjin_pice)]).replace('\0', ' '));
                    xianjin+=(xinjin_pice+"");
                    output.write(xianjin.getBytes("GBK"));
                    output.write(0x0A); // 换行
                }
                if (!TextUtils.isEmpty(weixin_pice)){
                    weixin+=(new String(new char[48-calculateDisplayWidth(weixin)-calculateDisplayWidth(weixin_pice)]).replace('\0', ' '));
                    weixin+=(weixin_pice+"");
                    output.write(weixin.getBytes("GBK"));
                    output.write(0x0A); // 换行
                }
                if (!TextUtils.isEmpty(zhifubao_pice)){
                    zhifubao+=(new String(new char[48-calculateDisplayWidth(zhifubao)-calculateDisplayWidth(zhifubao_pice)]).replace('\0', ' '));
                    zhifubao+=(zhifubao_pice+"");
                    output.write(zhifubao.getBytes("GBK"));
                    output.write(0x0A); // 换行
                }
                if (new BigDecimal(checkoutBean.getCash_change()).compareTo(BigDecimal.ZERO)>0){
                    String zhaolin="找零:";
                    zhaolin+=(new String(new char[48-5-calculateDisplayWidth(checkoutBean.getCash_change())]).replace('\0', ' '));
                    zhaolin+=(checkoutBean.getCash_change());
                    output.write(zhaolin.getBytes("GBK"));
                    output.write(0x0A); // 换行
                }


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
                // 设置居中对齐
                output.write(new byte[]{0x1B, 0x61, 0x01});
                output.write(0x0A); // 换行
                if(!TextUtils.isEmpty(order_sn)){
                    Bitmap biaoqingbitmap= generateBarcode(order_sn,BarcodeFormat.CODE_128,440,80);
                    Bitmap biaoqingprocessed = ImagePrinter.toMonochrome(biaoqingbitmap);
                    output.write(ImagePrinter.convertBitmapToEscPos(biaoqingprocessed));
                }
                output.write((order_sn+"\n").getBytes(Charset.forName("GBK")));
                output.write(0x0A); // 换行
                // 左对齐
                output.write(new byte[]{0x1B, 0x61, 0x00});
                output.write("此单据二维码为开具增值税普通发票\n".getBytes(Charset.forName("GBK")));
                output.write("的唯一凭证，请妥善保管。\n".getBytes(Charset.forName("GBK")));
                output.write("请保留此单据，作为退丶换货凭证。\n".getBytes(Charset.forName("GBK")));
                // 设置居中对齐
                output.write(new byte[]{0x1B, 0x61, 0x01});
                if (printDataBean!=null&&!TextUtils.isEmpty(printDataBean.getBottom_remarks())){
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


    /**
     * 异步打印结账   充值
     */
    public  void asyncPrintCheckout(Activity context, String pice, String pay_type) {
        MyPresentation.showHavePaidView();
        printExecutor.execute(() -> {
            try {
                Gson gson=new Gson();

                output = new ByteArrayOutputStream();
                // ESC POS初始化
                output.write(new byte[] { 0x1B, 0x40 });
                // 设置居中对齐
                output.write(new byte[]{0x1B, 0x61, 0x01});
//                String LogimageUrl="https://ww3.sinaimg.cn/mw690/008emNaGgy1heup6a9g6jj30j60j6my8.jpg";





                output.write(("优道佳家福会员超市"+"\n").getBytes(Charset.forName("GBK")));
//                output.write(("潍坊市经济开发区玄武街文化路西北角1611号"+"\n").getBytes(Charset.forName("GBK")));
                output.write(("全国客服热线:"+"17560635652"+"\n").getBytes(Charset.forName("GBK")));


//                output.write("潮享三句半\n".getBytes(Charset.forName("GBK")));
//                output.write("上海市静安区大悦城s516\n".getBytes(Charset.forName("GBK")));
//                output.write("全国客服热线:021-37631329\n".getBytes(Charset.forName("GBK")));
                // 左对齐
                output.write(new byte[]{0x1B, 0x61, 0x00});
                // 定义日期格式模板
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                // 获取当前时间（基于系统时区）
                String formattedTime = sdf.format(System.currentTimeMillis());
//                output.write(("收银时间:"+formattedTime+"\n").getBytes(Charset.forName("GBK")));
                String shouyinshijian="收银时间:"+formattedTime;
                String shouyinyuan="收银员:"+UserUtils.getInstance().getLoginBase().getData().getUserinfo().getNickname();
                String shouyinData=shouyinyuan;
                shouyinData+=(new String(new char[48-calculateDisplayWidth(shouyinyuan+shouyinshijian)]).replace('\0', ' '));
                shouyinData+=shouyinshijian;
                output.write(shouyinData.getBytes("GBK"));
                output.write(0x0A); // 换行
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

                CheckoutBean.GoodsJsonBean goodsJsonBean=new CheckoutBean.GoodsJsonBean();
                goodsJsonBean.setGoods_price(pice);
                goodsJsonBean.setPay_price(pice);
                goodsJsonBean.setGoods_num(1);
                goodsJsonBean.setTitle("会员卡充值");
                buildCheckoutLine(goodsJsonBean,"");

                output.write("------------------------------------------------\n".getBytes(Charset.forName("GBK")));

                output.write(0x0A); // 换行
                String xianjin="现金:";
                String zhifubao="支付宝:";
                String weixin="微信:";

                if (pay_type.equals("cash")){
                    xianjin+=(new String(new char[48-calculateDisplayWidth(xianjin)-calculateDisplayWidth(pice)]).replace('\0', ' '));
                    xianjin+=(pice+"");
                    output.write(xianjin.getBytes("GBK"));
                    output.write(0x0A); // 换行
                }else if (pay_type.equals("alipay")){
                    zhifubao+=(new String(new char[48-calculateDisplayWidth(zhifubao)-calculateDisplayWidth(pice)]).replace('\0', ' '));
                    zhifubao+=(pice+"");
                    output.write(zhifubao.getBytes("GBK"));
                    output.write(0x0A); // 换行
                }else if (pay_type.equals("wechat")){
                    weixin+=(new String(new char[48-calculateDisplayWidth(weixin)-calculateDisplayWidth(pice)]).replace('\0', ' '));
                    weixin+=(pice+"");
                    output.write(weixin.getBytes("GBK"));
                    output.write(0x0A); // 换行
                }


                output.write(0x0A); // 换行
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

                // 左对齐
                output.write(new byte[]{0x1B, 0x61, 0x00});
                output.write("此单据二维码为开具增值税普通发票\n".getBytes(Charset.forName("GBK")));
                output.write("的唯一凭证，请妥善保管。\n".getBytes(Charset.forName("GBK")));
                output.write("请保留此单据，作为退丶换货凭证。\n".getBytes(Charset.forName("GBK")));
                // 设置居中对齐
                output.write(new byte[]{0x1B, 0x61, 0x01});


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
    public  void asyncPrintCheckout(Activity context, CheckoutBean bean, PrintDataBean printDataBean, String xinjin_pice, String weixin_pice, String zhifubao_pice,String huiyuanka_pice,String order_sn) {
        MyPresentation.showHavePaidView();
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

                if (printDataBean!=null&&!TextUtils.isEmpty(printDataBean.getLogo())){
                    Bitmap Logbitmap=  Glide.with(context)
                            .asBitmap()
                            .load(printDataBean.getLogo())
                            .submit(200, 200) // 控制内存使用
                            .get();
                    Bitmap processed = ImagePrinter.toMonochrome(Logbitmap);
                    output.write(ImagePrinter.convertBitmapToEscPos(processed));
                }
                if (printDataBean!=null&&!TextUtils.isEmpty(printDataBean.getTitle())){
                    output.write((printDataBean.getTitle()+"\n").getBytes(Charset.forName("GBK")));
                }
                if (printDataBean!=null&&!TextUtils.isEmpty(printDataBean.getDescribe())){
                    output.write((printDataBean.getDescribe()+"\n").getBytes(Charset.forName("GBK")));
                }
                if (printDataBean!=null&&!TextUtils.isEmpty(printDataBean.getImage())){
//                    String biaoqingimageUrl="https://ww3.sinaimg.cn/mw690/008emNaGgy1heup6a9g6jj30j60j6my8.jpg";

                    Bitmap biaoqingbitmap=  Glide.with(context)
                            .asBitmap()
                            .load(printDataBean.getImage())
                            .submit(200, 200) // 控制内存使用
                            .get();
                    Bitmap biaoqingprocessed = ImagePrinter.toMonochrome(biaoqingbitmap);
                    output.write(ImagePrinter.convertBitmapToEscPos(biaoqingprocessed));
                }

                output.write(("优道佳家福会员超市"+"\n").getBytes(Charset.forName("GBK")));
                //                output.write(("潍坊市经济开发区玄武街文化路西北角1611号"+"\n").getBytes(Charset.forName("GBK")));
                output.write(("全国客服热线:"+"17560635652"+"\n").getBytes(Charset.forName("GBK")));


//                output.write("潮享三句半\n".getBytes(Charset.forName("GBK")));
//                output.write("上海市静安区大悦城s516\n".getBytes(Charset.forName("GBK")));
//                output.write("全国客服热线:021-37631329\n".getBytes(Charset.forName("GBK")));
                // 左对齐
                output.write(new byte[]{0x1B, 0x61, 0x00});
                if (!TextUtils.isEmpty(checkoutBean.getMember_name())){
                    String huiyuanmingcheng="会员名称:";
                    huiyuanmingcheng+=checkoutBean.getMember_name();
                    huiyuanmingcheng+=(new String(new char[48-18-calculateDisplayWidth(checkoutBean.getMember_name()+Utilis.maskPhone(checkoutBean.getMember_phone()))]).replace('\0', ' '));
                    String huiyuanshouji="会员手机:";
                    huiyuanmingcheng+=(huiyuanshouji+Utilis.maskPhone(checkoutBean.getMember_phone()));
                    output.write(huiyuanmingcheng.getBytes("GBK"));
                    output.write(0x0A); // 换行
                }
                // 定义日期格式模板
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                // 获取当前时间（基于系统时区）
                String formattedTime = sdf.format(System.currentTimeMillis());
//                output.write(("收银时间:"+formattedTime+"\n").getBytes(Charset.forName("GBK")));


                String shouyinshijian="收银时间:"+formattedTime;
                String shouyinyuan="收银员:"+UserUtils.getInstance().getLoginBase().getData().getUserinfo().getNickname();
                String shouyinData=shouyinyuan;
                shouyinData+=(new String(new char[48-calculateDisplayWidth(shouyinyuan+shouyinshijian)]).replace('\0', ' '));
                shouyinData+=shouyinshijian;
                output.write(shouyinData.getBytes("GBK"));
                output.write(0x0A); // 换行

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
                goumaishangpinshulian+=(new String(new char[48-13-calculateDisplayWidth(checkoutBean.getAllNum()+"件")]).replace('\0', ' '));
                goumaishangpinshulian+=(checkoutBean.getAllNum()+"件");
                Log.i("ttt","购买商品数量"+checkoutBean.getAllNum());
                output.write(goumaishangpinshulian.getBytes("GBK"));
                output.write(0x0A); // 换行

                String yingfuzongji="应付总计:";
                yingfuzongji+=(new String(new char[48-9-calculateDisplayWidth(checkoutBean.getTotal_amount()+"")]).replace('\0', ' '));
                yingfuzongji+=(checkoutBean.getTotal_amount()+"");
                output.write(yingfuzongji.getBytes("GBK"));
                output.write(0x0A); // 换行

                String youhuizongji="优惠总计:";
                youhuizongji+=(new String(new char[48-9-calculateDisplayWidth("-"+checkoutBean.getDiscount_fee()+"")]).replace('\0', ' '));
                youhuizongji+=("-"+checkoutBean.getDiscount_fee()+"");
                output.write(youhuizongji.getBytes("GBK"));
                output.write(0x0A); // 换行
                String daijinjuan="代金券抵扣:";
                daijinjuan+=(new String(new char[48-11-calculateDisplayWidth("-"+(TextUtils.isEmpty(checkoutBean.getCoupon_fee())?"0.00":checkoutBean.getCoupon_fee())+"")]).replace('\0', ' '));
                daijinjuan+=("-"+(TextUtils.isEmpty(checkoutBean.getCoupon_fee())?"0.00":checkoutBean.getCoupon_fee())+"");
                output.write(daijinjuan.getBytes("GBK"));
                output.write(0x0A); // 换行


                BigDecimal shifujine_pice=new BigDecimal("0.00");
                if (!TextUtils.isEmpty(xinjin_pice)){
                    shifujine_pice=shifujine_pice.add(new BigDecimal(xinjin_pice));
                }
                if (!TextUtils.isEmpty(weixin_pice)){
                    shifujine_pice=shifujine_pice.add(new BigDecimal(weixin_pice));
                }
                if (!TextUtils.isEmpty(zhifubao_pice)){
                    shifujine_pice=shifujine_pice.add(new BigDecimal(zhifubao_pice));
                }
                String shifujine="实付金额:";
                shifujine+=(new String(new char[48-9-calculateDisplayWidth(shifujine_pice.toString()+"")]).replace('\0', ' '));
                shifujine+=(shifujine_pice.toString()+"");
                output.write(shifujine.getBytes("GBK"));
                output.write(0x0A); // 换行
                String xianjin="现金:";
                String zhifubao="支付宝:";
                String weixin="微信:";
                String huiyuanka="会员卡:";


                if (!TextUtils.isEmpty(xinjin_pice)){
                    xianjin+=(new String(new char[48-calculateDisplayWidth(xianjin)-calculateDisplayWidth(xinjin_pice)]).replace('\0', ' '));
                    xianjin+=(xinjin_pice+"");
                    output.write(xianjin.getBytes("GBK"));
                    output.write(0x0A); // 换行
                }
                if (!TextUtils.isEmpty(weixin_pice)){
                    weixin+=(new String(new char[48-calculateDisplayWidth(weixin)-calculateDisplayWidth(weixin_pice)]).replace('\0', ' '));
                    weixin+=(weixin_pice+"");
                    output.write(weixin.getBytes("GBK"));
                    output.write(0x0A); // 换行
                }
                if (!TextUtils.isEmpty(zhifubao_pice)){
                    zhifubao+=(new String(new char[48-calculateDisplayWidth(zhifubao)-calculateDisplayWidth(zhifubao_pice)]).replace('\0', ' '));
                    zhifubao+=(zhifubao_pice+"");
                    output.write(zhifubao.getBytes("GBK"));
                    output.write(0x0A); // 换行
                }
                if (!TextUtils.isEmpty(huiyuanka_pice)){
                    huiyuanka+=(new String(new char[48-calculateDisplayWidth(huiyuanka)-calculateDisplayWidth(huiyuanka_pice)]).replace('\0', ' '));
                    huiyuanka+=(huiyuanka_pice+"");
                    output.write(huiyuanka.getBytes("GBK"));
                    output.write(0x0A); // 换行
                }
                if (new BigDecimal(checkoutBean.getCash_change()).compareTo(BigDecimal.ZERO)>0){
                    String zhaolin="找零:";
                    zhaolin+=(new String(new char[48-5-calculateDisplayWidth(checkoutBean.getCash_change())]).replace('\0', ' '));
                    zhaolin+=(checkoutBean.getCash_change());
                    output.write(zhaolin.getBytes("GBK"));
                    output.write(0x0A); // 换行
                }


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
                // 设置居中对齐
                output.write(new byte[]{0x1B, 0x61, 0x01});
                output.write(0x0A); // 换行
                if(!TextUtils.isEmpty(order_sn)){
                    Bitmap biaoqingbitmap= generateBarcode(order_sn,BarcodeFormat.CODE_128,440,80);
                    Bitmap biaoqingprocessed = ImagePrinter.toMonochrome(biaoqingbitmap);
                    output.write(ImagePrinter.convertBitmapToEscPos(biaoqingprocessed));
                }
                output.write((order_sn+"\n").getBytes(Charset.forName("GBK")));
                output.write(0x0A); // 换行
                // 左对齐
                output.write(new byte[]{0x1B, 0x61, 0x00});
                output.write("此单据二维码为开具增值税普通发票\n".getBytes(Charset.forName("GBK")));
                output.write("的唯一凭证，请妥善保管。\n".getBytes(Charset.forName("GBK")));
                output.write("请保留此单据，作为退丶换货凭证。\n".getBytes(Charset.forName("GBK")));
                // 设置居中对齐
                output.write(new byte[]{0x1B, 0x61, 0x01});
                if (printDataBean!=null&&!TextUtils.isEmpty(printDataBean.getBottom_remarks())){
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

    public  void asyncPrintLastOrder(Activity context,boolean isHistory, LastOrderBean bean, PrintDataBean printDataBean) {
        printExecutor.execute(() -> {
            try {
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
                if (printDataBean!=null&&!TextUtils.isEmpty(printDataBean.getLogo())){
                    Bitmap Logbitmap=  Glide.with(context)
                            .asBitmap()
                            .load(printDataBean.getLogo())
                            .submit(200, 200) // 控制内存使用
                            .get();
                    Bitmap processed = ImagePrinter.toMonochrome(Logbitmap);
                    output.write(ImagePrinter.convertBitmapToEscPos(processed));
                }
                if (printDataBean!=null&&!TextUtils.isEmpty(printDataBean.getTitle())){
                    output.write((printDataBean.getTitle()+"\n").getBytes(Charset.forName("GBK")));
                }
                if (printDataBean!=null&&!TextUtils.isEmpty(printDataBean.getDescribe())){
                    output.write((printDataBean.getDescribe()+"\n").getBytes(Charset.forName("GBK")));
                }
                if (printDataBean!=null&&!TextUtils.isEmpty(printDataBean.getImage())){
//                    String biaoqingimageUrl="https://ww3.sinaimg.cn/mw690/008emNaGgy1heup6a9g6jj30j60j6my8.jpg";

                    Bitmap biaoqingbitmap=  Glide.with(context)
                            .asBitmap()
                            .load(printDataBean.getImage())
                            .submit(200, 200) // 控制内存使用
                            .get();
                    Bitmap biaoqingprocessed = ImagePrinter.toMonochrome(biaoqingbitmap);
                    output.write(ImagePrinter.convertBitmapToEscPos(biaoqingprocessed));
                }



                output.write(("优道佳家福会员超市"+context.getString(R.string.Reprint)+"\n").getBytes(Charset.forName("GBK")));
                //                output.write(("潍坊市经济开发区玄武街文化路西北角1611号"+"\n").getBytes(Charset.forName("GBK")));
                output.write(("全国客服热线:"+"17560635652"+"\n").getBytes(Charset.forName("GBK")));
//                output.write("潮享三句半\n".getBytes(Charset.forName("GBK")));
//                output.write("上海市静安区大悦城s516\n".getBytes(Charset.forName("GBK")));
//                output.write("全国客服热线:021-37631329\n".getBytes(Charset.forName("GBK")));
                // 左对齐
                output.write(new byte[]{0x1B, 0x61, 0x00});
                if (!TextUtils.isEmpty(lastOrderBean.getConsignee())){
                    String huiyuanmingcheng="会员名称:";
                    huiyuanmingcheng+=lastOrderBean.getConsignee();
                    huiyuanmingcheng+=(new String(new char[48-18-calculateDisplayWidth(lastOrderBean.getConsignee()+Utilis.maskPhone(lastOrderBean.getPhone()))]).replace('\0', ' '));
                    String huiyuanshouji="会员手机:";
                    huiyuanmingcheng+=(huiyuanshouji+Utilis.maskPhone(lastOrderBean.getPhone()));
                    output.write(huiyuanmingcheng.getBytes("GBK"));
                    output.write(0x0A); // 换行
                }

                // 定义日期格式模板
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                // 获取当前时间（基于系统时区）
                String formattedTime = sdf.format(new Date(lastOrderBean.getPaytime()* 1000L));
//                output.write(("收银时间:"+formattedTime+"\n").getBytes(Charset.forName("GBK")));
                String shouyinshijian="收银时间:"+formattedTime;
                String shouyinyuan="收银员:"+UserUtils.getInstance().getLoginBase().getData().getUserinfo().getNickname();
                String shouyinData=shouyinyuan;
                shouyinData+=(new String(new char[48-calculateDisplayWidth(shouyinyuan+shouyinshijian)]).replace('\0', ' '));
                shouyinData+=shouyinshijian;
                output.write(shouyinData.getBytes("GBK"));
                output.write(0x0A); // 换行
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
                int allNum=0;
                for (int j=0;j<goodsJsonBeans.size();j++){
                    LastOrderBean.GoodsJsonBean goodsJsonBean=goodsJsonBeans.get(j);
                    allNum+=goodsJsonBean.getGoods_num();
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
                goumaishangpinshulian+=(new String(new char[48-13-calculateDisplayWidth(allNum+"件")]).replace('\0', ' '));
                Log.i("ttt","购买商品数量"+allNum);
                goumaishangpinshulian+=(allNum+"件");
                output.write(goumaishangpinshulian.getBytes("GBK"));
                output.write(0x0A); // 换行

                String yingfuzongji="应付总计:";
                yingfuzongji+=(new String(new char[48-9-calculateDisplayWidth(lastOrderBean.getTotal_amount()+"")]).replace('\0', ' '));
                yingfuzongji+=(lastOrderBean.getTotal_amount()+"");
                output.write(yingfuzongji.getBytes("GBK"));
                output.write(0x0A); // 换行

                String youhuizongji="优惠总计:";
                youhuizongji+=(new String(new char[48-9-calculateDisplayWidth("-"+lastOrderBean.getDiscount_fee()+"")]).replace('\0', ' '));
                youhuizongji+=("-"+lastOrderBean.getDiscount_fee()+"");
                output.write(youhuizongji.getBytes("GBK"));
                output.write(0x0A); // 换行

                String daijinjuan="代金券抵扣:";
                daijinjuan+=(new String(new char[48-11-calculateDisplayWidth("-"+(TextUtils.isEmpty(lastOrderBean.getCoupon_fee())?"0.00":lastOrderBean.getCoupon_fee())+"")]).replace('\0', ' '));
                daijinjuan+=("-"+(TextUtils.isEmpty(lastOrderBean.getCoupon_fee())?"0.00":lastOrderBean.getCoupon_fee())+"");
                output.write(daijinjuan.getBytes("GBK"));
                output.write(0x0A); // 换行

                BigDecimal shifujine_pice=new BigDecimal("0.00");
                String xianjin="";
                String weixin="";
                String zhifubao="";
                String huiyuanka="";
                if (lastOrderBean.getPayment()!=null&&!lastOrderBean.getPayment().isEmpty()){
                    for (LastOrderBean.PaymentlogBean paymentlogBean:lastOrderBean.getPayment()){
                        if (paymentlogBean.getPay_type().equals("cash")){
                            xianjin="现金:";

                            xianjin+=(new String(new char[48-calculateDisplayWidth(xianjin)-calculateDisplayWidth(paymentlogBean.getReceivedmoney()+"")]).replace('\0', ' '));
                            xianjin+=(paymentlogBean.getReceivedmoney()+"");
                        }else if (paymentlogBean.getPay_type().equals("alipay")){
                            zhifubao="支付宝:";
                            zhifubao+=(new String(new char[48-calculateDisplayWidth(zhifubao)-calculateDisplayWidth(paymentlogBean.getReceivedmoney()+"")]).replace('\0', ' '));
                            zhifubao+=(paymentlogBean.getReceivedmoney()+"");
                        }else if (paymentlogBean.getPay_type().equals("wechat")){
                            weixin="微信:";
                            weixin+=(new String(new char[48-calculateDisplayWidth(weixin)-calculateDisplayWidth(paymentlogBean.getReceivedmoney()+"")]).replace('\0', ' '));
                            weixin+=(paymentlogBean.getReceivedmoney()+"");
                        }else if (paymentlogBean.getPay_type().equals("wallet")){
                            huiyuanka="会员卡:";
                            huiyuanka+=(new String(new char[48-calculateDisplayWidth(huiyuanka)-calculateDisplayWidth(paymentlogBean.getReceivedmoney()+"")]).replace('\0', ' '));
                            huiyuanka+=(paymentlogBean.getReceivedmoney()+"");
                        }
                        shifujine_pice=shifujine_pice.add(new BigDecimal(paymentlogBean.getReceivedmoney()));
                    }
                }
                if (lastOrderBean.getPaymentlog()!=null&&!lastOrderBean.getPaymentlog().isEmpty()){
                    for (LastOrderBean.PaymentlogBean paymentlogBean:lastOrderBean.getPaymentlog()){
                        if (paymentlogBean.getPay_type().equals("cash")){
                            xianjin="现金:";
                            xianjin+=(new String(new char[48-calculateDisplayWidth(xianjin)-calculateDisplayWidth(paymentlogBean.getReceivedmoney()+"")]).replace('\0', ' '));
                            xianjin+=(paymentlogBean.getReceivedmoney()+"");
                        }else if (paymentlogBean.getPay_type().equals("alipay")){
                            zhifubao="支付宝:";
                            zhifubao+=(new String(new char[48-calculateDisplayWidth(zhifubao)-calculateDisplayWidth(paymentlogBean.getReceivedmoney()+"")]).replace('\0', ' '));
                            zhifubao+=(paymentlogBean.getReceivedmoney()+"");
                        }else if (paymentlogBean.getPay_type().equals("wechat")){
                            weixin="微信:";
                            weixin+=(new String(new char[48-calculateDisplayWidth(weixin)-calculateDisplayWidth(paymentlogBean.getReceivedmoney()+"")]).replace('\0', ' '));
                            weixin+=(paymentlogBean.getReceivedmoney()+"");
                        }else if (paymentlogBean.getPay_type().equals("wallet")){
                            huiyuanka="会员卡:";
                            huiyuanka+=(new String(new char[48-calculateDisplayWidth(huiyuanka)-calculateDisplayWidth(paymentlogBean.getReceivedmoney()+"")]).replace('\0', ' '));
                            huiyuanka+=(paymentlogBean.getReceivedmoney()+"");
                        }
                        shifujine_pice=shifujine_pice.add(new BigDecimal(paymentlogBean.getReceivedmoney()));

                    }
                }
                String shifujine="实付金额:";
                shifujine+=(new String(new char[48-9-calculateDisplayWidth(shifujine_pice.toString()+"")]).replace('\0', ' '));
                shifujine+=(shifujine_pice.toString()+"");
                output.write(shifujine.getBytes("GBK"));
                output.write(0x0A); // 换行

                if (!TextUtils.isEmpty(xianjin)){
                    output.write(xianjin.getBytes("GBK"));
                    output.write(0x0A); // 换行
                }
                if (!TextUtils.isEmpty(weixin)){
                    output.write(weixin.getBytes("GBK"));
                    output.write(0x0A); // 换行
                }
                if (!TextUtils.isEmpty(zhifubao)){
                    output.write(zhifubao.getBytes("GBK"));
                    output.write(0x0A); // 换行
                }
                if (!TextUtils.isEmpty(huiyuanka)){
                    output.write(huiyuanka.getBytes("GBK"));
                    output.write(0x0A); // 换行
                }

                if (shifujine_pice.subtract(new BigDecimal(lastOrderBean.getTotal_fee())).compareTo(BigDecimal.ZERO)>0){
                    String zhaolin="找零:";
                    zhaolin+=(new String(new char[48-5-calculateDisplayWidth((shifujine_pice.subtract(new BigDecimal(lastOrderBean.getTotal_fee()))).toString())]).replace('\0', ' '));
                    zhaolin+=(shifujine_pice.subtract(new BigDecimal(lastOrderBean.getTotal_fee()))).toString();
                    output.write(zhaolin.getBytes("GBK"));
                    output.write(0x0A); // 换行
                }
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

                // 设置居中对齐
                output.write(new byte[]{0x1B, 0x61, 0x01});
                output.write(0x0A); // 换行
                if(!TextUtils.isEmpty(lastOrderBean.getOrder_sn())){
                    Bitmap biaoqingbitmap= generateBarcode(lastOrderBean.getOrder_sn(),BarcodeFormat.CODE_128,440,80);
                    Bitmap biaoqingprocessed = ImagePrinter.toMonochrome(biaoqingbitmap);
                    output.write(ImagePrinter.convertBitmapToEscPos(biaoqingprocessed));
                }
                output.write((lastOrderBean.getOrder_sn()+"\n").getBytes(Charset.forName("GBK")));
                output.write(0x0A); // 换行
                // 左对齐
                output.write(new byte[]{0x1B, 0x61, 0x00});
                output.write("此单据二维码为开具增值税普通发票\n".getBytes(Charset.forName("GBK")));
                output.write("的唯一凭证，请妥善保管。\n".getBytes(Charset.forName("GBK")));
                output.write("请保留此单据，作为退丶换货凭证。\n".getBytes(Charset.forName("GBK")));
                // 设置居中对齐
                output.write(new byte[]{0x1B, 0x61, 0x01});
                if (printDataBean!=null&&!TextUtils.isEmpty(printDataBean.getBottom_remarks())){
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
                if (printDataBean!=null&&!TextUtils.isEmpty(printDataBean.getLogo())){
                    Bitmap Logbitmap=  Glide.with(context)
                            .asBitmap()
                            .load(printDataBean.getLogo())
                            .submit(200, 200) // 控制内存使用
                            .get();
                    Bitmap processed = ImagePrinter.toMonochrome(Logbitmap);
                    output.write(ImagePrinter.convertBitmapToEscPos(processed));
                }
                if (printDataBean!=null&&!TextUtils.isEmpty(printDataBean.getTitle())){
                    output.write((printDataBean.getTitle()+"\n").getBytes(Charset.forName("GBK")));
                }
                if (printDataBean!=null&&!TextUtils.isEmpty(printDataBean.getDescribe())){
                    output.write((printDataBean.getDescribe()+"\n").getBytes(Charset.forName("GBK")));
                }
                if (printDataBean!=null&&!TextUtils.isEmpty(printDataBean.getImage())){
//                    String biaoqingimageUrl="https://ww3.sinaimg.cn/mw690/008emNaGgy1heup6a9g6jj30j60j6my8.jpg";

                    Bitmap biaoqingbitmap=  Glide.with(context)
                            .asBitmap()
                            .load(printDataBean.getImage())
                            .submit(200, 200) // 控制内存使用
                            .get();
                    Bitmap biaoqingprocessed = ImagePrinter.toMonochrome(biaoqingbitmap);
                    output.write(ImagePrinter.convertBitmapToEscPos(biaoqingprocessed));
                }



                output.write(("优道佳家福会员超市"+"\n").getBytes(Charset.forName("GBK")));
                //                output.write(("潍坊市经济开发区玄武街文化路西北角1611号"+"\n").getBytes(Charset.forName("GBK")));
                output.write(("全国客服热线:"+"17560635652"+"\n").getBytes(Charset.forName("GBK")));
//                output.write("潮享三句半\n".getBytes(Charset.forName("GBK")));
//                output.write("上海市静安区大悦城s516\n".getBytes(Charset.forName("GBK")));
//                output.write("全国客服热线:021-37631329\n".getBytes(Charset.forName("GBK")));
                // 左对齐
                output.write(new byte[]{0x1B, 0x61, 0x00});
                if (!TextUtils.isEmpty(lastOrderBean.getConsignee())){
                    String huiyuanmingcheng="会员名称:";
                    huiyuanmingcheng+=lastOrderBean.getConsignee();
                    huiyuanmingcheng+=(new String(new char[48-18-calculateDisplayWidth(lastOrderBean.getConsignee()+Utilis.maskPhone(lastOrderBean.getPhone()))]).replace('\0', ' '));
                    String huiyuanshouji="会员手机:";
                    huiyuanmingcheng+=(huiyuanshouji+Utilis.maskPhone(lastOrderBean.getPhone()));
                    output.write(huiyuanmingcheng.getBytes("GBK"));
                    output.write(0x0A); // 换行
                }
                // 定义日期格式模板
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                // 获取当前时间（基于系统时区）
                String formattedTime = sdf.format(new Date(lastOrderBean.getPaytime()* 1000L));
//                output.write(("收银时间:"+formattedTime+"\n").getBytes(Charset.forName("GBK")));
                String shouyinshijian="收银时间:"+formattedTime;
                String shouyinyuan="收银员:"+UserUtils.getInstance().getLoginBase().getData().getUserinfo().getNickname();
                String shouyinData=shouyinyuan;
                shouyinData+=(new String(new char[48-calculateDisplayWidth(shouyinyuan+shouyinshijian)]).replace('\0', ' '));
                shouyinData+=shouyinshijian;
                output.write(shouyinData.getBytes("GBK"));
                output.write(0x0A); // 换行
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
                int allNum=0;
                for (int j=0;j<goodsJsonBeans.size();j++){
                    LastOrderBean.GoodsJsonBean goodsJsonBean=goodsJsonBeans.get(j);
                    allNum+=goodsJsonBean.getGoods_num();
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
                goumaishangpinshulian+=(new String(new char[48-13-calculateDisplayWidth(allNum+"件")]).replace('\0', ' '));
                Log.i("ttt","购买商品数量"+allNum);
                goumaishangpinshulian+=(allNum+"件");
                output.write(goumaishangpinshulian.getBytes("GBK"));
                output.write(0x0A); // 换行

                String yingfuzongji="应付总计:";
                yingfuzongji+=(new String(new char[48-9-calculateDisplayWidth(lastOrderBean.getTotal_amount()+"")]).replace('\0', ' '));
                yingfuzongji+=(lastOrderBean.getTotal_amount()+"");
                output.write(yingfuzongji.getBytes("GBK"));
                output.write(0x0A); // 换行

                String youhuizongji="优惠总计:";
                youhuizongji+=(new String(new char[48-9-calculateDisplayWidth("-"+lastOrderBean.getDiscount_fee()+"")]).replace('\0', ' '));
                youhuizongji+=("-"+lastOrderBean.getDiscount_fee()+"");
                output.write(youhuizongji.getBytes("GBK"));
                output.write(0x0A); // 换行
                String daijinjuan="代金券抵扣:";
                daijinjuan+=(new String(new char[48-11-calculateDisplayWidth("-"+(TextUtils.isEmpty(lastOrderBean.getCoupon_fee())?"0.00":lastOrderBean.getCoupon_fee())+"")]).replace('\0', ' '));
                daijinjuan+=("-"+(TextUtils.isEmpty(lastOrderBean.getCoupon_fee())?"0.00":lastOrderBean.getCoupon_fee())+"");
                output.write(daijinjuan.getBytes("GBK"));
                output.write(0x0A); // 换行

                BigDecimal shifujine_pice=new BigDecimal("0.00");
                String xianjin="";
                String weixin="";
                String zhifubao="";
                String huiyuanka="";
                if (lastOrderBean.getPayment()!=null&&!lastOrderBean.getPayment().isEmpty()){
                    for (LastOrderBean.PaymentlogBean paymentlogBean:lastOrderBean.getPayment()){
                        if (paymentlogBean.getPay_type().equals("cash")){
                            xianjin="现金:";

                            xianjin+=(new String(new char[48-calculateDisplayWidth(xianjin)-calculateDisplayWidth(paymentlogBean.getReceivedmoney()+"")]).replace('\0', ' '));
                            xianjin+=(paymentlogBean.getReceivedmoney()+"");
                        }else if (paymentlogBean.getPay_type().equals("alipay")){
                            zhifubao="支付宝:";
                            zhifubao+=(new String(new char[48-calculateDisplayWidth(zhifubao)-calculateDisplayWidth(paymentlogBean.getReceivedmoney()+"")]).replace('\0', ' '));
                            zhifubao+=(paymentlogBean.getReceivedmoney()+"");
                        }else if (paymentlogBean.getPay_type().equals("wechat")){
                            weixin="微信:";
                            weixin+=(new String(new char[48-calculateDisplayWidth(weixin)-calculateDisplayWidth(paymentlogBean.getReceivedmoney()+"")]).replace('\0', ' '));
                            weixin+=(paymentlogBean.getReceivedmoney()+"");
                        }else if (paymentlogBean.getPay_type().equals("wallet")){
                            huiyuanka="会员卡:";
                            huiyuanka+=(new String(new char[48-calculateDisplayWidth(huiyuanka)-calculateDisplayWidth(paymentlogBean.getReceivedmoney()+"")]).replace('\0', ' '));
                            huiyuanka+=(paymentlogBean.getReceivedmoney()+"");
                        }
                        shifujine_pice=shifujine_pice.add(new BigDecimal(paymentlogBean.getReceivedmoney()));
                    }
                }
                if (lastOrderBean.getPaymentlog()!=null&&!lastOrderBean.getPaymentlog().isEmpty()){
                    for (LastOrderBean.PaymentlogBean paymentlogBean:lastOrderBean.getPaymentlog()){
                        if (paymentlogBean.getPay_type().equals("cash")){
                            xianjin="现金:";
                            xianjin+=(new String(new char[48-calculateDisplayWidth(xianjin)-calculateDisplayWidth(paymentlogBean.getReceivedmoney()+"")]).replace('\0', ' '));
                            xianjin+=(paymentlogBean.getReceivedmoney()+"");
                        }else if (paymentlogBean.getPay_type().equals("alipay")){
                            zhifubao="支付宝:";
                            zhifubao+=(new String(new char[48-calculateDisplayWidth(zhifubao)-calculateDisplayWidth(paymentlogBean.getReceivedmoney()+"")]).replace('\0', ' '));
                            zhifubao+=(paymentlogBean.getReceivedmoney()+"");
                        }else if (paymentlogBean.getPay_type().equals("wechat")){
                            weixin="微信:";
                            weixin+=(new String(new char[48-calculateDisplayWidth(weixin)-calculateDisplayWidth(paymentlogBean.getReceivedmoney()+"")]).replace('\0', ' '));
                            weixin+=(paymentlogBean.getReceivedmoney()+"");
                        }else if (paymentlogBean.getPay_type().equals("wallet")){
                            huiyuanka="会员卡:";
                            huiyuanka+=(new String(new char[48-calculateDisplayWidth(huiyuanka)-calculateDisplayWidth(paymentlogBean.getReceivedmoney()+"")]).replace('\0', ' '));
                            huiyuanka+=(paymentlogBean.getReceivedmoney()+"");
                        }
                        shifujine_pice=shifujine_pice.add(new BigDecimal(paymentlogBean.getReceivedmoney()));

                    }
                }
                String shifujine="实付金额:";
                shifujine+=(new String(new char[48-9-calculateDisplayWidth(shifujine_pice.toString()+"")]).replace('\0', ' '));
                shifujine+=(shifujine_pice.toString()+"");
                output.write(shifujine.getBytes("GBK"));
                output.write(0x0A); // 换行

                if (!TextUtils.isEmpty(xianjin)){
                    output.write(xianjin.getBytes("GBK"));
                    output.write(0x0A); // 换行
                }
                if (!TextUtils.isEmpty(weixin)){
                    output.write(weixin.getBytes("GBK"));
                    output.write(0x0A); // 换行
                }
                if (!TextUtils.isEmpty(zhifubao)){
                    output.write(zhifubao.getBytes("GBK"));
                    output.write(0x0A); // 换行
                }
                if (!TextUtils.isEmpty(huiyuanka)){
                    output.write(huiyuanka.getBytes("GBK"));
                    output.write(0x0A); // 换行
                }

                if (shifujine_pice.subtract(new BigDecimal(lastOrderBean.getTotal_fee())).compareTo(BigDecimal.ZERO)>0){
                    String zhaolin="找零:";
                    zhaolin+=(new String(new char[48-5-calculateDisplayWidth((shifujine_pice.subtract(new BigDecimal(lastOrderBean.getTotal_fee()))).toString())]).replace('\0', ' '));
                    zhaolin+=(shifujine_pice.subtract(new BigDecimal(lastOrderBean.getTotal_fee()))).toString();
                    output.write(zhaolin.getBytes("GBK"));
                    output.write(0x0A); // 换行
                }
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

                // 设置居中对齐
                output.write(new byte[]{0x1B, 0x61, 0x01});
                output.write(0x0A); // 换行
                if(!TextUtils.isEmpty(lastOrderBean.getOrder_sn())){
                    Bitmap biaoqingbitmap= generateBarcode(lastOrderBean.getOrder_sn(),BarcodeFormat.CODE_128,440,80);
                    Bitmap biaoqingprocessed = ImagePrinter.toMonochrome(biaoqingbitmap);
                    output.write(ImagePrinter.convertBitmapToEscPos(biaoqingprocessed));
                }
                output.write((lastOrderBean.getOrder_sn()+"\n").getBytes(Charset.forName("GBK")));
                output.write(0x0A); // 换行
                // 左对齐
                output.write(new byte[]{0x1B, 0x61, 0x00});
                output.write("此单据二维码为开具增值税普通发票\n".getBytes(Charset.forName("GBK")));
                output.write("的唯一凭证，请妥善保管。\n".getBytes(Charset.forName("GBK")));
                output.write("请保留此单据，作为退丶换货凭证。\n".getBytes(Charset.forName("GBK")));
                // 设置居中对齐
                output.write(new byte[]{0x1B, 0x61, 0x01});
                if (printDataBean!=null&&!TextUtils.isEmpty(printDataBean.getBottom_remarks())){
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


    public void asyncOpenMoneyBox(Activity context){
        printExecutor.execute(()->{
            try {
                output = new ByteArrayOutputStream();
                // ESC POS初始化
                output.write(new byte[] {0x1B,0x40});
                output.write(new byte[] {0x1B, 0x70, 0x00, 0x60, 0x60});
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

            }catch (Exception e){
                Log.e("PrintError", "打印失败", e);
                sendPrintStatus(context, false);
            }
        });
    }

    /**
     * 打印交接班
     *
     * @return
     */
    public void asyncPrintRelieveShift(Activity context, RelieveShiftPrintBean relieveShiftPrintBean){
        printExecutor.execute(()->{
            try {
                output = new ByteArrayOutputStream();
                // ESC POS初始化
                output.write(new byte[] { 0x1B, 0x40 });
                // 设置居中对齐
                output.write(new byte[]{0x1B, 0x61, 0x01});
                output.write("交接单\n".getBytes(Charset.forName("GBK")));
                output.write("------------------------------------------------\n".getBytes(Charset.forName("GBK")));
                // 设置居左对齐
                output.write(new byte[]{0x1B, 0x61, 0x00});
                // 定义日期格式模板
                SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                // 获取当前时间（基于系统时区）
                String formattedTime = sdf.format(System.currentTimeMillis());
                output.write(("上班时间:"+sdf.format(new Date(relieveShiftPrintBean.getData().getLogintime()* 1000L))+"\n").getBytes(Charset.forName("GBK")));
                output.write(("交班时间:"+sdf.format(new Date(relieveShiftPrintBean.getData().getEndtime()* 1000L))+"\n").getBytes(Charset.forName("GBK")));
                output.write(("营业门店:"+"优道佳家福会员超市"+"\n").getBytes(Charset.forName("GBK")));
                output.write(("收银员:"+relieveShiftPrintBean.getData().getNickname()+"\n").getBytes(Charset.forName("GBK")));
                output.write("------------------------------------------------\n".getBytes(Charset.forName("GBK")));
                output.write(("收款信息"+"\n").getBytes(Charset.forName("GBK")));
                RelieveShiftPrintBean.DataBean dataBean=relieveShiftPrintBean.getData();
                BigDecimal toeal_shoukuan=new BigDecimal(0.00);
                if (!dataBean.getTotal().isEmpty()){
                    for (RelieveShiftPrintBean.DataBean.TotalBean totalBean:dataBean.getTotal()){
                        if (totalBean.getPay_type().equals("cash")){
                            output.write(("现金收款:"+(new String(new char[48-calculateDisplayWidth("现金收款:")-calculateDisplayWidth(totalBean.getTotal()+"")]).replace('\0', ' '))+totalBean.getTotal()+"\n").getBytes(Charset.forName("GBK")));
                        }else if (totalBean.getPay_type().equals("wallet")){
                            output.write(("会员卡收款:"+(new String(new char[48-calculateDisplayWidth("会员卡收款:")-calculateDisplayWidth(totalBean.getTotal()+"")]).replace('\0', ' '))+totalBean.getTotal()+"\n").getBytes(Charset.forName("GBK")));
                        }else if (totalBean.getPay_type().equals("wechat")){
                            output.write(("微信收款:"+(new String(new char[48-calculateDisplayWidth("微信收款:")-calculateDisplayWidth(totalBean.getTotal()+"")]).replace('\0', ' '))+totalBean.getTotal()+"\n").getBytes(Charset.forName("GBK")));
                        }else if (totalBean.getPay_type().equals("alipay")){
                            output.write(("支付宝收款:"+(new String(new char[48-calculateDisplayWidth("支付宝收款:")-calculateDisplayWidth(totalBean.getTotal()+"")]).replace('\0', ' '))+totalBean.getTotal()+"\n").getBytes(Charset.forName("GBK")));
                        }
                        toeal_shoukuan=toeal_shoukuan.add(new BigDecimal(totalBean.getTotal()));
                    }
                }

                output.write(("总收款:"+(new String(new char[48-calculateDisplayWidth("总收款:")-calculateDisplayWidth(toeal_shoukuan.toString())]).replace('\0', ' '))+toeal_shoukuan.toString()+"\n").getBytes(Charset.forName("GBK")));
                output.write("------------------------------------------------\n".getBytes(Charset.forName("GBK")));
                output.write(("退款信息"+"\n").getBytes(Charset.forName("GBK")));
                BigDecimal toeal_tuikuan=new BigDecimal(0.00);
                if (!dataBean.getRefund().isEmpty()){
                    for (RelieveShiftPrintBean.DataBean.RefundBean totalBean:dataBean.getRefund()){
                        if (totalBean.getPay_type().equals("cash")){
                            output.write(("现金退款:"+(new String(new char[48-calculateDisplayWidth("现金退款:")-calculateDisplayWidth(totalBean.getTotal()+"")]).replace('\0', ' '))+totalBean.getTotal()+"\n").getBytes(Charset.forName("GBK")));
                        }else if (totalBean.getPay_type().equals("wallet")){
                            output.write(("会员卡退款:"+(new String(new char[48-calculateDisplayWidth("会员卡退款:")-calculateDisplayWidth(totalBean.getTotal()+"")]).replace('\0', ' '))+totalBean.getTotal()+"\n").getBytes(Charset.forName("GBK")));
                        }else if (totalBean.getPay_type().equals("wechat")){
                            output.write(("微信退款:"+(new String(new char[48-calculateDisplayWidth("微信退款:")-calculateDisplayWidth(totalBean.getTotal()+"")]).replace('\0', ' '))+totalBean.getTotal()+"\n").getBytes(Charset.forName("GBK")));
                        }else if (totalBean.getPay_type().equals("alipay")){
                            output.write(("支付宝退款:"+(new String(new char[48-calculateDisplayWidth("支付宝退款:")-calculateDisplayWidth(totalBean.getTotal()+"")]).replace('\0', ' '))+totalBean.getTotal()+"\n").getBytes(Charset.forName("GBK")));
                        }
                        toeal_tuikuan=toeal_tuikuan.add(new BigDecimal(totalBean.getTotal()));
                    }
                }

                output.write(("总退款:"+(new String(new char[48-calculateDisplayWidth("总退款:")-calculateDisplayWidth(toeal_tuikuan.toString())]).replace('\0', ' '))+toeal_tuikuan.toString()+"\n").getBytes(Charset.forName("GBK")));
                output.write("------------------------------------------------\n".getBytes(Charset.forName("GBK")));
                output.write(0x0A); // 换行
                output.write(("营收总额:"+(new String(new char[48-calculateDisplayWidth("营收总额:")-calculateDisplayWidth(toeal_shoukuan.subtract(toeal_tuikuan).toString())]).replace('\0', ' '))+toeal_shoukuan.subtract(toeal_tuikuan).toString()+"\n").getBytes(Charset.forName("GBK")));
                output.write(0x0A); // 换行
                output.write(0x0A); // 换行
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
                    sendPrintStatus(context, false);

                    throw new IOException("打印数据传输失败");
                }
            }catch (Exception e){
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
//                    new DeleteShopPopupWindow(context,success ? "打印成功" : "打印失败",true).show();
                }
            }
        });
    }
}
