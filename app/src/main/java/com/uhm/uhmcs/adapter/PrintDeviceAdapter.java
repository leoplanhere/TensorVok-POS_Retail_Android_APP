package com.uhm.uhmcs.adapter;

import android.hardware.usb.UsbDevice;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.GrouponGoodsBean;

public class PrintDeviceAdapter extends BaseQuickAdapter<UsbDevice, BaseViewHolder> {

    public PrintDeviceAdapter() {
        super(R.layout.item_print_device);
    }


    @Override
    protected void convert(BaseViewHolder helper, UsbDevice item) {
        helper.setText(R.id.shebeimingcheng_tv,item.getDeviceName());
        helper.setText(R.id.shebeiid_tv,item.getProductId()+"");
        helper.setText(R.id.changshangid_tv,item.getVendorId()+"");
        helper.addOnClickListener(R.id.xuanze_btn);

    }
}
