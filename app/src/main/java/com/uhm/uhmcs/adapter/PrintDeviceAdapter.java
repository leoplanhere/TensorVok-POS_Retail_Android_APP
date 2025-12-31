package com.uhm.uhmcs.adapter;

import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.utils.UserUtils;

public class PrintDeviceAdapter extends BaseQuickAdapter<UsbDevice, BaseViewHolder> {
    private int type;

    public PrintDeviceAdapter(int type) {
        super(R.layout.item_print_device);
        this.type = type;
    }

    @Override
    protected void convert(BaseViewHolder helper, UsbDevice item) {
        // 1. 获取人性化的设备名称
        String friendlyName = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            friendlyName = item.getProductName(); // 获取产品型号名
            if (TextUtils.isEmpty(friendlyName)) {
                friendlyName = item.getManufacturerName(); // 备选：获取厂家名
            }
        }

        // 2. 如果系统没返回名称，则显示兜底文案
        if (TextUtils.isEmpty(friendlyName)) {
            friendlyName = "USB 打印机 (" + item.getVendorId() + ")";
        }

        // 3. 绑定基本信息
        helper.setText(R.id.shebeimingcheng_tv, friendlyName);
        helper.setText(R.id.shebeiid_tv, String.valueOf(item.getProductId()));
        helper.setText(R.id.changshangid_tv, String.valueOf(item.getVendorId()));

        // 4. 注册点击事件
        helper.addOnClickListener(R.id.xuanze_btn);

        // 5. 判断是否为当前已选中的设备，并更新按钮状态
        boolean isCurrent = false;
        if (type == 1) {
            // 普通小票打印机
            if (item.getVendorId() == UserUtils.getInstance().getVENDOR_ID() &&
                    item.getProductId() == UserUtils.getInstance().getPRODUCT_ID()) {
                isCurrent = true;
            }
        } else {
            // 标签打印机
            if (item.getVendorId() == UserUtils.getInstance().getLABEKS_VENDOR_ID() &&
                    item.getProductId() == UserUtils.getInstance().getLABEKS_PRODUCT_ID()) {
                isCurrent = true;
            }
        }

        // 根据选中状态修改 UI
        if (isCurrent) {
            helper.setText(R.id.xuanze_btn, "当前使用");
            helper.getView(R.id.xuanze_btn).setAlpha(0.5f); // 变淡表示不可重复选
            helper.getView(R.id.xuanze_btn).setEnabled(false);
        } else {
            helper.setText(R.id.xuanze_btn, "选择");
            helper.getView(R.id.xuanze_btn).setAlpha(1.0f);
            helper.getView(R.id.xuanze_btn).setEnabled(true);
        }
    }
}