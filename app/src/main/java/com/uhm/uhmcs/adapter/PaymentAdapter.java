package com.uhm.uhmcs.adapter;

import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.GrouponGoodsBean;
import com.uhm.uhmcs.bean.LastOrderBean;

public class PaymentAdapter extends BaseQuickAdapter<LastOrderBean.PaymentlogBean, BaseViewHolder> {

    public PaymentAdapter() {
        super(R.layout.item_payment);
    }


    @Override
    protected void convert(BaseViewHolder helper, LastOrderBean.PaymentlogBean item) {
        String lexing="";
        if (item.getPay_type().equals("cash")){
            lexing="现金";
        }else if (item.getPay_type().equals("alipay")){
            lexing="支付宝";
        }else if (item.getPay_type().equals("wechat")){
            lexing="微信";
        }
        helper.setText(R.id.liushuihao_tv, TextUtils.isEmpty(item.getTransaction_id())?"":item.getTransaction_id());
        helper.setText(R.id.zhifuleixing_tv, lexing);
        if (item.getOrder_status()==4){
            helper.setText(R.id.tuikuan_btn, "已退款");
        } else {
            helper.addOnClickListener(R.id.tuikuan_btn);
            helper.setText(R.id.tuikuan_btn, "退款");
        }
        helper.setText(R.id.jiage_tv,item.getReceivedmoney());

    }
}
