package com.uhm.uhmcs.utils;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;

public class MyUsbDeviceHelper {
    private Context context;
    private static MyUsbDeviceHelper instance;
    private  final String ACTION_USB_PERMISSION = "com.example.USB_PERMISSION";
    private UsbManager usbManager;
    private  final int VENDOR_ID = 1046;  // 替换为你的打印机厂商ID（如芯烨为 1155）
    private  final int PRODUCT_ID = 20497; // 替换为你的打印机产品ID

    /**
     * 获取单件实例
     *
     * @return
     */
    public static MyUsbDeviceHelper getInstance() {
        if (null == instance)
            instance = new MyUsbDeviceHelper();
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
                            /**
                             * 账单打印机
                             */
                            if (device.getVendorId() == VENDOR_ID && device.getProductId() == PRODUCT_ID) { // 替换为实际 VID/PID
                                UsbDeviceConnection usbConnection;
                                usbConnection = usbManager.openDevice(device);
                                MyPrinterHelper.getInstance().connectAndPrint(device,usbConnection);
                            }
                            /**
                             * 标签打印机
                             */
                            if (device.getVendorId() == 8137 && device.getProductId() == 8214) { // 替换为实际 VID/PID
                                UsbDeviceConnection usbConnection;
                                usbConnection = usbManager.openDevice(device);
                                MyLabeksPrinterHelper.getInstance().connectAndPrint(device,usbConnection);
                            }

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
            }
            if (device.getVendorId() == 8137 && device.getProductId() == 8214) { // 替换为实际 VID/PID
                requestUsbPermission(device);
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
}
