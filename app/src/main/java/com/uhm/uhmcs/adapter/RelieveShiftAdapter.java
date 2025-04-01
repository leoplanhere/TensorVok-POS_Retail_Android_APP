package com.uhm.uhmcs.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.LastOrderBean;
import com.uhm.uhmcs.bean.RelieveShiftBean;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        helper.setText(R.id.danhao_tv,item.getShift_id());
        helper.setText(R.id.yingyee_tv,item.getTurnover());



    }
}
