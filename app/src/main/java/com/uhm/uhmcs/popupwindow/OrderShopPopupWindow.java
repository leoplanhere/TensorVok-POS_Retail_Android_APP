package com.uhm.uhmcs.popupwindow;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.adapter.OrderShopAdapter;
import com.uhm.uhmcs.adapter.ShopAdapter;
import com.uhm.uhmcs.adapter.ShopTypeAdapter1;
import com.uhm.uhmcs.bean.CategoryListBean;
import com.uhm.uhmcs.bean.GrouponGoodsBean;
import com.uhm.uhmcs.bean.LastOrderBean;
import com.uhm.uhmcs.utils.UserUtils;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class OrderShopPopupWindow {
    private PopupWindow popupWindow;
    private Context context;



    private RecyclerView shop_rv;
    private OrderShopAdapter orderShopAdapter;

    private  ArrayList<LastOrderBean.GoodsJsonBean> goodsJsonBeanArrayList;


    public OrderShopPopupWindow(Context context, ArrayList<LastOrderBean.GoodsJsonBean> goodsJsonBeanArrayList){

        this.context = context;
        this.goodsJsonBeanArrayList = goodsJsonBeanArrayList;

        initPopup();
    }

    private void initPopup() {
        View popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_order_shop, null);
        popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true
        );
//        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupView.setBackgroundColor(context.getColor(R.color.black60));
        popupWindow.setOutsideTouchable(true);
        // 计算居中位置
        popupView.post(() -> {
            DisplayMetrics metrics = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int x = (metrics.widthPixels - popupView.getWidth()) / 2;
            int y = (metrics.heightPixels - popupView.getHeight()) / 2;
            popupWindow.update(x, y, -1, -1); // 更新位置
        });


        popupView.findViewById(R.id.guanbi_btn).setOnClickListener(v -> {
            popupWindow.dismiss();
        });

        shop_rv=popupView.findViewById(R.id.shop_rv);
        shop_rv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL,false));
        orderShopAdapter=new OrderShopAdapter();
        shop_rv.setAdapter(orderShopAdapter);
        orderShopAdapter.setNewData(goodsJsonBeanArrayList);






    }


    public void show() {
        View rootView = ((Activity) context).getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
    }
}
