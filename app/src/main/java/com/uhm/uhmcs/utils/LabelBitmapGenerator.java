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
 * 标签图片生成器 - 已修正规格与产地的映射逻辑
 */
public class LabelBitmapGenerator {

    private static final float MM_TO_DP = 8f;
    private static final int BARCODE_FIXED_WIDTH_MM = 35;

    public static Bitmap generateLabelBitmap(Context context, GrouponGoodsBean.GrouponGoodsModel goods) {
        try {
            // 1. 加载整个 DIY 布局
            View fullView = LayoutInflater.from(context).inflate(R.layout.popupwindow_label_diy, null);

            // 定位预览布局
            FrameLayout layoutPreview = fullView.findViewById(R.id.layout_preview);
            if (layoutPreview == null) return null;

            // 2. 尺寸计算
            int mmWidth = UserUtils.getInstance().getLabelWidth(context);
            int mmHeight = UserUtils.getInstance().getLabelHeight(context);
            if (mmWidth <= 0) mmWidth = 100;
            if (mmHeight <= 0) mmHeight = 40;

            float density = context.getResources().getDisplayMetrics().density;
            int pxWidth = (int) (mmWidth * MM_TO_DP * density);
            int pxHeight = (int) (mmHeight * MM_TO_DP * density);

            layoutPreview.setLayoutParams(new FrameLayout.LayoutParams(pxWidth, pxHeight));

            // 3. 绑定所有控件
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

            // 隐藏背景
            View ivPaperBg = layoutPreview.findViewById(R.id.iv_paper_background);
            if (ivPaperBg != null) ivPaperBg.setVisibility(View.GONE);

            layoutPreview.setBackgroundColor(Color.WHITE);
            layoutPreview.setScaleX(1.0f);
            layoutPreview.setScaleY(1.0f);

            // 4. 应用 CheckBox 显隐控制 (严格匹配 UserUtils 保存的 Key)
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

            // 5. 填充真实数据 (修正映射关系)
            if (goods != null) {
                // 店名
                String shopName = "";
                try {
                    if (UserUtils.getInstance().getShopDataBean() != null && UserUtils.getInstance().getShopDataBean().getData() != null) {
                        shopName = UserUtils.getInstance().getShopDataBean().getData().get(0).getName();
                    }
                } catch (Exception e) {}
                tvShop.setText(TextUtils.isEmpty(shopName) ? "店铺" : shopName);

                // 品名/价格
                tvName.setText(goods.getTitle());
                tvPrice.setText(goods.getPrice() + "元");

                // 编码
                tvSn.setText(goods.getSn());

                // 【关键修正】：规格展示真实的 Specs_title
                tvSpecs.setText(TextUtils.isEmpty(goods.getSpecs_title()) ? "" : goods.getSpecs_title());

                // 单位
                tvUnit.setText(TextUtils.isEmpty(goods.getUnit()) ? "1" : goods.getUnit());

                // 【关键修正】：产地控件显示 Subtitle (对应你之前的业务逻辑)
                tvOrigin.setText(TextUtils.isEmpty(goods.getSubtitle()) ? "" : goods.getSubtitle());

                // 固定文本
                tvStaff.setText("物价员");
                tvLevel.setText("合格品");

                // 积分与券
                String points = String.valueOf(goods.getReward_points());
                tvPoints.setText((TextUtils.isEmpty(points) || "null".equalsIgnoreCase(points)) ? "0.00" : points);
                String coupon = String.valueOf(goods.getDeduction_golive());
                tvCoupon.setText((TextUtils.isEmpty(coupon) || "null".equalsIgnoreCase(coupon)) ? "0.00" : coupon);

                // 条码
                String barcodeStr = TextUtils.isEmpty(goods.getSn()) ? goods.getGoods_sn() : goods.getSn();
                if (!TextUtils.isEmpty(barcodeStr)) {
                    tvCode.setText(TextUtils.isEmpty(goods.getGoods_sn()) ? barcodeStr : goods.getGoods_sn());
                    ivBarcode.setImageBitmap(createBarcode(barcodeStr));
                }
            }

            // 6. 条码宽度处理
            int barcodePxWidth = (int) (BARCODE_FIXED_WIDTH_MM * MM_TO_DP * density);
            if (layoutBarcode != null) {
                ViewGroup.LayoutParams params = layoutBarcode.getLayoutParams();
                params.width = Math.min(barcodePxWidth, pxWidth);
                layoutBarcode.setLayoutParams(params);
            }

            // 7. 强制测量布局
            layoutPreview.measure(
                    View.MeasureSpec.makeMeasureSpec(pxWidth, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(pxHeight, View.MeasureSpec.EXACTLY)
            );
            layoutPreview.layout(0, 0, pxWidth, pxHeight);

            // 8. 应用 DIY 保存的绝对坐标 (确保所有字段位置正确)
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

            // 9. 生成 Bitmap
            Bitmap bitmap = Bitmap.createBitmap(pxWidth, pxHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            layoutPreview.draw(canvas);

            return bitmap;

        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
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
            BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.CODE_128, 800, 200, hints);
            int width = matrix.getWidth();
            int height = matrix.getHeight();
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