package com.uhm.uhmcs.popupwindow;

import android.app.Activity;
import android.hardware.usb.UsbDevice; // 必须导入
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.uhm.uhmcs.R;
import com.uhm.uhmcs.adapter.PrintDeviceAdapter;
import com.uhm.uhmcs.utils.MyUsbDeviceHelper;
import com.uhm.uhmcs.utils.UserUtils;

import java.util.List;

public class PrintDevicePopupWindow {
    private PopupWindow popupWindow;
    private Activity context;
    private RecyclerView print_device_rv;
    private PrintDeviceAdapter printDeviceAdapter;
    private PopupWindowOnClickListener.PrintDeviceOnClickListener printDeviceOnClickListener;
    private int type;
    private TextView dqxz_tv; // 提成全局变量，方便更新

    public PrintDevicePopupWindow(Activity context, int type, PopupWindowOnClickListener.PrintDeviceOnClickListener printDeviceOnClickListener) {
        this.context = context;
        this.printDeviceOnClickListener = printDeviceOnClickListener;
        this.type = type;
        initPopup();
    }

    private void initPopup() {
        View popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_print_device, null);
        popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true
        );

        popupView.setBackgroundColor(context.getColor(R.color.black60));
        popupWindow.setOutsideTouchable(true);

        popupView.post(() -> {
            DisplayMetrics metrics = new DisplayMetrics();
            context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int x = (metrics.widthPixels - popupView.getWidth()) / 2;
            int y = (metrics.heightPixels - popupView.getHeight()) / 2;
            popupWindow.update(x, y, -1, -1);
        });

        popupView.findViewById(R.id.guanbi_btn).setOnClickListener(v -> popupWindow.dismiss());

        dqxz_tv = popupView.findViewById(R.id.dqxz_tv);

        // --- 核心修改 1: 初始化时显示当前已保存设备的名称 ---
        updateCurrentSelectionName();

        print_device_rv = popupView.findViewById(R.id.print_device_rv);
        print_device_rv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        printDeviceAdapter = new PrintDeviceAdapter(type);
        print_device_rv.setAdapter(printDeviceAdapter);

        List<UsbDevice> deviceList = MyUsbDeviceHelper.getInstance().getDeviceList();
        printDeviceAdapter.setNewData(deviceList);

        printDeviceAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (view.getId() == R.id.xuanze_btn) {
                UsbDevice selectedDevice = printDeviceAdapter.getItem(position);
                new DeleteShopPopupWindow(context, context.getString(R.string.Confirm_this_device), new PopupWindowOnClickListener.DeleteShopOnClickListener() {
                    @Override
                    public void onClick(String text) {
                        printDeviceOnClickListener.onClick(selectedDevice);

                        // --- 核心修改 2: 点击选择后，立即更新顶部的“当前选择”文本 ---
                        dqxz_tv.setText(getDeviceDisplayName(selectedDevice));

                        popupWindow.dismiss();
                    }
                }).show();
            }
        });
    }

    /**
     * 根据当前保存的 VID 和 PID，从设备列表中找出对应的名称并显示
     */
    private void updateCurrentSelectionName() {
        int savedVid, savedPid;
        if (type == 1) {
            savedVid = UserUtils.getInstance().getVENDOR_ID();
            savedPid = UserUtils.getInstance().getPRODUCT_ID();
        } else {
            savedVid = UserUtils.getInstance().getLABEKS_VENDOR_ID();
            savedPid = UserUtils.getInstance().getLABEKS_PRODUCT_ID();
        }

        if (savedVid <= 0) {
            dqxz_tv.setText("未设置");
            return;
        }

        // 从当前连接的设备列表中寻找匹配的
        List<UsbDevice> deviceList = MyUsbDeviceHelper.getInstance().getDeviceList();
        String foundName = "已保存设备 (未连接)"; // 默认值，如果没插着这个打印机

        for (UsbDevice device : deviceList) {
            if (device.getVendorId() == savedVid && device.getProductId() == savedPid) {
                foundName = getDeviceDisplayName(device);
                break;
            }
        }
        dqxz_tv.setText(foundName);
    }

    /**
     * 统一获取设备名称的方法（保持与 Adapter 一致）
     */
    private String getDeviceDisplayName(UsbDevice device) {
        if (device == null) return "未知";
        String displayName = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String manufacturer = device.getManufacturerName();
            String product = device.getProductName();
            StringBuilder sb = new StringBuilder();
            if (!TextUtils.isEmpty(manufacturer)) sb.append(manufacturer).append(" ");
            if (!TextUtils.isEmpty(product)) sb.append(product);
            displayName = sb.toString().trim();
        }
        return TextUtils.isEmpty(displayName) ?
                "USB设备 (" + device.getVendorId() + ":" + device.getProductId() + ")" : displayName;
    }

    public void show() {
        View rootView = context.getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
    }
}