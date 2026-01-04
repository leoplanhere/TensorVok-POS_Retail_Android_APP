package com.uhm.uhmcs.popupwindow;

import android.app.Activity;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
// 【关键修复】补全 ViewGroup 导入
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.uhm.uhmcs.R;
import com.uhm.uhmcs.adapter.ShopAdapter;
import com.uhm.uhmcs.bean.GrouponGoodsBean;
import com.uhm.uhmcs.utils.LabelBitmapGenerator;
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
    private TextView current_printer_tv;

    public PrintLabelsPopupWindow(Activity context) {
        this.context = context;
        initPopup();
    }

    private void initPopup() {
        View popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_print_labels, null);

        // 这里使用了 ViewGroup，所以必须 import android.view.ViewGroup
        popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true
        );

        popupView.setBackgroundColor(context.getColor(R.color.black60));
        popupWindow.setOutsideTouchable(true);

        popupView.post(() -> {
            DisplayMetrics metrics = new DisplayMetrics();
            context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int x = (metrics.widthPixels - popupView.getWidth()) / 2;
            int y = (metrics.heightPixels - popupView.getHeight()) / 2;
            popupWindow.update(x, y, -1, -1);
        });

        // 获取打印机名称
        try {
            current_printer_tv = popupView.findViewById(R.id.dqxz_tv);
            displayCurrentPrinterName();
        } catch (Exception e) {}

        // --- 按钮事件绑定 ---

        // 1. 标签设置 (DIY)
        popupView.findViewById(R.id.setting_btn).setOnClickListener(v -> {
            GrouponGoodsBean.GrouponGoodsModel sample = null;
            // 尝试获取选中的商品作为预览
            if (shopAdapter.getData() != null && !shopAdapter.getData().isEmpty()) {
                for (GrouponGoodsBean.GrouponGoodsModel m : shopAdapter.getData()) {
                    if (m.isSelected()) {
                        sample = m;
                        break;
                    }
                }
                // 没选中就拿第一个
                if (sample == null) sample = shopAdapter.getData().get(0);
            }
            // 列表为空则造假数据
            if (sample == null) {
                sample = new GrouponGoodsBean.GrouponGoodsModel();
                sample.setTitle("预览商品");
                sample.setPrice("0.00");
                sample.setSn("123456");
            }
            new LabelDiyPopupWindow(context, sample).show();
        });

        // 2. 选择商品
        popupView.findViewById(R.id.shop_btn).setOnClickListener(v -> {
            new ShopPopupWindow(context, grouponGoodsModelArrayList -> {
                is_all_select = true;
                all_select.setBackgroundResource(R.mipmap.checkbox_2);
                for (GrouponGoodsBean.GrouponGoodsModel model : grouponGoodsModelArrayList) {
                    model.setSelected(true);
                }
                shopAdapter.setNewData(grouponGoodsModelArrayList);
            }).show();
        });

        // 3. 打印 (核心功能：批量生成所见即所得图片)
        // 3. 打印 (修复版)
        // 3. 打印 (核心优化：流式队列、自动跳过无效码、防OOM)
        popupView.findViewById(R.id.print_btn).setOnClickListener(v -> {
            // 1. 获取选中的商品
            ArrayList<GrouponGoodsBean.GrouponGoodsModel> selectedList = shopAdapter.getData().stream()
                    .filter(GrouponGoodsBean.GrouponGoodsModel::isSelected)
                    .collect(Collectors.toCollection(ArrayList::new));

            if (selectedList.isEmpty()) {
                Toast.makeText(context, "请先选择商品", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. 弹出数量确认框
            new PrintNumPopupWindow(context, discount -> {
                int copies = Integer.parseInt(discount);
                Toast.makeText(context, "开始打印 " + selectedList.size() + " 个商品...", Toast.LENGTH_SHORT).show();

                // 关闭弹窗，避免遮挡
                popupWindow.dismiss();

                // 3. 开启单线程任务，串行处理
                new Thread(() -> {
                    int successCount = 0;
                    int skipCount = 0;

                    for (GrouponGoodsBean.GrouponGoodsModel goods : selectedList) {
                        // --- 步骤 A: 预检查 SN 码 ---
                        String code = goods.getSn();
                        if (TextUtils.isEmpty(code)) code = goods.getGoods_sn();

                        // 如果没有有效条码，直接跳过
                        if (TextUtils.isEmpty(code)) {
                            skipCount++;
                            continue;
                        }

                        // --- 步骤 B: 在主线程生成 Bitmap (必须在主线程 measure/layout) ---
                        // 使用数组来跨线程获取结果
                        final Bitmap[] holder = new Bitmap[1];
                        final GrouponGoodsBean.GrouponGoodsModel currentGoods = goods;

                        // 同步等待主线程生成完毕
                        context.runOnUiThread(() -> {
                            try {
                                holder[0] = LabelBitmapGenerator.generateLabelBitmap(context, currentGoods);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            // 唤醒等待的子线程
                            synchronized (holder) {
                                holder.notify();
                            }
                        });

                        // 子线程阻塞等待主线程完成图片生成
                        synchronized (holder) {
                            try {
                                if (holder[0] == null) holder.wait(2000); // 最多等2秒
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        Bitmap bmp = holder[0];

                        // --- 步骤 C: 发送打印并回收 ---
                        if (bmp != null) {
                            try {
                                // 1. 发送打印 (打印单张)
                                // 注意：这里我们复用 printBitmapLabel 方法，它内部是异步的，
                                // 为了防止 USB 缓冲区溢出，我们需要稍微 sleep 一下，或者让 printerHelper 提供同步方法。
                                // 这里简单起见，调用后 sleep 300ms 缓冲一下。
                                MyLabeksPrinterHelper.getInstance().printBitmapLabel(context, bmp, copies);

                                successCount++;

                                // 简单限流，防止打印机卡死
                                Thread.sleep(500);

                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                // 2. 【关键防OOM】立即回收 Bitmap
                                if (!bmp.isRecycled()) {
                                    bmp.recycle();
                                }
                                bmp = null;
                            }
                        }
                    }

                    // 全部完成后提示
                    final int s = successCount;
                    final int k = skipCount;
                    context.runOnUiThread(() ->
                            Toast.makeText(context, "打印完成: 成功 " + s + ", 跳过无码 " + k, Toast.LENGTH_LONG).show()
                    );

                }).start();

            }).show();
        });



        // ... 列表逻辑 ...
        shop_rv = popupView.findViewById(R.id.shop_rv);
        shop_rv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        shopAdapter = new ShopAdapter();
        shop_rv.setAdapter(shopAdapter);

        shopAdapter.setOnItemClickListener((adapter, view, position) -> {
            GrouponGoodsBean.GrouponGoodsModel item = shopAdapter.getData().get(position);
            item.setSelected(!item.isSelected());
            syncAllSelectState();
            shopAdapter.notifyItemChanged(position);
        });

        all_select = popupView.findViewById(R.id.all_select);
        all_select_btn = popupView.findViewById(R.id.all_select_btn);

        all_select_btn.setOnClickListener(v -> {
            is_all_select = !is_all_select;
            all_select.setBackgroundResource(is_all_select ? R.mipmap.checkbox_2 : R.mipmap.checkbox_1);
            for (GrouponGoodsBean.GrouponGoodsModel model : shopAdapter.getData()) {
                model.setSelected(is_all_select);
            }
            shopAdapter.notifyDataSetChanged();
        });

        popupView.findViewById(R.id.fanhui_btn).setOnClickListener(v -> popupWindow.dismiss());

        popupView.findViewById(R.id.all_delete_btn).setOnClickListener(v -> {
            shopAdapter.setNewData(new ArrayList<>());
            is_all_select = false;
            all_select.setBackgroundResource(R.mipmap.checkbox_1);
        });

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
    }

    private void displayCurrentPrinterName() {
        if (current_printer_tv == null) return;
        int vid = UserUtils.getInstance().getLABEKS_VENDOR_ID();
        List<UsbDevice> deviceList = MyUsbDeviceHelper.getInstance().getDeviceList();
        String friendlyName = "未连接标签打印机";
        for (UsbDevice device : deviceList) {
            if (device.getVendorId() == vid) {
                friendlyName = "USB 标签打印机 (" + vid + ")";
                break;
            }
        }
        current_printer_tv.setText(friendlyName);
    }

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