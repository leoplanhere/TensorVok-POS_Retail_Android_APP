package com.uhm.uhmcs.adapter;

import android.graphics.Color;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.PaymentBean;
import com.uhm.uhmcs.bean.RegistrationShopBean;

public class PaymentListAdapter extends BaseQuickAdapter<PaymentBean.DataBean, BaseViewHolder> {

    public PaymentListAdapter() {
        super(R.layout.item_payment_list);

    }
    @Override
    protected void convert(BaseViewHolder helper, PaymentBean.DataBean item) {
        helper.setText(R.id.wx_shanghuhao, item.getMerchant());
        helper.setText(R.id.wx_app_id, item.getSub_app_id());
        helper.setText(R.id.zfb_token, item.getApp_auth_token());
        helper.setText(R.id.is_moren, item.getZh_default()==1?"是":"否");
        helper.addOnClickListener(R.id.edit_btn);
        helper.addOnClickListener(R.id.delete_btn);
    }
}
