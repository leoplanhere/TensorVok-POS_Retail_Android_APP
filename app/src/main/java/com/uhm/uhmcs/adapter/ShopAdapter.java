package com.uhm.uhmcs.adapter;

import android.text.TextUtils; // 必须添加这一句，解决刚才的报错
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.GrouponGoodsBean;

/**
 * 打印清单列表适配器
 * 用于显示待打印商品的列表信息
 */
public class ShopAdapter extends BaseQuickAdapter<GrouponGoodsBean.GrouponGoodsModel, BaseViewHolder> {

    public ShopAdapter() {
        super(R.layout.item_shop);
    }

    @Override
    protected void convert(BaseViewHolder helper, GrouponGoodsBean.GrouponGoodsModel item) {
        // 1. 设置序号
        helper.setText(R.id.xuhao_tv, String.valueOf(helper.getLayoutPosition() + 1));

        // 2. 设置商品名称
        helper.setText(R.id.shop_name, item.getTitle());

        // 3. 设置编码 (SKU) - 增加判空保护逻辑
        String displayCode = TextUtils.isEmpty(item.getSn()) ? item.getGoods_sn() : item.getSn();
        helper.setText(R.id.bianma_tv, displayCode);

        // 4. 设置副标题
        helper.setText(R.id.fubiaoti_tv, item.getSubtitle());

        // 5. 设置售价
        helper.setText(R.id.shoujia_tv, item.getPrice());

        // 6. 设置原价
        helper.setText(R.id.yuanjia_tv, item.getOriginal_price());

        // 7. 设置成本价
        helper.setText(R.id.chengbenjia_tv, item.getCost_price());

        // 8. 设置勾选框状态
        if (item.isSelected()) {
            helper.setBackgroundRes(R.id.is_selected, R.mipmap.checkbox_2);
        } else {
            helper.setBackgroundRes(R.id.is_selected, R.mipmap.checkbox_1);
        }
    }
}