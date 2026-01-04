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
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.adapter.ShopAdapter;
import com.uhm.uhmcs.adapter.ShopTypeAdapter1;
import com.uhm.uhmcs.bean.CategoryListBean;
import com.uhm.uhmcs.bean.GrouponGoodsBean;
import com.uhm.uhmcs.utils.SerializableUtils;
import com.uhm.uhmcs.utils.UserUtils;
import com.uhm.uhmcs.view.CustomInputTextView;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShopPopupWindow {
    private PopupWindow popupWindow;
    private Context context;
    // 初始化列表，防止 NPE
    private ArrayList<GrouponGoodsBean.GrouponGoodsModel> allGrouponGoodsModelList = new ArrayList<>();
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
    private TextView zongshu_tv, yeshu_tv;
    private AppCompatImageView jian_btn, jia_btn;
    private CustomInputTextView qianwang_tv;
    private ArrayList<GrouponGoodsBean.GrouponGoodsModel> indexGrouponGoodsModelList = new ArrayList<>();
    private int grouponGoods_page = 1;

    // 【新增】加载进度条控件
    private ProgressBar loading_bar;

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
        popupView.setBackgroundColor(context.getColor(R.color.black60));
        popupWindow.setOutsideTouchable(true);

        // 计算居中位置
        popupView.post(() -> {
            DisplayMetrics metrics = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int x = (metrics.widthPixels - popupView.getWidth()) / 2;
            int y = (metrics.heightPixels - popupView.getHeight()) / 2;
            popupWindow.update(x, y, -1, -1);
        });

        // 绑定基础 View
        popupView.findViewById(R.id.guanbi_btn).setOnClickListener(v -> popupWindow.dismiss());
        popupView.findViewById(R.id.btn_quxiao).setOnClickListener(v -> popupWindow.dismiss());

        // 【新增】绑定进度条 View
        loading_bar = popupView.findViewById(R.id.loading_bar);

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
        zongshu_tv = popupView.findViewById(R.id.zongshu_tv);
        jian_btn = popupView.findViewById(R.id.jian_btn);
        yeshu_tv = popupView.findViewById(R.id.yeshu_tv);
        jia_btn = popupView.findViewById(R.id.jia_btn);
        qianwang_tv = popupView.findViewById(R.id.qianwang_tv);
        sousuo_tv = popupView.findViewById(R.id.sousuo_tv);

        // 1. 异步加载全量商品数据
        loadDataFromDatabaseAsync();

        // 2. 加载分类数据
        if (!TextUtils.isEmpty(UserUtils.getInstance().getCategoryListBeanJson())) {
            CategoryListBean categoryListBean = new Gson().fromJson(UserUtils.getInstance().getCategoryListBeanJson(), CategoryListBean.class);
            categoryListModelArrayList = categoryListBean.getData();
            if (categoryListModelArrayList != null && !categoryListModelArrayList.isEmpty()) {
                category_ids = categoryListModelArrayList.get(0).getId();
                shopTypeAdapter1.setNewData(categoryListModelArrayList);
                shopTypeAdapter1.setIndex(0);
            }
        }

        // 全选逻辑
        all_select_btn.setOnClickListener(v -> {
            is_all_select = !is_all_select;
            all_select.setBackgroundResource(is_all_select ? R.mipmap.checkbox_2 : R.mipmap.checkbox_1);
            for (GrouponGoodsBean.GrouponGoodsModel model : shopAdapter.getData()) {
                model.setSelected(is_all_select);
            }
            shopAdapter.notifyDataSetChanged();
        });

        // 搜索框 Enter 监听
        sousuo_tv.postDelayed(() -> sousuo_tv.requestFocus(), 100);
        sousuo_tv.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KEYCODE_NUMPAD_ENTER)) {
                performSearch();
                return true;
            }
            return false;
        });

        // 搜索按钮
        popupView.findViewById(R.id.sousuo_btn).setOnClickListener(v -> performSearch());

        // 添加按钮逻辑
        popupView.findViewById(R.id.add_btn).setOnClickListener(v -> {
            ArrayList<GrouponGoodsBean.GrouponGoodsModel> selected = shopAdapter.getData().stream()
                    .filter(GrouponGoodsBean.GrouponGoodsModel::isSelected)
                    .map(SerializableUtils::deepCopy) // 深拷贝防止干扰内存引用
                    .collect(Collectors.toCollection(ArrayList::new));
            shopOnClickListener.onClick(selected);
            popupWindow.dismiss();
        });

        // 翻页逻辑
        jia_btn.setOnClickListener(v -> {
            if (grouponGoods_page < getTotalPages(indexGrouponGoodsModelList)) {
                grouponGoods_page++;
                refreshAdapterPage();
            }
        });

        jian_btn.setOnClickListener(v -> {
            if (grouponGoods_page > 1) {
                grouponGoods_page--;
                refreshAdapterPage();
            }
        });

        qianwang_tv.setOnInputCompleteListener(text -> {
            if (isValidPositiveInteger(text, getTotalPages(indexGrouponGoodsModelList))) {
                grouponGoods_page = Integer.parseInt(text);
                refreshAdapterPage();
            } else {
                new DeleteShopPopupWindow(context, "请输入正确的页数", true).show();
            }
        });

        // 分类点击切换
        shopTypeAdapter1.setOnItemClickListener((adapter, view, position) -> {
            CategoryListBean.CategoryListModel model = shopTypeAdapter1.getData().get(position);
            String newId = (model == null || TextUtils.isEmpty(model.getId())) ? "" : model.getId();

            if (newId.equals(category_ids)) return;

            // 重置全选状态
            is_all_select = false;
            all_select.setBackgroundResource(R.mipmap.checkbox_1);

            if (allGrouponGoodsModelList != null) {
                for (GrouponGoodsBean.GrouponGoodsModel goods : allGrouponGoodsModelList) {
                    goods.setSelected(false);
                }
            }

            shopTypeAdapter1.setIndex(position);
            category_ids = newId;
            updateFilteredList();
        });

        // 商品点击选中
        shopAdapter.setOnItemClickListener((adapter, view, position) -> {
            GrouponGoodsBean.GrouponGoodsModel model = shopAdapter.getData().get(position);
            model.setSelected(!model.isSelected());

            // 检查是否触发/取消全选状态
            long unselectedCount = shopAdapter.getData().stream().filter(m -> !m.isSelected()).count();
            is_all_select = (unselectedCount == 0);
            all_select.setBackgroundResource(is_all_select ? R.mipmap.checkbox_2 : R.mipmap.checkbox_1);

            shopAdapter.notifyItemChanged(position);
        });
    }

    /**
     * 【核心修改】异步加载数据库，增加 Loading 状态控制
     */
    private void loadDataFromDatabaseAsync() {
        // 1. 开始加载前：显示转圈圈
        if (loading_bar != null) {
            loading_bar.setVisibility(View.VISIBLE);
        }

        new Thread(() -> {
            try {
                // 耗时操作：查询全表
                List<GrouponGoodsBean.GrouponGoodsModel> dbList = LitePal.findAll(GrouponGoodsBean.GrouponGoodsModel.class);

                // 切换回主线程更新 UI
                if (context instanceof Activity) {
                    ((Activity) context).runOnUiThread(() -> {
                        if (dbList != null) {
                            allGrouponGoodsModelList = new ArrayList<>(dbList);
                        } else {
                            allGrouponGoodsModelList = new ArrayList<>();
                        }
                        // 数据加载完毕后，显示数据
                        updateFilteredList();

                        // 2. 加载结束后：隐藏转圈圈
                        if (loading_bar != null) {
                            loading_bar.setVisibility(View.GONE);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("ShopPopup", "LitePal异步查询失败: " + e.getMessage());
                if (context instanceof Activity) {
                    ((Activity) context).runOnUiThread(() -> {
                        allGrouponGoodsModelList = new ArrayList<>();
                        updateFilteredList();

                        // 3. 异常结束后：也要隐藏转圈圈
                        if (loading_bar != null) {
                            loading_bar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * 执行搜索逻辑
     */
    private void performSearch() {
        String query = sousuo_tv.getText().toString().trim();
        shopTypeLinearLayoutManager.scrollToPosition(0);
        category_ids = "";
        shopTypeAdapter1.setIndex(0);

        if (TextUtils.isEmpty(query)) {
            indexGrouponGoodsModelList = new ArrayList<>(allGrouponGoodsModelList);
        } else {
            // 搜索标题或条码
            indexGrouponGoodsModelList = allGrouponGoodsModelList.stream()
                    .filter(m -> (m.getTitle() != null && m.getTitle().contains(query)) ||
                            (m.getSn() != null && m.getSn().equals(query)) ||
                            (m.getGoods_sn() != null && m.getGoods_sn().equals(query)))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        grouponGoods_page = 1;
        refreshAdapterPage();
        sousuo_tv.setText("");
    }

    /**
     * 根据分类 ID 更新过滤后的商品列表
     */
    private void updateFilteredList() {
        if (allGrouponGoodsModelList == null) return;

        if (TextUtils.isEmpty(category_ids)) {
            indexGrouponGoodsModelList = new ArrayList<>(allGrouponGoodsModelList);
        } else {
            indexGrouponGoodsModelList = allGrouponGoodsModelList.stream()
                    .filter(m -> m.getCategory_ids() != null && m.getCategory_ids().contains(category_ids))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        grouponGoods_page = 1;
        refreshAdapterPage();
    }

    /**
     * 刷新适配器显示的分页数据
     */
    private void refreshAdapterPage() {
        shopAdapter.setNewData(getPageData(grouponGoods_page, indexGrouponGoodsModelList));
        zongshu_tv.setText("共" + getTotalPages(indexGrouponGoodsModelList) + "页");
        yeshu_tv.setText(String.valueOf(grouponGoods_page));
    }

    public static boolean isValidPositiveInteger(String input, int max) {
        try {
            int num = Integer.parseInt(input);
            return num > 0 && num <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public ArrayList<GrouponGoodsBean.GrouponGoodsModel> getPageData(int currentPage, ArrayList<GrouponGoodsBean.GrouponGoodsModel> sourceList) {
        if (sourceList == null || sourceList.isEmpty()) return new ArrayList<>();
        int start = (currentPage - 1) * 50;
        int end = Math.min(start + 50, sourceList.size());
        if (start >= end || start < 0) return new ArrayList<>();
        return new ArrayList<>(sourceList.subList(start, end));
    }

    public int getTotalPages(ArrayList<GrouponGoodsBean.GrouponGoodsModel> sourceList) {
        if (sourceList == null || sourceList.isEmpty()) return 1;
        return (int) Math.ceil((double) sourceList.size() / 50);
    }

    public void show() {
        if (context instanceof Activity) {
            View rootView = ((Activity) context).getWindow().getDecorView();
            popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
        }
    }
}