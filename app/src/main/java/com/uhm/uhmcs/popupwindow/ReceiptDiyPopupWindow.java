package com.uhm.uhmcs.popupwindow;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.CheckoutBean;
import com.uhm.uhmcs.utils.MyPrinterHelper;
import com.uhm.uhmcs.utils.ReceiptBitmapGenerator;
import com.uhm.uhmcs.utils.ReceiptCommandUtils;
import com.uhm.uhmcs.utils.ReceiptConfigUtils;

public class ReceiptDiyPopupWindow {
    private PopupWindow popupWindow;
    private Activity context;
    private ImageView ivPreview;
    private CheckoutBean sampleData;

    public ReceiptDiyPopupWindow(Activity context) {
        this.context = context;
        initSampleData();
        init();
    }

    private void initSampleData() {
        sampleData = new CheckoutBean();
        sampleData.setMember_name("体验会员");
        sampleData.setMember_phone("13800138000");
        sampleData.setTotal_amount("88.00");
        sampleData.setAllNum(2);
        sampleData.setPay_type("alipay");
        // 构造模拟商品，测试长品名底部对齐
        sampleData.setGoodsjson("[{\"title\":\"可口可乐\",\"goods_num\":2,\"pay_price\":\"6.00\",\"online_type\":\"normal\",\"goods_price\":\"3.00\"}," +
                "{\"title\":\"统一阿萨姆奶茶经典原味饮料500ml\",\"goods_num\":1,\"pay_price\":\"3.99\",\"online_type\":\"normal\",\"goods_price\":\"3.99\"}]");
    }

    private void init() {
        View view = LayoutInflater.from(context).inflate(R.layout.popupwindow_receipt_diy, null);
        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));

        ivPreview = view.findViewById(R.id.iv_preview);
        ReceiptConfigUtils config = ReceiptConfigUtils.getInstance(context);

        // 1. 纸张规格
        RadioGroup rgPaper = view.findViewById(R.id.rg_paper_width);
        if (config.getPaperType() == 0) rgPaper.check(R.id.rb_58mm); else rgPaper.check(R.id.rb_80mm);

        // 2. 字体大小
        RadioGroup rgFont = view.findViewById(R.id.rg_font_size);
        int savedFont = config.getFontSize();
        if (savedFont == 0) rgFont.check(R.id.rb_font_small);
        else if (savedFont == 2) rgFont.check(R.id.rb_font_large);
        else rgFont.check(R.id.rb_font_normal);

        // 3. 各项内容控制
        CheckBox cbTopLogo = view.findViewById(R.id.cb_top_logo); cbTopLogo.setChecked(config.showTopLogo());
        CheckBox cbShopName = view.findViewById(R.id.cb_shop_name); cbShopName.setChecked(config.showShopName());
        CheckBox cbPhone = view.findViewById(R.id.cb_phone); cbPhone.setChecked(config.showPhone());
        CheckBox cbMember = view.findViewById(R.id.cb_member); cbMember.setChecked(config.showMember());
        CheckBox cbCashier = view.findViewById(R.id.cb_cashier); cbCashier.setChecked(config.showCashier());
        CheckBox cbBarcode = view.findViewById(R.id.cb_barcode); cbBarcode.setChecked(config.showBarcode());
        CheckBox cbQrcode = view.findViewById(R.id.cb_qrcode); cbQrcode.setChecked(config.showQrcode());
        CheckBox cbBottomText = view.findViewById(R.id.cb_bottom_text); cbBottomText.setChecked(config.showBottomText());
        CheckBox cbBottomLogo = view.findViewById(R.id.cb_bottom_logo); cbBottomLogo.setChecked(config.showBottomLogo());

        // 【关键修复】实时监听并更新持久化配置
        View.OnClickListener updateListener = v -> {
            // 将当前 UI 状态写入 SharedPreferences
            config.setPaperType(rgPaper.getCheckedRadioButtonId() == R.id.rb_58mm ? 0 : 1);

            int fontVal = 1;
            int checkedFontId = rgFont.getCheckedRadioButtonId();
            if (checkedFontId == R.id.rb_font_small) fontVal = 0;
            else if (checkedFontId == R.id.rb_font_large) fontVal = 2;
            config.setFontSize(fontVal);

            config.setShowTopLogo(cbTopLogo.isChecked());
            config.setShowShopName(cbShopName.isChecked());
            config.setShowPhone(cbPhone.isChecked());
            config.setShowMember(cbMember.isChecked());
            config.setShowCashier(cbCashier.isChecked());
            config.setShowBarcode(cbBarcode.isChecked());
            config.setShowQrcode(cbQrcode.isChecked());
            config.setShowBottomText(cbBottomText.isChecked());
            config.setShowBottomLogo(cbBottomLogo.isChecked());

            // 只有配置真正写入了，预览生成的图才会变化
            updatePreview();
        };

        // 绑定所有监听
        rgPaper.setOnCheckedChangeListener((g, i) -> updateListener.onClick(null));
        rgFont.setOnCheckedChangeListener((g, i) -> updateListener.onClick(null));
        cbTopLogo.setOnClickListener(updateListener);
        cbShopName.setOnClickListener(updateListener);
        cbPhone.setOnClickListener(updateListener);
        cbMember.setOnClickListener(updateListener);
        cbCashier.setOnClickListener(updateListener);
        cbBarcode.setOnClickListener(updateListener);
        cbQrcode.setOnClickListener(updateListener);
        cbBottomText.setOnClickListener(updateListener);
        cbBottomLogo.setOnClickListener(updateListener);

        // 按钮逻辑
        view.findViewById(R.id.btn_close).setOnClickListener(v -> popupWindow.dismiss());

        view.findViewById(R.id.btn_save).setOnClickListener(v -> {
            // 这里其实在 updateListener 里已经保存了，此处只需提示并关闭
            Toast.makeText(context, "设置已生效并保存", Toast.LENGTH_SHORT).show();
            popupWindow.dismiss();
        });

        // 测试打印
        view.findViewById(R.id.btn_test_print).setOnClickListener(v -> {
            // ⭐ 传入模拟的支付金额（现金88.00，其余0.00），以对齐 8 个参数
            byte[] printCmds = ReceiptCommandUtils.getReceiptCommands(
                    context,
                    sampleData,
                    "DIY-TEST-666888",
                    "88.00", // 模拟现金支付金额
                    "0.00",  // 模拟微信
                    "0.00",  // 模拟支付宝
                    "0.00",  // 模拟会员卡
                    "0.00"   // 模拟NETS
            );
            if (printCmds != null && printCmds.length > 0) {
                MyPrinterHelper.getInstance().printCommand(context, printCmds);
                Toast.makeText(context, "正在发送测试小票...", Toast.LENGTH_SHORT).show();
            }
        });

        updatePreview();
    }

    private void updatePreview() {
        // ReceiptBitmapGenerator 内部会调用 config.showMember() 等，所以配置必须实时保存
        Bitmap bmp = ReceiptBitmapGenerator.generateReceiptBitmap(context, sampleData, "DIY-TEST-666888");
        if (bmp != null) {
            ivPreview.setImageBitmap(bmp);
        }
    }

    public void show() {
        if (context != null && !context.isFinishing()) {
            popupWindow.showAtLocation(context.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
        }
    }
}