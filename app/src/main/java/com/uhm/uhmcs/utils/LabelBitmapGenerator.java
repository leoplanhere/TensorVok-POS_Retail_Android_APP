package com.uhm.uhmcs.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.GrouponGoodsBean;

import java.util.Hashtable;

public class LabelBitmapGenerator {

    // 比例尺：1mm = 8dp (必须与 DIY 界面一致)
    private static final float MM_TO_DP = 8f;

    // 【自定义】条码固定宽度 (mm)
    // 你要求 >= 30mm，这里设为 35mm 以保证容错率和清晰度
    // 如果纸张很宽(120mm)，条码依然保持这个宽度，不会拉伸变形
    private static final int BARCODE_FIXED_WIDTH_MM = 35;

    public static Bitmap generateLabelBitmap(Context context, GrouponGoodsBean.GrouponGoodsModel goods) {
        try {
            // 1. 加载模板 (必须对应 FrameLayout 的 xml)
            View view = LayoutInflater.from(context).inflate(R.layout.item_label_template, null);

            // 2. 计算标签物理像素尺寸
            int mmWidth = UserUtils.getInstance().getLabelWidth(context);
            int mmHeight = UserUtils.getInstance().getLabelHeight(context);
            if (mmWidth <= 0) mmWidth = 40;
            if (mmHeight <= 0) mmHeight = 30;

            int pxWidth = (int) (mmWidth * MM_TO_DP * context.getResources().getDisplayMetrics().density);
            int pxHeight = (int) (mmHeight * MM_TO_DP * context.getResources().getDisplayMetrics().density);

            // 设置根布局大小 (FrameLayout)
            view.setLayoutParams(new ViewGroup.LayoutParams(pxWidth, pxHeight));

            // 3. 绑定控件
            TextView tvShop = view.findViewById(R.id.tv_preview_shop);
            TextView tvName = view.findViewById(R.id.tv_preview_name);
            TextView tvPrice = view.findViewById(R.id.tv_preview_price);
            LinearLayout layoutBarcode = view.findViewById(R.id.layout_barcode_group);
            TextView tvCode = view.findViewById(R.id.tv_preview_code);
            ImageView ivBarcode = view.findViewById(R.id.iv_preview_barcode);

            // 4. 恢复可见性
            setViewVisibility(context, tvShop, "shop_name");
            setViewVisibility(context, tvName, "product_name");
            setViewVisibility(context, tvPrice, "price");
            setViewVisibility(context, layoutBarcode, "barcode");

            // 5. 填充数据
            if (goods != null) {
                String shopName = "";
                try {
                    // 【核心修正】改为获取 ShopDataBean -> getName()
                    if (UserUtils.getInstance().getShopDataBean() != null
                            && UserUtils.getInstance().getShopDataBean().getData() != null
                            && !UserUtils.getInstance().getShopDataBean().getData().isEmpty()) {

                        shopName = UserUtils.getInstance().getShopDataBean().getData().get(0).getName();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // 默认兜底
                if (TextUtils.isEmpty(shopName)) shopName = "店铺名称未获取";

                tvShop.setText(shopName);
                tvName.setText(goods.getTitle());
                String unit = TextUtils.isEmpty(goods.getUnit()) ? "" : "/" + goods.getUnit();
                tvPrice.setText("￥" + goods.getPrice() + unit);

                // 获取条码，如果为空则返回 null (配合外层跳过逻辑)
                String code = getValidCode(goods);
                if (TextUtils.isEmpty(code)) {
                    return null;
                }

                tvCode.setText(code);
                ivBarcode.setImageBitmap(createBarcode(code));
            }

            // =================================================================================
            // 【核心修复 A】 设置条码为固定宽度 (35mm)，而不是撑满整个标签
            // =================================================================================
            // 计算 35mm 对应的像素
            int barcodePxWidth = (int) (BARCODE_FIXED_WIDTH_MM * MM_TO_DP * context.getResources().getDisplayMetrics().density);

            // 安全检查：如果标签本身小于 35mm (比如 30mm纸)，则最大只能是标签宽
            if (barcodePxWidth > pxWidth) {
                barcodePxWidth = pxWidth;
            }

            ViewGroup.LayoutParams barcodeParams = layoutBarcode.getLayoutParams();
            if (barcodeParams == null) {
                barcodeParams = new FrameLayout.LayoutParams(barcodePxWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
            barcodeParams.width = barcodePxWidth; // 设置为固定宽
            layoutBarcode.setLayoutParams(barcodeParams);

            // 图片填满这个固定宽
            ViewGroup.LayoutParams imgParams = ivBarcode.getLayoutParams();
            imgParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            ivBarcode.setLayoutParams(imgParams);

            // =================================================================================
            // 【核心修复 B】 先测量 (Measure) 和 布局 (Layout)
            // =================================================================================
            view.measure(
                    View.MeasureSpec.makeMeasureSpec(pxWidth, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(pxHeight, View.MeasureSpec.EXACTLY)
            );
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

            // =================================================================================
            // 【核心修复 C】 应用 DIY 保存的坐标
            // =================================================================================
            applyPosition(context, tvShop, "shop");
            applyPosition(context, tvName, "name");
            applyPosition(context, tvPrice, "price");

            // 条码位置特殊处理
            float barcodeX = UserUtils.getInstance().getElementX(context, "barcode");
            float barcodeY = UserUtils.getInstance().getElementY(context, "barcode");

            if (barcodeX != -1 && barcodeY != -1) {
                // 如果用户拖拽保存过位置，使用用户的位置
                layoutBarcode.setX(barcodeX);
                layoutBarcode.setY(barcodeY);
            } else {
                // 如果没保存过：
                // X轴：默认水平居中
                layoutBarcode.setX((pxWidth - barcodePxWidth) / 2f);
                // Y轴：默认放底部
                layoutBarcode.setY(pxHeight - convertDpToPx(context, 85));
            }

            // =================================================================================
            // 【核心修复 D】 绘制
            // =================================================================================
            Bitmap bitmap = Bitmap.createBitmap(pxWidth, pxHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);
            view.draw(canvas);

            return bitmap;

        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void setViewVisibility(Context context, View view, String configKey) {
        boolean isShow = UserUtils.getInstance().getLabelConfig(context, configKey, true);
        view.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    private static void applyPosition(Context context, View view, String elementKey) {
        float x = UserUtils.getInstance().getElementX(context, elementKey);
        float y = UserUtils.getInstance().getElementY(context, elementKey);

        if (x != -1 && y != -1) {
            view.setX(x);
            view.setY(y);
        }
    }

    private static String getValidCode(GrouponGoodsBean.GrouponGoodsModel model) {
        String code = model.getSn();
        if (!TextUtils.isEmpty(code)) return code.replaceAll("[^\\x00-\\x7F]", "").trim();

        code = model.getGoods_sn();
        if (!TextUtils.isEmpty(code)) return code.replaceAll("[^\\x00-\\x7F]", "").trim();

        return null; // 无码返回null
    }

    private static int convertDpToPx(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    private static Bitmap createBarcode(String content) {
        try {
            Hashtable<com.google.zxing.EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(com.google.zxing.EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(com.google.zxing.EncodeHintType.MARGIN, 0);

            // 宽度 1000 保证清晰度
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
            return null;
        }
    }
}