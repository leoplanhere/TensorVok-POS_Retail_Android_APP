package com.uhm.uhmcs.popupwindow;

import static android.view.KeyEvent.KEYCODE_NUMPAD_ENTER;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.activity.MainActivity;
import com.uhm.uhmcs.adapter.GuaDanShopAdapter;
import com.uhm.uhmcs.adapter.ShopAdapter;
import com.uhm.uhmcs.adapter.ShopTypeAdapter1;
import com.uhm.uhmcs.bean.CategoryListBean;
import com.uhm.uhmcs.bean.GrouponGoodsBean;
import com.uhm.uhmcs.utils.UserUtils;
import com.uhm.uhmcs.view.CustomInputTextView;

import java.util.ArrayList;
import java.util.Arrays;
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
    private TextView zongshu_tv,yeshu_tv;
    private AppCompatImageView jian_btn,jia_btn;
    private CustomInputTextView qianwang_tv;
    private ArrayList<GrouponGoodsBean.GrouponGoodsModel> indexGrouponGoodsModelList = new ArrayList<>();
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

        zongshu_tv=popupView.findViewById(R.id.zongshu_tv);
        jian_btn=popupView.findViewById(R.id.jian_btn);
        yeshu_tv=popupView.findViewById(R.id.yeshu_tv);
        jia_btn=popupView.findViewById(R.id.jia_btn);
        qianwang_tv=popupView.findViewById(R.id.qianwang_tv);



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
        sousuo_tv.postDelayed(() -> sousuo_tv.requestFocus(), 100);
        sousuo_tv.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_ENTER|| keyCode ==KEYCODE_NUMPAD_ENTER)) {
                // 处理 Enter 键
                // 滚动到位置 0（第一条）
                shopTypeLinearLayoutManager.scrollToPosition(0);  // 立即滚动，无动画效果
                category_ids="";
                shopTypeAdapter1.setIndex(0);
                if (TextUtils.isEmpty(sousuo_tv.getText().toString())){
                    indexGrouponGoodsModelList=allGrouponGoodsModelList;
                    grouponGoods_page=1;
                    shopAdapter.setNewData(getPageData(grouponGoods_page, indexGrouponGoodsModelList));
                    zongshu_tv.setText("共"+getTotalPages(indexGrouponGoodsModelList)+"页");
                    yeshu_tv.setText(grouponGoods_page+"");
                    return true;
                }
                Log.i("ttt",new Gson().toJson((ArrayList<GrouponGoodsBean.GrouponGoodsModel>)allGrouponGoodsModelList.stream()
                        .filter(grouponGoodsModel -> grouponGoodsModel.getTitle().contains(sousuo_tv.getText().toString())||(!TextUtils.isEmpty(grouponGoodsModel.getSn())&&grouponGoodsModel.getSn().equals(sousuo_tv.getText().toString())))
                        .collect(Collectors.toCollection(ArrayList::new))));
                indexGrouponGoodsModelList=allGrouponGoodsModelList.stream()
                        .filter(grouponGoodsModel -> grouponGoodsModel.getTitle().contains(sousuo_tv.getText().toString())||(!TextUtils.isEmpty(grouponGoodsModel.getSn())&&grouponGoodsModel.getSn().equals(sousuo_tv.getText().toString())))
                        .collect(Collectors.toCollection(ArrayList::new));
                grouponGoods_page=1;
                shopAdapter.setNewData(getPageData(grouponGoods_page, indexGrouponGoodsModelList));
                zongshu_tv.setText("共"+getTotalPages(indexGrouponGoodsModelList)+"页");
                yeshu_tv.setText(grouponGoods_page+"");
                sousuo_tv.setText("");
                return true; // 消费事件
            }
            return false; // 允许事件传递
        });

        popupView.findViewById(R.id.sousuo_btn).setOnClickListener(v -> {
            // 滚动到位置 0（第一条）
            shopTypeLinearLayoutManager.scrollToPosition(0);  // 立即滚动，无动画效果
            category_ids="";
            shopTypeAdapter1.setIndex(0);
            if (TextUtils.isEmpty(sousuo_tv.getText().toString())){
                indexGrouponGoodsModelList=allGrouponGoodsModelList;
                grouponGoods_page=1;
                shopAdapter.setNewData(getPageData(grouponGoods_page, indexGrouponGoodsModelList));
                zongshu_tv.setText("共"+getTotalPages(indexGrouponGoodsModelList)+"页");
                yeshu_tv.setText(grouponGoods_page+"");
                return;
            }
            Log.i("ttt",new Gson().toJson((ArrayList<GrouponGoodsBean.GrouponGoodsModel>)allGrouponGoodsModelList.stream()
                    .filter(grouponGoodsModel -> grouponGoodsModel.getTitle().contains(sousuo_tv.getText().toString())||(!TextUtils.isEmpty(grouponGoodsModel.getSn())&&grouponGoodsModel.getSn().equals(sousuo_tv.getText().toString())))
                    .collect(Collectors.toCollection(ArrayList::new))));
            indexGrouponGoodsModelList=allGrouponGoodsModelList.stream()
                    .filter(grouponGoodsModel -> grouponGoodsModel.getTitle().contains(sousuo_tv.getText().toString())||(!TextUtils.isEmpty(grouponGoodsModel.getSn())&&grouponGoodsModel.getSn().equals(sousuo_tv.getText().toString())))
                    .collect(Collectors.toCollection(ArrayList::new));
            grouponGoods_page=1;
            shopAdapter.setNewData(getPageData(grouponGoods_page, indexGrouponGoodsModelList));
            zongshu_tv.setText("共"+getTotalPages(indexGrouponGoodsModelList)+"页");
            yeshu_tv.setText(grouponGoods_page+"");
            sousuo_tv.setText("");

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
            // 因为 getData() 现在返回的是 DataWrapper 容器，需要通过 getGoodsList() 拿到里面的列表
            if (grouponGoodsBean.getData() != null) {
                allGrouponGoodsModelList = grouponGoodsBean.getData().getGoodsList();
            }
//            for (GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel : allGrouponGoodsModelList) {
//                grouponGoodsModel.setSelected(false);
//            }

            if (TextUtils.isEmpty(category_ids)) {


                indexGrouponGoodsModelList=allGrouponGoodsModelList;

            }else {
                indexGrouponGoodsModelList=allGrouponGoodsModelList.stream()
                        .filter(grouponGoodsModel -> grouponGoodsModel.getCategory_ids().equals(category_ids))
                        .collect(Collectors.toCollection(ArrayList::new));

            }
            grouponGoods_page=1;
            shopAdapter.setNewData(getPageData(grouponGoods_page, indexGrouponGoodsModelList));
            zongshu_tv.setText("共"+getTotalPages(indexGrouponGoodsModelList)+"页");
            yeshu_tv.setText(grouponGoods_page+"");

        }
        jia_btn.setOnClickListener(v -> {
            if (grouponGoods_page==getTotalPages(indexGrouponGoodsModelList)){
                return;
            }
            grouponGoods_page++;
            yeshu_tv.setText(grouponGoods_page+"");
            shopAdapter.setNewData(getPageData(grouponGoods_page, indexGrouponGoodsModelList));
        });
        jian_btn.setOnClickListener(v -> {
            if (grouponGoods_page==1){
                return;
            }
            grouponGoods_page--;
            yeshu_tv.setText(grouponGoods_page+"");
            shopAdapter.setNewData(getPageData(grouponGoods_page, indexGrouponGoodsModelList));
        });

        // 自动获取焦点
        qianwang_tv.postDelayed(() -> qianwang_tv.requestFocus(), 100);

        qianwang_tv.setOnInputCompleteListener(text -> {
            if (!isValidPositiveInteger(text,getTotalPages(indexGrouponGoodsModelList))){
                new DeleteShopPopupWindow(context,"请输入正确的页数",true).show();
                return;
            }
            grouponGoods_page=Integer.parseInt(text);
            yeshu_tv.setText(grouponGoods_page+"");
            shopAdapter.setNewData(getPageData(grouponGoods_page, indexGrouponGoodsModelList));
        });


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
                    indexGrouponGoodsModelList=allGrouponGoodsModelList;
                    grouponGoods_page=1;
                    shopAdapter.setNewData(getPageData(grouponGoods_page, indexGrouponGoodsModelList));
                    zongshu_tv.setText("共"+getTotalPages(indexGrouponGoodsModelList)+"页");
                    yeshu_tv.setText(grouponGoods_page+"");
                    return;
                }
                ArrayList<GrouponGoodsBean.GrouponGoodsModel> grouponGoodsModelArrayList = new ArrayList<>();
                grouponGoodsModelArrayList = allGrouponGoodsModelList.stream()
                        .filter(grouponGoodsModel -> !Arrays.asList(grouponGoodsModel.getCategory_ids().split(",")).stream().filter(s ->s .equals(category_ids)) .collect(Collectors.toCollection(ArrayList::new)).isEmpty())
                        .collect(Collectors.toCollection(ArrayList::new));
                indexGrouponGoodsModelList=grouponGoodsModelArrayList;
                grouponGoods_page=1;
                shopAdapter.setNewData(getPageData(grouponGoods_page, indexGrouponGoodsModelList));
                zongshu_tv.setText("共"+getTotalPages(indexGrouponGoodsModelList)+"页");
                yeshu_tv.setText(grouponGoods_page+"");
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

    public static boolean isValidPositiveInteger(String input,int tos) {
        try {
            int num = Integer.parseInt(input);
            return num > 0 && num <=tos;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    private int grouponGoods_page = 1;

    public ArrayList<GrouponGoodsBean.GrouponGoodsModel> getPageData(int currentPage, ArrayList<GrouponGoodsBean.GrouponGoodsModel> sourceList) {
        int start = (currentPage - 1) * 50;
        int end = Math.min(start + 50, sourceList.size());
        if (start >= end) return new ArrayList<>();
        return new ArrayList<>(sourceList.subList(start, end)); // 避免直接使用 subList
    }
    // 计算总页数
    public int getTotalPages(ArrayList<GrouponGoodsBean.GrouponGoodsModel> sourceList) {
        return (int) Math.ceil((double) sourceList.size() / 50);
    }

    public void show() {
        View rootView = ((Activity) context).getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
    }
}
