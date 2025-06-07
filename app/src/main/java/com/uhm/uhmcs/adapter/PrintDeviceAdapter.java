package com.uhm.uhmcs.adapter;

import android.hardware.usb.UsbDevice;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.GrouponGoodsBean;
import com.uhm.uhmcs.utils.UserUtils;

public class PrintDeviceAdapter extends BaseQuickAdapter<UsbDevice, BaseViewHolder> {
    private int type;
    public PrintDeviceAdapter(int type) {
        super(R.layout.item_print_device);
        this.type = type;
    }


    @Override
    protected void convert(BaseViewHolder helper, UsbDevice item) {
        helper.setText(R.id.shebeimingcheng_tv,item.getDeviceName());
        helper.setText(R.id.shebeiid_tv,item.getProductId()+"");
        helper.setText(R.id.changshangid_tv,item.getVendorId()+"");
        helper.addOnClickListener(R.id.xuanze_btn);
        if (type==1){
            if (item.getVendorId() ==  UserUtils.getInstance().getVENDOR_ID() && item.getProductId() ==  UserUtils.getInstance().getPRODUCT_ID()) { // 替换为实际 VID/PID

            }

        }else {
            if (item.getVendorId() ==  UserUtils.getInstance().getLABEKS_VENDOR_ID() && item.getProductId() ==  UserUtils.getInstance().getLABEKS_PRODUCT_ID()) { // 替换为实际 VID/PID

            }
        }
    }
}
