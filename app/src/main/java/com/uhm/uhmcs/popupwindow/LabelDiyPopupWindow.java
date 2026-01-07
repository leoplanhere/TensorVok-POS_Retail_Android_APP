package com.uhm.uhmcs.popupwindow;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
    private FrameLayout flContainer;   // 画布的父容器
    private ImageView ivPaperBackground; // 模拟底图容器

    // 比例尺：1mm = 8dp
    private static final float MM_TO_DP = 8f;

    // UI 元素声明
    private TextView tvShop, tvName, tvPrice, tvCode, tvSn, tvSpecs, tvUnit, tvStaff, tvLevel, tvOrigin, tvPoints, tvCoupon;
    private LinearLayout layoutBarcodeGroup;
    private ImageView ivBarcode;
    private CheckBox cbShopName, cbProductName, cbPrice, cbBarcode, cbSn, cbSpecs, cbUnit, cbStaff, cbLevel, cbOrigin, cbPoints, cbCoupon;

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

        // 1. 绑定基础控件
        layoutPreview = view.findViewById(R.id.layout_preview);
        flContainer = view.findViewById(R.id.fl_container);
        ivPaperBackground = view.findViewById(R.id.iv_paper_background);

        tvShop = view.findViewById(R.id.tv_preview_shop);
        tvName = view.findViewById(R.id.tv_preview_name);
        tvPrice = view.findViewById(R.id.tv_preview_price);
        layoutBarcodeGroup = view.findViewById(R.id.layout_barcode_group);
        tvCode = view.findViewById(R.id.tv_preview_code);
        ivBarcode = view.findViewById(R.id.iv_preview_barcode);

        tvSn = view.findViewById(R.id.tv_preview_sn);
        tvSpecs = view.findViewById(R.id.tv_preview_specs);
        tvUnit = view.findViewById(R.id.tv_preview_unit);
        tvStaff = view.findViewById(R.id.tv_preview_staff);
        tvLevel = view.findViewById(R.id.tv_preview_level);
        tvOrigin = view.findViewById(R.id.tv_preview_origin);
        tvPoints = view.findViewById(R.id.tv_preview_points);
        tvCoupon = view.findViewById(R.id.tv_preview_coupon);

        EditText etWidth = view.findViewById(R.id.et_width);
        EditText etHeight = view.findViewById(R.id.et_height);

        // 3. 绑定所有 CheckBox
        cbShopName = view.findViewById(R.id.cb_shop_name);
        cbProductName = view.findViewById(R.id.cb_product_name);
        cbPrice = view.findViewById(R.id.cb_price);
        cbBarcode = view.findViewById(R.id.cb_barcode);
        cbSn = view.findViewById(R.id.cb_sn);
        cbSpecs = view.findViewById(R.id.cb_specs);
        cbUnit = view.findViewById(R.id.cb_unit);
        cbStaff = view.findViewById(R.id.cb_staff);
        cbLevel = view.findViewById(R.id.cb_level);
        cbOrigin = view.findViewById(R.id.cb_origin);
        cbPoints = view.findViewById(R.id.cb_points);
        cbCoupon = view.findViewById(R.id.cb_coupon);

        // 4. 启用拖拽监听并初始化背景透明及字号切换
        DragTouchListener dragListener = new DragTouchListener();
        View[] draggableElements = {tvShop, tvName, tvPrice, layoutBarcodeGroup, tvSn, tvSpecs, tvUnit, tvStaff, tvLevel, tvOrigin, tvPoints, tvCoupon};

        for (View v : draggableElements) {
            if (v == null) continue;

            // 恢复保存的字号
            String key = getElementKey(v.getId());
            int savedSize = UserUtils.getInstance().getElementTextSize(context, key, 16);
            if (v instanceof TextView) {
                ((TextView) v).setTextSize(savedSize);
            } else if (v.getId() == R.id.layout_barcode_group) {
                // 条码组特殊处理：设置下方数字的字号
                if (tvCode != null) tvCode.setTextSize(savedSize);
            }

            // 设置点击事件：点击一次切换一个档位 (16->20->30)
            v.setOnClickListener(clickedView -> {
                TextView targetTv = null;
                if (clickedView instanceof TextView) {
                    targetTv = (TextView) clickedView;
                } else if (clickedView.getId() == R.id.layout_barcode_group) {
                    targetTv = tvCode;
                }

                if (targetTv != null) {
                    float density = context.getResources().getDisplayMetrics().density;
                    int currentSize = Math.round(targetTv.getTextSize() / density);

                    int nextSize;
                    if (currentSize < 20) nextSize = 20;
                    else if (currentSize < 30) nextSize = 30;
                    else nextSize = 16;

                    targetTv.setTextSize(nextSize);
                    UserUtils.getInstance().setElementTextSize(context, key, nextSize);
                    Toast.makeText(context, "字号已切换至: " + (nextSize == 16 ? "小" : nextSize == 20 ? "中" : "大"), Toast.LENGTH_SHORT).show();
                }
            });

            // 开启拖拽并设为点击可用
            v.setOnTouchListener(dragListener);
            v.setClickable(true);
            v.setBackgroundColor(Color.TRANSPARENT);
        }

        // 5. 初始化画布尺寸
        int savedW = UserUtils.getInstance().getLabelWidth(context);
        int savedH = UserUtils.getInstance().getLabelHeight(context);
        if (savedW <= 0) savedW = 100;
        if (savedH <= 0) savedH = 40;

        etWidth.setText(String.valueOf(savedW));
        etHeight.setText(String.valueOf(savedH));
        updateCanvasSize(savedW, savedH);

        // 同步条码物理尺寸预览
        float density = context.getResources().getDisplayMetrics().density;
        int syncBarWidthPx = (int) (30 * MM_TO_DP * density);
        int syncBarHeightPx = (int) (10 * MM_TO_DP * density);

        if (layoutBarcodeGroup != null) {
            ViewGroup.LayoutParams params = layoutBarcodeGroup.getLayoutParams();
            params.width = syncBarWidthPx;
            layoutBarcodeGroup.setLayoutParams(params);

            if (ivBarcode != null) {
                ViewGroup.LayoutParams imgParams = ivBarcode.getLayoutParams();
                imgParams.width = syncBarWidthPx;
                imgParams.height = syncBarHeightPx;
                ivBarcode.setLayoutParams(imgParams);
                ivBarcode.setScaleType(ImageView.ScaleType.FIT_XY);
            }
        }

        // 6. 交互：点击画布背景(灰色区域)切换底图显示
        if (flContainer != null) {
            flContainer.setOnClickListener(v -> {
                if (ivPaperBackground != null) {
                    boolean isVisible = ivPaperBackground.getVisibility() == View.VISIBLE;
                    ivPaperBackground.setVisibility(isVisible ? View.GONE : View.VISIBLE);
                    UserUtils.getInstance().setLabelConfig(context, "show_background", !isVisible);
                    Toast.makeText(context, isVisible ? "底图已关闭" : "底图已开启", Toast.LENGTH_SHORT).show();
                }
            });
        }
        // 拦截画布点击，防止点击白色区域时意外关闭底图
        if (layoutPreview != null) {
            layoutPreview.setOnClickListener(v -> { /* 消费事件 */ });
        }
        // 读取并应用底图保存状态
        boolean isBgVisible = UserUtils.getInstance().getLabelConfig(context, "show_background", true);
        if (ivPaperBackground != null) {
            ivPaperBackground.setVisibility(isBgVisible ? View.VISIBLE : View.GONE);
        }

        // 7. 填充数据
        fillGoodsData();

        // 8. 恢复坐标位置
        layoutPreview.post(() -> {
            restorePosition(tvShop, "shop");
            restorePosition(tvName, "name");
            restorePosition(tvPrice, "price");
            restorePosition(layoutBarcodeGroup, "barcode");
            restorePosition(tvSn, "sn");
            restorePosition(tvSpecs, "specs");
            restorePosition(tvUnit, "unit");
            restorePosition(tvStaff, "staff");
            restorePosition(tvLevel, "level");
            restorePosition(tvOrigin, "origin");
            restorePosition(tvPoints, "points");
            restorePosition(tvCoupon, "coupon");
        });

        // 9. 绑定显隐联动
        setupCheckListener(cbShopName, tvShop, "shop_name");
        setupCheckListener(cbProductName, tvName, "product_name");
        setupCheckListener(cbPrice, tvPrice, "price");
        setupCheckListener(cbBarcode, layoutBarcodeGroup, "barcode");
        setupCheckListener(cbSn, tvSn, "sn");
        setupCheckListener(cbSpecs, tvSpecs, "specs");
        setupCheckListener(cbUnit, tvUnit, "unit");
        setupCheckListener(cbStaff, tvStaff, "staff");
        setupCheckListener(cbLevel, tvLevel, "level");
        setupCheckListener(cbOrigin, tvOrigin, "origin");
        setupCheckListener(cbPoints, tvPoints, "points");
        setupCheckListener(cbCoupon, tvCoupon, "coupon");

        // 10. 底部按钮事件
        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> popupWindow.dismiss());
        view.findViewById(R.id.btn_apply_size).setOnClickListener(v -> {
            String wStr = etWidth.getText().toString();
            String hStr = etHeight.getText().toString();
            if (!TextUtils.isEmpty(wStr) && !TextUtils.isEmpty(hStr)) {
                updateCanvasSize(Integer.parseInt(wStr), Integer.parseInt(hStr));
            }
        });

        view.findViewById(R.id.btn_print_preview).setOnClickListener(v -> {
            String w = etWidth.getText().toString();
            String h = etHeight.getText().toString();
            if (TextUtils.isEmpty(w) || TextUtils.isEmpty(h)) {
                Toast.makeText(context, "请输入宽高", Toast.LENGTH_SHORT).show();
                return;
            }
            UserUtils.getInstance().setLabelWidth(context, Integer.parseInt(w));
            UserUtils.getInstance().setLabelHeight(context, Integer.parseInt(h));
            saveAllConfigs();

            // 打印前强制隐藏底图
            if (ivPaperBackground != null) ivPaperBackground.setVisibility(View.GONE);
            layoutPreview.setBackgroundColor(Color.WHITE);
            clearFocusBackground(draggableElements);

            Bitmap originalBitmap = viewToBitmap(layoutPreview);
            if (originalBitmap != null) {
                Bitmap printerBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, false);
                MyLabeksPrinterHelper.getInstance().printBitmapLabel(context, printerBitmap, 1);
                // 打印后恢复底图状态
                if (ivPaperBackground != null) {
                    boolean bgState = UserUtils.getInstance().getLabelConfig(context, "show_background", true);
                    ivPaperBackground.setVisibility(bgState ? View.VISIBLE : View.GONE);
                }
                popupWindow.dismiss();
            }
        });
    }

    private String getElementKey(int id) {
        if (id == R.id.tv_preview_shop) return "shop";
        if (id == R.id.tv_preview_name) return "name";
        if (id == R.id.tv_preview_price) return "price";
        if (id == R.id.layout_barcode_group) return "barcode";
        if (id == R.id.tv_preview_sn) return "sn";
        if (id == R.id.tv_preview_specs) return "specs";
        if (id == R.id.tv_preview_unit) return "unit";
        if (id == R.id.tv_preview_origin) return "origin";
        if (id == R.id.tv_preview_points) return "points";
        if (id == R.id.tv_preview_coupon) return "coupon";
        return "default";
    }

    private void fillGoodsData() {
        if (goodsModel != null) {
            String shopName = "";
            try {
                shopName = UserUtils.getInstance().getLoginBase().getData().getUserinfo().getNickname();
            } catch (Exception e) {}
            tvShop.setText(TextUtils.isEmpty(shopName) ? "店铺名称" : shopName);
            tvName.setText(goodsModel.getTitle());
            tvPrice.setText(goodsModel.getPrice() + "元");

            String barcodeContent = TextUtils.isEmpty(goodsModel.getSn()) ? goodsModel.getGoods_sn() : goodsModel.getSn();
            tvCode.setText(TextUtils.isEmpty(goodsModel.getGoods_sn()) ? barcodeContent : goodsModel.getGoods_sn());
            ivBarcode.setImageBitmap(generateBarcode(barcodeContent));

            tvSn.setText(goodsModel.getSn());
            tvSpecs.setText(TextUtils.isEmpty(goodsModel.getSpecs_title()) ? "规格" : goodsModel.getSpecs_title());
            tvUnit.setText(TextUtils.isEmpty(goodsModel.getUnit()) ? "单位：个" : goodsModel.getUnit());
            String originText = goodsModel.getSubtitle();
            tvOrigin.setText(TextUtils.isEmpty(originText) ? "产地/副标题" : originText);
            tvStaff.setText("物价员");
            tvLevel.setText("合格品");

            String points = String.valueOf(goodsModel.getReward_points());
            tvPoints.setText((TextUtils.isEmpty(points) || "null".equalsIgnoreCase(points)) ? "可获积分" : points);
            String coupon = String.valueOf(goodsModel.getDeduction_golive());
            tvCoupon.setText((TextUtils.isEmpty(coupon) || "null".equalsIgnoreCase(coupon)) ? "可用代金券" : coupon);

            applyBlackStyle(tvShop, tvName, tvPrice, tvCode, tvSn, tvSpecs, tvUnit, tvStaff, tvLevel, tvOrigin, tvPoints, tvCoupon);
        }
    }

    private void applyBlackStyle(View... views) {
        for (View v : views) {
            if (v instanceof TextView) {
                ((TextView) v).setTextColor(Color.BLACK);
                ((TextView) v).setPaintFlags(((TextView) v).getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
            }
        }
    }

    private void drawFakePrePrintBackground(int pxW, int pxH) {
        if (ivPaperBackground == null) return;
        Bitmap bgBitmap = Bitmap.createBitmap(pxW, pxH, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bgBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawColor(Color.WHITE);
        paint.setColor(Color.parseColor("#F5E6E6"));
        canvas.drawRect(pxW * 0.52f, 0, pxW, pxH, paint);
        paint.setColor(Color.DKGRAY);
        float textSize = pxH * 0.08f;
        paint.setTextSize(textSize);
        float leftMargin = pxW * 0.02f;
        float firstLineY = pxH * 0.30f;
        float lineGap = pxH * 0.15f;
        canvas.drawText("品牌品名：", leftMargin, firstLineY, paint);
        canvas.drawLine(leftMargin + (paint.measureText("品牌品名：")), firstLineY + 5, pxW * 0.50f, firstLineY + 5, paint);
        canvas.drawText("条码：", leftMargin, firstLineY + lineGap, paint);
        canvas.drawLine(leftMargin + (paint.measureText("条码：")), firstLineY + lineGap + 5, pxW * 0.50f, firstLineY + lineGap + 5, paint);
        canvas.drawText("编号：", leftMargin, firstLineY + lineGap * 2, paint);
        canvas.drawLine(leftMargin + (paint.measureText("编号：")), firstLineY + lineGap * 2 + 5, pxW * 0.25f, firstLineY + lineGap * 2 + 5, paint);
        canvas.drawText("规格：", pxW * 0.26f, firstLineY + lineGap * 2, paint);
        canvas.drawLine(pxW * 0.26f + (paint.measureText("规格：")), firstLineY + lineGap * 2 + 5, pxW * 0.50f, firstLineY + lineGap * 2 + 5, paint);
        canvas.drawText("计价单位：", leftMargin, firstLineY + lineGap * 3, paint);
        canvas.drawLine(leftMargin + (paint.measureText("计价单位：")), firstLineY + lineGap * 3 + 5, pxW * 0.25f, firstLineY + lineGap * 3 + 5, paint);
        canvas.drawText("物价员：", pxW * 0.26f, firstLineY + lineGap * 3, paint);
        canvas.drawLine(pxW * 0.26f + (paint.measureText("物价员：")), firstLineY + lineGap * 3 + 5, pxW * 0.50f, firstLineY + lineGap * 3 + 5, paint);
        canvas.drawText("等级：", leftMargin, firstLineY + lineGap * 4, paint);
        canvas.drawLine(leftMargin + (paint.measureText("等级：")), firstLineY + lineGap * 4 + 5, pxW * 0.25f, firstLineY + lineGap * 4 + 5, paint);
        canvas.drawText("产地：", pxW * 0.26f, firstLineY + lineGap * 4, paint);
        canvas.drawLine(pxW * 0.26f + (paint.measureText("产地：")), firstLineY + lineGap * 4 + 5, pxW * 0.50f, firstLineY + lineGap * 4 + 5, paint);
        paint.setTextSize(pxH * 0.09f);
        canvas.drawText("零售价：", pxW * 0.53f, pxH * 0.30f, paint);
        canvas.drawText("元", pxW * 0.92f, pxH * 0.30f, paint);
        paint.setTextSize(pxH * 0.07f);
        canvas.drawText("可获得积分：", pxW * 0.53f, pxH * 0.55f, paint);
        canvas.drawText("可用代金券：", pxW * 0.53f, pxH * 0.75f, paint);
        paint.setTextSize(pxH * 0.05f);
        canvas.drawText("明码实价标价签", pxW * 0.75f, pxH * 0.08f, paint);
        canvas.drawText("监督电话：12315", pxW * 0.75f, pxH * 0.95f, paint);
        ivPaperBackground.setImageBitmap(bgBitmap);
    }

    private void saveAllConfigs() {
        UserUtils.getInstance().setLabelConfig(context, "shop_name", cbShopName.isChecked());
        UserUtils.getInstance().setLabelConfig(context, "product_name", cbProductName.isChecked());
        UserUtils.getInstance().setLabelConfig(context, "price", cbPrice.isChecked());
        UserUtils.getInstance().setLabelConfig(context, "barcode", cbBarcode.isChecked());
        UserUtils.getInstance().setLabelConfig(context, "sn", cbSn.isChecked());
        UserUtils.getInstance().setLabelConfig(context, "specs", cbSpecs.isChecked());
        UserUtils.getInstance().setLabelConfig(context, "unit", cbUnit.isChecked());
        UserUtils.getInstance().setLabelConfig(context, "staff", cbStaff.isChecked());
        UserUtils.getInstance().setLabelConfig(context, "level", cbLevel.isChecked());
        UserUtils.getInstance().setLabelConfig(context, "origin", cbOrigin.isChecked());
        UserUtils.getInstance().setLabelConfig(context, "points", cbPoints.isChecked());
        UserUtils.getInstance().setLabelConfig(context, "coupon", cbCoupon.isChecked());

        savePosition(tvShop, "shop");
        savePosition(tvName, "name");
        savePosition(tvPrice, "price");
        savePosition(layoutBarcodeGroup, "barcode");
        savePosition(tvSn, "sn");
        savePosition(tvSpecs, "specs");
        savePosition(tvUnit, "unit");
        savePosition(tvStaff, "staff");
        savePosition(tvLevel, "level");
        savePosition(tvOrigin, "origin");
        savePosition(tvPoints, "points");
        savePosition(tvCoupon, "coupon");
    }

    private void updateCanvasSize(int mmWidth, int mmHeight) {
        final float density = context.getResources().getDisplayMetrics().density;
        final int targetPxWidth = (int) (mmWidth * MM_TO_DP * density);
        final int targetPxHeight = (int) (mmHeight * MM_TO_DP * density);
        ViewGroup.LayoutParams params = layoutPreview.getLayoutParams();
        params.width = targetPxWidth;
        params.height = targetPxHeight;
        layoutPreview.setLayoutParams(params);
        drawFakePrePrintBackground(targetPxWidth, targetPxHeight);
        if (flContainer != null) {
            flContainer.post(() -> {
                int containerW = flContainer.getWidth();
                int containerH = flContainer.getHeight();
                if (containerW == 0 || containerH == 0) return;
                int availableW = containerW - 40;
                int availableH = containerH - 40;
                float scaleX = (float) availableW / targetPxWidth;
                float scaleY = (float) availableH / targetPxHeight;
                float finalScale = Math.min(scaleX, scaleY);
                if (finalScale > 1.0f) finalScale = 1.0f;
                layoutPreview.setPivotX(targetPxWidth / 2f);
                layoutPreview.setPivotY(targetPxHeight / 2f);
                layoutPreview.setScaleX(finalScale);
                layoutPreview.setScaleY(finalScale);
            });
        }
        layoutPreview.requestLayout();
    }

    private void restorePosition(View view, String key) {
        float x = UserUtils.getInstance().getElementX(context, key);
        float y = UserUtils.getInstance().getElementY(context, key);
        if (x != -1 && y != -1) {
            view.setX(x);
            view.setY(y);
        }
    }

    private void savePosition(View view, String key) {
        UserUtils.getInstance().setElementX(context, key, view.getX());
        UserUtils.getInstance().setElementY(context, key, view.getY());
    }

    private void clearFocusBackground(View... views) {
        for (View v : views) {
            if (v != null) v.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void setupCheckListener(CheckBox cb, View target, String configKey) {
        boolean isChecked = UserUtils.getInstance().getLabelConfig(context, configKey, true);
        cb.setChecked(isChecked);
        target.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        cb.setOnCheckedChangeListener((v, isCheckedNow) -> {
            target.setVisibility(isCheckedNow ? View.VISIBLE : View.GONE);
        });
    }

    private Bitmap viewToBitmap(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private Bitmap generateBarcode(String content) {
        try {
            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(EncodeHintType.MARGIN, 0);
            int width = 1000;
            int height = 300;
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