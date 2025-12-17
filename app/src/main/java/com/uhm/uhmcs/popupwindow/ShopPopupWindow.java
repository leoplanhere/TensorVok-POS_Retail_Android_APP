package com.uhm.uhmcs.popupwindow;

import static android.view.KeyEvent.KEYCODE_NUMPAD_ENTER;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
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
import com.uhm.uhmcs.adapter.ShopAdapter;
import com.uhm.uhmcs.adapter.ShopTypeAdapter1;
import com.uhm.uhmcs.bean.CategoryListBean;
import com.uhm.uhmcs.bean.GrouponGoodsBean;
import com.uhm.uhmcs.utils.UserUtils;
import com.uhm.uhmcs.utils.Utilis;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ShopPopupWindow {
    private PopupWindow popupWindow;
    private Context context;
    // 这里的 List 泛型应该是具体的 Model，而不是 DataWrapper
    private List<GrouponGoodsBean.GrouponGoodsModel> allGrouponGoodsModelList = new ArrayList<>();
    private ArrayList<CategoryListBean.CategoryListModel> categoryListModelArrayList = new ArrayList<>();
    private RecyclerView shop_type_rv;
    private ShopTypeAdapter1 shopTypeAdapter1;
    private ShopAdapter shopAdapter;
    private RecyclerView shop_rv;
    private String category_ids = "";
    private TextView all_select;
    private EditText sousuo_tv;
    private boolean is_all_select = false;
    private LinearLayout all_select_btn;
    private LinearLayoutManager shopTypeLinearLayoutManager;
    private PopupWindowOnClickListener.ShopOnClickListener shopOnClickListener;

    public ShopPopupWindow(Context context, PopupWindowOnClickListener.ShopOnClickListener shopOnClickListener) {
        this.context = context;
        this.shopOnClickListener = shopOnClickListener;
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
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // 修复透明背景可能导致的问题
        // popupView.setBackgroundColor(context.getColor(R.color.black60)); // 如果xml里有背景色，这行可以注释

        // 绑定子 View 事件
        popupView.findViewById(R.id.guanbi_btn).setOnClickListener(v -> popupWindow.dismiss());
        popupView.findViewById(R.id.btn_quxiao).setOnClickListener(v -> popupWindow.dismiss());

        shop_type_rv = popupView.findViewById(R.id.shop_type_rv);
        shopTypeLinearLayoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
        shop_type_rv.setLayoutManager(shopTypeLinearLayoutManager);
        shopTypeAdapter1 = new ShopTypeAdapter1();
        shop_type_rv.setAdapter(shopTypeAdapter1);

        shop_rv = popupView.findViewById(R.id.shop_rv);
        shop_rv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        shopAdapter = new ShopAdapter();
        shop_rv.setAdapter(shopAdapter);
        all_select = popupView.findViewById(R.id.all_select);
        all_select_btn = popupView.findViewById(R.id.all_select_btn);

        all_select_btn.setOnClickListener(v -> {
            if (shopAdapter.getData().isEmpty()) return; // 没数据点全选无效

            if (is_all_select) {
                is_all_select = false;
                all_select.setBackgroundResource(R.mipmap.checkbox_1);
                for (GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel : shopAdapter.getData()) {
                    grouponGoodsModel.setSelected(false);
                }
            } else {
                is_all_select = true;
                all_select.setBackgroundResource(R.mipmap.checkbox_2);
                for (GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel : shopAdapter.getData()) {
                    grouponGoodsModel.setSelected(true);
                }
            }
            shopAdapter.notifyDataSetChanged();
        });

        sousuo_tv = popupView.findViewById(R.id.sousuo_tv);

        sousuo_tv.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KEYCODE_NUMPAD_ENTER)) {
                performSearch();
                return true;
            }
            return false;
        });

        popupView.findViewById(R.id.sousuo_btn).setOnClickListener(v -> performSearch());

        popupView.findViewById(R.id.add_btn).setOnClickListener(v -> {
            if (Utilis.isFastClick()) return;

            ArrayList<GrouponGoodsBean.GrouponGoodsModel> selectedList;
            if (is_all_select) {
                selectedList = new ArrayList<>(shopAdapter.getData());
            } else {
                selectedList = shopAdapter.getData().stream()
                        .filter(GrouponGoodsBean.GrouponGoodsModel::isSelected)
                        .collect(Collectors.toCollection(ArrayList::new));
            }

            if (selectedList != null && !selectedList.isEmpty()) {
                shopOnClickListener.onClick(selectedList);
            }
            popupWindow.dismiss();
        });

        shopTypeAdapter1.setOnItemClickListener((adapter, view, position) -> {
            String clickedId = TextUtils.isEmpty(shopTypeAdapter1.getData().get(position).getId()) ? "" : shopTypeAdapter1.getData().get(position).getId();

            if (clickedId.equals(category_ids)) return;

            is_all_select = false;
            all_select.setBackgroundResource(R.mipmap.checkbox_1);

            // 清除之前的选中状态
            if (allGrouponGoodsModelList != null) {
                for (GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel : allGrouponGoodsModelList) {
                    grouponGoodsModel.setSelected(false);
                }
            }

            shopTypeAdapter1.setIndex(position);
            category_ids = clickedId;

            if (TextUtils.isEmpty(category_ids)) {
                shopAdapter.setNewData(allGrouponGoodsModelList);
            } else {
                // 根据分类ID过滤
                List<GrouponGoodsBean.GrouponGoodsModel> filteredList = new ArrayList<>();
                if (allGrouponGoodsModelList != null) {
                    filteredList = allGrouponGoodsModelList.stream()
                            .filter(goods -> {
                                if (TextUtils.isEmpty(goods.getCategory_ids())) return false;
                                String[] ids = goods.getCategory_ids().split(",");
                                for (String id : ids) {
                                    if (id.equals(category_ids)) return true;
                                }
                                return false;
                            })
                            .collect(Collectors.toList());
                }
                shopAdapter.setNewData(filteredList);
            }
        });

        shopAdapter.setOnItemClickListener((adapter, view, position) -> {
            GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel = shopAdapter.getData().get(position);
            grouponGoodsModel.setSelected(!grouponGoodsModel.isSelected());

            // 检查是否全选/全不选
            boolean hasUnselected = shopAdapter.getData().stream().anyMatch(model -> !model.isSelected());
            if (hasUnselected) {
                is_all_select = false;
                all_select.setBackgroundResource(R.mipmap.checkbox_1);
            } else if (!shopAdapter.getData().isEmpty()) {
                is_all_select = true;
                all_select.setBackgroundResource(R.mipmap.checkbox_2);
            }
            shopAdapter.notifyItemChanged(position);
        });
    }

    private void performSearch() {
        category_ids = "";
        String searchText = sousuo_tv.getText().toString().trim();

        if (TextUtils.isEmpty(searchText)) {
            shopAdapter.setNewData(allGrouponGoodsModelList);
        } else {
            if (allGrouponGoodsModelList != null) {
                List<GrouponGoodsBean.GrouponGoodsModel> searchResult = allGrouponGoodsModelList.stream()
                        .filter(goods -> (goods.getTitle() != null && goods.getTitle().contains(searchText))
                                || (goods.getSn() != null && goods.getSn().equals(searchText)))
                        .collect(Collectors.toList());
                shopAdapter.setNewData(searchResult);
            }
        }
        shopTypeLinearLayoutManager.scrollToPositionWithOffset(0, 0);
        sousuo_tv.setText("");
    }

    public void show() {
        View rootView = ((Activity) context).getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
        sousuo_tv.postDelayed(() -> sousuo_tv.requestFocus(), 100);

        new Thread(() -> {
            // 1. 加载分类数据 (从 UserUtils)
            if (!TextUtils.isEmpty(UserUtils.getInstance().getCategoryListBeanJson())) {
                try {
                    Gson gson = new Gson();
                    CategoryListBean categoryListBean = gson.fromJson(UserUtils.getInstance().getCategoryListBeanJson(), CategoryListBean.class);
                    if (categoryListBean != null && categoryListBean.getData() != null) {
                        categoryListModelArrayList = categoryListBean.getData();
                        if (!categoryListModelArrayList.isEmpty()) {
                            // 默认选中第一个分类（通常是“全部”）
                            // category_ids = categoryListModelArrayList.get(0).getCategory_id(); // 暂时不默认选中具体分类，显示全部
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 2. ▼▼▼▼▼▼ 核心修改：改为从数据库查数据，而不是读 UserUtils 的大 JSON ▼▼▼▼▼▼
            // 直接查询 LitePal 数据库里的所有商品
            // 注意：如果数据量特别大(3万条)，一次性查出来可能会有短暂卡顿，但比解析 JSON 快得多。
            // 如果觉得卡，这里也可以改成 limit(500) 先查一部分。
            allGrouponGoodsModelList = LitePal.findAll(GrouponGoodsBean.GrouponGoodsModel.class);

            // 初始化选中状态
            if (allGrouponGoodsModelList != null) {
                for (GrouponGoodsBean.GrouponGoodsModel model : allGrouponGoodsModelList) {
                    model.setSelected(false);
                }
            } else {
                allGrouponGoodsModelList = new ArrayList<>(); // 判空，防止崩溃
            }
            // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲

            ((Activity) context).runOnUiThread(() -> {
                shopTypeAdapter1.setNewData(categoryListModelArrayList);
                shopTypeAdapter1.setIndex(0);
                shopAdapter.setNewData(allGrouponGoodsModelList);
            });

        }).start();
    }
}