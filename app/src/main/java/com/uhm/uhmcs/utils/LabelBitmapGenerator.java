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

/**
 * 标签图片生成器 - 使用专用模板 (item_label_template)
 */
public class LabelBitmapGenerator {

    private static final float MM_TO_DP = 8f;
    private static final int BARCODE_FIXED_WIDTH_MM = 30;
    private static final int BARCODE_HEIGHT_MM = 10;

    public static Bitmap generateLabelBitmap(Context context, GrouponGoodsBean.GrouponGoodsModel goods) {
        try {
            // 1. ⭐ 修改点：直接加载专用的打印模板布局
            // 该布局的根节点就是 FrameLayout (id 为 layout_preview)
            View layoutPreview = LayoutInflater.from(context).inflate(R.layout.item_label_template, null);

            if (layoutPreview == null) return null;

            // 2. 尺寸计算
            int mmWidth = UserUtils.getInstance().getLabelWidth(context);
            int mmHeight = UserUtils.getInstance().getLabelHeight(context);
            if (mmWidth <= 0) mmWidth = 100;
            if (mmHeight <= 0) mmHeight = 40;

            float density = context.getResources().getDisplayMetrics().density;
            int pxWidth = (int) (mmWidth * MM_TO_DP * density);
            int pxHeight = (int) (mmHeight * MM_TO_DP * density);

            // 强制设置根布局宽高
            layoutPreview.setLayoutParams(new FrameLayout.LayoutParams(pxWidth, pxHeight));

            // 3. 绑定所有控件 (ID 需与 item_label_template 保持一致)
            TextView tvShop = layoutPreview.findViewById(R.id.tv_preview_shop);
            TextView tvName = layoutPreview.findViewById(R.id.tv_preview_name);
            TextView tvPrice = layoutPreview.findViewById(R.id.tv_preview_price);
            LinearLayout layoutBarcode = layoutPreview.findViewById(R.id.layout_barcode_group);
            ImageView ivBarcode = layoutPreview.findViewById(R.id.iv_preview_barcode);
            TextView tvCode = layoutPreview.findViewById(R.id.tv_preview_code);
            TextView tvSn = layoutPreview.findViewById(R.id.tv_preview_sn);
            TextView tvSpecs = layoutPreview.findViewById(R.id.tv_preview_specs);
            TextView tvUnit = layoutPreview.findViewById(R.id.tv_preview_unit);
            TextView tvStaff = layoutPreview.findViewById(R.id.tv_preview_staff);
            TextView tvLevel = layoutPreview.findViewById(R.id.tv_preview_level);
            TextView tvOrigin = layoutPreview.findViewById(R.id.tv_preview_origin);
            TextView tvPoints = layoutPreview.findViewById(R.id.tv_preview_points);
            TextView tvCoupon = layoutPreview.findViewById(R.id.tv_preview_coupon);

            layoutPreview.setBackgroundColor(Color.WHITE);

            // 4. 应用 CheckBox 显隐控制
            setViewVisibility(context, tvShop, "shop_name");
            setViewVisibility(context, tvName, "product_name");
            setViewVisibility(context, tvPrice, "price");
            setViewVisibility(context, layoutBarcode, "barcode");
            setViewVisibility(context, tvSn, "sn");
            setViewVisibility(context, tvSpecs, "specs");
            setViewVisibility(context, tvUnit, "unit");
            setViewVisibility(context, tvStaff, "staff");
            setViewVisibility(context, tvLevel, "level");
            setViewVisibility(context, tvOrigin, "origin");
            setViewVisibility(context, tvPoints, "points");
            setViewVisibility(context, tvCoupon, "coupon");

            // 5. 应用 DIY 选定的字号
            applyElementStyle(context, tvShop, "shop");
            applyElementStyle(context, tvName, "name");
            applyElementStyle(context, tvPrice, "price");
            applyElementStyle(context, tvSn, "sn");
            applyElementStyle(context, tvSpecs, "specs");
            applyElementStyle(context, tvUnit, "unit");
            applyElementStyle(context, tvOrigin, "origin");
            applyElementStyle(context, tvPoints, "points");
            applyElementStyle(context, tvCoupon, "coupon");
            applyElementStyle(context, tvCode, "barcode");

            // 6. 填充真实数据
            if (goods != null) {
                String symbol = CurrencyUtils.getSymbol();

                String shopName = "";
                try {
                    if (UserUtils.getInstance().getShopDataBean() != null && UserUtils.getInstance().getShopDataBean().getData() != null) {
                        shopName = UserUtils.getInstance().getShopDataBean().getData().get(0).getName();
                    }
                } catch (Exception e) {}
                tvShop.setText(TextUtils.isEmpty(shopName) ? "店铺" : shopName);

                // ⭐ 优化：处理商品名称自动换行，不使用省略号
                if (tvName != null) {
                    tvName.setText(goods.getTitle());
                    tvName.setSingleLine(false);
                    tvName.setEllipsize(null);
                    tvName.setMaxLines(3);
                    tvName.setLineSpacing(0, 0.8f);
                    tvName.setMaxWidth((int) (pxWidth * 0.9)); // 限制宽度强制换行
                }

                tvPrice.setText(symbol + goods.getPrice());
                tvSn.setText(goods.getSn());
                tvSpecs.setText(TextUtils.isEmpty(goods.getSpecs_title()) ? "" : goods.getSpecs_title());
                tvUnit.setText(TextUtils.isEmpty(goods.getUnit()) ? "1" : goods.getUnit());
                tvOrigin.setText(TextUtils.isEmpty(goods.getSubtitle()) ? "" : goods.getSubtitle());
                tvStaff.setText("物价员");
                tvLevel.setText("合格品");

                String points = String.valueOf(goods.getReward_points());
                tvPoints.setText((TextUtils.isEmpty(points) || "null".equalsIgnoreCase(points)) ? "0.00" : points);

                String coupon = String.valueOf(goods.getDeduction_golive());
                tvCoupon.setText((TextUtils.isEmpty(coupon) || "null".equalsIgnoreCase(coupon)) ? "0.00" : symbol + coupon);

                String barcodeStr = TextUtils.isEmpty(goods.getSn()) ? goods.getGoods_sn() : goods.getSn();
                if (!TextUtils.isEmpty(barcodeStr)) {
                    tvCode.setText(barcodeStr);
                    ivBarcode.setImageBitmap(createBarcode(barcodeStr));
                } else {
                    tvCode.setText("");
                    ivBarcode.setImageBitmap(null);
                }
            }

            // 7. 条码布局处理 (保持比例)
            int barcodePxWidth = (int) (BARCODE_FIXED_WIDTH_MM * MM_TO_DP * density);
            int barcodePxHeight = (int) (BARCODE_HEIGHT_MM * MM_TO_DP * density);

            if (layoutBarcode != null) {
                ViewGroup.LayoutParams params = layoutBarcode.getLayoutParams();
                params.width = barcodePxWidth;
                layoutBarcode.setLayoutParams(params);

                if (ivBarcode != null) {
                    ViewGroup.LayoutParams imgParams = ivBarcode.getLayoutParams();
                    imgParams.width = barcodePxWidth;
                    imgParams.height = barcodePxHeight;
                    ivBarcode.setLayoutParams(imgParams);
                    ivBarcode.setScaleType(ImageView.ScaleType.FIT_XY);
                }
            }

            // 8. 强制测量与布局 (关键步)
            layoutPreview.measure(
                    View.MeasureSpec.makeMeasureSpec(pxWidth, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(pxHeight, View.MeasureSpec.EXACTLY)
            );
            layoutPreview.layout(0, 0, pxWidth, pxHeight);

            // 9. 应用坐标 (在测绘完成后应用用户保存的 X, Y)
            applyPosition(context, tvShop, "shop");
            applyPosition(context, tvName, "name");
            applyPosition(context, tvPrice, "price");
            applyPosition(context, layoutBarcode, "barcode");
            applyPosition(context, tvSn, "sn");
            applyPosition(context, tvSpecs, "specs");
            applyPosition(context, tvUnit, "unit");
            applyPosition(context, tvStaff, "staff");
            applyPosition(context, tvLevel, "level");
            applyPosition(context, tvOrigin, "origin");
            applyPosition(context, tvPoints, "points");
            applyPosition(context, tvCoupon, "coupon");

            // 10. 生成最终位图
            Bitmap bitmap = Bitmap.createBitmap(pxWidth, pxHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            layoutPreview.draw(canvas);

            return bitmap;

        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void applyElementStyle(Context context, TextView tv, String key) {
        if (tv == null) return;
        int size = UserUtils.getInstance().getElementTextSize(context, key, 16);
        tv.setTextSize(size);
        tv.getPaint().setFakeBoldText(true);
    }

    private static void setViewVisibility(Context context, View view, String configKey) {
        if (view == null) return;
        boolean isShow = UserUtils.getInstance().getLabelConfig(context, configKey, true);
        view.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    private static void applyPosition(Context context, View view, String elementKey) {
        if (view == null) return;
        float x = UserUtils.getInstance().getElementX(context, elementKey);
        float y = UserUtils.getInstance().getElementY(context, elementKey);
        if (x != -1 && y != -1) {
            view.setX(x);
            view.setY(y);
        }
    }

    private static Bitmap createBarcode(String content) {
        try {
            Hashtable<com.google.zxing.EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(com.google.zxing.EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(com.google.zxing.EncodeHintType.MARGIN, 0);
            int width = 1600;
            int height = 500;
            BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.CODE_128, width, height, hints);
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    pixels[y * width + x] = matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (Exception e) { return null; }
    }
}