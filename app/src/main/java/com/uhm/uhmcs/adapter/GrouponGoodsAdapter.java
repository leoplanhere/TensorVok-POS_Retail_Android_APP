package com.uhm.uhmcs.adapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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
        // 1. 获取标题控件
        TextView textView = helper.getView(R.id.title);
        helper.setText(R.id.title, item.getTitle());

        // 设置你想要的字号
        textView.setTextSize(20);

        // 2. 图片相关逻辑全部注释掉，防止 NullPointerException
        /*
        ImageView imageView = (ImageView) helper.getView(R.id.image);
        if (TextUtils.isEmpty(item.getImage())){
            if (imageView != null) imageView.setVisibility(GONE);
        } else {
            if (imageView != null) {
                imageView.setVisibility(VISIBLE);
                Glide.with(context)
                        .load(item.getImage())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imageView);
            }
        }
        */

        // 3. 设置价格
        helper.setText(R.id.price, "￥" + item.getPrice());
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
