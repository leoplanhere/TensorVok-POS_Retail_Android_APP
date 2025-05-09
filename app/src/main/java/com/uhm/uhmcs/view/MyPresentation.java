package com.uhm.uhmcs.view;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bumptech.glide.Glide;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.adapter.ShopAdapter1;
import com.uhm.uhmcs.bean.GrouponGoodsBean;
import com.uhm.uhmcs.popupwindow.HavePaidPopupWindow;

import java.util.ArrayList;
import java.util.List;

public class MyPresentation extends Presentation {
    private ConvenientBanner<String> convenientBanner;
    private List<String> imageUrls = new ArrayList<>();  // 图片资源列表（支持本地/网络路径）‌:ml-citation{ref="4,5" data="citationList"}
    private RecyclerView shop_rv;
    private static ShopAdapter1 shopAdapter1;

    private static List<GrouponGoodsBean.GrouponGoodsModel> shopArrayList;

    private static TextView tv_zongjia,all_num,daizhifu_tv;

    public MyPresentation(Context context, Display display) {
        super(context,display);
        this.context=context;
    }
    private final Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.presentation_view);
        initView();
    }

    private void initView() {
        convenientBanner = findViewById(R.id.convenientBanner);

        // 添加测试数据
        imageUrls.add("https://img1.baidu.com/it/u=2123036331,2002942410&fm=253&fmt=auto&app=120&f=JPEG?w=666&h=500");  // 网络图片
//        imageUrls.add(R.drawable.local_image2);           // 本地资源‌:ml-citation{ref="4,5" data="citationList"}

        // 设置轮播图适配器
        convenientBanner.setPages(new CBViewHolderCreator<NetworkImageHolderView>() {
                    @Override
                    public NetworkImageHolderView createHolder() {
                        return new NetworkImageHolderView();  // 自定义图片加载逻辑‌:ml-citation{ref="5" data="citationList"}
                    }
                }, imageUrls)
                .setOnItemClickListener(position -> {
                    Toast.makeText(context, "点击了第" + position + "张图", Toast.LENGTH_SHORT).show();
                });
        convenientBanner.startTurning(3000);

        shop_rv=findViewById(R.id.shop_rv);
        shop_rv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        shopAdapter1=new ShopAdapter1();
        shop_rv.setAdapter(shopAdapter1);
        tv_zongjia=findViewById(R.id.tv_zongjia);
        all_num=findViewById(R.id.all_num);
        daizhifu_tv=findViewById(R.id.daizhifu_tv);
        ImageView imageView=findViewById(R.id.image);

        // 加载本地资源
        Glide.with(context).load(R.drawable.have_paid_img).into(imageView);
        have_paid_view=findViewById(R.id.have_paid_view);
    }
    private static LinearLayout have_paid_view;

    public List<GrouponGoodsBean.GrouponGoodsModel> getShopArrayList() {
        return shopArrayList;
    }
    public static void showHavePaidView(){
        if (have_paid_view==null){
            return;
        }
        have_paid_view.setVisibility(VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                have_paid_view.setVisibility(GONE);
            }
        }, 3000);
    }

    @SuppressLint("SetTextI18n")
    public static void setShopArrayList(List<GrouponGoodsBean.GrouponGoodsModel> shopArrayList1,int allNum) {
        if (shopAdapter1!=null){
            shopArrayList = shopArrayList1;
            shopAdapter1.setNewData(shopArrayList);
            all_num.setText("x"+allNum);
            have_paid_view.setVisibility(GONE);
        }

    }
    @SuppressLint("SetTextI18n")
    public static void setZongjia(String zongjia) {
        if (tv_zongjia==null){
            return;
        }
        tv_zongjia.setText("￥"+zongjia);
        daizhifu_tv.setText("￥"+zongjia);
    }

    public static void setDaizhifu_tv(String daizhifu) {
        if (daizhifu_tv==null){
            return;
        }
        daizhifu_tv.setText("￥"+daizhifu);
    }
}
