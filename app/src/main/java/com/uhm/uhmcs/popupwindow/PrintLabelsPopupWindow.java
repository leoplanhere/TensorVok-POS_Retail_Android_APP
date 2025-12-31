package com.uhm.uhmcs.popupwindow;

import android.app.Activity;
import android.hardware.usb.UsbDevice;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.uhm.uhmcs.R;
import com.uhm.uhmcs.adapter.ShopAdapter;
import com.uhm.uhmcs.bean.GrouponGoodsBean;
import com.uhm.uhmcs.utils.MyLabeksPrinterHelper;
import com.uhm.uhmcs.utils.MyUsbDeviceHelper;
import com.uhm.uhmcs.utils.UserUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PrintLabelsPopupWindow {
    private PopupWindow popupWindow;
    private Activity context;
    private ShopAdapter shopAdapter;
    private RecyclerView shop_rv;

    private TextView all_select;
    private boolean is_all_select = false;
    private LinearLayout all_select_btn;

    // 用于显示当前标签打印机名称的 TextView
    private TextView current_printer_tv;

    public PrintLabelsPopupWindow(Activity context) {
        this.context = context;
        initPopup();
    }

    private void initPopup() {
        View popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_print_labels, null);
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
            context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int x = (metrics.widthPixels - popupView.getWidth()) / 2;
            int y = (metrics.heightPixels - popupView.getHeight()) / 2;
            popupWindow.update(x, y, -1, -1);
        });

        // --- 核心优化：获取并显示当前标签打印机的“友好名称” ---
        current_printer_tv = popupView.findViewById(R.id.dqxz_tv); // 借用现有的 ID 或确保 XML 中有对应 ID
        displayCurrentPrinterName();

        // 选择商品按钮
        popupView.findViewById(R.id.shop_btn).setOnClickListener(v -> {
            new ShopPopupWindow(context, grouponGoodsModelArrayList -> {
                is_all_select = true;
                all_select.setBackgroundResource(R.mipmap.checkbox_2);
                // 新加入的商品默认设为选中
                for (GrouponGoodsBean.GrouponGoodsModel model : grouponGoodsModelArrayList) {
                    model.setSelected(true);
                }
                shopAdapter.setNewData(grouponGoodsModelArrayList);
            }).show();
        });

        shop_rv = popupView.findViewById(R.id.shop_rv);
        shop_rv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        shopAdapter = new ShopAdapter();
        shop_rv.setAdapter(shopAdapter);

        // 单项点击监听：自动更新“全选”状态
        shopAdapter.setOnItemClickListener((adapter, view, position) -> {
            GrouponGoodsBean.GrouponGoodsModel item = shopAdapter.getData().get(position);
            item.setSelected(!item.isSelected());

            // 实时检查是否所有项都已选中，从而同步全选按钮状态
            syncAllSelectState();
            shopAdapter.notifyItemChanged(position);
        });

        all_select = popupView.findViewById(R.id.all_select);
        all_select_btn = popupView.findViewById(R.id.all_select_btn);

        // 全选按钮逻辑
        all_select_btn.setOnClickListener(v -> {
            is_all_select = !is_all_select;
            all_select.setBackgroundResource(is_all_select ? R.mipmap.checkbox_2 : R.mipmap.checkbox_1);
            for (GrouponGoodsBean.GrouponGoodsModel model : shopAdapter.getData()) {
                model.setSelected(is_all_select);
            }
            shopAdapter.notifyDataSetChanged();
        });

        popupView.findViewById(R.id.fanhui_btn).setOnClickListener(v -> popupWindow.dismiss());

        // 清空列表
        popupView.findViewById(R.id.all_delete_btn).setOnClickListener(v -> {
            shopAdapter.setNewData(new ArrayList<>());
            is_all_select = false;
            all_select.setBackgroundResource(R.mipmap.checkbox_1);
        });

        // 移除选中项
        popupView.findViewById(R.id.delete_btn).setOnClickListener(v -> {
            List<GrouponGoodsBean.GrouponGoodsModel> filteredList = shopAdapter.getData().stream()
                    .filter(model -> !model.isSelected())
                    .collect(Collectors.toList());
            shopAdapter.setNewData(filteredList);

            if (shopAdapter.getItemCount() == 0) {
                is_all_select = false;
                all_select.setBackgroundResource(R.mipmap.checkbox_1);
            } else {
                syncAllSelectState();
            }
        });

        // 打印逻辑
        popupView.findViewById(R.id.print_btn).setOnClickListener(v -> {
            ArrayList<GrouponGoodsBean.GrouponGoodsModel> selectedList = shopAdapter.getData().stream()
                    .filter(GrouponGoodsBean.GrouponGoodsModel::isSelected)
                    .collect(Collectors.toCollection(ArrayList::new));

            if (selectedList.isEmpty()) {
                return;
            }

            // 弹出打印数量确认框
            new PrintNumPopupWindow(context, discount -> {
                MyLabeksPrinterHelper.getInstance().asyncPrintCheckout(context, selectedList, Integer.parseInt(discount));
                popupWindow.dismiss();
            }).show();
        });
    }

    /**
     * 查找并显示当前标签打印机的人性化名称
     */
    private void displayCurrentPrinterName() {
        if (current_printer_tv == null) return;

        int vid = UserUtils.getInstance().getLABEKS_VENDOR_ID();
        int pid = UserUtils.getInstance().getLABEKS_PRODUCT_ID();

        List<UsbDevice> deviceList = MyUsbDeviceHelper.getInstance().getDeviceList();
        String friendlyName = "未连接标签打印机";

        for (UsbDevice device : deviceList) {
            if (device.getVendorId() == vid && device.getProductId() == pid) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    friendlyName = device.getProductName();
                    if (TextUtils.isEmpty(friendlyName)) {
                        friendlyName = device.getManufacturerName();
                    }
                }
                if (TextUtils.isEmpty(friendlyName)) {
                    friendlyName = "USB 标签打印机 (" + vid + ")";
                }
                break;
            }
        }
        current_printer_tv.setText(friendlyName);
    }

    /**
     * 同步全选按钮的 UI 状态
     */
    private void syncAllSelectState() {
        if (shopAdapter.getData().isEmpty()) {
            is_all_select = false;
        } else {
            is_all_select = shopAdapter.getData().stream().allMatch(GrouponGoodsBean.GrouponGoodsModel::isSelected);
        }
        all_select.setBackgroundResource(is_all_select ? R.mipmap.checkbox_2 : R.mipmap.checkbox_1);
    }

    public void show() {
        View rootView = context.getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
    }
}