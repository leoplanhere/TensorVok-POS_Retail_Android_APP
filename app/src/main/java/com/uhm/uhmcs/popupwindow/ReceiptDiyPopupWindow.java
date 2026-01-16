package com.uhm.uhmcs.popupwindow;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
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
import com.uhm.uhmcs.utils.UserUtils;

public class ReceiptDiyPopupWindow {
    private PopupWindow popupWindow;
    private Activity context;
    private ImageView ivPreview;
    private CheckoutBean sampleData;

    // 新增：动态输入框变量
    private EditText etHotline, etQrUrl, etFooterNote;

    public ReceiptDiyPopupWindow(Activity context) {
        this.context = context;
        initSampleData();
        init();
    }

    /**
     * 初始化模拟数据：用于预览图展示
     */
    private void initSampleData() {
        sampleData = new CheckoutBean();

        // 获取真实的收银员名称
        String nickname = "管理员";
        try {
            if (UserUtils.getInstance().getLoginBase() != null &&
                    UserUtils.getInstance().getLoginBase().getData() != null) {
                nickname = UserUtils.getInstance().getLoginBase().getData().getUserinfo().getNickname();
            }
        } catch (Exception ignored) {}

        sampleData.setMachineNumber(nickname); // 设置动态收银员
        sampleData.setMember_name("体验会员");
        sampleData.setMember_phone("13800138000");
        sampleData.setTotal_amount("88.00");
        sampleData.setPay_fee("88.00");
        sampleData.setAllNum(2);
        sampleData.setPay_type("alipay");
        sampleData.setGoodsjson("[{\"title\":\"可口可乐\",\"goods_num\":2,\"pay_price\":\"6.00\",\"online_type\":\"normal\",\"goods_price\":\"3.00\"}," +
                "{\"title\":\"统一阿萨姆奶茶经典原味饮料500ml\",\"goods_num\":1,\"pay_price\":\"3.99\",\"online_type\":\"normal\",\"goods_price\":\"3.99\"}]");
    }

    private void init() {
        View view = LayoutInflater.from(context).inflate(R.layout.popupwindow_receipt_diy, null);
        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));

        ivPreview = view.findViewById(R.id.iv_preview);
        ReceiptConfigUtils config = ReceiptConfigUtils.getInstance(context);

        // 1. 初始化输入框并回显已保存的文字
        etHotline = view.findViewById(R.id.et_hotline);
        etQrUrl = view.findViewById(R.id.et_qr_url);
        etFooterNote = view.findViewById(R.id.et_footer_note);

        etHotline.setText(config.getHotline());
        etQrUrl.setText(config.getQrUrl());
        etFooterNote.setText(config.getFooterNote());

        // 2. 纸张规格回显
        RadioGroup rgPaper = view.findViewById(R.id.rg_paper_width);
        if (config.getPaperType() == 0) rgPaper.check(R.id.rb_58mm); else rgPaper.check(R.id.rb_80mm);

        // 3. 字体大小回显
        RadioGroup rgFont = view.findViewById(R.id.rg_font_size);
        int savedFont = config.getFontSize();
        if (savedFont == 0) rgFont.check(R.id.rb_font_small);
        else if (savedFont == 2) rgFont.check(R.id.rb_font_large);
        else rgFont.check(R.id.rb_font_normal);

        // 4. CheckBox 内容控制回显
        CheckBox cbTopLogo = view.findViewById(R.id.cb_top_logo); cbTopLogo.setChecked(config.showTopLogo());
        CheckBox cbShopName = view.findViewById(R.id.cb_shop_name); cbShopName.setChecked(config.showShopName());
        CheckBox cbPhone = view.findViewById(R.id.cb_phone); cbPhone.setChecked(config.showPhone());
        CheckBox cbMember = view.findViewById(R.id.cb_member); cbMember.setChecked(config.showMember());
        CheckBox cbCashier = view.findViewById(R.id.cb_cashier); cbCashier.setChecked(config.showCashier());
        CheckBox cbBarcode = view.findViewById(R.id.cb_barcode); cbBarcode.setChecked(config.showBarcode());
        CheckBox cbQrcode = view.findViewById(R.id.cb_qrcode); cbQrcode.setChecked(config.showQrcode());
        CheckBox cbBottomText = view.findViewById(R.id.cb_bottom_text); cbBottomText.setChecked(config.showBottomText());
        CheckBox cbBottomLogo = view.findViewById(R.id.cb_bottom_logo); cbBottomLogo.setChecked(config.showBottomLogo());

        // 【核心逻辑】统一下发更新并保存配置的方法
        Runnable saveAndRefresh = () -> {
            // 保存所有 CheckBox 和 RadioGroup 状态
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

            // 保存所有 EditText 输入的内容
            config.setHotline(etHotline.getText().toString().trim());
            config.setQrUrl(etQrUrl.getText().toString().trim());
            config.setFooterNote(etFooterNote.getText().toString().trim());

            // 更新预览图
            updatePreview();
        };

        // 绑定 CheckBox 和 RadioGroup 监听
        View.OnClickListener clickListener = v -> saveAndRefresh.run();
        rgPaper.setOnCheckedChangeListener((g, i) -> saveAndRefresh.run());
        rgFont.setOnCheckedChangeListener((g, i) -> saveAndRefresh.run());
        cbTopLogo.setOnClickListener(clickListener);
        cbShopName.setOnClickListener(clickListener);
        cbPhone.setOnClickListener(clickListener);
        cbMember.setOnClickListener(clickListener);
        cbCashier.setOnClickListener(clickListener);
        cbBarcode.setOnClickListener(clickListener);
        cbQrcode.setOnClickListener(clickListener);
        cbBottomText.setOnClickListener(clickListener);
        cbBottomLogo.setOnClickListener(clickListener);

        // 绑定输入框文字实时监听（打字即预览）
        TextWatcher textWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { saveAndRefresh.run(); }
        };
        etHotline.addTextChangedListener(textWatcher);
        etQrUrl.addTextChangedListener(textWatcher);
        etFooterNote.addTextChangedListener(textWatcher);

        // 底部功能按钮
        view.findViewById(R.id.btn_close).setOnClickListener(v -> popupWindow.dismiss());

        view.findViewById(R.id.btn_save).setOnClickListener(v -> {
            saveAndRefresh.run();
            Toast.makeText(context, "设置已生效并保存", Toast.LENGTH_SHORT).show();
            popupWindow.dismiss();
        });

        // 测试打印
        view.findViewById(R.id.btn_test_print).setOnClickListener(v -> {
            // 使用当前配置生成指令
            byte[] printCmds = ReceiptCommandUtils.getReceiptCommands(
                    context,
                    sampleData,
                    "DIY-TEST-888888",
                    "88.00", "0.00", "0.00", "0.00", "0.00"
            );
            if (printCmds != null && printCmds.length > 0) {
                MyPrinterHelper.getInstance().printCommand(context, printCmds);
                Toast.makeText(context, "已发送测试指令", Toast.LENGTH_SHORT).show();
            }
        });

        updatePreview();
    }

    /**
     * 渲染预览图
     */
    private void updatePreview() {
        // ReceiptBitmapGenerator 会从 ConfigUtils 中读取最新的文字和开关
        Bitmap bmp = ReceiptBitmapGenerator.generateReceiptBitmap(context, sampleData, "DIY-TEST-888888");
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