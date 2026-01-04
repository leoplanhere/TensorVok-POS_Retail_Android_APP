package com.uhm.uhmcs.popupwindow;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Hashtable;
import com.google.zxing.EncodeHintType;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.GrouponGoodsBean;
import com.uhm.uhmcs.utils.DragTouchListener;
import com.uhm.uhmcs.utils.MyLabeksPrinterHelper;
import com.uhm.uhmcs.utils.UserUtils;

public class LabelDiyPopupWindow {
    private PopupWindow popupWindow;
    private Activity context;
    private GrouponGoodsBean.GrouponGoodsModel goodsModel;

    private FrameLayout layoutPreview; // 画布
    private FrameLayout flContainer;   // 【新增】画布的父容器，用于计算缩放

    // 比例尺：1mm = 8dp (这是为了在屏幕上模拟显示，打印时我们发图)
    private static final float MM_TO_DP = 8f;

    public LabelDiyPopupWindow(Activity context, GrouponGoodsBean.GrouponGoodsModel goods) {
        this.context = context;
        this.goodsModel = goods;
        init();
    }

    private void init() {
        View view = LayoutInflater.from(context).inflate(R.layout.popupwindow_label_diy, null);
        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        popupWindow.setOutsideTouchable(true);

        // 绑定控件
        layoutPreview = view.findViewById(R.id.layout_preview);
        flContainer = view.findViewById(R.id.fl_container); // 【新增】绑定父容器

        TextView tvShop = view.findViewById(R.id.tv_preview_shop);
        TextView tvName = view.findViewById(R.id.tv_preview_name);
        TextView tvPrice = view.findViewById(R.id.tv_preview_price);
        LinearLayout layoutBarcode = view.findViewById(R.id.layout_barcode_group); // 条码组
        TextView tvCode = view.findViewById(R.id.tv_preview_code);
        ImageView ivBarcode = view.findViewById(R.id.iv_preview_barcode);

        EditText etWidth = view.findViewById(R.id.et_width);
        EditText etHeight = view.findViewById(R.id.et_height);

        CheckBox cbShopName = view.findViewById(R.id.cb_shop_name);
        CheckBox cbProductName = view.findViewById(R.id.cb_product_name);
        CheckBox cbPrice = view.findViewById(R.id.cb_price);
        CheckBox cbBarcode = view.findViewById(R.id.cb_barcode);

        // 1. 启用拖拽 (核心功能)
        DragTouchListener dragListener = new DragTouchListener();
        tvShop.setOnTouchListener(dragListener);
        tvName.setOnTouchListener(dragListener);
        tvPrice.setOnTouchListener(dragListener);
        layoutBarcode.setOnTouchListener(dragListener);

        // 2. 回显数据与配置
        int savedW = UserUtils.getInstance().getLabelWidth(context);
        int savedH = UserUtils.getInstance().getLabelHeight(context);
        etWidth.setText(String.valueOf(savedW));
        etHeight.setText(String.valueOf(savedH));

        // 立即应用一次尺寸，把画布撑开
        updateCanvasSize(savedW, savedH);

        // 填充商品信息
        if (goodsModel != null) {
            String shopName = "";
            try {
                // 【核心修正】改为从 ShopDataBean 获取店铺名称 (getName)
                if (UserUtils.getInstance().getShopDataBean() != null
                        && UserUtils.getInstance().getShopDataBean().getData() != null
                        && !UserUtils.getInstance().getShopDataBean().getData().isEmpty()) {

                    shopName = UserUtils.getInstance().getShopDataBean().getData().get(0).getName();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

// 如果获取失败，使用默认值
            if (TextUtils.isEmpty(shopName)) shopName = "店铺名称未获取";
            tvShop.setText(shopName);
            tvName.setText(goodsModel.getTitle());
            String unit = TextUtils.isEmpty(goodsModel.getUnit()) ? "" : "/" + goodsModel.getUnit();
            tvPrice.setText("￥" + goodsModel.getPrice() + unit);

            String code = getValidCode(goodsModel);
            tvCode.setText(code);
            ivBarcode.setImageBitmap(generateBarcode(code));
        }

        // 3. 恢复上次保存的坐标位置
        // 注意：要在 View 布局完成后恢复，或者简单的延迟恢复，这里我们简单处理
        layoutPreview.post(() -> {
            restorePosition(tvShop, "shop");
            restorePosition(tvName, "name");
            restorePosition(tvPrice, "price");
            restorePosition(layoutBarcode, "barcode");
        });

        // 4. 可见性联动
        setupCheckListener(cbShopName, tvShop);
        setupCheckListener(cbProductName, tvName);
        setupCheckListener(cbPrice, tvPrice);
        setupCheckListener(cbBarcode, layoutBarcode);

        // 初始化勾选状态
        cbShopName.setChecked(UserUtils.getInstance().getLabelConfig(context, "shop_name", true));
        cbProductName.setChecked(UserUtils.getInstance().getLabelConfig(context, "product_name", true));
        cbPrice.setChecked(UserUtils.getInstance().getLabelConfig(context, "price", true));
        cbBarcode.setChecked(UserUtils.getInstance().getLabelConfig(context, "barcode", true));

        // 5. 按钮事件
        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> popupWindow.dismiss());

        // "应用尺寸" 按钮
        view.findViewById(R.id.btn_apply_size).setOnClickListener(v -> {
            String wStr = etWidth.getText().toString();
            String hStr = etHeight.getText().toString();
            if (!TextUtils.isEmpty(wStr) && !TextUtils.isEmpty(hStr)) {
                updateCanvasSize(Integer.parseInt(wStr), Integer.parseInt(hStr));
            }
        });

        // "保存并打印" 按钮
        view.findViewById(R.id.btn_print_preview).setOnClickListener(v -> {
            String w = etWidth.getText().toString();
            String h = etHeight.getText().toString();
            if (TextUtils.isEmpty(w) || TextUtils.isEmpty(h)) {
                Toast.makeText(context, "请输入宽高", Toast.LENGTH_SHORT).show();
                return;
            }

            // A. 保存尺寸配置
            UserUtils.getInstance().setLabelWidth(context, Integer.parseInt(w));
            UserUtils.getInstance().setLabelHeight(context, Integer.parseInt(h));

            // B. 保存开关配置
            UserUtils.getInstance().setLabelConfig(context, "shop_name", cbShopName.isChecked());
            UserUtils.getInstance().setLabelConfig(context, "product_name", cbProductName.isChecked());
            UserUtils.getInstance().setLabelConfig(context, "price", cbPrice.isChecked());
            UserUtils.getInstance().setLabelConfig(context, "barcode", cbBarcode.isChecked());

            // C. 保存元素坐标 (关键!)
            savePosition(tvShop, "shop");
            savePosition(tvName, "name");
            savePosition(tvPrice, "price");
            savePosition(layoutBarcode, "barcode");

            // D. 截图并打印
            // 为了打印清晰，去除选中背景色后再截图
            clearFocusBackground(tvShop, tvName, tvPrice, layoutBarcode);
            Bitmap bitmap = viewToBitmap(layoutPreview);
            if (bitmap != null) {
                MyLabeksPrinterHelper.getInstance().printBitmapLabel(context, bitmap, 1);
                Toast.makeText(context, "保存成功并打印", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            }
        });
    }

    // 【修改】动态调整画布大小，并增加缩放逻辑
    private void updateCanvasSize(int mmWidth, int mmHeight) {
        // 1. 计算目标像素大小 (1mm = 8dp)
        final float density = context.getResources().getDisplayMetrics().density;
        final int targetPxWidth = (int) (mmWidth * MM_TO_DP * density);
        final int targetPxHeight = (int) (mmHeight * MM_TO_DP * density);

        // 2. 设置 LayoutParams (这是真实的物理像素，保证打印清晰度)
        ViewGroup.LayoutParams params = layoutPreview.getLayoutParams();
        params.width = targetPxWidth;
        params.height = targetPxHeight;
        layoutPreview.setLayoutParams(params);

        // 3. 计算屏幕缩放比例 (Scale)，确保大标签也能完整显示
        if (flContainer != null) {
            flContainer.post(() -> {
                int containerW = flContainer.getWidth();
                int containerH = flContainer.getHeight();

                if (containerW == 0 || containerH == 0) return;

                // 留出一点边距
                int availableW = containerW - 40;
                int availableH = containerH - 40;

                float scaleX = 1.0f;
                float scaleY = 1.0f;

                // 计算宽度的缩放比
                if (targetPxWidth > availableW) {
                    scaleX = (float) availableW / targetPxWidth;
                }
                // 计算高度的缩放比
                if (targetPxHeight > availableH) {
                    scaleY = (float) availableH / targetPxHeight;
                }

                // 取较小的比例，保持宽高比
                float finalScale = Math.min(scaleX, scaleY);

                // 如果不需要缩小(scale >= 1)，则保持 1.0，或者你可以允许放大
                if (finalScale > 1.0f) finalScale = 1.0f;

                // 设置缩放中心点为中心
                layoutPreview.setPivotX(targetPxWidth / 2f);
                layoutPreview.setPivotY(targetPxHeight / 2f);

                // 应用缩放
                layoutPreview.setScaleX(finalScale);
                layoutPreview.setScaleY(finalScale);
            });
        }

        layoutPreview.requestLayout();
    }

    private void restorePosition(View view, String key) {
        float x = UserUtils.getInstance().getElementX(context, key);
        float y = UserUtils.getInstance().getElementY(context, key);

        // 如果有保存过，就恢复；否则保持布局文件的默认位置
        if (x != -1 && y != -1) {
            view.setX(x);
            view.setY(y);
        }
    }

    private void savePosition(View view, String key) {
        UserUtils.getInstance().setElementX(context, key, view.getX());
        UserUtils.getInstance().setElementY(context, key, view.getY());
    }

    // 辅助方法：清除拖拽时的临时背景色，打印白底黑字
    private void clearFocusBackground(View... views) {
        for (View v : views) {
            v.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void setupCheckListener(CheckBox cb, View target) {
        target.setVisibility(cb.isChecked() ? View.VISIBLE : View.GONE);
        cb.setOnCheckedChangeListener((v, isChecked) -> {
            target.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
    }

    private String getValidCode(GrouponGoodsBean.GrouponGoodsModel model) {
        String code = model.getSn();
        if (TextUtils.isEmpty(code)) code = model.getGoods_sn();
        if (TextUtils.isEmpty(code)) return "123456";
        return code.replaceAll("[^\\x00-\\x7F]", "").trim();
    }

    private Bitmap viewToBitmap(View view) {
        // 保证画布有纯白底色
        view.setBackgroundColor(Color.WHITE);
        // 注意：这里创建 Bitmap 用的是 view.getWidth/Height，也就是 LayoutParams 设置的原始高分辨率尺寸
        // 无论 View 在屏幕上被 ScaleX/ScaleY 缩放到多小，draw(canvas) 都会绘制出原始尺寸的高清图
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    // 引入需要的包
    // import java.util.Hashtable;
    // import com.google.zxing.EncodeHintType;

    private Bitmap generateBarcode(String content) {
        try {
            // 1. 配置参数：取消白边
            java.util.Hashtable<com.google.zxing.EncodeHintType, Object> hints = new java.util.Hashtable<>();
            hints.put(com.google.zxing.EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(com.google.zxing.EncodeHintType.MARGIN, 0);

            // 2. 提高分辨率 (宽度 1000)
            int width = 1000;
            int height = 300;

            BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.CODE_128, width, height, hints);

            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (matrix.get(x, y)) {
                        pixels[y * width + x] = 0xFF000000;
                    } else {
                        pixels[y * width + x] = 0xFFFFFFFF;
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void show() {
        if (context != null && !context.isFinishing()) {
            popupWindow.showAtLocation(context.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
        }
    }
}