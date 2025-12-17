package com.uhm.uhmcs.popupwindow;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.uhm.uhmcs.utils.UserUtils;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.GrouponGoodsBean;
import com.uhm.uhmcs.http.OkHttpUtil;
import com.uhm.uhmcs.http.POSApiSerview;
import com.uhm.uhmcs.view.CustomInputTextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList; // ★★★ 补回了这行 ★★★
import java.util.HashMap;
import java.util.Map;

public class GoodsWarehousingPopupWindow {
    private PopupWindow popupWindow;
    private final Activity context;
    private CustomInputTextView shangpintiaoma_tv, kuchunshuliang_tv;
    private TextView shangpin_tv;
    private GrouponGoodsBean.GrouponGoodsModel currentGoods; // 当前选中的商品
    private final PopupWindowOnClickListener.GoodsWarehousingOnClickListener listener;

    // 构造函数
    public GoodsWarehousingPopupWindow(Activity context, ArrayList<GrouponGoodsBean.GrouponGoodsModel> unused, PopupWindowOnClickListener.GoodsWarehousingOnClickListener listener) {
        this.context = context;
        this.listener = listener;
        initPopup();
    }

    private void initPopup() {
        View popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_goods_warehousing, null);
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        popupView.setBackgroundColor(context.getColor(R.color.black60));
        popupWindow.setOutsideTouchable(true);

        // 居中逻辑
        popupView.post(() -> {
            DisplayMetrics metrics = new DisplayMetrics();
            context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            popupWindow.update((metrics.widthPixels - popupView.getWidth()) / 2, (metrics.heightPixels - popupView.getHeight()) / 2, -1, -1);
        });

        bindViews(popupView);
        setupListeners(popupView);
    }

    private void bindViews(View view) {
        shangpintiaoma_tv = view.findViewById(R.id.shangpintiaoma_tv);
        kuchunshuliang_tv = view.findViewById(R.id.kuchunshuliang_tv);
        shangpin_tv = view.findViewById(R.id.shangpin_tv);

        // 自动聚焦
        shangpintiaoma_tv.postDelayed(() -> shangpintiaoma_tv.requestFocus(), 100);
    }

    private void setupListeners(View view) {
        view.findViewById(R.id.guanbi_btn).setOnClickListener(v -> popupWindow.dismiss());
        view.findViewById(R.id.all_view).setOnClickListener(v -> popupWindow.dismiss());

        // 扫码查询逻辑：改用 LitePal 数据库查询
        shangpintiaoma_tv.setOnInputCompleteListener(this::queryGoodsByBarcode);

        // 点击输入框重新聚焦
        shangpintiaoma_tv.setOnClickListener(v -> {
            shangpintiaoma_tv.requestFocus();
            currentGoods = null; // 重新输入时重置商品
            shangpin_tv.setText("");
        });
        kuchunshuliang_tv.setOnClickListener(v -> {
            if (currentGoods != null) kuchunshuliang_tv.requestFocus();
        });

        // 入库按钮逻辑
        view.findViewById(R.id.warehousing_btn).setOnClickListener(v -> {
            String barcode = shangpintiaoma_tv.getText().toString();
            String stockNum = kuchunshuliang_tv.getText().toString();

            if (currentGoods == null) {
                // 如果没选中商品，尝试再次查询
                queryGoodsByBarcode(barcode);
                return;
            }
            if (stockNum.isEmpty()) {
                new DeleteShopPopupWindow(context, context.getString(R.string.Quantity_in_stock), true).show();
                return;
            }
            addStore(stockNum);
        });
    }

    // 查询商品核心方法
    private void queryGoodsByBarcode(String barcode) {
        if (barcode.isEmpty()) return;

        // 核心修复：直接查数据库
        currentGoods = LitePal.where("sn = ?", barcode).findFirst(GrouponGoodsBean.GrouponGoodsModel.class);

        if (currentGoods == null) {
            new DeleteShopPopupWindow(context, context.getString(R.string.product_not_found_in_inventory), true).show();
            shangpin_tv.setText("");
        } else {
            shangpin_tv.setText(currentGoods.getTitle());
            kuchunshuliang_tv.postDelayed(() -> kuchunshuliang_tv.requestFocus(), 100);
        }
    }

    private void addStore(String stockNum) {
        String shopId = "";
        if (UserUtils.getInstance().getShopDataBean() != null &&
                UserUtils.getInstance().getShopDataBean().getData() != null &&
                !UserUtils.getInstance().getShopDataBean().getData().isEmpty()) {
            shopId = UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid();
        }

        Map<String, String> params = new HashMap<>();

        // 1. 继续使用 Ggspid (数字ID 58918 应该是对的，它是SKU ID)
        params.put("goods_id", String.valueOf(currentGoods.getGgspid()));

        // ★★★ 核心修改：强制 goods_sn 也传 条形码(sn) ★★★
        // 之前的 11600 可能只是内部编码，入库时服务器可能更认条形码
        params.put("goods_sn", currentGoods.getSn());

        params.put("shop_id", shopId);
        params.put("offline_stock", stockNum);
        params.put("sn", currentGoods.getSn());

        Log.e("StoreDebug", "修正入库请求: shop_id=" + shopId +
                ", goods_id=" + params.get("goods_id") +
                ", goods_sn=" + params.get("goods_sn") +
                ", sn=" + params.get("sn"));

        OkHttpUtil.postFormAsync(POSApiSerview.POS_URL + POSApiSerview.addStore, params, context, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                context.runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        // 打印一下服务器返回的完整信息，万一还有错能看
                        Log.e("StoreDebug", "服务器返回: " + response);

                        if (listener != null) {
                            listener.onClick(jsonObject.optInt("code"), jsonObject.optString("msg"));
                        }
                        popupWindow.dismiss();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onFailure(IOException e) {
                context.runOnUiThread(() ->
                        new DeleteShopPopupWindow(context, "网络请求失败", true).show()
                );
            }
        });
    }

    public void show() {
        View rootView = context.getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
    }
}