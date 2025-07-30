package com.uhm.uhmcs.adapter;

import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.LastOrderBean;
import com.uhm.uhmcs.bean.RelieveShiftBean;
import com.uhm.uhmcs.bean.RelieveShiftPrintBean;
import com.uhm.uhmcs.utils.GsonSandL;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RelieveShiftAdapter extends BaseQuickAdapter<RelieveShiftBean, BaseViewHolder> {

    public RelieveShiftAdapter() {
        super(R.layout.item_relieve_shift);
    }


    @Override
    protected void convert(BaseViewHolder helper, RelieveShiftBean item) {
        helper.setText(R.id.xuhao_tv,(helper.getLayoutPosition()+1)+"");
        helper.setText(R.id.time_tv,new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(item.getCreatetime()* 1000L)));
        helper.setText(R.id.shouyinyuan_tv, item.getNickname());

        BigDecimal toeal_shoukuan=new BigDecimal(0.00);

        if (!TextUtils.isEmpty(item.getPaymentjson())){
            List<RelieveShiftPrintBean.DataBean.TotalBean> totalBeans= GsonSandL.getInstance().GsonStoL(item.getPaymentjson(),RelieveShiftPrintBean.DataBean.TotalBean.class);
            for (RelieveShiftPrintBean.DataBean.TotalBean totalBean:totalBeans){
                toeal_shoukuan=toeal_shoukuan.add(new BigDecimal(totalBean.getTotal()));
            }

        }
        BigDecimal toeal_tuikuan=new BigDecimal(0.00);

        if (!TextUtils.isEmpty(item.getRefundjson())){
            List<RelieveShiftPrintBean.DataBean.RefundBean> refundBeans= GsonSandL.getInstance().GsonStoL(item.getRefundjson(),RelieveShiftPrintBean.DataBean.RefundBean.class);
            for (RelieveShiftPrintBean.DataBean.RefundBean refundBean:refundBeans){
                toeal_tuikuan=toeal_tuikuan.add(new BigDecimal(refundBean.getTotal()));
            }

        }
        helper.setText(R.id.shoukuan_tv,toeal_shoukuan.toString());
        helper.setText(R.id.tuikuan_tv,toeal_tuikuan.toString());
        helper.setText(R.id.yingshou_tv,toeal_shoukuan.subtract(toeal_tuikuan).toString());
        helper.addOnClickListener(R.id.print_btn);


    }
}
