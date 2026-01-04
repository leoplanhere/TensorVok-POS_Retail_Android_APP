package com.uhm.uhmcs.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class ReceiptConfigUtils {
    private static final String PREF_NAME = "receipt_config";
    private static ReceiptConfigUtils instance;
    private SharedPreferences sp;

    public static ReceiptConfigUtils getInstance(Context context) {
        if (instance == null) instance = new ReceiptConfigUtils(context);
        return instance;
    }

    private ReceiptConfigUtils(Context context) {
        sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // --- 配置项 ---

    // 纸张宽度: 0=58mm, 1=80mm
    public int getPaperType() { return sp.getInt("paper_type", 0); }
    public void setPaperType(int type) { sp.edit().putInt("paper_type", type).apply(); }

    // Logo设置
    public boolean showTopLogo() { return sp.getBoolean("show_top_logo", false); }
    public void setShowTopLogo(boolean b) { sp.edit().putBoolean("show_top_logo", b).apply(); }

    public boolean showBottomLogo() { return sp.getBoolean("show_bottom_logo", false); }
    public void setShowBottomLogo(boolean b) { sp.edit().putBoolean("show_bottom_logo", b).apply(); }

    // 基础信息开关
    public boolean showShopName() { return sp.getBoolean("show_shop_name", true); }
    public void setShowShopName(boolean b) { sp.edit().putBoolean("show_shop_name", b).apply(); }

    public boolean showPhone() { return sp.getBoolean("show_phone", true); }
    public void setShowPhone(boolean b) { sp.edit().putBoolean("show_phone", b).apply(); }

    public boolean showCashier() { return sp.getBoolean("show_cashier", true); }
    public void setShowCashier(boolean b) { sp.edit().putBoolean("show_cashier", b).apply(); }

    public boolean showTime() { return sp.getBoolean("show_time", true); }
    public void setShowTime(boolean b) { sp.edit().putBoolean("show_time", b).apply(); }

    public boolean showMember() { return sp.getBoolean("show_member", true); }
    public void setShowMember(boolean b) { sp.edit().putBoolean("show_member", b).apply(); }

    // 底部条码/二维码
    public boolean showBarcode() { return sp.getBoolean("show_barcode", true); }
    public void setShowBarcode(boolean b) { sp.edit().putBoolean("show_barcode", b).apply(); }

    public boolean showQrcode() { return sp.getBoolean("show_qrcode", true); }
    public void setShowQrcode(boolean b) { sp.edit().putBoolean("show_qrcode", b).apply(); }

    public boolean showBottomText() { return sp.getBoolean("show_bottom_text", true); }
    public void setShowBottomText(boolean b) { sp.edit().putBoolean("show_bottom_text", b).apply(); }

    // 【修改】字体大小: 0=小, 1=中(标准), 2=大(老人模式)
    public int getFontSize() { return sp.getInt("font_size", 1); }
    public void setFontSize(int size) { sp.edit().putInt("font_size", size).apply(); }
}