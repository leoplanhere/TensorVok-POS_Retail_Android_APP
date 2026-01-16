package com.uhm.uhmcs.adapter;

import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.text.TextUtils;

import androidx.core.content.ContextCompat;

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
        // 1. 获取人类可读的设备名称
        String displayName = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String manufacturer = item.getManufacturerName(); // 制造商
            String product = item.getProductName();           // 产品型号

            StringBuilder sb = new StringBuilder();
            if (!TextUtils.isEmpty(manufacturer)) {
                sb.append(manufacturer).append(" ");
            }
            if (!TextUtils.isEmpty(product)) {
                sb.append(product);
            }
            displayName = sb.toString().trim();
        }

        // 如果名称为空（权限未允许或设备未提供），使用 VID:PID 兜底显示，避免显示 /dev/bus/usb...
        if (TextUtils.isEmpty(displayName)) {
            displayName = "USB设备 (" + item.getVendorId() + ":" + item.getProductId() + ")";
        }

        // 2. 设置基础文本信息
        helper.setText(R.id.shebeimingcheng_tv, displayName);
        helper.setText(R.id.shebeiid_tv, String.valueOf(item.getProductId()));
        helper.setText(R.id.changshangid_tv, String.valueOf(item.getVendorId()));
        helper.addOnClickListener(R.id.xuanze_btn);

        // 3. 判断当前设备是否为已选中的设备
        boolean isSelected = false;
        if (type == 1) { // 普通打印机
            if (item.getVendorId() == UserUtils.getInstance().getVENDOR_ID() &&
                    item.getProductId() == UserUtils.getInstance().getPRODUCT_ID()) {
                isSelected = true;
            }
        } else if (type == 2) { // 标签打印机
            if (item.getVendorId() == UserUtils.getInstance().getLABEKS_VENDOR_ID() &&
                    item.getProductId() == UserUtils.getInstance().getLABEKS_PRODUCT_ID()) {
                isSelected = true;
            }
        }

        // 4. 根据选中状态更新 UI 样式
        // 使用 mContext (BaseQuickAdapter 内部变量) 获取资源
        if (isSelected) {
            // 选中状态：蓝色（或 colorAccent）加粗显示，按钮禁用
            int activeColor = ContextCompat.getColor(mContext, R.color.colorAccent);
            helper.setTextColor(R.id.shebeimingcheng_tv, activeColor);
            helper.setText(R.id.xuanze_btn, "当前使用");
            helper.getView(R.id.xuanze_btn).setEnabled(false);
        } else {
            // 未选中状态：常规黑色，按钮可用
            int normalColor = ContextCompat.getColor(mContext, R.color.black);
            helper.setTextColor(R.id.shebeimingcheng_tv, normalColor);
            helper.setText(R.id.xuanze_btn, "选择");
            helper.getView(R.id.xuanze_btn).setEnabled(true);
        }
    }
}