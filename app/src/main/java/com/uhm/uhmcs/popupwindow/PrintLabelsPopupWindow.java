package com.uhm.uhmcs.popupwindow;

import android.app.Activity;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
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
        // 加载新 App 的布局
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

        // --- 0. 打印机状态显示与点击选择 ---
        current_printer_tv = popupView.findViewById(R.id.dqxz_tv);
        displayCurrentPrinterName(); // 初始加载已保存的设备

        if (current_printer_tv != null) {
            current_printer_tv.setOnClickListener(v -> {
                // 弹出你刚创建好的 PrintDevicePopupWindow
                new PrintDevicePopupWindow(context, 2, new PopupWindowOnClickListener.PrintDeviceOnClickListener() {
                    @Override
                    public void onClick(UsbDevice selectedDevice) {
                        try {
                            // 尝试建立连接
                            UsbDeviceConnection connection = MyUsbDeviceHelper.getInstance().connectDevice(selectedDevice);
                            if (connection != null) {
                                MyLabeksPrinterHelper.getInstance().connectAndPrint(selectedDevice, connection);
                                displayCurrentPrinterName(); // 刷新界面名称
                                Toast.makeText(context, "设备已就绪", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(context, "连接失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).show();
            });
        }

        // --- 1. 标签设置按钮逻辑 ---
        View settingBtn = popupView.findViewById(R.id.setting_btn);
        if (settingBtn != null) {
            settingBtn.setOnClickListener(v -> {
                GrouponGoodsBean.GrouponGoodsModel sample = null;
                if (shopAdapter != null && shopAdapter.getData() != null && !shopAdapter.getData().isEmpty()) {
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
                }
                new LabelDiyPopupWindow(context, sample).show();
            });
        }

        // --- 2. 选择商品按钮 ---
        popupView.findViewById(R.id.shop_btn).setOnClickListener(v -> {
            new ShopPopupWindow(context, grouponGoodsModelArrayList -> {
                is_all_select = true;
                if (all_select != null) {
                    all_select.setBackgroundResource(R.mipmap.checkbox_2);
                }
                for (GrouponGoodsBean.GrouponGoodsModel model : grouponGoodsModelArrayList) {
                    model.setSelected(true);
                }
                shopAdapter.setNewData(grouponGoodsModelArrayList);
            }).show();
        });

        // --- 3. 核心打印执行逻辑 ---

        popupView.findViewById(R.id.print_btn).setOnClickListener(v -> {
            ArrayList<GrouponGoodsBean.GrouponGoodsModel> selectedList = shopAdapter.getData().stream()
                    .filter(GrouponGoodsBean.GrouponGoodsModel::isSelected)
                    .collect(Collectors.toCollection(ArrayList::new));

            if (selectedList.isEmpty()) {
                Toast.makeText(context, "请先选择商品", Toast.LENGTH_SHORT).show();
                return;
            }

            // 找到 PrintLabelsPopupWindow.java 中的打印按钮点击事件，替换核心逻辑：
            new PrintNumPopupWindow(context, numStr -> {
                int copies = Integer.parseInt(numStr);

                // 1. 创建并显示进度锁定弹窗
                PrintProgressPopupWindow progressPopup = new PrintProgressPopupWindow(context);
                progressPopup.show();

                new Thread(() -> {
                    int successCount = 0;
                    int skipCount = 0;
                    int totalItems = selectedList.size();
                    int totalLabels = totalItems * copies;
                    int currentLabelIndex = 0;

                    for (int i = 0; i < totalItems; i++) {
                        GrouponGoodsBean.GrouponGoodsModel goods = selectedList.get(i);
                        String code = TextUtils.isEmpty(goods.getSn()) ? goods.getGoods_sn() : goods.getSn();

                        if (TextUtils.isEmpty(code)) {
                            skipCount += copies;
                            continue;
                        }

                        // 1. 生成图片（每个商品只生成一次，节省 CPU）
                        final Bitmap[] holder = new Bitmap[1];
                        CountDownLatch latch = new CountDownLatch(1);
                        context.runOnUiThread(() -> {
                            try {
                                holder[0] = LabelBitmapGenerator.generateLabelBitmap(context, goods);
                            } finally {
                                latch.countDown();
                            }
                        });
                        try { latch.await(); } catch (Exception ignored) {}

                        Bitmap bmp = holder[0];
                        if (bmp != null) {
                            for (int j = 0; j < copies; j++) {
                                currentLabelIndex++;
                                final int progressValue = currentLabelIndex;
                                context.runOnUiThread(() -> progressPopup.update(progressValue, totalLabels));

                                try {
                                    // --- 核心修正点 1：精准获取宽高 ---
                                    int w = UserUtils.getInstance().getLabelWidth(context);
                                    int h = UserUtils.getInstance().getLabelHeight(context);

                                    // 强制纠错：如果获取不到，给一个标准值
                                    if (w <= 0) w = 100;
                                    if (h <= 0) h = 40;

                                    // --- 核心修正点 2：打印指令 ---
                                    // 这里份数必须传 1，我们要靠循环来保证每一张都是独立的任务
                                    MyLabeksPrinterHelper.getInstance().printBitmapLabel(context, bmp, w, h, 1);
                                    successCount++;

                                    // --- 核心修正点 3：物理延时 ---
                                    // 标签打印机需要时间把纸吐出来并找到下一张的“黑标/间隙”。
                                    // 600ms 太快了，容易错位。建议改为 1200ms 甚至 1500ms
                                    Thread.sleep(1200);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    // 打印结束处理
                    final int s = successCount, k = skipCount;
                    context.runOnUiThread(() -> {
                        if (progressPopup != null) progressPopup.dismiss();

                        // 自动取消勾选逻辑...
                        for (GrouponGoodsBean.GrouponGoodsModel m : shopAdapter.getData()) {
                            m.setSelected(false);
                        }
                        shopAdapter.notifyDataSetChanged();
                        is_all_select = false;
                        if (all_select != null) all_select.setBackgroundResource(R.mipmap.checkbox_1);

                        DeleteShopPopupWindow resultPopup = new DeleteShopPopupWindow(context,
                                "批量打印完毕\n成功出纸: " + s + " 张", true);
                        resultPopup.setDismissTime(3000);
                        resultPopup.show();
                    });
                }).start();
            }).show();
        });






        // --- 4. 列表初始化 ---
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

        // --- 5. 全选与按钮逻辑 ---
        all_select = popupView.findViewById(R.id.all_select);
        all_select_btn = popupView.findViewById(R.id.all_select_btn);
        if (all_select_btn != null) {
            all_select_btn.setOnClickListener(v -> {
                is_all_select = !is_all_select;
                if (all_select != null) {
                    all_select.setBackgroundResource(is_all_select ? R.mipmap.checkbox_2 : R.mipmap.checkbox_1);
                }
                for (GrouponGoodsBean.GrouponGoodsModel model : shopAdapter.getData()) {
                    model.setSelected(is_all_select);
                }
                shopAdapter.notifyDataSetChanged();
            });
        }

        popupView.findViewById(R.id.fanhui_btn).setOnClickListener(v -> popupWindow.dismiss());

        popupView.findViewById(R.id.all_delete_btn).setOnClickListener(v -> {
            shopAdapter.setNewData(new ArrayList<>());
            is_all_select = false;
            if (all_select != null) all_select.setBackgroundResource(R.mipmap.checkbox_1);
        });

        popupView.findViewById(R.id.delete_btn).setOnClickListener(v -> {
            List<GrouponGoodsBean.GrouponGoodsModel> filtered = shopAdapter.getData().stream()
                    .filter(model -> !model.isSelected()).collect(Collectors.toList());
            shopAdapter.setNewData(filtered);
            syncAllSelectState();
        });
    }

    /**
     * 更新当前连接或保存的打印机名称
     */
    private void displayCurrentPrinterName() {
        if (current_printer_tv == null) return;
        int vid = UserUtils.getInstance().getLABEKS_VENDOR_ID();
        int pid = UserUtils.getInstance().getLABEKS_PRODUCT_ID();

        List<UsbDevice> deviceList = MyUsbDeviceHelper.getInstance().getDeviceList();
        String friendlyName = "未连接打印机 (点击选择)";

        for (UsbDevice device : deviceList) {
            if (device.getVendorId() == vid && device.getProductId() == pid) {
                friendlyName = "已连：" + (device.getProductName() != null ? device.getProductName() : "USB设备");
                break;
            }
        }
        current_printer_tv.setText(friendlyName);
    }

    private void syncAllSelectState() {
        if (all_select == null || shopAdapter == null) return;
        if (shopAdapter.getData().isEmpty()) {
            is_all_select = false;
        } else {
            is_all_select = shopAdapter.getData().stream().allMatch(GrouponGoodsBean.GrouponGoodsModel::isSelected);
        }
        all_select.setBackgroundResource(is_all_select ? R.mipmap.checkbox_2 : R.mipmap.checkbox_1);
    }

    public void show() {
        if (context != null && !context.isFinishing()) {
            popupWindow.showAtLocation(context.getWindow().getDecorView(), Gravity.NO_GRAVITY, 0, 0);
        }
    }
}