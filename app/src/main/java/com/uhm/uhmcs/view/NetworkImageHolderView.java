package com.uhm.uhmcs.view;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.bigkoo.convenientbanner.holder.Holder;
import com.bumptech.glide.Glide;

public class NetworkImageHolderView implements Holder<String> {
    private ImageView imageView;

    @Override
    public View createView(Context context) {
        imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return imageView;
    }

    @Override
    public void UpdateUI(Context context, int position, String data) {
        if (data.startsWith("http")) {
            // 加载网络图片
            Glide.with(context).load(data).into(imageView);  // 使用 Glide 加载‌:ml-citation{ref="4,5" data="citationList"}
        } else {
            // 加载本地资源
            imageView.setImageResource(Integer.parseInt(data));
        }
    }
}
