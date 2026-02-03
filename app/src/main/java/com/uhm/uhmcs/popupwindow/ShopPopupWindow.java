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
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.List;
import java.util.stream.Collectors;

public class ShopPopupWindow {
    private PopupWindow popupWindow;
    private Context context;

    // 静态内存缓存：确保全局只加载一次数据库
    private static List<GrouponGoodsBean.GrouponGoodsModel> staticGoodsList = null;
    private static ArrayList<CategoryListBean.CategoryListModel> staticCategoryList = null;

    private List<GrouponGoodsBean.GrouponGoodsModel> allGrouponGoodsModelList = new ArrayList<>();
    private ArrayList<CategoryListBean.CategoryListModel> categoryListModelArrayList = new ArrayList<>();

    private RecyclerView shop_type_rv;
    private ShopTypeAdapter1 shopTypeAdapter1;
    private ShopAdapter shopAdapter;
    private RecyclerView shop_rv;
    private ProgressBar loading_pb; // 加载转圈圈
    private EditText sousuo_tv;     // 搜索框
    private TextView all_select;
    private LinearLayout all_select_btn;
    private TextView btn_refresh;   // 同步按钮

    private String category_ids = "";
    private boolean is_all_select = false;
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
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // 1. 初始化所有 View 控件
        loading_pb = popupView.findViewById(R.id.loading_pb);
        sousuo_tv = popupView.findViewById(R.id.sousuo_tv);
        all_select = popupView.findViewById(R.id.all_select);
        all_select_btn = popupView.findViewById(R.id.all_select_btn);
        btn_refresh = popupView.findViewById(R.id.btn_refresh); // 绑定新按钮

        popupView.findViewById(R.id.guanbi_btn).setOnClickListener(v -> popupWindow.dismiss());
        popupView.findViewById(R.id.btn_quxiao).setOnClickListener(v -> popupWindow.dismiss());

        // 同步按钮点击事件
        if (btn_refresh != null) {
            btn_refresh.setOnClickListener(v -> {
                if (Utilis.isFastClick()) return;
                clearCache(); // 清除内存缓存
                loadDataFromDb(true); // 强制重新从数据库加载
            });
        }

        // 2. 初始化左侧分类列表
        shop_type_rv = popupView.findViewById(R.id.shop_type_rv);
        shopTypeLinearLayoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
        shop_type_rv.setLayoutManager(shopTypeLinearLayoutManager);
        shopTypeAdapter1 = new ShopTypeAdapter1();
        shop_type_rv.setAdapter(shopTypeAdapter1);

        // 3. 初始化右侧商品列表
        shop_rv = popupView.findViewById(R.id.shop_rv);
        shop_rv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        shopAdapter = new ShopAdapter();
        shop_rv.setAdapter(shopAdapter);

        // 4. 全选逻辑
        all_select_btn.setOnClickListener(v -> {
            if (shopAdapter.getData().isEmpty()) return;
            is_all_select = !is_all_select;
            all_select.setBackgroundResource(is_all_select ? R.mipmap.checkbox_2 : R.mipmap.checkbox_1);
            for (GrouponGoodsBean.GrouponGoodsModel model : shopAdapter.getData()) {
                model.setSelected(is_all_select);
            }
            shopAdapter.notifyDataSetChanged();
        });

        // 5. 搜索框监听
        if (sousuo_tv != null) {
            sousuo_tv.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KEYCODE_NUMPAD_ENTER)) {
                    performSearch();
                    return true;
                }
                return false;
            });
        }
        popupView.findViewById(R.id.sousuo_btn).setOnClickListener(v -> performSearch());

        // 6. 添加选中商品
        popupView.findViewById(R.id.add_btn).setOnClickListener(v -> {
            if (Utilis.isFastClick()) return;
            // 从全量数据中筛选，解决跨分类丢失问题
            ArrayList<GrouponGoodsBean.GrouponGoodsModel> selectedList = allGrouponGoodsModelList.stream()
                    .filter(GrouponGoodsBean.GrouponGoodsModel::isSelected)
                    .collect(Collectors.toCollection(ArrayList::new));

            if (!selectedList.isEmpty()) {
                shopOnClickListener.onClick(selectedList);
            }
            popupWindow.dismiss();
        });

        // 7. 分类点击监听
        shopTypeAdapter1.setOnItemClickListener((adapter, view, position) -> {
            String clickedId = TextUtils.isEmpty(shopTypeAdapter1.getData().get(position).getId()) ? "" : shopTypeAdapter1.getData().get(position).getId();
            if (clickedId.equals(category_ids)) return;

            shopTypeAdapter1.setIndex(position);
            category_ids = clickedId;

            List<GrouponGoodsBean.GrouponGoodsModel> filteredList;
            if (TextUtils.isEmpty(category_ids)) {
                filteredList = allGrouponGoodsModelList;
            } else {
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
            updateAllSelectStatus(filteredList);
            shopAdapter.setNewData(filteredList);
        });

        // 8. 商品条目点击监听
        shopAdapter.setOnItemClickListener((adapter, view, position) -> {
            GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel = shopAdapter.getData().get(position);
            grouponGoodsModel.setSelected(!grouponGoodsModel.isSelected());
            updateAllSelectStatus(shopAdapter.getData());
            shopAdapter.notifyItemChanged(position);
        });
    }

    private void updateAllSelectStatus(List<GrouponGoodsBean.GrouponGoodsModel> currentList) {
        if (currentList == null || currentList.isEmpty()) {
            is_all_select = false;
        } else {
            is_all_select = currentList.stream().allMatch(GrouponGoodsBean.GrouponGoodsModel::isSelected);
        }
        if (all_select != null) {
            all_select.setBackgroundResource(is_all_select ? R.mipmap.checkbox_2 : R.mipmap.checkbox_1);
        }
    }

    private void performSearch() {
        category_ids = "";
        String searchText = (sousuo_tv != null) ? sousuo_tv.getText().toString().trim() : "";
        List<GrouponGoodsBean.GrouponGoodsModel> searchResult;

        if (TextUtils.isEmpty(searchText)) {
            searchResult = allGrouponGoodsModelList;
        } else {
            searchResult = allGrouponGoodsModelList.stream()
                    .filter(goods -> (goods.getTitle() != null && goods.getTitle().contains(searchText))
                            || (goods.getSn() != null && goods.getSn().equals(searchText)))
                    .collect(Collectors.toList());
        }

        updateAllSelectStatus(searchResult);
        shopAdapter.setNewData(searchResult);
        if (shopTypeLinearLayoutManager != null) {
            shopTypeLinearLayoutManager.scrollToPositionWithOffset(0, 0);
        }
        if (sousuo_tv != null) sousuo_tv.setText("");
    }

    /**
     * 从数据库加载数据的核心方法
     * @param isManualRefresh 是否是手动点击同步触发的
     */
    private void loadDataFromDb(boolean isManualRefresh) {
        if (loading_pb != null) loading_pb.setVisibility(View.VISIBLE);

        new Thread(() -> {
            // 1. 加载分类
            if (!TextUtils.isEmpty(UserUtils.getInstance().getCategoryListBeanJson())) {
                try {
                    Gson gson = new Gson();
                    CategoryListBean categoryListBean = gson.fromJson(UserUtils.getInstance().getCategoryListBeanJson(), CategoryListBean.class);
                    if (categoryListBean != null && categoryListBean.getData() != null) {
                        staticCategoryList = categoryListBean.getData();
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }

            // 2. 加载商品（LitePal）
            staticGoodsList = LitePal.findAll(GrouponGoodsBean.GrouponGoodsModel.class);
            if (staticGoodsList != null) {
                for (GrouponGoodsBean.GrouponGoodsModel model : staticGoodsList) {
                    model.setSelected(false);
                }
            } else {
                staticGoodsList = new ArrayList<>();
            }

            if (staticCategoryList == null) staticCategoryList = new ArrayList<>();

            this.allGrouponGoodsModelList = staticGoodsList;
            this.categoryListModelArrayList = staticCategoryList;

            ((Activity) context).runOnUiThread(() -> {
                shopTypeAdapter1.setNewData(categoryListModelArrayList);
                shopTypeAdapter1.setIndex(0);
                shopAdapter.setNewData(allGrouponGoodsModelList);

                // 如果是手动同步，重置搜索状态和全选状态
                if (isManualRefresh) {
                    category_ids = "";
                    is_all_select = false;
                    if (all_select != null) all_select.setBackgroundResource(R.mipmap.checkbox_1);
                    if (sousuo_tv != null) sousuo_tv.setText("");
                }

                // 隐藏转圈圈
                if (loading_pb != null) loading_pb.setVisibility(View.GONE);
            });
        }).start();
    }

    public void show() {
        View rootView = ((Activity) context).getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);

        // --- 修复崩溃：增加空判断 ---
        if (sousuo_tv != null) {
            sousuo_tv.postDelayed(() -> {
                if (sousuo_tv != null) {
                    sousuo_tv.requestFocus();
                    sousuo_tv.setText("");
                }
            }, 100);
        }

        // 重置 UI 状态
        is_all_select = false;
        if (all_select != null) all_select.setBackgroundResource(R.mipmap.checkbox_1);
        category_ids = "";

        if (staticGoodsList != null && !staticGoodsList.isEmpty() && staticCategoryList != null) {
            // 使用缓存加速
            this.allGrouponGoodsModelList = staticGoodsList;
            this.categoryListModelArrayList = staticCategoryList;

            for (GrouponGoodsBean.GrouponGoodsModel model : allGrouponGoodsModelList) {
                model.setSelected(false);
            }

            shopTypeAdapter1.setNewData(categoryListModelArrayList);
            shopTypeAdapter1.setIndex(0);
            shopAdapter.setNewData(allGrouponGoodsModelList);

            // 隐藏转圈圈
            if (loading_pb != null) loading_pb.setVisibility(View.GONE);

        } else {
            // 第一次进入，执行异步加载
            loadDataFromDb(false);
        }
    }

    public static void clearCache() {
        staticGoodsList = null;
        staticCategoryList = null;
    }
}