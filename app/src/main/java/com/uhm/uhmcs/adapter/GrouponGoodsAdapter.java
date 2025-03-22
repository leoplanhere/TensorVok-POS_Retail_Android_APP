package com.uhm.uhmcs.adapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.GrouponGoodsBean;


import java.util.List;

public class GrouponGoodsAdapter extends BaseQuickAdapter <GrouponGoodsBean.GrouponGoodsModel, BaseViewHolder> {
    public boolean isLoading = false; // 标记是否正在加载更多
    public boolean hasMore = true; // 标记是否还有更多数据可以加载
    private Context context;
    public GrouponGoodsAdapter(Context context, int layoutResId) {
        super(layoutResId);
        this.context=context;
    }

    @Override
    protected void convert(BaseViewHolder helper, GrouponGoodsBean.GrouponGoodsModel item) {
        TextView textView=helper.getView(R.id.title);
        helper.setText(R.id.title,item.getTitle());
        ImageView imageView=(ImageView) helper.getView(R.id.image);
        if (TextUtils.isEmpty(item.getImage())){
            imageView.setVisibility(GONE);
            textView.setTextSize(40);
        }else {
            imageView.setVisibility(VISIBLE);
            textView.setTextSize(24);
            Glide.with(context)
                    .load(item.getImage())
//                    .placeholder(R.mipmap.ic_launcher)
//                    .error(R.mipmap.ic_launcher)
                    .into(imageView);
        }
        helper.setText(R.id.price,item.getPrice()+"元");

    }
    public void loadMoreData(List<GrouponGoodsBean.GrouponGoodsModel> newData) {
        if (!isLoading && hasMore) {
            isLoading = true; // 开始加载更多数据
            addData(newData); // 添加新数据到列表末尾
            notifyDataSetChanged(); // 通知适配器数据已改变
            isLoading = false; // 加载完成，重置状态
            if (newData.isEmpty()) { // 如果新数据为空，则表示没有更多数据了
                hasMore = false;
            }
        }
    }

}
