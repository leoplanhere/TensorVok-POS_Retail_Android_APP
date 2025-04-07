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
import com.uhm.uhmcs.adapter.GuaDanShopAdapter;
import com.uhm.uhmcs.adapter.ShopAdapter;
import com.uhm.uhmcs.adapter.ShopTypeAdapter1;
import com.uhm.uhmcs.bean.CategoryListBean;
import com.uhm.uhmcs.bean.GrouponGoodsBean;
import com.uhm.uhmcs.utils.UserUtils;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ShopPopupWindow {
    private PopupWindow popupWindow;
    private Context context;
    private ArrayList<GrouponGoodsBean.GrouponGoodsModel> allGrouponGoodsModelList;
    private ArrayList<CategoryListBean.CategoryListModel> categoryListModelArrayList;
    private RecyclerView shop_type_rv;
    private ShopTypeAdapter1 shopTypeAdapter1;
    private ShopAdapter shopAdapter;
    private RecyclerView shop_rv;
    private String category_ids="";
    private TextView all_select;
    private EditText sousuo_tv;
    private boolean is_all_select=false;
    private LinearLayout all_select_btn;
    private LinearLayoutManager shopTypeLinearLayoutManager;
    private  PopupWindowOnClickListener.ShopOnClickListener shopOnClickListener;


    public ShopPopupWindow(Context context, PopupWindowOnClickListener.ShopOnClickListener shopOnClickListener){

        this.context = context;
        this.shopOnClickListener=shopOnClickListener;

        initPopup();
    }

    private void initPopup() {
        View popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_shop, null);
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
        // 绑定子 View 事件
        popupView.findViewById(R.id.guanbi_btn).setOnClickListener(v -> {
            popupWindow.dismiss();
        });
        // 绑定子 View 事件
        popupView.findViewById(R.id.btn_quxiao).setOnClickListener(v -> {
            popupWindow.dismiss();
        });
        shop_type_rv=popupView.findViewById(R.id.shop_type_rv);
        shopTypeLinearLayoutManager=new LinearLayoutManager(context, RecyclerView.VERTICAL,false);
        shop_type_rv.setLayoutManager(shopTypeLinearLayoutManager);
        shopTypeAdapter1=new ShopTypeAdapter1();
        shop_type_rv.setAdapter(shopTypeAdapter1);


        shop_rv=popupView.findViewById(R.id.shop_rv);
        shop_rv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL,false));
        shopAdapter=new ShopAdapter();
        shop_rv.setAdapter(shopAdapter);
        all_select=popupView.findViewById(R.id.all_select);
        all_select_btn=popupView.findViewById(R.id.all_select_btn);

        all_select_btn.setOnClickListener(v -> {
            if (is_all_select){
                is_all_select=false;
                all_select.setBackgroundResource(R.mipmap.checkbox_1);
                for (GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel : shopAdapter.getData()) {
                    grouponGoodsModel.setSelected(false);
                }
            }else {
                is_all_select=true;
                all_select.setBackgroundResource(R.mipmap.checkbox_2);
                for (GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel : shopAdapter.getData()) {
                    grouponGoodsModel.setSelected(true);
                }
            }
            shopAdapter.notifyDataSetChanged();
        });
        sousuo_tv=popupView.findViewById(R.id.sousuo_tv);
        popupView.findViewById(R.id.sousuo_btn).setOnClickListener(v -> {
            // 滚动到位置 0（第一条）
            shopTypeLinearLayoutManager.scrollToPosition(0);  // 立即滚动，无动画效果
            category_ids="";
            if (TextUtils.isEmpty(sousuo_tv.getText().toString())){
                shopAdapter.setNewData(allGrouponGoodsModelList);
                return;
            }
            shopAdapter.setNewData(allGrouponGoodsModelList.stream()
                    .filter(grouponGoodsModel -> grouponGoodsModel.getTitle().contains(sousuo_tv.getText().toString())||grouponGoodsModel.getGoods_sn().contains(sousuo_tv.getText().toString()))
                    .collect(Collectors.toCollection(ArrayList::new)));

        });
        popupView.findViewById(R.id.add_btn).setOnClickListener(v -> {
            if (is_all_select){
                shopOnClickListener.onClick((ArrayList<GrouponGoodsBean.GrouponGoodsModel>) shopAdapter.getData());
            }else {
                shopOnClickListener.onClick(shopAdapter.getData().stream()
                        .filter(GrouponGoodsBean.GrouponGoodsModel::isSelected)
                        .collect(Collectors.toCollection(ArrayList::new)));
            }
            popupWindow.dismiss();
        });






        if (!TextUtils.isEmpty(UserUtils.getInstance().getCategoryListBeanJson())) {
            Gson gson = new Gson();
            CategoryListBean categoryListBean = gson.fromJson(UserUtils.getInstance().getCategoryListBeanJson(), CategoryListBean.class);
            categoryListModelArrayList=categoryListBean.getData();
            category_ids=categoryListModelArrayList.get(0).getCategory_id();
            shopTypeAdapter1.setNewData(categoryListModelArrayList);
            shopTypeAdapter1.setIndex(0);
        }
        if (!TextUtils.isEmpty(UserUtils.getInstance().getGrouponGoodsBeanJson())) {
            Gson gson = new Gson();
            GrouponGoodsBean grouponGoodsBean = gson.fromJson(UserUtils.getInstance().getGrouponGoodsBeanJson(), GrouponGoodsBean.class);
            allGrouponGoodsModelList = grouponGoodsBean.getData();
//            for (GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel : allGrouponGoodsModelList) {
//                grouponGoodsModel.setSelected(false);
//            }
            if (TextUtils.isEmpty(category_ids)) {
                shopAdapter.setNewData(allGrouponGoodsModelList);
            }else {
                shopAdapter.setNewData(allGrouponGoodsModelList.stream()
                        .filter(grouponGoodsModel -> grouponGoodsModel.getCategory_ids().equals(category_ids))
                        .collect(Collectors.toCollection(ArrayList::new)));
            }

        }

        shopTypeAdapter1.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if ((TextUtils.isEmpty(shopTypeAdapter1.getData().get(position).getId()) ? "" : shopTypeAdapter1.getData().get(position).getId()).equals(category_ids)){
                    return;
                }
                is_all_select=false;
                all_select.setBackgroundResource(R.mipmap.checkbox_1);

                for (GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel : allGrouponGoodsModelList) {
                    grouponGoodsModel.setSelected(false);
                }
                shopTypeAdapter1.setIndex(position);
                category_ids = TextUtils.isEmpty(shopTypeAdapter1.getData().get(position).getId()) ? "" : shopTypeAdapter1.getData().get(position).getId();
//                grouponGoods_page = 1;
//                grouponGoodsAdapter.hasMore = true;
//                goods_sn = "";
//                getGrouponGoods();
                if (TextUtils.isEmpty(category_ids)) {
                    shopAdapter.setNewData(allGrouponGoodsModelList);
                    return;
                }
                ArrayList<GrouponGoodsBean.GrouponGoodsModel> grouponGoodsModelArrayList = new ArrayList<>();
                grouponGoodsModelArrayList = allGrouponGoodsModelList.stream()
                        .filter(grouponGoodsModel -> grouponGoodsModel.getCategory_ids().equals(category_ids))
                        .collect(Collectors.toCollection(ArrayList::new));

                shopAdapter.setNewData(grouponGoodsModelArrayList);
            }
        });
        shopAdapter.setOnItemClickListener((adapter, view, position) -> {
            GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel=shopAdapter.getData().get(position);
            if (grouponGoodsModel.isSelected()){
                grouponGoodsModel.setSelected(false);
                is_all_select=false;
                all_select.setBackgroundResource(R.mipmap.checkbox_1);
            }else {
                grouponGoodsModel.setSelected(true);
                ArrayList<GrouponGoodsBean.GrouponGoodsModel> grouponGoodsModelArrayList = new ArrayList<>();
                grouponGoodsModelArrayList = shopAdapter.getData().stream()
                        .filter(grouponGoodsModel1 -> !grouponGoodsModel1.isSelected())
                        .collect(Collectors.toCollection(ArrayList::new));
                if (grouponGoodsModelArrayList.isEmpty()){
                    is_all_select=true;
                    all_select.setBackgroundResource(R.mipmap.checkbox_2);
                }
            }
            shopAdapter.notifyItemChanged(position);
        });






    }

    public void show() {
        View rootView = ((Activity) context).getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
    }
}
