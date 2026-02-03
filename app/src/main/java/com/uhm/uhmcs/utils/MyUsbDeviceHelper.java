package com.uhm.uhmcs.utils;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyUsbDeviceHelper {
    private Context context;
    private static MyUsbDeviceHelper instance;
    private final String ACTION_USB_PERMISSION = "com.example.USB_PERMISSION";
    private UsbManager usbManager;

    /**
     * 获取单件实例
     */
    public static MyUsbDeviceHelper getInstance() {
        if (null == instance)
            instance = new MyUsbDeviceHelper();
        return instance;
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    public void inti(Context context) {
        this.context = context;
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
                Log.i("ttt", ">>>>sss>>>>>>>>>");
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (device != null) {
                        try {
                            /**
                             * 账单打印机连接逻辑
                             */
                            if (device.getVendorId() == UserUtils.getInstance().getVENDOR_ID() && device.getProductId() == UserUtils.getInstance().getPRODUCT_ID()) {
                                UsbDeviceConnection usbConnection = usbManager.openDevice(device);
                                MyPrinterHelper.getInstance().connectAndPrint(device, usbConnection);
                            }
                            /**
                             * 标签打印机连接逻辑
                             */
                            if (device.getVendorId() == UserUtils.getInstance().getLABEKS_VENDOR_ID() && device.getProductId() == UserUtils.getInstance().getLABEKS_PRODUCT_ID()) {
                                UsbDeviceConnection usbConnection = usbManager.openDevice(device);
                                MyLabeksPrinterHelper.getInstance().connectAndPrint(device, usbConnection);
                            }

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } else {
                    Log.i("ttt", "Permission denied");
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                checkConnectedDevices();
            }
        }
    };

    /**
     * 核心新增：连接设备方法
     * 用于主动获取连接对象
     */
    public UsbDeviceConnection connectDevice(UsbDevice device) {
        if (device == null) return null;
        if (usbManager == null && context != null) {
            usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        }
        if (usbManager == null) return null;

        if (usbManager.hasPermission(device)) {
            return usbManager.openDevice(device);
        } else {
            requestUsbPermission(device);
            return null;
        }
    }

    // 检测已连接的打印机
    public void checkConnectedDevices() {
        if (usbManager == null) return;
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        for (UsbDevice device : deviceList.values()) {
            if (device.getVendorId() == UserUtils.getInstance().getVENDOR_ID() && device.getProductId() == UserUtils.getInstance().getPRODUCT_ID()) {
                requestUsbPermission(device);
            }
            if (device.getVendorId() == UserUtils.getInstance().getLABEKS_VENDOR_ID() && device.getProductId() == UserUtils.getInstance().getLABEKS_PRODUCT_ID()) {
                requestUsbPermission(device);
            }
        }
    }

    // 获取当前在线的打印机列表
    public List<UsbDevice> getDeviceList() {
        List<UsbDevice> usbDeviceList = new ArrayList<>();
        if (usbManager == null) return usbDeviceList;
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        for (UsbDevice device : deviceList.values()) {
            for (int i = 0; i < device.getInterfaceCount(); i++) {
                UsbInterface usbInterface = device.getInterface(i);
                if (usbInterface.getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER) {
                    usbDeviceList.add(device);
                    break;
                }
            }
        }
        return usbDeviceList;
    }

    // 请求 USB 权限
    public void requestUsbPermission(UsbDevice device) {
        if (context == null || usbManager == null) return;
        PendingIntent permissionIntent = PendingIntent.getBroadcast(
                context, 0, new Intent(ACTION_USB_PERMISSION), android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S ? PendingIntent.FLAG_MUTABLE : 0
        );
        usbManager.requestPermission(device, permissionIntent);
    }

    public void unregisterReceiver() {
        if (context != null) context.unregisterReceiver(usbReceiver);
    }
}