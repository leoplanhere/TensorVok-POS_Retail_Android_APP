package com.uhm.uhmcs.popupwindow;

import android.app.Activity;
import android.graphics.Bitmap;
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
import java.util.concurrent.CountDownLatch;
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

        try {
            current_printer_tv = popupView.findViewById(R.id.dqxz_tv);
            displayCurrentPrinterName();
        } catch (Exception e) {}

        // 1. 标签设置
        popupView.findViewById(R.id.setting_btn).setOnClickListener(v -> {
            GrouponGoodsBean.GrouponGoodsModel sample = null;
            if (shopAdapter.getData() != null && !shopAdapter.getData().isEmpty()) {
                for (GrouponGoodsBean.GrouponGoodsModel m : shopAdapter.getData()) {
                    if (m.isSelected()) {
                        sample = m;
                        break;
                    }
                }
                if (sample == null) sample = shopAdapter.getData().get(0);
            }
            if (sample == null) {
                sample = new GrouponGoodsBean.GrouponGoodsModel();
                sample.setTitle("预览商品名称");
                sample.setPrice("0.00");
                sample.setSn("123456789");
                sample.setSubtitle("深圳");
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

        // 3. 核心打印逻辑 (已修复崩溃问题)
        popupView.findViewById(R.id.print_btn).setOnClickListener(v -> {
            ArrayList<GrouponGoodsBean.GrouponGoodsModel> selectedList = shopAdapter.getData().stream()
                    .filter(GrouponGoodsBean.GrouponGoodsModel::isSelected)
                    .collect(Collectors.toCollection(ArrayList::new));

            if (selectedList.isEmpty()) {
                Toast.makeText(context, "请先选择商品", Toast.LENGTH_SHORT).show();
                return;
            }

            new PrintNumPopupWindow(context, discount -> {
                int copies = Integer.parseInt(discount);
                Toast.makeText(context, "正在准备打印...", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();

                new Thread(() -> {
                    int successCount = 0;
                    int skipCount = 0;

                    for (GrouponGoodsBean.GrouponGoodsModel goods : selectedList) {
                        // A. 基础检查
                        String code = goods.getSn();
                        if (TextUtils.isEmpty(code)) code = goods.getGoods_sn();
                        if (TextUtils.isEmpty(code)) {
                            skipCount++;
                            continue;
                        }

                        // B. 主线程同步生成 Bitmap (使用 Latch 替代 wait)
                        final Bitmap[] holder = new Bitmap[1];
                        CountDownLatch latch = new CountDownLatch(1);

                        context.runOnUiThread(() -> {
                            try {
                                holder[0] = LabelBitmapGenerator.generateLabelBitmap(context, goods);
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                latch.countDown();
                            }
                        });

                        try {
                            latch.await(); // 等待图片生成结束
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        Bitmap bmp = holder[0];

                        // C. 异步发送打印 (修复：不手动执行 recycle)
                        if (bmp != null) {
                            try {
                                // 调用打印机Helper
                                // 1. 先获取保存的标签宽高（mm）
                                int savedW = com.uhm.uhmcs.utils.UserUtils.getInstance().getLabelWidth(context);
                                int savedH = com.uhm.uhmcs.utils.UserUtils.getInstance().getLabelHeight(context);

// 2. 如果获取不到，设置一个默认值（例如 100x40 或 40x30）
                                if (savedW <= 0) savedW = 100;
                                if (savedH <= 0) savedH = 40;

// 3. 使用新的 5 参数方法进行调用
                                MyLabeksPrinterHelper.getInstance().printBitmapLabel(context, bmp, savedW, savedH, copies);

                                successCount++;

                                // 【关键修复】批量打印必须增加等待时间，防止打印机固件死机
                                Thread.sleep(800);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            // 注意：此处绝对不要调用 bmp.recycle()！！
                            // 打印机 Helper 是异步的，它在另一个线程读取此 bmp 的像素。
                            // 在循环结束后或由系统 GC 自动处理。
                        }
                    }

                    final int s = successCount;
                    final int k = skipCount;
                    context.runOnUiThread(() ->
                            Toast.makeText(context, "任务提交完毕: 成功 " + s + ", 跳过 " + k, Toast.LENGTH_LONG).show()
                    );
                }).start();
            }).show();
        });

        // 4. 列表初始化
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