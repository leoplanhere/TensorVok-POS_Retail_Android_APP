package com.uhm.uhmcs.popupwindow;

import android.app.Activity;
import android.hardware.usb.UsbDevice;
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

        // 计算居中位置
        popupView.post(() -> {
            DisplayMetrics metrics = new DisplayMetrics();
            context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int x = (metrics.widthPixels - popupView.getWidth()) / 2;
            int y = (metrics.heightPixels - popupView.getHeight()) / 2;
            popupWindow.update(x, y, -1, -1);
        });

        popupView.findViewById(R.id.guanbi_btn).setOnClickListener(v -> popupWindow.dismiss());

        print_device_rv = popupView.findViewById(R.id.print_device_rv);
        print_device_rv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        printDeviceAdapter = new PrintDeviceAdapter(type);
        print_device_rv.setAdapter(printDeviceAdapter);

        // 获取当前连接的 USB 设备列表
        List<UsbDevice> deviceList = MyUsbDeviceHelper.getInstance().getDeviceList();
        printDeviceAdapter.setNewData(deviceList);

        printDeviceAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (view.getId() == R.id.xuanze_btn) {
                new DeleteShopPopupWindow(context, context.getString(R.string.Confirm_this_device), new PopupWindowOnClickListener.DeleteShopOnClickListener() {
                    @Override
                    public void onClick(String text) {
                        printDeviceOnClickListener.onClick(printDeviceAdapter.getItem(position));
                        popupWindow.dismiss();
                    }
                }).show();
            }
        });

        // --- 优化：显示当前选中的设备名称 ---
        TextView dqxz_tv = popupView.findViewById(R.id.dqxz_tv);
        if (type == 1) {
            // 普通打印机
            String name = findDeviceName(UserUtils.getInstance().getVENDOR_ID(), UserUtils.getInstance().getPRODUCT_ID(), deviceList);
            dqxz_tv.setText(name);
        } else if (type == 2) {
            // 标签打印机
            String name = findDeviceName(UserUtils.getInstance().getLABEKS_VENDOR_ID(), UserUtils.getInstance().getLABEKS_PRODUCT_ID(), deviceList);
            dqxz_tv.setText(name);
        }





    }

    /**
     * 根据 VID 和 PID 在当前列表中查找设备名称
     */
    private String findDeviceName(int vid, int pid, List<UsbDevice> list) {
        if (vid == 0 && pid == 0) return "未设置";
        for (UsbDevice device : list) {
            if (device.getVendorId() == vid && device.getProductId() == pid) {
                return getDeviceFriendlyName(device);
            }
        }
        return "设备未连接 (ID: " + vid + ")";
    }

    /**
     * 获取易读的设备名称
     */
    public static String getDeviceFriendlyName(UsbDevice device) {
        if (device == null) return "Unknown";
        String name = "";
        // 获取产品名称 (例如: XP-80)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            name = device.getProductName();
        }
        // 如果产品名为空，则尝试获取制造商名 (例如: Xprinter)
        if (name == null || name.isEmpty()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                name = device.getManufacturerName();
            }
        }
        // 如果都获取不到，显示通用的 USB 描述
        if (name == null || name.isEmpty()) {
            name = "USB设备 (" + device.getVendorId() + ")";
        }
        return name;
    }

    public void show() {
        View rootView = context.getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
    }
}