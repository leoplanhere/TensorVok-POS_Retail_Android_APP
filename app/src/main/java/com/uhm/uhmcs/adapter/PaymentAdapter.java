package com.uhm.uhmcs.adapter;

import android.content.Context;
import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.GrouponGoodsBean;
import com.uhm.uhmcs.bean.LastOrderBean;

public class PaymentAdapter extends BaseQuickAdapter<LastOrderBean.PaymentlogBean, BaseViewHolder> {
    Context context;
    public PaymentAdapter(Context context) {
        super(R.layout.item_payment);
        this.context=context;
    }


    @Override
    protected void convert(BaseViewHolder helper, LastOrderBean.PaymentlogBean item) {
        String lexing="";
        if (item.getPay_type().equals("cash")){
            lexing=context.getString(R.string.cash);
        }else if (item.getPay_type().equals("alipay")){
            lexing=context.getString(R.string.alipay);
        }else if (item.getPay_type().equals("wechat")){
            lexing=context.getString(R.string.wechat_pay);
        }
        helper.setText(R.id.liushuihao_tv, TextUtils.isEmpty(item.getTransaction_id())?"":item.getTransaction_id());
        helper.setText(R.id.zhifuleixing_tv, lexing);
        if (item.getOrder_status()==4){
            helper.setText(R.id.tuikuan_btn, context.getString(R.string.Refunded));
        } else {
            helper.addOnClickListener(R.id.tuikuan_btn);
            helper.setText(R.id.tuikuan_btn, context.getString(R.string.refund));
        }
        helper.setText(R.id.jiage_tv,item.getReceivedmoney());

    }
}
